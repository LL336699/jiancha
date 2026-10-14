package com.jiancha.biz.petition.domain;

/**
 * 举报流转请求（管理端使用）
 *
 * <p>与线索模块的 ClueActionRequest 保持同构：动作类型 + 办理意见 + 分办目标
 * （承办部门 / 处理人）+ 反馈内容。分办动作必须同时给出承办部门 ID，
 * 因为转线索时线索的承办部门为必填。</p>
 *
 * @author 小标
 */
public class PetitionActionRequest {

    /** 动作类型（accept/review/dispatch/handle/feedback/archive/reject） */
    private String action;

    /** 办理意见（写入轨迹留痕） */
    private String opinion;

    /** 分办指定的承办部门ID（dispatch 时必填） */
    private Long deptId;

    /** 分办指定的处理人ID（选填） */
    private Long handlerId;

    /** 反馈内容（feedback 动作时必填，举报人凭回执码可见） */
    private String feedback;

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

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Long getHandlerId() {
        return handlerId;
    }

    public void setHandlerId(Long handlerId) {
        this.handlerId = handlerId;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
