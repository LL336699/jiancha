package com.jiancha.biz.dashboard.mapper;

import com.jiancha.biz.dashboard.domain.DashboardCards;
import com.jiancha.biz.dashboard.domain.MonthCount;
import com.jiancha.biz.dashboard.domain.NameValue;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 数据看板 Mapper（M8 数据看板）
 *
 * <p>纯聚合只读查询，跨线索 / 廉政档案 / 知识库 / 文书 / 利益冲突多域。</p>
 *
 * @author 小标
 */
@Mapper
public interface JcDashboardMapper {

    /** 核心指标卡（单行长列表） */
    DashboardCards summaryCards();

    /** 线索状态分布 */
    List<NameValue> clueByStatus();

    /** 线索来源分布 */
    List<NameValue> clueBySource();

    /** 问题类型分布 */
    List<NameValue> clueByViolation();

    /** 线索月度趋势 */
    List<MonthCount> clueMonthly();

    /** 知识库分类分布（仅已入库） */
    List<NameValue> kbByCategory();

    /** 文书生成月度趋势 */
    List<MonthCount> reportMonthly();
}
