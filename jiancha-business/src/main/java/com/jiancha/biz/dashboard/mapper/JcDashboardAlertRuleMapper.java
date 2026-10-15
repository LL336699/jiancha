package com.jiancha.biz.dashboard.mapper;

import com.jiancha.biz.dashboard.domain.DashboardAlertRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 看板预警阈值规则 Mapper（段12 纪检综合看板 · 预警规则引擎）
 *
 * @author 小标
 */
@Mapper
public interface JcDashboardAlertRuleMapper {

    /** 按条件查询规则列表 */
    List<DashboardAlertRule> selectList(DashboardAlertRule rule);

    /** 查询所有启用中的规则（供定时评估与实时评估使用） */
    List<DashboardAlertRule> selectEnabled();

    /** 主键查询 */
    DashboardAlertRule selectById(Long ruleId);

    /** 新增规则 */
    int insert(DashboardAlertRule rule);

    /** 更新规则 */
    int update(DashboardAlertRule rule);

    /** 逻辑删除（按主键批量） */
    int deleteByIds(@Param("ids") Long[] ids);
}
