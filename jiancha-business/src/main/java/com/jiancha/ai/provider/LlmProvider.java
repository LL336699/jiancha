package com.jiancha.ai.provider;

import com.jiancha.ai.dto.ChatRequest;
import com.jiancha.ai.dto.ChatResponse;
import reactor.core.publisher.Flux;

/**
 * 大模型统一抽象接口
 *
 * <p><b>设计目标</b>：一套代码，两种部署形态。
 * 自用阶段接DeepSeek 云端 API（快、便宜）；公司阶段切本地 Ollama 私有化模型（数据不出内网）。
 * 切换<b>只改配置，不改代码</b>。</p>
 *
 * <p><b>实现类</b>：</p>
 * <ul>
 *   <li>{@link com.jiancha.ai.provider.impl.OpenAiCompatProvider} —— 覆盖 DeepSeek / 通义 / Kimi / vLLM 等所有 OpenAI 兼容端点</li>
 *   <li>{@link com.jiancha.ai.provider.impl.OllamaProvider} —— 本地私有化</li>
 * </ul>
 *
 * @author 小标
 */
public interface LlmProvider {

    /**
     * 供应商标识
     */
    String name();

    /**
     * 是否就绪（探活）
     *
     * @return true 表示模型可调用
     */
    boolean available();

    /**
     * 同步对话
     *
     * @param request 对话请求
     * @return 对话响应
     */
    ChatResponse chat(ChatRequest request);

    /**
     * 流式对话（前端打字机效果）
     *
     * @param request 对话请求
     * @return 字符流
     */
    Flux<String> chatStream(ChatRequest request);

    /**
     * 估算 token 数（成本核算，日志用）
     *
     * <p>粗略估算：中文按1 字≈1 token，英文按 4 字符≈1 token。</p>
     *
     * @param text 文本
     * @return token 数
     */
    int estimateTokens(String text);
}
