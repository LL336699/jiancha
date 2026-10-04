package com.jiancha.biz.archive.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jiancha.biz.archive.domain.JcPersonTag;
import com.jiancha.biz.archive.mapper.JcPersonTagMapper;
import com.jiancha.biz.archive.service.IJcPersonTagService;

/**
 * 廉政画像标签 Service 实现（全部可溯源）
 *
 * @author 小标
 */
@Service
public class JcPersonTagServiceImpl implements IJcPersonTagService
{
    @Autowired
    private JcPersonTagMapper jcPersonTagMapper;

    @Override
    public List<JcPersonTag> selectJcPersonTagList(JcPersonTag jcPersonTag)
    {
        return jcPersonTagMapper.selectJcPersonTagList(jcPersonTag);
    }

    @Override
    public List<JcPersonTag> selectByPersonId(Long personId)
    {
        return jcPersonTagMapper.selectByPersonId(personId);
    }

    @Override
    public JcPersonTag selectByTagId(Long tagId)
    {
        return jcPersonTagMapper.selectByTagId(tagId);
    }

    @Override
    public int insertJcPersonTag(JcPersonTag jcPersonTag)
    {
        return jcPersonTagMapper.insertJcPersonTag(jcPersonTag);
    }

    @Override
    public int updateJcPersonTag(JcPersonTag jcPersonTag)
    {
        return jcPersonTagMapper.updateJcPersonTag(jcPersonTag);
    }

    @Override
    public int deleteJcPersonTagByTagId(Long tagId)
    {
        return jcPersonTagMapper.deleteJcPersonTagByTagId(tagId);
    }

    @Override
    public int deleteByPersonId(Long personId)
    {
        return jcPersonTagMapper.deleteByPersonId(personId);
    }
}
