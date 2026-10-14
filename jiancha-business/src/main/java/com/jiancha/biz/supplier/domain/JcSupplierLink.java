package com.jiancha.biz.supplier.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 供应商关联排查记录 jc_supplier_link（E4）
 *
 * <p>两类来源：人工排查登记（排查/举报/申报转来）与系统自动比对
 * （Quartz 日调度扫描 jc_person_relation / jc_conflict_item 中员工申报的
 * 关联企业，命中供应商库即生成比对记录）。命中后供应商自动转「观察」，
 * 核实属实可一键转整改问题（E3），rectify_issue_id 回指闭环。</p>
 *
 * @author 小标
 */
public class JcSupplierLink extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 关联ID */
    private Long linkId;

    /** 供应商ID */
    private Long supplierId;

    /** 关联员工ID（jc_person.person_id） */
    private Long personId;

    /** 关联员工姓名（冗余展示，避免跨模块 JOIN） */
    private String personName;

    /** 关联类型（字典 jc_supplier_link_type：亲属持股/本人持股/亲属任职/本人任职/兼职取酬/实际控制/其他） */
    private String linkType;

    /** 持股/出资比例(%) */
    private BigDecimal holdRatio;

    /** 发现渠道（字典 jc_supplier_source：申报/排查/举报/系统比对/人工录入） */
    private String sourceType;

    /** 证据说明（如申报编号、比对命中字段） */
    private String evidence;

    /** 核实状态（字典 jc_verify_status：未核实/已核实/存疑） */
    private String verifyStatus;

    /** 处置结果（字典 jc_link_handle：待处置/正常/观察/禁入/解除） */
    private String handleResult;

    /** 处置时间 */
    private Date handleTime;

    /** 处置人 */
    private String handleBy;

    /** 处置意见 */
    private String handleOpinion;

    /** 转整改问题ID（jc_rectify_issue.issue_id，回指 E3） */
    private Long rectifyIssueId;

    /** 转整改问题编号（非持久化，列表 JOIN jc_rectify_issue 展示用） */
    private String issueNo;

    /** 供应商名称（非持久化，列表 JOIN jc_supplier 展示用） */
    private String supplierName;

    public Long getLinkId() {
        return linkId;
    }

    public void setLinkId(Long linkId) {
        this.linkId = linkId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
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

    public String getLinkType() {
        return linkType;
    }

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    public BigDecimal getHoldRatio() {
        return holdRatio;
    }

    public void setHoldRatio(BigDecimal holdRatio) {
        this.holdRatio = holdRatio;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getEvidence() {
        return evidence;
    }

    public void setEvidence(String evidence) {
        this.evidence = evidence;
    }

    public String getVerifyStatus() {
        return verifyStatus;
    }

    public void setVerifyStatus(String verifyStatus) {
        this.verifyStatus = verifyStatus;
    }

    public String getHandleResult() {
        return handleResult;
    }

    public void setHandleResult(String handleResult) {
        this.handleResult = handleResult;
    }

    public Date getHandleTime() {
        return handleTime;
    }

    public void setHandleTime(Date handleTime) {
        this.handleTime = handleTime;
    }

    public String getHandleBy() {
        return handleBy;
    }

    public void setHandleBy(String handleBy) {
        this.handleBy = handleBy;
    }

    public String getHandleOpinion() {
        return handleOpinion;
    }

    public void setHandleOpinion(String handleOpinion) {
        this.handleOpinion = handleOpinion;
    }

    public Long getRectifyIssueId() {
        return rectifyIssueId;
    }

    public void setRectifyIssueId(Long rectifyIssueId) {
        this.rectifyIssueId = rectifyIssueId;
    }

    public String getIssueNo() {
        return issueNo;
    }

    public void setIssueNo(String issueNo) {
        this.issueNo = issueNo;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }
}
