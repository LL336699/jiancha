package com.jiancha.biz.edu.mapper;

import java.util.List;
import java.util.Map;

/**
 * 教育统计聚合 Mapper（M9）
 *
 * <p>SQL 层聚合保证口径统一；JDBC 返回值经 Number 转换后使用。</p>
 *
 * @author 小标
 */
public interface JcEduScanMapper {

    /** 成绩分布：优(>=90)/良(80-89)/中(60-79)/差(<60) */
    List<Map<String, Object>> scoreDistribution();

    /** 题型构成 */
    List<Map<String, Object>> byQtype();

    /** 分类题库量 */
    List<Map<String, Object>> byCategory();

    /** 月度考试量（近12个月） */
    List<Map<String, Object>> monthlyExams();

    /** 错题 TopN（按答错次数降序） */
    List<Map<String, Object>> topWrongQuestions(int limit);

    /** 承诺书签收明细（按人：应签/已签） */
    List<Map<String, Object>> commitSignDetail();

    /** 考生成绩排行 */
    List<Map<String, Object>> examRanking(int limit);

    /** 轻量参数读取（jiancha.edu.*，不引入 sys 模块依赖） */
    String selectConfigValue(String key);
}
