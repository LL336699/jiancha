package com.jiancha.ai.service;

import com.jiancha.ai.config.JianchaAiProperties;
import com.jiancha.ai.dto.ChatRequest;
import com.jiancha.ai.dto.ChatResponse;
import com.jiancha.ai.provider.LlmProvider;
import com.jiancha.ai.provider.impl.OllamaProvider;
import com.jiancha.ai.provider.impl.OpenAiCompatProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * LLM 路由服务
 *
 * <p>根据配置 jiancha.ai.provider 决定走云端还是本地私有化，
 * 业务代码只依赖本服务，不直接依赖具体 Provider。</p>
 *
 * @author 小标
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmRouterService {

    private final JianchaAiProperties properties;
    private final OpenAiCompatProvider openAiCompatProvider;
    private final OllamaProvider ollamaProvider;

    /**
     * 获取当前生效的 Provider
     *
     * <p>路由表在方法内构建——字段初始化时构造器注入的引用尚未赋值，
     * 直接在字段声明处初始化会拿到 null。</p>
     */
    public LlmProvider current() {
        Map<String, LlmProvider> routes = new HashMap<>(4);
        routes.put("openai", openAiCompatProvider);
        routes.put("ollama", ollamaProvider);

        String name = properties.getProvider();
        LlmProvider provider = routes.get(name);
        if (provider == null) {
            log.warn("未识别的LLM 供应商 [{}]，回退到 openai", name);
            return openAiCompatProvider;
        }
        return provider;
    }

    /**
     * 同步对话（自动路由）
     */
    public ChatResponse chat(ChatRequest request) {
        return current().chat(request);
    }

    /**
     * 流式对话（自动路由）
     */
    public Flux<String> chatStream(ChatRequest request) {
        return current().chatStream(request);
    }

    /**
     * 探活
     */
    public boolean available() {
        return current().available();
    }
}
