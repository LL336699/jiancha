package com.jiancha.biz.knowledge.service;

import java.util.List;
import java.util.Map;

/**
 * 知识库问答 Service（M1）
 *
 * <p><b>纪检场景刚性约束</b>（设计文档 M1 设计要点）：</p>
 * <ol>
 *   <li>回答<b>必须带出处</b>（文件名 + 条款号 / 页码）；无出处的结论不做展示</li>
 *   <li>检索不到时明确回答"知识库中无相关规定支撑"，<b>禁止推测</b>，且不调用 LLM</li>
 *   <li>失效法规（expire_date 早于今天）在检索阶段强制排除</li>
 *   <li>量纪建议输出必须带"仅供参考，须人工复核"免责声明</li>
 * </ol>
 *
 * @author 小标
 */
public interface IJcKbChatService {

    /**
     * 知识库问答（非流式）。
     *
     * @param question   用户问题
     * @param categoryId 限定分类（null 为全库）
     * @param sessionId  会话ID（多轮对话记忆用）
     * @return 含 answer / citations / retrieved 的结构化结果
     */
    Map<String, Object> ask(String question, Long categoryId, String sessionId);

    /**
     * 知识库问答（流式，SSE）。
     *
     * @param question   用户问题
     * @param categoryId 限定分类
     * @param sessionId  会话ID
     * @param onChunk    增量回调
     * @param onCitations 首包回调（先推送出处再推送正文）
     */
    void askStream(String question, Long categoryId, String sessionId,
                   java.util.function.Consumer<String> onChunk,
                   java.util.function.Consumer<List<Map<String, Object>>> onCitations);

    /**
     * 纯检索（不调 LLM），用于前端"检索预览"与分块质量核查。
     */
    List<Map<String, Object>> search(String query, Long categoryId);

    /** 清空指定会话的记忆 */
    void clearSession(String sessionId);

    /** 当前 AI 底座是否可用（LLM 是否配置了有效 key） */
    Map<String, Object> aiStatus();
}
