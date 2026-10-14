package com.jiancha.biz.edu.service;

import java.util.List;

import com.jiancha.biz.edu.domain.EduRandomRule;
import com.jiancha.biz.edu.domain.JcEduPaper;

/**
 * 廉政教育试卷服务（M9）
 *
 * <p>两种组卷方式：手工选题目录 + 随机策略抽题；发布时固化题目快照。</p>
 *
 * @author 小标
 */
public interface IJcEduPaperService {

    /** 列表 */
    List<JcEduPaper> selectPaperList(JcEduPaper query);

    /** 详情（含组卷题目） */
    JcEduPaper selectPaperById(Long paperId);

    /** 已发布试卷（考试入口） */
    List<JcEduPaper> selectPublishedPapers();

    /** 新增（草稿） */
    int insertPaper(JcEduPaper paper);

    /** 修改（仅草稿可改，含题目目录整体替换） */
    int updatePaper(JcEduPaper paper);

    /** 按随机策略抽题并组卷（草稿） */
    JcEduPaper randomPaper(JcEduPaper paper, List<EduRandomRule> rules);

    /** 发布（固化题目快照 + 总分/题量校验 + 题目引用计数累加） */
    int publishPaper(Long paperId);

    /** 关闭 */
    int closePaper(Long paperId);

    /** 删除（仅草稿） */
    int deletePaper(Long paperId);
}
