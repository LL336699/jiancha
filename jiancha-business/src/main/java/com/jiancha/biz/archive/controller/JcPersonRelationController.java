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
import com.jiancha.biz.archive.domain.JcPersonRelation;
import com.jiancha.biz.archive.service.IJcPersonRelationService;

/**
 * 亲属关系及经商办企业情况 Controller
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/archive/relation")
public class JcPersonRelationController extends BaseController
{
    @Autowired
    private IJcPersonRelationService jcPersonRelationService;

    @PreAuthorize("@ss.hasPermi('jiancha:archive:list')")
    @GetMapping("/list")
    public TableDataInfo list(JcPersonRelation jcPersonRelation)
    {
        startPage();
        List<JcPersonRelation> list = jcPersonRelationService.selectJcPersonRelationList(jcPersonRelation);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('jiancha:archive:query')")
    @GetMapping("/byPerson/{personId}")
    public AjaxResult byPerson(@PathVariable Long personId)
    {
        return success(jcPersonRelationService.selectByPersonId(personId));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:archive:query')")
    @GetMapping(value = "/{relationId}")
    public AjaxResult getInfo(@PathVariable Long relationId)
    {
        return success(jcPersonRelationService.selectByRelationId(relationId));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:archive:add')")
    @Log(title = "亲属及经商情况", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody JcPersonRelation jcPersonRelation)
    {
        jcPersonRelation.setCreateBy(getUsername());
        return toAjax(jcPersonRelationService.insertJcPersonRelation(jcPersonRelation));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:archive:edit')")
    @Log(title = "亲属及经商情况", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody JcPersonRelation jcPersonRelation)
    {
        jcPersonRelation.setUpdateBy(getUsername());
        return toAjax(jcPersonRelationService.updateJcPersonRelation(jcPersonRelation));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:archive:remove')")
    @Log(title = "亲属及经商情况", businessType = BusinessType.DELETE)
    @DeleteMapping("/{relationIds}")
    public AjaxResult remove(@PathVariable Long[] relationIds)
    {
        int rows = 0;
        for (Long relationId : relationIds)
        {
            rows += jcPersonRelationService.deleteJcPersonRelationByRelationId(relationId);
        }
        return toAjax(rows);
    }
}
