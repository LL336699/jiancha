package com.jiancha.biz.clue.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 问题线索主表 jc_clue
 *
 * <p>企业版口径：线索来源为企业内部举报 / 审计 / 专项检查 / 合规检查 / 上级单位交办 / AI 预警；
 * 处置方式为谈话提醒、内部核查、纪律处分、整改处理、移交司法等，不含党政执法措施。</p>
 *
 * @author 小标
 */
public class JcClue extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 线索ID */
    private Long clueId;

    /** 线索编号（JC + 日期 + 流水） */
    @Excel(name = "线索编号")
    private String clueNo;

    /** 来源类型（字典 jc_clue_source） */
    @Excel(name = "来源类型")
    private String sourceType;

    /** 问题类型（字典 jc_violation_type） */
    @Excel(name = "问题类型")
    private String violationType;

    /** 涉及人员ID */
    private Long personId;

    /** 关联人员（多人，逗号分隔） */
    private String relatedPersons;

    /** 线索摘要 */
    @Excel(name = "线索摘要")
    private String summary;

    /** 详细情况 */
    private String detail;

    /** 承办部门ID */
    private Long deptId;

    /** 当前处理人ID */
    private Long handlerId;

    /** 办理状态（字典 jc_clue_status） */
    @Excel(name = "办理状态")
    private String status;

    /** 处置方式（字典 jc_clue_disposition） */
    @Excel(name = "处置方式")
    private String disposition;

    /** 办理截止日期 */
    @Excel(name = "截止日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date deadline;

    /** 预警级别（字典 jc_warn_level） */
    @Excel(name = "预警级别")
    private String warnLevel;

    /** 是否重复线索（0否 1是） */
    private String isDuplicate;

    /** 重复指向的线索ID */
    private Long duplicateOf;

    /** 来源文号 / 编号 */
    private String sourceRef;

    // ───────────── 展示扩展字段（非表字段，列表 JOIN 得出）─────────────

    /** 涉及人员姓名 */
    private String personName;

    /** 承办部门名称 */
    private String deptName;

    /** 当前处理人姓名 */
    private String handlerName;

    /** 剩余天数（负数表示已超期） */
    private Long remainDays;

    public Long getClueId() {
        return clueId;
    }

    public void setClueId(Long clueId) {
        this.clueId = clueId;
    }

    public String getClueNo() {
        return clueNo;
    }

    public void setClueNo(String clueNo) {
        this.clueNo = clueNo;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getViolationType() {
        return violationType;
    }

    public void setViolationType(String violationType) {
        this.violationType = violationType;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public String getRelatedPersons() {
        return relatedPersons;
    }

    public void setRelatedPersons(String relatedPersons) {
        this.relatedPersons = relatedPersons;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Long getHandlerId() {
        return handlerId;
    }

    public void setHandlerId(Long handlerId) {
        this.handlerId = handlerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDisposition() {
        return disposition;
    }

    public void setDisposition(String disposition) {
        this.disposition = disposition;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public String getWarnLevel() {
        return warnLevel;
    }

    public void setWarnLevel(String warnLevel) {
        this.warnLevel = warnLevel;
    }

    public String getIsDuplicate() {
        return isDuplicate;
    }

    public void setIsDuplicate(String isDuplicate) {
        this.isDuplicate = isDuplicate;
    }

    public Long getDuplicateOf() {
        return duplicateOf;
    }

    public void setDuplicateOf(Long duplicateOf) {
        this.duplicateOf = duplicateOf;
    }

    public String getSourceRef() {
        return sourceRef;
    }

    public void setSourceRef(String sourceRef) {
        this.sourceRef = sourceRef;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    public Long getRemainDays() {
        return remainDays;
    }

    public void setRemainDays(Long remainDays) {
        this.remainDays = remainDays;
    }
}
