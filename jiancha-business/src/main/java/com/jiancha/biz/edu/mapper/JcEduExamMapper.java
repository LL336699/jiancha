package com.jiancha.biz.edu.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jiancha.biz.edu.domain.JcEduExam;

/**
 * 考试记录 Mapper（M9）
 *
 * @author 小标
 */
public interface JcEduExamMapper {

    /** 列表 */
    List<JcEduExam> selectJcEduExamList(JcEduExam jcEduExam);

    /** 详情 */
    JcEduExam selectJcEduExamByExamId(Long examId);

    /** 某考生在某试卷的进行中考试（断点续考：避免重复开考） */
    JcEduExam selectDoingExam(@Param("paperId") Long paperId, @Param("personId") Long personId);

    /** 某考生已通过某试卷的考试记录（通过后不可重考，参数 jiancha.edu.retake=Y 放开） */
    JcEduExam selectPassedExam(@Param("paperId") Long paperId, @Param("personId") Long personId);

    /** 新增（开考） */
    int insertJcEduExam(JcEduExam jcEduExam);

    /** 交卷：写入得分/通过/提交时间/状态 */
    int submitExam(JcEduExam jcEduExam);

    /** 弃考：超时未交卷由调度或考生主动置弃考 */
    int abandonExam(Long examId, String updateBy);

    /** 统计：考试总人次 */
    long countTotal();

    /** 统计：通过人次 */
    long countPassed();

    /** 统计：某人考试记录数（学习进度） */
    long countByPerson(Long personId);

    /** 统计：某人通过次数（学习进度） */
    long countPassedByPerson(Long personId);
}
