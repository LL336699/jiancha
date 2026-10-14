package com.jiancha.biz.edu.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jiancha.biz.edu.domain.JcEduPaperQuestion;

/**
 * 试卷-试题关联 Mapper（M9 组卷快照）
 *
 * @author 小标
 */
public interface JcEduPaperQuestionMapper {

    /** 按试卷查关联（含题目快照，按 sort_no 排序） */
    List<JcEduPaperQuestion> selectByPaperId(Long paperId);

    /** 批量落库（组卷时一次性插入） */
    int batchInsert(@Param("list") List<JcEduPaperQuestion> list);

    /** 按试卷逻辑删除（重新组卷前清空旧关联） */
    int deleteByPaperId(Long paperId);

    /** 统计：某试卷题目数 */
    long countByPaperId(Long paperId);
}
