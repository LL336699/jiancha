package com.jiancha.biz.archive.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.jiancha.biz.archive.domain.RiskEvaluationResult;
import com.jiancha.biz.archive.service.IRiskWarnService;

/**
 * 廉政风险预警（12 类）Controller
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/archive/risk")
public class RiskWarnController extends BaseController
{
    @Autowired
    private IRiskWarnService riskWarnService;

    /** 评估（只读，不落库） */
    @PreAuthorize("@ss.hasPermi('jiancha:archive:query')")
    @GetMapping("/evaluate/{personId}")
    public AjaxResult evaluate(@PathVariable Long personId)
    {
        RiskEvaluationResult result = riskWarnService.evaluate(personId);
        if (result == null)
        {
            return error("人员不存在");
        }
        return success(result);
    }

    /** 评估并写回 risk_level / integrity_score 及派生标签 */
    @PreAuthorize("@ss.hasPermi('jiancha:archive:edit')")
    @Log(title = "廉政风险预警", businessType = BusinessType.UPDATE)
    @PostMapping("/apply/{personId}")
    public AjaxResult apply(@PathVariable Long personId)
    {
        RiskEvaluationResult result = riskWarnService.applyToPerson(personId);
        if (result == null)
        {
            return error("人员不存在");
        }
        return success(result);
    }
}
