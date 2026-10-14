package com.jiancha.biz.rectify.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 整改复核/回头看记录 jc_rectify_review
 *
 * <p>两类记录：销号复核（review_type=1，通过则销号，不通过退回整改）；
 * 回头看（review_type=2，销号后抽查，不通过/复发则重开问题）。全程留痕。</p>
 *
 * @author 小标
 */
public class JcRectifyReview extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long reviewId;

    /** 问题ID */
    private Long issueId;

    /** 记录类型（1销号复核 2回头看） */
    private String reviewType;

    /** 结论（0不通过 1通过） */
    private String result;

    /** 复核意见 */
    private String opinion;

    /** 复核人 */
    private String reviewer;

    /** 复核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reviewTime;

    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public String getReviewType() {
        return reviewType;
    }

    public void setReviewType(String reviewType) {
        this.reviewType = reviewType;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    public Date getReviewTime() {
        return reviewTime;
    }

    public void setReviewTime(Date reviewTime) {
        this.reviewTime = reviewTime;
    }
}
