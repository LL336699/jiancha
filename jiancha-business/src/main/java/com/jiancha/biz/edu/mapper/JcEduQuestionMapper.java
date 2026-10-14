package com.jiancha.biz.edu.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jiancha.biz.edu.domain.JcEduQuestion;

/**
 * 题库 Mapper（M9）
 *
 * @author 小标
 */
public interface JcEduQuestionMapper {

    /** 列表（不含 answer/analysis 列，防泄题） */
    List<JcEduQuestion> selectJcEduQuestionList(JcEduQuestion jcEduQuestion);

    /** 详情（含答案与解析，维护端用） */
    JcEduQuestion selectJcEduQuestionByQuestionId(Long questionId);

    /** 按 ID 批量查询（随机组卷抽取用） */
    List<JcEduQuestion> selectJcEduQuestionByIds(@Param("ids") List<Long> ids);

    /** 按分类+题型+数量随机抽取启用题目（题库充足时随机顺序） */
    List<JcEduQuestion> selectRandomByRule(@Param("category") String category,
                                           @Param("qtype") String qtype,
                                           @Param("limit") int limit);

    /** 题目引用计数 +1（组卷发布时） */
    int increaseUseCount(Long questionId);

    /** 新增 */
    int insertJcEduQuestion(JcEduQuestion jcEduQuestion);

    /** 修改 */
    int updateJcEduQuestion(JcEduQuestion jcEduQuestion);

    /** 批量删除 */
    int deleteJcEduQuestionByQuestionIds(Long[] questionIds);

    /** 统计：题目总数 */
    long countTotal();

    /** 统计：启用题目数 */
    long countEnabled();
}
