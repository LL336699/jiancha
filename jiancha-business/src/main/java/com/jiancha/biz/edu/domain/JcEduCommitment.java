package com.jiancha.biz.edu.domain;

import jakarta.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 廉洁承诺书签收 jc_edu_commitment（M9 廉政教育题库）
 *
 * <p>企业版口径（设计文档 §M9「承诺书签收」）：承诺书发布 → 指定范围人员签收
 * （一人一条签收记录），签收留时间戳；统计口径 = 已签收 / 应签收。</p>
 *
 * @author 小标
 */
public class JcEduCommitment extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 承诺书ID */
    private Long commitId;

    /** 承诺书标题 */
    @NotBlank(message = "承诺书标题不能为空")
    private String title;

    /** 正文内容 */
    private String content;

    /** 适用范围（字典 jc_edu_commit_scope：全员/关键岗位/新员工/指定人员） */
    private String scopeType;

    /** 签收截止日期 */
    private java.util.Date deadline;

    /** 状态（字典 jc_edu_commit_status：草稿/已发布/已关闭） */
    private String status;

    /** 签收人ID（签收记录时填写；发布态为空） */
    private Long personId;

    /** 签收人姓名（快照） */
    private String personName;

    /** 签收时间（未签收为空） */
    private java.util.Date signTime;

    /** 签收方式（字典 jc_edu_sign_type：系统签收） */
    private String signType;

    /** 记录类型（非持久化过滤用：0 承诺书本体 / 1 签收记录） */
    private String recordType;

    public Long getCommitId() {
        return commitId;
    }

    public void setCommitId(Long commitId) {
        this.commitId = commitId;
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

    public String getScopeType() {
        return scopeType;
    }

    public void setScopeType(String scopeType) {
        this.scopeType = scopeType;
    }

    public java.util.Date getDeadline() {
        return deadline;
    }

    public void setDeadline(java.util.Date deadline) {
        this.deadline = deadline;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public java.util.Date getSignTime() {
        return signTime;
    }

    public void setSignTime(java.util.Date signTime) {
        this.signTime = signTime;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }
}
