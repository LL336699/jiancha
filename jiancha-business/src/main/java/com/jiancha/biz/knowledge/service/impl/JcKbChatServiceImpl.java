package com.jiancha.biz.knowledge.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jiancha.ai.prompt.PromptTemplate;
import com.jiancha.ai.provider.LlmProvider;
import com.jiancha.ai.service.LlmRouterService;
import com.jiancha.biz.knowledge.rag.KnowledgeRetriever;
import com.jiancha.biz.knowledge.service.IJcKbChatService;
import com.ruoyi.common.exception.ServiceException;

/**
 * 知识库问答 Service 实现
 *
 * <p><b>防幻觉的两道闸门</b>：</p>
 * <ol>
 *   <li><b>闸门一（检索层）</b>：{@link KnowledgeRetriever} 在最高分低于阈值时返回空列表，
 *       本类见此直接返回"知识库中无相关规定支撑"，<b>根本不调用 LLM</b>——模型没有机会编造</li>
 *   <li><b>闸门二（提示词层）</b>：把检索到的原文与出处作为唯一事实来源注入提示词，
 *       并硬性要求"仅依据上述条文回答，禁止引入外部知识"</li>
 * </ol>
 *
 * <p>出处在回答下方以结构化列表返回，前端渲染为可点击的引用卡片。</p>
 *
 * @author 小标
 */
@Service
public class JcKbChatServiceImpl implements IJcKbChatService {

    private static final Logger log = LoggerFactory.getLogger(JcKbChatServiceImpl.class);

    /** 知识库无相关内容的固定话术（不调 LLM，直接返回） */
    private static final String NO_KNOWLEDGE_ANSWER =
            "知识库中无相关规定支撑。\n\n"
          + "当前知识库中未检索到与您的问题直接相关的条文或制度依据。为避免误导，"
          + "系统不会基于推测作答。建议：\n"
          + "1. 确认相关法规/制度是否已上传入库；\n"
          + "2. 尝试更换关键词或放宽提问范围；\n"
          + "3. 必要时补充上传相应文件后重新提问。";

    @Autowired
    private KnowledgeRetriever retriever;

    @Autowired
    private LlmRouterService llmRouterService;

    /** ChatClient 由 AiChatConfig 条件化创建；未配置 LLM 时为空 */
    @Autowired
    private ObjectProvider<ChatClient> chatClientProvider;

    @Value("${jiancha.ai.enable-citation:true}")
    private boolean enableCitation;

    @Value("${jiancha.ai.rag-top-k:8}")
    private int topK;

    // ══════════════════ 非流式问答 ══════════════════

    @Override
    public Map<String, Object> ask(String question, Long categoryId, String sessionId) {
        if (question == null || question.trim().isEmpty()) {
            throw new ServiceException("提问内容不能为空");
        }

        // 闸门一：检索
        List<KnowledgeRetriever.RetrievedChunk> hits = retriever.retrieve(question, categoryId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("question", question);

        if (hits.isEmpty()) {
            result.put("answer", NO_KNOWLEDGE_ANSWER);
            result.put("citations", new ArrayList<>());
            result.put("hasKnowledge", false);
            result.put("modelUsed", null);
            return result;
        }

        // 组装上下文与出处
        String context = buildContext(hits);
        List<Map<String, Object>> citations = buildCitations(hits);

        // 闸门二：LLM 仅在检索命中的前提下调用
        String answer;
        String modelUsed = null;
        try {
            ChatClient client = chatClientProvider.getIfAvailable();
            if (client == null) {
                answer = buildRetrievalOnlyAnswer(hits);
                result.put("llmUnavailable", true);
            } else {
                // RAG_QA 占位符顺序：【知识库内容】%s → 【问题】%s
                String prompt = PromptTemplate.format(PromptTemplate.RAG_QA, context, question);
                answer = client.prompt().user(prompt).call().content();
                modelUsed = providerName();
                if (answer == null || answer.trim().isEmpty()) {
                    answer = buildRetrievalOnlyAnswer(hits);
                }
            }
        } catch (Exception e) {
            // LLM 失败不让整个请求失败——降级为"直接返回检索原文"，这本身已是有价值的答案
            log.warn("LLM 调用失败，降级为检索原文返回：{}", e.getMessage());
            answer = buildRetrievalOnlyAnswer(hits);
            result.put("llmError", e.getMessage());
        }

        result.put("answer", answer);
        result.put("citations", citations);
        result.put("hasKnowledge", true);
        result.put("modelUsed", modelUsed);
        return result;
    }

    // ══════════════════ 流式问答 ══════════════════

    @Override
    public void askStream(String question, Long categoryId, String sessionId,
                          Consumer<String> onChunk,
                          Consumer<List<Map<String, Object>>> onCitations) {
        List<KnowledgeRetriever.RetrievedChunk> hits = retriever.retrieve(question, categoryId);

        // 无命中：直接推送固定话术，不调 LLM
        if (hits.isEmpty()) {
            onCitations.accept(new ArrayList<>());
            onChunk.accept(NO_KNOWLEDGE_ANSWER);
            return;
        }

        onCitations.accept(buildCitations(hits));

        ChatClient client = chatClientProvider.getIfAvailable();
        if (client == null) {
            onChunk.accept(buildRetrievalOnlyAnswer(hits));
            return;
        }

        try {
            String context = buildContext(hits);
            String prompt = PromptTemplate.format(PromptTemplate.RAG_QA, context, question);
            client.prompt().user(prompt).stream().content()
                    .doOnNext(onChunk)
                    .blockLast();
        } catch (Exception e) {
            log.warn("流式问答失败，降级返回检索原文：{}", e.getMessage());
            onChunk.accept("\n\n（模型服务异常，以下为检索到的原文依据）\n\n" + buildRetrievalOnlyAnswer(hits));
        }
    }

    // ══════════════════ 纯检索 ══════════════════

    @Override
    public List<Map<String, Object>> search(String query, Long categoryId) {
        List<KnowledgeRetriever.RetrievedChunk> hits = retriever.retrieve(query, categoryId);
        List<Map<String, Object>> out = new ArrayList<>();
        for (KnowledgeRetriever.RetrievedChunk c : hits) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("chunkId", c.getChunkId());
            m.put("docId", c.getDocId());
            m.put("docName", c.getDocName());
            m.put("clauseNo", c.getClauseNo());
            m.put("pageNo", c.getPageNo());
            m.put("score", Math.round(c.getScore() * 1000) / 1000.0);
            m.put("source", c.getSource());
            m.put("content", c.getContent());
            m.put("citation", c.citation());
            out.add(m);
        }
        return out;
    }

