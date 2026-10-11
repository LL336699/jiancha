package com.jiancha.biz.report.mapper;

import com.jiancha.biz.report.domain.JcReportTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文书模板 Mapper（M6 文书辅助）
 */
@Mapper
public interface JcReportTemplateMapper {

    JcReportTemplate selectByPrimaryKey(@Param("templateId") Long templateId);

    List<JcReportTemplate> selectList(@Param("template") JcReportTemplate template);

    int insert(JcReportTemplate template);

    int update(JcReportTemplate template);

    int deleteByPrimaryKey(@Param("templateId") Long templateId);
}
