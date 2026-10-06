package com.jiancha.biz.archive.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jiancha.biz.archive.domain.JcConflictDeclare;
import com.jiancha.biz.archive.mapper.JcConflictDeclareMapper;
import com.jiancha.biz.archive.service.IJcConflictDeclareService;

/**
 * 利益冲突申报单 Service 实现
 *
 * @author 小标
 */
@Service
public class JcConflictDeclareServiceImpl implements IJcConflictDeclareService
{
    @Autowired
    private JcConflictDeclareMapper declareMapper;

    @Override
    public List<JcConflictDeclare> selectJcConflictDeclareList(JcConflictDeclare declare)
    {
        return declareMapper.selectJcConflictDeclareList(declare);
    }

    @Override
    public List<JcConflictDeclare> selectByPersonId(Long personId)
    {
        return declareMapper.selectByPersonId(personId);
    }

    @Override
    public JcConflictDeclare selectByDeclareId(Long declareId)
    {
        return declareMapper.selectByDeclareId(declareId);
    }

    @Override
    public int insertJcConflictDeclare(JcConflictDeclare declare)
    {
        return declareMapper.insertJcConflictDeclare(declare);
    }

    @Override
    public int updateJcConflictDeclare(JcConflictDeclare declare)
    {
        return declareMapper.updateJcConflictDeclare(declare);
    }

    @Override
    public int deleteJcConflictDeclareByDeclareId(Long declareId)
    {
        return declareMapper.deleteJcConflictDeclareByDeclareId(declareId);
    }
}
