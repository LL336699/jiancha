package com.jiancha.biz.risk.domain;

import java.util.List;

/**
 * 风控统计与三级风险视图汇总
 *
 * <p>统计口径统一在后端 SQL 层聚合；三级风险视图（设计文档 §M10）：
 * 部门层（未闭环预警数 + 最高等级）、岗位层（模型权重分）、个人层（未闭环预警数）。</p>
 *
 * @author 小标
 */
public class RiskStat {

    /** 预警总量 */
    private Long total;

    /** 待核实 */
    private Long pending;

    /** 核实中 */
    private Long verifying;

    /** 已确认转整改 */
    private Long confirmed;

    /** 已排除误报 */
    private Long excluded;

    /** 高级别（高）未闭环预警数 */
    private Long highOpen;

    /** 转整改率（已确认 / 已核实，百分比） */
    private Double confirmRate;

    /** 等级分布 */
    private List<RiskStatItem> byLevel;

    /** 状态分布 */
    private List<RiskStatItem> byStatus;

    /** 规则命中分布 */
    private List<RiskStatItem> byRule;

    /** 近12个月预警趋势 */
    private List<RiskStatItem> monthlyTrend;

    /** 三级视图-部门层 */
    private List<RiskStatItem> deptView;

    /** 三级视图-岗位层（岗位名 + 模型权重分） */
    private List<RiskStatItem> positionView;

    /** 三级视图-个人层 */
    private List<RiskStatItem> personView;

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

    public Long getVerifying() {
        return verifying;
    }

    public void setVerifying(Long verifying) {
        this.verifying = verifying;
    }

    public Long getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Long confirmed) {
        this.confirmed = confirmed;
    }

    public Long getExcluded() {
        return excluded;
    }

    public void setExcluded(Long excluded) {
        this.excluded = excluded;
    }

    public Long getHighOpen() {
        return highOpen;
    }

    public void setHighOpen(Long highOpen) {
        this.highOpen = highOpen;
    }

    public Double getConfirmRate() {
        return confirmRate;
    }

    public void setConfirmRate(Double confirmRate) {
        this.confirmRate = confirmRate;
    }

    public List<RiskStatItem> getByLevel() {
        return byLevel;
    }

    public void setByLevel(List<RiskStatItem> byLevel) {
        this.byLevel = byLevel;
    }

    public List<RiskStatItem> getByStatus() {
        return byStatus;
    }

    public void setByStatus(List<RiskStatItem> byStatus) {
        this.byStatus = byStatus;
    }

    public List<RiskStatItem> getByRule() {
        return byRule;
    }

    public void setByRule(List<RiskStatItem> byRule) {
        this.byRule = byRule;
    }

    public List<RiskStatItem> getMonthlyTrend() {
        return monthlyTrend;
    }

    public void setMonthlyTrend(List<RiskStatItem> monthlyTrend) {
        this.monthlyTrend = monthlyTrend;
    }

    public List<RiskStatItem> getDeptView() {
        return deptView;
    }

    public void setDeptView(List<RiskStatItem> deptView) {
        this.deptView = deptView;
    }

    public List<RiskStatItem> getPositionView() {
        return positionView;
    }

    public void setPositionView(List<RiskStatItem> positionView) {
        this.positionView = positionView;
    }

    public List<RiskStatItem> getPersonView() {
        return personView;
    }

    public void setPersonView(List<RiskStatItem> personView) {
        this.personView = personView;
    }
}
