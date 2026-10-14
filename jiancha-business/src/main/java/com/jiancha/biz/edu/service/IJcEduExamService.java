package com.jiancha.biz.edu.service;

import java.util.List;
import java.util.Map;

import com.jiancha.biz.edu.domain.EduSubmitRequest;
import com.jiancha.biz.edu.domain.JcEduExam;
import com.jiancha.biz.edu.domain.JcEduExamAnswer;

/**
 * 廉政教育考试服务（M9）
 *
 * @author 小标
 */
public interface IJcEduExamService {

    /** 考试记录列表 */
    List<JcEduExam> selectExamList(JcEduExam query);

    /** 考试详情（含答题明细） */
    Map<String, Object> selectExamDetail(Long examId);

    /** 开考（返回试卷题目，不含答案；断点续考/通过禁重考由开关控制） */
    Map<String, Object> startExam(Long paperId, Long personId, String personName);

    /** 交卷评分（单选/判断精确比对，多选集合比对；返回得分与是否通过） */
    Map<String, Object> submitExam(EduSubmitRequest request);

    /** 弃考 */
    int abandonExam(Long examId);

    /** 错题本（某人答错明细，取试卷题目快照解析） */
    List<JcEduExamAnswer> wrongBook(Long personId, int limit);

    /** 学习进度（某人考试数/通过数） */
    Map<String, Object> personProgress(Long personId);

    /** 超时未交卷自动弃考（调度入口） */
    int autoAbandonTimeout();
}
