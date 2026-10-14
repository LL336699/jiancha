package com.jiancha.biz.risk.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

import com.jiancha.biz.rectify.domain.JcRectifyIssue;
import com.jiancha.biz.rectify.service.IJcRectifyService;
import com.jiancha.biz.risk.domain.JcRiskAlert;
import com.jiancha.biz.risk.domain.JcRiskModel;
import com.jiancha.biz.risk.domain.JcRiskRule;
import com.jiancha.biz.risk.domain.RiskStat;
import com.jiancha.biz.risk.domain.RiskStatItem;
import com.jiancha.biz.risk.domain.RiskVerifyRequest;
import com.jiancha.biz.risk.engine.RiskHit;
import com.jiancha.biz.risk.engine.RiskRuleEngine;
import com.jiancha.biz.risk.mapper.JcRiskAlertMapper;
import com.jiancha.biz.risk.mapper.JcRiskModelMapper;
import com.jiancha.biz.risk.mapper.JcRiskRuleMapper;
import com.jiancha.biz.risk.mapper.JcRiskScanMapper;
import com.jiancha.biz.risk.service.IJcRiskService;

/**
 * 廉政风险防控 Service 实现（M10）
 *
 * <p>设计要点（设计文档 §M10，企业版口径）：</p>
 * <ul>
 *   <li>规则与阈值全部落表可配，引擎按规则编码分发评估，新增规则不改调用方；</li>
 *   <li>扫描落警去重：同规则同对象已有未闭环预警（待核实/核实中）则跳过，避免每日重复刷屏；</li>
 *   <li>确认即转整改：调 E3 整改模块落一条 source_type=warn 的问题，issue_id 回填，
 *      实现「预警 → 核实 → 整改 → 复核 → 归档」闭环；</li>
 *   <li>三级风险视图（部门/岗位/个人）由 SQL 层聚合，前端不做二次计算。</li>
 * </ul>
 *
 * @author 小标
 */
@Service
public class JcRiskServiceImpl implements IJcRiskService {

    /** 预警编号前缀 */
    private static final String NO_PREFIX = "RA";

    /** 预警状态 */
    private static final String STATUS_PENDING    = "0";
    private static final String STATUS_VERIFYING  = "1";
    private static final String STATUS_CONFIRMED  = "2";
    private static final String STATUS_EXCLUDED   = "3";
    private static final String STATUS_ARCHIVED   = "4";

    @Autowired
    private JcRiskRuleMapper ruleMapper;

    @Autowired
    private JcRiskAlertMapper alertMapper;

    @Autowired
    private JcRiskModelMapper modelMapper;

    @Autowired
    private JcRiskScanMapper scanMapper;

    @Autowired
    private RiskRuleEngine ruleEngine;

    @Autowired
    private IJcRectifyService rectifyService;

    // ═══════════════════════ 规则管理 ═══════════════════════

    @Override
    public List<JcRiskRule> selectJcRiskRuleList(JcRiskRule jcRiskRule) {
        return ruleMapper.selectJcRiskRuleList(jcRiskRule);
    }

    @Override
    public JcRiskRule selectJcRiskRuleByRuleId(Long ruleId) {
        return ruleMapper.selectJcRiskRuleByRuleId(ruleId);
    }

    @Override
    public int insertJcRiskRule(JcRiskRule jcRiskRule) {
        if (jcRiskRule == null || StringUtils.isEmpty(jcRiskRule.getRuleCode())) {
            throw new ServiceException("规则编码不能为空");
        }
        if (StringUtils.isEmpty(jcRiskRule.getStatus())) {
            jcRiskRule.setStatus("0");
        }
        if (jcRiskRule.getThreshold() == null) {
            jcRiskRule.setThreshold(1d);
        }
        jcRiskRule.setCreateBy(SecurityUtils.getUsername());
        return ruleMapper.insertJcRiskRule(jcRiskRule);
    }

    @Override
    public int updateJcRiskRule(JcRiskRule jcRiskRule) {
        jcRiskRule.setUpdateBy(SecurityUtils.getUsername());
        return ruleMapper.updateJcRiskRule(jcRiskRule);
    }

    @Override
    public int deleteJcRiskRuleByRuleId(Long ruleId) {
        return ruleMapper.deleteJcRiskRuleByRuleId(ruleId);
    }

    // ═══════════════════════ 岗位风险模型 ═══════════════════════

    @Override
    public List<JcRiskModel> selectJcRiskModelList(JcRiskModel jcRiskModel) {
        return modelMapper.selectJcRiskModelList(jcRiskModel);
    }

    @Override
    public List<Map<String, Object>> sumWeightByPosition() {
        return modelMapper.sumWeightByPosition();
    }

