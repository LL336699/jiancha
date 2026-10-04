package com.jiancha.biz.archive.domain;

/**
 * 单条风险命中结果（12 类预警之一）
 *
 * @author 小标
 */
public class RiskHit
{
    /** 预警编号（1-12，见设计文档 M5） */
    private int categoryNo;
    /** 预警类型名称 */
    private String categoryName;
    /** 严重度：高/中/低 */
    private String level;
    /** 命中说明 */
    private String description;
    /** 证据（命中了哪些原始记录，便于溯源） */
    private String evidence;
    /** 处置建议 */
    private String suggestion;

    public int getCategoryNo() { return categoryNo; }
    public void setCategoryNo(int categoryNo) { this.categoryNo = categoryNo; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getEvidence() { return evidence; }
    public void setEvidence(String evidence) { this.evidence = evidence; }
    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
}
