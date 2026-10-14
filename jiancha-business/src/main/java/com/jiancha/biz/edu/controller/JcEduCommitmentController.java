package com.jiancha.biz.edu.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.jiancha.biz.edu.domain.JcEduCommitment;
import com.jiancha.biz.edu.service.IJcEduCommitmentService;

/**
 * 廉洁承诺书签收（M9）
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/edu/commitment")
public class JcEduCommitmentController extends BaseController {

    @Autowired
    private IJcEduCommitmentService commitmentService;

    @PreAuthorize("@ss.hasPermi('jiancha:edu:commitment:list')")
    @GetMapping("/list")
    public TableDataInfo list(JcEduCommitment query) {
        startPage();
        List<JcEduCommitment> list = commitmentService.selectCommitmentList(query);
        return getDataTable(list);
    }

    /** 已发布承诺书（签收入口） */
    @PreAuthorize("@ss.hasPermi('jiancha:edu:commitment:sign')")
    @GetMapping("/published")
    public AjaxResult published() {
        return success(commitmentService.selectPublished());
    }

    @PreAuthorize("@ss.hasPermi('jiancha:edu:commitment:query')")
    @GetMapping("/{commitId}")
    public AjaxResult getInfo(@PathVariable Long commitId) {
        return success(commitmentService.selectCommitmentById(commitId));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:edu:commitment:add')")
    @Log(title = "廉洁承诺书", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody JcEduCommitment commitment) {
        return toAjax(commitmentService.insertCommitment(commitment));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:edu:commitment:edit')")
    @Log(title = "廉洁承诺书", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody JcEduCommitment commitment) {
        return toAjax(commitmentService.updateCommitment(commitment));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:edu:commitment:publish')")
    @Log(title = "廉洁承诺书发布", businessType = BusinessType.UPDATE)
    @PutMapping("/publish/{commitId}")
    public AjaxResult publish(@PathVariable Long commitId) {
        return toAjax(commitmentService.publishCommitment(commitId));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:edu:commitment:publish')")
    @Log(title = "廉洁承诺书关闭", businessType = BusinessType.UPDATE)
    @PutMapping("/close/{commitId}")
    public AjaxResult close(@PathVariable Long commitId) {
        return toAjax(commitmentService.closeCommitment(commitId));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:edu:commitment:remove')")
    @Log(title = "廉洁承诺书", businessType = BusinessType.DELETE)
    @DeleteMapping("/{commitId}")
    public AjaxResult remove(@PathVariable Long commitId) {
        return toAjax(commitmentService.deleteCommitment(commitId));
    }

    /** 签收（幂等） */
    @PreAuthorize("@ss.hasPermi('jiancha:edu:commitment:sign')")
    @Log(title = "廉洁承诺书签收", businessType = BusinessType.UPDATE)
    @PostMapping("/sign/{commitId}")
    public AjaxResult sign(@PathVariable Long commitId,
                           @RequestParam Long personId,
                           @RequestParam String personName) {
        return success(commitmentService.sign(commitId, personId, personName));
    }

    /** 签收进度 */
    @PreAuthorize("@ss.hasPermi('jiancha:edu:commitment:query')")
    @GetMapping("/progress/{commitId}")
    public AjaxResult progress(@PathVariable Long commitId) {
        Map<String, Object> progress = commitmentService.signProgress(commitId);
        return success(progress);
    }
}
