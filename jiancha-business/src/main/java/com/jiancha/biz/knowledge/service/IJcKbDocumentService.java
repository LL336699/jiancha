package com.jiancha.biz.knowledge.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.jiancha.biz.knowledge.domain.JcKbChunk;
import com.jiancha.biz.knowledge.domain.JcKbDocument;

/**
 * 知识库文档 Service
 *
 * <p>负责文档的元数据管理与入库编排：上传 → 哈希去重 → Tika 解析 → 结构感知分块
 * → Embedding 向量化 → 写 Chroma + jc_kb_chunk。</p>
 *
 * @author 小标
 */
public interface IJcKbDocumentService {

    /** 查询文档列表 */
    List<JcKbDocument> selectJcKbDocumentList(JcKbDocument document);

    /** 查询文档详情 */
    JcKbDocument selectJcKbDocumentById(Long docId);

    /** 修改文档元数据 */
    int updateJcKbDocument(JcKbDocument document);

    /** 删除文档（联动删除分块与向量） */
    int deleteJcKbDocumentById(Long docId);

    /** 批量删除文档（联动删除分块与向量） */
    int deleteJcKbDocumentByIds(Long[] docIds);

    /**
     * 上传并入库（同步执行解析与向量化）。
     *
     * <p>返回结果 map：
     * <ul>
     *   <li>{@code duplicated=true} —— 命中文件哈希，已跳过入库，附 docId</li>
     *   <li>{@code docId} —— 文档ID</li>
     *   <li>{@code chunkCount} —— 分块数</li>
     *   <li>{@code status} —— 最终状态</li>
     *   <li>{@code parseError} —— 失败原因（如有）</li>
     * </ul>
     *
     * @param file     上传文件
     * @param categoryId 归属分类
     * @param doc      附加元数据（发文机关 / 文号 / 生效失效日期等）
     * @param operator 操作人
     */
    Map<String, Object> uploadAndIngest(MultipartFile file, Long categoryId, JcKbDocument doc, String operator);

    /**
     * 重新解析入库（解析失败后重试 / 元数据变更后重建向量）。
     */
    Map<String, Object> reingest(Long docId, String operator);

    /** 查看文档分块（前端核对分块质量） */
    List<JcKbChunk> selectChunksByDocId(Long docId);

    /** 知识库概览统计 */
    Map<String, Object> statOverview();
}
