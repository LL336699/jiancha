package com.jiancha.biz.archive.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jiancha.biz.archive.domain.JcPerson;
import com.jiancha.biz.archive.mapper.JcPersonMapper;
import com.jiancha.biz.archive.service.IJcPersonService;

/**
 * 干部廉政档案 Service 实现
 *
 * @author 小标
 */
@Service
public class JcPersonServiceImpl implements IJcPersonService
{
    @Autowired
    private JcPersonMapper jcPersonMapper;

    @Override
    public List<JcPerson> selectJcPersonList(JcPerson jcPerson)
    {
        return jcPersonMapper.selectJcPersonList(jcPerson);
    }

    @Override
    public JcPerson selectJcPersonByPersonId(Long personId)
    {
        return jcPersonMapper.selectJcPersonByPersonId(personId);
    }

    @Override
    public int insertJcPerson(JcPerson jcPerson)
    {
        return jcPersonMapper.insertJcPerson(jcPerson);
    }

    @Override
    public int updateJcPerson(JcPerson jcPerson)
    {
        return jcPersonMapper.updateJcPerson(jcPerson);
    }

    @Override
    public int deleteJcPersonByPersonId(Long personId)
    {
        // 档案涉及监督留痕，逻辑删除（del_flag = '2'）
        return jcPersonMapper.deleteJcPersonByPersonId(personId);
    }

    @Override
    public int countAll()
    {
        return jcPersonMapper.countAll();
    }
}
