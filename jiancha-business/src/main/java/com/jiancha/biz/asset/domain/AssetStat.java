package com.jiancha.biz.asset.domain;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 涉案财物统计（段11 E6）
 *
 * 卡片：财物总量 / 暂存中 / 处置中 / 已处置 / 已退还 / 超期未处置 / 估值总额
 * 分布：按类别 / 按状态 / 按来源 / 按保管人 / 按月扣押趋势
 */
public class AssetStat implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 卡片指标 */
    private List<Map<String, Object>> cards;

    /** 按物品类别分布 */
    private List<Map<String, Object>> byCategory;

    /** 按保管状态分布 */
    private List<Map<String, Object>> byStatus;

    /** 按来源类型分布 */
    private List<Map<String, Object>> bySource;

    /** 按保管人分布（暂存中的财物在谁手上） */
    private List<Map<String, Object>> byKeeper;

    /** 近12个月扣押量趋势 */
    private List<Map<String, Object>> monthlySeize;

    public List<Map<String, Object>> getCards()
    {
        return cards;
    }

    public void setCards(List<Map<String, Object>> cards)
    {
        this.cards = cards;
    }

    public List<Map<String, Object>> getByCategory()
    {
        return byCategory;
    }

    public void setByCategory(List<Map<String, Object>> byCategory)
    {
        this.byCategory = byCategory;
    }

    public List<Map<String, Object>> getByStatus()
    {
        return byStatus;
    }

    public void setByStatus(List<Map<String, Object>> byStatus)
    {
        this.byStatus = byStatus;
    }

    public List<Map<String, Object>> getBySource()
    {
        return bySource;
    }

    public void setBySource(List<Map<String, Object>> bySource)
    {
        this.bySource = bySource;
    }

    public List<Map<String, Object>> getByKeeper()
    {
        return byKeeper;
    }

    public void setByKeeper(List<Map<String, Object>> byKeeper)
    {
        this.byKeeper = byKeeper;
    }

    public List<Map<String, Object>> getMonthlySeize()
    {
        return monthlySeize;
    }

    public void setMonthlySeize(List<Map<String, Object>> monthlySeize)
    {
        this.monthlySeize = monthlySeize;
    }
}
