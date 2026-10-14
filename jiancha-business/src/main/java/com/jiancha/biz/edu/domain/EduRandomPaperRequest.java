package com.jiancha.biz.edu.domain;

import java.io.Serializable;
import java.util.List;

/**
 * 随机组卷请求（M9）：试卷属性 + 抽题策略
 *
 * @author 小标
 */
public class EduRandomPaperRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 试卷属性 */
    private JcEduPaper paper;

    /** 抽题策略（分类 × 题型 × 数量） */
    private List<EduRandomRule> rules;

    public JcEduPaper getPaper() {
        return paper;
    }

    public void setPaper(JcEduPaper paper) {
        this.paper = paper;
    }

    public List<EduRandomRule> getRules() {
        return rules;
    }

    public void setRules(List<EduRandomRule> rules) {
        this.rules = rules;
    }
}
