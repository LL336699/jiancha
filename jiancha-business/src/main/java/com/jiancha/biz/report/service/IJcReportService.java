package com.jiancha.biz.report.service;

import com.jiancha.biz.report.domain.JcReportRecord;
import com.jiancha.biz.report.domain.JcReportTemplate;

import java.util.List;
import java.util.Map;

/**
 * 文书辅助 Service（M6 文书辅助）
 */
public interface IJcReportService {

    /** 模板列表 */
    List<JcReportTemplate> listTemplates(JcReportTemplate template);

    /** 模板详情 */
    JcReportTemplate getTemplate(Long templateId);

    /**
     * 生成文书草稿 + 量纪建议
     *
     * @param templateId 所选模板
     * @param params     用户填写的案情要素（key 对应模板变量 ${key}）
     * @return 含 generatedContent / penaltyAdvice / clauseBasis / recordId
     */
    Map<String, Object> generate(Long templateId, Map<String, String> params);

    /** 生成记录列表 */
    List<JcReportRecord> listRecords(JcReportRecord record);

    /** 生成记录详情 */
    JcReportRecord getRecord(Long recordId);

    /** 删除生成记录 */
    int removeRecord(Long recordId);
}
