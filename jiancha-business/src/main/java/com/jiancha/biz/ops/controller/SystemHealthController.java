package com.jiancha.biz.ops.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jiancha.biz.ops.service.SystemHealthService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * 系统健康巡检 接口层（段17 全局打磨）
 *
 * <p>只读探测，供运维页一键自检：数据层 / 缓存层 / AI 能力 / 定时任务 / 业务待办。
 * 巡检权限独立于业务权限，便于授予运维岗而不开放业务数据。</p>
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/ops")
public class SystemHealthController extends BaseController {

    @Autowired
    private SystemHealthService healthService;

    /**
     * 一键巡检（全量检查项 + 待办摘要）
     */
    @PreAuthorize("@ss.hasPermi('jiancha:ops:health')")
    @GetMapping("/health")
    public AjaxResult health() {
        return AjaxResult.success(healthService.checkAll());
    }
}
