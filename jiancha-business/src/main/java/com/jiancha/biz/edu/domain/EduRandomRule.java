package com.jiancha.biz.edu.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 随机组卷策略请求（M9）
 *
 * <p>random 组卷方式：按分类 + 题型 + 数量策略从启用题库抽取；
 * 策略项支持多组（如 单选×5 + 多选×3 + 判断×4），服务层逐组抽取并落快照。</p>
 *
 * @author 小标
 */
public class EduRandomRule {

    /** 题库分类（字典 jc_edu_category） */
    @NotBlank(message = "策略分类不能为空")
    private String category;

    /** 题型（字典 jc_edu_qtype） */
    @NotBlank(message = "策略题型不能为空")
    private String qtype;

    /** 抽取数量 */
    @Min(value = 1, message = "抽取数量必须大于0")
    private Integer count;

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

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
