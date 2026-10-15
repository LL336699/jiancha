package com.jiancha.biz.reply.controller;

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

import com.jiancha.biz.reply.domain.JcReplyRequest;
import com.jiancha.biz.reply.domain.JcReplyScreenItem;
import com.jiancha.biz.reply.domain.ReplyActionRequest;
import com.jiancha.biz.reply.domain.ReplyStat;
import com.jiancha.biz.reply.service.IJcReplyService;

/**
 * 廉政意见回复 Controller（E2）
 *
 * <p>权限前缀 jiancha:reply:*。涵盖申请登记、系统自动筛查（跨六大模块聚合）、
 * 人工核实出具正式结论（无违纪/暂缓/否决）、关闭、筛查明细与统计。</p>
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/reply")
public class JcReplyController extends BaseController {

    @Autowired
    private IJcReplyService replyService;

    /** 申请列表（分页） */
    @PreAuthorize("@ss.hasPermi('jiancha:reply:list')")
    @GetMapping("/list")
    public TableDataInfo list(JcReplyRequest jcReplyRequest) {
        startPage();
        List<JcReplyRequest> list = replyService.selectJcReplyRequestList(jcReplyRequest);
        return getDataTable(list);
    }

    /** 申请详情 */
    @PreAuthorize("@ss.hasPermi('jiancha:reply:query')")
    @GetMapping(value = "/{requestId}")
    public AjaxResult getInfo(@PathVariable Long requestId) {
        return success(replyService.selectJcReplyRequestByRequestId(requestId));
    }

    /** 登记申请（自动编号，同人同事项进行中不可重复） */
    @PreAuthorize("@ss.hasPermi('jiancha:reply:add')")
    @Log(title = "廉政意见回复", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody JcReplyRequest jcReplyRequest) {
        return success(replyService.register(jcReplyRequest));
    }

    /** 修改登记信息（仅待筛查状态） */
    @PreAuthorize("@ss.hasPermi('jiancha:reply:edit')")
    @Log(title = "廉政意见回复", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody JcReplyRequest jcReplyRequest) {
        return toAjax(replyService.updateJcReplyRequest(jcReplyRequest));
    }

    /** 系统自动筛查（0 → 1，输出命中明细与建议结论） */
    @PreAuthorize("@ss.hasPermi('jiancha:reply:screen')")
    @Log(title = "廉政意见回复", businessType = BusinessType.UPDATE)
    @PostMapping("/screen/{requestId}")
    public AjaxResult screen(@PathVariable Long requestId) {
        return success(replyService.screenRequest(requestId));
    }

    /** 出具正式结论（1 → 2，人工核实后） */
    @PreAuthorize("@ss.hasPermi('jiancha:reply:reply')")
    @Log(title = "廉政意见回复", businessType = BusinessType.UPDATE)
    @PostMapping("/reply/{requestId}")
    public AjaxResult reply(@PathVariable Long requestId,
                            @Validated @RequestBody ReplyActionRequest req) {
        return success(replyService.reply(requestId, req));
    }

    /** 关闭申请（0/1 → 3） */
    @PreAuthorize("@ss.hasPermi('jiancha:reply:close')")
    @Log(title = "廉政意见回复", businessType = BusinessType.UPDATE)
    @PostMapping("/close/{requestId}")
    public AjaxResult close(@PathVariable Long requestId) {
        return success(replyService.close(requestId));
    }

    /** 筛查明细 */
    @PreAuthorize("@ss.hasPermi('jiancha:reply:query')")
    @GetMapping("/screenItems/{requestId}")
    public AjaxResult screenItems(@PathVariable Long requestId) {
        List<JcReplyScreenItem> list = replyService.selectScreenItems(requestId);
        return success(list);
    }

    /** 批量系统筛查（成批征求意见；单条不合资格跳过并记入 skipped） */
    @PreAuthorize("@ss.hasPermi('jiancha:reply:screen')")
    @Log(title = "廉政意见回复", businessType = BusinessType.UPDATE)
    @PostMapping("/screenBatch")
    public AjaxResult screenBatch(@RequestBody List<Long> requestIds) {
        return success(replyService.screenBatch(requestIds));
    }

    /** 人员检索（登记选人：姓名/编号模糊） */
    @PreAuthorize("@ss.hasPermi('jiancha:reply:add')")
    @GetMapping("/persons")
    public AjaxResult persons(@RequestParam(required = false) String keyword) {
        return success(replyService.searchPersons(keyword));
    }

    /** 删除申请（级联筛查明细） */
    @PreAuthorize("@ss.hasPermi('jiancha:reply:remove')")
    @Log(title = "廉政意见回复", businessType = BusinessType.DELETE)
    @DeleteMapping("/{requestId}")
    public AjaxResult remove(@PathVariable Long requestId) {
        return toAjax(replyService.deleteJcReplyRequestByRequestId(requestId));
    }

    /** 回复统计 */
    @PreAuthorize("@ss.hasPermi('jiancha:reply:stat')")
    @GetMapping("/stat")
    public AjaxResult stat() {
        ReplyStat stat = replyService.stat();
        return success(stat);
    }
}
