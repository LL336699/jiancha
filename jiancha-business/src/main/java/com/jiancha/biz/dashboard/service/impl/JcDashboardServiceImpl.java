package com.jiancha.biz.dashboard.service.impl;

import com.jiancha.biz.dashboard.domain.DashboardOverview;
import com.jiancha.biz.dashboard.mapper.JcDashboardMapper;
import com.jiancha.biz.dashboard.service.IJcDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 数据看板 Service 实现（M8 数据看板）
 *
 * @author 小标
 */
@Service
public class JcDashboardServiceImpl implements IJcDashboardService {

    @Autowired
    private JcDashboardMapper dashboardMapper;

    @Override
    public DashboardOverview overview() {
        DashboardOverview o = new DashboardOverview();
        o.setCards(dashboardMapper.summaryCards());
        o.setClueByStatus(dashboardMapper.clueByStatus());
        o.setClueBySource(dashboardMapper.clueBySource());
        o.setClueByViolation(dashboardMapper.clueByViolation());
        o.setClueMonthly(dashboardMapper.clueMonthly());
        o.setKbByCategory(dashboardMapper.kbByCategory());
        o.setReportMonthly(dashboardMapper.reportMonthly());
        return o;
    }
}
