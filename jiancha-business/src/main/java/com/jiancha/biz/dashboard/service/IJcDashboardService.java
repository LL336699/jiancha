package com.jiancha.biz.dashboard.service;

import com.jiancha.biz.dashboard.domain.DashboardAlertResult;
import com.jiancha.biz.dashboard.domain.DashboardAlertRule;
import com.jiancha.biz.dashboard.domain.DashboardOverview;
import com.jiancha.biz.dashboard.domain.DeptWorkload;
import com.jiancha.biz.dashboard.domain.ModuleHealth;
import com.jiancha.biz.dashboard.domain.ModuleLight;
import com.jiancha.biz.dashboard.domain.ModuleStat;
import com.jiancha.biz.dashboard.domain.MonthCount;
import com.jiancha.biz.dashboard.domain.NameValue;
import com.jiancha.biz.dashboard.domain.OverviewSnapshot;
import com.jiancha.biz.dashboard.domain.OverdueItem;
import com.jiancha.biz.dashboard.domain.SnapshotCompare;

import java.util.List;

/**
 * 数据看板 Service（M8 数据看板 + 段12 纪检综合看板）
 *
 * @author 小标
 */
public interface IJcDashboardService {

    /**
     * 获取看板总览（M8 基础版）：核心指标卡 + 各维度分布与趋势。
     */
    DashboardOverview overview();

    /**
     * 段12 纪检综合看板：在 M8 基础上叠加 P8 八大模块聚合、红黄绿灯、最新快照。
     */
    DashboardOverview comprehensive();

    /**
     * 段12 P8 八大模块运行统计。
     */
    List<ModuleStat> moduleStats();

    /**
     * 段12 各模块红黄绿灯。
     */
    List<ModuleLight> lights();

    /**
     * 段12 最近快照历史（最多 30 条）。
     */
    List<OverviewSnapshot> snapshots();

    /**
     * 段12 立即构建并落库一条当日快照（供定时任务与手动刷新调用），返回受影响行数。
     */
    int refreshSnapshot();

    /**
     * 段12 指定模块月度趋势（module 取值 petition/rectify/reply/conduct/risk）。
     */
    List<MonthCount> moduleTrend(String module);

    /**
     * 段12 指定模块状态分布（module 取值 asset/supplier/edu）。
     */
    List<NameValue> moduleStatus(String module);

    /**
     * 段12 跨模块超期事项清单；module 为空/未知时返回全部（仅含带期限字段的 5 个模块）。
     */
    List<OverdueItem> overdue(String module);

    /**
     * 段12 部门维度办理效能；module 为空/未知时返回全部（仅含带责任部门字段的 5 个模块）。
     */
    List<DeptWorkload> deptWorkload(String module);

    /**
     * 段12 预警规则列表（按条件）。
     */
    List<DashboardAlertRule> alertRules(DashboardAlertRule rule);

    /**
     * 段12 预警规则明细。
     */
    DashboardAlertRule alertRule(Long ruleId);

    /**
     * 段12 新增预警规则。
     */
    int addAlertRule(DashboardAlertRule rule);

    /**
     * 段12 更新预警规则。
     */
    int updateAlertRule(DashboardAlertRule rule);

    /**
     * 段12 删除预警规则（按主键批量）。
     */
    int removeAlertRules(Long[] ids);

    /**
     * 段12 实时评估所有启用规则，返回命中结果。
     */
    List<DashboardAlertResult> evaluateAlerts();

    /**
     * 段12 各模块健康评分（0-100，含评级与扣分理由）。
     */
    List<ModuleHealth> healthScores();

    /**
     * 段12 导出综合看板数据为 XLSX 字节流（多 Sheet：模块统计/红黄绿灯/超期清单/部门效能）。
     */
    byte[] exportComprehensive() throws Exception;

    /**
     * 段12 最新快照与约一周前快照的环比对比（在办/超期增量与态势趋势）。
     */
    SnapshotCompare snapshotCompare();
}
