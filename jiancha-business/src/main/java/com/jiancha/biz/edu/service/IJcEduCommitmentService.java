package com.jiancha.biz.edu.service;

import java.util.List;
import java.util.Map;

import com.jiancha.biz.edu.domain.JcEduCommitment;

/**
 * 廉洁承诺书签收服务（M9）
 *
 * @author 小标
 */
public interface IJcEduCommitmentService {

    /** 列表（recordType 区分本体/签收记录） */
    List<JcEduCommitment> selectCommitmentList(JcEduCommitment query);

    /** 详情 */
    JcEduCommitment selectCommitmentById(Long commitId);

    /** 已发布承诺书（签收入口） */
    List<JcEduCommitment> selectPublished();

    /** 新增承诺书（草稿） */
    int insertCommitment(JcEduCommitment commitment);

    /** 修改 */
    int updateCommitment(JcEduCommitment commitment);

    /** 发布（草稿→已发布） */
    int publishCommitment(Long commitId);

    /** 关闭（已发布→已关闭） */
    int closeCommitment(Long commitId);

    /** 删除（仅草稿） */
    int deleteCommitment(Long commitId);

    /** 签收（幂等：重复签收返回既有记录） */
    JcEduCommitment sign(Long commitId, Long personId, String personName);

    /** 签收进度（全员 scope 才有应签人数预测） */
    Map<String, Object> signProgress(Long commitId);
}
