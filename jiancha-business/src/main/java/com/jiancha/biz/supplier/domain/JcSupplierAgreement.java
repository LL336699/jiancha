package com.jiancha.biz.supplier.domain;

import java.util.Date;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 供应商廉洁协议 jc_supplier_agreement（E4）
 *
 * <p>签署 / 续签 / 解除全生命周期管理；状态由 Quartz 日调度刷新：
 * 距到期 ≤ 预警天数（sys_config jiancha.supplier.agreement.warn.days）转「临期」，
 * 超过到期日转「已过期」，办理业务前应要求先续签。</p>
 *
 * @author 小标
 */
public class JcSupplierAgreement extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 协议ID */
    private Long agreementId;

    /** 供应商ID */
    private Long supplierId;

    /** 协议编号（唯一，如 HA20260001） */
    private String agreementNo;

    /** 签署日期 */
    private Date signDate;

    /** 到期日期 */
    private Date expireDate;

    /** 协议文件名 */
    private String fileName;

    /** 协议文件URL（附件） */
    private String fileUrl;

    /** 状态（字典 jc_agreement_status：已签署/临期/已过期/已解除） */
    private String status;

    /** 续签次数 */
    private Integer renewCount;

    /** 最近续签日期 */
    private Date lastRenewDate;

    /** 供应商名称（非持久化，列表 JOIN jc_supplier 展示用） */
    private String supplierName;

    public Long getAgreementId() {
        return agreementId;
    }

    public void setAgreementId(Long agreementId) {
        this.agreementId = agreementId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getAgreementNo() {
        return agreementNo;
    }

    public void setAgreementNo(String agreementNo) {
        this.agreementNo = agreementNo;
    }

    public Date getSignDate() {
        return signDate;
    }

    public void setSignDate(Date signDate) {
        this.signDate = signDate;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getRenewCount() {
        return renewCount;
    }

    public void setRenewCount(Integer renewCount) {
        this.renewCount = renewCount;
    }

    public Date getLastRenewDate() {
        return lastRenewDate;
    }

    public void setLastRenewDate(Date lastRenewDate) {
        this.lastRenewDate = lastRenewDate;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }
}
