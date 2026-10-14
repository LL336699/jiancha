package com.jiancha.biz.risk.service;

import java.util.List;

import com.jiancha.biz.risk.domain.JcRiskAlert;
import com.jiancha.biz.risk.domain.JcRiskModel;
import com.jiancha.biz.risk.domain.JcRiskRule;
import com.jiancha.biz.risk.domain.RiskStat;
import com.jiancha.biz.risk.domain.RiskVerifyRequest;

/**
 * 廉政风险防控 Service（M10）
 *
 * <p>三条主线：规则管理（可配置阈值）、引擎扫描落警（日调度 + 手动）、
 * 预警核实闭环（待核实 → 核实中 → 确认转整改 / 排除误报 → 归档）。</p>
 *
 * @author 小标
 */
public interface IJcRiskService {

    // ═══════════════════════ 规则管理 ═══════════════════════

    /** 规则列表 */
    List<JcRiskRule> selectJcRiskRuleList(JcRiskRule jcRiskRule);

    /** 规则详情 */
    JcRiskRule selectJcRiskRuleByRuleId(Long ruleId);

    /** 新增规则 */
    int insertJcRiskRule(JcRiskRule jcRiskRule);

    /** 更新规则 */
    int updateJcRiskRule(JcRiskRule jcRiskRule);

    /** 删除规则 */
    int deleteJcRiskRuleByRuleId(Long ruleId);

    // ═══════════════════════ 岗位风险模型 ═══════════════════════

    /** 模型行列表 */
    List<JcRiskModel> selectJcRiskModelList(JcRiskModel jcRiskModel);

    /** 岗位权重汇总 */
    List<java.util.Map<String, Object>> sumWeightByPosition();

    /** 新增模型行 */
    int insertJcRiskModel(JcRiskModel jcRiskModel);

    /** 更新模型行 */
    int updateJcRiskModel(JcRiskModel jcRiskModel);

    /** 删除模型行 */
    int deleteJcRiskModelByModelId(Long modelId);

    // ═══════════════════════ 预警记录 ═══════════════════════

    /** 预警分页列表 */
    List<JcRiskAlert> selectJcRiskAlertList(JcRiskAlert jcRiskAlert);

    /** 预警详情 */
    JcRiskAlert selectJcRiskAlertByAlertId(Long alertId);

    /** 手动登记预警 */
    int insertJcRiskAlert(JcRiskAlert jcRiskAlert);

    /**
     * 核实预警。
     *
     * <p>confirm 确认：必须给出责任部门，自动落一条 source_type=warn 的整改问题并回填 issue_id；
     * exclude 排除（误报）；archive 归档。</p>
     */
    JcRiskAlert verify(Long alertId, RiskVerifyRequest req);

    // ═══════════════════════ 引擎扫描 ═══════════════════════

    /**
     * 全量扫描：按启用规则逐条评估，命中且无未闭环同类预警时落库。
     *
     * @return 新增预警条数
     */
    int scanAndGenerate();

    // ═══════════════════════ 统计 ═══════════════════════

    /** 统计与三级风险视图 */
    RiskStat stat();
}
