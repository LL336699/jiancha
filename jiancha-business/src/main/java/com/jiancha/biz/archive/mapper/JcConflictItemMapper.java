package com.jiancha.biz.archive.mapper;

import java.util.List;
import com.jiancha.biz.archive.domain.JcConflictItem;

/**
 * 利益冲突申报事项明细 数据层
 *
 * @author 小标
 */
public interface JcConflictItemMapper
{
    /** 按申报单查询明细 */
    List<JcConflictItem> selectByDeclareId(Long declareId);

    /** 按ID查询 */
    JcConflictItem selectByItemId(Long itemId);

    /** 新增明细 */
    int insertJcConflictItem(JcConflictItem item);

    /** 修改明细 */
    int updateJcConflictItem(JcConflictItem item);

    /** 逻辑删除 */
    int deleteJcConflictItemByItemId(Long itemId);

    /** 删除申报单下全部明细 */
    int deleteByDeclareId(Long declareId);
}
