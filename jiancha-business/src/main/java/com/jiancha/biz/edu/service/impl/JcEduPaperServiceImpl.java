package com.jiancha.biz.edu.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.jiancha.biz.edu.domain.EduRandomRule;
import com.jiancha.biz.edu.domain.JcEduPaper;
import com.jiancha.biz.edu.domain.JcEduPaperQuestion;
import com.jiancha.biz.edu.domain.JcEduQuestion;
import com.jiancha.biz.edu.mapper.JcEduPaperMapper;
import com.jiancha.biz.edu.mapper.JcEduPaperQuestionMapper;
import com.jiancha.biz.edu.mapper.JcEduQuestionMapper;
import com.jiancha.biz.edu.service.IJcEduPaperService;

/**
 * 廉政教育试卷服务实现（M9）
 *
 * @author 小标
 */
@Service
public class JcEduPaperServiceImpl implements IJcEduPaperService {

    /** 试卷状态：草稿/已发布/已关闭 */
    private static final String PAPER_DRAFT = "0";
    private static final String PAPER_PUBLISHED = "1";

    /** 组卷方式：手工/随机 */
    private static final String MODE_MANUAL = "手工";
    private static final String MODE_RANDOM = "随机";

    @Autowired
    private JcEduPaperMapper paperMapper;

    @Autowired
    private JcEduPaperQuestionMapper paperQuestionMapper;

    @Autowired
    private JcEduQuestionMapper questionMapper;

    @Override
    public List<JcEduPaper> selectPaperList(JcEduPaper query) {
        return paperMapper.selectJcEduPaperList(query);
    }

    @Override
    public JcEduPaper selectPaperById(Long paperId) {
        JcEduPaper paper = paperMapper.selectJcEduPaperByPaperId(paperId);
        if (paper == null) {
            throw new ServiceException("试卷不存在");
        }
        paper.setQuestions(paperQuestionMapper.selectByPaperId(paperId));
        return paper;
    }

    @Override
    public List<JcEduPaper> selectPublishedPapers() {
        return paperMapper.selectPublishedPapers();
    }

    @Override
    @Transactional
    public int insertPaper(JcEduPaper paper) {
        if (StringUtils.isEmpty(paper.getPaperName())) {
            throw new ServiceException("试卷名称不能为空");
        }
        if (StringUtils.isEmpty(paper.getPaperMode())) {
            paper.setPaperMode(MODE_MANUAL);
        }
        paper.setPaperCode(generatePaperCode());
        paper.setStatus(PAPER_DRAFT);
        paper.setCreateBy(SecurityUtils.getUsername());
        int rows = paperMapper.insertJcEduPaper(paper);
        replaceQuestions(paper.getPaperId(), paper.getQuestions());
        recalcScore(paper.getPaperId());
        return rows;
    }

    @Override
    @Transactional
    public int updatePaper(JcEduPaper paper) {
        JcEduPaper exists = paperMapper.selectJcEduPaperByPaperId(paper.getPaperId());
        if (exists == null) {
            throw new ServiceException("试卷不存在");
        }
        if (!PAPER_DRAFT.equals(exists.getStatus())) {
            throw new ServiceException("仅草稿状态试卷可修改，已发布试卷请关闭后重新创建");
        }
        paper.setUpdateBy(SecurityUtils.getUsername());
        int rows = paperMapper.updateJcEduPaper(paper);
        if (paper.getQuestions() != null) {
            replaceQuestions(paper.getPaperId(), paper.getQuestions());
            recalcScore(paper.getPaperId());
        }
        return rows;
    }

    @Override
    @Transactional
    public JcEduPaper randomPaper(JcEduPaper paper, List<EduRandomRule> rules) {
        if (rules == null || rules.isEmpty()) {
            throw new ServiceException("随机组卷策略不能为空");
        }
        List<JcEduPaperQuestion> picked = new ArrayList<>();
        for (EduRandomRule rule : rules) {
            int count = rule.getCount() == null ? 0 : rule.getCount();
            if (count <= 0) {
                throw new ServiceException("抽题数量必须大于 0");
            }
            List<JcEduQuestion> drawn = questionMapper.selectRandomByRule(rule.getCategory(), rule.getQtype(), count);
            if (drawn.size() < count) {
                throw new ServiceException("题库「" + nv(rule.getCategory()) + "｜"
                        + nv(rule.getQtype()) + "」仅 " + drawn.size()
                        + " 道启用题，不足 " + count + " 道");
            }
            for (JcEduQuestion q : drawn) {
                picked.add(toPaperQuestion(q));
            }
        }
        paper.setQuestions(picked);
        paper.setPaperMode(MODE_RANDOM);
        insertPaper(paper);
        return paper;
    }

