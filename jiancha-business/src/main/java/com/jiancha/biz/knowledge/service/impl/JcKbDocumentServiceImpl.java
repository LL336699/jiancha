package com.jiancha.biz.knowledge.service.impl;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.jiancha.biz.knowledge.domain.JcKbCategory;
import com.jiancha.biz.knowledge.domain.JcKbChunk;
import com.jiancha.biz.knowledge.domain.JcKbDocument;
import com.jiancha.biz.knowledge.mapper.JcKbCategoryMapper;
import com.jiancha.biz.knowledge.mapper.JcKbChunkMapper;
import com.jiancha.biz.knowledge.mapper.JcKbDocumentMapper;
import com.jiancha.biz.knowledge.rag.DocumentParser;
import com.jiancha.biz.knowledge.rag.StructureAwareChunker;
import com.jiancha.biz.knowledge.rag.VectorIndexer;
import com.jiancha.biz.knowledge.service.IJcKbDocumentService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * 知识库文档 Service 实现
 *
 * <p><b>入库链路</b>（设计文档 7.1）：</p>
 * <pre>
 *   上传 → SHA-256 去重 → 落盘 → Tika 解析 → 结构感知分块
 *        → 写 jc_kb_chunk → Embedding 向量化 → 写 Chroma
 * </pre>
 *
 * <p>状态流转：待处理 → 解析中 → 已入库 / 解析失败。任一环节异常均落地
 * {@code parse_error}，前端可据此重试，不会出现"卡在处理中"的僵尸文档。</p>
 *
 * @author 小标
 */
@Service
public class JcKbDocumentServiceImpl implements IJcKbDocumentService {

    private static final Logger log = LoggerFactory.getLogger(JcKbDocumentServiceImpl.class);

    /** 状态常量（与字典 jc_kb_doc_status 严格一致） */
    private static final String ST_PENDING = "待处理";
    private static final String ST_PARSING = "解析中";
    private static final String ST_READY = "已入库";
    private static final String ST_FAILED = "解析失败";

    @Autowired
    private JcKbDocumentMapper documentMapper;

    @Autowired
    private JcKbChunkMapper chunkMapper;

    @Autowired
    private JcKbCategoryMapper categoryMapper;

    @Autowired
    private DocumentParser documentParser;

    @Autowired
    private StructureAwareChunker chunker;

    @Autowired
    private VectorIndexer vectorIndexer;

    /** 文件存储根目录 */
    @Value("${jiancha.ai.upload-dir:./uploads/knowledge}")
    private String uploadDir;

    /** 单次上传最大尺寸（MB） */
    @Value("${jiancha.ai.max-upload-size-mb:50}")
    private int maxUploadSizeMb;

    // ══════════════════ 查询 ══════════════════

    @Override
    public List<JcKbDocument> selectJcKbDocumentList(JcKbDocument document) {
        return documentMapper.selectJcKbDocumentList(document);
    }

    @Override
    public JcKbDocument selectJcKbDocumentById(Long docId) {
        return documentMapper.selectJcKbDocumentById(docId);
    }

    @Override
    public List<JcKbChunk> selectChunksByDocId(Long docId) {
        return chunkMapper.selectChunksByDocId(docId);
    }

    // ══════════════════ 修改 ══════════════════

    @Override
    public int updateJcKbDocument(JcKbDocument document) {
        return documentMapper.updateJcKbDocument(document);
    }

    @Override
    @Transactional
    public int deleteJcKbDocumentById(Long docId) {
        // 向量必须先删——否则检索会命中已删除文档的残留块
        vectorIndexer.removeByDocId(docId);
        chunkMapper.deleteChunksByDocId(docId);
        return documentMapper.deleteJcKbDocumentById(docId);
    }

    @Override
    @Transactional
    public int deleteJcKbDocumentByIds(Long[] docIds) {
        int rows = 0;
        for (Long id : docIds) {
            rows += deleteJcKbDocumentById(id);
        }
        return rows;
    }

    // ══════════════════ 入库编排 ══════════════════

