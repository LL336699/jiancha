package com.jiancha.biz.knowledge.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiancha.biz.knowledge.service.IJcKbChatService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * 知识库问答 Controller（M1）
 *
 * <p>权限前缀 jiancha:kb:chat:*。</p>
 *
 * <p><b>SSE 协议约定</b>：事件名 {@code citations} 携带出处列表（先发），
 * 事件名 {@code message} 携带增量正文，{@code done} 标志结束，{@code error} 携带异常。</p>
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/kb/chat")
public class JcKbChatController extends BaseController {

    @Autowired
    private IJcKbChatService chatService;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** AI 底座状态（LLM 供应商 / 是否就绪 / 向量库是否可用） */
    @PreAuthorize("@ss.hasPermi('jiancha:kb:chat:query')")
    @GetMapping("/status")
    public AjaxResult status() {
        return success(chatService.aiStatus());
    }

    /** 非流式问答 */
    @PreAuthorize("@ss.hasPermi('jiancha:kb:chat:query')")
    @PostMapping("/ask")
    public AjaxResult ask(@RequestBody Map<String, Object> body) {
        String question = str(body.get("question"));
        Long categoryId = lng(body.get("categoryId"));
        String sessionId = str(body.get("sessionId"));
        return success(chatService.ask(question, categoryId, sessionId));
    }

    /** 纯检索（不调 LLM），用于检索预览与分块质量核查 */
    @PreAuthorize("@ss.hasPermi('jiancha:kb:chat:query')")
    @GetMapping("/search")
    public AjaxResult search(@RequestParam String query,
                             @RequestParam(required = false) Long categoryId) {
        List<Map<String, Object>> hits = chatService.search(query, categoryId);
        return success(hits);
    }

    /** 清空会话记忆 */
    @PreAuthorize("@ss.hasPermi('jiancha:kb:chat:query')")
    @PostMapping("/clear")
    public AjaxResult clear(@RequestParam String sessionId) {
        chatService.clearSession(sessionId);
        return success();
    }

    /**
     * 流式问答（SSE）。
     *
     * <p>用 GET + query 参数以兼容前端 EventSource；出处先于正文推送。</p>
     */
    @PreAuthorize("@ss.hasPermi('jiancha:kb:chat:query')")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam String question,
                             @RequestParam(required = false) Long categoryId,
                             @RequestParam(required = false) String sessionId) {
        SseEmitter emitter = new SseEmitter(180_000L);
        AtomicBoolean closed = new AtomicBoolean(false);

        emitter.onTimeout(() -> closed.set(true));
        emitter.onError(e -> closed.set(true));

        try {
            chatService.askStream(question, categoryId, sessionId,
                    chunk -> {
                        if (closed.get()) {
                            return;
                        }
                        try {
                            emitter.send(SseEmitter.event().name("message").data(chunk));
                        } catch (IOException e) {
                            closed.set(true);
                        }
                    },
                    citations -> {
                        if (closed.get()) {
                            return;
                        }
                        try {
                            emitter.send(SseEmitter.event().name("citations")
                                    .data(MAPPER.writeValueAsString(citations)));
                        } catch (IOException e) {
                            closed.set(true);
                        }
                    });
            if (!closed.get()) {
                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
            }
        } catch (Exception e) {
            try {
                emitter.send(SseEmitter.event().name("error").data(e.getMessage() == null ? "未知错误" : e.getMessage()));
            } catch (IOException ignored) {
                // 连接已断开，忽略
            }
        } finally {
            emitter.complete();
        }
        return emitter;
    }

    private String str(Object o) {
        return o == null ? null : o.toString();
    }

    private Long lng(Object o) {
        if (o == null) {
            return null;
        }
        String s = o.toString().trim();
        return s.isEmpty() ? null : Long.valueOf(s);
    }
}
