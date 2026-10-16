package com.jiancha.biz.petition.mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

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

    // ═══════════════ 段16：满意度 / 催办 / 督办 ═══════════════

    /** 统计：满意度均分与评价人数 */
    Map<String, Object> statSatisfaction();

    /** 统计：满意度分布（1-5 星） */
    List<Map<String, Object>> statSatisfactionDist();

    /** 统计：累计催办次数 */
    Long statUrgeTotal();

    /** 超期督办清单（未办结且超截止时间，按超期时长升序） */
    List<JcPetition> selectOverdue();

    // ═══════════════ 段16：并案 / 热点分析 / 低分复核 ═══════════════

    /** 并案候选清单：同一被举报对象、非本件、双方均非终态可并（跨终态只读展示） */
    List<JcPetition> selectMergeCandidates(@Param("targetName") String targetName, @Param("excludeId") Long excludeId);

    /** 并案主件详情（校验主件存在） */
    JcPetition selectMergeMaster(@Param("masterId") Long masterId);

    /** 执行并案：从件指向主件 */
    int updateMerge(@Param("petitionId") Long petitionId, @Param("masterId") Long masterId,
                    @Param("remark") String remark, @Param("updateBy") String updateBy);

    /** 解除并案：从件恢复独立 */
    int updateUnmerge(@Param("petitionId") Long petitionId, @Param("updateBy") String updateBy);

    /** 已并入主件的从件清单 */
    List<JcPetition> selectMergedChildren(@Param("masterId") Long masterId);

    /** 热点分析：被举报对象 TopN（次数/未办结数/最近举报时间） */
    List<Map<String, Object>> statTopTargets(@Param("limit") int limit);

    /** 低分满意度复核清单（1-2 星评价） */
    List<JcPetition> selectDissatisfied();

    /** 超期且从未催办的举报（自动催办任务扫描用） */
    List<JcPetition> selectOverdueNeverUrged();
}
