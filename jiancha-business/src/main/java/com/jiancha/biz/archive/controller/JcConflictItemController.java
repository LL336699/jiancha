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
import com.ruoyi.common.utils.poi.ExcelUtil;
import jakarta.servlet.http.HttpServletResponse;
import com.jiancha.biz.archive.domain.JcConflictItem;
import com.jiancha.biz.archive.service.IJcConflictItemService;
import com.jiancha.biz.archive.service.IJcConflictLogService;

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

    @Autowired
    private IJcConflictLogService logService;

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
            // 段15：删除前先取归属申报单，便于留痕（jc_conflict_log.declare_id 非空）
            JcConflictItem exist = itemService.selectByItemId(itemId);
            rows += itemService.deleteJcConflictItemByItemId(itemId);
            if (rows > 0 && exist != null)
            {
                logService.record(exist.getDeclareId(), "edit",
                        "删除事项明细：" + exist.getItemDesc(), getUsername());
            }
        }
        return toAjax(rows);
    }

    /**
     * 段15：批量插入申报事项明细（申报单与事项一次提交，declareId 走路径）。
     */
    @PreAuthorize("@ss.hasPermi('jiancha:archive:add')")
    @Log(title = "利益冲突申报明细", businessType = BusinessType.INSERT)
    @PostMapping("/batch/{declareId}")
    public AjaxResult batchAdd(@PathVariable Long declareId, @RequestBody List<JcConflictItem> items)
    {
        for (JcConflictItem item : items)
        {
            item.setDeclareId(declareId);
            item.setCreateBy(getUsername());
        }
        int rows = itemService.batchInsert(items);
        if (rows > 0)
        {
            // 段15：事项级变更留痕
            logService.record(declareId, "add", "批量新增事项明细 " + rows + " 条", getUsername());
        }
        return toAjax(rows);
    }

    /**
     * 段15：按申报单全量替换事项明细（事务内先删后插）。
     */
    @PreAuthorize("@ss.hasPermi('jiancha:archive:edit')")
    @Log(title = "利益冲突申报明细", businessType = BusinessType.UPDATE)
    @PutMapping("/replace/{declareId}")
    public AjaxResult replace(@PathVariable Long declareId, @RequestBody List<JcConflictItem> items)
    {
        for (JcConflictItem item : items)
        {
            item.setDeclareId(declareId);
            item.setUpdateBy(getUsername());
        }
        int rows = itemService.replaceItems(declareId, items);
        if (rows > 0)
        {
            // 段15：事项级变更留痕（全量替换后条数即最终明细数）
            logService.record(declareId, "edit", "全量替换事项明细，替换后 " + rows + " 条", getUsername());
        }
        return toAjax(rows);
    }

    /**
     * 段15：按申报单导出事项明细（含黑名单命中列，供呈报使用）。
     */
    @PreAuthorize("@ss.hasPermi('jiancha:archive:list')")
    @Log(title = "利益冲突申报明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export/{declareId}")
    public void export(HttpServletResponse response, @PathVariable Long declareId)
    {
        List<JcConflictItem> list = itemService.selectByDeclareId(declareId);
        ExcelUtil<JcConflictItem> util = new ExcelUtil<>(JcConflictItem.class);
        util.exportExcel(response, list, "申报事项明细");
    }
}
