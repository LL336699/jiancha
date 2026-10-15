package com.jiancha.biz.reply.domain;

import java.util.Date;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 廉政意见回复申请（E2 廉政意见回复）
 *
 * <p>提拔/任用/评优前由纪检出具廉洁意见：申请登记 → 系统自动筛查（跨模块聚合）
 * → 人工核实 → 出具正式结论（无违纪/暂缓/否决）→ 归档。</p>
 *
 * @author 小标
 */
public class JcReplyRequest extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 申请ID */
    private Long requestId;

    /** 申请编号（RQ+yyyyMMdd+seq） */
    private String requestNo;

    /** 对象人员ID（jc_person.person_id） */
    private Long personId;

    /** 对象姓名（冗余快照） */
    private String personName;

    /** 对象部门（冗余快照） */
    private String personDept;

    /** 事项类型（字典 jc_reply_matter：提拔任用/评优评先/出国境/职称评聘/其他） */
    private String matterType;

    /** 事项说明（拟任岗位/评优名称等） */
    private String matterDesc;

    /** 申请单位（来人/来函单位） */
    private String applyDept;

    /** 申请联系人 */
    private String applyContact;

    /** 申请来文号 */
    private String applyDocNo;

    /** 收到日期 */
    private Date receiveDate;

    /** 办理时限（天，超时标红） */
    private Integer deadlineDays;

    /** 状态（0待筛查 1已筛查待核实 2已回复 3已关闭） */
    private String status;

    /** 系统建议结论（字典 jc_reply_conclusion：建议通过/建议暂缓/建议否决） */
    private String suggestResult;

    /** 正式结论（人工出具：无违纪/暂缓/否决） */
    private String finalResult;

    /** 回复内容（正式意见正文） */
    private String replyContent;

    /** 回复日期 */
    private Date replyDate;

    /** 回复人 */
    private String replyBy;

    /** 筛查命中数（展示） */
    private Integer hitCount;

    /** 剩余天数（负数=已超期，聚合展示字段） */
    private Integer remainDays;

    /** 预警级别（空/黄灯/红灯，Quartz 日调度刷新，口径同线索/整改） */
    private String warnLevel;

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
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

    public String getMatterType() {
        return matterType;
    }

    public void setMatterType(String matterType) {
        this.matterType = matterType;
    }

    public String getMatterDesc() {
        return matterDesc;
    }

    public void setMatterDesc(String matterDesc) {
        this.matterDesc = matterDesc;
    }

    public String getApplyDept() {
        return applyDept;
    }

    public void setApplyDept(String applyDept) {
        this.applyDept = applyDept;
    }

    public String getApplyContact() {
        return applyContact;
    }

    public void setApplyContact(String applyContact) {
        this.applyContact = applyContact;
    }

    public String getApplyDocNo() {
        return applyDocNo;
    }

    public void setApplyDocNo(String applyDocNo) {
        this.applyDocNo = applyDocNo;
    }

    public Date getReceiveDate() {
        return receiveDate;
    }

    public void setReceiveDate(Date receiveDate) {
        this.receiveDate = receiveDate;
    }

    public Integer getDeadlineDays() {
        return deadlineDays;
    }

    public void setDeadlineDays(Integer deadlineDays) {
        this.deadlineDays = deadlineDays;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSuggestResult() {
        return suggestResult;
    }

    public void setSuggestResult(String suggestResult) {
        this.suggestResult = suggestResult;
    }

    public String getFinalResult() {
        return finalResult;
    }

    public void setFinalResult(String finalResult) {
        this.finalResult = finalResult;
    }

    public String getReplyContent() {
        return replyContent;
    }

    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }

    public Date getReplyDate() {
        return replyDate;
    }

    public void setReplyDate(Date replyDate) {
        this.replyDate = replyDate;
    }

    public String getReplyBy() {
        return replyBy;
    }

    public void setReplyBy(String replyBy) {
        this.replyBy = replyBy;
    }

    public Integer getHitCount() {
        return hitCount;
    }

    public void setHitCount(Integer hitCount) {
        this.hitCount = hitCount;
    }

    public Integer getRemainDays() {
        return remainDays;
    }

    public void setRemainDays(Integer remainDays) {
        this.remainDays = remainDays;
    }

    public String getWarnLevel() {
        return warnLevel;
    }

    public void setWarnLevel(String warnLevel) {
        this.warnLevel = warnLevel;
    }
}
