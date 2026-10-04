package com.jiancha.ai.dto;

import java.util.List;

/**
 * AI 对话请求对象
 *
 * @author 小标
 */
public class ChatRequest {

    /** 用户问题 */
    private String question;

    /** 会话ID，用于多轮记忆 */
    private String sessionId;

    /** 业务模块（知识库问答 / 量纪建议 / 文书生成 / 文档摘要） */
    private String bizModule;

    /** RAG 检索到的上下文（法规原文片段） */
    private String context;

    /** 引用出处列表，回答必须携带 */
    private List<Citation> citations;

    /** 附加参数（如 schema、模板ID） */
    private String extra;

    /** 是否强制带出处（纪检场景刚性要求） */
    private Boolean requireCitation = true;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getBizModule() {
        return bizModule;
    }

    public void setBizModule(String bizModule) {
        this.bizModule = bizModule;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public List<Citation> getCitations() {
        return citations;
    }

    public void setCitations(List<Citation> citations) {
        this.citations = citations;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public Boolean getRequireCitation() {
        return requireCitation;
    }

    public void setRequireCitation(Boolean requireCitation) {
        this.requireCitation = requireCitation;
    }
}
