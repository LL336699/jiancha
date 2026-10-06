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
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.jiancha.biz.archive.domain.JcConflictDeclare;
import com.jiancha.biz.archive.service.IJcConflictDeclareService;

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
        return toAjax(declareService.insertJcConflictDeclare(declare));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:archive:edit')")
    @Log(title = "利益冲突申报", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody JcConflictDeclare declare)
    {
        declare.setUpdateBy(getUsername());
        return toAjax(declareService.updateJcConflictDeclare(declare));
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
        return toAjax(declareService.updateJcConflictDeclare(declare));
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
        }
        return toAjax(rows);
    }
}
