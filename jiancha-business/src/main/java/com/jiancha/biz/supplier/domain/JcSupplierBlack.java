package com.jiancha.biz.supplier.domain;

import java.util.Date;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 供应商黑名单记录 jc_supplier_black（E4）
 *
 * <p>拉黑必须填写原因与期限（black_until 为空视为永久）；到期由 Quartz 日调度
 * 自动解除，也可人工提前解除（必须填解除意见，全程留痕）。拉黑/解除联动
 * jc_supplier.status（黑名单 / 恢复正常）。</p>
 *
 * @author 小标
 */
public class JcSupplierBlack extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 黑名单ID */
    private Long blackId;

    /** 供应商ID */
    private Long supplierId;

    /** 拉黑原因 */
    private String blackReason;

    /** 拉黑日期 */
    private Date blackDate;

    /** 解除到期日（NULL=永久） */
    private Date blackUntil;

    /** 状态（0生效 1已解除） */
    private String status;

    /** 解除日期 */
    private Date liftDate;

    /** 解除操作人 */
    private String liftBy;

    /** 解除意见 */
    private String liftOpinion;

    /** 供应商名称（非持久化，列表 JOIN jc_supplier 展示用） */
    private String supplierName;

    public Long getBlackId() {
        return blackId;
    }

    public void setBlackId(Long blackId) {
        this.blackId = blackId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getBlackReason() {
        return blackReason;
    }

    public void setBlackReason(String blackReason) {
        this.blackReason = blackReason;
    }

    public Date getBlackDate() {
        return blackDate;
    }

    public void setBlackDate(Date blackDate) {
        this.blackDate = blackDate;
    }

    public Date getBlackUntil() {
        return blackUntil;
    }

    public void setBlackUntil(Date blackUntil) {
        this.blackUntil = blackUntil;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getLiftDate() {
        return liftDate;
    }

    public void setLiftDate(Date liftDate) {
        this.liftDate = liftDate;
    }

    public String getLiftBy() {
        return liftBy;
    }

    public void setLiftBy(String liftBy) {
        this.liftBy = liftBy;
    }

    public String getLiftOpinion() {
        return liftOpinion;
    }

    public void setLiftOpinion(String liftOpinion) {
        this.liftOpinion = liftOpinion;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }
}
