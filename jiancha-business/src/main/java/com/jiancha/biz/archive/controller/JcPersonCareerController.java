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
import com.jiancha.biz.archive.domain.JcPersonCareer;
import com.jiancha.biz.archive.service.IJcPersonCareerService;

/**
 * 任职经历 Controller
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/archive/career")
public class JcPersonCareerController extends BaseController
{
    @Autowired
    private IJcPersonCareerService jcPersonCareerService;

    @PreAuthorize("@ss.hasPermi('jiancha:archive:list')")
    @GetMapping("/list")
    public TableDataInfo list(JcPersonCareer jcPersonCareer)
    {
        startPage();
        List<JcPersonCareer> list = jcPersonCareerService.selectJcPersonCareerList(jcPersonCareer);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('jiancha:archive:query')")
    @GetMapping("/byPerson/{personId}")
    public AjaxResult byPerson(@PathVariable Long personId)
    {
        return success(jcPersonCareerService.selectByPersonId(personId));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:archive:query')")
    @GetMapping(value = "/{careerId}")
    public AjaxResult getInfo(@PathVariable Long careerId)
    {
        return success(jcPersonCareerService.selectByCareerId(careerId));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:archive:add')")
    @Log(title = "任职经历", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody JcPersonCareer jcPersonCareer)
    {
        jcPersonCareer.setCreateBy(getUsername());
        return toAjax(jcPersonCareerService.insertJcPersonCareer(jcPersonCareer));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:archive:edit')")
    @Log(title = "任职经历", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody JcPersonCareer jcPersonCareer)
    {
        jcPersonCareer.setUpdateBy(getUsername());
        return toAjax(jcPersonCareerService.updateJcPersonCareer(jcPersonCareer));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:archive:remove')")
    @Log(title = "任职经历", businessType = BusinessType.DELETE)
    @DeleteMapping("/{careerIds}")
    public AjaxResult remove(@PathVariable Long[] careerIds)
    {
        int rows = 0;
        for (Long careerId : careerIds)
        {
            rows += jcPersonCareerService.deleteJcPersonCareerByCareerId(careerId);
        }
        return toAjax(rows);
    }
}
