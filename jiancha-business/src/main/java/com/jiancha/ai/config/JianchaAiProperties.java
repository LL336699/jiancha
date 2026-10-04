package com.jiancha.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 纪检助手 AI 配置属性
 *
 * <p>配置前缀：jiancha.ai</p>
 *
 * @author 小标
 */
@Component
@ConfigurationProperties(prefix = "jiancha.ai")
public class JianchaAiProperties {

    /**
     * 供应商标识：openai（云端）| ollama（本地私有化）
     */
    private String provider = "openai";

    /**
     * 是否强制带出处（纪检刚性要求，原则上不关）
     */
    private boolean enableCitation = true;

    /**
     * 是否记录 AI 调用日志（审计与成本核算）
     */
    private boolean enableAuditLog = true;

    /**
     * RAG 相似度阈值，低于此值不调用模型，直接返回"无相关规定支撑"
     */
    private double ragSimilarityThreshold = 3.0;

    /**
     * RAG 召回数量
     */
    private int ragTopK = 8;

    /**
     * 流式响应超时（秒）
     */
    private int streamTimeout = 120;

    /**
     * 单次上传最大尺寸（MB）
     */
    private int maxUploadSizeMb = 50;

    /**
     * 文档解析超时（秒），超限标记为需人工处理
     */
    private int parseTimeoutSeconds = 30;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public boolean isEnableCitation() {
        return enableCitation;
    }

    public void setEnableCitation(boolean enableCitation) {
        this.enableCitation = enableCitation;
    }

    public boolean isEnableAuditLog() {
        return enableAuditLog;
    }

    public void setEnableAuditLog(boolean enableAuditLog) {
        this.enableAuditLog = enableAuditLog;
    }

    public double getRagSimilarityThreshold() {
        return ragSimilarityThreshold;
    }

    public void setRagSimilarityThreshold(double ragSimilarityThreshold) {
        this.ragSimilarityThreshold = ragSimilarityThreshold;
    }

    public int getRagTopK() {
        return ragTopK;
    }

    public void setRagTopK(int ragTopK) {
        this.ragTopK = ragTopK;
    }

    public int getStreamTimeout() {
        return streamTimeout;
    }

    public void setStreamTimeout(int streamTimeout) {
        this.streamTimeout = streamTimeout;
    }

    public int getMaxUploadSizeMb() {
        return maxUploadSizeMb;
    }

    public void setMaxUploadSizeMb(int maxUploadSizeMb) {
        this.maxUploadSizeMb = maxUploadSizeMb;
    }

    public int getParseTimeoutSeconds() {
        return parseTimeoutSeconds;
    }

    public void setParseTimeoutSeconds(int parseTimeoutSeconds) {
        this.parseTimeoutSeconds = parseTimeoutSeconds;
    }
}
