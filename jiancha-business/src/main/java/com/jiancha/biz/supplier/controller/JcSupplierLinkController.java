package com.jiancha.biz.supplier.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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

import com.jiancha.biz.supplier.domain.JcSupplierLink;
import com.jiancha.biz.supplier.domain.SupplierActionRequest;
import com.jiancha.biz.supplier.service.IJcSupplierLinkService;

/**
 * 供应商关联排查 Controller（E4）
 *
 * <p>权限前缀 jiancha:supplier:link:*。人工登记 + 系统比对（Quartz 日调度）
 * 两类入口；处置 observe/deny 联动档案状态；toRectify 一键转 E3 整改闭环。</p>
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/supplier/link")
public class JcSupplierLinkController extends BaseController {

    @Autowired
    private IJcSupplierLinkService linkService;

    /** 关联排查列表（含供应商名称） */
    @PreAuthorize("@ss.hasPermi('jiancha:supplier:link:list')")
    @GetMapping("/list")
    public TableDataInfo list(JcSupplierLink jcSupplierLink) {
        startPage();
        List<JcSupplierLink> list = linkService.selectJcSupplierLinkList(jcSupplierLink);
        return getDataTable(list);
    }

    /** 关联详情 */
    @PreAuthorize("@ss.hasPermi('jiancha:supplier:link:query')")
    @GetMapping(value = "/{linkId}")
    public AjaxResult getInfo(@PathVariable Long linkId) {
        return success(linkService.selectJcSupplierLinkByLinkId(linkId));
    }

    /** 按供应商查关联记录 */
    @PreAuthorize("@ss.hasPermi('jiancha:supplier:link:list')")
    @GetMapping(value = "/bySupplier/{supplierId}")
    public AjaxResult bySupplier(@PathVariable Long supplierId) {
        return success(linkService.selectBySupplierId(supplierId));
    }

    /** 人工登记关联 */
    @PreAuthorize("@ss.hasPermi('jiancha:supplier:link:add')")
    @Log(title = "供应商关联排查", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody JcSupplierLink jcSupplierLink) {
        return success(linkService.add(jcSupplierLink));
    }

    /** 关联处置：handleResult=normal 正常 / observe 观察 / deny 禁入 / clear 解除 */
    @PreAuthorize("@ss.hasPermi('jiancha:supplier:link:handle')")
    @Log(title = "供应商关联处置", businessType = BusinessType.UPDATE)
    @PostMapping("/handle/{linkId}")
    public AjaxResult handle(@PathVariable Long linkId, @RequestBody SupplierActionRequest req) {
        return success(linkService.handle(linkId, req));
    }

    /** 一键转整改（targetDeptId 必填） */
    @PreAuthorize("@ss.hasPermi('jiancha:supplier:link:rectify')")
    @Log(title = "供应商关联转整改", businessType = BusinessType.INSERT)
    @PostMapping("/toRectify/{linkId}")
    public AjaxResult toRectify(@PathVariable Long linkId, @RequestBody SupplierActionRequest req) {
        return success(linkService.toRectify(linkId, req));
    }

    /** 手动触发系统比对（E1 申报 × 供应商库） */
    @PreAuthorize("@ss.hasPermi('jiancha:supplier:link:scan')")
    @Log(title = "供应商关联系统比对", businessType = BusinessType.INSERT)
    @PostMapping("/scan")
    public AjaxResult scan() {
        int created = linkService.scanMatch();
        return success("系统比对完成，新增关联记录 " + created + " 条");
    }
}
