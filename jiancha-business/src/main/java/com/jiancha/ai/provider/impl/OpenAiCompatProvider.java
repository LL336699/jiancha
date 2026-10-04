package com.jiancha.ai.provider.impl;

import com.jiancha.ai.dto.ChatRequest;
import com.jiancha.ai.dto.ChatResponse;
import com.jiancha.ai.prompt.PromptTemplate;
import com.jiancha.ai.provider.LlmProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenAI 兼容协议 Provider
 *
 * <p><b>覆盖范围</b>：DeepSeek / 通义千问 / Kimi / 本地 vLLM / 月之暗面 等所有兼容 OpenAI 协议的端点。
 * 切换供应商只需改配置里的 base-url，无需改代码。</p>
 *
 * <p><b>重要</b>：纪检场景默认走这个实现（自用阶段接云端 DeepSeek，快且便宜）。</p>
 *
 * @author 小标
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiCompatProvider implements LlmProvider {

    private final ChatClient.Builder chatClientBuilder;

    @Override
    public String name() {
        return "openai";
    }

    @Override
    public boolean available() {
        try {
            ChatClient client = chatClientBuilder.build();
            String resp = client.prompt()
                    .user("ping")
                    .call()
                    .content();
            return resp != null && !resp.isEmpty();
        } catch (Exception e) {
            log.warn("OpenAI 兼容端点探活失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        long start = System.currentTimeMillis();
        String prompt = buildPrompt(request);

        String answer = chatClientBuilder.build()
                .prompt()
                .user(prompt)
                .call()
                .content();

        ChatResponse resp = new ChatResponse();
        resp.setAnswer(answer);
        resp.setProvider(name());
        resp.setModelName("openai-compatible");
        resp.setTokenUsed(estimateTokens(prompt) + estimateTokens(answer));
        resp.setElapsedMs(System.currentTimeMillis() - start);
        resp.setCitations(request.getCitations() == null ? new ArrayList<>() : request.getCitations());

        // 纪检铁律：量纪类输出必须人工复核
        if ("量纪建议".equals(request.getBizModule()) || "案件审理".equals(request.getBizModule())) {
            resp.setNeedManualReview(Boolean.TRUE);
            resp.setDisclaimer("本建议由AI辅助生成，仅供参考，须经人工复核后作出认定。");
        }
        return resp;
    }

    @Override
    public Flux<String> chatStream(ChatRequest request) {
        String prompt = buildPrompt(request);
        return chatClientBuilder.build()
                .prompt()
                .user(prompt)
                .stream()
                .content();
    }

    @Override
    public int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        // 中文按 1 字≈1 token，英文按 4 字符≈1 token，粗略估算
        long cjk = text.chars().filter(c -> c >= 0x4E00 && c <= 0x9FFF).count();
        int others = text.length() - (int) cjk;
        return (int) cjk + others / 4;
    }

    /**
     * 根据业务模块选择对应提示词模板
     */
    private String buildPrompt(ChatRequest request) {
        String context = request.getContext() == null ? "（无检索结果）" : request.getContext();
        return switch (request.getBizModule() == null ? "知识库问答" : request.getBizModule()) {
            case "量纪建议" -> PromptTemplate.format(PromptTemplate.QUANTIFY,
                    request.getQuestion(), context);
            case "文档摘要" -> PromptTemplate.format(PromptTemplate.DOC_SUMMARY,
                    context);
            case "文书生成" -> PromptTemplate.format(PromptTemplate.REPORT_TEMPLATE,
                    request.getExtra(), context);
            case "案例匹配" -> PromptTemplate.format(PromptTemplate.CASE_MATCH,
                    request.getQuestion(), context);
            default -> PromptTemplate.format(PromptTemplate.RAG_QA, context, request.getQuestion());
        };
    }

    /**
     * 兜底提示：检索无果时不调模型
     */
    public static final String NO_KNOWLEDGE_ANSWER = "知识库中无相关规定支撑。";
}
