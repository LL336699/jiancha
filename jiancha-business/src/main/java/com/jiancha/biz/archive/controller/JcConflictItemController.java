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
import com.jiancha.biz.archive.domain.JcConflictItem;
import com.jiancha.biz.archive.service.IJcConflictItemService;

/**
 * 利益冲突申报事项明细 Controller
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/archive/declareItem")
public class JcConflictItemController extends BaseController
{
    @Autowired
    private IJcConflictItemService itemService;

    @PreAuthorize("@ss.hasPermi('jiancha:archive:list')")
    @GetMapping("/list")
    public TableDataInfo list(Long declareId)
    {
        startPage();
        List<JcConflictItem> list = itemService.selectByDeclareId(declareId);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('jiancha:archive:query')")
    @GetMapping("/byDeclare/{declareId}")
    public AjaxResult byDeclare(@PathVariable Long declareId)
    {
        return success(itemService.selectByDeclareId(declareId));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:archive:query')")
    @GetMapping(value = "/{itemId}")
    public AjaxResult getInfo(@PathVariable Long itemId)
    {
        return success(itemService.selectByItemId(itemId));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:archive:add')")
    @Log(title = "利益冲突申报明细", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody JcConflictItem item)
    {
        item.setCreateBy(getUsername());
        return toAjax(itemService.insertJcConflictItem(item));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:archive:edit')")
    @Log(title = "利益冲突申报明细", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody JcConflictItem item)
    {
        item.setUpdateBy(getUsername());
        return toAjax(itemService.updateJcConflictItem(item));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:archive:remove')")
    @Log(title = "利益冲突申报明细", businessType = BusinessType.DELETE)
    @DeleteMapping("/{itemIds}")
    public AjaxResult remove(@PathVariable Long[] itemIds)
    {
        int rows = 0;
        for (Long itemId : itemIds)
        {
            rows += itemService.deleteJcConflictItemByItemId(itemId);
        }
        return toAjax(rows);
    }
}
