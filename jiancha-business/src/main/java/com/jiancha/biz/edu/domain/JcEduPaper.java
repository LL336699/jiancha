package com.jiancha.biz.edu.domain;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 廉政教育试卷 jc_edu_paper（M9 廉政教育题库）
 *
 * <p>组卷方式（字典 jc_edu_paper_mode）：手工选题 fixed / 随机抽题 random。
 * 随机抽题按 category + qtype + count 策略从题库抽取启用状态试题，
 * 组卷快照固化到 jc_edu_paper_question（试卷发布后不受题库改动影响）。</p>
 *
 * @author 小标
 */
public class JcEduPaper extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 试卷ID */
    private Long paperId;

    /** 试卷编号（唯一，如 PA20260001） */
    private String paperCode;

    /** 试卷名称 */
    @NotBlank(message = "试卷名称不能为空")
    private String paperName;

    /** 组卷方式（字典 jc_edu_paper_mode：fixed 手工 / random 随机） */
    private String paperMode;

    /** 总分（手工组卷按所选题目分值汇总；随机组卷按策略估算） */
    private Integer totalScore;

    /** 及格分 */
    @NotNull(message = "及格分不能为空")
    @Min(value = 1, message = "及格分必须大于0")
    private Integer passScore;

    /** 考试时长（分钟） */
    @NotNull(message = "考试时长不能为空")
    @Min(value = 1, message = "考试时长必须大于0")
    private Integer durationMin;

    /** 状态（字典 jc_edu_paper_status：草稿/已发布/已关闭；仅已发布可考） */
    private String status;

    /** 发布时间 */
    private Date publishTime;

    /** 试卷-试题关联（非持久化，组卷时一次性提交，服务层落库） */
    private List<JcEduPaperQuestion> questions;

    public Long getPaperId() {
        return paperId;
    }

    public void setPaperId(Long paperId) {
        this.paperId = paperId;
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

    public String getPaperMode() {
        return paperMode;
    }

    public void setPaperMode(String paperMode) {
        this.paperMode = paperMode;
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

    public Integer getDurationMin() {
        return durationMin;
    }

    public void setDurationMin(Integer durationMin) {
        this.durationMin = durationMin;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    public List<JcEduPaperQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<JcEduPaperQuestion> questions) {
        this.questions = questions;
    }
}
