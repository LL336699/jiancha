package com.jiancha.biz.edu.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.jiancha.biz.edu.domain.EduSubmitRequest;
import com.jiancha.biz.edu.domain.JcEduExam;
import com.jiancha.biz.edu.domain.JcEduExamAnswer;
import com.jiancha.biz.edu.domain.JcEduPaper;
import com.jiancha.biz.edu.domain.JcEduPaperQuestion;
import com.jiancha.biz.edu.domain.JcEduQuestion;
import com.jiancha.biz.edu.mapper.JcEduExamAnswerMapper;
import com.jiancha.biz.edu.mapper.JcEduExamMapper;
import com.jiancha.biz.edu.mapper.JcEduPaperMapper;
import com.jiancha.biz.edu.mapper.JcEduPaperQuestionMapper;
import com.jiancha.biz.edu.mapper.JcEduQuestionMapper;
import com.jiancha.biz.edu.mapper.JcEduScanMapper;
import com.jiancha.biz.edu.service.IJcEduExamService;

/**
 * 廉政教育考试服务实现（M9）
 *
 * <p>评分引擎：单选/判断答案字母精确比对；多选按集合比对（完全一致才得分，避免半对给分争议）。</p>
 *
 * @author 小标
 */
@Service
public class JcEduExamServiceImpl implements IJcEduExamService {

    /** 考试状态：进行中/已交卷/弃考 */
    private static final String EXAM_DOING = "0";
    private static final String EXAM_SUBMITTED = "1";
    private static final String EXAM_ABANDONED = "2";

    private static final String QTYPE_MULTI = "多选";

    @Autowired
    private JcEduExamMapper examMapper;

    @Autowired
    private JcEduExamAnswerMapper answerMapper;

    @Autowired
    private JcEduPaperMapper paperMapper;

    @Autowired
    private JcEduPaperQuestionMapper paperQuestionMapper;

    @Autowired
    private JcEduQuestionMapper questionMapper;

    @Autowired
    private JcEduScanMapper scanMapper;

    @Override
    public List<JcEduExam> selectExamList(JcEduExam query) {
        return examMapper.selectJcEduExamList(query);
    }

