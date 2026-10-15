package com.jiancha.biz.dashboard.service.impl;

import com.jiancha.biz.dashboard.domain.DashboardAlertResult;
import com.jiancha.biz.dashboard.domain.DashboardAlertRule;
import com.jiancha.biz.dashboard.domain.DashboardCards;
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
import com.jiancha.biz.dashboard.mapper.JcDashboardAlertRuleMapper;
import com.jiancha.biz.dashboard.mapper.JcDashboardMapper;
import com.jiancha.biz.dashboard.mapper.JcOverviewSnapshotMapper;
import com.jiancha.biz.dashboard.service.IJcDashboardService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据看板 Service 实现（M8 数据看板 + 段12 纪检综合看板）
 *
 * @author 小标
 */
@Service
public class JcDashboardServiceImpl implements IJcDashboardService {

    @Autowired
    private JcDashboardMapper dashboardMapper;

    @Autowired
    private JcOverviewSnapshotMapper snapshotMapper;

    @Autowired
    private JcDashboardAlertRuleMapper alertRuleMapper;

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

    @Override
    public DashboardOverview comprehensive() {
        // 复用 M8 基础版分布数据
        DashboardOverview o = overview();

        List<ModuleStat> stats = moduleStats();
        Long supplierBlack = dashboardMapper.supplierBlack();
        DashboardCards moduleCards = toModuleCards(stats, supplierBlack);
        List<ModuleLight> lights = toLights(stats, supplierBlack);
        OverviewSnapshot snapshot = snapshotMapper.selectLatest();

        o.setModuleCards(moduleCards);
        o.setModuleStats(stats);
        o.setLights(lights);
        o.setSnapshot(snapshot);
        return o;
    }

    @Override
    public List<ModuleStat> moduleStats() {
        List<ModuleStat> stats = dashboardMapper.moduleStats();
        for (ModuleStat s : stats) {
            s.setLight(deriveLight(s.getOverdue(), s.getProcessing(), false));
        }
        return stats;
    }

    @Override
    public List<ModuleLight> lights() {
        return toLights(moduleStats(), dashboardMapper.supplierBlack());
    }

    @Override
    public List<OverviewSnapshot> snapshots() {
        return snapshotMapper.selectHistory();
    }

    @Override
    public int refreshSnapshot() {
        OverviewSnapshot snapshot = buildSnapshot();
        return snapshotMapper.insertOrUpdate(snapshot);
    }

    @Override
    public List<MonthCount> moduleTrend(String module) {
        return dashboardMapper.moduleTrend(module);
    }

    @Override
    public List<NameValue> moduleStatus(String module) {
        return dashboardMapper.moduleStatus(module);
    }

    @Override
    public List<OverdueItem> overdue(String module) {
        return dashboardMapper.overdueItems(module);
    }

    @Override
    public List<DeptWorkload> deptWorkload(String module) {
        return dashboardMapper.deptWorkload(module);
    }

    @Override
    public List<DashboardAlertRule> alertRules(DashboardAlertRule rule) {
        return alertRuleMapper.selectList(rule);
    }

    @Override
    public DashboardAlertRule alertRule(Long ruleId) {
        return alertRuleMapper.selectById(ruleId);
    }

    @Override
    public int addAlertRule(DashboardAlertRule rule) {
        if (rule.getEnabled() == null || rule.getEnabled().isEmpty()) {
            rule.setEnabled("1");
        }
        return alertRuleMapper.insert(rule);
    }

    @Override
    public int updateAlertRule(DashboardAlertRule rule) {
        return alertRuleMapper.update(rule);
    }