    @Override
    public Map<String, Object> uploadAndIngest(MultipartFile file, Long categoryId,
                                               JcKbDocument meta, String operator) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("上传文件为空");
        }
        long sizeLimit = (long) maxUploadSizeMb * 1024 * 1024;
        if (file.getSize() > sizeLimit) {
            throw new ServiceException("文件超过大小上限 " + maxUploadSizeMb + "MB");
        }

        // ① 哈希去重：同一份文件重复上传直接复用已有记录
        String hash = sha256(file);
        JcKbDocument exist = documentMapper.selectByFileHash(hash);
        if (exist != null) {
            Map<String, Object> dup = new LinkedHashMap<>();
            dup.put("duplicated", true);
            dup.put("docId", exist.getDocId());
            dup.put("docName", exist.getDocName());
            dup.put("chunkCount", exist.getChunkCount());
            dup.put("status", exist.getStatus());
            dup.put("message", "该文件已入库，已跳过重复处理");
            return dup;
        }

        // ② 落盘 + 建记录
        JcKbDocument doc = new JcKbDocument();
        doc.setDocName(file.getOriginalFilename());
        doc.setCategoryId(categoryId);
        doc.setFileHash(hash);
        doc.setFileSize(file.getSize());
        doc.setDocType(extractExt(file.getOriginalFilename()));
        doc.setStatus(ST_PENDING);
        doc.setChunkCount(0);
        doc.setCreateBy(operator);
        if (meta != null) {
            doc.setVersion(StringUtils.isEmpty(meta.getVersion()) ? "v1" : meta.getVersion());
            doc.setIssuer(meta.getIssuer());
            doc.setDocNo(meta.getDocNo());
            doc.setEffectiveDate(meta.getEffectiveDate());
            doc.setExpireDate(meta.getExpireDate());
            doc.setRemark(meta.getRemark());
        }
        String storedPath = storeFile(file, hash);
        doc.setFilePath(storedPath);
        documentMapper.insertJcKbDocument(doc);

        // ③ 解析 + 分块 + 向量化
        return ingest(doc, operator);
    }

    @Override
    public Map<String, Object> reingest(Long docId, String operator) {
        JcKbDocument doc = documentMapper.selectJcKbDocumentById(docId);
        if (doc == null) {
            throw new ServiceException("文档不存在：" + docId);
        }
        // 清掉旧分块与旧向量，避免重复堆积
        vectorIndexer.removeByDocId(docId);
        chunkMapper.physicalDeleteByDocId(docId);
        return ingest(doc, operator);
    }

    /**
     * 解析 → 分块 → 落库 → 向量化。
     */
    private Map<String, Object> ingest(JcKbDocument doc, String operator) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("duplicated", false);
        result.put("docId", doc.getDocId());
        result.put("docName", doc.getDocName());

        documentMapper.updateStatus(doc.getDocId(), ST_PARSING, 0, null);

        try {
            // ① 解析
            DocumentParser.ParseResult parsed;
            try (InputStream in = openStoredFile(doc.getFilePath())) {
                parsed = documentParser.parse(in, doc.getDocName());
            } catch (Exception e) {
                fail(doc, "读取文件失败：" + e.getMessage());
                result.put("status", ST_FAILED);
                result.put("parseError", "读取文件失败：" + e.getMessage());
                return result;
            }

            if (!parsed.isSuccess()) {
                fail(doc, parsed.getError());
                result.put("status", ST_FAILED);
                result.put("parseError", parsed.getError());
                return result;
            }

            // ② 分块（策略由分类类型决定）
            JcKbCategory category = categoryMapper.selectJcKbCategoryById(doc.getCategoryId());
            StructureAwareChunker.DocCategory docCategory =
                    StructureAwareChunker.DocCategory.fromCatType(category == null ? null : category.getCatType());
            List<JcKbChunk> chunks = chunker.chunk(parsed.getText(), docCategory);

            if (chunks.isEmpty()) {
                fail(doc, "分块结果为空，请检查文档内容");
                result.put("status", ST_FAILED);
                result.put("parseError", "分块结果为空");
                return result;
            }

            // ③ 落库
            for (JcKbChunk c : chunks) {
                c.setDocId(doc.getDocId());
                c.setDocName(doc.getDocName());
                c.setDocType(doc.getDocType());
                c.setCreateBy(operator);
            }
            chunkMapper.batchInsertChunks(chunks);

            // ④ 向量化（未就绪时降级为关键词模式，不阻断入库）
            int indexed = vectorIndexer.index(chunks);

            documentMapper.updateStatus(doc.getDocId(), ST_READY, chunks.size(), null);

            result.put("status", ST_READY);
            result.put("chunkCount", chunks.size());
            result.put("vectorCount", indexed);
            result.put("vectorEnabled", vectorIndexer.isReady());
            result.put("docCategory", docCategory.name());
            result.put("pageCount", parsed.getPageCount());
            result.put("message", indexed > 0
                    ? "入库完成，已建立向量索引"
                    : "入库完成；向量库未就绪，当前仅支持关键词检索");
            log.info("文档入库完成 docId={} 分块={} 向量={}", doc.getDocId(), chunks.size(), indexed);
            return result;

        } catch (Exception e) {
            log.error("文档入库异常 docId={}：{}", doc.getDocId(), e.getMessage(), e);
            String msg = "入库异常：" + e.getMessage();
            fail(doc, truncate(msg));
            result.put("status", ST_FAILED);
            result.put("parseError", truncate(msg));
            return result;
        }
    }

    // ══════════════════ 统计 ══════════════════

    @Override
    public Map<String, Object> statOverview() {
        Map<String, Object> stat = new HashMap<>();
        stat.put("docTotal", documentMapper.countAll());
        stat.put("docReady", documentMapper.countByStatus(ST_READY));
        stat.put("docPending", documentMapper.countByStatus(ST_PENDING));
        stat.put("docParsing", documentMapper.countByStatus(ST_PARSING));
        stat.put("docFailed", documentMapper.countByStatus(ST_FAILED));
        stat.put("chunkTotal", chunkMapper.countAll());
        stat.put("vectorEnabled", vectorIndexer.isReady());
        return stat;
    }

    // ══════════════════ 内部工具 ══════════════════

    private void fail(JcKbDocument doc, String error) {
        documentMapper.updateStatus(doc.getDocId(), ST_FAILED, 0, truncate(error));
        log.warn("文档处理失败 docId={}：{}", doc.getDocId(), error);
    }

    private String truncate(String s) {
        if (s == null) {
            return null;
        }
        return s.length() > 900 ? s.substring(0, 900) + "…" : s;
    }

    /** 计算文件 SHA-256 */
    private String sha256(MultipartFile file) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (InputStream in = file.getInputStream()) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) != -1) {
                    md.update(buf, 0, n);
                }
            }
            StringBuilder sb = new StringBuilder();
            for (byte b : md.digest()) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new ServiceException("计算文件哈希失败：" + e.getMessage());
        }
    }

    /** 落盘，返回相对路径 */
    private String storeFile(MultipartFile file, String hash) {
        try {
            java.io.File dir = new java.io.File(uploadDir);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new ServiceException("创建上传目录失败：" + dir.getAbsolutePath());
            }
            String ext = extractExt(file.getOriginalFilename());
            String fileName = hash.substring(0, 16) + (ext.isEmpty() ? "" : "." + ext);
            java.io.File target = new java.io.File(dir, fileName);
            file.transferTo(target.getAbsoluteFile());
            return target.getPath();
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("文件落盘失败：" + e.getMessage());
        }
    }

    private InputStream openStoredFile(String path) throws Exception {
        return new java.io.FileInputStream(new java.io.File(path));
    }

    /** 提取扩展名（小写，不含点） */
    private String extractExt(String fileName) {
        if (fileName == null) {
            return "";
        }
        int i = fileName.lastIndexOf('.');
        return (i < 0) ? "" : fileName.substring(i + 1).toLowerCase();
    }

    /** 供外部（Controller）判断向量能力 */
    public boolean isVectorReady() {
        return vectorIndexer.isReady();
    }
}
