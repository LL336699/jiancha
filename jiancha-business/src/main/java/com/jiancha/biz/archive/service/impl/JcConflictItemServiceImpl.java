package com.jiancha.biz.archive.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jiancha.biz.archive.domain.JcConflictItem;
import com.jiancha.biz.archive.mapper.JcConflictItemMapper;
import com.jiancha.biz.archive.service.IJcConflictItemService;

/**
 * 利益冲突申报事项明细 Service 实现
 *
 * @author 小标
 */
@Service
public class JcConflictItemServiceImpl implements IJcConflictItemService
{
    @Autowired
    private JcConflictItemMapper itemMapper;

    @Override
    public List<JcConflictItem> selectByDeclareId(Long declareId)
    {
        return itemMapper.selectByDeclareId(declareId);
    }

    @Override
    public JcConflictItem selectByItemId(Long itemId)
    {
        return itemMapper.selectByItemId(itemId);
    }

    @Override
    public int insertJcConflictItem(JcConflictItem item)
    {
        return itemMapper.insertJcConflictItem(item);
    }

    @Override
    public int updateJcConflictItem(JcConflictItem item)
    {
        return itemMapper.updateJcConflictItem(item);
    }

    @Override
    public int deleteJcConflictItemByItemId(Long itemId)
    {
        return itemMapper.deleteJcConflictItemByItemId(itemId);
    }
}
