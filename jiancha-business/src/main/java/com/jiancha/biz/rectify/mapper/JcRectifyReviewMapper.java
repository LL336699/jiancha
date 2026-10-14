package com.jiancha.biz.rectify.mapper;

import java.util.List;

import com.jiancha.biz.rectify.domain.JcRectifyReview;

/**
 * 整改复核/回头看记录 Mapper
 *
 * @author 小标
 */
public interface JcRectifyReviewMapper {

    /** 按问题查询复核记录（时间正序） */
    List<JcRectifyReview> selectReviewByIssueId(Long issueId);

    /** 新增复核/回头看记录 */
    int insertReview(JcRectifyReview review);

    /** 删除问题时级联清理复核记录 */
    int deleteReviewByIssueId(Long issueId);
}
