package com.jiancha.biz.archive.mapper;

import java.util.List;
import com.jiancha.biz.archive.domain.JcConflictDeclare;

/**
 * 利益冲突申报单 数据层
 *
 * @author 小标
 */
public interface JcConflictDeclareMapper
{
    /** 查询申报单列表 */
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
}
