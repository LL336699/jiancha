package com.jiancha.biz.risk.domain;

/**
 * 预警核实请求
 *
 * <p>confirm 确认（转整改，须给出责任部门）/ exclude 排除（误报）/ archive 归档。</p>
 *
 * @author 小标
 */
public class RiskVerifyRequest {

    /** 核实结论（confirm/exclude/archive） */
    private String action;

    /** 核实意见 */
    private String opinion;

    /** 确认转整顿时指定的整改责任部门ID */
    private Long targetDeptId;

    /** 确认转整顿时的问题严重程度（一般/较大/重大） */
    private String severity;

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
}
