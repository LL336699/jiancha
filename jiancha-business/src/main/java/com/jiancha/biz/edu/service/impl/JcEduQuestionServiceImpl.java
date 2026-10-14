package com.jiancha.biz.edu.service;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.jiancha.biz.edu.domain.JcEduQuestion;
import com.jiancha.biz.edu.mapper.JcEduQuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 廉政教育题库服务实现（M9）
 *
 * @author 小标
 */
@Service
public class JcEduQuestionServiceImpl implements IJcEduQuestionService {

    /** 题型（字典 jc_edu_qtype） */
    private static final String QTYPE_SINGLE = "单选";
    private static final String QTYPE_MULTI = "多选";
    private static final String QTYPE_JUDGE = "判断";

    @Autowired
    private JcEduQuestionMapper questionMapper;

    @Override
    public List<JcEduQuestion> selectQuestionList(JcEduQuestion query) {
        return questionMapper.selectJcEduQuestionList(query);
    }

    @Override
    public JcEduQuestion selectQuestionById(Long questionId) {
        return questionMapper.selectJcEduQuestionByQuestionId(questionId);
    }

    @Override
    @Transactional
    public int insertQuestion(JcEduQuestion question) {
        validateAnswer(question);
        question.setCreateBy(SecurityUtils.getUsername());
        question.setStatus(StringUtils.isEmpty(question.getStatus()) ? "0" : question.getStatus());
        return questionMapper.insertJcEduQuestion(question);
    }

    @Override
    @Transactional
    public int updateQuestion(JcEduQuestion question) {
        validateAnswer(question);
        question.setUpdateBy(SecurityUtils.getUsername());
        return questionMapper.updateJcEduQuestion(question);
    }

    @Override
    @Transactional
    public int deleteQuestions(Long[] questionIds) {
        return questionMapper.deleteJcEduQuestionByQuestionIds(questionIds);
    }

    @Override
    public long countTotal() {
        return questionMapper.countTotal();
    }

    @Override
    public long countEnabled() {
        return questionMapper.countEnabled();
    }

    /**
     * 校验答案格式与题型匹配：
     * 单选/判断答案必须是选项字母之一；多选答案为 2-4 个逗号分隔字母（如 "A,B"）。
     */
    private void validateAnswer(JcEduQuestion question) {
        if (StringUtils.isEmpty(question.getTitle())) {
            throw new com.ruoyi.common.exception.ServiceException("题目标题不能为空");
        }
        if (StringUtils.isEmpty(question.getQtype())) {
            throw new com.ruoyi.common.exception.ServiceException("题型不能为空");
        }
        if (StringUtils.isEmpty(question.getAnswer())) {
            throw new com.ruoyi.common.exception.ServiceException("答案不能为空");
        }
        String answer = question.getAnswer().trim().toUpperCase();
        if (QTYPE_MULTI.equals(question.getQtype())) {
            String[] parts = answer.split(",");
            if (parts.length < 2 || parts.length > 4) {
                throw new com.ruoyi.common.exception.ServiceException("多选答案需为 2-4 个逗号分隔字母，如 A,B");
            }
            for (String part : parts) {
                if (!part.matches("[A-F]")) {
                    throw new com.ruoyi.common.exception.ServiceException("多选答案含非法选项：" + part);
                }
            }
        } else if (QTYPE_SINGLE.equals(question.getQtype()) || QTYPE_JUDGE.equals(question.getQtype())) {
            if (!answer.matches("[A-F]")) {
                throw new com.ruoyi.common.exception.ServiceException("单选/判断题答案必须是单个选项字母");
            }
            if (QTYPE_JUDGE.equals(question.getQtype()) && StringUtils.isEmpty(question.getOptions())) {
                question.setOptions("A.正确|B.错误");
            }
        } else {
            throw new com.ruoyi.common.exception.ServiceException("不支持的题型：" + question.getQtype());
        }
    }
}
