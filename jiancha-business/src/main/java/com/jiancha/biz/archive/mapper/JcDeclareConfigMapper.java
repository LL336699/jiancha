package com.jiancha.biz.archive.mapper;

import java.util.List;
import com.jiancha.biz.archive.domain.JcDeclareConfig;

/**
 * 年度集中申报截止配置 数据层（段15）
 *
 * @author 小标
 */
public interface JcDeclareConfigMapper
{
    /** 查询配置列表 */
    List<JcDeclareConfig> selectJcDeclareConfigList(JcDeclareConfig config);

    /** 按年度查询配置 */
    JcDeclareConfig selectByYear(Integer year);

    /** 按ID查询 */
    JcDeclareConfig selectByConfigId(Long configId);

    /** 新增配置 */
    int insertJcDeclareConfig(JcDeclareConfig config);

    /** 修改配置 */
    int updateJcDeclareConfig(JcDeclareConfig config);

    /** 删除配置 */
    int deleteJcDeclareConfigByConfigId(Long configId);
}
