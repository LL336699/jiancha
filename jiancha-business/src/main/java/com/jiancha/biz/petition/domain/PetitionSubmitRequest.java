package com.jiancha.biz.petition.domain;

import java.math.BigDecimal;

/**
 * 举报提交请求（免登录举报箱使用）
 *
 * <p>承载举报人一次提交所需的全部要素；captchaUuid / captchaCode 仅在
 * {@code jiancha.petition.captcha} 开关打开时必填。</p>
 *
 * @author 小标
 */
public class PetitionSubmitRequest {

    /** 事由标题（必填） */
    private String title;

    /** 举报内容（必填） */
    private String content;

    /** 舞弊类型（字典 jc_petition_category，必填） */
    private String category;

    /** 提交渠道（字典 jc_petition_channel，默认 web） */
    private String channel;

    /** 是否匿名（0实名 1匿名，默认 1） */
    private String anonymousFlag;

    /** 举报人姓名（实名时必填，加密存储） */
    private String reporterName;

    /** 举报人电话（实名时必填，加密存储） */
    private String reporterPhone;

    /** 举报人身份证号（选填，加密存储） */
    private String reporterIdcard;

    /** 被举报对象姓名（选填） */
    private String targetName;

    /** 被举报对象所在部门（选填） */
    private String targetDept;

    /** 涉及金额（元，选填） */
    private BigDecimal amount;

    /** 验证码uuid（开关打开时必填） */
    private String captchaUuid;

    /** 验证码答案（开关打开时必填） */
    private String captchaCode;

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
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

    public String getCaptchaUuid() {
        return captchaUuid;
    }

    public void setCaptchaUuid(String captchaUuid) {
        this.captchaUuid = captchaUuid;
    }

    public String getCaptchaCode() {
        return captchaCode;
    }

    public void setCaptchaCode(String captchaCode) {
        this.captchaCode = captchaCode;
    }
}
