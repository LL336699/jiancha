package com.jiancha.biz.edu.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.jiancha.biz.edu.domain.EduSubmitRequest;
import com.jiancha.biz.edu.domain.JcEduExam;
import com.jiancha.biz.edu.domain.JcEduExamAnswer;
import com.jiancha.biz.edu.service.IJcEduExamService;

/**
 * 廉政教育考试（M9）
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/edu/exam")
public class JcEduExamController extends BaseController {

    @Autowired
    private IJcEduExamService examService;

    @PreAuthorize("@ss.hasPermi('jiancha:edu:exam:list')")
    @GetMapping("/list")
    public TableDataInfo list(JcEduExam query) {
        startPage();
        List<JcEduExam> list = examService.selectExamList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('jiancha:edu:exam:query')")
    @GetMapping("/{examId}")
    public AjaxResult getInfo(@PathVariable Long examId) {
        return success(examService.selectExamDetail(examId));
    }

    /** 开考（返回试卷题目，不含答案） */
    @PreAuthorize("@ss.hasPermi('jiancha:edu:exam:start')")
    @Log(title = "廉政教育开考", businessType = BusinessType.INSERT)
    @PostMapping("/start/{paperId}")
    public AjaxResult start(@PathVariable Long paperId,
                            @RequestParam Long personId,
                            @RequestParam String personName) {
        return success(examService.startExam(paperId, personId, personName));
    }

    /** 交卷评分 */
    @PreAuthorize("@ss.hasPermi('jiancha:edu:exam:submit')")
    @Log(title = "廉政教育交卷", businessType = BusinessType.UPDATE)
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody EduSubmitRequest request) {
        return success(examService.submitExam(request));
    }

    /** 弃考 */
    @PreAuthorize("@ss.hasPermi('jiancha:edu:exam:submit')")
    @Log(title = "廉政教育弃考", businessType = BusinessType.UPDATE)
    @PutMapping("/abandon/{examId}")
    public AjaxResult abandon(@PathVariable Long examId) {
        return toAjax(examService.abandonExam(examId));
    }

    /** 错题本 */
    @PreAuthorize("@ss.hasPermi('jiancha:edu:exam:query')")
    @GetMapping("/wrong/{personId}")
    public AjaxResult wrongBook(@PathVariable Long personId,
                                @RequestParam(defaultValue = "50") int limit) {
        List<JcEduExamAnswer> list = examService.wrongBook(personId, limit);
        return success(list);
    }

    /** 学习进度 */
    @PreAuthorize("@ss.hasPermi('jiancha:edu:exam:query')")
    @GetMapping("/progress/{personId}")
    public AjaxResult progress(@PathVariable Long personId) {
        Map<String, Object> progress = examService.personProgress(personId);
        return success(progress);
    }
}
