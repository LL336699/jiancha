package com.jiancha.biz.risk.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

import com.jiancha.biz.risk.domain.JcRiskAlert;
import com.jiancha.biz.risk.domain.JcRiskRule;
import com.jiancha.biz.risk.domain.RiskVerifyRequest;
import com.jiancha.biz.risk.service.IJcRiskService;

/**
 * 风险预警 Controller（M10）
 *
 * <p>权限前缀 jiancha:risk:*。涵盖预警列表/详情、核实闭环（确认转整改 / 排除误报 / 归档）、
 * 手动登记预警、触发引擎扫描。</p>
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/risk")
public class JcRiskAlertController extends BaseController {

    @Autowired
    private IJcRiskService riskService;

    /** 预警列表（分页） */
    @PreAuthorize("@ss.hasPermi('jiancha:risk:list')")
    @GetMapping("/alert/list")
    public TableDataInfo alertList(JcRiskAlert jcRiskAlert) {
        startPage();
        List<JcRiskAlert> list = riskService.selectJcRiskAlertList(jcRiskAlert);
        return getDataTable(list);
    }

    /** 预警详情 */
    @PreAuthorize("@ss.hasPermi('jiancha:risk:query')")
    @GetMapping(value = "/alert/{alertId}")
    public AjaxResult alertInfo(@PathVariable Long alertId) {
        return success(riskService.selectJcRiskAlertByAlertId(alertId));
    }

    /** 手动登记预警 */
    @PreAuthorize("@ss.hasPermi('jiancha:risk:add')")
    @Log(title = "风险预警", businessType = BusinessType.INSERT)
    @PostMapping("/alert")
    public AjaxResult addAlert(@Validated @RequestBody JcRiskAlert jcRiskAlert) {
        return toAjax(riskService.insertJcRiskAlert(jcRiskAlert));
    }

    /** 核实预警（confirm 确认转整改 / exclude 排除 / archive 归档） */
    @PreAuthorize("@ss.hasPermi('jiancha:risk:verify')")
    @Log(title = "风险预警", businessType = BusinessType.UPDATE)
    @PostMapping("/alert/verify")
    public AjaxResult verify(@Validated @RequestBody RiskVerifyRequest req,
                             @RequestParam Long alertId) {
        return success(riskService.verify(alertId, req));
    }

    /** 手动触发引擎扫描（默认由 Quartz 日调度执行） */
    @PreAuthorize("@ss.hasPermi('jiancha:risk:scan')")
    @Log(title = "风险预警", businessType = BusinessType.OTHER)
    @PostMapping("/alert/scan")
    public AjaxResult scan() {
        int created = riskService.scanAndGenerate();
        return success("扫描完成，新增预警 " + created + " 条");
    }

    /** 统计与三级风险视图 */
    @PreAuthorize("@ss.hasPermi('jiancha:risk:stat')")
    @GetMapping("/stat")
    public AjaxResult stat() {
        return success(riskService.stat());
    }
}
