package com.jiancha.biz.supplier.domain;

/**
 * 供应商模块动作请求 DTO（准入审查 / 关联处置 / 一键转整改 / 黑名单解除）
 *
 * @author 小标
 */
public class SupplierActionRequest {

    /** 动作类型 */
    private String action;

    /** 办理意见（必填） */
    private String opinion;

    /** 准入审查结果：pass 正常 / observe 观察 / deny 禁入（action=audit 时必填） */
    private String auditResult;

    /** 关联处置结果：normal 正常 / observe 观察 / deny 禁入 / clear 解除（action=handleLink 时必填） */
    private String handleResult;

    /** 转整改责任部门ID（action=toRectify 时必填，落 jc_rectify_issue.dept_id） */
    private Long targetDeptId;

    /** 转整改严重程度（高/较大/一般） */
    private String severity;

    /** 黑名单期限（天，action=black 时可空=永久） */
    private Integer blackDays;

    /** 拉黑原因（action=black 时必填） */
    private String blackReason;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }

    public String getAuditResult() {
        return auditResult;
    }

    public void setAuditResult(String auditResult) {
        this.auditResult = auditResult;
    }

    public String getHandleResult() {
        return handleResult;
    }

    public void setHandleResult(String handleResult) {
        this.handleResult = handleResult;
    }

    public Long getTargetDeptId() {
        return targetDeptId;
    }

    public void setTargetDeptId(Long targetDeptId) {
        this.targetDeptId = targetDeptId;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Integer getBlackDays() {
        return blackDays;
    }

    public void setBlackDays(Integer blackDays) {
        this.blackDays = blackDays;
    }

    public String getBlackReason() {
        return blackReason;
    }

    public void setBlackReason(String blackReason) {
        this.blackReason = blackReason;
    }
}
