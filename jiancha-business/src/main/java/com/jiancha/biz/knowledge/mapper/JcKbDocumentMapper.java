package com.jiancha.biz.knowledge.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jiancha.biz.knowledge.domain.JcKbDocument;

/**
 * 知识库文档 Mapper
 *
 * @author 小标
 */
public interface JcKbDocumentMapper {

    /** 查询文档列表（含分类名称） */
    List<JcKbDocument> selectJcKbDocumentList(JcKbDocument document);

    /** 查询文档详情 */
    JcKbDocument selectJcKbDocumentById(Long docId);

    /** 按文件哈希查询（上传去重） */
    JcKbDocument selectByFileHash(@Param("fileHash") String fileHash);

    /** 新增文档 */
    int insertJcKbDocument(JcKbDocument document);

    /** 修改文档 */
    int updateJcKbDocument(JcKbDocument document);

    /** 更新处理状态与分块数 */
    int updateStatus(@Param("docId") Long docId,
                     @Param("status") String status,
                     @Param("chunkCount") Integer chunkCount,
                     @Param("parseError") String parseError);

    /** 删除文档（逻辑删除） */
    int deleteJcKbDocumentById(Long docId);

    /** 批量删除文档（逻辑删除） */
    int deleteJcKbDocumentByIds(Long[] docIds);

    /** 统计各状态文档数 */
    int countByStatus(@Param("status") String status);

    /** 文档总数 */
    int countAll();
}
