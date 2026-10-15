package com.jiancha.biz.reply.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 廉政意见回复·筛查明细（E2）
 *
 * <p>系统自动筛查时命中的跨模块记录快照：线索/举报/整改/预警/供应商关联/处分。</p>
 *
 * @author 小标
 */
public class JcReplyScreenItem extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long itemId;

    /** 申请ID */
    private Long requestId;

    /** 来源模块（字典 jc_reply_source：问题线索/舞弊举报/整改问题/风险预警/供应商关联/廉政处分） */
    private String sourceModule;

    /** 来源单号（线索号/回执码/问题编号/预警号等） */
    private String sourceNo;

    /** 摘要（标题或事项简述） */
    private String summary;

    /** 来源状态（进行中/已完结等，快照） */
    private String sourceStatus;

    /** 风险权重（0-100，汇总为建议结论依据） */
    private Integer riskWeight;

    /** 筛查时间 */
    private java.util.Date screenTime;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getSourceModule() {
        return sourceModule;
    }

    public void setSourceModule(String sourceModule) {
        this.sourceModule = sourceModule;
    }

    public String getSourceNo() {
        return sourceNo;
    }

    public void setSourceNo(String sourceNo) {
        this.sourceNo = sourceNo;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSourceStatus() {
        return sourceStatus;
    }

    public void setSourceStatus(String sourceStatus) {
        this.sourceStatus = sourceStatus;
    }

    public Integer getRiskWeight() {
        return riskWeight;
    }

    public void setRiskWeight(Integer riskWeight) {
        this.riskWeight = riskWeight;
    }

    public java.util.Date getScreenTime() {
        return screenTime;
    }

    public void setScreenTime(java.util.Date screenTime) {
        this.screenTime = screenTime;
    }
}
