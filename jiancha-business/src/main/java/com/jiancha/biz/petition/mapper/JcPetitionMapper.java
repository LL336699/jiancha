package com.jiancha.biz.petition.mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.jiancha.biz.petition.domain.JcPetition;

/**
 * 舞弊举报 Mapper
 *
 * @author 小标
 */
public interface JcPetitionMapper {

    /** 按回执码检索哈希定位举报（O(1)，免登录回执查询用） */
    JcPetition selectByReceiptLookup(String receiptLookup);

    /** 分页查询（含关联线索编号展示） */
    List<JcPetition> selectJcPetitionList(JcPetition jcPetition);

    /** 详情 */
    JcPetition selectJcPetitionByPetitionId(Long petitionId);

    /** 新增举报（免登录提交） */
    int insertJcPetition(JcPetition jcPetition);

    /** 更新举报（流转/回填） */
    int updateJcPetition(JcPetition jcPetition);

    /** 更新预警级别 */
    int updateWarnLevel(Long petitionId, String warnLevel);

    /** 逻辑删除 */
    int deleteJcPetitionByPetitionId(Long petitionId);

    /** 未办结举报（预警扫描用） */
    List<JcPetition> selectActivePetitions();

    /** 统计：总量/待受理/办理中/已办/超期 */
    Map<String, Object> statCounts();

    /** 统计：平均办理小时数 */
    Double statAvgHandleHours();

    /** 统计：状态分布（已映射中文标签） */
    List<Map<String, Object>> statByStatus();

    /** 统计：舞弊类型分布（已映射中文标签） */
    List<Map<String, Object>> statByCategory();

    /** 统计：渠道分布（已映射中文标签） */
    List<Map<String, Object>> statByChannel();

    /** 统计：近12个月提交趋势 */
    List<Map<String, Object>> statMonthlyTrend();

    /** 按被举报对象+时间窗查询历史举报（重复提示，不自动合并） */
    List<JcPetition> selectRecentByTarget(String targetName, Date since);
}
