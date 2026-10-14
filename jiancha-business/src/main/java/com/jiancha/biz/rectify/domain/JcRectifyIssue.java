package com.jiancha.biz.rectify.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 整改问题台账主表 jc_rectify_issue
 *
 * <p>企业版口径（设计文档 §E3）：审计 / 检查 / 巡察 / 风险预警 / 举报 / 线索发现的问题，
 * 统一登记立案，走「整改 → 销号 → 回头看」闭环；预警类问题由 M10 风控模块转入，
 * 举报 / 线索类由 M3 / M4 模块转入（source_ref 回指来源单号）。</p>
 *
 * <p>状态机（严格单向 + 回头看重开环）：0待整改 → 1整改中 → 2待复核 → 3已销号；
 * 2 可退回到 1（复核不通过）；3 可经回头看重开回 1（复发 / 整改不实）。</p>
 *
 * @author 小标
 */
public class JcRectifyIssue extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 问题ID */
    private Long issueId;

    /** 问题编号（RX+yyyyMMdd+seq） */
    @Excel(name = "问题编号")
    private String issueNo;

    /** 来源类型（字典 jc_rectify_source：audit/check/inspection/warn/petition/clue） */
    private String sourceType;

    /** 来源单号（如 petition:12 / clue:JC20261013001 / warn:35） */
    private String sourceRef;

    /** 问题标题 */
    @Excel(name = "问题标题")
    private String title;

    /** 问题描述 */
    private String description;

    /** 问题类型（字典 jc_rectify_type） */
    private String problemType;

    /** 严重程度（字典 jc_rectify_severity：一般/较大/重大） */
    private String severity;

    /** 发现日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date foundDate;

    /** 发现问题单位/部门名称 */
    private String foundDept;

    /** 整改责任部门ID（sys_dept） */
    private Long targetDeptId;

    /** 整改责任人ID（jc_person，可选） */
    private Long targetPersonId;

    /** 整改截止日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date deadline;

    /** 状态（0待整改 1整改中 2待复核 3已销号 4已作废） */
    @Excel(name = "状态")
    private String status;

    /** 预警级别（""/黄灯/红灯） */
    private String warnLevel;

    /** 整改情况综述（销号复核时可见） */
    private String rectifySummary;

    /** 销号时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date closeTime;

    /** 整改措施数（展示字段） */
    private Integer taskCount;

    /** 已完成措施数（展示字段） */
    private Integer taskDoneCount;

    /** 剩余天数（展示字段，负数表示已超期） */
    private Long remainDays;

    /** 责任部门名称（展示字段） */
    private String deptName;

    /** 责任人姓名（展示字段） */
    private String personName;

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

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceRef() {
        return sourceRef;
    }

    public void setSourceRef(String sourceRef) {
        this.sourceRef = sourceRef;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProblemType() {
        return problemType;
    }

    public void setProblemType(String problemType) {
        this.problemType = problemType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Date getFoundDate() {
        return foundDate;
    }

    public void setFoundDate(Date foundDate) {
        this.foundDate = foundDate;
    }

    public String getFoundDept() {
        return foundDept;
    }

    public void setFoundDept(String foundDept) {
        this.foundDept = foundDept;
    }

    public Long getTargetDeptId() {
        return targetDeptId;
    }

    public void setTargetDeptId(Long targetDeptId) {
        this.targetDeptId = targetDeptId;
    }

    public Long getTargetPersonId() {
        return targetPersonId;
    }

    public void setTargetPersonId(Long targetPersonId) {
        this.targetPersonId = targetPersonId;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
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

    public String getRectifySummary() {
        return rectifySummary;
    }

    public void setRectifySummary(String rectifySummary) {
        this.rectifySummary = rectifySummary;
    }

    public Date getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(Date closeTime) {
        this.closeTime = closeTime;
    }

    public Integer getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(Integer taskCount) {
        this.taskCount = taskCount;
    }

    public Integer getTaskDoneCount() {
        return taskDoneCount;
    }

    public void setTaskDoneCount(Integer taskDoneCount) {
        this.taskDoneCount = taskDoneCount;
    }

    public Long getRemainDays() {
        return remainDays;
    }

    public void setRemainDays(Long remainDays) {
        this.remainDays = remainDays;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }
}
