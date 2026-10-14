package com.jiancha.biz.edu.domain;

import java.util.List;
import java.util.Map;

/**
 * 廉政教育统计视图（M9 廉政教育题库）
 *
 * <p>三层口径：卡片总览（题库/试卷/考试/通过率/承诺书签收率）→
 * 图表分布（成绩分布/题型构成/分类题库量/月度考试量）→ 明细下钻（错题 TopN / 参考人员）。</p>
 *
 * @author 小标
 */
public class EduStat {

    /** 卡片：题目总数 */
    private Long questionTotal;

    /** 卡片：启用题目数 */
    private Long questionEnabled;

    /** 卡片：试卷总数 */
    private Long paperTotal;

    /** 卡片：考试总人次 */
    private Long examTotal;

    /** 卡片：通过人次 */
    private Long examPassed;

    /** 卡片：通过率(%) */
    private Double passRate;

    /** 卡片：承诺书应签收人数 */
    private Long commitExpected;

    /** 卡片：承诺书已签收人数 */
    private Long commitSigned;

    /** 图表：成绩分布(优/良/中/差) */
    private List<Map<String, Object>> scoreDistribution;

    /** 图表：题型构成 */
    private List<Map<String, Object>> byQtype;

    /** 图表：分类题库量 */
    private List<Map<String, Object>> byCategory;

    /** 图表：月度考试量 */
    private List<Map<String, Object>> monthlyExams;

    /** 下钻：错题 TopN */
    private List<Map<String, Object>> topWrong;

    /** 下钻：承诺书签收明细(按人) */
    private List<Map<String, Object>> commitDetail;

    public Long getQuestionTotal() {
        return questionTotal;
    }

    public void setQuestionTotal(Long questionTotal) {
        this.questionTotal = questionTotal;
    }

    public Long getQuestionEnabled() {
        return questionEnabled;
    }

    public void setQuestionEnabled(Long questionEnabled) {
        this.questionEnabled = questionEnabled;
    }

    public Long getPaperTotal() {
        return paperTotal;
    }

    public void setPaperTotal(Long paperTotal) {
        this.paperTotal = paperTotal;
    }

    public Long getExamTotal() {
        return examTotal;
    }

    public void setExamTotal(Long examTotal) {
        this.examTotal = examTotal;
    }

    public Long getExamPassed() {
        return examPassed;
    }

    public void setExamPassed(Long examPassed) {
        this.examPassed = examPassed;
    }

    public Double getPassRate() {
        return passRate;
    }

    public void setPassRate(Double passRate) {
        this.passRate = passRate;
    }

    public Long getCommitExpected() {
        return commitExpected;
    }

    public void setCommitExpected(Long commitExpected) {
        this.commitExpected = commitExpected;
    }

    public Long getCommitSigned() {
        return commitSigned;
    }

    public void setCommitSigned(Long commitSigned) {
        this.commitSigned = commitSigned;
    }

    public List<Map<String, Object>> getScoreDistribution() {
        return scoreDistribution;
    }

    public void setScoreDistribution(List<Map<String, Object>> scoreDistribution) {
        this.scoreDistribution = scoreDistribution;
    }

    public List<Map<String, Object>> getByQtype() {
        return byQtype;
    }

    public void setByQtype(List<Map<String, Object>> byQtype) {
        this.byQtype = byQtype;
    }

    public List<Map<String, Object>> getByCategory() {
        return byCategory;
    }

    public void setByCategory(List<Map<String, Object>> byCategory) {
        this.byCategory = byCategory;
    }

    public List<Map<String, Object>> getMonthlyExams() {
        return monthlyExams;
    }

    public void setMonthlyExams(List<Map<String, Object>> monthlyExams) {
        this.monthlyExams = monthlyExams;
    }

    public List<Map<String, Object>> getTopWrong() {
        return topWrong;
    }

    public void setTopWrong(List<Map<String, Object>> topWrong) {
        this.topWrong = topWrong;
    }

    public List<Map<String, Object>> getCommitDetail() {
        return commitDetail;
    }

    public void setCommitDetail(List<Map<String, Object>> commitDetail) {
        this.commitDetail = commitDetail;
    }
}
