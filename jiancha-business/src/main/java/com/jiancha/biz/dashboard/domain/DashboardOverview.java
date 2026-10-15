package com.jiancha.biz.dashboard.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 数据看板总览聚合对象（M8 数据看板）
 *
 * <p>一次返回前端所需的全部卡片与图表数据，避免多次请求。</p>
 *
 * @author 小标
 */
@Data
public class DashboardOverview implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 核心指标卡 */
    private DashboardCards cards;

    /** 线索状态分布 */
    private List<NameValue> clueByStatus;

    /** 线索来源分布 */
    private List<NameValue> clueBySource;

    /** 问题类型分布 */
    private List<NameValue> clueByViolation;

    /** 线索月度趋势 */
    private List<MonthCount> clueMonthly;

    /** 知识库分类分布（仅已入库文档） */
    private List<NameValue> kbByCategory;

    /** 文书生成月度趋势 */
    private List<MonthCount> reportMonthly;

    /* ===================== 段12 纪检综合看板：P8 模块聚合 ===================== */

    /** P8 模块指标卡（含跨模块汇总与整体灯色） */
    private DashboardCards moduleCards;

    /** 各模块运行统计（总量 / 在办 / 已办结 / 超期） */
    private List<ModuleStat> moduleStats;

    /** 各模块红黄绿灯 */
    private List<ModuleLight> lights;

    /** 最新一次每日快照（无则为 null） */
    private OverviewSnapshot snapshot;
}