    @Override
    public int removeAlertRules(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return 0;
        }
        return alertRuleMapper.deleteByIds(ids);
    }

    @Override
    public List<DashboardAlertResult> evaluateAlerts() {
        List<DashboardAlertRule> rules = alertRuleMapper.selectEnabled();
        if (rules == null || rules.isEmpty()) {
            return new ArrayList<>();
        }
        List<ModuleStat> stats = moduleStats();
        Long supplierBlack = dashboardMapper.supplierBlack();
        Map<String, ModuleStat> statMap = new LinkedHashMap<>();
        for (ModuleStat s : stats) {
            statMap.put(s.getModuleKey(), s);
        }
        List<DashboardAlertResult> hits = new ArrayList<>();
        for (DashboardAlertRule r : rules) {
            Long actual = metricValue(r.getModuleKey(), r.getMetricType(), statMap, supplierBlack);
            if (actual == null) {
                continue;
            }
            long threshold = r.getThreshold() == null ? 0L : r.getThreshold().longValue();
            if (actual >= threshold) {
                DashboardAlertResult res = new DashboardAlertResult();
                res.setRuleName(r.getRuleName());
                res.setModuleKey(r.getModuleKey());
                res.setModuleName(moduleNameOf(r.getModuleKey()));
                res.setMetricType(r.getMetricType());
                res.setThreshold(r.getThreshold());
                res.setActual(actual);
                res.setAlertLevel(r.getAlertLevel());
                res.setMessage(moduleNameOf(r.getModuleKey()) + " 的「" + metricLabel(r.getMetricType())
                        + "」当前为 " + actual + "，已达阈值 " + r.getThreshold());
                hits.add(res);
            }
        }
        return hits;
    }

    @Override
    public List<ModuleHealth> healthScores() {
        List<ModuleStat> stats = moduleStats();
        Long supplierBlack = dashboardMapper.supplierBlack();
        List<ModuleHealth> list = new ArrayList<>();
        for (ModuleStat s : stats) {
            ModuleHealth h = new ModuleHealth();
            h.setModuleKey(s.getModuleKey());
            h.setModuleName(s.getModuleName());
            long total = nz(s.getTotal());
            long over = nz(s.getOverdue());
            long proc = nz(s.getProcessing());
            double overRatio = total > 0 ? (double) over / total : 0.0;
            double procRatio = total > 0 ? (double) proc / total : 0.0;
            int deduct = 0;
            StringBuilder reason = new StringBuilder();
            if ("supplier".equals(s.getModuleKey())) {
                long black = nz(supplierBlack);
                if (black > 0) {
                    deduct += (int) Math.min(60, black * 30);
                    reason.append("存在黑名单/禁入供应商 ").append(black).append(" 家；");
                }
            } else {
                if (over > 0) {
                    deduct += (int) Math.min(60, overRatio * 60);
                    reason.append("超期 ").append(over).append(" 件(占比").append(pct(overRatio)).append(")；");
                }
                if (proc > 0) {
                    deduct += (int) Math.min(25, procRatio * 25);
                    reason.append("在办 ").append(proc).append(" 件(占比").append(pct(procRatio)).append(")；");
                }
            }
            int score = Math.max(0, 100 - deduct);
            h.setScore(score);
            h.setLevel(score >= 90 ? "优" : score >= 75 ? "良" : score >= 60 ? "中" : "差");
            h.setReason(reason.length() > 0 ? reason.toString() : "运行平稳，无超期/积压");
            list.add(h);
        }
        return list;
    }

    @Override
    public byte[] exportComprehensive() throws Exception {
        List<ModuleStat> stats = moduleStats();
        List<ModuleLight> lights = lights();
        List<OverdueItem> overdue = dashboardMapper.overdueItems(null);
        List<DeptWorkload> depts = dashboardMapper.deptWorkload(null);

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet s1 = wb.createSheet("模块运行统计");
            String[] headers1 = {"模块", "总量", "在办", "已办结", "超期"};
            writeHeader(wb, s1, headers1);
            int r = 1;
            for (ModuleStat m : stats) {
                Row row = s1.createRow(r++);
                setCell(row, 0, m.getModuleName());
                setCell(row, 1, nz(m.getTotal()));
                setCell(row, 2, nz(m.getProcessing()));
                setCell(row, 3, nz(m.getDone()));
                setCell(row, 4, nz(m.getOverdue()));
            }
            autoSize(s1, headers1.length);

            Sheet s2 = wb.createSheet("红黄绿灯");
            String[] headers2 = {"模块", "灯色", "说明"};
            writeHeader(wb, s2, headers2);
            r = 1;
            for (ModuleLight l : lights) {
                Row row = s2.createRow(r++);
                setCell(row, 0, l.getModuleName());
                setCell(row, 1, l.getLight());
                setCell(row, 2, l.getNote());
            }
            autoSize(s2, headers2.length);

            Sheet s3 = wb.createSheet("超期事项清单");
            String[] headers3 = {"模块", "单号", "标题", "责任部门", "责任人", "期限", "超期天数"};
            writeHeader(wb, s3, headers3);
            r = 1;
            for (OverdueItem o : overdue) {
                Row row = s3.createRow(r++);
                setCell(row, 0, o.getModuleName());
                setCell(row, 1, o.getBizId() == null ? "" : o.getBizId().toString());
                setCell(row, 2, o.getTitle());
                setCell(row, 3, o.getOwnerDept());
                setCell(row, 4, o.getOwnerName());
                setCell(row, 5, o.getDeadline() == null ? "" : o.getDeadline().toString());
                setCell(row, 6, o.getOverdueDays() == null ? 0L : o.getOverdueDays());
            }
            autoSize(s3, headers3.length);

            Sheet s4 = wb.createSheet("部门办理效能");
            String[] headers4 = {"模块", "责任部门", "总量", "在办", "超期"};
            writeHeader(wb, s4, headers4);
            r = 1;
            for (DeptWorkload d : depts) {
                Row row = s4.createRow(r++);
                setCell(row, 0, d.getModuleName());
                setCell(row, 1, d.getDeptName());
                setCell(row, 2, nz(d.getTotal()));
                setCell(row, 3, nz(d.getProcessing()));
                setCell(row, 4, nz(d.getOverdue()));
            }
            autoSize(s4, headers4.length);

            wb.write(out);
            return out.toByteArray();
        }
    }

    /* ===================== 段12 预警/健康/导出辅助 ===================== */

    private Long metricValue(String moduleKey, String metricType, Map<String, ModuleStat> statMap, Long supplierBlack) {
        if ("all".equals(moduleKey)) {
            long sum = 0L;
            for (ModuleStat s : statMap.values()) {
                sum += metricOf(s, metricType, supplierBlack);
            }
            return sum;
        }
        ModuleStat s = statMap.get(moduleKey);
        if (s == null) {
            return null;
        }
        return metricOf(s, metricType, supplierBlack);
    }

    private Long metricOf(ModuleStat s, String metricType, Long supplierBlack) {
        if ("overdue".equals(metricType)) {
            if ("supplier".equals(s.getModuleKey())) {
                return nz(supplierBlack);
            }
            return nz(s.getOverdue());
        } else if ("processing".equals(metricType)) {
            return nz(s.getProcessing());
        } else if ("total".equals(metricType)) {
            return nz(s.getTotal());
        }
        return 0L;
    }

    private String metricLabel(String metricType) {
        if ("overdue".equals(metricType)) {
            return "超期数";
        } else if ("processing".equals(metricType)) {
            return "在办数";
        } else if ("total".equals(metricType)) {
            return "总量";
        }
        return metricType;
    }

    private String moduleNameOf(String moduleKey) {
        switch (moduleKey != null ? moduleKey : "") {
            case "petition":
                return "信访举报";
            case "rectify":
                return "整改闭环";
            case "risk":
                return "廉政风控";
            case "supplier":
                return "供应商廉洁";
            case "edu":
                return "廉政教育";
            case "reply":
                return "廉政意见回复";
            case "conduct":
                return "作风建设";
            case "asset":
                return "涉案财物";
            case "all":
                return "全局";
            default:
                return moduleKey;
        }
    }

    private String pct(double ratio) {
        return String.format("%.0f%%", ratio * 100);
    }

    private void writeHeader(Workbook wb, Sheet sheet, String[] headers) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell c = row.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(style);
        }
    }

    private void setCell(Row row, int idx, Object value) {
        Cell c = row.createCell(idx);
        if (value == null) {
            c.setCellValue("");
        } else if (value instanceof Number) {
            c.setCellValue(((Number) value).doubleValue());
        } else {
            c.setCellValue(value.toString());
        }
    }

    private void autoSize(Sheet sheet, int cols) {
        for (int i = 0; i < cols; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    @Override
    public SnapshotCompare snapshotCompare() {
        List<OverviewSnapshot> hist = snapshotMapper.selectHistory();
        if (hist == null || hist.size() < 2) {
            return null;
        }
        OverviewSnapshot newest = hist.get(0);
        OverviewSnapshot older = null;
        for (int i = 1; i < hist.size(); i++) {
            long diffMs = newest.getSnapshotDate().getTime() - hist.get(i).getSnapshotDate().getTime();
            long days = diffMs / 86400000L;
            if (days >= 1) {
                older = hist.get(i);
                break;
            }
        }
        if (older == null) {
            older = hist.get(hist.size() - 1);
        }
        long np = nz(newest.getTotalProcessing());
        long op = nz(older.getTotalProcessing());
        long no = nz(newest.getTotalOverdue());
        long oo = nz(older.getTotalOverdue());
        SnapshotCompare c = new SnapshotCompare();
        c.setNewDate(newest.getSnapshotDate());
        c.setOldDate(older.getSnapshotDate());
        c.setNewProcessing(np);
        c.setOldProcessing(op);
        c.setDeltaProcessing(np - op);
        c.setNewOverdue(no);
        c.setOldOverdue(oo);
        c.setDeltaOverdue(no - oo);
        c.setNewLight(newest.getLightLevel());
        c.setOldLight(older.getLightLevel());
        long dOver = no - oo;
        c.setTrend(dOver < 0 ? "improved" : dOver > 0 ? "worse" : "flat");
        return c;
    }

    /* ===================== 段12 内部聚合辅助 ===================== */

    /** 由模块统计列表推导 P8 模块指标卡（含跨模块汇总与整体灯色） */
    private DashboardCards toModuleCards(List<ModuleStat> stats, Long supplierBlack) {
        DashboardCards c = new DashboardCards();
        long totalProc = 0L;
        long totalOver = 0L;
        for (ModuleStat s : stats) {
            Long total = nz(s.getTotal());
            Long proc = nz(s.getProcessing());
            Long done = nz(s.getDone());
            Long over = nz(s.getOverdue());
            totalProc += proc;
            totalOver += over;
            String key = s.getModuleKey();
            switch (key != null ? key : "") {
                case "petition":
                    c.setPetitionTotal(total); c.setPetitionProcessing(proc); c.setPetitionDone(done); c.setPetitionOverdue(over); break;
                case "rectify":
                    c.setRectifyTotal(total); c.setRectifyProcessing(proc); c.setRectifyDone(done); c.setRectifyOverdue(over); break;
                case "risk":
                    c.setRiskTotal(total); c.setRiskProcessing(proc); c.setRiskDone(done); break;
                case "supplier":
                    c.setSupplierTotal(total); c.setSupplierBlack(supplierBlack); break;
                case "edu":
                    c.setEduTotal(total); c.setEduDone(done); break;
                case "reply":
                    c.setReplyTotal(total); c.setReplyProcessing(proc); c.setReplyDone(done); c.setReplyOverdue(over); break;
                case "conduct":
                    c.setConductTotal(total); c.setConductProcessing(proc); c.setConductDone(done); c.setConductOverdue(over); break;
                case "asset":
                    c.setAssetTotal(total); c.setAssetProcessing(proc); c.setAssetDone(done); c.setAssetOverdue(over); break;
                default:
                    break;
            }
        }
        c.setTotalProcessing(totalProc);
        c.setTotalOverdue(totalOver);
        c.setLightLevel(deriveOverallLight(stats, supplierBlack, totalProc, totalOver));
        return c;
    }

    /** 由模块统计列表推导各模块红黄绿灯 */
    private List<ModuleLight> toLights(List<ModuleStat> stats, Long supplierBlack) {
        List<ModuleLight> lights = new ArrayList<>();
        for (ModuleStat s : stats) {
            ModuleLight l = new ModuleLight();
            l.setModuleKey(s.getModuleKey());
            l.setModuleName(s.getModuleName());
            l.setOverdue(s.getOverdue());
            l.setProcessing(s.getProcessing());
            Long over = nz(s.getOverdue());
            Long proc = nz(s.getProcessing());
            if ("supplier".equals(s.getModuleKey())) {
                if (nz(supplierBlack) > 0) {
                    l.setLight("red");
                    l.setNote("存在黑名单/禁入供应商 " + supplierBlack + " 家");
                } else if (proc > 0) {
                    l.setLight("yellow");
                    l.setNote("有待审查供应商 " + proc + " 家");
                } else {
                    l.setLight("green");
                    l.setNote("供应商均正常");
                }
            } else {
                if (over > 0) {
                    l.setLight("red");
                    l.setNote("超期 " + over + " 件");
                } else if (proc > 0) {
                    l.setLight("yellow");
                    l.setNote("在办 " + proc + " 件");
                } else {
                    l.setLight("green");
                    l.setNote("无在办/超期");
                }
            }
            lights.add(l);
        }
        return lights;
    }

    /** 构建当日快照对象（不落库） */
    private OverviewSnapshot buildSnapshot() {
        OverviewSnapshot s = new OverviewSnapshot();
        s.setSnapshotDate(new Date());
        List<ModuleStat> stats = moduleStats();
        Long supplierBlack = dashboardMapper.supplierBlack();
        long totalProc = 0L;
        long totalOver = 0L;
        for (ModuleStat m : stats) {
            Long total = nz(m.getTotal());
            Long proc = nz(m.getProcessing());
            Long done = nz(m.getDone());
            Long over = nz(m.getOverdue());
            totalProc += proc;
            totalOver += over;
            String key = m.getModuleKey();
            switch (key != null ? key : "") {
                case "petition":
                    s.setPetitionTotal(total); s.setPetitionProcessing(proc); s.setPetitionOverdue(over); break;
                case "rectify":
                    s.setRectifyTotal(total); s.setRectifyProcessing(proc); s.setRectifyOverdue(over); break;
                case "risk":
                    s.setRiskTotal(total); s.setRiskProcessing(proc); break;
                case "supplier":
                    s.setSupplierTotal(total); s.setSupplierBlack(supplierBlack); break;
                case "edu":
                    s.setEduTotal(total); s.setEduDone(done); break;
                case "reply":
                    s.setReplyTotal(total); s.setReplyProcessing(proc); s.setReplyOverdue(over); break;
                case "conduct":
                    s.setConductTotal(total); s.setConductProcessing(proc); s.setConductOverdue(over); break;
                case "asset":
                    s.setAssetTotal(total); s.setAssetProcessing(proc); s.setAssetOverdue(over); break;
                default:
                    break;
            }
        }
        s.setTotalProcessing(totalProc);
        s.setTotalOverdue(totalOver);
        s.setLightLevel(deriveOverallLight(stats, supplierBlack, totalProc, totalOver));
        s.setCreateBy("system");
        s.setUpdateBy("system");
        s.setRemark("每日综合看板快照（自动/手动生成）");
        return s;
    }

    /** 单模块灯色：red=超期(或供应商黑名单)，yellow=在办，green=其它 */
    private String deriveLight(Long overdue, Long processing, boolean supplierBlack) {
        if (supplierBlack || nz(overdue) > 0) {
            return "red";
        }
        if (nz(processing) > 0) {
            return "yellow";
        }
        return "green";
    }

    /** 整体灯色：任一模块红灯或存在超期/黑名单→红；有在办→黄；否则绿 */
    private String deriveOverallLight(List<ModuleStat> stats, Long supplierBlack, long totalProc, long totalOver) {
        if (nz(supplierBlack) > 0 || totalOver > 0) {
            return "red";
        }
        boolean anyRed = stats.stream().anyMatch(s -> "red".equals(s.getLight()));
        if (anyRed) {
            return "red";
        }
        if (totalProc > 0) {
            return "yellow";
        }
        return "green";
    }

    private long nz(Long v) {
        return v == null ? 0L : v.longValue();
    }
}
