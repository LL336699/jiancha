package com.jiancha.biz.clue.domain;

/**
 * 线索流转请求
 *
 * <p>承载一次流转动作所需的全部参数：动作类型、办理意见、分办目标（部门 / 处理人）、
 * 处置方式，以及研判环节允许修正的问题类型。</p>
 *
 * @author 小标
 */
public class ClueActionRequest {

    /** 动作类型（accept/review/assign/dispose/close/reject/return） */
    private String action;

    /** 办理意见 / 处置说明（写入轨迹留痕） */
    private String opinion;

    /** 分办指定的处理人ID */
    private Long handlerId;

    /** 分办指定的承办部门ID */
    private Long deptId;

    /** 研判环节可修正的问题类型 */
    private String violationType;

    /** 处置方式（字典 jc_clue_disposition） */
    private String disposition;

    /** 变更后的办理截止日期（可选，重新计算时限用） */
    private String deadline;

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

    public Long getHandlerId() {
        return handlerId;
    }

    public void setHandlerId(Long handlerId) {
        this.handlerId = handlerId;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getViolationType() {
        return violationType;
    }

    public void setViolationType(String violationType) {
        this.violationType = violationType;
    }

    public String getDisposition() {
        return disposition;
    }

    public void setDisposition(String disposition) {
        this.disposition = disposition;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }
}
