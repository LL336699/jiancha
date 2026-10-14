package com.jiancha.biz.risk.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysConfigService;

import com.jiancha.biz.risk.domain.JcRiskRule;
import com.jiancha.biz.risk.mapper.JcRiskScanMapper;

/**
 * 风险规则引擎
 *
 * <p>设计文档 §M10（企业版口径）：规则可配置、阈值可配置、算法不写死。
 * 引擎按启用规则逐条评估，评估逻辑与规则编码（R01-R06）一一对应；
 * 新增规则 = 加一条 jc_risk_rule 行 + 本类加一个 evaluate 分支，不改调用方。</p>
 *
 * <p>评分口径：基础分 40，每超出阈值 1 个单位 +15，封顶 100；
 * 等级由规则配置决定（alert_level），分数仅用于同等级内排序。</p>
 *
 * @author 小标
 */
@Component
public class RiskRuleEngine {

    /** 规则编码：举报集中反映 */
    public static final String RULE_R01 = "R01";
    /** 规则编码：整改超期 */
    public static final String RULE_R02 = "R02";
    /** 规则编码：整改回头看不通过 */
    public static final String RULE_R03 = "R03";
    /** 规则编码：线索超期未办结 */
    public static final String RULE_R04 = "R04";
    /** 规则编码：利益冲突申报存疑 */
    public static final String RULE_R05 = "R05";
    /** 规则编码：岗位高风险 */
    public static final String RULE_R06 = "R06";

    /** sys_config 键：举报集中反映统计窗口（天） */
    private static final String CFG_WINDOW_DAYS = "jiancha.risk.window.days";

    /** 默认统计窗口（天） */
    private static final int DEFAULT_WINDOW_DAYS = 90;

    @Autowired
    private JcRiskScanMapper scanMapper;

    @Autowired
    private ISysConfigService configService;

    /**
     * 评估单条规则，返回命中列表。
     *
     * @param rule 启用的规则（含阈值配置）
     * @return 命中列表；无命中返回空列表
     */
    public List<RiskHit> evaluate(JcRiskRule rule) {
        if (rule == null || !"0".equals(rule.getStatus())) {
            return new ArrayList<>();
        }
        double threshold = rule.getThreshold() == null ? 1d : rule.getThreshold();
        switch (StringUtils.nvl(rule.getRuleCode(), "")) {
            case RULE_R01:
                return evaluatePetitionConcentration(threshold);
            case RULE_R02:
                return evaluateOverdueIssue(threshold);
            case RULE_R03:
                return evaluateRevisitFail(threshold);
            case RULE_R04:
                return evaluateOverdueClue(threshold);
            case RULE_R05:
                return evaluateConflictSuspect(threshold);
            case RULE_R06:
                return evaluatePositionRisk(threshold);
            default:
                return new ArrayList<>();
        }
    }

    /** R01 举报集中反映：统计窗口内同一被举报对象举报数 ≥ 阈值 */
    private List<RiskHit> evaluatePetitionConcentration(double threshold) {
        int days = readWindowDays();
        List<RiskHit> hits = new ArrayList<>();
        for (Map<String, Object> row : scanMapper.countPetitionByTarget(days)) {
            long value = toLong(row.get("value"));
            if (value < threshold) {
                continue;
            }
            hits.add(new RiskHit(RULE_R01, "0", null, str(row.get("name")),
                    score(value, threshold),
                    String.format("近%d天被举报%d次（举报单：%s）", days, value, str(row.get("refs")))));
        }
        return hits;
    }

    /** R02 整改超期：部门超期未销号问题数 ≥ 阈值 */
    private List<RiskHit> evaluateOverdueIssue(double threshold) {
        List<RiskHit> hits = new ArrayList<>();
        for (Map<String, Object> row : scanMapper.countOverdueIssueByDept()) {
            long value = toLong(row.get("value"));
            if (value < threshold) {
                continue;
            }
            hits.add(new RiskHit(RULE_R02, "1", toLongObj(row.get("id")), str(row.get("name")),
                    score(value, threshold),
                    String.format("超期未销号整改问题%d项（单号：%s）", value, str(row.get("refs")))));
        }
        return hits;
    }

    /** R03 整改回头看不通过：部门回头看不通过次数 ≥ 阈值 */
    private List<RiskHit> evaluateRevisitFail(double threshold) {
        List<RiskHit> hits = new ArrayList<>();
        for (Map<String, Object> row : scanMapper.countRevisitFailByDept()) {
            long value = toLong(row.get("value"));
            if (value < threshold) {
                continue;
            }
            hits.add(new RiskHit(RULE_R03, "1", toLongObj(row.get("id")), str(row.get("name")),
                    score(value, threshold),
                    String.format("整改回头看不通过%d次（单号：%s）", value, str(row.get("refs")))));
        }
        return hits;
    }

    /** R04 线索超期未办结：部门超期线索数 ≥ 阈值 */
    private List<RiskHit> evaluateOverdueClue(double threshold) {
        List<RiskHit> hits = new ArrayList<>();
        for (Map<String, Object> row : scanMapper.countOverdueClueByDept()) {
            long value = toLong(row.get("value"));
            if (value < threshold) {
                continue;
            }
            hits.add(new RiskHit(RULE_R04, "1", toLongObj(row.get("id")), str(row.get("name")),
                    score(value, threshold),
                    String.format("超期未办结线索%d条（单号：%s）", value, str(row.get("refs")))));
        }
        return hits;
    }

    /** R05 利益冲突申报存疑：人员存疑申报数 ≥ 阈值 */
    private List<RiskHit> evaluateConflictSuspect(double threshold) {
        List<RiskHit> hits = new ArrayList<>();
        for (Map<String, Object> row : scanMapper.countConflictSuspectByPerson()) {
            long value = toLong(row.get("value"));
            if (value < threshold) {
                continue;
            }
            hits.add(new RiskHit(RULE_R05, "0", toLongObj(row.get("id")), str(row.get("name")),
                    score(value, threshold),
                    String.format("利益冲突申报存疑%d笔（申报单：%s）", value, str(row.get("refs")))));
        }
        return hits;
    }

    /** R06 岗位高风险：岗位模型权重合计 ≥ 阈值 */
    private List<RiskHit> evaluatePositionRisk(double threshold) {
        List<RiskHit> hits = new ArrayList<>();
        for (Map<String, Object> row : scanMapper.sumPositionWeight()) {
            long value = toLong(row.get("value"));
            if (value < threshold) {
                continue;
            }
            hits.add(new RiskHit(RULE_R06, "2", null, str(row.get("name")),
                    score(value, threshold),
                    String.format("岗位风险模型权重合计%d分（因子：%s）", value, str(row.get("refs")))));
        }
        return hits;
    }

    /** 评分：基础 40 分，每超阈值 1 单位 +15，封顶 100 */
    private double score(double value, double threshold) {
        double s = 40d + (value - threshold) * 15d;
        return Math.min(100d, Math.max(40d, s));
    }

    /** 读取统计窗口（天），配置缺失走兜底 */
    private int readWindowDays() {
        try {
            String v = configService.selectConfigByKey(CFG_WINDOW_DAYS);
            if (StringUtils.isNotEmpty(v)) {
                int d = (int) Double.parseDouble(v.trim());
                if (d > 0) {
                    return d;
                }
            }
        } catch (Exception ignored) {
            // 配置缺失或缓存不可用走兜底
        }
        return DEFAULT_WINDOW_DAYS;
    }

    private static long toLong(Object value) {
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }

    private static Long toLongObj(Object value) {
        return value instanceof Number ? ((Number) value).longValue() : null;
    }

    private static String str(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
