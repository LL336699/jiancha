package com.jiancha.biz.petition.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 舞弊举报办理轨迹 jc_petition_trace
 *
 * <p>举报每一次流转（提交 / 受理 / 研判 / 分办 / 办理 / 反馈 / 归档 / 不予受理）
 * 均落一条轨迹，全程留痕、不可修改；回执码查询进度时只暴露状态与时间，不暴露轨迹详情。</p>
 *
 * @author 小标
 */
public class JcPetitionTrace extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 轨迹ID */
    private Long traceId;

    /** 举报ID */
    private Long petitionId;

    /** 动作（submit/accept/review/dispatch/handle/feedback/archive/reject） */
    private String action;

    /** 动作说明 */
    private String actionDesc;

    /** 流转前状态 */
    private String fromStatus;

    /** 流转后状态 */
    private String toStatus;

    /** 操作人（免登录提交时记为「举报人」） */
    private String operator;

    /** 流转时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getTraceId() {
        return traceId;
    }

    public void setTraceId(Long traceId) {
        this.traceId = traceId;
    }

    public Long getPetitionId() {
        return petitionId;
    }

    public void setPetitionId(Long petitionId) {
        this.petitionId = petitionId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getActionDesc() {
        return actionDesc;
    }

    public void setActionDesc(String actionDesc) {
        this.actionDesc = actionDesc;
    }

    public String getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(String fromStatus) {
        this.fromStatus = fromStatus;
    }

    public String getToStatus() {
        return toStatus;
    }

    public void setToStatus(String toStatus) {
        this.toStatus = toStatus;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
