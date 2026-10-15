package com.jiancha.biz.reply.domain;

import java.io.Serializable;
import java.util.Map;

/**
 * 廉政意见回复·统计视图（E2）
 *
 * @author 小标
 */
public class ReplyStat implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 卡片统计 */
    private Map<String, Object> cards;

    /** 按事项类型分布 */
    private java.util.List<Map<String, Object>> byMatter;

    /** 按结论分布 */
    private java.util.List<Map<String, Object>> byResult;

    /** 月度申请量（近12个月） */
    private java.util.List<Map<String, Object>> monthlyRequests;

    /** 按对象部门分布（Top10） */
    private java.util.List<Map<String, Object>> byDept;

    /** 分来源命中统计 */
    private java.util.List<Map<String, Object>> hitsByModule;

    public Map<String, Object> getCards() {
        return cards;
    }

    public void setCards(Map<String, Object> cards) {
        this.cards = cards;
    }

    public java.util.List<Map<String, Object>> getByMatter() {
        return byMatter;
    }

    public void setByMatter(java.util.List<Map<String, Object>> byMatter) {
        this.byMatter = byMatter;
    }

    public java.util.List<Map<String, Object>> getByResult() {
        return byResult;
    }

    public void setByResult(java.util.List<Map<String, Object>> byResult) {
        this.byResult = byResult;
    }

    public java.util.List<Map<String, Object>> getMonthlyRequests() {
        return monthlyRequests;
    }

    public void setMonthlyRequests(java.util.List<Map<String, Object>> monthlyRequests) {
        this.monthlyRequests = monthlyRequests;
    }

    public java.util.List<Map<String, Object>> getByDept() {
        return byDept;
    }

    public void setByDept(java.util.List<Map<String, Object>> byDept) {
        this.byDept = byDept;
    }

    public java.util.List<Map<String, Object>> getHitsByModule() {
        return hitsByModule;
    }

    public void setHitsByModule(java.util.List<Map<String, Object>> hitsByModule) {
        this.hitsByModule = hitsByModule;
    }
}
