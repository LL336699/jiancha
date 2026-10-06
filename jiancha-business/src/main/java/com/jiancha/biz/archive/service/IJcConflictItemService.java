package com.jiancha.biz.archive.service;

import java.util.List;
import com.jiancha.biz.archive.domain.JcConflictItem;

/**
 * 利益冲突申报事项明细 Service
 *
 * @author 小标
 */
public interface IJcConflictItemService
{
    List<JcConflictItem> selectByDeclareId(Long declareId);

    JcConflictItem selectByItemId(Long itemId);

    int insertJcConflictItem(JcConflictItem item);

    int updateJcConflictItem(JcConflictItem item);

    int deleteJcConflictItemByItemId(Long itemId);
}