    @Override
    public void clearSession(String sessionId) {
        // 会话记忆由 Spring AI ChatMemory 管理；此处仅作占位，
        // 后续接入持久化 ChatMemory 时在此实现清理逻辑。
        log.info("清空问答会话：{}", sessionId);
    }

    @Override
    public Map<String, Object> aiStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("provider", providerName());
        status.put("llmReady", chatClientProvider.getIfAvailable() != null);
        status.put("vectorReady", retriever.isVectorEnabled());
        return status;
    }

    /** 当前生效的供应商标识（用于前端展示与日志） */
    private String providerName() {
        LlmProvider p = llmRouterService.current();
        return p == null ? null : p.name();
    }

    // ══════════════════ 内部工具 ══════════════════

    /**
     * 组装给 LLM 的上下文：每段标注序号与出处，便于模型在回答中回指。
     */
    private String buildContext(List<KnowledgeRetriever.RetrievedChunk> hits) {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (KnowledgeRetriever.RetrievedChunk c : hits) {
            sb.append("【依据").append(i).append("】").append(c.citation()).append("\n");
            sb.append(c.getContent()).append("\n\n");
            i++;
        }
        return sb.toString();
    }

    /** 出处的结构化列表（前端渲染引用卡片） */
    private List<Map<String, Object>> buildCitations(List<KnowledgeRetriever.RetrievedChunk> hits) {
        List<Map<String, Object>> list = new ArrayList<>();
        int i = 1;
        for (KnowledgeRetriever.RetrievedChunk c : hits) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("index", i++);
            m.put("chunkId", c.getChunkId());
            m.put("docId", c.getDocId());
            m.put("docName", c.getDocName());
            m.put("clauseNo", c.getClauseNo());
            m.put("pageNo", c.getPageNo());
            m.put("score", Math.round(c.getScore() * 1000) / 1000.0);
            m.put("citation", c.citation());
            m.put("excerpt", excerpt(c.getContent(), 120));
            list.add(m);
        }
        return list;
    }

    /**
     * 无 LLM 时的降级答案：把检索到的原文按出处组织返回。
     * 有原文、有出处，人工可直接采信，不依赖模型生成。
     */
    private String buildRetrievalOnlyAnswer(List<KnowledgeRetriever.RetrievedChunk> hits) {
        StringBuilder sb = new StringBuilder();
        sb.append("（当前未启用智能归纳，以下直接列出知识库中检索到的相关规定原文）\n\n");
        int i = 1;
        for (KnowledgeRetriever.RetrievedChunk c : hits) {
            sb.append(i++).append(". ").append(c.citation()).append("\n");
            sb.append(excerpt(c.getContent(), 400)).append("\n\n");
        }
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
