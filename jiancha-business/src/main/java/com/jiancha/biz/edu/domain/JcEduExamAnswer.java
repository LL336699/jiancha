package com.jiancha.biz.edu.domain;

import jakarta.validation.constraints.NotNull;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 答题明细 jc_edu_exam_answer（M9 廉政教育题库）
 *
 * <p>逐题留痕：考生答案、是否正确、得分快照；评分后用于成绩复核与错题本生成。</p>
 *
 * @author 小标
 */
public class JcEduExamAnswer extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long answerId;

    /** 考试ID */
    @NotNull(message = "考试ID不能为空")
    private Long examId;

    /** 题目ID */
    @NotNull(message = "题目不能为空")
    private Long questionId;

    /** 考生作答（单选/判断为单个值；多选为逗号分隔，如 "A,C"） */
    private String myAnswer;

    /** 是否正确（字典 sys_yes_no） */
    private String correct;

    /** 本题得分 */
    private Integer score;

    /** 题型（非持久化，JOIN 组卷题目快照展示用） */
    private String qtype;

    /** 题目标题（非持久化，JOIN 组卷题目快照展示用） */
    private String title;

    /** 标准答案（非持久化，错题本展示用） */
    private String answer;

    /** 解析（非持久化，错题本展示用） */
    private String analysis;

    public Long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Long answerId) {
        this.answerId = answerId;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getMyAnswer() {
        return myAnswer;
    }

    public void setMyAnswer(String myAnswer) {
        this.myAnswer = myAnswer;
    }

    public String getCorrect() {
        return correct;
    }

    public void setCorrect(String correct) {
        this.correct = correct;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getQtype() {
        return qtype;
    }

    public void setQtype(String qtype) {
        this.qtype = qtype;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }
}
