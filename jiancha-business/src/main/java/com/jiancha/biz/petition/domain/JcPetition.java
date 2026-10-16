package com.jiancha.biz.petition.domain;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 舞弊举报主表 jc_petition
 *
 * <p>企业版口径（设计文档 §M3）：面向舞弊举报（采购回扣 / 串标围标 / 虚报冒领 / 吃拿卡要 /
 * 利益冲突），举报入口免登录，支持匿名与实名；敏感字段（举报人姓名 / 电话 / 身份证）
 * AES-GCM 字段级加密存储，管理端按权限解密；回执码只存哈希，举报人凭回执码跟踪进度。</p>
 *
 * <p>状态机（严格单向，不可跳步）：0待受理 → 1已受理 → 2研判中 → 3已分办 → 4办理中
 * → 5已反馈 → 6已归档；1/2 环节可作不予受理（7，终态）。</p>
 *
 * @author 小标
 */
public class JcPetition extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 举报ID */
    private Long petitionId;

    /** 回执码（16位，仅提交成功时返回一次；落库只有哈希） */
    private String receiptCode;

    /** 回执码检索哈希（SHA-256(码+pepper)，建唯一索引，用于 O(1) 定位） */
    private String receiptLookup;

    /** 回执码 BCrypt 哈希（定位后二次校验，防索引碰撞/彩虹表） */
    private String receiptHash;

    /** 事由标题 */
    @Excel(name = "事由标题")
    private String title;

    /** 举报内容 */
    private String content;

    /** 提交渠道（字典 jc_petition_channel：web/email/phone） */
    private String channel;

    /** 舞弊类型（字典 jc_petition_category） */
    @Excel(name = "舞弊类型")
    private String category;

    /** 是否匿名（0实名 1匿名） */
    @Excel(name = "匿名/实名", readConverterExp = "0=实名,1=匿名")
    private String anonymousFlag;

    /** 举报人姓名（AES-GCM 密文，管理端按权限解密） */
    private String reporterName;

    /** 举报人电话（AES-GCM 密文） */
    private String reporterPhone;

    /** 举报人身份证号（AES-GCM 密文） */
    private String reporterIdcard;

    /** 被举报对象姓名 */
    @Excel(name = "被举报对象")
    private String targetName;

    /** 被举报对象所在部门 */
    @Excel(name = "对象所在部门")
    private String targetDept;

    /** 涉及金额（元） */
    @Excel(name = "涉及金额")
    private BigDecimal amount;

    /** 办理状态（0待受理 1已受理 2研判中 3已分办 4办理中 5已反馈 6已归档 7不予受理） */
    @Excel(name = "办理状态")
    private String status;

    /** 预警级别（""/黄灯/红灯，与线索口径一致） */
    @Excel(name = "预警级别")
    private String warnLevel;

    /** 办理截止时间（受理时按 sys_config 时限计算） */
    @Excel(name = "办理截止时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date handleDeadline;

    /** 分办转线索后关联的线索ID */
    private Long clueId;

    /** 反馈内容（状态达到已反馈后，举报人凭回执码可见） */
    private String feedback;

    /** 举报人满意度（1-5，已反馈后凭回执码评价，段16） */
    @Excel(name = "满意度(1-5)")
    private Integer satisfaction;

    /** 评价说明（段16） */
    @Excel(name = "评价说明")
    private String satisfactionComment;

    /** 评价时间（段16） */
    @Excel(name = "评价时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date satisfactionTime;

    /** 催办次数（段16） */
    @Excel(name = "催办次数")
    private Integer urgeCount;

    /** 最近催办时间（段16） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUrgeTime;

    /** 并案主件举报ID（NULL=独立件；段16 并案管理） */
    private Long mergedInto;

    /** 并案说明（为何并入主件；段16） */
    private String mergedRemark;

    /** 并案主件事由标题（联查展示，不落库；段16） */
    private String mergedTitle;

    /** 关联线索编号（展示字段，来自 jc_clue.clue_no） */
    private String clueNo;

    /** 剩余办理小时数（展示字段，负数表示已超期） */
    private Long remainHours;

    public Long getPetitionId() {
        return petitionId;
    }

    public void setPetitionId(Long petitionId) {
        this.petitionId = petitionId;
    }

    public String getReceiptCode() {
        return receiptCode;
    }

    public void setReceiptCode(String receiptCode) {
        this.receiptCode = receiptCode;
    }

    public String getReceiptLookup() {
        return receiptLookup;
    }

    public void setReceiptLookup(String receiptLookup) {
        this.receiptLookup = receiptLookup;
    }

    public String getReceiptHash() {
        return receiptHash;
    }

    public void setReceiptHash(String receiptHash) {
        this.receiptHash = receiptHash;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAnonymousFlag() {
        return anonymousFlag;
    }

    public void setAnonymousFlag(String anonymousFlag) {
        this.anonymousFlag = anonymousFlag;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getReporterPhone() {
        return reporterPhone;
    }

    public void setReporterPhone(String reporterPhone) {
        this.reporterPhone = reporterPhone;
    }

    public String getReporterIdcard() {
        return reporterIdcard;
    }

    public void setReporterIdcard(String reporterIdcard) {
        this.reporterIdcard = reporterIdcard;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getTargetDept() {
        return targetDept;
    }

    public void setTargetDept(String targetDept) {
        this.targetDept = targetDept;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    public Date getHandleDeadline() {
        return handleDeadline;
    }

    public void setHandleDeadline(Date handleDeadline) {
        this.handleDeadline = handleDeadline;
    }

    public Long getClueId() {
        return clueId;
    }

    public void setClueId(Long clueId) {
        this.clueId = clueId;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Integer getSatisfaction() {
        return satisfaction;
    }

    public void setSatisfaction(Integer satisfaction) {
        this.satisfaction = satisfaction;
    }

    public String getSatisfactionComment() {
        return satisfactionComment;
    }

    public void setSatisfactionComment(String satisfactionComment) {
        this.satisfactionComment = satisfactionComment;
    }

    public Date getSatisfactionTime() {
        return satisfactionTime;
    }

    public void setSatisfactionTime(Date satisfactionTime) {
        this.satisfactionTime = satisfactionTime;
    }

    public Integer getUrgeCount() {
        return urgeCount;
    }

    public void setUrgeCount(Integer urgeCount) {
        this.urgeCount = urgeCount;
    }

    public Date getLastUrgeTime() {
        return lastUrgeTime;
    }

    public void setLastUrgeTime(Date lastUrgeTime) {
        this.lastUrgeTime = lastUrgeTime;
    }

    public Long getMergedInto() {
        return mergedInto;
    }

    public void setMergedInto(Long mergedInto) {
        this.mergedInto = mergedInto;
    }

    public String getMergedRemark() {
        return mergedRemark;
    }

    public void setMergedRemark(String mergedRemark) {
        this.mergedRemark = mergedRemark;
    }

    public String getMergedTitle() {
        return mergedTitle;
    }

    public void setMergedTitle(String mergedTitle) {
        this.mergedTitle = mergedTitle;
    }

    public String getClueNo() {
        return clueNo;
    }

    public void setClueNo(String clueNo) {
        this.clueNo = clueNo;
    }

    public Long getRemainHours() {
        return remainHours;
    }

    public void setRemainHours(Long remainHours) {
        this.remainHours = remainHours;
    }
}
