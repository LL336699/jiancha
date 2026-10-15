package com.jiancha.biz.asset.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 涉案财物台账（段11 E6）
 *
 * 状态机：0 已扣押待入库 / 1 入库暂存 / 2 处置中 / 3 已处置 / 4 已退还
 * 编号规则：CA + yyyyMMdd + 3 位序号
 * 全程流转记录落 jc_asset_flow，超期未处置由 Quartz 日刷红黄灯
 */
public class JcAssetInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 财物主键 */
    private Long assetId;

    /** 财物编号（CA+日期+序号） */
    private String assetNo;

    /** 来源类型（clue 线索 / conduct 作风 / petition 举报 / manual 手工登记） */
    private String sourceType;

    /** 来源单号（业务单号快照，如 CF20261015001） */
    private String sourceNo;

    /** 涉事人员ID（来源带出或手工指定） */
    private Long personId;

    /** 涉事人姓名（档案快照） */
    private String personName;

    /** 涉事人部门（档案快照） */
    private String personDept;

    /** 物品名称 */
    private String itemName;

    /** 物品类别（字典 jc_asset_category：现金/礼品/有价证券/电子设备/贵重物品/其他） */
    private String itemCategory;

    /** 数量 */
    private Integer itemQty;

    /** 单位（张/个/台/件/批） */
    private String unit;

    /** 估值（元） */
    private BigDecimal estimatedValue;

    /** 保管人 */
    private String keeper;

    /** 存放地点 */
    private String storagePlace;

    /** 扣押日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date seizeDate;

    /** 入库日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date storeDate;

    /** 处置期限 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date disposeDeadline;

    /** 处置方式（字典 jc_asset_dispose：退还/移交/销毁/收归/暂存待处） */
    private String disposeType;

    /** 处置日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date disposeDate;

    /** 处置说明 */
    private String disposeDesc;

    /** 状态（0待入库/1暂存中/2处置中/3已处置/4已退还） */
    private String status;

    /** 预警灯色（red/yellow/空，Quartz 日刷） */
    private String warnLevel;

    /** 距处置期限剩余天数（负数=已超期，聚合展示字段） */
    private Integer remainDays;

    /** 流转次数（聚合展示字段） */
    private Integer flowCount;

    public Long getAssetId()
    {
        return assetId;
    }

    public void setAssetId(Long assetId)
    {
        this.assetId = assetId;
    }

    public String getAssetNo()
    {
        return assetNo;
    }

    public void setAssetNo(String assetNo)
    {
        this.assetNo = assetNo;
    }

    public String getSourceType()
    {
        return sourceType;
    }

    public void setSourceType(String sourceType)
    {
        this.sourceType = sourceType;
    }

    public String getSourceNo()
    {
        return sourceNo;
    }

    public void setSourceNo(String sourceNo)
    {
        this.sourceNo = sourceNo;
    }

    public Long getPersonId()
    {
        return personId;
    }

    public void setPersonId(Long personId)
    {
        this.personId = personId;
    }

    public String getPersonName()
    {
        return personName;
    }

    public void setPersonName(String personName)
    {
        this.personName = personName;
    }

    public String getPersonDept()
    {
        return personDept;
    }

    public void setPersonDept(String personDept)
    {
        this.personDept = personDept;
    }

    public String getItemName()
    {
        return itemName;
    }

    public void setItemName(String itemName)
    {
        this.itemName = itemName;
    }

    public String getItemCategory()
    {
        return itemCategory;
    }

    public void setItemCategory(String itemCategory)
    {
        this.itemCategory = itemCategory;
    }

    public Integer getItemQty()
    {
        return itemQty;
    }

    public void setItemQty(Integer itemQty)
    {
        this.itemQty = itemQty;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public BigDecimal getEstimatedValue()
    {
        return estimatedValue;
    }

    public void setEstimatedValue(BigDecimal estimatedValue)
    {
        this.estimatedValue = estimatedValue;
    }

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

    public Date getSeizeDate()
    {
        return seizeDate;
    }

    public void setSeizeDate(Date seizeDate)
    {
        this.seizeDate = seizeDate;
    }

    public Date getStoreDate()
    {
        return storeDate;
    }

    public void setStoreDate(Date storeDate)
    {
        this.storeDate = storeDate;
    }

    public Date getDisposeDeadline()
    {
        return disposeDeadline;
    }

    public void setDisposeDeadline(Date disposeDeadline)
    {
        this.disposeDeadline = disposeDeadline;
    }

    public String getDisposeType()
    {
        return disposeType;
    }

    public void setDisposeType(String disposeType)
    {
        this.disposeType = disposeType;
    }

    public Date getDisposeDate()
    {
        return disposeDate;
    }

    public void setDisposeDate(Date disposeDate)
    {
        this.disposeDate = disposeDate;
    }

    public String getDisposeDesc()
    {
        return disposeDesc;
    }

    public void setDisposeDesc(String disposeDesc)
    {
        this.disposeDesc = disposeDesc;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getWarnLevel()
    {
        return warnLevel;
    }

    public void setWarnLevel(String warnLevel)
    {
        this.warnLevel = warnLevel;
    }

    public Integer getRemainDays()
    {
        return remainDays;
    }

    public void setRemainDays(Integer remainDays)
    {
        this.remainDays = remainDays;
    }

    public Integer getFlowCount()
    {
        return flowCount;
    }

    public void setFlowCount(Integer flowCount)
    {
        this.flowCount = flowCount;
    }
}
