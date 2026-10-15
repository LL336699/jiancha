package com.jiancha.biz.conduct.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 作风建设问题台账主表 jc_conduct_issue
 *
 * <p>企业版口径（设计文档 §E5 作风建设专项）：落实中央八项规定精神
 * （企业版对应：违规吃喝、违规收送礼品礼金、公车私用、公款旅游、
 * 大办婚丧喜庆、违规发放津补贴、楼堂馆所违规等）问题专项台账，
 * 走「登记 → 核查 → 处置（退缴/谈话/诫勉/处分）→ 转整改/转处分 → 关闭」
 * 闭环；转整改回填 rectify_id 关联 E3 整改台账，转处分回填 punish_id
 * 关联干部处分记录。</p>
 *
 * <p>状态机（严格单向，作废环除外）：0待核查 → 1已核查待处理 → 2已处理 → 3已关闭；
 * 0/1/2 均可转 4 已作废。</p>
 *
 * @author 小标
 */
public class JcConductIssue extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 问题ID */
    private Long issueId;

    /** 问题编号（CF+yyyyMMdd+seq） */
    @Excel(name = "问题编号")
    private String issueNo;

    /** 问题人员ID（jc_person） */
    private Long personId;

    /** 问题人员姓名（登记时档案快照） */
    @Excel(name = "问题人员")
    private String personName;

    /** 问题人员部门（登记时档案快照） */
    private String personDept;

    /** 问题类型（字典 jc_conduct_type） */
    @Excel(name = "问题类型")
    private String violationType;

    /** 问题发生日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date violationDate;

    /** 问题描述 */
    private String violationDesc;

    /** 涉及金额/礼品价值（元） */
    private java.math.BigDecimal amount;

    /** 退缴金额（元） */
    private java.math.BigDecimal amountRecovered;

    /** 来源类型（字典 jc_conduct_source） */
    private String sourceType;

    /** 严重程度（字典 jc_conduct_severity：一般/较大/重大） */
    @Excel(name = "严重程度")
    private String severity;

    /** 核查截止日期（红黄灯口径：到期未核查亮灯） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date checkDeadline;

    /** 状态（0待核查 1已核查待处理 2已处理 3已关闭 4已作废） */
    @Excel(name = "状态")
    private String status;

    /** 预警级别（""/黄灯/红灯） */
    private String warnLevel;

    /** 处置方式（字典 jc_conduct_handle：提醒谈话/诫勉谈话/组织处理/纪律处分/移送司法） */
    private String handleType;

    /** 处置日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date handleDate;

    /** 处置说明 */
    private String handleDesc;

    /** 转处分：干部处分记录ID（jc_person_punish.punish_id） */
    private Long punishId;

    /** 转整改：整改问题ID（jc_rectify_issue.issue_id） */
    private Long rectifyId;

    /** 关闭时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date closeTime;

    /** 剩余天数（展示字段，负数表示核查已超期） */
    private Long remainDays;

    /** 转整改后整改状态（展示字段） */
    private String rectifyStatus;

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

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getPersonDept() {
        return personDept;
    }

    public void setPersonDept(String personDept) {
        this.personDept = personDept;
    }

    public String getViolationType() {
        return violationType;
    }

    public void setViolationType(String violationType) {
        this.violationType = violationType;
    }

    public Date getViolationDate() {
        return violationDate;
    }

    public void setViolationDate(Date violationDate) {
        this.violationDate = violationDate;
    }

    public String getViolationDesc() {
        return violationDesc;
    }

    public void setViolationDesc(String violationDesc) {
        this.violationDesc = violationDesc;
    }

    public java.math.BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(java.math.BigDecimal amount) {
        this.amount = amount;
    }

    public java.math.BigDecimal getAmountRecovered() {
        return amountRecovered;
    }

    public void setAmountRecovered(java.math.BigDecimal amountRecovered) {
        this.amountRecovered = amountRecovered;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Date getCheckDeadline() {
        return checkDeadline;
    }

    public void setCheckDeadline(Date checkDeadline) {
        this.checkDeadline = checkDeadline;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWarnLevel() {
        return warnLevel;
    }

    public void setWarnLevel(String warnLevel) {
        this.warnLevel = warnLevel;
    }

    public String getHandleType() {
        return handleType;
    }

    public void setHandleType(String handleType) {
        this.handleType = handleType;
    }

    public Date getHandleDate() {
        return handleDate;
    }

    public void setHandleDate(Date handleDate) {
        this.handleDate = handleDate;
    }

    public String getHandleDesc() {
        return handleDesc;
    }

    public void setHandleDesc(String handleDesc) {
        this.handleDesc = handleDesc;
    }

    public Long getPunishId() {
        return punishId;
    }

    public void setPunishId(Long punishId) {
        this.punishId = punishId;
    }

    public Long getRectifyId() {
        return rectifyId;
    }

    public void setRectifyId(Long rectifyId) {
        this.rectifyId = rectifyId;
    }

    public Date getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(Date closeTime) {
        this.closeTime = closeTime;
    }

    public Long getRemainDays() {
        return remainDays;
    }

    public void setRemainDays(Long remainDays) {
        this.remainDays = remainDays;
    }

    public String getRectifyStatus() {
        return rectifyStatus;
    }

    public void setRectifyStatus(String rectifyStatus) {
        this.rectifyStatus = rectifyStatus;
    }
}
