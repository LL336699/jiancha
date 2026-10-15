package com.jiancha.biz.dashboard.controller;

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
import com.jiancha.biz.dashboard.service.IJcDashboardService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 数据看板 Controller（M8 数据看板 + 段12 纪检综合看板）
 *
 * <p>接口前缀 {@code /jiancha/dashboard}，权限前缀 {@code jiancha:dashboard:*}。</p>
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/dashboard")
public class JcDashboardController extends BaseController {

    @Autowired
    private IJcDashboardService dashboardService;

    /** 看板总览数据（M8 基础版） */
    @PreAuthorize("@ss.hasPermi('jiancha:dashboard:view')")
    @GetMapping("/overview")
    public AjaxResult overview() {
        DashboardOverview data = dashboardService.overview();
        return success(data);
    }

    /** 段12 纪检综合看板：P8 八大模块聚合 + 红黄绿灯 + 最新快照 */
    @PreAuthorize("@ss.hasPermi('jiancha:dashboard:view')")
    @GetMapping("/comprehensive")
    public AjaxResult comprehensive() {
        DashboardOverview data = dashboardService.comprehensive();
        return success(data);
    }

    /** 段12 各模块运行统计 */
    @PreAuthorize("@ss.hasPermi('jiancha:dashboard:view')")
    @GetMapping("/modules")
    public AjaxResult modules() {
        List<ModuleStat> data = dashboardService.moduleStats();
        return success(data);
    }

    /** 段12 各模块红黄绿灯 */
    @PreAuthorize("@ss.hasPermi('jiancha:dashboard:view')")
    @GetMapping("/lights")
    public AjaxResult lights() {
        List<ModuleLight> data = dashboardService.lights();
        return success(data);
    }

    /** 段12 快照历史 */
    @PreAuthorize("@ss.hasPermi('jiancha:dashboard:view')")
    @GetMapping("/snapshots")
    public AjaxResult snapshots() {
        List<OverviewSnapshot> data = dashboardService.snapshots();
        return success(data);
    }

    /** 段12 立即生成当日快照（手动刷新，供定时任务外的按需触发） */
    @PreAuthorize("@ss.hasPermi('jiancha:dashboard:view')")
    @PostMapping("/snapshot/refresh")
    public AjaxResult refreshSnapshot() {
        int rows = dashboardService.refreshSnapshot();
        return success("已生成/更新快照，影响行数 " + rows);
    }

    /** 段12 指定模块月度趋势（module 取值 petition/rectify/reply/conduct/risk） */
    @PreAuthorize("@ss.hasPermi('jiancha:dashboard:view')")
    @GetMapping("/trend")
    public AjaxResult trend(@RequestParam(required = false) String module) {
        List<MonthCount> data = dashboardService.moduleTrend(module);
        return success(data);
    }

    /** 段12 指定模块状态分布（module 取值 asset/supplier/edu） */
    @PreAuthorize("@ss.hasPermi('jiancha:dashboard:view')")
    @GetMapping("/status")
    public AjaxResult status(@RequestParam(required = false) String module) {
        List<NameValue> data = dashboardService.moduleStatus(module);
        return success(data);
    }

    /** 段12 跨模块超期事项清单（module 为空/未知时返回全部） */
    @PreAuthorize("@ss.hasPermi('jiancha:dashboard:view')")
    @GetMapping("/overdue")
    public AjaxResult overdue(@RequestParam(required = false) String module) {
        List<OverdueItem> data = dashboardService.overdue(module);
        return success(data);
    }

    /** 段12 部门维度办理效能（module 为空/未知时返回全部） */
    @PreAuthorize("@ss.hasPermi('jiancha:dashboard:view')")
    @GetMapping("/dept")
    public AjaxResult dept(@RequestParam(required = false) String module) {
        List<DeptWorkload> data = dashboardService.deptWorkload(module);
        return success(data);
    }

    /** 段12 各模块健康评分（0-100） */
    @PreAuthorize("@ss.hasPermi('jiancha:dashboard:view')")
    @GetMapping("/health")
    public AjaxResult health() {
        List<ModuleHealth> data = dashboardService.healthScores();
        return success(data);
    }

    /** 段12 预警规则列表 */
    @PreAuthorize("@ss.hasPermi('jiancha:dashboard:view')")
    @GetMapping("/alerts")
    public AjaxResult alerts(DashboardAlertRule rule) {
        List<DashboardAlertRule> data = dashboardService.alertRules(rule);
        return success(data);
    }

    /** 段12 实时评估所有启用预警规则，返回命中结果 */
    @PreAuthorize("@ss.hasPermi('jiancha:dashboard:view')")
    @GetMapping("/alerts/evaluate")
    public AjaxResult evaluateAlerts() {
        List<DashboardAlertResult> data = dashboardService.evaluateAlerts();
        return success(data);
    }

    /** 段12 新增预警规则 */
    @PreAuthorize("@ss.hasPermi('jiancha:dashboard:view')")
    @PostMapping("/alerts")
    public AjaxResult addAlert(@RequestBody DashboardAlertRule rule) {
        return success("新增规则数 " + dashboardService.addAlertRule(rule));
    }

    /** 段12 更新预警规则 */
    @PreAuthorize("@ss.hasPermi('jiancha:dashboard:view')")
    @PutMapping("/alerts")
    public AjaxResult updateAlert(@RequestBody DashboardAlertRule rule) {
        return success("更新规则数 " + dashboardService.updateAlertRule(rule));
    }

    /** 段12 删除预警规则（按主键，逗号分隔） */
    @PreAuthorize("@ss.hasPermi('jiancha:dashboard:view')")
    @DeleteMapping("/alerts/{ruleIds}")
    public AjaxResult removeAlerts(@PathVariable Long[] ruleIds) {
        return success("删除规则数 " + dashboardService.removeAlertRules(ruleIds));
    }

    /** 段12 导出综合看板数据（XLSX 多 Sheet） */
    @PreAuthorize("@ss.hasPermi('jiancha:dashboard:view')")
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws Exception {
        byte[] bytes = dashboardService.exportComprehensive();
        response.reset();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        String fileName = "纪检综合看板_" + System.currentTimeMillis() + ".xlsx";
        response.setHeader("Content-Disposition",
                "attachment;filename=" + new String(fileName.getBytes("UTF-8"), "ISO-8859-1"));
        response.getOutputStream().write(bytes);
        response.getOutputStream().flush();
    }

    /** 段12 最新快照与约一周前快照的环比对比 */
    @PreAuthorize("@ss.hasPermi('jiancha:dashboard:view')")
    @GetMapping("/snapshot/compare")
    public AjaxResult snapshotCompare() {
        return success(dashboardService.snapshotCompare());
    }
}
