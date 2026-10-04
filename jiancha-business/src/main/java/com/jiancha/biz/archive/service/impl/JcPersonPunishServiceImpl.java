package com.jiancha.biz.archive.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jiancha.biz.archive.domain.JcPersonPunish;
import com.jiancha.biz.archive.mapper.JcPersonPunishMapper;
import com.jiancha.biz.archive.service.IJcPersonPunishService;

/**
 * 处分记录 Service 实现
 *
 * @author 小标
 */
@Service
public class JcPersonPunishServiceImpl implements IJcPersonPunishService
{
    @Autowired
    private JcPersonPunishMapper jcPersonPunishMapper;

    @Override
    public List<JcPersonPunish> selectJcPersonPunishList(JcPersonPunish jcPersonPunish)
    {
        return jcPersonPunishMapper.selectJcPersonPunishList(jcPersonPunish);
    }

    @Override
    public List<JcPersonPunish> selectByPersonId(Long personId)
    {
        return jcPersonPunishMapper.selectByPersonId(personId);
    }

    @Override
    public JcPersonPunish selectByPunishId(Long punishId)
    {
        return jcPersonPunishMapper.selectByPunishId(punishId);
    }

    @Override
    public int insertJcPersonPunish(JcPersonPunish jcPersonPunish)
    {
        return jcPersonPunishMapper.insertJcPersonPunish(jcPersonPunish);
    }

    @Override
    public int updateJcPersonPunish(JcPersonPunish jcPersonPunish)
    {
        return jcPersonPunishMapper.updateJcPersonPunish(jcPersonPunish);
    }

    @Override
    public int deleteJcPersonPunishByPunishId(Long punishId)
    {
        return jcPersonPunishMapper.deleteJcPersonPunishByPunishId(punishId);
    }

    @Override
    public int deleteByPersonId(Long personId)
    {
        return jcPersonPunishMapper.deleteByPersonId(personId);
    }
}
