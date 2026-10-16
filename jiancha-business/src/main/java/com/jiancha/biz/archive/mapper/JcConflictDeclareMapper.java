package com.jiancha.biz.archive.mapper;

import java.util.List;
import java.util.Map;
import com.jiancha.biz.archive.domain.ConflictNameValue;
import com.jiancha.biz.archive.domain.JcConflictDeclare;
import com.jiancha.biz.archive.domain.JcPerson;

/**
 * 利益冲突申报单 数据层
 *
 * @author 小标
 */
public interface JcConflictDeclareMapper
{
    /** 查询申报单列表（联表人员姓名/部门） */
    List<JcConflictDeclare> selectJcConflictDeclareList(JcConflictDeclare declare);

    /** 按人员查询申报记录 */
    List<JcConflictDeclare> selectByPersonId(Long personId);

    /** 按ID查询 */
    JcConflictDeclare selectByDeclareId(Long declareId);

    /** 新增申报单 */
    int insertJcConflictDeclare(JcConflictDeclare declare);

    /** 修改申报单 */
    int updateJcConflictDeclare(JcConflictDeclare declare);

    /** 逻辑删除 */
    int deleteJcConflictDeclareByDeclareId(Long declareId);

    /** 段15：按年度统计申报总量 */
    Long countByYear(Integer year);

    /** 段15：按年度统计状态分布（名/量） */
    List<ConflictNameValue> statByStatus(Integer year);

    /** 段15：按年度统计申报类型分布（名/量） */
    List<ConflictNameValue> statByType(Integer year);

    /** 段15：按年度统计月度申报趋势（YYYY-MM/量） */
    List<ConflictNameValue> statByMonth(Integer year);

    /** 段15：按年度统计存在利益冲突事项（is_conflict=1）的申报单数 */
    Long countConflictDeclares(Integer year);

    /** 段15：按年度统计命中供应商黑名单/禁入的申报事项数 */
    Long countBlacklistItems(Integer year);

    /** 段15：该年度尚无任何申报的在岗人员（集中申报进度用） */
    List<JcPerson> selectUnDeclared(Integer year);

    /** 段15：按人员维度聚合申报情况（重点关注人员：申报数/事项数/冲突事项数） */
    List<Map<String, Object>> statByPerson(Integer year);

    /** 段15：年度申报清单全量导出（联表人员，不分页） */
    List<JcConflictDeclare> selectDeclareForExport(Integer year);
}