    @Override
    public Map<String, Object> selectExamDetail(Long examId) {
        JcEduExam exam = examMapper.selectJcEduExamByExamId(examId);
        if (exam == null) {
            throw new ServiceException("考试记录不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("exam", exam);
        result.put("answers", answerMapper.selectByExamId(examId));
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> startExam(Long paperId, Long personId, String personName) {
        if (personId == null) {
            throw new ServiceException("考生不能为空");
        }
        JcEduPaper paper = paperMapper.selectJcEduPaperByPaperId(paperId);
        if (paper == null || !"1".equals(paper.getStatus())) {
            throw new ServiceException("试卷不存在或未发布");
        }
        // 断点续考：进行中考试直接返回，不重复开考
        JcEduExam doing = examMapper.selectDoingExam(paperId, personId);
        if (doing != null) {
            return examPayload(doing, paper, false);
        }
        // 通过禁重考（开关 jiancha.edu.retake=Y 放开）
        JcEduExam passed = examMapper.selectPassedExam(paperId, personId);
        if (passed != null && !retakeEnabled()) {
            throw new ServiceException("该试卷已通过（得分 " + passed.getScore() + "），不可重复考试");
        }
        JcEduExam exam = new JcEduExam();
        exam.setPaperId(paperId);
        exam.setPersonId(personId);
        exam.setPersonName(personName);
        exam.setStartTime(new Date());
        exam.setStatus(EXAM_DOING);
        exam.setCreateBy(SecurityUtils.getUsername());
        examMapper.insertJcEduExam(exam);
        return examPayload(exam, paper, true);
    }

    @Override
    @Transactional
    public Map<String, Object> submitExam(EduSubmitRequest request) {
        if (request == null || request.getExamId() == null) {
            throw new ServiceException("考试ID不能为空");
        }
        JcEduExam exam = examMapper.selectJcEduExamByExamId(request.getExamId());
        if (exam == null) {
            throw new ServiceException("考试记录不存在");
        }
        if (!EXAM_DOING.equals(exam.getStatus())) {
            throw new ServiceException("考试已交卷或已弃考，不能重复交卷");
        }
        JcEduPaper paper = paperMapper.selectJcEduPaperByPaperId(exam.getPaperId());
        List<JcEduPaperQuestion> questions = paperQuestionMapper.selectByPaperId(exam.getPaperId());
        if (questions.isEmpty()) {
            throw new ServiceException("试卷无题目，无法评分");
        }
        // 答案表：questionId -> 我的作答
        Map<Long, String> myAnswers = new HashMap<>();
        if (request.getAnswers() != null) {
            for (Map<String, String> a : request.getAnswers()) {
                if (a == null || StringUtils.isEmpty(a.get("questionId"))) {
                    continue;
                }
                myAnswers.put(Long.valueOf(a.get("questionId")), nv(a.get("myAnswer")).trim());
            }
        }
        // 逐题评分
        String operator = SecurityUtils.getUsername();
        List<JcEduExamAnswer> details = new ArrayList<>();
        int total = 0;
        for (JcEduPaperQuestion pq : questions) {
            JcEduQuestion q = questionMapper.selectJcEduQuestionByQuestionId(pq.getQuestionId());
            if (q == null) {
                continue;
            }
            String mine = myAnswers.getOrDefault(pq.getQuestionId(), "");
            boolean right = judge(q, mine);
            int gain = right ? (pq.getScore() == null ? 0 : pq.getScore()) : 0;
            total += gain;
            JcEduExamAnswer detail = new JcEduExamAnswer();
            detail.setExamId(exam.getExamId());
            detail.setQuestionId(pq.getQuestionId());
            detail.setMyAnswer(mine);
            detail.setCorrect(right ? "1" : "0");
            detail.setScore(gain);
            detail.setCreateBy(operator);
            details.add(detail);
        }
        answerMapper.deleteByExamId(exam.getExamId());
        answerMapper.batchInsert(details);
        // 总分/是否通过（试卷总分为空时按组卷分值兜底）
        int paperTotal = paper.getTotalScore() != null && paper.getTotalScore() > 0
                ? paper.getTotalScore()
                : questions.stream().mapToInt(pq -> pq.getScore() == null ? 0 : pq.getScore()).sum();
        int passScore = paper.getPassScore() == null ? (int) Math.round(paperTotal * 0.6) : paper.getPassScore();
        boolean passed = total >= passScore;
        JcEduExam submit = new JcEduExam();
        submit.setExamId(exam.getExamId());
        submit.setSubmitTime(new Date());
        submit.setScore(total);
        submit.setPassed(passed ? "1" : "0");
        submit.setStatus(EXAM_SUBMITTED);
        submit.setUpdateBy(operator);
        examMapper.submitExam(submit);

        Map<String, Object> result = new HashMap<>();
        result.put("examId", exam.getExamId());
        result.put("score", total);
        result.put("totalScore", paperTotal);
        result.put("passScore", passScore);
        result.put("passed", passed);
        result.put("answeredCount", myAnswers.size());
        result.put("questionCount", questions.size());
        return result;
    }

    @Override
    @Transactional
    public int abandonExam(Long examId) {
        return examMapper.abandonExam(examId, SecurityUtils.getUsername());
    }

    @Override
    public List<JcEduExamAnswer> wrongBook(Long personId, int limit) {
        return answerMapper.selectWrongByPerson(personId, limit <= 0 ? 50 : limit);
    }

    @Override
    public Map<String, Object> personProgress(Long personId) {
        Map<String, Object> progress = new HashMap<>();
        progress.put("examCount", examMapper.countByPerson(personId));
        progress.put("passedCount", examMapper.countPassedByPerson(personId));
        return progress;
    }

    @Override
    @Transactional
    public int autoAbandonTimeout() {
        // 超过时长 1.5 倍仍未交卷的进行中考试自动弃考
        List<JcEduExam> doing = examMapper.selectJcEduExamList(statusOf(EXAM_DOING));
        int abandoned = 0;
        LocalDateTime now = LocalDateTime.now();
        for (JcEduExam exam : doing) {
            JcEduPaper paper = paperMapper.selectJcEduPaperByPaperId(exam.getPaperId());
            if (paper == null || paper.getDurationMin() == null || paper.getDurationMin() <= 0) {
                continue;
            }
            if (exam.getStartTime() == null) {
                continue;
            }
            LocalDateTime start = LocalDateTime.ofInstant(exam.getStartTime().toInstant(),
                    java.time.ZoneId.systemDefault());
            long minutes = Duration.between(start, now).toMinutes();
            if (minutes > paper.getDurationMin() * 1.5) {
                examMapper.abandonExam(exam.getExamId(), "system");
                abandoned++;
            }
        }
        return abandoned;
    }

    /** 构造开考返回体：试卷信息 + 题目（不含答案与解析） */
    private Map<String, Object> examPayload(JcEduExam exam, JcEduPaper paper, boolean created) {
        List<JcEduPaperQuestion> questions = paperQuestionMapper.selectByPaperId(exam.getPaperId());
        List<Map<String, Object>> items = questions.stream().map(pq -> {
            Map<String, Object> item = new HashMap<>();
            item.put("questionId", pq.getQuestionId());
            item.put("qtype", pq.getQtype());
            item.put("score", pq.getScore());
            item.put("sortNo", pq.getSortNo());
            item.put("title", pq.getTitleSnapshot());
            return item;
        }).collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("examId", exam.getExamId());
        result.put("created", created);
        result.put("paperId", paper.getPaperId());
        result.put("paperName", paper.getPaperName());
        result.put("durationMin", paper.getDurationMin());
        result.put("passScore", paper.getPassScore());
        result.put("totalScore", paper.getTotalScore());
        result.put("startTime", exam.getStartTime());
        result.put("questions", items);
        return result;
    }

    /**
     * 判定单题对错：
     * 单选/判断：答案字母忽略大小写精确比对；
     * 多选：逗号分隔集合完全一致（顺序无关、去重）才得分。
     */
    private boolean judge(JcEduQuestion q, String mine) {
        if (StringUtils.isEmpty(mine)) {
            return false;
        }
        String std = nv(q.getAnswer()).trim().toUpperCase();
        String my = mine.trim().toUpperCase();
        if (QTYPE_MULTI.equals(q.getQtype())) {
            Set<String> stdSet = new HashSet<>(Arrays.asList(std.split(",")));
            Set<String> mySet = new HashSet<>(Arrays.asList(my.split(",")));
            return !stdSet.isEmpty() && stdSet.equals(mySet);
        }
        return std.equals(my);
    }

    private int questionScore(Long questionId) {
        JcEduQuestion q = questionMapper.selectJcEduQuestionByQuestionId(questionId);
        return q == null || q.getScore() == null ? 0 : q.getScore();
    }

    private boolean retakeEnabled() {
        String cfg = scanMapper.selectConfigValue("jiancha.edu.retake");
        return "Y".equalsIgnoreCase(nv(cfg).trim());
    }

    private JcEduExam statusOf(String status) {
        JcEduExam q = new JcEduExam();
        q.setStatus(status);
        return q;
    }

    /** null 安全转空串（RuoYi StringUtils 无 nullToEmpty） */
    private String nv(String s) {
        return s == null ? "" : s;
    }
}
