package com.jiancha.biz.dashboard.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jiancha.biz.dashboard.service.IJcDashboardService;

/**
 * 综合看板每日快照任务（段12 纪检综合看板）
 *
 * <p>每日聚合全模块指标生成快照落库，供趋势与跨日环比使用。
 * 通过「系统监控 → 定时任务」中的 {@code jcDashboardSnapshotTask.run()} 调度（默认启用）。</p>
 */
@Component("jcDashboardSnapshotTask")
public class JcDashboardSnapshotTask {

    @Autowired
    private IJcDashboardService dashboardService;

    public void run() {
        int rows = dashboardService.refreshSnapshot();
        if (rows > 0) {
            System.out.println("[jcDashboardSnapshotTask] 综合看板每日快照已生成/更新，影响行数 " + rows);
        }
    }
}
