package com.jiancha.biz.risk.mapper;

import java.util.List;

import com.jiancha.biz.risk.domain.JcRiskRule;

/**
 * 风险预警规则 Mapper
 *
 * @author 小标
 */
public interface JcRiskRuleMapper {

    /** 全部规则（含停用） */
    List<JcRiskRule> selectJcRiskRuleList(JcRiskRule jcRiskRule);

    /** 启用规则（引擎评估用） */
    List<JcRiskRule> selectEnabledRules();

    /** 详情 */
    JcRiskRule selectJcRiskRuleByRuleId(Long ruleId);

    /** 新增规则 */
    int insertJcRiskRule(JcRiskRule jcRiskRule);

    /** 更新规则 */
    int updateJcRiskRule(JcRiskRule jcRiskRule);

    /** 删除规则 */
    int deleteJcRiskRuleByRuleId(Long ruleId);
}
