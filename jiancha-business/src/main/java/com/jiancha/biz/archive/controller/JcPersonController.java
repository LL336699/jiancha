package com.jiancha.biz.archive.controller;

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
import com.jiancha.biz.archive.domain.JcPerson;
import com.jiancha.biz.archive.service.IJcPersonService;

/**
 * 干部廉政档案 Controller
 *
 * <p>权限前缀 jiancha:archive:*（设计文档 §4 权限矩阵）。</p>
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/archive/person")
public class JcPersonController extends BaseController
{
    @Autowired
    private IJcPersonService jcPersonService;

    /** 列表（分页） */
    @PreAuthorize("@ss.hasPermi('jiancha:archive:list')")
    @GetMapping("/list")
    public TableDataInfo list(JcPerson jcPerson)
    {
        startPage();
        List<JcPerson> list = jcPersonService.selectJcPersonList(jcPerson);
        return getDataTable(list);
    }

    /** 详情 */
    @PreAuthorize("@ss.hasPermi('jiancha:archive:query')")
    @GetMapping(value = "/{personId}")
    public AjaxResult getInfo(@PathVariable Long personId)
    {
        return success(jcPersonService.selectJcPersonByPersonId(personId));
    }

    /** 新增 */
    @PreAuthorize("@ss.hasPermi('jiancha:archive:add')")
    @Log(title = "廉政档案", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody JcPerson jcPerson)
    {
        jcPerson.setCreateBy(getUsername());
        return toAjax(jcPersonService.insertJcPerson(jcPerson));
    }

    /** 修改 */
    @PreAuthorize("@ss.hasPermi('jiancha:archive:edit')")
    @Log(title = "廉政档案", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody JcPerson jcPerson)
    {
        jcPerson.setUpdateBy(getUsername());
        return toAjax(jcPersonService.updateJcPerson(jcPerson));
    }

    /** 删除（逻辑删除，支持批量） */
    @PreAuthorize("@ss.hasPermi('jiancha:archive:remove')")
    @Log(title = "廉政档案", businessType = BusinessType.DELETE)
    @DeleteMapping("/{personIds}")
    public AjaxResult remove(@PathVariable Long[] personIds)
    {
        int rows = 0;
        for (Long personId : personIds)
        {
            rows += jcPersonService.deleteJcPersonByPersonId(personId);
        }
        return toAjax(rows);
    }
}
