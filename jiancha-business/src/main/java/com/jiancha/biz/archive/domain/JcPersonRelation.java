package com.jiancha.biz.archive.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 关联关系及经商办企业 / 合作方任职情况 jc_person_relation（企业口径）
 *
 * <p>企业版在原有亲属关系基础上扩展了"关联企业主体、持股性质、是否本公司供应商/客户"
 * 等字段，用于支撑利益冲突排查与企业版风险预警规则。</p>
 *
 * @author 小标
 */
public class JcPersonRelation extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long relationId;
    private Long personId;
    /** 关系（字典 jc_relation_type：本人/配偶/子女/父母/兄弟姐妹/其他亲属/同学/朋友） */
    private String relationType;
    /** 关联人姓名 */
    private String relationName;
    /** 任职单位/经营主体 */
    private String relationWork;
    /** 职务 */
    private String relationDuty;
    /** 关联企业/经营主体名称 */
    private String entName;
    /** 统一社会信用代码 */
    private String entCreditCode;
    /** 关联性质（字典 jc_relation_kind：持股/任职/兼职取酬/实际控制/劳务报酬/其他） */
    private String relationKind;
    /** 持股/出资比例(%) */
    private BigDecimal holdRatio;
    /** 是否本公司供应商（0否 1是） */
    private String isSupplier;
    /** 是否本公司客户（0否 1是） */
    private String isCustomer;
    /** 合作方类型（字典 jc_partner_type：供应商/客户/承包商/服务商/其他） */
    private String partnerType;
    /** 关联起始日期 */
    private Date startDate;
    /** 关联结束日期 */
    private Date endDate;
    /** 是否境外（0否 1是） */
    private String isAbroad;
    /** 本人是否关键岗位（0否 1是） */
    private String isKeyPosition;
    /** 核实状态（字典 jc_verify_status：未核实/已核实/存疑） */
    private String verifyStatus;
    /** 数据来源（字典 jc_data_source：申报/排查/举报/人工录入） */
    private String dataSource;

    public Long getRelationId() { return relationId; }
    public void setRelationId(Long relationId) { this.relationId = relationId; }
    public Long getPersonId() { return personId; }
    public void setPersonId(Long personId) { this.personId = personId; }
    public String getRelationType() { return relationType; }
    public void setRelationType(String relationType) { this.relationType = relationType; }
    public String getRelationName() { return relationName; }
    public void setRelationName(String relationName) { this.relationName = relationName; }
    public String getRelationWork() { return relationWork; }
    public void setRelationWork(String relationWork) { this.relationWork = relationWork; }
    public String getRelationDuty() { return relationDuty; }
    public void setRelationDuty(String relationDuty) { this.relationDuty = relationDuty; }
    public String getEntName() { return entName; }
    public void setEntName(String entName) { this.entName = entName; }
    public String getEntCreditCode() { return entCreditCode; }
    public void setEntCreditCode(String entCreditCode) { this.entCreditCode = entCreditCode; }
    public String getRelationKind() { return relationKind; }
    public void setRelationKind(String relationKind) { this.relationKind = relationKind; }
    public BigDecimal getHoldRatio() { return holdRatio; }
    public void setHoldRatio(BigDecimal holdRatio) { this.holdRatio = holdRatio; }
    public String getIsSupplier() { return isSupplier; }
    public void setIsSupplier(String isSupplier) { this.isSupplier = isSupplier; }
    public String getIsCustomer() { return isCustomer; }
    public void setIsCustomer(String isCustomer) { this.isCustomer = isCustomer; }
    public String getPartnerType() { return partnerType; }
    public void setPartnerType(String partnerType) { this.partnerType = partnerType; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public String getIsAbroad() { return isAbroad; }
    public void setIsAbroad(String isAbroad) { this.isAbroad = isAbroad; }
    public String getIsKeyPosition() { return isKeyPosition; }
    public void setIsKeyPosition(String isKeyPosition) { this.isKeyPosition = isKeyPosition; }
    public String getVerifyStatus() { return verifyStatus; }
    public void setVerifyStatus(String verifyStatus) { this.verifyStatus = verifyStatus; }
    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
}
