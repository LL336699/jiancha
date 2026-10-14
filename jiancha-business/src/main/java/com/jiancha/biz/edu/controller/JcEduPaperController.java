package com.jiancha.biz.edu.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.jiancha.biz.edu.domain.EduRandomPaperRequest;
import com.jiancha.biz.edu.domain.JcEduPaper;
import com.jiancha.biz.edu.service.IJcEduPaperService;

/**
 * 廉政教育试卷（M9）
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/edu/paper")
public class JcEduPaperController extends BaseController {

    @Autowired
    private IJcEduPaperService paperService;

    @PreAuthorize("@ss.hasPermi('jiancha:edu:paper:list')")
    @GetMapping("/list")
    public TableDataInfo list(JcEduPaper query) {
        startPage();
        List<JcEduPaper> list = paperService.selectPaperList(query);
        return getDataTable(list);
    }

    /** 已发布试卷（考试入口，无答案） */
    @PreAuthorize("@ss.hasPermi('jiancha:edu:exam:start')")
    @GetMapping("/published")
    public AjaxResult published() {
        return success(paperService.selectPublishedPapers());
    }

    @PreAuthorize("@ss.hasPermi('jiancha:edu:paper:query')")
    @GetMapping("/{paperId}")
    public AjaxResult getInfo(@PathVariable Long paperId) {
        return success(paperService.selectPaperById(paperId));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:edu:paper:add')")
    @Log(title = "廉政教育试卷", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody JcEduPaper paper) {
        return toAjax(paperService.insertPaper(paper));
    }

    /** 随机组卷（rules 为抽题策略） */
    @PreAuthorize("@ss.hasPermi('jiancha:edu:paper:random')")
    @Log(title = "廉政教育随机组卷", businessType = BusinessType.INSERT)
    @PostMapping("/random")
    public AjaxResult random(@RequestBody EduRandomPaperRequest request) {
        return success(paperService.randomPaper(request.getPaper(), request.getRules()));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:edu:paper:edit')")
    @Log(title = "廉政教育试卷", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody JcEduPaper paper) {
        return toAjax(paperService.updatePaper(paper));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:edu:paper:publish')")
    @Log(title = "廉政教育试卷发布", businessType = BusinessType.UPDATE)
    @PutMapping("/publish/{paperId}")
    public AjaxResult publish(@PathVariable Long paperId) {
        return toAjax(paperService.publishPaper(paperId));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:edu:paper:publish')")
    @Log(title = "廉政教育试卷关闭", businessType = BusinessType.UPDATE)
    @PutMapping("/close/{paperId}")
    public AjaxResult close(@PathVariable Long paperId) {
        return toAjax(paperService.closePaper(paperId));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:edu:paper:remove')")
    @Log(title = "廉政教育试卷", businessType = BusinessType.DELETE)
    @DeleteMapping("/{paperId}")
    public AjaxResult remove(@PathVariable Long paperId) {
        return toAjax(paperService.deletePaper(paperId));
    }
}
