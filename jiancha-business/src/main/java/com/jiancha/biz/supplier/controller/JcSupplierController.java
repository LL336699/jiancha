package com.jiancha.biz.supplier.controller;

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

import com.jiancha.biz.supplier.domain.JcSupplier;
import com.jiancha.biz.supplier.domain.JcSupplierBlack;
import com.jiancha.biz.supplier.domain.SupplierActionRequest;
import com.jiancha.biz.supplier.domain.SupplierStat;
import com.jiancha.biz.supplier.service.IJcSupplierService;

/**
 * 供应商廉洁管理 Controller（E4：档案 / 准入审查 / 黑名单 / 统计）
 *
 * <p>权限前缀 jiancha:supplier:*。档案状态机与黑名单全程留痕，
 * 恢复类动作（解除黑名单）必须填意见。</p>
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/supplier")
public class JcSupplierController extends BaseController {

    @Autowired
    private IJcSupplierService supplierService;

    // ═══════════════════════ 档案管理 ═══════════════════════

    /** 供应商列表 */
    @PreAuthorize("@ss.hasPermi('jiancha:supplier:list')")
    @GetMapping("/list")
    public TableDataInfo list(JcSupplier jcSupplier) {
        startPage();
        List<JcSupplier> list = supplierService.selectJcSupplierList(jcSupplier);
        return getDataTable(list);
    }

    /** 供应商详情 */
    @PreAuthorize("@ss.hasPermi('jiancha:supplier:query')")
    @GetMapping(value = "/{supplierId}")
    public AjaxResult getInfo(@PathVariable Long supplierId) {
        return success(supplierService.selectJcSupplierBySupplierId(supplierId));
    }

    /** 新增供应商（初始状态=待审查） */
    @PreAuthorize("@ss.hasPermi('jiancha:supplier:add')")
    @Log(title = "供应商档案", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody JcSupplier jcSupplier) {
        return success(supplierService.insertJcSupplier(jcSupplier));
    }

    /** 修改供应商 */
    @PreAuthorize("@ss.hasPermi('jiancha:supplier:edit')")
    @Log(title = "供应商档案", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody JcSupplier jcSupplier) {
        return toAjax(supplierService.updateJcSupplier(jcSupplier));
    }

    /** 删除供应商（仅待审查状态） */
    @PreAuthorize("@ss.hasPermi('jiancha:supplier:remove')")
    @Log(title = "供应商档案", businessType = BusinessType.DELETE)
    @DeleteMapping("/{supplierId}")
    public AjaxResult remove(@PathVariable Long supplierId) {
        return toAjax(supplierService.deleteJcSupplierBySupplierId(supplierId));
    }

    // ═══════════════════════ 准入审查 ═══════════════════════

    /** 准入审查：auditResult=pass 正常 / observe 观察 / deny 禁入 */
    @PreAuthorize("@ss.hasPermi('jiancha:supplier:audit')")
    @Log(title = "供应商准入审查", businessType = BusinessType.UPDATE)
    @PostMapping("/audit/{supplierId}")
    public AjaxResult audit(@PathVariable Long supplierId, @RequestBody SupplierActionRequest req) {
        return success(supplierService.audit(supplierId, req));
    }

    // ═══════════════════════ 黑名单 ═══════════════════════

    /** 拉黑（blackReason 必填，blackDays 可空=永久） */
    @PreAuthorize("@ss.hasPermi('jiancha:supplier:black')")
    @Log(title = "供应商黑名单", businessType = BusinessType.INSERT)
    @PostMapping("/black/{supplierId}")
    public AjaxResult black(@PathVariable Long supplierId, @RequestBody SupplierActionRequest req) {
        return success(supplierService.black(supplierId, req));
    }

    /** 黑名单列表 */
    @PreAuthorize("@ss.hasPermi('jiancha:supplier:black:list')")
    @GetMapping("/black/list")
    public TableDataInfo blackList(JcSupplierBlack jcSupplierBlack) {
        startPage();
        List<JcSupplierBlack> list = supplierService.selectJcSupplierBlackList(jcSupplierBlack);
        return getDataTable(list);
    }

    /** 黑名单详情 */
    @PreAuthorize("@ss.hasPermi('jiancha:supplier:black:query')")
    @GetMapping(value = "/black/{blackId}")
    public AjaxResult blackInfo(@PathVariable Long blackId) {
        return success(supplierService.selectJcSupplierBlackByBlackId(blackId));
    }

    /** 按供应商查黑名单沿革 */
    @PreAuthorize("@ss.hasPermi('jiancha:supplier:black:list')")
    @GetMapping(value = "/black/bySupplier/{supplierId}")
    public AjaxResult blackBySupplier(@PathVariable Long supplierId) {
        return success(supplierService.selectBlackBySupplierId(supplierId));
    }

    /** 解除黑名单（opinion 必填） */
    @PreAuthorize("@ss.hasPermi('jiancha:supplier:black:lift')")
    @Log(title = "供应商黑名单解除", businessType = BusinessType.UPDATE)
    @PostMapping("/black/lift/{blackId}")
    public AjaxResult lift(@PathVariable Long blackId, @RequestBody SupplierActionRequest req) {
        return success(supplierService.lift(blackId, req));
    }

    // ═══════════════════════ 统计 ═══════════════════════

    /** 廉洁管理统计 */
    @PreAuthorize("@ss.hasPermi('jiancha:supplier:stat')")
    @GetMapping("/stat")
    public AjaxResult stat() {
        return success(supplierService.stat());
    }
}
