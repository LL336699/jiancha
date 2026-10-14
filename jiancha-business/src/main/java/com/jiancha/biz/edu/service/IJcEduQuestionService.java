package com.jiancha.biz.edu.service;

import java.util.List;

import com.jiancha.biz.edu.domain.JcEduQuestion;

/**
 * 廉政教育题库服务（M9）
 *
 * @author 小标
 */
public interface IJcEduQuestionService {

    /** 列表（不返回答案与解析，防泄题） */
    List<JcEduQuestion> selectQuestionList(JcEduQuestion query);

    /** 详情（维护端查看答案与解析） */
    JcEduQuestion selectQuestionById(Long questionId);

    /** 新增（校验题型答案格式） */
    int insertQuestion(JcEduQuestion question);

    /** 修改 */
    int updateQuestion(JcEduQuestion question);

    /** 批量删除 */
    int deleteQuestions(Long[] questionIds);

    /** 统计：题库总量 / 启用量 */
    long countTotal();

    long countEnabled();
}
