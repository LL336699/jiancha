package com.jiancha.biz.archive.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jiancha.biz.archive.domain.JcDeclareConfig;
import com.jiancha.biz.archive.mapper.JcDeclareConfigMapper;
import com.jiancha.biz.archive.service.IJcDeclareConfigService;

/**
 * 年度集中申报截止配置 Service 实现（段15）
 *
 * @author 小标
 */
@Service
public class JcDeclareConfigServiceImpl implements IJcDeclareConfigService
{
    @Autowired
    private JcDeclareConfigMapper configMapper;

    @Override
    public List<JcDeclareConfig> selectJcDeclareConfigList(JcDeclareConfig config)
    {
        return configMapper.selectJcDeclareConfigList(config);
    }

    @Override
    public JcDeclareConfig selectByYear(Integer year)
    {
        return configMapper.selectByYear(year);
    }

    @Override
    public JcDeclareConfig selectByConfigId(Long configId)
    {
        return configMapper.selectByConfigId(configId);
    }

    @Override
    public int insertJcDeclareConfig(JcDeclareConfig config)
    {
        return configMapper.insertJcDeclareConfig(config);
    }

    @Override
    public int updateJcDeclareConfig(JcDeclareConfig config)
    {
        return configMapper.updateJcDeclareConfig(config);
    }

    @Override
    public int deleteJcDeclareConfigByConfigId(Long configId)
    {
        return configMapper.deleteJcDeclareConfigByConfigId(configId);
    }
}
