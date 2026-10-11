package com.jiancha.biz.dashboard.service;

import com.jiancha.biz.dashboard.domain.DashboardOverview;

/**
 * 数据看板 Service（M8 数据看板）
 *
 * @author 小标
 */
public interface IJcDashboardService {

    /**
     * 获取看板总览：核心指标卡 + 各维度分布与趋势。
     */
    DashboardOverview overview();
}
