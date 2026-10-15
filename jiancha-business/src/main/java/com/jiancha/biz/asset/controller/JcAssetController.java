package com.jiancha.biz.asset.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jiancha.biz.asset.domain.AssetActionRequest;
import com.jiancha.biz.asset.domain.AssetStat;
import com.jiancha.biz.asset.domain.JcAssetFlow;
import com.jiancha.biz.asset.domain.JcAssetInfo;
import com.jiancha.biz.asset.service.IJcAssetService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

/**
 * 涉案财物管理（段11 E6）
 *
 * 权限前缀：jiancha:asset:*
 */
@RestController
@RequestMapping("/jiancha/asset")
public class JcAssetController extends BaseController
{
    @Autowired
    private IJcAssetService assetService;

    // ═══════════════════════ 查询 ═══════════════════════

    /** 列表（分页 + 条件） */
    @PreAuthorize("@ss.hasPermi('jiancha:asset:query')")
    @GetMapping("/list")
    public TableDataInfo list(JcAssetInfo jcAssetInfo)
    {
        startPage();
        List<JcAssetInfo> list = assetService.selectAssetList(jcAssetInfo);
        return getDataTable(list);
    }

    /** 详情 */
    @PreAuthorize("@ss.hasPermi('jiancha:asset:query')")
    @GetMapping("/{assetId}")
    public AjaxResult getInfo(@PathVariable Long assetId)
    {
        return success(assetService.selectAssetById(assetId));
    }

    /** 按财物编号查 */
    @PreAuthorize("@ss.hasPermi('jiancha:asset:query')")
    @GetMapping("/no/{assetNo}")
    public AjaxResult getByNo(@PathVariable String assetNo)
    {
        return success(assetService.selectByAssetNo(assetNo));
    }

    /** 流转记录（全程保管链） */
    @PreAuthorize("@ss.hasPermi('jiancha:asset:query')")
    @GetMapping("/flows/{assetId}")
    public AjaxResult flows(@PathVariable Long assetId)
    {
        List<JcAssetFlow> list = assetService.selectFlows(assetId);
        return success(list);
    }

    /** 统计 */
    @PreAuthorize("@ss.hasPermi('jiancha:asset:query')")
    @GetMapping("/stat")
    public AjaxResult stat()
    {
        AssetStat stat = assetService.stat();
        return success(stat);
    }

    /** 人员检索（登记时选人） */
    @PreAuthorize("@ss.hasPermi('jiancha:asset:query')")
    @GetMapping("/persons")
    public AjaxResult persons(@RequestParam(required = false) String keyword)
    {
        List<Map<String, Object>> list = assetService.searchPersons(keyword);
        return success(list);
    }

    // ═══════════════════════ 登记与维护 ═══════════════════════

    /** 扣押登记（可传来源类型+单号自动带出涉事人） */
    @PreAuthorize("@ss.hasPermi('jiancha:asset:add')")
    @Log(title = "涉案财物-扣押登记", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody JcAssetInfo jcAssetInfo)
    {
        JcAssetInfo asset = assetService.register(jcAssetInfo);
        return success(asset);
    }

    /** 批量扣押登记（现场一次多件，单批最多 50 件，任一不合规整批回滚） */
    @PreAuthorize("@ss.hasPermi('jiancha:asset:add')")
    @Log(title = "涉案财物-批量扣押登记", businessType = BusinessType.INSERT)
    @PostMapping("/batch")
    public AjaxResult addBatch(@RequestBody List<JcAssetInfo> assets)
    {
        List<JcAssetInfo> list = assetService.registerBatch(assets);
        return success(list);
    }

    /** 修改登记（仅待入库状态） */
    @PreAuthorize("@ss.hasPermi('jiancha:asset:edit')")
    @Log(title = "涉案财物-修改登记", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody JcAssetInfo jcAssetInfo)
    {
        int rows = assetService.updateAsset(jcAssetInfo);
        return toAjax(rows);
    }

    /** 删除登记（仅待入库状态） */
    @PreAuthorize("@ss.hasPermi('jiancha:asset:remove')")
    @Log(title = "涉案财物-删除登记", businessType = BusinessType.DELETE)
    @DeleteMapping("/{assetId}")
    public AjaxResult remove(@PathVariable Long assetId)
    {
        int rows = assetService.removeAsset(assetId);
        return toAjax(rows);
    }

    // ═══════════════════════ 状态流转 ═══════════════════════

    /** 入库暂存（0→1） */
    @PreAuthorize("@ss.hasPermi('jiancha:asset:store')")
    @Log(title = "涉案财物-入库暂存", businessType = BusinessType.UPDATE)
    @PostMapping("/store/{assetId}")
    public AjaxResult store(@PathVariable Long assetId, @RequestBody AssetActionRequest request)
    {
        JcAssetInfo asset = assetService.store(assetId, request);
        return success(asset);
    }

    /** 移交保管（保管人变更，流水留痕） */
    @PreAuthorize("@ss.hasPermi('jiancha:asset:store')")
    @Log(title = "涉案财物-移交保管", businessType = BusinessType.UPDATE)
    @PostMapping("/handover/{assetId}")
    public AjaxResult handover(@PathVariable Long assetId, @RequestBody AssetActionRequest request)
    {
        JcAssetInfo asset = assetService.handover(assetId, request);
        return success(asset);
    }

    /** 处置申请（1→2） */
    @PreAuthorize("@ss.hasPermi('jiancha:asset:dispose')")
    @Log(title = "涉案财物-处置申请", businessType = BusinessType.UPDATE)
    @PostMapping("/disposeApply/{assetId}")
    public AjaxResult disposeApply(@PathVariable Long assetId, @RequestBody AssetActionRequest request)
    {
        JcAssetInfo asset = assetService.disposeApply(assetId, request);
        return success(asset);
    }

    /** 处置执行（2→3） */
    @PreAuthorize("@ss.hasPermi('jiancha:asset:dispose')")
    @Log(title = "涉案财物-处置执行", businessType = BusinessType.UPDATE)
    @PostMapping("/disposeDone/{assetId}")
    public AjaxResult disposeDone(@PathVariable Long assetId, @RequestBody AssetActionRequest request)
    {
        JcAssetInfo asset = assetService.disposeDone(assetId, request);
        return success(asset);
    }

    /** 直接退还（1→4，暂存中经审批免处置流程） */
    @PreAuthorize("@ss.hasPermi('jiancha:asset:dispose')")
    @Log(title = "涉案财物-直接退还", businessType = BusinessType.UPDATE)
    @PostMapping("/refund/{assetId}")
    public AjaxResult refund(@PathVariable Long assetId, @RequestBody AssetActionRequest request)
    {
        JcAssetInfo asset = assetService.refund(assetId, request);
        return success(asset);
    }
}
