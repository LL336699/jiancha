package com.jiancha.ai.dto;

import java.io.Serializable;
import java.util.List;

/**
 * AI 对话响应对象
 *
 * @author 小标
 */
public class ChatResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 模型回答 */
    private String answer;

    /** 引用出处列表（前端必须展示） */
    private List<Citation> citations;

    /** 实际使用的模型 */
    private String modelName;

    /** 供应商标识 */
    private String provider;

    /** 消耗 token 数（成本核算） */
    private Integer tokenUsed;

    /** 耗时（毫秒） */
    private Long elapsedMs;

    /**
     * 是否需要人工复核
     *
     * <p>纪检场景铁律：涉及定性与量纪的输出一律为true，
     * 前端需加水印"仅供参考，须人工复核"。</p>
     */
    private Boolean needManualReview = Boolean.FALSE;

    /** 免责声明 */
    private String disclaimer;

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<Citation> getCitations() {
        return citations;
    }

    public void setCitations(List<Citation> citations) {
        this.citations = citations;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Integer getTokenUsed() {
        return tokenUsed;
    }

    public void setTokenUsed(Integer tokenUsed) {
        this.tokenUsed = tokenUsed;
    }

    public Long getElapsedMs() {
        return elapsedMs;
    }

    public void setElapsedMs(Long elapsedMs) {
        this.elapsedMs = elapsedMs;
    }

    public Boolean getNeedManualReview() {
        return needManualReview;
    }

    public void setNeedManualReview(Boolean needManualReview) {
        this.needManualReview = needManualReview;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}
