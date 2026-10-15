package com.jiancha.biz.conduct.domain;

import java.util.List;
import java.util.Map;

/**
 * 作风建设专项统计
 *
 * @author 小标
 */
public class ConductStat {

    /** 卡片：总量 / 待核查 / 已核查待处理 / 已处理 / 已关闭 / 退缴总额 */
    private List<Map<String, Object>> cards;

    /** 分问题类型统计 */
    private List<Map<String, Object>> byType;

    /** 分严重程度统计 */
    private List<Map<String, Object>> bySeverity;

    /** 分来源统计 */
    private List<Map<String, Object>> bySource;

    /** 分部门统计 */
    private List<Map<String, Object>> byDept;

    /** 分处置方式统计 */
    private List<Map<String, Object>> byHandle;

    /** 月度问题量（近12个月） */
    private List<Map<String, Object>> monthlyIssues;

    public List<Map<String, Object>> getCards() {
        return cards;
    }

    public void setCards(List<Map<String, Object>> cards) {
        this.cards = cards;
    }

    public List<Map<String, Object>> getByType() {
        return byType;
    }

    public void setByType(List<Map<String, Object>> byType) {
        this.byType = byType;
    }

    public List<Map<String, Object>> getBySeverity() {
        return bySeverity;
    }

    public void setBySeverity(List<Map<String, Object>> bySeverity) {
        this.bySeverity = bySeverity;
    }

    public List<Map<String, Object>> getBySource() {
        return bySource;
    }

    public void setBySource(List<Map<String, Object>> bySource) {
        this.bySource = bySource;
    }

    public List<Map<String, Object>> getByDept() {
        return byDept;
    }

    public void setByDept(List<Map<String, Object>> byDept) {
        this.byDept = byDept;
    }

    public List<Map<String, Object>> getByHandle() {
        return byHandle;
    }

    public void setByHandle(List<Map<String, Object>> byHandle) {
        this.byHandle = byHandle;
    }

    public List<Map<String, Object>> getMonthlyIssues() {
        return monthlyIssues;
    }

    public void setMonthlyIssues(List<Map<String, Object>> monthlyIssues) {
        this.monthlyIssues = monthlyIssues;
    }
}
