package com.jiancha.biz.supplier.domain;

import java.util.List;
import java.util.Map;

/**
 * 供应商廉洁管理统计 SupplierStat（口径统一后端 SQL 层聚合）
 *
 * @author 小标
 */
public class SupplierStat {

    /** 供应商总数 */
    private Long totalSuppliers;

    /** 正常合作数 */
    private Long normalCount;

    /** 观察期数 */
    private Long observeCount;

    /** 禁入/黑名单数 */
    private Long blackCount;

    /** 待审查数 */
    private Long pendingCount;

    /** 有效廉洁协议数（已签署+临期） */
    private Long activeAgreements;

    /** 协议临期数（预警天数内到期） */
    private Long expiringAgreements;

    /** 协议签署率（有有效协议的供应商 / 正常合作供应商，0-1） */
    private Double agreementRate;

    /** 关联排查命中总数 */
    private Long linkTotal;

    /** 其中系统比对命中数 */
    private Long autoLinkCount;

    /** 其中已转整改闭环数 */
    private Long linkToRectifyCount;

    /** 关联命中未处置数（handleResult=待处置） */
    private Long linkPendingCount;

    /** 按状态分布（字典值→数量） */
    private List<Map<String, Object>> byStatus;

    /** 按类别分布 */
    private List<Map<String, Object>> byCategory;

    /** 按关联类型分布（命中 TOP） */
    private List<Map<String, Object>> byLinkType;

    /** 近12个月黑名单新增趋势（月份→数量） */
    private List<Map<String, Object>> blackTrend;

    /** 关联命中供应商 TOP10（供应商名称→命中次数） */
    private List<Map<String, Object>> topHitSuppliers;

    public Long getTotalSuppliers() {
        return totalSuppliers;
    }

    public void setTotalSuppliers(Long totalSuppliers) {
        this.totalSuppliers = totalSuppliers;
    }

    public Long getNormalCount() {
        return normalCount;
    }

    public void setNormalCount(Long normalCount) {
        this.normalCount = normalCount;
    }

    public Long getObserveCount() {
        return observeCount;
    }

    public void setObserveCount(Long observeCount) {
        this.observeCount = observeCount;
    }

    public Long getBlackCount() {
        return blackCount;
    }

    public void setBlackCount(Long blackCount) {
        this.blackCount = blackCount;
    }

    public Long getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(Long pendingCount) {
        this.pendingCount = pendingCount;
    }

    public Long getActiveAgreements() {
        return activeAgreements;
    }

    public void setActiveAgreements(Long activeAgreements) {
        this.activeAgreements = activeAgreements;
    }

    public Long getExpiringAgreements() {
        return expiringAgreements;
    }

    public void setExpiringAgreements(Long expiringAgreements) {
        this.expiringAgreements = expiringAgreements;
    }

    public Double getAgreementRate() {
        return agreementRate;
    }

    public void setAgreementRate(Double agreementRate) {
        this.agreementRate = agreementRate;
    }

    public Long getLinkTotal() {
        return linkTotal;
    }

    public void setLinkTotal(Long linkTotal) {
        this.linkTotal = linkTotal;
    }

    public Long getAutoLinkCount() {
        return autoLinkCount;
    }

    public void setAutoLinkCount(Long autoLinkCount) {
        this.autoLinkCount = autoLinkCount;
    }

    public Long getLinkToRectifyCount() {
        return linkToRectifyCount;
    }

    public void setLinkToRectifyCount(Long linkToRectifyCount) {
        this.linkToRectifyCount = linkToRectifyCount;
    }

    public Long getLinkPendingCount() {
        return linkPendingCount;
    }

    public void setLinkPendingCount(Long linkPendingCount) {
        this.linkPendingCount = linkPendingCount;
    }

    public List<Map<String, Object>> getByStatus() {
        return byStatus;
    }

    public void setByStatus(List<Map<String, Object>> byStatus) {
        this.byStatus = byStatus;
    }

    public List<Map<String, Object>> getByCategory() {
        return byCategory;
    }

    public void setByCategory(List<Map<String, Object>> byCategory) {
        this.byCategory = byCategory;
    }

    public List<Map<String, Object>> getByLinkType() {
        return byLinkType;
    }

    public void setByLinkType(List<Map<String, Object>> byLinkType) {
        this.byLinkType = byLinkType;
    }

    public List<Map<String, Object>> getBlackTrend() {
        return blackTrend;
    }

    public void setBlackTrend(List<Map<String, Object>> blackTrend) {
        this.blackTrend = blackTrend;
    }

    public List<Map<String, Object>> getTopHitSuppliers() {
        return topHitSuppliers;
    }

    public void setTopHitSuppliers(List<Map<String, Object>> topHitSuppliers) {
        this.topHitSuppliers = topHitSuppliers;
    }
}
