package com.jiancha.biz.asset.domain;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 涉案财物流转记录（段11 E6）
 *
 * 财物每发生一次状态变迁（扣押登记/入库/移交保管/处置申请/处置执行/退还/出库），
 * 均落一条流水，形成全程可追溯的保管链。
 */
public class JcAssetFlow extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 流水主键 */
    private Long flowId;

    /** 关联财物ID */
    private Long assetId;

    /** 流转类型（seize 扣押登记 / store 入库 / handover 移交保管 / disposeApply 处置申请 / dispose 处置执行 / refund 退还 / outbound 出库） */
    private String flowType;

    /** 流转前状态 */
    private String fromStatus;

    /** 流转后状态 */
    private String toStatus;

    /** 经办人 */
    private String operator;

    /** 流转日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date flowDate;

    /** 流转说明 */
    private String flowDesc;

    /** 凭据号（移交单/退还签收单/销毁清单编号等） */
    private String voucherNo;

    public Long getFlowId()
    {
        return flowId;
    }

    public void setFlowId(Long flowId)
    {
        this.flowId = flowId;
    }

    public Long getAssetId()
    {
        return assetId;
    }

    public void setAssetId(Long assetId)
    {
        this.assetId = assetId;
    }

    public String getFlowType()
    {
        return flowType;
    }

    public void setFlowType(String flowType)
    {
        this.flowType = flowType;
    }

    public String getFromStatus()
    {
        return fromStatus;
    }

    public void setFromStatus(String fromStatus)
    {
        this.fromStatus = fromStatus;
    }

    public String getToStatus()
    {
        return toStatus;
    }

    public void setToStatus(String toStatus)
    {
        this.toStatus = toStatus;
    }

    public String getOperator()
    {
        return operator;
    }

    public void setOperator(String operator)
    {
        this.operator = operator;
    }

    public Date getFlowDate()
    {
        return flowDate;
    }

    public void setFlowDate(Date flowDate)
    {
        this.flowDate = flowDate;
    }

    public String getFlowDesc()
    {
        return flowDesc;
    }

    public void setFlowDesc(String flowDesc)
    {
        this.flowDesc = flowDesc;
    }

    public String getVoucherNo()
    {
        return voucherNo;
    }

    public void setVoucherNo(String voucherNo)
    {
        this.voucherNo = voucherNo;
    }
}
