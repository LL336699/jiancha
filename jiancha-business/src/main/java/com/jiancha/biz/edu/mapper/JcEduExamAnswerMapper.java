package com.jiancha.biz.edu.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jiancha.biz.edu.domain.JcEduExamAnswer;

/**
 * 答题明细 Mapper（M9）
 *
 * @author 小标
 */
public interface JcEduExamAnswerMapper {

    /** 按考试查明细 */
    List<JcEduExamAnswer> selectByExamId(Long examId);

    /** 按考生查错题明细（错题本：join 题目快照取解析） */
    List<JcEduExamAnswer> selectWrongByPerson(@Param("personId") Long personId,
                                              @Param("limit") int limit);

    /** 批量写入（交卷评分时一次性插入） */
    int batchInsert(@Param("list") List<JcEduExamAnswer> list);

    /** 按考试删除（弃考重置评分时） */
    int deleteByExamId(Long examId);
}
