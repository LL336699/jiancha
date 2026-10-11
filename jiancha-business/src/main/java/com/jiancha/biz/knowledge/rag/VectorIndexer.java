package com.jiancha.biz.knowledge.rag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.jiancha.biz.knowledge.domain.JcKbChunk;

/**
 * 向量化写入器
 *
 * <p>把分块写入向量库。每个向量块携带<b>完整出处元数据</b>
 * （chunk_id / doc_id / doc_name / clause_no / page_no），
 * 检索命中后无需二次查询即可拼出引用来源。</p>
 *
 * <p>关键约束：向量库若不可用（未配置 Embedding 模型或 Chroma 未启用），
 * <b>不抛异常终止入库</b>——分块仍写入 MySQL，关键词检索依然可用，
 * 只是降级为"纯关键词模式"。这保证知识库功能在 Embedding 未就绪时仍可交付使用。</p>
 *
 * @author 小标
 */
@Component
public class VectorIndexer {

    private static final Logger log = LoggerFactory.getLogger(VectorIndexer.class);

    @Autowired(required = false)
    @Qualifier("jianchaVectorStore")
    private VectorStore vectorStore;

    @Autowired(required = false)
    private EmbeddingModel embeddingModel;

    /**
     * 向量库是否真正就绪（既要有 store，也要能算 embedding）。
     */
    public boolean isReady() {
        return vectorStore != null && embeddingModel != null;
    }

    /**
     * 写入文档的全部分块。
     *
     * @param chunks 分块列表（须已设置 chunkId）
     * @return 实际写入的向量数；未就绪时返回 0
     */
    public int index(List<JcKbChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return 0;
        }
        if (!isReady()) {
            log.warn("向量库未就绪（store={}, embeddingModel={}），跳过向量化，仅保留关键词检索",
                    vectorStore != null, embeddingModel != null);
            return 0;
        }

        try {
            List<Document> docs = new ArrayList<>(chunks.size());
            for (JcKbChunk c : chunks) {
                docs.add(toVectorDocument(c));
            }
            vectorStore.add(docs);
            log.info("向量化完成，写入 {} 个分块", docs.size());
            return docs.size();
        } catch (Exception e) {
            log.error("向量化写入失败（分块已入库，检索将降级为关键词模式）：{}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 删除某文档的全部向量。
     *
     * <p>文档删除时必须同步删除向量，否则检索会命中"已删除文档"的残留块。</p>
     */
    public int removeByDocId(Long docId) {
        if (!isReady()) {
            return 0;
        }
        try {
            // 通过元数据过滤删除（Chroma 支持 where 条件删除）
            vectorStore.delete("doc_id == '" + docId + "'");
            log.info("已删除文档 {} 的向量", docId);
            return 1;
        } catch (Exception e) {
            log.warn("删除文档 {} 向量失败：{}", docId, e.getMessage());
            return 0;
        }
    }

    /**
     * 构造向量文档：正文 + 引用所需元数据。
     */
    private Document toVectorDocument(JcKbChunk c) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("chunk_id", c.getChunkId());
        meta.put("doc_id", c.getDocId());
        meta.put("chunk_index", c.getChunkIndex());
        if (c.getDocName() != null) {
            meta.put("doc_name", c.getDocName());
        }
        if (c.getClauseNo() != null && !c.getClauseNo().isEmpty()) {
            meta.put("clause_no", c.getClauseNo());
        }
        if (c.getPageNo() != null) {
            meta.put("page_no", c.getPageNo());
        }
        if (c.getCharStart() != null) {
            meta.put("char_start", c.getCharStart());
        }
        if (c.getCharEnd() != null) {
            meta.put("char_end", c.getCharEnd());
        }
        return new Document(c.getContent(), meta);
    }
}
