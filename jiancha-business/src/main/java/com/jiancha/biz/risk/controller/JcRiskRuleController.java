package com.jiancha.biz.risk.controller;

import java.util.List;
import java.util.Map;

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

import com.jiancha.biz.risk.domain.JcRiskModel;
import com.jiancha.biz.risk.domain.JcRiskRule;
import com.jiancha.biz.risk.service.IJcRiskService;

/**
 * 风险规则与岗位模型 Controller（M10）
 *
 * <p>权限前缀 jiancha:risk:*。规则与岗位模型的阈值 / 权重全部可配，
 * 业务专家在页面上即可调整，无需改代码（设计文档 §M10「模型可配置」要求）。</p>
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/risk")
public class JcRiskRuleController extends BaseController {

    @Autowired
    private IJcRiskService riskService;

    // ═══════════════════════ 规则管理 ═══════════════════════

    /** 规则列表 */
    @PreAuthorize("@ss.hasPermi('jiancha:risk:rule:list')")
    @GetMapping("/rule/list")
    public TableDataInfo ruleList(JcRiskRule jcRiskRule) {
        startPage();
        List<JcRiskRule> list = riskService.selectJcRiskRuleList(jcRiskRule);
        return getDataTable(list);
    }

    /** 规则详情 */
    @PreAuthorize("@ss.hasPermi('jiancha:risk:rule:query')")
    @GetMapping(value = "/rule/{ruleId}")
    public AjaxResult ruleInfo(@PathVariable Long ruleId) {
        return success(riskService.selectJcRiskRuleByRuleId(ruleId));
    }

    /** 新增规则 */
    @PreAuthorize("@ss.hasPermi('jiancha:risk:rule:add')")
    @Log(title = "风险规则", businessType = BusinessType.INSERT)
    @PostMapping("/rule")
    public AjaxResult addRule(@Validated @RequestBody JcRiskRule jcRiskRule) {
        return toAjax(riskService.insertJcRiskRule(jcRiskRule));
    }

    /** 更新规则 */
    @PreAuthorize("@ss.hasPermi('jiancha:risk:rule:edit')")
    @Log(title = "风险规则", businessType = BusinessType.UPDATE)
    @PutMapping("/rule")
    public AjaxResult editRule(@Validated @RequestBody JcRiskRule jcRiskRule) {
        return toAjax(riskService.updateJcRiskRule(jcRiskRule));
    }

    /** 删除规则 */
    @PreAuthorize("@ss.hasPermi('jiancha:risk:rule:remove')")
    @Log(title = "风险规则", businessType = BusinessType.DELETE)
    @DeleteMapping("/rule/{ruleId}")
    public AjaxResult removeRule(@PathVariable Long ruleId) {
        return toAjax(riskService.deleteJcRiskRuleByRuleId(ruleId));
    }

    // ═══════════════════════ 岗位风险模型 ═══════════════════════

    /** 模型行列表 */
    @PreAuthorize("@ss.hasPermi('jiancha:risk:model:list')")
    @GetMapping("/model/list")
    public TableDataInfo modelList(JcRiskModel jcRiskModel) {
        startPage();
        List<JcRiskModel> list = riskService.selectJcRiskModelList(jcRiskModel);
        return getDataTable(list);
    }

    /** 岗位权重汇总（三级视图-岗位层） */
    @PreAuthorize("@ss.hasPermi('jiancha:risk:model:list')")
    @GetMapping("/model/position")
    public AjaxResult positionSummary() {
        List<Map<String, Object>> list = riskService.sumWeightByPosition();
        return success(list);
    }

    /** 新增模型行 */
    @PreAuthorize("@ss.hasPermi('jiancha:risk:model:add')")
    @Log(title = "岗位风险模型", businessType = BusinessType.INSERT)
    @PostMapping("/model")
    public AjaxResult addModel(@Validated @RequestBody JcRiskModel jcRiskModel) {
        return toAjax(riskService.insertJcRiskModel(jcRiskModel));
    }

    /** 更新模型行 */
    @PreAuthorize("@ss.hasPermi('jiancha:risk:model:edit')")
    @Log(title = "岗位风险模型", businessType = BusinessType.UPDATE)
    @PutMapping("/model")
    public AjaxResult editModel(@Validated @RequestBody JcRiskModel jcRiskModel) {
        return toAjax(riskService.updateJcRiskModel(jcRiskModel));
    }

    /** 删除模型行 */
    @PreAuthorize("@ss.hasPermi('jiancha:risk:model:remove')")
    @Log(title = "岗位风险模型", businessType = BusinessType.DELETE)
    @DeleteMapping("/model/{modelId}")
    public AjaxResult removeModel(@PathVariable Long modelId) {
        return toAjax(riskService.deleteJcRiskModelByModelId(modelId));
    }
}
