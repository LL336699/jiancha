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
import com.jiancha.biz.archive.domain.JcDeclareConfig;
import com.jiancha.biz.archive.service.IJcDeclareConfigService;

/**
 * 年度集中申报截止配置 Controller（段15）
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/archive/declareConfig")
public class JcDeclareConfigController extends BaseController
{
    @Autowired
    private IJcDeclareConfigService configService;

    @PreAuthorize("@ss.hasPermi('jiancha:archive:list')")
    @GetMapping("/list")
    public TableDataInfo list(JcDeclareConfig config)
    {
        startPage();
        List<JcDeclareConfig> list = configService.selectJcDeclareConfigList(config);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('jiancha:archive:query')")
    @GetMapping(value = "/{configId}")
    public AjaxResult getInfo(@PathVariable Long configId)
    {
        return success(configService.selectByConfigId(configId));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:archive:add')")
    @Log(title = "申报截止配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody JcDeclareConfig config)
    {
        config.setCreateBy(getUsername());
        return toAjax(configService.insertJcDeclareConfig(config));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:archive:edit')")
    @Log(title = "申报截止配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody JcDeclareConfig config)
    {
        config.setUpdateBy(getUsername());
        return toAjax(configService.updateJcDeclareConfig(config));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:archive:remove')")
    @Log(title = "申报截止配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{configIds}")
    public AjaxResult remove(@PathVariable Long[] configIds)
    {
        int rows = 0;
        for (Long configId : configIds)
        {
            rows += configService.deleteJcDeclareConfigByConfigId(configId);
        }
        return toAjax(rows);
    }
}
