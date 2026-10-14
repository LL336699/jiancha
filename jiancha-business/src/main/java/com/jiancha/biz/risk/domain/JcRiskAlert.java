package com.jiancha.biz.risk.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 风险预警记录 jc_risk_alert
 *
 * <p>由风控引擎按日扫描生成（也可手动登记）。闭环路径：
 * 待核实 → 核实中 → 已确认(转整改) / 已排除(误报) → 已归档。
 * 确认后通过 {@code IJcRectifyService.register} 落一条 source_type=warn 的整改问题，
 * issue_id 回填，实现「预警 → 核实 → 整改 → 复核 → 归档」全链闭环（设计文档 §M10）。</p>
 *
 * @author 小标
 */
public class JcRiskAlert extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 预警ID */
    private Long alertId;

    /** 预警编号（RA+yyyyMMdd+seq） */
    @Excel(name = "预警编号")
    private String alertNo;

    /** 命中规则ID */
    private Long ruleId;

    /** 命中规则编码（冗余展示） */
    private String ruleCode;

    /** 命中规则名称（冗余展示） */
    private String ruleName;

    /** 预警对象类型（0人员 1部门 2岗位） */
    private String objectType;

    /** 预警对象ID（person_id / dept_id / model_id） */
    private Long objectId;

    /** 预警对象名称（冗余，人员离职后仍可读） */
    @Excel(name = "预警对象")
    private String objectName;

    /** 预警等级（0低 1中 2高） */
    @Excel(name = "预警等级")
    private String alertLevel;

    /** 风险分（0-100） */
    private Double score;

    /** 命中证据（文本描述，说明为什么命中） */
    private String evidence;

    /** 状态（0待核实 1核实中 2已确认转整改 3已排除误报 4已归档） */
    @Excel(name = "状态")
    private String status;

    /** 核实意见 */
    private String verifyOpinion;

    /** 核实人 */
    private String verifyBy;

    /** 核实时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date verifyTime;

    /** 转整改后关联的问题ID（jc_rectify_issue.issue_id） */
    private Long issueId;

    /** 关联问题编号（展示字段） */
    private String issueNo;

    public Long getAlertId() {
        return alertId;
    }

    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }

    public String getAlertNo() {
        return alertNo;
    }

    public void setAlertNo(String alertNo) {
        this.alertNo = alertNo;
    }

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

    public String getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(String alertLevel) {
        this.alertLevel = alertLevel;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getEvidence() {
        return evidence;
    }

    public void setEvidence(String evidence) {
        this.evidence = evidence;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVerifyOpinion() {
        return verifyOpinion;
    }

    public void setVerifyOpinion(String verifyOpinion) {
        this.verifyOpinion = verifyOpinion;
    }

    public String getVerifyBy() {
        return verifyBy;
    }

    public void setVerifyBy(String verifyBy) {
        this.verifyBy = verifyBy;
    }

    public Date getVerifyTime() {
        return verifyTime;
    }

    public void setVerifyTime(Date verifyTime) {
        this.verifyTime = verifyTime;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public String getIssueNo() {
        return issueNo;
    }

    public void setIssueNo(String issueNo) {
        this.issueNo = issueNo;
    }
}
