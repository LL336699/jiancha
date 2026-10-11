package com.jiancha.biz.knowledge.rag;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.jiancha.biz.knowledge.domain.JcKbChunk;

/**
 * 结构感知分块器
 *
 * <p><b>为什么不直接用固定长度分块：</b>法规条文若被按字数硬切，会出现"前半句在 A 块、
 * 后半句在 B 块"的语义断裂，检索命中后拼出的出处根本不成立。纪检场景要求
 * "引用必须能对回原文条款"，因此必须按文档自身的结构边界切分。</p>
 *
 * <p>分块策略（与设计文档 7.2 对齐）：</p>
 * <table border="1">
 *   <tr><th>文档类型</th><th>策略</th><th>目标大小</th></tr>
 *   <tr><td>法规</td><td>按"条"切，一条一块，不跨条</td><td>200-1500 字</td></tr>
 *   <tr><td>制度</td><td>按"章/条"两级，大章按小节切</td><td>500-1000 字</td></tr>
 *   <tr><td>案例</td><td>按"基本案情/处理结果/警示要点"段切</td><td>800-1500 字</td></tr>
 *   <tr><td>通用</td><td>递归字符分块，重叠 50 字</td><td>500-1000 字</td></tr>
 * </table>
 *
 * <p>每块强制携带 {@code clauseNo} / {@code pageNo} / {@code charStart} / {@code charEnd}——
 * 这是回答能带出处的<b>前提条件</b>。</p>
 *
 * @author 小标
 */
@Component
public class StructureAwareChunker {

    /** 条文起始标记：第X条 / 第X条之N */
    private static final Pattern CLAUSE_PATTERN =
            Pattern.compile("(?m)^\\s*(第[一二三四五六七八九十百零〇0-9]+条(?:之[一二三四五六七八九十]+)?)");

    /** 章节标记：第X章 / 第X节 */
    private static final Pattern CHAPTER_PATTERN =
            Pattern.compile("(?m)^\\s*(第[一二三四五六七八九十百零〇0-9]+(章|节))\\s*(.*)$");

    /** 案例分段标记 */
    private static final Pattern CASE_SECTION_PATTERN =
            Pattern.compile("(?m)^\\s*(基本案情|案情简介|处理结果|处理情况|警示[要意]点?|案例点评|评析)[：:]*\\s*$");

    /** 通用分块目标大小（字符） */
    private static final int DEFAULT_CHUNK_SIZE = 800;

    /** 通用分块重叠（字符） */
    private static final int DEFAULT_OVERLAP = 50;

    /** 单块最大字符数（超长条文强制再切，避免超出 embedding 窗口） */
    private static final int MAX_CHUNK_SIZE = 1500;

    /** 过短块的合并阈值：低于此值的相邻块尝试合并 */
    private static final int MIN_CHUNK_SIZE = 100;

