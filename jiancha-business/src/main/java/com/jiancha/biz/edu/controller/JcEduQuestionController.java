package com.jiancha.biz.edu.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.jiancha.biz.edu.domain.JcEduQuestion;
import com.jiancha.biz.edu.service.IJcEduQuestionService;

/**
 * 廉政教育题库（M9）
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/edu/question")
public class JcEduQuestionController extends BaseController {

    @Autowired
    private IJcEduQuestionService questionService;

    @PreAuthorize("@ss.hasPermi('jiancha:edu:question:list')")
    @GetMapping("/list")
    public TableDataInfo list(JcEduQuestion query) {
        startPage();
        List<JcEduQuestion> list = questionService.selectQuestionList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('jiancha:edu:question:query')")
    @GetMapping("/{questionId}")
    public AjaxResult getInfo(@PathVariable Long questionId) {
        return success(questionService.selectQuestionById(questionId));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:edu:question:add')")
    @Log(title = "廉政教育题库", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody JcEduQuestion question) {
        return toAjax(questionService.insertQuestion(question));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:edu:question:edit')")
    @Log(title = "廉政教育题库", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody JcEduQuestion question) {
        return toAjax(questionService.updateQuestion(question));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:edu:question:remove')")
    @Log(title = "廉政教育题库", businessType = BusinessType.DELETE)
    @DeleteMapping("/{questionIds}")
    public AjaxResult remove(@PathVariable Long[] questionIds) {
        return toAjax(questionService.deleteQuestions(questionIds));
    }
}
