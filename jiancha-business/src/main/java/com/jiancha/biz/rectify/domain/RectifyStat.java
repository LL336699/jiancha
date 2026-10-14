package com.jiancha.biz.rectify.domain;

import java.util.List;

/**
 * 整改闭环统计汇总
 *
 * <p>统计口径统一在后端 SQL 层聚合，前端不做二次计算。</p>
 *
 * @author 小标
 */
public class RectifyStat {

    /** 问题总量 */
    private Long total;

    /** 待整改 */
    private Long pending;

    /** 整改中（含待复核） */
    private Long rectifying;

    /** 已销号 */
    private Long closed;

    /** 当前超期（红灯） */
    private Long overdue;

    /** 平均销号天数（登记 → 销号） */
    private Double avgCloseDays;

    /** 回头看重开次数 */
    private Long revisitCount;

    /** 来源分布 */
    private List<RectifyStatItem> bySource;

    /** 问题类型分布 */
    private List<RectifyStatItem> byType;

    /** 严重程度分布 */
    private List<RectifyStatItem> bySeverity;

    /** 状态分布 */
    private List<RectifyStatItem> byStatus;

    /** 近12个月登记趋势 */
    private List<RectifyStatItem> monthlyTrend;

    /** 责任部门未销号问题 Top10（部门对比） */
    private List<RectifyStatItem> byTargetDept;

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getPending() {
        return pending;
    }

    public void setPending(Long pending) {
        this.pending = pending;
    }

    public Long getRectifying() {
        return rectifying;
    }

    public void setRectifying(Long rectifying) {
        this.rectifying = rectifying;
    }

    public Long getClosed() {
        return closed;
    }

    public void setClosed(Long closed) {
        this.closed = closed;
    }

    public Long getOverdue() {
        return overdue;
    }

    public void setOverdue(Long overdue) {
        this.overdue = overdue;
    }

    public Double getAvgCloseDays() {
        return avgCloseDays;
    }

    public void setAvgCloseDays(Double avgCloseDays) {
        this.avgCloseDays = avgCloseDays;
    }

    public Long getRevisitCount() {
        return revisitCount;
    }

    public void setRevisitCount(Long revisitCount) {
        this.revisitCount = revisitCount;
    }

    public List<RectifyStatItem> getBySource() {
        return bySource;
    }

    public void setBySource(List<RectifyStatItem> bySource) {
        this.bySource = bySource;
    }

    public List<RectifyStatItem> getByType() {
        return byType;
    }

    public void setByType(List<RectifyStatItem> byType) {
        this.byType = byType;
    }

    public List<RectifyStatItem> getBySeverity() {
        return bySeverity;
    }

    public void setBySeverity(List<RectifyStatItem> bySeverity) {
        this.bySeverity = bySeverity;
    }

    public List<RectifyStatItem> getByStatus() {
        return byStatus;
    }

    public void setByStatus(List<RectifyStatItem> byStatus) {
        this.byStatus = byStatus;
    }

    public List<RectifyStatItem> getMonthlyTrend() {
        return monthlyTrend;
    }

    public void setMonthlyTrend(List<RectifyStatItem> monthlyTrend) {
        this.monthlyTrend = monthlyTrend;
    }

    public List<RectifyStatItem> getByTargetDept() {
        return byTargetDept;
    }

    public void setByTargetDept(List<RectifyStatItem> byTargetDept) {
        this.byTargetDept = byTargetDept;
    }
}
