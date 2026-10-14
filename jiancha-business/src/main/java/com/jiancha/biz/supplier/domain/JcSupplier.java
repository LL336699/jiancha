package com.jiancha.biz.supplier.domain;

import java.util.Date;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 供应商廉洁档案 jc_supplier（E4 供应商廉洁管理）
 *
 * <p>企业版口径（设计文档 §1.4 / E4）：供应商建档 → 准入审查 → 廉洁协议 → 关联排查
 * → 黑名单管理，与 E1 利益冲突申报（jc_person_relation / jc_conflict_item 的关联企业）
 * 联动做系统比对，命中关联的供应商自动转「观察」并可一键转整改（E3）。</p>
 *
 * @author 小标
 */
public class JcSupplier extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 供应商ID */
    private Long supplierId;

    /** 供应商编号（唯一，如 SUP20260001） */
    private String supplierCode;

    /** 供应商名称 */
    private String supplierName;

    /** 统一社会信用代码（唯一） */
    private String creditCode;

    /** 供应商类别（字典 jc_supplier_category：物资/设备/工程/服务/物流/其他） */
    private String category;

    /** 联系人 */
    private String contactName;

    /** 联系电话 */
    private String contactPhone;

    /** 电子邮箱 */
    private String contactEmail;

    /** 地址 */
    private String address;

    /** 状态（字典 jc_supplier_status：待审查/正常/观察/禁入/黑名单） */
    private String status;

    /** 准入审查日期 */
    private Date admitDate;

    /** 禁入/黑名单到期日（NULL=永久，到期由 Quartz 自动解除） */
    private Date blackUntil;

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierCode() {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        this.supplierCode = supplierCode;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getCreditCode() {
        return creditCode;
    }

    public void setCreditCode(String creditCode) {
        this.creditCode = creditCode;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getAdmitDate() {
        return admitDate;
    }

    public void setAdmitDate(Date admitDate) {
        this.admitDate = admitDate;
    }

    public Date getBlackUntil() {
        return blackUntil;
    }

    public void setBlackUntil(Date blackUntil) {
        this.blackUntil = blackUntil;
    }
}