    @Override
    public int insertJcRiskModel(JcRiskModel jcRiskModel) {
        if (jcRiskModel == null || StringUtils.isEmpty(jcRiskModel.getPositionName())) {
            throw new ServiceException("岗位名称不能为空");
        }
        if (jcRiskModel.getWeight() == null) {
            jcRiskModel.setWeight(0d);
        }
        if (StringUtils.isEmpty(jcRiskModel.getStatus())) {
            jcRiskModel.setStatus("0");
        }
        jcRiskModel.setCreateBy(SecurityUtils.getUsername());
        return modelMapper.insertJcRiskModel(jcRiskModel);
    }

    @Override
    public int updateJcRiskModel(JcRiskModel jcRiskModel) {
        return modelMapper.updateJcRiskModel(jcRiskModel);
    }

    @Override
    public int deleteJcRiskModelByModelId(Long modelId) {
        return modelMapper.deleteJcRiskModelByModelId(modelId);
    }

    // ═══════════════════════ 预警记录 ═══════════════════════

    @Override
    public List<JcRiskAlert> selectJcRiskAlertList(JcRiskAlert jcRiskAlert) {
        return alertMapper.selectJcRiskAlertList(jcRiskAlert);
    }

    @Override
    public JcRiskAlert selectJcRiskAlertByAlertId(Long alertId) {
        return alertMapper.selectJcRiskAlertByAlertId(alertId);
    }

    @Override
    public int insertJcRiskAlert(JcRiskAlert jcRiskAlert) {
        if (jcRiskAlert == null || jcRiskAlert.getRuleId() == null) {
            throw new ServiceException("预警必须关联规则");
        }
        if (StringUtils.isEmpty(jcRiskAlert.getAlertNo())) {
            jcRiskAlert.setAlertNo(generateAlertNo());
        }
        if (StringUtils.isEmpty(jcRiskAlert.getStatus())) {
            jcRiskAlert.setStatus(STATUS_PENDING);
        }
        JcRiskRule rule = ruleMapper.selectJcRiskRuleByRuleId(jcRiskAlert.getRuleId());
        if (rule != null) {
            if (StringUtils.isEmpty(jcRiskAlert.getRuleCode())) {
                jcRiskAlert.setRuleCode(rule.getRuleCode());
            }
            if (StringUtils.isEmpty(jcRiskAlert.getRuleName())) {
                jcRiskAlert.setRuleName(rule.getRuleName());
            }
            if (StringUtils.isEmpty(jcRiskAlert.getAlertLevel())) {
                jcRiskAlert.setAlertLevel(rule.getAlertLevel());
            }
        }
        jcRiskAlert.setCreateBy(SecurityUtils.getUsername());
        return alertMapper.insertJcRiskAlert(jcRiskAlert);
    }

    @Override
    @Transactional
    public JcRiskAlert verify(Long alertId, RiskVerifyRequest req) {
        if (req == null || StringUtils.isEmpty(req.getAction())) {
            throw new ServiceException("核实结论不能为空");
        }
        JcRiskAlert alert = alertMapper.selectJcRiskAlertByAlertId(alertId);
        if (alert == null) {
            throw new ServiceException("预警不存在或已删除");
        }
        if (STATUS_CONFIRMED.equals(alert.getStatus()) || STATUS_EXCLUDED.equals(alert.getStatus())
                || STATUS_ARCHIVED.equals(alert.getStatus())) {
            throw new ServiceException("预警已核实完结，不能重复处理");
        }
        String action = req.getAction();
        String operator = SecurityUtils.getUsername();
        alert.setVerifyBy(operator);
        alert.setVerifyTime(new Date());
        alert.setVerifyOpinion(StringUtils.isEmpty(req.getOpinion()) ? "" : req.getOpinion());
        alert.setUpdateBy(operator);

        switch (action) {
            case "confirm": {
                if (req.getTargetDeptId() == null) {
                    throw new ServiceException("确认转整改必须指定整改责任部门");
                }
                if (StringUtils.isEmpty(req.getOpinion())) {
                    throw new ServiceException("核实意见不能为空");
                }
                alert.setStatus(STATUS_CONFIRMED);
                alertMapper.updateJcRiskAlert(alert);
                // 确认即转整改：落一条 source_type=warn 的问题（E3 模块负责幂等）
                JcRectifyIssue issue = new JcRectifyIssue();
                issue.setSourceType("warn");
                issue.setSourceRef("warn:" + alertId);
                issue.setTitle("【风险预警转整改】" + StringUtils.nvl(alert.getRuleName(), "")
                        + " - " + StringUtils.nvl(alert.getObjectName(), ""));
                issue.setDescription("命中规则 " + StringUtils.nvl(alert.getRuleCode(), "")
                        + "：" + StringUtils.nvl(alert.getEvidence(), ""));
                issue.setProblemType("3"); // 廉洁风险
                issue.setSeverity(StringUtils.isEmpty(req.getSeverity())
                        ? mapLevelToSeverity(alert.getAlertLevel()) : req.getSeverity());
                issue.setTargetDeptId(req.getTargetDeptId());
                JcRectifyIssue created = rectifyService.register(issue);
                // 回填 issue_id 到预警（独立 update，避免上面 update 被覆盖）
                JcRiskAlert backfill = new JcRiskAlert();
                backfill.setAlertId(alertId);
                backfill.setIssueId(created.getIssueId());
                backfill.setUpdateBy(operator);
                alertMapper.updateJcRiskAlert(backfill);
                alert.setIssueId(created.getIssueId());
                alert.setIssueNo(created.getIssueNo());
                break;
            }
            case "exclude":
                alert.setStatus(STATUS_EXCLUDED);
                alertMapper.updateJcRiskAlert(alert);
                break;
            case "archive":
                alert.setStatus(STATUS_ARCHIVED);
                alertMapper.updateJcRiskAlert(alert);
                break;
            default:
                throw new ServiceException("未知的核实结论：" + action);
        }
        return alertMapper.selectJcRiskAlertByAlertId(alertId);
    }

