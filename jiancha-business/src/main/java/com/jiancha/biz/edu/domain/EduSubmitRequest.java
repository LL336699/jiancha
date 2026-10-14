package com.jiancha.biz.edu.domain;

import java.util.List;
import java.util.Map;

/**
 * 考试交卷请求（M9）
 *
 * <p>answers 为题目ID → 考生作答；多选题作答格式为逗号分隔字母（如 "A,C"），
 * 顺序无关（服务端比对时按字符集排序后比较）。</p>
 *
 * @author 小标
 */
public class EduSubmitRequest {

    /** 考试ID */
    private Long examId;

    /** 题目ID → 作答 */
    private List<Map<String, String>> answers;

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public List<Map<String, String>> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Map<String, String>> answers) {
        this.answers = answers;
    }
}
