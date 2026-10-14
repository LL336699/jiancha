package com.jiancha.biz.rectify.mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.jiancha.biz.rectify.domain.JcRectifyIssue;

/**
 * 整改问题台账 Mapper
 *
 * @author 小标
 */
public interface JcRectifyIssueMapper {

    /** 分页列表（含措施完成数、剩余天数、关联名称） */
    List<JcRectifyIssue> selectJcRectifyIssueList(JcRectifyIssue jcRectifyIssue);

    /** 详情（含措施完成数、剩余天数、关联名称） */
    JcRectifyIssue selectJcRectifyIssueByIssueId(Long issueId);

    /** 按来源单号查询（防重复登记：同一 petition/clue/warn 只立一项） */
    JcRectifyIssue selectBySourceRef(String sourceRef);

    /** 按编号前缀取最大编号（生成 issueNo 用） */
    String selectMaxNoByPrefix(String prefix);

    /** 新增问题登记 */
    int insertJcRectifyIssue(JcRectifyIssue jcRectifyIssue);

    /** 更新问题（流转/回填） */
    int updateJcRectifyIssue(JcRectifyIssue jcRectifyIssue);

    /** 更新预警级别 */
    int updateWarnLevel(Long issueId, String warnLevel);

    /** 回头看不通过重开：清空销号时间与复核结论（专用语句，通用 update 的 null 过滤会跳过置空） */
    int reopenIssue(Long issueId, String warnLevel, String updateBy);

    /** 逻辑删除（级联措施与复核记录） */
    int deleteJcRectifyIssueByIssueId(Long issueId);

    /** 未销号问题（预警扫描用） */
    List<JcRectifyIssue> selectActiveIssues();

    /** 统计：总量/待整改/整改中/已销号/超期 */
    Map<String, Object> statCounts();

    /** 统计：平均销号天数 */
    Double statAvgCloseDays();

    /** 统计：回头看重开次数 */
    Long statRevisitCount();

    /** 统计：来源分布（已映射中文标签） */
    List<Map<String, Object>> statBySource();

    /** 统计：问题类型分布（已映射中文标签） */
    List<Map<String, Object>> statByType();

    /** 统计：严重程度分布（已映射中文标签） */
    List<Map<String, Object>> statBySeverity();

    /** 统计：状态分布（已映射中文标签） */
    List<Map<String, Object>> statByStatus();

    /** 统计：近12个月登记趋势 */
    List<Map<String, Object>> statMonthlyTrend();

    /** 按责任部门统计未销号问题数（部门对比） */
    List<Map<String, Object>> statByTargetDept(Date since);
}
