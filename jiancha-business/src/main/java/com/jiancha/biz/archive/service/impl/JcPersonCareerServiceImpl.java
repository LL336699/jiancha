package com.jiancha.biz.archive.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jiancha.biz.archive.domain.JcPersonCareer;
import com.jiancha.biz.archive.mapper.JcPersonCareerMapper;
import com.jiancha.biz.archive.service.IJcPersonCareerService;

/**
 * 任职经历 Service 实现
 *
 * @author 小标
 */
@Service
public class JcPersonCareerServiceImpl implements IJcPersonCareerService
{
    @Autowired
    private JcPersonCareerMapper jcPersonCareerMapper;

    @Override
    public List<JcPersonCareer> selectJcPersonCareerList(JcPersonCareer jcPersonCareer)
    {
        return jcPersonCareerMapper.selectJcPersonCareerList(jcPersonCareer);
    }

    @Override
    public List<JcPersonCareer> selectByPersonId(Long personId)
    {
        return jcPersonCareerMapper.selectByPersonId(personId);
    }

    @Override
    public JcPersonCareer selectByCareerId(Long careerId)
    {
        return jcPersonCareerMapper.selectByCareerId(careerId);
    }

    @Override
    public int insertJcPersonCareer(JcPersonCareer jcPersonCareer)
    {
        return jcPersonCareerMapper.insertJcPersonCareer(jcPersonCareer);
    }

    @Override
    public int updateJcPersonCareer(JcPersonCareer jcPersonCareer)
    {
        return jcPersonCareerMapper.updateJcPersonCareer(jcPersonCareer);
    }

    @Override
    public int deleteJcPersonCareerByCareerId(Long careerId)
    {
        return jcPersonCareerMapper.deleteJcPersonCareerByCareerId(careerId);
    }

    @Override
    public int deleteByPersonId(Long personId)
    {
        return jcPersonCareerMapper.deleteByPersonId(personId);
    }
}
