package com.jiancha.biz.ops.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;

import com.jiancha.biz.knowledge.domain.JcKbDocument;
import com.jiancha.biz.knowledge.service.IJcKbDocumentService;
import com.jiancha.biz.petition.domain.JcPetition;
import com.jiancha.biz.petition.service.IJcPetitionService;
import com.jiancha.biz.rectify.domain.JcRectifyIssue;
import com.jiancha.biz.rectify.service.IJcRectifyService;

/**
 * 系统健康巡检 服务（段17 全局打磨）
 *
 * <p>面向运维的一键自检：数据层（MySQL 连通 + 核心业务表行数）、缓存层（Redis 连通）、
 * AI 能力（对话供应商 / Embedding 模式 / 向量库开关）、定时任务（启用与暂停分布）、
 * 业务待办摘要（超期举报 / 临期整改 / 解析失败文档）。
 * 巡检为只读探测，任何单项失败只降级为该项「异常 + 原因摘要」，不影响整体响应。</p>
 *
 * @author 小标
 */
@Service
public class SystemHealthService {

    private static final Logger log = LoggerFactory.getLogger(SystemHealthService.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired
    private IJcPetitionService petitionService;

    @Autowired
    private IJcRectifyService rectifyService;

    @Autowired
    private IJcKbDocumentService kbDocumentService;

    /** AI 对话供应商（openai / ollama 兼容端点复用 openai 配置） */
    @Value("${spring.ai.openai.api-key:}")
    private String aiApiKey;

    @Value("${spring.ai.openai.base-url:}")
    private String aiBaseUrl;

    @Value("${spring.ai.model.embedding:none}")
    private String embeddingMode;

    @Value("${spring.ai.vectorstore.chroma.enabled:false}")
    private boolean chromaEnabled;

    /**
     * 执行全量巡检，返回分组检查项与待办摘要。
     */
    public Map<String, Object> checkAll() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("groups", List.of(
                checkDatabase(),
                checkRedis(),
                checkAi(),
                checkJobs(),
                checkTodos()));
        result.put("checkTime", new java.util.Date());
        return result;
    }

    /**
     * 数据层：连通性 + 核心业务表行数（告知部署规模）。
     */
    private Map<String, Object> checkDatabase() {
        Map<String, Object> group = baseGroup("数据层", "MySQL");
        try (Connection conn = dataSource.getConnection()) {
            group.put("status", "正常");
            Map<String, Object> tables = new LinkedHashMap<>();
            for (String t : List.of("jc_person", "jc_clue", "jc_petition", "jc_rectify_issue",
                    "jc_supplier_info", "jc_kb_document", "jc_notify_message", "jc_conflict_declare")) {
                tables.put(t, safeCount(conn, t));
            }
            group.put("detail", tables);
        } catch (Exception e) {
            group.put("status", "异常");
            group.put("error", brief(e));
            log.warn("[系统巡检] 数据层检查失败", e);
        }
        return group;
    }

    /**
     * 缓存层：Redis 连通（登录态 / 验证码 / 限流器依赖）。
     */
    private Map<String, Object> checkRedis() {
        Map<String, Object> group = baseGroup("缓存层", "Redis");
        try {
            Long dbSize = redisConnectionFactory.getConnection().serverCommands().dbSize();
            group.put("status", "正常");
            group.put("detail", Map.of("dbSize", dbSize == null ? 0 : dbSize));
        } catch (Exception e) {
            group.put("status", "异常");
            group.put("error", brief(e));
            log.warn("[系统巡检] Redis 检查失败", e);
        }
        return group;
    }

    /**
     * AI 能力：对话端点配置、Embedding 模式、向量库开关（与 KnowledgeRetriever 降级策略对齐）。
     */
    private Map<String, Object> checkAi() {
        Map<String, Object> group = baseGroup("AI 能力", "对话/检索");
        boolean keyConfigured = aiApiKey != null && !aiApiKey.isBlank() && !aiApiKey.startsWith("sk-placeholder");
        String chatMode = keyConfigured
                ? ("OpenAI 兼容端点(" + (aiBaseUrl == null || aiBaseUrl.isBlank() ? "默认端点" : aiBaseUrl) + ")")
                : "未配置（api-key 为占位符，知识库问答不可用）";
        group.put("status", keyConfigured ? "正常" : "未配置");
        group.put("detail", Map.of(
                "chat", chatMode,
                "embedding", embeddingMode + (chromaEnabled ? "（向量库已启用）" : "（关键词检索降级）")));
        return group;
    }

    /**
     * 定时任务：启用 / 暂停分布（直接 JDBC 查询，避免依赖 quartz 模块）。
     */
    private Map<String, Object> checkJobs() {
        Map<String, Object> group = baseGroup("定时任务", "Quartz");
        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT status, COUNT(*) FROM sys_job GROUP BY status")) {
            long running = 0, paused = 0;
            while (rs.next()) {
                if ("0".equals(rs.getString(1))) {
                    running = rs.getLong(2);
                } else {
                    paused += rs.getLong(2);
                }
            }
            group.put("status", "正常");
            group.put("detail", Map.of("running", running, "paused", paused));
        } catch (Exception e) {
            group.put("status", "异常");
            group.put("error", brief(e));
        }
        return group;
    }

    /**
     * 业务待办摘要：超期举报 / 临期与超期整改 / 知识库解析失败。
     */
    private Map<String, Object> checkTodos() {
        Map<String, Object> group = baseGroup("业务待办", "摘要");
        Map<String, Object> detail = new LinkedHashMap<>();
        try {
            List<JcPetition> overdue = petitionService.selectOverdue();
            detail.put("超期未办结举报", overdue == null ? 0 : overdue.size());
        } catch (Exception e) {
            detail.put("超期未办结举报", "查询失败");
        }
        try {
            int due = 0;
            for (String status : List.of("0", "1")) {
                JcRectifyIssue query = new JcRectifyIssue();
                query.setStatus(status);
                for (JcRectifyIssue issue : rectifyService.selectJcRectifyIssueList(query)) {
                    if (issue.getDeadline() != null && issue.getDeadline().before(new java.util.Date())) {
                        due++;
                    }
                }
            }
            detail.put("超期整改问题", due);
        } catch (Exception e) {
            detail.put("超期整改问题", "查询失败");
        }
        try {
            JcKbDocument query = new JcKbDocument();
            query.setStatus("解析失败");
            List<JcKbDocument> failed = kbDocumentService.selectJcKbDocumentList(query);
            detail.put("知识库解析失败文档", failed == null ? 0 : failed.size());
        } catch (Exception e) {
            detail.put("知识库解析失败文档", "查询失败");
        }
        group.put("status", "正常");
        group.put("detail", detail);
        return group;
    }

    private Map<String, Object> baseGroup(String name, String target) {
        Map<String, Object> g = new LinkedHashMap<>();
        g.put("name", name);
        g.put("target", target);
        return g;
    }

    private long safeCount(Connection conn, String table) {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (Exception e) {
            return -1;
        }
    }

    private String brief(Exception e) {
        String msg = e.getMessage();
        if (msg == null) {
            return e.getClass().getSimpleName();
        }
        return msg.length() > 120 ? msg.substring(0, 120) + "..." : msg;
    }
}
