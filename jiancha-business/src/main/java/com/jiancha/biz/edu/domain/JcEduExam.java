package com.jiancha.biz.edu.domain;

import java.util.Date;

import jakarta.validation.constraints.NotNull;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 考试记录 jc_edu_exam（M9 廉政教育题库）
 *
 * <p>提交后服务端按试卷快照客观题自动评分（单选/判断全对得分；多选少选按比例或不得分，
 * 由 jiancha.edu.multi.part 参数控制），得分 >= 及格分即通过；
 * 答错题目自动入错题本（jc_edu_wrong）。</p>
 *
 * @author 小标
 */
public class JcEduExam extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 考试ID */
    private Long examId;

    /** 试卷ID */
    @NotNull(message = "试卷不能为空")
    private Long paperId;

    /** 考生ID（jc_person.person_id，企业口径用人员档案） */
    @NotNull(message = "考生不能为空")
    private Long personId;

    /** 考生姓名（快照） */
    private String personName;

    /** 开始时间 */
    private Date startTime;

    /** 提交时间 */
    private Date submitTime;

    /** 得分 */
    private Integer score;

    /** 是否通过（字典 sys_yes_no） */
    private String passed;

    /** 状态（字典 jc_edu_exam_status：进行中/已交卷/已评分/弃考） */
    private String status;

    /** 试卷编号（非持久化，列表 JOIN jc_edu_paper 展示用） */
    private String paperCode;

    /** 试卷名称（非持久化，列表 JOIN jc_edu_paper 展示用） */
    private String paperName;

    /** 试卷总分（非持久化，列表 JOIN 展示用） */
    private Integer totalScore;

    /** 试卷及格分（非持久化，列表 JOIN 展示用） */
    private Integer passScore;

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public Long getPaperId() {
        return paperId;
    }

    public void setPaperId(Long paperId) {
        this.paperId = paperId;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getPassed() {
        return passed;
    }

    public void setPassed(String passed) {
        this.passed = passed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaperCode() {
        return paperCode;
    }

    public void setPaperCode(String paperCode) {
        this.paperCode = paperCode;
    }

    public String getPaperName() {
        return paperName;
    }

    public void setPaperName(String paperName) {
        this.paperName = paperName;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public Integer getPassScore() {
        return passScore;
    }

    public void setPassScore(Integer passScore) {
        this.passScore = passScore;
    }
}
