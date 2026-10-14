package com.jiancha.biz.edu.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jiancha.biz.edu.domain.JcEduCommitment;

/**
 * 廉洁承诺书签收 Mapper（M9）
 *
 * <p>承诺书本体与签收记录合一表，recordType=0 为承诺书、1 为签收记录。</p>
 *
 * @author 小标
 */
public interface JcEduCommitmentMapper {

    /** 列表（按 recordType 过滤） */
    List<JcEduCommitment> selectJcEduCommitmentList(JcEduCommitment jcEduCommitment);

    /** 详情 */
    JcEduCommitment selectJcEduCommitmentByCommitId(Long commitId);

    /** 某人某承诺书的签收记录（幂等：重复签收返回既有） */
    JcEduCommitment selectSignRecord(@Param("commitId") Long commitId, @Param("personId") Long personId);

    /** 已发布承诺书（签收入口） */
    List<JcEduCommitment> selectPublished();

    /** 新增（承诺书本体或签收记录） */
    int insertJcEduCommitment(JcEduCommitment jcEduCommitment);

    /** 修改 */
    int updateJcEduCommitment(JcEduCommitment jcEduCommitment);

    /** 发布（草稿→已发布） */
    int publishCommitment(Long commitId, String updateBy);

    /** 关闭（已发布→已关闭） */
    int closeCommitment(Long commitId, String updateBy);

    /** 签收（写入签收人与时间戳） */
    int signCommitment(Long commitId, Long personId, String personName, String updateBy);

    /** 逻辑删除 */
    int deleteJcEduCommitmentByCommitId(Long commitId);

    /** 统计：承诺书本体数 */
    long countTotal();

    /** 统计：应签收总人次（各承诺书按适用范围展开，由服务层组装） */
    long countExpected(@Param("commitId") Long commitId);

    /** 统计：已签收人次 */
    long countSigned(@Param("commitId") Long commitId);
}
