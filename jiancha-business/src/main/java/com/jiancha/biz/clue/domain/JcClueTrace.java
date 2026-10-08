package com.jiancha.biz.clue.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 问题线索办理轨迹 jc_clue_trace
 *
 * <p>线索每一次流转（受理 / 研判 / 分办 / 处置 / 办结 / 退回 / 不予受理）
 * 均落一条轨迹，全程留痕、不可修改。</p>
 *
 * @author 小标
 */
public class JcClueTrace extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 轨迹ID */
    private Long traceId;

    /** 线索ID */
    private Long clueId;

    /** 动作（字典 jc_clue_action） */
    private String action;

    /** 动作说明 */
    private String actionDesc;

    /** 变更前状态 */
    private String beforeStatus;

    /** 变更后状态 */
    private String afterStatus;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人姓名（冗余，防用户改名后追溯失真） */
    private String operatorName;

    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operateTime;

    public Long getTraceId() {
        return traceId;
    }

    public void setTraceId(Long traceId) {
        this.traceId = traceId;
    }

    public Long getClueId() {
        return clueId;
    }

    public void setClueId(Long clueId) {
        this.clueId = clueId;
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

    public String getBeforeStatus() {
        return beforeStatus;
    }

    public void setBeforeStatus(String beforeStatus) {
        this.beforeStatus = beforeStatus;
    }

    public String getAfterStatus() {
        return afterStatus;
    }

    public void setAfterStatus(String afterStatus) {
        this.afterStatus = afterStatus;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public Date getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(Date operateTime) {
        this.operateTime = operateTime;
    }
}
