package com.jiancha.biz.dashboard.mapper;

import com.jiancha.biz.dashboard.domain.DashboardCards;
import com.jiancha.biz.dashboard.domain.DeptWorkload;
import com.jiancha.biz.dashboard.domain.ModuleStat;
import com.jiancha.biz.dashboard.domain.MonthCount;
import com.jiancha.biz.dashboard.domain.NameValue;
import com.jiancha.biz.dashboard.domain.OverdueItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 数据看板 Mapper（M8 数据看板 + 段12 纪检综合看板）
 *
 * <p>纯聚合只读查询，跨线索 / 廉政档案 / 知识库 / 文书 / 利益冲突 / P8 八大业务模块。</p>
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

    /** 段12：P8 八大模块运行统计（UNION ALL，每行一个模块） */
    List<ModuleStat> moduleStats();

    /** 段12：供应商黑名单(含禁入)数量，用于供应商模块红灯判定 */
    Long supplierBlack();

    /** 段12：指定模块月度趋势（module 取值 petition/rectify/reply/conduct/risk；其它返回空） */
    List<MonthCount> moduleTrend(String module);

    /** 段12：指定模块状态分布（module 取值 asset/supplier/edu；其它返回空） */
    List<NameValue> moduleStatus(String module);

    /** 段12：跨模块超期事项清单（module 为空/未知时返回全部；仅含带期限字段的 5 个模块） */
    List<OverdueItem> overdueItems(String module);

    /** 段12：部门维度办理效能（module 为空/未知时返回全部；仅含带责任部门字段的 5 个模块） */
    List<DeptWorkload> deptWorkload(String module);
}
