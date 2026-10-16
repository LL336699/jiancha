package com.jiancha.biz.archive.service;

import java.util.List;
import com.jiancha.biz.archive.domain.JcDeclareConfig;

/**
 * 年度集中申报截止配置 Service（段15）
 *
 * @author 小标
 */
public interface IJcDeclareConfigService
{
    List<JcDeclareConfig> selectJcDeclareConfigList(JcDeclareConfig config);

    JcDeclareConfig selectByYear(Integer year);

    JcDeclareConfig selectByConfigId(Long configId);

    int insertJcDeclareConfig(JcDeclareConfig config);

    int updateJcDeclareConfig(JcDeclareConfig config);

    int deleteJcDeclareConfigByConfigId(Long configId);
}
