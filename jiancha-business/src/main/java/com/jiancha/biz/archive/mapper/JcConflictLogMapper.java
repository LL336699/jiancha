package com.jiancha.biz.archive.mapper;

import java.util.List;
import com.jiancha.biz.archive.domain.JcConflictLog;

/**
 * 利益冲突申报变更留痕 数据层（段15）
 *
 * @author 小标
 */
public interface JcConflictLogMapper
{
    /** 按申报单查询留痕（时间正序） */
    List<JcConflictLog> selectByDeclareId(Long declareId);

    /** 插入一条留痕 */
    int insertJcConflictLog(JcConflictLog log);
}
