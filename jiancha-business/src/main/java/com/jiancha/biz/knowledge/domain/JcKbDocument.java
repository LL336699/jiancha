package com.jiancha.biz.knowledge.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 知识库文档 jc_kb_document
 *
 * <p>法规时效性由 effective_date / expire_date 控制：检索时若 expire_date 早于今天，
 * 则强制排除该文档及其全部向量块——用失效条文量纪是严重错误。</p>
 *
 * @author 小标
 */
public class JcKbDocument extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 文档ID */
    private Long docId;

    /** 文档名称 */
    @Excel(name = "文档名称")
    private String docName;

    /** 分类ID */
    private Long categoryId;

    /** 文件存储路径（相对应用上传根目录） */
    private String filePath;

    /** 文件哈希（SHA-256，用于上传去重） */
    private String fileHash;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 文件类型（PDF / DOCX / TXT / MD …） */
    @Excel(name = "文件类型")
    private String docType;

    /** 版本号 */
    @Excel(name = "版本")
    private String version;

    /** 发文机关 / 来源 */
    @Excel(name = "发文机关")
    private String issuer;

    /** 文号 */
    @Excel(name = "文号")
    private String docNo;

    /** 生效日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "生效日期", dateFormat = "yyyy-MM-dd")
    private Date effectiveDate;

    /** 失效日期（早于今天则检索时强制排除） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "失效日期", dateFormat = "yyyy-MM-dd")
    private Date expireDate;

    /** 分块总数 */
    private Integer chunkCount;

    /** 状态（字典 jc_kb_doc_status：待处理/解析中/已入库/解析失败） */
    @Excel(name = "状态")
    private String status;

    /** 解析错误信息 */
    private String parseError;

    // ───────────── 展示扩展字段（非表字段）─────────────

    /** 分类名称 */
    private String catName;

    /** 是否已失效（expire_date 早于今天） */
    private Boolean expired;

    public Long getDocId() {
        return docId;
    }

    public void setDocId(Long docId) {
        this.docId = docId;
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getDocNo() {
        return docNo;
    }

    public void setDocNo(String docNo) {
        this.docNo = docNo;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public Integer getChunkCount() {
        return chunkCount;
    }

    public void setChunkCount(Integer chunkCount) {
        this.chunkCount = chunkCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getParseError() {
        return parseError;
    }

    public void setParseError(String parseError) {
        this.parseError = parseError;
    }

    public String getCatName() {
        return catName;
    }

    public void setCatName(String catName) {
        this.catName = catName;
    }

    public Boolean getExpired() {
        return expired;
    }

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }
}
