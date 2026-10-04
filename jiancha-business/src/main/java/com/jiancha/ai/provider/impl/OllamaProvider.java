package com.jiancha.ai.provider.impl;

import com.jiancha.ai.dto.ChatRequest;
import com.jiancha.ai.dto.ChatResponse;
import com.jiancha.ai.prompt.PromptTemplate;
import com.jiancha.ai.provider.LlmProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;

/**
 * 本地私有化 Provider（Ollama）
 *
 * <p><b>用途</b>：公司部署时启用，数据完全不出内网，满足保密要求。</p>
 *
 * <p><b>注意</b>：此处用独立的 ChatClient.Builder（限定 ollama），
 * 避免与云端 OpenAI 端点混用导致串味。</p>
 *
 * @author 小标
 */
@Slf4j
@Component("ollamaProvider")
@RequiredArgsConstructor
public class OllamaProvider implements LlmProvider {

    private final ChatClient.Builder ollamaChatClientBuilder;

    @Override
    public String name() {
        return "ollama";
    }

    @Override
    public boolean available() {
        try {
            String resp = ollamaChatClientBuilder.build()
                    .prompt().user("ping").call().content();
            return resp != null && !resp.isEmpty();
        } catch (Exception e) {
            log.warn("Ollama 端点探活失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        long start = System.currentTimeMillis();
        String context = request.getContext() == null ? "（无检索结果）" : request.getContext();
        String prompt = switch (request.getBizModule() == null ? "知识库问答" : request.getBizModule()) {
            case "量纪建议" -> PromptTemplate.format(PromptTemplate.QUANTIFY, request.getQuestion(), context);
            case "文档摘要" -> PromptTemplate.format(PromptTemplate.DOC_SUMMARY, context);
            default -> PromptTemplate.format(PromptTemplate.RAG_QA, context, request.getQuestion());
        };

        String answer = ollamaChatClientBuilder.build()
                .prompt().user(prompt).call().content();

        ChatResponse resp = new ChatResponse();
        resp.setAnswer(answer);
        resp.setProvider(name());
        resp.setModelName("ollama-local");
        resp.setTokenUsed(estimateTokens(prompt) + estimateTokens(answer));
        resp.setElapsedMs(System.currentTimeMillis() - start);
        resp.setCitations(new ArrayList<>());
        if ("量纪建议".equals(request.getBizModule())) {
            resp.setNeedManualReview(Boolean.TRUE);
            resp.setDisclaimer("本建议由AI辅助生成，仅供参考，须经人工复核后作出认定。");
        }
        return resp;
    }

    @Override
    public Flux<String> chatStream(ChatRequest request) {
        String context = request.getContext() == null ? "（无检索结果）" : request.getContext();
        String prompt = PromptTemplate.format(PromptTemplate.RAG_QA, context, request.getQuestion());
        return ollamaChatClientBuilder.build()
                .prompt().user(prompt).stream().content();
    }

    @Override
    public int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        long cjk = text.chars().filter(c -> c >= 0x4E00 && c <= 0x9FFF).count();
        return (int) cjk + (text.length() - (int) cjk) / 4;
    }
}
