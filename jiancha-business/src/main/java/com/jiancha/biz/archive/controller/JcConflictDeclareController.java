package com.jiancha.biz.archive.controller;

import java.util.Date;
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
import com.ruoyi.common.utils.poi.ExcelUtil;
import jakarta.servlet.http.HttpServletResponse;
import com.jiancha.biz.archive.domain.JcConflictDeclare;
import com.jiancha.biz.archive.service.IJcConflictDeclareService;
import com.jiancha.biz.archive.service.IJcConflictLogService;

/**
 * 利益冲突申报 Controller
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/archive/declare")
public class JcConflictDeclareController extends BaseController
{
    @Autowired
    private IJcConflictDeclareService declareService;

    @Autowired
    private IJcConflictLogService logService;

    @PreAuthorize("@ss.hasPermi('jiancha:archive:list')")
    @GetMapping("/list")
    public TableDataInfo list(JcConflictDeclare declare)
    {
        startPage();
        List<JcConflictDeclare> list = declareService.selectJcConflictDeclareList(declare);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('jiancha:archive:query')")
    @GetMapping("/byPerson/{personId}")
    public AjaxResult byPerson(@PathVariable Long personId)
    {
        return success(declareService.selectByPersonId(personId));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:archive:query')")
    @GetMapping(value = "/{declareId}")
    public AjaxResult getInfo(@PathVariable Long declareId)
    {
        return success(declareService.selectByDeclareId(declareId));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:archive:add')")
    @Log(title = "利益冲突申报", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody JcConflictDeclare declare)
    {
        declare.setCreateBy(getUsername());
        declareService.insertJcConflictDeclare(declare);
        // 段15：回填自增主键，便于前端紧接着批量保存事项明细
        logService.record(declare.getDeclareId(), "add", "新增利益冲突申报（" + declare.getDeclareType() + "）", getUsername());
        return success(declare.getDeclareId());
    }

    @PreAuthorize("@ss.hasPermi('jiancha:archive:edit')")
    @Log(title = "利益冲突申报", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody JcConflictDeclare declare)
    {
        declare.setUpdateBy(getUsername());
        int rows = declareService.updateJcConflictDeclare(declare);
        if (rows > 0)
        {
            logService.record(declare.getDeclareId(), "edit", "修改申报信息（" + declare.getDeclareType() + "/" + declare.getDeclareYear() + "）", getUsername());
        }
        return toAjax(rows);
    }

    /**
     * 核实申报：写入核实人、核实时间、核实结论与状态。
     */
    @PreAuthorize("@ss.hasPermi('jiancha:archive:edit')")
    @Log(title = "利益冲突申报", businessType = BusinessType.UPDATE)
    @PutMapping("/verify")
    public AjaxResult verify(@RequestBody JcConflictDeclare declare)
    {
        declare.setVerifyBy(getUsername());
        declare.setVerifyTime(new Date());
        declare.setUpdateBy(getUsername());
        int rows = declareService.updateJcConflictDeclare(declare);
        if (rows > 0)
        {
            logService.record(declare.getDeclareId(), "verify", "核实结论：" + declare.getVerifyResult(), getUsername());
        }
        return toAjax(rows);
    }

    @PreAuthorize("@ss.hasPermi('jiancha:archive:remove')")
    @Log(title = "利益冲突申报", businessType = BusinessType.DELETE)
    @DeleteMapping("/{declareIds}")
    public AjaxResult remove(@PathVariable Long[] declareIds)
    {
        int rows = 0;
        for (Long declareId : declareIds)
        {
            rows += declareService.deleteJcConflictDeclareByDeclareId(declareId);
            // 段15：删除留痕（log 表独立留存，单据删除后仍可审计追溯）
            if (rows > 0)
            {
                logService.record(declareId, "delete", "删除利益冲突申报单", getUsername());
            }
        }
        return toAjax(rows);
    }

    /**
     * 段15：申报单变更留痕查询（时间正序，前端时间线抽屉数据源）。
     */
    @PreAuthorize("@ss.hasPermi('jiancha:archive:query')")
    @GetMapping("/log/{declareId}")
    public AjaxResult log(@PathVariable Long declareId)
    {
        return success(logService.selectByDeclareId(declareId));
    }

    /**
     * 段15：年度申报统计（看板：总量/状态分布/类型分布/月度趋势/含冲突事项申报单数）。
     */
    @PreAuthorize("@ss.hasPermi('jiancha:archive:list')")
    @GetMapping("/stat")
    public AjaxResult stat(@RequestParam(required = false) Integer year)
    {
        if (year == null)
        {
            year = java.time.Year.now().getValue();
        }
        return success(declareService.statByYear(year));
    }

    /**
     * 段15：该年度尚无任何申报的在岗人员（集中申报进度督办）。
     */
    @PreAuthorize("@ss.hasPermi('jiancha:archive:list')")
    @GetMapping("/unDeclared")
    public AjaxResult unDeclared(@RequestParam(required = false) Integer year)
    {
        if (year == null)
        {
            year = java.time.Year.now().getValue();
        }
        return success(declareService.selectUnDeclared(year));
    }

    /**
     * 段15：按人员维度聚合申报情况（重点关注人员 Top10）。
     */
    @PreAuthorize("@ss.hasPermi('jiancha:archive:list')")
    @GetMapping("/personStat")
    public AjaxResult personStat(@RequestParam(required = false) Integer year)
    {
        if (year == null)
        {
            year = java.time.Year.now().getValue();
        }
        return success(declareService.statByPerson(year));
    }

    /**
     * 段15：年度申报清单导出（Excel）。
     */
    @PreAuthorize("@ss.hasPermi('jiancha:archive:list')")
    @Log(title = "利益冲突申报", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, @RequestParam(required = false) Integer year)
    {
        if (year == null)
        {
            year = java.time.Year.now().getValue();
        }
        List<JcConflictDeclare> list = declareService.selectDeclareForExport(year);
        ExcelUtil<JcConflictDeclare> util = new ExcelUtil<>(JcConflictDeclare.class);
        util.exportExcel(response, list, "利益冲突申报清单");
    }

    /**
     * 段15：未申报人员督办清单导出（集中申报催办用）。
     */
    @PreAuthorize("@ss.hasPermi('jiancha:archive:list')")
    @Log(title = "利益冲突申报", businessType = BusinessType.EXPORT)
    @PostMapping("/exportUnDeclared")
    public void exportUnDeclared(HttpServletResponse response, @RequestParam(required = false) Integer year)
    {
        if (year == null)
        {
            year = java.time.Year.now().getValue();
        }
        List<com.jiancha.biz.archive.domain.JcPerson> list = declareService.selectUnDeclared(year);
        ExcelUtil<com.jiancha.biz.archive.domain.JcPerson> util = new ExcelUtil<>(com.jiancha.biz.archive.domain.JcPerson.class);
        util.exportExcel(response, list, year + "年度未申报人员督办清单");
    }
}
