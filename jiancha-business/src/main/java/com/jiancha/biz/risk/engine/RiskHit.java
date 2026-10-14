package com.jiancha.biz.risk.engine;

/**
 * 规则命中结果（引擎输出）
 *
 * <p>一条命中 = 某个规则在某个对象上触发。evidence 说明命中原因（含来源单号），
 * score 由超出阈值的幅度折算（0-100），落库前由 Service 做去重（同规则同对象
 * 已有未闭环预警则跳过，避免每日重复刷屏）。</p>
 *
 * @author 小标
 */
public class RiskHit {

    /** 规则编码 */
    private String ruleCode;

    /** 对象类型（0人员 1部门 2岗位） */
    private String objectType;

    /** 对象ID */
    private Long objectId;

    /** 对象名称 */
    private String objectName;

    /** 风险分（0-100） */
    private double score;

    /** 命中证据（人类可读，含来源单号） */
    private String evidence;

    public RiskHit() {
    }

    public RiskHit(String ruleCode, String objectType, Long objectId, String objectName,
                   double score, String evidence) {
        this.ruleCode = ruleCode;
        this.objectType = objectType;
        this.objectId = objectId;
        this.objectName = objectName;
        this.score = score;
        this.evidence = evidence;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getEvidence() {
        return evidence;
    }

    public void setEvidence(String evidence) {
        this.evidence = evidence;
    }
}
