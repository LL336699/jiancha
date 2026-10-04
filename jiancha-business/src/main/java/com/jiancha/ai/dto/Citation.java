package com.jiancha.ai.dto;

import java.io.Serializable;

/**
 * 引用出处（纪检场景刚性要求：回答必须带出处）
 *
 * <p>没有出处的内容不展示，这是防止 AI 编造法规条款的关键机制。</p>
 *
 * @author 小标
 */
public class Citation implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 文档名称 */
    private String docName;

    /** 法规条款号，如"第九十条" */
    private String clauseNo;

    /** 页码 */
    private Integer pageNo;

    /** 命中的原文片段 */
    private String snippet;

    /** 相似度得分 */
    private Double score;

    /** 生效日期（用于判断法规是否有效） */
    private String effectiveDate;

    /** 失效日期（检索层已强制排除过期法规，此字段仅作留痕） */
    private String expireDate;

    public Citation() {
    }

    public Citation(String docName, String clauseNo, Integer pageNo, String snippet, Double score) {
        this.docName = docName;
        this.clauseNo = clauseNo;
        this.pageNo = pageNo;
        this.snippet = snippet;
        this.score = score;
    }

    /**
     * 生成展示用引用串：中国共产党纪律处分条例 第九十条 (p12)
     */
    public String toDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append(docName == null ? "未知文档" : docName);
        if (clauseNo != null && !clauseNo.isEmpty()) {
            sb.append(" ").append(clauseNo);
        }
        if (pageNo != null) {
            sb.append(" (p").append(pageNo).append(")");
        }
        return sb.toString();
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public String getClauseNo() {
        return clauseNo;
    }

    public void setClauseNo(String clauseNo) {
        this.clauseNo = clauseNo;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(String effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }
}
