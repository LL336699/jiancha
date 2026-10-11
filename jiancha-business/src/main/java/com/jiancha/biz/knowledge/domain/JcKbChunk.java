package com.jiancha.biz.knowledge.domain;

import jakarta.validation.constraints.NotBlank;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 知识库分块 jc_kb_chunk
 *
 * <p><b>带出处的核心载体</b>：每个分块强制携带 clauseNo（条款号）、pageNo（页码）、
 * charStart/charEnd（原文偏移），回答时据此回填"文件名 + 条款号 / 页码"。
 * 缺少这些元数据的回答在纪检场景不可采信。</p>
 *
 * <p>分块策略见 {@code com.jiancha.biz.knowledge.rag.StructureAwareChunker}：
 * 法规按"条"切、制度按"章/条"两级切、案例按语义段切、通用文档递归切。</p>
 *
 * @author 小标
 */
public class JcKbChunk extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 分块ID */
    private Long chunkId;

    /** 所属文档ID */
    private Long docId;

    /** 块序号（文档内从 0 递增） */
    private Integer chunkIndex;

    /** 分块正文 */
    @NotBlank(message = "分块内容不能为空")
    private String content;

    /** 内容哈希（SHA-256，用于变更检测） */
    private String contentHash;

    /** 页码（PDF 等分页文档） */
    private Integer pageNo;

    /** 条款号（如"第十二条"、"第三章第二节"） */
    private String clauseNo;

    /** 原文起始偏移 */
    private Integer charStart;

    /** 原文结束偏移 */
    private Integer charEnd;

    // ───────────── 展示扩展字段（非表字段）─────────────

    /** 所属文档名称（检索结果回填出处用） */
    private String docName;

    /** 文档类型 */
    private String docType;

    /** 检索相似度得分（仅检索时填充） */
    private Double score;

    /** 关键词命中词数（仅关键词检索时填充，用于计分与排序） */
    private Integer hitCount;

    public Long getChunkId() {
        return chunkId;
    }

    public void setChunkId(Long chunkId) {
        this.chunkId = chunkId;
    }

    public Long getDocId() {
        return docId;
    }

    public void setDocId(Long docId) {
        this.docId = docId;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public String getClauseNo() {
        return clauseNo;
    }

    public void setClauseNo(String clauseNo) {
        this.clauseNo = clauseNo;
    }

    public Integer getCharStart() {
        return charStart;
    }

    public void setCharStart(Integer charStart) {
        this.charStart = charStart;
    }

    public Integer getCharEnd() {
        return charEnd;
    }

    public void setCharEnd(Integer charEnd) {
        this.charEnd = charEnd;
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Integer getHitCount() {
        return hitCount;
    }

    public void setHitCount(Integer hitCount) {
        this.hitCount = hitCount;
    }
}
