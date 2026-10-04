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
import com.jiancha.biz.archive.domain.JcPersonTag;
import com.jiancha.biz.archive.service.IJcPersonTagService;

/**
 * 廉政画像标签 Controller（全部可溯源）
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/archive/tag")
public class JcPersonTagController extends BaseController
{
    @Autowired
    private IJcPersonTagService jcPersonTagService;

    /** 标签列表（可按 personId / tagType 过滤） */
    @PreAuthorize("@ss.hasPermi('jiancha:archive:list')")
    @GetMapping("/list")
    public TableDataInfo list(JcPersonTag jcPersonTag)
    {
        startPage();
        List<JcPersonTag> list = jcPersonTagService.selectJcPersonTagList(jcPersonTag);
        return getDataTable(list);
    }

    /** 某人员的全部标签 */
    @PreAuthorize("@ss.hasPermi('jiancha:archive:query')")
    @GetMapping("/byPerson/{personId}")
    public AjaxResult byPerson(@PathVariable Long personId)
    {
        return success(jcPersonTagService.selectByPersonId(personId));
    }

    /** 标签详情 */
    @PreAuthorize("@ss.hasPermi('jiancha:archive:query')")
    @GetMapping(value = "/{tagId}")
    public AjaxResult getInfo(@PathVariable Long tagId)
    {
        return success(jcPersonTagService.selectByTagId(tagId));
    }

    /** 新增标签（建议填写 sourceTable/sourceId 保证可溯源） */
    @PreAuthorize("@ss.hasPermi('jiancha:archive:add')")
    @Log(title = "廉政画像标签", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody JcPersonTag jcPersonTag)
    {
        jcPersonTag.setCreateBy(getUsername());
        return toAjax(jcPersonTagService.insertJcPersonTag(jcPersonTag));
    }

    /** 修改标签 */
    @PreAuthorize("@ss.hasPermi('jiancha:archive:edit')")
    @Log(title = "廉政画像标签", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody JcPersonTag jcPersonTag)
    {
        jcPersonTag.setUpdateBy(getUsername());
        return toAjax(jcPersonTagService.updateJcPersonTag(jcPersonTag));
    }

    /** 删除标签（逻辑删除） */
    @PreAuthorize("@ss.hasPermi('jiancha:archive:remove')")
    @Log(title = "廉政画像标签", businessType = BusinessType.DELETE)
    @DeleteMapping("/{tagIds}")
    public AjaxResult remove(@PathVariable Long[] tagIds)
    {
        int rows = 0;
        for (Long tagId : tagIds)
        {
            rows += jcPersonTagService.deleteJcPersonTagByTagId(tagId);
        }
        return toAjax(rows);
    }
}
