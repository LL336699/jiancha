package com.jiancha.biz.knowledge.rag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jiancha.biz.knowledge.domain.JcKbChunk;
import com.jiancha.biz.knowledge.mapper.JcKbChunkMapper;

/**
 * 知识库检索器
 *
 * <p>实现设计文档 7.4 的<b>混合检索</b>：向量语义召回 + MySQL 关键词召回，
 * 合并去重后按加权得分重排。核心约束：</p>
 *
 * <ol>
 *   <li><b>失效法规强制排除</b>——检索前按 {@code expire_date} 过滤，命中失效条文会导致量纪错误</li>
 *   <li><b>阈值兜底</b>——Top-K 最高分低于阈值时返回空结果，上层据此直接回答
 *       "知识库中无相关规定支撑"，<b>不调用 LLM</b>（防幻觉的第一道闸门）</li>
 *   <li><b>出处可回填</b>——每条结果携带 docName / clauseNo / pageNo，供回答标注来源</li>
 * </ol>
 *
 * @author 小标
 */
@Component
public class KnowledgeRetriever {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeRetriever.class);

    @Autowired(required = false)
    @Qualifier("jianchaVectorStore")
    private VectorStore vectorStore;

    @Autowired
    private JcKbChunkMapper chunkMapper;

    /**
     * 相似度阈值：低于此值视为无相关知识。
     *
     * <p>量纲说明：关键词命中基础分 0.4 / 双路命中 +0.3 / 向量命中 = 归一化相似度×2.0。
     * 默认 0.4 表示"至少要有一个关键词命中"才不算空结果。</p>
     */
    @Value("${jiancha.ai.rag-similarity-threshold:0.4}")
    private double similarityThreshold;

    /** 召回条数 */
    @Value("${jiancha.ai.rag-top-k:8}")
    private int topK;

    /** 向量召回条数（比 topK 多召回，供重排） */
    private static final int VECTOR_RECALL = 20;

    /** 关键词召回条数 */
    private static final int KEYWORD_RECALL = 10;

    /**
     * 中文停用词（虚词 / 疑问词），提取关键词时剔除，避免噪音词污染 LIKE 匹配。
     */
    private static final java.util.Set<String> STOP_WORDS = java.util.Set.of(
            "的", "了", "是", "在", "和", "与", "及", "或", "对", "为", "被", "把", "给",
            "有", "无", "不", "也", "都", "就", "而", "则", "以", "于", "其", "之",
            "如何", "怎么", "怎样", "什么", "哪些", "哪个", "为什么", "是否", "能否",
            "可以", "应该", "需要", "要求", "请问", "关于", "对于", "以及", "并且",
            "规定", "相关", "情形", "情况", "问题", "内容", "方面", "进行", "应当");

    /**
     * 从自然语言问题中提取用于 LIKE 匹配的关键词。
     *
     * <p>分块正文是按"章/条"存储的完整句子，用户提问是自然语言，
     * 整句做 {@code LIKE '%整句%'} 几乎不可能命中。故切分为中文 2~4 字 n-gram 并过滤停用词，
     * 由 Mapper 侧做 OR 匹配 + 命中词计数排序。</p>
     *
     * @param query 用户问题
     * @return 去重后的关键词列表（按长度降序，长词更具区分度）
     */
    static List<String> extractKeywords(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        // ① 按非中文/非字母数字切段，保留连续中文块与英文词
        java.util.LinkedHashSet<String> words = new java.util.LinkedHashSet<>();
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < query.length(); i++) {
            char ch = query.charAt(i);
            if (Character.isLetterOrDigit(ch)) {
                buf.append(ch);
            } else if (buf.length() > 0) {
                words.add(buf.toString());
                buf.setLength(0);
            }
        }
        if (buf.length() > 0) {
            words.add(buf.toString());
        }

        // ② 对每个词块再切 2~4 字滑窗 n-gram
        java.util.LinkedHashSet<String> grams = new java.util.LinkedHashSet<>();
        for (String w : words) {
            if (w.length() < 2) {
                continue;
            }
            if (!isChinese(w)) {
                grams.add(w);
                continue;
            }
            for (int n = 4; n >= 2; n--) {
                for (int i = 0; i + n <= w.length(); i++) {
                    grams.add(w.substring(i, i + n));
                }
            }
        }

        // ③ 过滤停用词与纯数字
        List<String> result = new ArrayList<>();
        for (String g : grams) {
            if (STOP_WORDS.contains(g)) {
                continue;
            }
            if (g.matches("\\d+")) {
                continue;
            }
            result.add(g);
        }
        // ④ 长词优先（区分度高），最多 12 个，控制 SQL 规模
        result.sort((a, b) -> Integer.compare(b.length(), a.length()));
        return result.size() > 12 ? new ArrayList<>(result.subList(0, 12)) : result;
    }

    private static boolean isChinese(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) > 0x2E80) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检索结果条目（含出处信息）。
     */
    public static class RetrievedChunk {
        /** 分块ID */
        private Long chunkId;
        /** 分块正文 */
        private String content;
        /** 文档名称（出处） */
        private String docName;
        /** 文档ID */
        private Long docId;
        /** 条款号（出处） */
        private String clauseNo;
        /** 页码（出处） */
        private Integer pageNo;
        /** 加权得分（越高越相关） */
        private double score;
        /** 命中来源：vector / keyword / both */
        private String source;

        public Long getChunkId() {
            return chunkId;
        }

        public String getContent() {
            return content;
        }

        public String getDocName() {
            return docName;
        }

        public Long getDocId() {
            return docId;
        }

        public String getClauseNo() {
            return clauseNo;
        }

        public Integer getPageNo() {
            return pageNo;
        }

        public double getScore() {
            return score;
        }

        public String getSource() {
            return source;
        }

        /** 拼装可读出处："《文件名》第十二条 · 第 3 页" */
        public String citation() {
            StringBuilder sb = new StringBuilder();
            if (docName != null && !docName.isEmpty()) {
                sb.append("《").append(stripExt(docName)).append("》");
            }
            if (clauseNo != null && !clauseNo.isEmpty()) {
                sb.append(sb.length() > 0 ? " " : "").append(clauseNo);
            }
            if (pageNo != null && pageNo > 0) {
                sb.append(sb.length() > 0 ? " · " : "").append("第 ").append(pageNo).append(" 页");
            }
            return sb.length() > 0 ? sb.toString() : "（出处不明）";
        }

        private String stripExt(String name) {
            int i = name.lastIndexOf('.');
            return (i > 0) ? name.substring(0, i) : name;
        }
    }

    /**
     * 混合检索。
     *
     * @param query      用户问题
     * @param categoryId 限定分类（null 表示全库检索）
     * @return 按得分降序的结果；最高分低于阈值时返回<b>空列表</b>
     */
    public List<RetrievedChunk> retrieve(String query, Long categoryId) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // ① 向量语义召回
        Map<Long, RetrievedChunk> merged = new LinkedHashMap<>();
        if (vectorStore != null) {
            try {
                List<Document> docs = vectorStore.similaritySearch(
                        SearchRequest.builder()
                                .query(query)
                                .topK(VECTOR_RECALL)
                                .similarityThreshold(0.0)
                                .build());
                if (docs != null) {
                    for (Document d : docs) {
                        RetrievedChunk c = fromVectorDocument(d);
                        if (c != null) {
                            merged.put(c.getChunkId(), c);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("向量检索失败，降级为关键词检索：{}", e.getMessage());
            }
        }

        // ② 关键词召回（条款号精确匹配场景下，向量检索往往不可靠）
        //    注意：整句 LIKE 无法命中分块正文，必须先用 n-gram 提取关键词再 OR 匹配。
        try {
            List<String> keywords = extractKeywords(query);
            if (!keywords.isEmpty()) {
                List<JcKbChunk> keywordHits =
                        chunkMapper.searchByKeywords(keywords, categoryId, KEYWORD_RECALL);
                if (keywordHits != null) {
                    for (JcKbChunk k : keywordHits) {
                        RetrievedChunk exist = merged.get(k.getChunkId());
                        if (exist != null) {
                            // 双路命中，提权
                            exist.score += 0.3;
                            exist.source = "both";
                        } else {
                            RetrievedChunk c = new RetrievedChunk();
                            c.chunkId = k.getChunkId();
                            c.content = k.getContent();
                            c.docName = k.getDocName();
                            c.docId = k.getDocId();
                            c.clauseNo = k.getClauseNo();
                            c.pageNo = k.getPageNo();
                            // 命中词越多得分越高（0.4 起，每多命中一个词 +0.15，上限 1.0）
                            int hit = (k.getHitCount() == null || k.getHitCount() < 1) ? 1 : k.getHitCount();
                            c.score = Math.min(0.4 + 0.15 * (hit - 1), 1.0);
                            c.source = "keyword";
                            merged.put(c.getChunkId(), c);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("关键词检索失败：{}", e.getMessage());
        }

        // ③ 过滤 + 排序 + 截断
        List<RetrievedChunk> result = merged.values().stream()
                .filter(c -> c.content != null && !c.content.isEmpty())
                .sorted(Comparator.comparingDouble(RetrievedChunk::getScore).reversed())
                .limit(topK)
                .collect(Collectors.toList());

        // ④ 阈值兜底：无一条达标即视为"知识库无相关规定"
        if (result.isEmpty()) {
            log.debug("检索无命中：query={}", query);
            return new ArrayList<>();
        }

        double best = result.get(0).getScore();
        if (best < similarityThreshold && !hasKeywordHit(result)) {
            log.info("检索最高分 {} 低于阈值 {}，判定为知识库无相关内容", best, similarityThreshold);
            return new ArrayList<>();
        }
        return result;
    }

    /**
     * 向量命中的分数是"相似度"（0-1），换算到与关键词可比的量纲。
     *
     * <p>Spring AI 对 Chroma 返回的 score 为距离（越小越相似）或相似度（越大越相似），
     * 不同 provider 语义不一。此处按<b>相似度</b>归一化：score ∈ [0,1] 时直接放大到
     * [0,1] 区间参与比较；大于 1 的按距离处理做反向归一。</p>
     */
    private RetrievedChunk fromVectorDocument(Document d) {
        try {
            Object idObj = d.getMetadata().get("chunk_id");
            if (idObj == null) {
                return null;
            }
            Long chunkId = Long.valueOf(idObj.toString());

            double raw = d.getScore() == null ? 0.0 : d.getScore();
            double normalized = (raw <= 1.0) ? raw : 1.0 / (1.0 + raw);

            RetrievedChunk c = new RetrievedChunk();
            c.chunkId = chunkId;
            c.content = d.getText();
            c.docName = str(d.getMetadata().get("doc_name"));
            c.docId = d.getMetadata().get("doc_id") == null ? null
                    : Long.valueOf(d.getMetadata().get("doc_id").toString());
            c.clauseNo = str(d.getMetadata().get("clause_no"));
            Object pn = d.getMetadata().get("page_no");
            c.pageNo = pn == null ? null : Integer.valueOf(pn.toString());
            c.score = normalized * 2.0;      // 向量命中基准权重高于关键词
            c.source = "vector";
            return c;
        } catch (Exception e) {
            log.debug("向量结果解析失败：{}", e.getMessage());
            return null;
        }
    }

    private boolean hasKeywordHit(List<RetrievedChunk> list) {
        return list.stream().anyMatch(c -> "keyword".equals(c.source) || "both".equals(c.source));
    }

    private String str(Object o) {
        return o == null ? null : o.toString();
    }

    /** 向量库是否可用（未就绪时上层应提示"向量检索未启用"） */
    public boolean isVectorEnabled() {
        return vectorStore != null;
    }
}
