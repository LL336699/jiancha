package com.jiancha.biz.asset.domain;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 涉案财物动作请求（段11 E6）
 *
 * 覆盖入库、移交保管、处置申请、处置执行、退还五类动作；
 * 每类动作落一条流转流水（经办人/日期/说明/凭据号）。
 */
public class AssetActionRequest implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 入库：保管人 */
    private String keeper;

    /** 入库/移交：存放地点 */
    private String storagePlace;

    /** 移交保管：接收方（新保管人） */
    private String handoverTo;

    /** 处置申请：处置方式（字典 jc_asset_dispose） */
    private String disposeType;

    /** 处置申请：处置期限 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date disposeDeadline;

    /** 处置执行/退还：处置或退还日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date actionDate;

    /** 处置执行：处置说明 */
    private String disposeDesc;

    /** 经办人（缺省当前登录用户） */
    private String operator;

    /** 凭据号 */
    private String voucherNo;

    /** 备注说明 */
    private String remark;

    public String getKeeper()
    {
        return keeper;
    }

    public void setKeeper(String keeper)
    {
        this.keeper = keeper;
    }

    public String getStoragePlace()
    {
        return storagePlace;
    }

    public void setStoragePlace(String storagePlace)
    {
        this.storagePlace = storagePlace;
    }

    public String getHandoverTo()
    {
        return handoverTo;
    }

    public void setHandoverTo(String handoverTo)
    {
        this.handoverTo = handoverTo;
    }

    public String getDisposeType()
    {
        return disposeType;
    }

    public void setDisposeType(String disposeType)
    {
        this.disposeType = disposeType;
    }

    public Date getDisposeDeadline()
    {
        return disposeDeadline;
    }

    public void setDisposeDeadline(Date disposeDeadline)
    {
        this.disposeDeadline = disposeDeadline;
    }

    public Date getActionDate()
    {
        return actionDate;
    }

    public void setActionDate(Date actionDate)
    {
        this.actionDate = actionDate;
    }

    public String getDisposeDesc()
    {
        return disposeDesc;
    }

    public void setDisposeDesc(String disposeDesc)
    {
        this.disposeDesc = disposeDesc;
    }

    public String getOperator()
    {
        return operator;
    }

    public void setOperator(String operator)
    {
        this.operator = operator;
    }

    public String getVoucherNo()
    {
        return voucherNo;
    }

    public void setVoucherNo(String voucherNo)
    {
        this.voucherNo = voucherNo;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }
}
