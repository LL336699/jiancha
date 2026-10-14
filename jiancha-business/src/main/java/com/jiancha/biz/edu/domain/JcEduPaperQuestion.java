package com.jiancha.biz.edu.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 试卷-试题关联 jc_edu_paper_question（M9 廉政教育题库）
 *
 * <p>组卷快照：试卷发布时固化题目、题型、分值，后续题库改题/停用不影响已发布试卷。</p>
 *
 * @author 小标
 */
public class JcEduPaperQuestion extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 关联ID */
    private Long id;

    /** 试卷ID */
    private Long paperId;

    /** 题目ID */
    private Long questionId;

    /** 题目快照标题（发布时固化） */
    private String titleSnapshot;

    /** 题型快照 */
    private String qtype;

    /** 分值快照 */
    private Integer score;

    /** 排序号 */
    private Integer sortNo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPaperId() {
        return paperId;
    }

    public void setPaperId(Long paperId) {
        this.paperId = paperId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getTitleSnapshot() {
        return titleSnapshot;
    }

    public void setTitleSnapshot(String titleSnapshot) {
        this.titleSnapshot = titleSnapshot;
    }

    public String getQtype() {
        return qtype;
    }

    public void setQtype(String qtype) {
        this.qtype = qtype;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }
}
