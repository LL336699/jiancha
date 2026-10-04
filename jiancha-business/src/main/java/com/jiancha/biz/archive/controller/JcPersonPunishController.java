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
import com.jiancha.biz.archive.domain.JcPersonPunish;
import com.jiancha.biz.archive.service.IJcPersonPunishService;

/**
 * 处分记录 Controller
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/archive/punish")
public class JcPersonPunishController extends BaseController
{
    @Autowired
    private IJcPersonPunishService jcPersonPunishService;

    @PreAuthorize("@ss.hasPermi('jiancha:archive:list')")
    @GetMapping("/list")
    public TableDataInfo list(JcPersonPunish jcPersonPunish)
    {
        startPage();
        List<JcPersonPunish> list = jcPersonPunishService.selectJcPersonPunishList(jcPersonPunish);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('jiancha:archive:query')")
    @GetMapping("/byPerson/{personId}")
    public AjaxResult byPerson(@PathVariable Long personId)
    {
        return success(jcPersonPunishService.selectByPersonId(personId));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:archive:query')")
    @GetMapping(value = "/{punishId}")
    public AjaxResult getInfo(@PathVariable Long punishId)
    {
        return success(jcPersonPunishService.selectByPunishId(punishId));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:archive:add')")
    @Log(title = "处分记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody JcPersonPunish jcPersonPunish)
    {
        jcPersonPunish.setCreateBy(getUsername());
        return toAjax(jcPersonPunishService.insertJcPersonPunish(jcPersonPunish));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:archive:edit')")
    @Log(title = "处分记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody JcPersonPunish jcPersonPunish)
    {
        jcPersonPunish.setUpdateBy(getUsername());
        return toAjax(jcPersonPunishService.updateJcPersonPunish(jcPersonPunish));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:archive:remove')")
    @Log(title = "处分记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{punishIds}")
    public AjaxResult remove(@PathVariable Long[] punishIds)
    {
        int rows = 0;
        for (Long punishId : punishIds)
        {
            rows += jcPersonPunishService.deleteJcPersonPunishByPunishId(punishId);
        }
        return toAjax(rows);
    }
}
