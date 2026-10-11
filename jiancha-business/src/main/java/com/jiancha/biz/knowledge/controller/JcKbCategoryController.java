package com.jiancha.biz.knowledge.controller;

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

import com.jiancha.biz.knowledge.domain.JcKbCategory;
import com.jiancha.biz.knowledge.service.IJcKbCategoryService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;

/**
 * 知识库分类 Controller（M1）
 *
 * <p>权限前缀 jiancha:kb:category:*。分类树按 cat_type 区分"外部法规 / 内部制度"，
 * 保证两类知识域互不污染。</p>
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/kb/category")
public class JcKbCategoryController extends BaseController {

    @Autowired
    private IJcKbCategoryService categoryService;

    /** 分类列表（平铺，可按 catType / catName 过滤） */
    @PreAuthorize("@ss.hasPermi('jiancha:kb:category:list')")
    @GetMapping("/list")
    public AjaxResult list(JcKbCategory category) {
        List<JcKbCategory> list = categoryService.selectJcKbCategoryList(category);
        return success(list);
    }

    /** 分类树（含每个分类的文档数，前端左侧树用） */
    @PreAuthorize("@ss.hasPermi('jiancha:kb:category:list')")
    @GetMapping("/tree")
    public AjaxResult tree() {
        return success(categoryService.selectCategoryTree());
    }

    /** 分类详情 */
    @PreAuthorize("@ss.hasPermi('jiancha:kb:category:query')")
    @GetMapping(value = "/{categoryId}")
    public AjaxResult getInfo(@PathVariable Long categoryId) {
        return success(categoryService.selectJcKbCategoryById(categoryId));
    }

    /** 新增分类 */
    @PreAuthorize("@ss.hasPermi('jiancha:kb:category:add')")
    @Log(title = "知识库分类", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody JcKbCategory category) {
        category.setCreateBy(getUsername());
        return toAjax(categoryService.insertJcKbCategory(category));
    }

    /** 修改分类 */
    @PreAuthorize("@ss.hasPermi('jiancha:kb:category:edit')")
    @Log(title = "知识库分类", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody JcKbCategory category) {
        category.setUpdateBy(getUsername());
        return toAjax(categoryService.updateJcKbCategory(category));
    }

    /** 删除分类（存在子分类或文档时拒绝） */
    @PreAuthorize("@ss.hasPermi('jiancha:kb:category:remove')")
    @Log(title = "知识库分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{categoryId}")
    public AjaxResult remove(@PathVariable Long categoryId) {
        return toAjax(categoryService.deleteJcKbCategoryById(categoryId));
    }
}
