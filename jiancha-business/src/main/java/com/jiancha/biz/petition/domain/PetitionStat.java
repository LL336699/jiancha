package com.jiancha.biz.petition.domain;

import java.util.List;

/**
 * 举报办理统计汇总
 *
 * <p>统计口径统一在后端 SQL 层聚合（设计文档 §M8 要点），前端不做二次计算。</p>
 *
 * @author 小标
 */
public class PetitionStat {

    /** 举报总量 */
    private Long total;

    /** 待受理量 */
    private Long pending;

    /** 办理中量（已受理~已反馈） */
    private Long handling;

    /** 已归档/不予受理量 */
    private Long finished;

    /** 当前超期量（红灯） */
    private Long overdue;

    /** 平均办理时长（小时，受理 → 反馈/归档） */
    private Double avgHandleHours;

    /** 状态分布 */
    private List<PetitionStatItem> byStatus;

    /** 舞弊类型分布 */
    private List<PetitionStatItem> byCategory;

    /** 提交渠道分布 */
    private List<PetitionStatItem> byChannel;

    /** 近12个月提交趋势 */
    private List<PetitionStatItem> monthlyTrend;

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

    public Long getHandling() {
        return handling;
    }

    public void setHandling(Long handling) {
        this.handling = handling;
    }

    public Long getFinished() {
        return finished;
    }

    public void setFinished(Long finished) {
        this.finished = finished;
    }

    public Long getOverdue() {
        return overdue;
    }

    public void setOverdue(Long overdue) {
        this.overdue = overdue;
    }

    public Double getAvgHandleHours() {
        return avgHandleHours;
    }

    public void setAvgHandleHours(Double avgHandleHours) {
        this.avgHandleHours = avgHandleHours;
    }

    public List<PetitionStatItem> getByStatus() {
        return byStatus;
    }

    public void setByStatus(List<PetitionStatItem> byStatus) {
        this.byStatus = byStatus;
    }

    public List<PetitionStatItem> getByCategory() {
        return byCategory;
    }

    public void setByCategory(List<PetitionStatItem> byCategory) {
        this.byCategory = byCategory;
    }

    public List<PetitionStatItem> getByChannel() {
        return byChannel;
    }

    public void setByChannel(List<PetitionStatItem> byChannel) {
        this.byChannel = byChannel;
    }

    public List<PetitionStatItem> getMonthlyTrend() {
        return monthlyTrend;
    }

    public void setMonthlyTrend(List<PetitionStatItem> monthlyTrend) {
        this.monthlyTrend = monthlyTrend;
    }
}
