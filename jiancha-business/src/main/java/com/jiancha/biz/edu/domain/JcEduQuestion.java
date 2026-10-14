package com.jiancha.biz.edu.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 廉政教育题库 jc_edu_question（M9 廉政教育题库）
 *
 * <p>企业版口径（设计文档 §M9）：题库按分类（党纪国法/企业规章/内控制度/典型案例/廉洁从业）
 * 与题型（单选/多选/判断）管理，支持后台维护与 Excel 批量导入；答案与解析分离存储，
 * 列表接口不返回答案列，考试时才下发（防刷题泄题）。</p>
 *
 * @author 小标
 */
public class JcEduQuestion extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 题目ID */
    private Long questionId;

    /** 题库分类（字典 jc_edu_category：党纪国法/企业规章/内控制度/典型案例/廉洁从业） */
    @NotBlank(message = "题库分类不能为空")
    private String category;

    /** 题型（字典 jc_edu_qtype：single 单选/multi 多选/judge 判断） */
    @NotBlank(message = "题型不能为空")
    private String qtype;

    /** 难度（字典 jc_edu_level：易/中/难） */
    private String level;

    /** 题干 */
    @NotBlank(message = "题干不能为空")
    @Size(max = 1000, message = "题干长度不能超过1000")
    private String title;

    /** 选项 JSON（判断题为 ["对","错"]；选择题如 {"A":"..","B":"..","C":"..","D":".."}） */
    private String options;

    /** 正确答案（单选/判断为单个字母或"对/错"；多选为逗号分隔字母，如 "A,C"） */
    @NotBlank(message = "正确答案不能为空")
    private String answer;

    /** 答案解析 */
    private String analysis;

    /** 分值 */
    @NotNull(message = "分值不能为空")
    private Integer score;

    /** 状态（字典 jc_edu_qstatus：启用/停用；停用不进组卷） */
    private String status;

    /** 引用次数（组卷引用计数，供题库热度统计） */
    private Integer useCount;

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getQtype() {
        return qtype;
    }

    public void setQtype(String qtype) {
        this.qtype = qtype;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getUseCount() {
        return useCount;
    }

    public void setUseCount(Integer useCount) {
        this.useCount = useCount;
    }
}
