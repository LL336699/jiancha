package com.jiancha.biz.reply.domain;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 廉政意见回复·筛查结果（E2）
 *
 * <p>自动筛查输出：命中明细列表 + 汇总建议结论。</p>
 *
 * @author 小标
 */
public class ReplyScreenResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 申请ID */
    private Long requestId;

    /** 命中明细 */
    private List<JcReplyScreenItem> items;

    /** 命中总数 */
    private Integer hitCount;

    /** 最高风险权重 */
    private Integer maxWeight;

    /** 系统建议结论（建议通过/建议暂缓/建议否决） */
    private String suggestResult;

    /** 结论依据说明 */
    private String suggestReason;

    /** 分来源计数（sourceModule -> cnt） */
    private Map<String, Object> byModule;

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public List<JcReplyScreenItem> getItems() {
        return items;
    }

    public void setItems(List<JcReplyScreenItem> items) {
        this.items = items;
    }

    public Integer getHitCount() {
        return hitCount;
    }

    public void setHitCount(Integer hitCount) {
        this.hitCount = hitCount;
    }

    public Integer getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(Integer maxWeight) {
        this.maxWeight = maxWeight;
    }

    public String getSuggestResult() {
        return suggestResult;
    }

    public void setSuggestResult(String suggestResult) {
        this.suggestResult = suggestResult;
    }

    public String getSuggestReason() {
        return suggestReason;
    }

    public void setSuggestReason(String suggestReason) {
        this.suggestReason = suggestReason;
    }

    public Map<String, Object> getByModule() {
        return byModule;
    }

    public void setByModule(Map<String, Object> byModule) {
        this.byModule = byModule;
    }
}
