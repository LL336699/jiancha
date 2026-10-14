package com.jiancha.biz.supplier.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

import com.jiancha.biz.supplier.domain.AgreementRenewRequest;
import com.jiancha.biz.supplier.domain.JcSupplierAgreement;
import com.jiancha.biz.supplier.service.IJcSupplierAgreementService;

/**
 * 供应商廉洁协议 Controller（E4）
 *
 * <p>权限前缀 jiancha:supplier:agreement:*。签署 / 续签 / 解除全生命周期，
 * 临期与过期状态由 Quartz 日调度刷新，页面按状态展示。</p>
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/supplier/agreement")
public class JcSupplierAgreementController extends BaseController {

    @Autowired
    private IJcSupplierAgreementService agreementService;

    /** 协议列表（含供应商名称） */
    @PreAuthorize("@ss.hasPermi('jiancha:supplier:agreement:list')")
    @GetMapping("/list")
    public TableDataInfo list(JcSupplierAgreement jcSupplierAgreement) {
        startPage();
        List<JcSupplierAgreement> list = agreementService.selectJcSupplierAgreementList(jcSupplierAgreement);
        return getDataTable(list);
    }

    /** 协议详情 */
    @PreAuthorize("@ss.hasPermi('jiancha:supplier:agreement:query')")
    @GetMapping(value = "/{agreementId}")
    public AjaxResult getInfo(@PathVariable Long agreementId) {
        return success(agreementService.selectJcSupplierAgreementByAgreementId(agreementId));
    }

    /** 按供应商查协议沿革 */
    @PreAuthorize("@ss.hasPermi('jiancha:supplier:agreement:list')")
    @GetMapping(value = "/bySupplier/{supplierId}")
    public AjaxResult bySupplier(@PathVariable Long supplierId) {
        return success(agreementService.selectBySupplierId(supplierId));
    }

    /** 签署协议（expireDate 必填且晚于签署日） */
    @PreAuthorize("@ss.hasPermi('jiancha:supplier:agreement:add')")
    @Log(title = "供应商廉洁协议", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody JcSupplierAgreement jcSupplierAgreement) {
        return success(agreementService.sign(jcSupplierAgreement));
    }

    /** 续签（newExpireDate 必填） */
    @PreAuthorize("@ss.hasPermi('jiancha:supplier:agreement:renew')")
    @Log(title = "供应商廉洁协议续签", businessType = BusinessType.UPDATE)
    @PostMapping("/renew/{agreementId}")
    public AjaxResult renew(@PathVariable Long agreementId, @RequestBody AgreementRenewRequest req) {
        return success(agreementService.renew(agreementId, req.getNewExpireDate(), req.getOpinion()));
    }

    /** 解除协议 */
    @PreAuthorize("@ss.hasPermi('jiancha:supplier:agreement:lift')")
    @Log(title = "供应商廉洁协议解除", businessType = BusinessType.UPDATE)
    @PostMapping("/lift/{agreementId}")
    public AjaxResult lift(@PathVariable Long agreementId) {
        return toAjax(agreementService.lift(agreementId));
    }

    /** 删除协议 */
    @PreAuthorize("@ss.hasPermi('jiancha:supplier:agreement:remove')")
    @Log(title = "供应商廉洁协议", businessType = BusinessType.DELETE)
    @DeleteMapping("/{agreementId}")
    public AjaxResult remove(@PathVariable Long agreementId) {
        return toAjax(agreementService.deleteJcSupplierAgreementByAgreementId(agreementId));
    }
}
