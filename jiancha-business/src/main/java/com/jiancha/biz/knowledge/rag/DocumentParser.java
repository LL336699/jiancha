package com.jiancha.biz.knowledge.rag;

import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 文档解析器（M2）
 *
 * <p>统一入口 Tika {@link AutoDetectParser}，覆盖 PDF / DOCX / XLSX / PPTX / TXT / MD 等格式；
 * 解析在独立线程中执行并受超时约束——<b>超限标记为"解析失败（需人工处理）"，不阻塞入库队列</b>。</p>
 *
 * <p>设计取舍：扫描件 PDF 的 OCR <b>不在此处实现</b>。Tika 内嵌 OCR 依赖重、易卡死，
 * 设计文档已明确"走独立 OCR 服务"。此处仅做文本型 PDF 的提取；扫描件会解析出空文本，
 * 由上层识别并给出明确提示。</p>
 *
 * @author 小标
 */
@Component
public class DocumentParser {

    private static final Logger log = LoggerFactory.getLogger(DocumentParser.class);

    /** 解析超时（秒），超限标记需人工处理 */
    @Value("${jiancha.ai.parse-timeout-seconds:30}")
    private int parseTimeoutSeconds;

    /** 单次提取的最大字符数（Tika 安全上限，防超大文档撑爆内存） */
    private static final int MAX_CHARS = 2_000_000;

    /** 解析结果 */
    public static class ParseResult {
        private final String text;
        private final int pageCount;
        private final boolean empty;
        private final String error;

        public ParseResult(String text, int pageCount, boolean empty, String error) {
            this.text = text;
            this.pageCount = pageCount;
            this.empty = empty;
            this.error = error;
        }

        public String getText() {
            return text;
        }

        public int getPageCount() {
            return pageCount;
        }

        /** 是否提取到空文本（多为扫描件 / 图片型 PDF） */
        public boolean isEmpty() {
            return empty;
        }

        /** 解析错误信息，正常为 null */
        public String getError() {
            return error;
        }

        public boolean isSuccess() {
            return error == null && !empty;
        }
    }

    /**
     * 解析文档正文。
     *
     * @param in       文件输入流（由调用方负责关闭）
     * @param fileName 原始文件名（Tika 借此辅助格式判定）
     * @return 解析结果，永不返回 null；失败时 {@link ParseResult#getError()} 有值
     */
    public ParseResult parse(InputStream in, String fileName) {
        ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "tika-parse");
            t.setDaemon(true);
            return t;
        });
        try {
            Future<ParseResult> future = executor.submit((Callable<ParseResult>) () -> doParse(in, fileName));
            return future.get(parseTimeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.warn("文档解析超时（{}s）：{}", parseTimeoutSeconds, fileName);
            return new ParseResult("", 0, false,
                    "解析超时（超过 " + parseTimeoutSeconds + " 秒），已标记为需人工处理");
        } catch (Exception e) {
            log.warn("文档解析失败：{} —— {}", fileName, e.getMessage());
            return new ParseResult("", 0, false, "解析失败：" + e.getMessage());
        } finally {
            executor.shutdownNow();
        }
    }

    private ParseResult doParse(InputStream in, String fileName) throws Exception {
        Metadata metadata = new Metadata();
        metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, fileName);

        BodyContentHandler handler = new BodyContentHandler(MAX_CHARS);
        ParseContext context = new ParseContext();
        AutoDetectParser parser = new AutoDetectParser();
        parser.parse(in, handler, metadata, context);

        String text = handler.toString();
        int pageCount = parsePageCount(metadata);

        // 归一化：压缩连续空行，去除零宽字符
        String cleaned = clean(text);

        if (cleaned.trim().isEmpty()) {
            return new ParseResult("", pageCount, true,
                    "未提取到文本内容，可能是扫描件或图片型文档，需人工处理或接入 OCR");
        }
        return new ParseResult(cleaned, pageCount, false, null);
    }

    /** 从元数据读取页数（PDF 等文档才有） */
    private int parsePageCount(Metadata metadata) {
        try {
            String v = metadata.get("xmpTPg:NPages");
            if (v == null) {
                v = metadata.get("meta:page-count");
            }
            return v == null ? 0 : Integer.parseInt(v.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 文本清洗：统一换行、压缩空白、去除零宽与 BOM。
     */
    private String clean(String raw) {
        if (raw == null) {
            return "";
        }
        return raw
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replace("\uFEFF", "")        // BOM
                .replace("\u200B", "")        // 零宽空格
                .replace("\u00A0", " ")       // 不间断空格
                .replaceAll("[ \t]+\n", "\n") // 行尾空白
                .replaceAll("\n{3,}", "\n\n") // 连续空行压缩
                .trim();
    }
}
