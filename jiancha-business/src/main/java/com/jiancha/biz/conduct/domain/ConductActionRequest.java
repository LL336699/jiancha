package com.jiancha.biz.conduct.domain;

/**
 * 作风建设问题处置 / 转办请求
 *
 * <p>verify：核查登记（0→1）；handle：处置（1→2）；
 * transfer：转整改 / 转处分 / 关闭（2→3）的参数载体。</p>
 *
 * @author 小标
 */
public class ConductActionRequest {

    /** 问题ID */
    private Long issueId;

    /** 核查结论：问题描述 */
    private String violationDesc;

    /** 涉及金额/礼品价值（元） */
    private java.math.BigDecimal amount;

    /** 严重程度（字典 jc_conduct_severity） */
    private String severity;

    /** 处置方式（字典 jc_conduct_handle） */
    private String handleType;

    /** 处置日期 */
    private String handleDate;

    /** 处置说明 */
    private String handleDesc;

    /** 退缴金额（元） */
    private java.math.BigDecimal amountRecovered;

    /** 转处分：处分种类（字典 jc_punish_type） */
    private String punishType;

    /** 转处分：处分事由 */
    private String punishReason;

    /** 转整改：整改问题标题 */
    private String rectifyTitle;

    /** 转整改：整改责任部门ID（sys_dept） */
    private Long rectifyDeptId;

    /** 转整改：整改截止日期 */
    private String rectifyDeadline;

    /** 作废 / 关闭原因 */
    private String reason;

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public String getViolationDesc() {
        return violationDesc;
    }

    public void setViolationDesc(String violationDesc) {
        this.violationDesc = violationDesc;
    }

    public java.math.BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(java.math.BigDecimal amount) {
        this.amount = amount;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getHandleType() {
        return handleType;
    }

    public void setHandleType(String handleType) {
        this.handleType = handleType;
    }

    public String getHandleDate() {
        return handleDate;
    }

    public void setHandleDate(String handleDate) {
        this.handleDate = handleDate;
    }

    public String getHandleDesc() {
        return handleDesc;
    }

    public void setHandleDesc(String handleDesc) {
        this.handleDesc = handleDesc;
    }

    public java.math.BigDecimal getAmountRecovered() {
        return amountRecovered;
    }

    public void setAmountRecovered(java.math.BigDecimal amountRecovered) {
        this.amountRecovered = amountRecovered;
    }

    public String getPunishType() {
        return punishType;
    }

    public void setPunishType(String punishType) {
        this.punishType = punishType;
    }

    public String getPunishReason() {
        return punishReason;
    }

    public void setPunishReason(String punishReason) {
        this.punishReason = punishReason;
    }

    public String getRectifyTitle() {
        return rectifyTitle;
    }

    public void setRectifyTitle(String rectifyTitle) {
        this.rectifyTitle = rectifyTitle;
    }

    public Long getRectifyDeptId() {
        return rectifyDeptId;
    }

    public void setRectifyDeptId(Long rectifyDeptId) {
        this.rectifyDeptId = rectifyDeptId;
    }

    public String getRectifyDeadline() {
        return rectifyDeadline;
    }

    public void setRectifyDeadline(String rectifyDeadline) {
        this.rectifyDeadline = rectifyDeadline;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
