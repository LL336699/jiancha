package com.jiancha.biz.rectify.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

import com.jiancha.biz.rectify.domain.JcRectifyIssue;
import com.jiancha.biz.rectify.domain.JcRectifyReview;
import com.jiancha.biz.rectify.domain.JcRectifyTask;
import com.jiancha.biz.rectify.domain.RectifyActionRequest;
import com.jiancha.biz.rectify.domain.RectifyStat;
import com.jiancha.biz.rectify.service.IJcRectifyService;

/**
 * 整改闭环台账 Controller（E3）
 *
 * <p>权限前缀 jiancha:rectify:*。涵盖问题登记（含举报一键转整改）、整改措施管理、
 * 状态机流转（开始/提交复核/销号/退回/回头看/作废）、复核记录与统计。</p>
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/rectify")
public class JcRectifyController extends BaseController {

    @Autowired
    private IJcRectifyService rectifyService;

    /** 问题列表（分页） */
    @PreAuthorize("@ss.hasPermi('jiancha:rectify:list')")
    @GetMapping("/list")
    public TableDataInfo list(JcRectifyIssue jcRectifyIssue) {
        startPage();
        List<JcRectifyIssue> list = rectifyService.selectJcRectifyIssueList(jcRectifyIssue);
        return getDataTable(list);
    }

    /** 问题详情 */
    @PreAuthorize("@ss.hasPermi('jiancha:rectify:query')")
    @GetMapping(value = "/{issueId}")
    public AjaxResult getInfo(@PathVariable Long issueId) {
        return success(rectifyService.selectJcRectifyIssueByIssueId(issueId));
    }

    /** 登记问题 */
    @PreAuthorize("@ss.hasPermi('jiancha:rectify:add')")
    @Log(title = "整改问题台账", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody JcRectifyIssue jcRectifyIssue) {
        return success(rectifyService.register(jcRectifyIssue));
    }

    /** 从舞弊举报一键登记整改问题（幂等） */
    @PreAuthorize("@ss.hasPermi('jiancha:rectify:add')")
    @Log(title = "整改问题台账", businessType = BusinessType.INSERT)
    @PostMapping("/fromPetition")
    public AjaxResult fromPetition(@RequestParam Long petitionId,
                                   @RequestParam Long targetDeptId,
                                   @RequestParam(required = false) String problemType,
                                   @RequestParam(required = false) String severity) {
        return success(rectifyService.registerFromPetition(petitionId, targetDeptId, problemType, severity));
    }

    /** 修改问题 */
    @PreAuthorize("@ss.hasPermi('jiancha:rectify:edit')")
    @Log(title = "整改问题台账", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody JcRectifyIssue jcRectifyIssue) {
        return toAjax(rectifyService.updateJcRectifyIssue(jcRectifyIssue));
    }

    /** 办理流转（状态机） */
    @PreAuthorize("@ss.hasPermi('jiancha:rectify:action')")
    @Log(title = "整改问题台账", businessType = BusinessType.UPDATE)
    @PostMapping("/action")
    public AjaxResult action(@Validated @RequestBody RectifyActionRequest req,
                             @RequestParam Long issueId) {
        return success(rectifyService.handleAction(issueId, req));
    }

    /** 删除问题（级联措施与复核记录；正式数据建议只销号不删除） */
    @PreAuthorize("@ss.hasPermi('jiancha:rectify:remove')")
    @Log(title = "整改问题台账", businessType = BusinessType.DELETE)
    @DeleteMapping("/{issueId}")
    public AjaxResult remove(@PathVariable Long issueId) {
        return toAjax(rectifyService.deleteJcRectifyIssueByIssueId(issueId));
    }

    // ═══════════════════════ 整改措施 ═══════════════════════

    /** 措施列表 */
    @PreAuthorize("@ss.hasPermi('jiancha:rectify:query')")
    @GetMapping("/task/{issueId}")
    public AjaxResult taskList(@PathVariable Long issueId) {
        List<JcRectifyTask> list = rectifyService.selectTaskByIssueId(issueId);
        return success(list);
    }

    /** 新增措施 */
    @PreAuthorize("@ss.hasPermi('jiancha:rectify:task')")
    @Log(title = "整改措施", businessType = BusinessType.INSERT)
    @PostMapping("/task")
    public AjaxResult addTask(@Validated @RequestBody JcRectifyTask task) {
        return toAjax(rectifyService.insertTask(task));
    }

    /** 更新措施 */
    @PreAuthorize("@ss.hasPermi('jiancha:rectify:task')")
    @Log(title = "整改措施", businessType = BusinessType.UPDATE)
    @PutMapping("/task")
    public AjaxResult editTask(@Validated @RequestBody JcRectifyTask task) {
        return toAjax(rectifyService.updateTask(task));
    }

    /** 删除措施 */
    @PreAuthorize("@ss.hasPermi('jiancha:rectify:task')")
    @Log(title = "整改措施", businessType = BusinessType.DELETE)
    @DeleteMapping("/task/{taskId}")
    public AjaxResult removeTask(@PathVariable Long taskId) {
        return toAjax(rectifyService.deleteTaskByTaskId(taskId));
    }

    // ═══════════════════════ 复核记录 ═══════════════════════

    /** 复核/回头看记录 */
    @PreAuthorize("@ss.hasPermi('jiancha:rectify:query')")
    @GetMapping("/review/{issueId}")
    public AjaxResult reviewList(@PathVariable Long issueId) {
        List<JcRectifyReview> list = rectifyService.selectReviewByIssueId(issueId);
        return success(list);
    }

    /** 整改统计 */
    @PreAuthorize("@ss.hasPermi('jiancha:rectify:stat')")
    @GetMapping("/stat")
    public AjaxResult stat() {
        RectifyStat stat = rectifyService.stat();
        return success(stat);
    }
}
