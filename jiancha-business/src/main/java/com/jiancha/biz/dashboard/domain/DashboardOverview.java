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
}
