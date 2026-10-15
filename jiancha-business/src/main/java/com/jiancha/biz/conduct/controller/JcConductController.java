package com.jiancha.biz.conduct.controller;

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
import com.jiancha.biz.conduct.domain.ConductActionRequest;
import com.jiancha.biz.conduct.domain.ConductStat;
import com.jiancha.biz.conduct.domain.JcConductIssue;
import com.jiancha.biz.conduct.domain.JcConductRecovery;
import com.jiancha.biz.conduct.service.IJcConductService;

/**
 * 作风建设问题台账 Controller（E5）
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/conduct")
public class JcConductController extends BaseController {

    @Autowired
    private IJcConductService conductService;

    // ═══════════════════════ 问题登记 ═══════════════════════

    /** 登记问题 */
    @PreAuthorize("@ss.hasPermi('jiancha:conduct:add')")
    @Log(title = "作风建设问题登记", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody JcConductIssue issue) {
        return success(conductService.register(issue));
    }

    /** 问题列表 */
    @PreAuthorize("@ss.hasPermi('jiancha:conduct:list')")
    @GetMapping("/list")
    public TableDataInfo list(JcConductIssue query) {
        startPage();
        List<JcConductIssue> list = conductService.selectJcConductIssueList(query);
        return getDataTable(list);
    }

    /** 问题详情 */
    @PreAuthorize("@ss.hasPermi('jiancha:conduct:query')")
    @GetMapping(value = "/{issueId}")
    public AjaxResult getInfo(@PathVariable Long issueId) {
        return success(conductService.selectJcConductIssueByIssueId(issueId));
    }

    /** 按编号查详情 */
    @PreAuthorize("@ss.hasPermi('jiancha:conduct:query')")
    @GetMapping(value = "/no/{issueNo}")
    public AjaxResult getInfoByNo(@PathVariable String issueNo) {
        return success(conductService.selectByIssueNo(issueNo));
    }

    /** 修改登记信息（仅待核查） */
    @PreAuthorize("@ss.hasPermi('jiancha:conduct:edit')")
    @Log(title = "作风建设问题修改", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody JcConductIssue issue) {
        return toAjax(conductService.updateJcConductIssue(issue));
    }

    /** 删除（仅待核查） */
    @PreAuthorize("@ss.hasPermi('jiancha:conduct:remove')")
    @Log(title = "作风建设问题删除", businessType = BusinessType.DELETE)
    @DeleteMapping("/{issueId}")
    public AjaxResult remove(@PathVariable Long issueId) {
        return toAjax(conductService.deleteJcConductIssueByIssueId(issueId));
    }

    // ═══════════════════════ 核查 / 处置 / 转办 ═══════════════════════

    /** 核查登记（0→1） */
    @PreAuthorize("@ss.hasPermi('jiancha:conduct:verify')")
    @Log(title = "作风建设问题核查", businessType = BusinessType.UPDATE)
    @PostMapping("/verify/{issueId}")
    public AjaxResult verify(@PathVariable Long issueId, @RequestBody ConductActionRequest req) {
        return success(conductService.verify(issueId, req));
    }

    /** 处置登记（1→2） */
    @PreAuthorize("@ss.hasPermi('jiancha:conduct:handle')")
    @Log(title = "作风建设问题处置", businessType = BusinessType.UPDATE)
    @PostMapping("/handle/{issueId}")
    public AjaxResult handle(@PathVariable Long issueId, @RequestBody ConductActionRequest req) {
        return success(conductService.handle(issueId, req));
    }

    /** 转整改（关联 E3 整改台账） */
    @PreAuthorize("@ss.hasPermi('jiancha:conduct:transfer')")
    @Log(title = "作风建设问题转整改", businessType = BusinessType.INSERT)
    @PostMapping("/transferRectify/{issueId}")
    public AjaxResult transferRectify(@PathVariable Long issueId, @RequestBody ConductActionRequest req) {
        return success(conductService.transferRectify(issueId, req));
    }

    /** 转处分（写干部处分记录） */
    @PreAuthorize("@ss.hasPermi('jiancha:conduct:transfer')")
    @Log(title = "作风建设问题转处分", businessType = BusinessType.INSERT)
    @PostMapping("/transferPunish/{issueId}")
    public AjaxResult transferPunish(@PathVariable Long issueId, @RequestBody ConductActionRequest req) {
        return success(conductService.transferPunish(issueId, req));
    }

    /** 关闭（0/1/2→3） */
    @PreAuthorize("@ss.hasPermi('jiancha:conduct:close')")
    @Log(title = "作风建设问题关闭", businessType = BusinessType.UPDATE)
    @PostMapping("/close/{issueId}")
    public AjaxResult close(@PathVariable Long issueId, @RequestBody(required = false) ConductActionRequest req) {
        return success(conductService.close(issueId, req));
    }

    /** 作废（0/1/2→4） */
    @PreAuthorize("@ss.hasPermi('jiancha:conduct:cancel')")
    @Log(title = "作风建设问题作废", businessType = BusinessType.UPDATE)
    @PostMapping("/cancel/{issueId}")
    public AjaxResult cancel(@PathVariable Long issueId, @RequestBody(required = false) ConductActionRequest req) {
        return success(conductService.cancel(issueId, req));
    }

    // ═══════════════════════ 辅助与统计 ═══════════════════════

    /** 登记退缴明细（多笔退缴，自动汇总主表退缴额） */
    @PreAuthorize("@ss.hasPermi('jiancha:conduct:recovery')")
    @Log(title = "作风问题退缴登记", businessType = BusinessType.INSERT)
    @PostMapping("/recovery/{issueId}")
    public AjaxResult addRecovery(@PathVariable Long issueId, @RequestBody JcConductRecovery recovery) {
        return success(conductService.addRecovery(issueId, recovery));
    }

    /** 退缴明细列表（按问题） */
    @PreAuthorize("@ss.hasPermi('jiancha:conduct:query')")
    @GetMapping("/recovery/{issueId}")
    public AjaxResult listRecoveries(@PathVariable Long issueId) {
        return success(conductService.listRecoveries(issueId));
    }

    /** 删除退缴登记（同步重算主表退缴额） */
    @PreAuthorize("@ss.hasPermi('jiancha:conduct:recovery')")
    @Log(title = "作风问题退缴删除", businessType = BusinessType.DELETE)
    @DeleteMapping("/recovery/{recoveryId}")
    public AjaxResult deleteRecovery(@PathVariable Long recoveryId) {
        return toAjax(conductService.deleteRecovery(recoveryId));
    }

    /** 人员检索（登记时选人） */
    @PreAuthorize("@ss.hasPermi('jiancha:conduct:query')")
    @GetMapping("/persons")
    public AjaxResult persons(@RequestParam(required = false) String keyword) {
        List<Map<String, Object>> list = conductService.searchPersons(keyword);
        return success(list);
    }

    /** 统计汇总 */
    @PreAuthorize("@ss.hasPermi('jiancha:conduct:stat')")
    @GetMapping("/stat")
    public AjaxResult stat() {
        ConductStat stat = conductService.stat();
        return success(stat);
    }
}
