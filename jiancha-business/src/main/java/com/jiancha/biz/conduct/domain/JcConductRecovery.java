package com.jiancha.biz.conduct.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 作风建设问题退缴明细 jc_conduct_recovery
 *
 * <p>违规收送礼品礼金 / 违规吃喝等问题的退缴登记：一案可有多笔退缴
 * （礼品、购物卡、现金分次退缴），主表 amount_recovered 由明细自动汇总。</p>
 *
 * @author 小标
 */
public class JcConductRecovery extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 退缴明细ID */
    private Long recoveryId;

    /** 问题ID（jc_conduct_issue.issue_id） */
    private Long issueId;

    /** 退缴物品名称 */
    @Excel(name = "退缴物品")
    private String itemName;

    /** 数量 */
    @Excel(name = "数量")
    private Integer itemQty;

    /** 单位 */
    private String unit;

    /** 金额（元，单笔） */
    @Excel(name = "金额(元)")
    private java.math.BigDecimal amount;

    /** 退缴日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date recoveryDate;

    /** 接收人（纪检收缴登记人） */
    private String receiver;

    /** 问题编号（展示字段） */
    private String issueNo;

    /** 问题人员姓名（展示字段） */
    private String personName;

    public Long getRecoveryId() {
        return recoveryId;
    }

    public void setRecoveryId(Long recoveryId) {
        this.recoveryId = recoveryId;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getItemQty() {
        return itemQty;
    }

    public void setItemQty(Integer itemQty) {
        this.itemQty = itemQty;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public java.math.BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(java.math.BigDecimal amount) {
        this.amount = amount;
    }

    public Date getRecoveryDate() {
        return recoveryDate;
    }

    public void setRecoveryDate(Date recoveryDate) {
        this.recoveryDate = recoveryDate;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getIssueNo() {
        return issueNo;
    }

    public void setIssueNo(String issueNo) {
        this.issueNo = issueNo;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }
}
