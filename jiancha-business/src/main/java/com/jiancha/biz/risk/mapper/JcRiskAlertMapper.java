package com.jiancha.biz.risk.mapper;

import java.util.List;
import java.util.Map;

import com.jiancha.biz.risk.domain.JcRiskAlert;

/**
 * 风险预警记录 Mapper
 *
 * @author 小标
 */
public interface JcRiskAlertMapper {

    /** 分页列表（含关联问题编号） */
    List<JcRiskAlert> selectJcRiskAlertList(JcRiskAlert jcRiskAlert);

    /** 详情 */
    JcRiskAlert selectJcRiskAlertByAlertId(Long alertId);

    /** 查重：同规则同对象是否已有未闭环预警（status 0/1） */
    int countOpenByRuleAndObject(Long ruleId, String objectType, Long objectId, String objectName);

    /** 新增预警 */
    int insertJcRiskAlert(JcRiskAlert jcRiskAlert);

    /** 更新预警（核实/转整改/归档） */
    int updateJcRiskAlert(JcRiskAlert jcRiskAlert);

    /** 统计：总量/待核实/核实中/已确认/已排除/高级别未闭环 */
    Map<String, Object> statCounts();

    /** 统计：等级分布 */
    List<Map<String, Object>> statByLevel();

    /** 统计：状态分布 */
    List<Map<String, Object>> statByStatus();

    /** 统计：规则命中分布 */
    List<Map<String, Object>> statByRule();

    /** 统计：近12个月预警趋势 */
    List<Map<String, Object>> statMonthlyTrend();

    /** 三级视图-部门层：未闭环预警按部门聚合 */
    List<Map<String, Object>> statDeptView();

    /** 三级视图-个人层：未闭环预警按人员聚合 */
    List<Map<String, Object>> statPersonView();
}
