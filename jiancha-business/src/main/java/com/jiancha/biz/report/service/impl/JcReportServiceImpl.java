package com.jiancha.biz.report.service.impl;

import com.jiancha.ai.prompt.PromptTemplate;
import com.jiancha.biz.knowledge.rag.KnowledgeRetriever;
import com.jiancha.biz.report.domain.JcReportRecord;
import com.jiancha.biz.report.domain.JcReportTemplate;
import com.jiancha.biz.report.mapper.JcReportRecordMapper;
import com.jiancha.biz.report.mapper.JcReportTemplateMapper;
import com.jiancha.biz.report.service.IJcReportService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 文书辅助 Service 实现（M6 文书辅助）
 *
 * <p><b>设计要点</b>（见设计文档 §M6）：</p>
 * <ol>
 *   <li>文书草稿与量纪建议均基于"已检索到的法条 + 用户填写的要素"生成，不凭空编造；</li>
 *   <li>量纪建议仅作提示（{@code QUANTIFY} 模板内置 disclaimer，且 {@code COMMON_CONSTRAINT} 第 5 条约束），最终认定须人工签字；</li>
 *   <li>每次生成均落库留痕（操作人 + 时间 + 部门），满足流程强制留痕。</li>
 * </ol>
 *
 * @author 小标
 */
@Service
public class JcReportServiceImpl implements IJcReportService {

    private static final Logger log = LoggerFactory.getLogger(JcReportServiceImpl.class);

    @Autowired
    private JcReportTemplateMapper templateMapper;

    @Autowired
    private JcReportRecordMapper recordMapper;

    /** 复用 P4 知识库 RAG 检索法条 */
    @Autowired
    private KnowledgeRetriever retriever;

    /** ChatClient 由 AiChatConfig 条件化创建；未配置 LLM 时为空 */
    @Autowired
    private ObjectProvider<ChatClient> chatClientProvider;

    @Override
    public List<JcReportTemplate> listTemplates(JcReportTemplate template) {
        return templateMapper.selectList(template == null ? new JcReportTemplate() : template);
    }

    @Override
    public JcReportTemplate getTemplate(Long templateId) {
        return templateMapper.selectByPrimaryKey(templateId);
    }

    @Override
    public Map<String, Object> generate(Long templateId, Map<String, String> params) {
        JcReportTemplate tpl = templateMapper.selectByPrimaryKey(templateId);
        if (tpl == null) {
            throw new ServiceException("文书模板不存在");
        }
        if (params == null) {
            params = new LinkedHashMap<>();
        }

        // ① 模板变量替换
        Map<String, Object> paramMap = new LinkedHashMap<>(params);
        String filled = PromptTemplate.formatMap(tpl.getTemplateContent(), paramMap);

        // ② 拼装案情摘要，用于检索知识库法条
        String factSummary = buildFactSummary(params);

        // ③ 检索相关法条（复用 P4 知识库 RAG）
        List<KnowledgeRetriever.RetrievedChunk> hits = retriever.retrieve(factSummary, null);
        String lawContext = buildLawContext(hits);

        // ④ 生成文书草稿
        String draft = genDraft(tpl.getDocType(), filled);
        // ⑤ 量纪建议（结构化，含免责说明）
        String advice = genPenalty(factSummary, lawContext);

        // ⑥ 落库留痕
        String operator = SecurityUtils.getUsername();
        String deptName = deptNameOf();
        JcReportRecord rec = new JcReportRecord();
        rec.setTemplateId(templateId);
        rec.setDocType(tpl.getDocType());
        rec.setTitle(tpl.getTemplateName());
        rec.setInputJson(toJson(params));
        rec.setGeneratedContent(draft);
        rec.setPenaltyAdvice(advice);
        rec.setOperator(operator);
        rec.setDeptName(deptName);
        recordMapper.insert(rec);

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("recordId", rec.getRecordId());
        r.put("generatedContent", draft);
        r.put("penaltyAdvice", advice);
        r.put("clauseBasis", buildCitations(hits));
        r.put("operator", operator);
        r.put("deptName", deptName);
        return r;
    }

    @Override
    public List<JcReportRecord> listRecords(JcReportRecord record) {
        return recordMapper.selectList(record == null ? new JcReportRecord() : record);
    }

