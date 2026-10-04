package com.jiancha.ai.config;

import com.jiancha.ai.provider.impl.OllamaProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.ollama.OllamaChatModel;

/**
 * Spring AI Bean 配置
 *
 * <p><b>关键配置</b>：</p>
 * <ul>
 *   <li>默认（云端）：注入 OpenAI 兼容 ChatModel（DeepSeek 等）</li>
 *   <li>本地私有化：jiancha.ai.provider=ollama 时启用 Ollama 独立客户端</li>
 * </ul>
 *
 * <p><b>温度设定</b>：0.2 —— 纪检场景要严谨，不要创造性输出。</p>
 *
 * @author 小标
 */
@Slf4j
@Configuration
public class AiChatConfig {

    /**
     * 默认 ChatClient（云端 OpenAI 兼容端点）
     *
     * <p>DeepSeek 兼容 OpenAI 协议，只需改 spring.ai.openai.base-url，无需专用 starter。</p>
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "jiancha.ai.provider", havingValue = "openai", matchIfMissing = true)
    public ChatClient cloudChatClient(ChatModel chatModel,
                                     JianchaAiProperties properties) {
        log.info("初始化云端 ChatClient，供应商=openai，强制带出处={}", properties.isEnableCitation());
        return ChatClient.builder(chatModel).build();
    }

    /**
     * 本地私有化 ChatClient（Ollama）
     *
     * <p>仅当 jiancha.ai.provider=ollama 时启用，与云端客户端完全隔离，
     * 避免两个端点互相串味。</p>
     */
    @Bean("ollamaChatClientBuilder")
    @ConditionalOnProperty(name = "jiancha.ai.provider", havingValue = "ollama")
    public ChatClient.Builder ollamaChatClientBuilder(OllamaChatModel ollamaChatModel) {
        log.info("初始化本地私有化 ChatClient，供应商=ollama（数据不出内网）");
        return ChatClient.builder(ollamaChatModel);
    }
}
