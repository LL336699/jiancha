package com.jiancha.biz.dashboard.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import java.io.Serial;

/**
 * 看板预警阈值规则（段12 纪检综合看板 · 预警规则引擎）
 *
 * <p>纪委可配置「某模块某指标超过阈值即触发预警」，指标类型 {@code metricType}
 * 支持 {@code overdue}(超期数) / {@code processing}(在办数) / {@code total}(总量)。
 * {@code moduleKey} 取具体模块标识或 {@code all} 表示全局汇总。</p>
 *
 * @author 小标
 */
public class DashboardAlertRule extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 规则主键 */
    @Excel(name = "规则ID")
    private Long ruleId;

    /** 规则名称 */
    @Excel(name = "规则名称")
    private String ruleName;

    /** 适用模块：petition/rectify/risk/supplier/edu/reply/conduct/asset/all */
    @Excel(name = "适用模块")
    private String moduleKey;

    /** 指标类型：overdue/processing/total */
    @Excel(name = "指标类型")
    private String metricType;

    /** 触发阈值（实际值 >= 阈值即预警） */
    @Excel(name = "阈值")
    private Integer threshold;

    /** 预警级别：red/yellow */
    @Excel(name = "预警级别")
    private String alertLevel;

    /** 是否启用：1 启用 / 0 停用 */
    @Excel(name = "启用", readConverterExp = "1=启用,0=停用")
    private String enabled;

    /** 备注 */
    @Excel(name = "备注")
    private String remark;

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getModuleKey() {
        return moduleKey;
    }

    public void setModuleKey(String moduleKey) {
        this.moduleKey = moduleKey;
    }

    public String getMetricType() {
        return metricType;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }

    public String getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(String alertLevel) {
        this.alertLevel = alertLevel;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