    /**
     * 按文档类型分块。
     *
     * @param text    解析后的纯文本
     * @param docType 文档类型（法规 / 制度 / 案例 / 通用），由调用方判定
     * @return 分块列表（已填充 clauseNo / charStart / charEnd / chunkIndex）
     */
    public List<JcKbChunk> chunk(String text, DocCategory docType) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }
        // 统一换行，避免 \r\n 干扰偏移计算
        String normalized = text.replace("\r\n", "\n").replace("\r", "\n");

        List<JcKbChunk> chunks;
        switch (docType) {
            case REGULATION:
                chunks = chunkByClause(normalized);
                break;
            case POLICY:
                chunks = chunkByChapterClause(normalized);
                break;
            case CASE:
                chunks = chunkByCaseSection(normalized);
                break;
            default:
                chunks = chunkByRecursive(normalized);
        }

        // 超长块二次切分 + 过短块合并 + 序号重排
        chunks = postProcess(chunks, normalized);
        for (int i = 0; i < chunks.size(); i++) {
            chunks.get(i).setChunkIndex(i);
        }
        return chunks;
    }

    /** 默认分块（通用递归） */
    public List<JcKbChunk> chunk(String text) {
        return chunk(text, DocCategory.GENERAL);
    }

    // ══════════════════ 策略实现 ══════════════════

    /**
     * 法规：按"条"切分。条文是法规的最小引用单位，一条一块保证出处可对回原文。
     */
    private List<JcKbChunk> chunkByClause(String text) {
        List<JcKbChunk> result = new ArrayList<>();
        Matcher m = CLAUSE_PATTERN.matcher(text);

        List<int[]> bounds = new ArrayList<>();   // [start, end]
        List<String> labels = new ArrayList<>();
        while (m.find()) {
            bounds.add(new int[]{m.start(), m.end()});
            labels.add(m.group(1));
        }

        if (bounds.isEmpty()) {
            // 未识别到条文结构，退化为通用分块
            return chunkByRecursive(text);
        }

        // 条文之前的前言（如"第一章 总则"标题、发布说明）单独成块
        if (bounds.get(0)[0] > 0) {
            String preamble = text.substring(0, bounds.get(0)[0]).trim();
            if (!preamble.isEmpty()) {
                result.add(buildChunk(preamble, null, 0, bounds.get(0)[0], "前言"));
            }
        }

        for (int i = 0; i < bounds.size(); i++) {
            int start = bounds.get(i)[0];
            int end = (i + 1 < bounds.size()) ? bounds.get(i + 1)[0] : text.length();
            String content = text.substring(start, end).trim();
            if (!content.isEmpty()) {
                result.add(buildChunk(content, labels.get(i), start, end, null));
            }
        }
        return result;
    }

    /**
     * 制度：按"章/条"两级。大章（超过 MAX_CHUNK_SIZE）再按条切，小章整章成块。
     */
    private List<JcKbChunk> chunkByChapterClause(String text) {
        List<JcKbChunk> result = new ArrayList<>();
        Matcher mc = CHAPTER_PATTERN.matcher(text);

        List<int[]> chapterBounds = new ArrayList<>();
        List<String> chapterLabels = new ArrayList<>();
        while (mc.find()) {
            chapterBounds.add(new int[]{mc.start(), mc.end()});
            chapterLabels.add(mc.group(1) + " " + mc.group(3));
        }

        if (chapterBounds.isEmpty()) {
            // 无章结构，退化为按条
            return chunkByClause(text);
        }

        for (int i = 0; i < chapterBounds.size(); i++) {
            int start = chapterBounds.get(i)[0];
            int end = (i + 1 < chapterBounds.size()) ? chapterBounds.get(i + 1)[0] : text.length();
            String chapterText = text.substring(start, end).trim();
            String chapterLabel = chapterLabels.get(i);

            if (chapterText.length() <= MAX_CHUNK_SIZE) {
                // 小章整章成块
                if (!chapterText.isEmpty()) {
                    result.add(buildChunk(chapterText, chapterLabel, start, end, null));
                }
            } else {
                // 大章按条切
                List<JcKbChunk> sub = chunkByClause(chapterText);
                for (JcKbChunk c : sub) {
                    // 偏移回填到全文坐标系
                    c.setCharStart(safeAdd(c.getCharStart(), start));
                    c.setCharEnd(safeAdd(c.getCharEnd(), start));
                    if (c.getClauseNo() == null || c.getClauseNo().isEmpty()) {
                        c.setClauseNo(chapterLabel);
                    } else {
                        c.setClauseNo(chapterLabel + " " + c.getClauseNo());
                    }
                    result.add(c);
                }
            }
        }
        return result;
    }

    /**
     * 案例：按"基本案情 / 处理结果 / 警示要点"等语义段切，一段一块。
     */
    private List<JcKbChunk> chunkByCaseSection(String text) {
        List<JcKbChunk> result = new ArrayList<>();
        Matcher m = CASE_SECTION_PATTERN.matcher(text);

        List<int[]> bounds = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        while (m.find()) {
            bounds.add(new int[]{m.start(), m.end()});
            labels.add(m.group(1));
        }

        if (bounds.isEmpty()) {
            // 未识别到案例结构，退化通用分块
            return chunkByRecursive(text);
        }

        if (bounds.get(0)[0] > 0) {
            String head = text.substring(0, bounds.get(0)[0]).trim();
            if (!head.isEmpty()) {
                result.add(buildChunk(head, null, 0, bounds.get(0)[0], "案例概况"));
            }
        }

        for (int i = 0; i < bounds.size(); i++) {
            int start = bounds.get(i)[0];
            int end = (i + 1 < bounds.size()) ? bounds.get(i + 1)[0] : text.length();
            String content = text.substring(start, end).trim();
            if (!content.isEmpty()) {
                result.add(buildChunk(content, null, start, end, labels.get(i)));
            }
        }
        return result;
    }

    /**
     * 通用：递归字符分块（优先在段落 / 句号边界断开），重叠 50 字。
     */
    private List<JcKbChunk> chunkByRecursive(String text) {
        List<JcKbChunk> result = new ArrayList<>();
        int len = text.length();
        int pos = 0;

        while (pos < len) {
            int end = Math.min(pos + DEFAULT_CHUNK_SIZE, len);

            // 未到文末则寻找合适的断点：段落 → 句号 → 分号 → 逗号 → 强制
            if (end < len) {
                int boundary = findBoundary(text, pos, end);
                if (boundary > pos) {
                    end = boundary;
                }
            }

            String content = text.substring(pos, end).trim();
            if (!content.isEmpty()) {
                result.add(buildChunk(content, null, pos, end, null));
            }

            if (end >= len) {
                break;
            }
            // 前进：整块长度减去重叠，避免死循环
            pos = Math.max(pos + 1, end - DEFAULT_OVERLAP);
        }
        return result;
    }

    /** 在 [from, to] 区间内从后向前寻找自然断点 */
    private int findBoundary(String text, int from, int to) {
        // 优先段落边界
        int candidate = text.lastIndexOf("\n\n", to);
        if (candidate > from + MIN_CHUNK_SIZE) {
            return candidate + 2;
        }
        // 其次句末标点
        for (String p : new String[]{"。", "！", "？", "；", "\n"}) {
            candidate = text.lastIndexOf(p, to);
            if (candidate > from + MIN_CHUNK_SIZE) {
                return candidate + p.length();
            }
        }
        return to;
    }

    // ══════════════════ 后处理 ══════════════════

    /**
     * 后处理：超长块二次切分、过短块合并、内容哈希计算。
     */
    private List<JcKbChunk> postProcess(List<JcKbChunk> chunks, String originText) {
        List<JcKbChunk> out = new ArrayList<>();

        for (JcKbChunk c : chunks) {
            if (c.getContent().length() > MAX_CHUNK_SIZE) {
                // 超长块按段落二次切分，clauseNo 保留（同一"条"内的多个块共用条款号）
                out.addAll(splitOversize(c));
            } else {
                out.add(c);
            }
        }

        // 过短块与相邻块合并（仅当两块同源且合并后不超限）
        List<JcKbChunk> merged = new ArrayList<>();
        for (JcKbChunk c : out) {
            if (!merged.isEmpty()) {
                JcKbChunk last = merged.get(merged.size() - 1);
                boolean tooShort = last.getContent().length() < MIN_CHUNK_SIZE;
                boolean sameClause = java.util.Objects.equals(last.getClauseNo(), c.getClauseNo());
                boolean fits = last.getContent().length() + c.getContent().length() <= MAX_CHUNK_SIZE;
                if (tooShort && sameClause && fits) {
                    last.setContent(last.getContent() + "\n" + c.getContent());
                    last.setCharEnd(c.getCharEnd());
                    continue;
                }
            }
            merged.add(c);
        }

        // 计算内容哈希（变更检测用）
        for (JcKbChunk c : merged) {
            c.setContentHash(sha256(c.getContent()));
        }
        return merged;
    }

    /** 超长块按段落边界二次切分 */
    private List<JcKbChunk> splitOversize(JcKbChunk src) {
        List<JcKbChunk> result = new ArrayList<>();
        String content = src.getContent();
        int base = src.getCharStart() == null ? 0 : src.getCharStart();
        int pos = 0;

        while (pos < content.length()) {
            int end = Math.min(pos + DEFAULT_CHUNK_SIZE, content.length());
            if (end < content.length()) {
                int b = content.lastIndexOf("\n\n", end);
                if (b <= pos + MIN_CHUNK_SIZE) {
                    b = content.lastIndexOf("。", end);
                }
                if (b > pos + MIN_CHUNK_SIZE) {
                    end = b + 1;
                }
            }
            String piece = content.substring(pos, end).trim();
            if (!piece.isEmpty()) {
                JcKbChunk c = new JcKbChunk();
                c.setContent(piece);
                c.setClauseNo(src.getClauseNo());
                c.setPageNo(src.getPageNo());
                c.setCharStart(base + pos);
                c.setCharEnd(base + end);
                result.add(c);
            }
            pos = Math.max(pos + 1, end - DEFAULT_OVERLAP);
        }
        return result;
    }

    // ══════════════════ 工具方法 ══════════════════

    private JcKbChunk buildChunk(String content, String clauseNo, int start, int end, String sectionLabel) {
        JcKbChunk c = new JcKbChunk();
        c.setContent(content);
        c.setClauseNo(clauseNo != null ? clauseNo : sectionLabel);
        c.setCharStart(start);
        c.setCharEnd(end);
        return c;
    }

    private Integer safeAdd(Integer v, int delta) {
        return v == null ? null : v + delta;
    }

    /** 计算 SHA-256（内容变更检测） */
    private String sha256(String s) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 文档语义类别（决定分块策略）。
     */
    public enum DocCategory {
        /** 法规（国家法律、党纪法规）——按条切 */
        REGULATION,
        /** 制度（企业规章、内部制度）——按章/条切 */
        POLICY,
        /** 案例（典型案例、警示教育）——按语义段切 */
        CASE,
        /** 通用文档——递归切 */
        GENERAL;

        /**
         * 由分类类型推断分块策略。
         *
         * <p>取值与字典 {@code jc_kb_cat_type} 严格对齐：
         * 国法 / 党纪 → 按条切；规章 / 制度 → 按章条切；案例 / 警示 → 按语义段切。</p>
         *
         * @param catType 字典 jc_kb_cat_type 的值
         */
        public static DocCategory fromCatType(String catType) {
            if (catType == null || catType.isEmpty()) {
                return GENERAL;
            }
            switch (catType) {
                case "国法":
                case "党纪":
                    return REGULATION;
                case "规章":
                case "制度":
                    return POLICY;
                case "案例":
                case "警示":
                    return CASE;
                default:
                    return GENERAL;
            }
        }
    }
}