    // ═══════════════════════ 引擎扫描 ═══════════════════════

    @Override
    public int scanAndGenerate() {
        List<JcRiskRule> rules = ruleMapper.selectEnabledRules();
        int created = 0;
        for (JcRiskRule rule : rules) {
            for (RiskHit hit : ruleEngine.evaluate(rule)) {
                // 去重：同规则同对象已有未闭环预警则跳过
                int open = alertMapper.countOpenByRuleAndObject(rule.getRuleId(),
                        hit.getObjectType(), hit.getObjectId(), hit.getObjectName());
                if (open > 0) {
                    continue;
                }
                JcRiskAlert alert = new JcRiskAlert();
                alert.setAlertNo(generateAlertNo());
                alert.setRuleId(rule.getRuleId());
                alert.setRuleCode(rule.getRuleCode());
                alert.setRuleName(rule.getRuleName());
                alert.setObjectType(hit.getObjectType());
                alert.setObjectId(hit.getObjectId());
                alert.setObjectName(hit.getObjectName());
                alert.setAlertLevel(rule.getAlertLevel());
                alert.setScore(hit.getScore());
                alert.setEvidence(hit.getEvidence());
                alert.setStatus(STATUS_PENDING);
                alert.setCreateBy("风控引擎");
                created += alertMapper.insertJcRiskAlert(alert);
            }
        }
        return created;
    }

    // ═══════════════════════ 统计 ═══════════════════════

    @Override
    public RiskStat stat() {
        RiskStat stat = new RiskStat();
        Map<String, Object> counts = alertMapper.statCounts();
        stat.setTotal(toLong(counts.get("total")));
        stat.setPending(toLong(counts.get("pending")));
        stat.setVerifying(toLong(counts.get("verifying")));
        stat.setConfirmed(toLong(counts.get("confirmed")));
        stat.setExcluded(toLong(counts.get("excluded")));
        stat.setHighOpen(toLong(counts.get("highOpen")));
        long confirmed = toLong(counts.get("confirmed"));
        long excluded = toLong(counts.get("excluded"));
        long verified = confirmed + excluded;
        stat.setConfirmRate(verified == 0 ? 0.0
                : Math.round(((double) confirmed / verified) * 1000) / 10.0);
        stat.setByLevel(toItems(alertMapper.statByLevel()));
        stat.setByStatus(toItems(alertMapper.statByStatus()));
        stat.setByRule(toItems(alertMapper.statByRule()));
        stat.setMonthlyTrend(toItems(alertMapper.statMonthlyTrend()));
        stat.setDeptView(toItems(alertMapper.statDeptView()));
        stat.setPersonView(toItems(alertMapper.statPersonView()));
        // 岗位层来自模型权重汇总（值即分数，非条数）
        stat.setPositionView(toItems(sumWeightByPosition()));
        return stat;
    }

    // ═══════════════════════ 内部方法 ═══════════════════════

    /** 预警等级 -> 问题严重程度 */
    private String mapLevelToSeverity(String alertLevel) {
        if ("2".equals(alertLevel)) {
            return "重大";
        }
        if ("1".equals(alertLevel)) {
            return "较大";
        }
        return "一般";
    }

    /** 生成预警编号：RA+yyyyMMdd+seq */
    private String generateAlertNo() {
        String prefix = NO_PREFIX + java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String max = scanMapper.selectMaxNoByPrefix(prefix);
        int seq = 1;
        if (StringUtils.isNotEmpty(max) && max.length() > prefix.length()) {
            try {
                seq = Integer.parseInt(max.substring(prefix.length())) + 1;
            } catch (NumberFormatException ignored) {
                // 编号被人工改坏时从 1 重排
            }
        }
        return prefix + String.format("%03d", seq);
    }

    private static Long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }

    private static List<RiskStatItem> toItems(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        List<RiskStatItem> items = new java.util.ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            String name = row.get("name") == null ? "" : String.valueOf(row.get("name"));
            items.add(new RiskStatItem(name, toLong(row.get("value"))));
        }
        return items;
    }
}
