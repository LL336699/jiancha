package com.jiancha.biz.risk.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 风险预警规则 jc_risk_rule
 *
 * <p>企业版口径（设计文档 §M10）：规则与阈值全部可配置（参数表思路落到业务表），
 * 不写死算法，业务专家可自行调整。初始内置 6 条规则，覆盖设计文档
 * 「风险预警规则池」中当前数据可支撑的部分（举报集中、整改超期、回头看复发、
 * 线索超期、利益冲突申报存疑、岗位高风险）。</p>
 *
 * @author 小标
 */
public class JcRiskRule extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 规则ID */
    private Long ruleId;

    /** 规则编码（R01-R12，与引擎 evaluate 分支对应） */
    private String ruleCode;

    /** 规则名称 */
    private String ruleName;

    /** 规则类型（字典 jc_risk_rule_type） */
    private String ruleType;

    /** 规则说明（命中逻辑的自然语言描述） */
    private String description;

    /** 阈值（如 3 次 / 2 条 / 0.7 分） */
    private Double threshold;

    /** 阈值单位（次/条/分/天） */
    private String thresholdUnit;

    /** 命中后默认预警等级（0低 1中 2高） */
    private String alertLevel;

    /** 状态（0启用 1停用） */
    private String status;

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public String getThresholdUnit() {
        return thresholdUnit;
    }

    public void setThresholdUnit(String thresholdUnit) {
        this.thresholdUnit = thresholdUnit;
    }

    public String getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(String alertLevel) {
        this.alertLevel = alertLevel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
