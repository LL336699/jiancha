package com.jiancha.biz.archive.service;

import java.util.List;
import java.util.Map;
import com.jiancha.biz.archive.domain.JcConflictDeclare;
import com.jiancha.biz.archive.domain.JcPerson;

/**
 * 利益冲突申报单 Service
 *
 * @author 小标
 */
public interface IJcConflictDeclareService
{
    List<JcConflictDeclare> selectJcConflictDeclareList(JcConflictDeclare declare);

    List<JcConflictDeclare> selectByPersonId(Long personId);

    JcConflictDeclare selectByDeclareId(Long declareId);

    int insertJcConflictDeclare(JcConflictDeclare declare);

    int updateJcConflictDeclare(JcConflictDeclare declare);

    int deleteJcConflictDeclareByDeclareId(Long declareId);

    /**
     * 段15：年度申报统计（看板用）
     *
     * @param year 申报年度
     * @return total 总量 / byStatus 状态分布 / byType 类型分布 / monthly 月度趋势 / conflictDeclares 含冲突事项申报单数
     */
    Map<String, Object> statByYear(Integer year);

    /**
     * 段15：该年度尚无任何申报的在岗人员（集中申报进度督办）
     */
    List<JcPerson> selectUnDeclared(Integer year);

    /**
     * 段15：按人员维度聚合申报情况（重点关注人员 Top10）
     */
    List<Map<String, Object>> statByPerson(Integer year);

    /**
     * 段15：年度申报清单全量导出（联表人员，不分页）
     */
    List<JcConflictDeclare> selectDeclareForExport(Integer year);
}
