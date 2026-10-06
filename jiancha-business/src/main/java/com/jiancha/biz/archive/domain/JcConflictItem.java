package com.jiancha.biz.archive.domain;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 利益冲突申报事项明细 jc_conflict_item（企业版新增 E1）
 *
 * @author 小标
 */
public class JcConflictItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long itemId;
    private Long declareId;
    /** 事项类型（字典 jc_conflict_item_type：经商办企业/兼职取酬/亲属从业/对外投资/关联交易/劳务报酬/其他） */
    private String itemType;
    /** 事项描述 */
    private String itemDesc;
    /** 关联企业/经营主体名称 */
    private String entName;
    /** 统一社会信用代码 */
    private String entCreditCode;
    /** 持股/出资比例(%) */
    private BigDecimal holdRatio;
    /** 涉及关联人 */
    private String relationPerson;
    /** 是否构成利益冲突（0否 1是） */
    private String isConflict;
    /** 处置建议 */
    private String handleAdvice;

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public Long getDeclareId() { return declareId; }
    public void setDeclareId(Long declareId) { this.declareId = declareId; }
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public String getItemDesc() { return itemDesc; }
    public void setItemDesc(String itemDesc) { this.itemDesc = itemDesc; }
    public String getEntName() { return entName; }
    public void setEntName(String entName) { this.entName = entName; }
    public String getEntCreditCode() { return entCreditCode; }
    public void setEntCreditCode(String entCreditCode) { this.entCreditCode = entCreditCode; }
    public BigDecimal getHoldRatio() { return holdRatio; }
    public void setHoldRatio(BigDecimal holdRatio) { this.holdRatio = holdRatio; }
    public String getRelationPerson() { return relationPerson; }
    public void setRelationPerson(String relationPerson) { this.relationPerson = relationPerson; }
    public String getIsConflict() { return isConflict; }
    public void setIsConflict(String isConflict) { this.isConflict = isConflict; }
    public String getHandleAdvice() { return handleAdvice; }
    public void setHandleAdvice(String handleAdvice) { this.handleAdvice = handleAdvice; }
}