    @Override
    public JcReportRecord getRecord(Long recordId) {
        return recordMapper.selectByPrimaryKey(recordId);
    }

    @Override
    public int removeRecord(Long recordId) {
        return recordMapper.deleteByPrimaryKey(recordId);
    }

    // ══════════════════ 内部工具 ════════════════

    private String genDraft(String docType, String filled) {
        try {
            ChatClient client = chatClientProvider.getIfAvailable();
            if (client == null) {
                return buildDraftFallback(filled);
            }
            String prompt = PromptTemplate.format(PromptTemplate.REPORT_TEMPLATE, docType, filled);
            String out = client.prompt().user(prompt).call().content();
            return (out == null || out.trim().isEmpty()) ? buildDraftFallback(filled) : out;
        } catch (Exception e) {
            log.warn("文书生成调用 LLM 失败，降级为模板原文：{}", e.getMessage());
            return buildDraftFallback(filled);
        }
    }

    private String buildDraftFallback(String filled) {
        return "（当前未启用智能归纳，以下为按模板填充后的文书草稿，请人工完善）\n\n" + filled
                + "\n\n（本初稿由系统辅助生成，须经承办人员核实定稿。）";
    }

    private String genPenalty(String factSummary, String lawContext) {
        try {
            ChatClient client = chatClientProvider.getIfAvailable();
            if (client == null) {
                return buildPenaltyFallback(lawContext);
            }
            String prompt = PromptTemplate.format(PromptTemplate.QUANTIFY, factSummary, lawContext);
            String out = client.prompt().user(prompt).call().content();
            return (out == null || out.trim().isEmpty()) ? buildPenaltyFallback(lawContext) : out;
        } catch (Exception e) {
            log.warn("量纪建议调用 LLM 失败，降级为检索依据：{}", e.getMessage());
            return buildPenaltyFallback(lawContext);
        }
    }

    private String buildPenaltyFallback(String lawContext) {
        if (lawContext == null || lawContext.trim().isEmpty()) {
            return "知识库中未检索到相关法条依据，无法生成量纪建议。请补充上传对应法规制度后重试。\n\n"
                    + "本建议由AI辅助生成，仅供参考，须经人工复核后作出认定。";
        }
        return "（以下为检索到的相关法条依据，量纪建议须人工结合全案认定）\n\n" + lawContext
                + "\n\n本建议由AI辅助生成，仅供参考，须经人工复核后作出认定。";
    }

    private String buildFactSummary(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (e.getValue() != null && !e.getValue().trim().isEmpty()) {
                sb.append(e.getKey()).append("：").append(e.getValue()).append("；");
            }
        }
        return sb.toString();
    }

    private String buildLawContext(List<KnowledgeRetriever.RetrievedChunk> hits) {
        if (hits == null || hits.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (KnowledgeRetriever.RetrievedChunk c : hits) {
            sb.append("【依据").append(i).append("】").append(c.citation()).append("\n");
            sb.append(c.getContent()).append("\n\n");
            i++;
        }
        return sb.toString();
    }

    private List<Map<String, Object>> buildCitations(List<KnowledgeRetriever.RetrievedChunk> hits) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (hits == null) {
            return list;
        }
        int i = 1;
        for (KnowledgeRetriever.RetrievedChunk c : hits) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("index", i++);
            m.put("docName", c.getDocName());
            m.put("clauseNo", c.getClauseNo());
            m.put("score", Math.round(c.getScore() * 1000) / 1000.0);
            m.put("citation", c.citation());
            m.put("excerpt", excerpt(c.getContent(), 120));
            list.add(m);
        }
        return list;
    }

    private String deptNameOf() {
        try {
            var user = SecurityUtils.getLoginUser().getUser();
            if (user.getDept() != null) {
                return user.getDept().getDeptName();
            }
        } catch (Exception ignored) {
            // 非 Web 上下文或用户信息缺失时不报错
        }
        return null;
    }

    private String toJson(Map<String, String> params) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            String v = e.getValue() == null ? "" : e.getValue().replace("\"", "\\\"");
            sb.append("\"").append(e.getKey()).append("\":\"").append(v).append("\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private String excerpt(String s, int max) {
        if (s == null) {
            return "";
        }
        String t = s.trim();
        return t.length() <= max ? t : t.substring(0, max) + "…";
    }
}
