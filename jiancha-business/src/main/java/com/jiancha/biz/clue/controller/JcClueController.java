package com.jiancha.biz.clue.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

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
import com.ruoyi.common.utils.poi.ExcelUtil;

import com.jiancha.biz.clue.domain.ClueActionRequest;
import com.jiancha.biz.clue.domain.JcClue;
import com.jiancha.biz.clue.service.IJcClueService;
import com.jiancha.biz.clue.service.IJcClueTraceService;

/**
 * 问题线索 Controller（M4）
 *
 * <p>权限前缀 jiancha:clue:*。涵盖线索录入、分办流转、时限红黄灯、重复检测与统计。</p>
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/clue")
public class JcClueController extends BaseController {

    @Autowired
    private IJcClueService clueService;

    @Autowired
    private IJcClueTraceService traceService;

    /** 线索列表（分页） */
    @PreAuthorize("@ss.hasPermi('jiancha:clue:list')")
    @GetMapping("/list")
    public TableDataInfo list(JcClue jcClue) {
        startPage();
        List<JcClue> list = clueService.selectJcClueList(jcClue);
        return getDataTable(list);
    }

    /** 线索详情 */
    @PreAuthorize("@ss.hasPermi('jiancha:clue:query')")
    @GetMapping(value = "/{clueId}")
    public AjaxResult getInfo(@PathVariable Long clueId) {
        return success(clueService.selectJcClueByClueId(clueId));
    }

    /** 新增线索 */
    @PreAuthorize("@ss.hasPermi('jiancha:clue:add')")
    @Log(title = "问题线索", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody JcClue jcClue) {
        jcClue.setCreateBy(getUsername());
        return toAjax(clueService.insertJcClue(jcClue));
    }

    /** 修改线索 */
    @PreAuthorize("@ss.hasPermi('jiancha:clue:edit')")
    @Log(title = "问题线索", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody JcClue jcClue) {
        jcClue.setUpdateBy(getUsername());
        return toAjax(clueService.updateJcClue(jcClue));
    }

    /** 删除线索（逻辑删除，支持批量） */
    @PreAuthorize("@ss.hasPermi('jiancha:clue:remove')")
    @Log(title = "问题线索", businessType = BusinessType.DELETE)
    @DeleteMapping("/{clueIds}")
    public AjaxResult remove(@PathVariable Long[] clueIds) {
        int rows = 0;
        for (Long clueId : clueIds) {
            rows += clueService.deleteJcClueByClueId(clueId);
        }
        return toAjax(rows);
    }

    /** 线索流转（受理 / 研判 / 分办 / 处置 / 办结 / 退回 / 不予受理） */
    @PreAuthorize("@ss.hasPermi('jiancha:clue:handle')")
    @Log(title = "问题线索流转", businessType = BusinessType.UPDATE)
    @PostMapping("/handle/{clueId}")
    public AjaxResult handle(@PathVariable Long clueId, @RequestBody ClueActionRequest req) {
        return success(clueService.handleAction(clueId, req));
    }

    /** 线索办理轨迹（全程留痕） */
    @PreAuthorize("@ss.hasPermi('jiancha:clue:query')")
    @GetMapping("/trace/{clueId}")
    public AjaxResult trace(@PathVariable Long clueId) {
        return success(traceService.selectTraceByClueId(clueId));
    }

    /** 重复线索检测：同一被反映人在时间窗口内的其它线索 */
    @PreAuthorize("@ss.hasPermi('jiancha:clue:query')")
    @GetMapping("/duplicate")
    public AjaxResult duplicate(@RequestParam Long personId,
                                @RequestParam(required = false) Long excludeClueId,
                                @RequestParam(required = false, defaultValue = "90") Integer days) {
        return success(clueService.detectDuplicate(personId, excludeClueId, days));
    }

    /** 预览下一个线索编号 */
    @PreAuthorize("@ss.hasPermi('jiancha:clue:add')")
    @GetMapping("/nextNo")
    public AjaxResult nextNo() {
        return success(clueService.generateClueNo());
    }

    /** 统计总览（看板） */
    @PreAuthorize("@ss.hasPermi('jiancha:clue:list')")
    @GetMapping("/stat/overview")
    public AjaxResult statOverview() {
        return success(clueService.statOverview());
    }

    /** 手动刷新红黄灯预警 */
    @PreAuthorize("@ss.hasPermi('jiancha:clue:edit')")
    @PostMapping("/refreshWarn")
    public AjaxResult refreshWarn() {
        return success(clueService.refreshWarnLevel());
    }

    /** 导出线索清单 */
    @PreAuthorize("@ss.hasPermi('jiancha:clue:export')")
    @Log(title = "问题线索", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, JcClue jcClue) {
        List<JcClue> list = clueService.selectJcClueList(jcClue);
        ExcelUtil<JcClue> util = new ExcelUtil<>(JcClue.class);
        util.exportExcel(response, list, "问题线索数据");
    }
}
