package com.jiancha.biz.archive.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jiancha.biz.archive.domain.JcPersonRelation;
import com.jiancha.biz.archive.mapper.JcPersonRelationMapper;
import com.jiancha.biz.archive.service.IJcPersonRelationService;

/**
 * 亲属关系及经商办企业情况 Service 实现
 *
 * @author 小标
 */
@Service
public class JcPersonRelationServiceImpl implements IJcPersonRelationService
{
    @Autowired
    private JcPersonRelationMapper jcPersonRelationMapper;

    @Override
    public List<JcPersonRelation> selectJcPersonRelationList(JcPersonRelation jcPersonRelation)
    {
        return jcPersonRelationMapper.selectJcPersonRelationList(jcPersonRelation);
    }

    @Override
    public List<JcPersonRelation> selectByPersonId(Long personId)
    {
        return jcPersonRelationMapper.selectByPersonId(personId);
    }

    @Override
    public JcPersonRelation selectByRelationId(Long relationId)
    {
        return jcPersonRelationMapper.selectByRelationId(relationId);
    }

    @Override
    public int insertJcPersonRelation(JcPersonRelation jcPersonRelation)
    {
        return jcPersonRelationMapper.insertJcPersonRelation(jcPersonRelation);
    }

    @Override
    public int updateJcPersonRelation(JcPersonRelation jcPersonRelation)
    {
        return jcPersonRelationMapper.updateJcPersonRelation(jcPersonRelation);
    }

    @Override
    public int deleteJcPersonRelationByRelationId(Long relationId)
    {
        return jcPersonRelationMapper.deleteJcPersonRelationByRelationId(relationId);
    }

    @Override
    public int deleteByPersonId(Long personId)
    {
        return jcPersonRelationMapper.deleteByPersonId(personId);
    }
}
