package com.jiancha.biz.edu.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jiancha.biz.edu.mapper.JcEduCommitmentMapper;
import com.jiancha.biz.edu.mapper.JcEduExamMapper;
import com.jiancha.biz.edu.mapper.JcEduPaperMapper;
import com.jiancha.biz.edu.mapper.JcEduQuestionMapper;
import com.jiancha.biz.edu.mapper.JcEduScanMapper;
import com.jiancha.biz.edu.service.IJcEduStatService;

/**
 * 廉政教育统计服务实现（M9）
 *
 * @author 小标
 */
@Service
public class JcEduStatServiceImpl implements IJcEduStatService {

    @Autowired
    private JcEduQuestionMapper questionMapper;

    @Autowired
    private JcEduPaperMapper paperMapper;

    @Autowired
    private JcEduExamMapper examMapper;

    @Autowired
    private JcEduCommitmentMapper commitmentMapper;

    @Autowired
    private JcEduScanMapper scanMapper;

    @Override
    public Map<String, Object> stat() {
        Map<String, Object> result = new HashMap<>();
        // 卡片
        Map<String, Object> cards = new HashMap<>();
        cards.put("questionTotal", questionMapper.countTotal());
        cards.put("questionEnabled", questionMapper.countEnabled());
        cards.put("paperTotal", paperMapper.countTotal());
        cards.put("examTotal", examMapper.countTotal());
        cards.put("examPassed", examMapper.countPassed());
        cards.put("commitTotal", commitmentMapper.countTotal());
        cards.put("commitSigned", commitmentMapper.countSigned(null));
        result.put("cards", cards);
        // 图表
        result.put("scoreDistribution", scanMapper.scoreDistribution());
        result.put("byQtype", scanMapper.byQtype());
        result.put("byCategory", scanMapper.byCategory());
        result.put("monthlyExams", scanMapper.monthlyExams());
        // 明细视图
        result.put("topWrongQuestions", scanMapper.topWrongQuestions(10));
        result.put("commitSignDetail", scanMapper.commitSignDetail());
        result.put("examRanking", scanMapper.examRanking(10));
        return result;
    }
}
