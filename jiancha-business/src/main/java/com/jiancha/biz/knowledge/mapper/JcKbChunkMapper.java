package com.jiancha.biz.knowledge.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jiancha.biz.knowledge.domain.JcKbChunk;

/**
 * 知识库分块 Mapper
 *
 * @author 小标
 */
public interface JcKbChunkMapper {

    /** 查询文档的全部分块（按块序号） */
    List<JcKbChunk> selectChunksByDocId(Long docId);

    /** 查询分块详情（含文档名称，回填出处用） */
    JcKbChunk selectChunkById(Long chunkId);

    /** 按分块ID批量查询（向量检索命中后回填正文与出处） */
    List<JcKbChunk> selectChunksByIds(@Param("chunkIds") List<Long> chunkIds);

    /**
     * 关键词检索（与向量检索合并去重）。
     *
     * <p>由 {@code keywords} 做 OR 匹配，命中词数越多排序越靠前。</p>
     *
     * @param keywords   关键词列表（由 KnowledgeRetriever 从问题中提取的 n-gram）
     * @param categoryId 限定分类，null 表示全库
     * @param limit      召回条数
     */
    List<JcKbChunk> searchByKeywords(@Param("keywords") List<String> keywords,
                                     @Param("categoryId") Long categoryId,
                                     @Param("limit") int limit);

    /** 批量新增分块 */
    int batchInsertChunks(@Param("list") List<JcKbChunk> list);

    /** 删除文档的全部分块（逻辑删除） */
    int deleteChunksByDocId(Long docId);

    /** 物理删除文档的全部分块（文档彻底清除时） */
    int physicalDeleteByDocId(Long docId);

    /** 统计分块总数 */
    int countAll();
}
