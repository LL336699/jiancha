package com.jiancha.biz.archive.service;

import java.util.List;
import com.jiancha.biz.archive.domain.JcConflictLog;

/**
 * 利益冲突申报变更留痕 Service（段15）
 *
 * @author 小标
 */
public interface IJcConflictLogService
{
    /** 按申报单查询留痕（时间正序） */
    List<JcConflictLog> selectByDeclareId(Long declareId);

    /** 记录一条留痕 */
    int record(Long declareId, String action, String detail, String operator);
}
