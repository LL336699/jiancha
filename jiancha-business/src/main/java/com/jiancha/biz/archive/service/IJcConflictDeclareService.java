package com.jiancha.biz.archive.service;

import java.util.List;
import com.jiancha.biz.archive.domain.JcConflictDeclare;

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
}
