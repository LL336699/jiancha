package com.jiancha.biz.dashboard.controller;

import com.jiancha.biz.dashboard.domain.DashboardOverview;
import com.jiancha.biz.dashboard.service.IJcDashboardService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据看板 Controller（M8 数据看板）
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

    /** 看板总览数据 */
    @PreAuthorize("@ss.hasPermi('jiancha:dashboard:view')")
    @GetMapping("/overview")
    public AjaxResult overview() {
        DashboardOverview data = dashboardService.overview();
        return success(data);
    }
}
