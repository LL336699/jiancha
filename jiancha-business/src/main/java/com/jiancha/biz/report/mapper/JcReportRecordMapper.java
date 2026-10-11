package com.jiancha.biz.report.mapper;

import com.jiancha.biz.report.domain.JcReportRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文书生成记录 Mapper（M6 文书辅助）
 */
@Mapper
public interface JcReportRecordMapper {

    JcReportRecord selectByPrimaryKey(@Param("recordId") Long recordId);

    List<JcReportRecord> selectList(@Param("record") JcReportRecord record);

    int insert(JcReportRecord record);

    int deleteByPrimaryKey(@Param("recordId") Long recordId);
}
