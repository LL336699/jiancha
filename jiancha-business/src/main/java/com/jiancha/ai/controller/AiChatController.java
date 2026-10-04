package com.jiancha.ai.controller;

import com.jiancha.ai.dto.ChatRequest;
import com.jiancha.ai.dto.ChatResponse;
import com.jiancha.ai.service.LlmRouterService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import org.springframework.http.MediaType;

/**
 * AI 助手统一接口（M1 知识库问答 + M6 文书辅助 + M7 审理辅助）
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/ai")
public class AiChatController extends BaseController {

    @Autowired
    private LlmRouterService llmRouterService;

    /**
     * 探活
     */
    @PostMapping("/health")
    public AjaxResult health() {
        return AjaxResult.success(llmRouterService.available() ? "模型可用" : "模型不可用");
    }

    /**
     * 同步问答
     *
     * <p>权限：jiancha:ai:query</p>
     */
    @Log(title = "AI 助手问答", businessType = BusinessType.OTHER)
    @PreAuthorize("@ss.hasPermi('jiancha:ai:query')")
    @PostMapping("/chat")
    public AjaxResult chat(@RequestBody ChatRequest request) {
        ChatResponse resp = llmRouterService.chat(request);
        return AjaxResult.success(resp);
    }

    /**
     * 流式问答（SSE）
     *
     * <p>权限：jiancha:ai:query</p>
     */
    @PreAuthorize("@ss.hasPermi('jiancha:ai:query')")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatRequest request) {
        return llmRouterService.chatStream(request);
    }
}