    @Override
    @Transactional
    public int publishPaper(Long paperId) {
        JcEduPaper paper = paperMapper.selectJcEduPaperByPaperId(paperId);
        if (paper == null) {
            throw new ServiceException("试卷不存在");
        }
        if (!PAPER_DRAFT.equals(paper.getStatus())) {
            throw new ServiceException("仅草稿状态试卷可发布");
        }
        List<JcEduPaperQuestion> questions = paperQuestionMapper.selectByPaperId(paperId);
        if (questions.isEmpty()) {
            throw new ServiceException("试卷未组题，不能发布");
        }
        // 固化题目快照：先删后插，写入题目当前标题/题型/分值，题库后续改动不影响已发布试卷
        List<Long> ids = questions.stream().map(JcEduPaperQuestion::getQuestionId).collect(Collectors.toList());
        List<JcEduQuestion> fresh = questionMapper.selectJcEduQuestionByIds(ids);
        for (JcEduPaperQuestion pq : questions) {
            for (JcEduQuestion q : fresh) {
                if (q.getQuestionId().equals(pq.getQuestionId())) {
                    pq.setTitleSnapshot(q.getTitle());
                    pq.setQtype(q.getQtype());
                    if (pq.getScore() == null) {
                        pq.setScore(q.getScore());
                    }
                    break;
                }
            }
        }
        paperQuestionMapper.deleteByPaperId(paperId);
        paperQuestionMapper.batchInsert(questions);
        // 题目引用计数累加
        for (Long qid : ids) {
            questionMapper.increaseUseCount(qid);
        }
        return paperMapper.publishPaper(paperId, SecurityUtils.getUsername());
    }

    @Override
    public int closePaper(Long paperId) {
        return paperMapper.closePaper(paperId, SecurityUtils.getUsername());
    }

    @Override
    @Transactional
    public int deletePaper(Long paperId) {
        JcEduPaper paper = paperMapper.selectJcEduPaperByPaperId(paperId);
        if (paper == null) {
            throw new ServiceException("试卷不存在");
        }
        if (!PAPER_DRAFT.equals(paper.getStatus())) {
            throw new ServiceException("仅草稿状态试卷可删除");
        }
        paperQuestionMapper.deleteByPaperId(paperId);
        return paperMapper.deleteJcEduPaperByPaperId(paperId);
    }

    /** 整体替换组卷题目（草稿态） */
    private void replaceQuestions(Long paperId, List<JcEduPaperQuestion> questions) {
        paperQuestionMapper.deleteByPaperId(paperId);
        if (questions != null && !questions.isEmpty()) {
            int sortNo = 1;
            for (JcEduPaperQuestion pq : questions) {
                pq.setPaperId(paperId);
                pq.setSortNo(sortNo++);
            }
            paperQuestionMapper.batchInsert(questions);
        }
    }

    /** 依据组卷题目重算总分 */
    private void recalcScore(Long paperId) {
        List<JcEduPaperQuestion> questions = paperQuestionMapper.selectByPaperId(paperId);
        int total = questions.stream()
                .mapToInt(q -> q.getScore() == null ? 0 : q.getScore())
                .sum();
        JcEduPaper update = new JcEduPaper();
        update.setPaperId(paperId);
        update.setTotalScore(total);
        update.setPassScore((int) Math.round(total * 0.6));
        paperMapper.updateJcEduPaper(update);
    }

    /** null 安全转空串（RuoYi StringUtils 无 nullToEmpty） */
    private String nv(String s) {
        return s == null ? "" : s;
    }

    private JcEduPaperQuestion toPaperQuestion(JcEduQuestion q) {
        JcEduPaperQuestion pq = new JcEduPaperQuestion();
        pq.setQuestionId(q.getQuestionId());
        pq.setQtype(q.getQtype());
        pq.setScore(q.getScore());
        return pq;
    }

    /** 生成试卷编号：PA+yyyyMMdd+seq */
    private String generatePaperCode() {
        String prefix = "PA" + java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String max = paperMapper.selectMaxCodeByPrefix(prefix);
        int seq = 1;
        if (StringUtils.isNotEmpty(max) && max.length() >= prefix.length()) {
            String tail = max.substring(prefix.length());
            if (tail.matches("\\d+")) {
                seq = Integer.parseInt(tail) + 1;
            }
        }
        return prefix + String.format("%03d", seq);
    }
}
