package com.jiancha.biz.knowledge.service;

import java.util.List;

import com.jiancha.biz.knowledge.domain.JcKbCategory;

/**
 * 知识库分类 Service
 *
 * @author 小标
 */
public interface IJcKbCategoryService {

    /** 查询分类列表 */
    List<JcKbCategory> selectJcKbCategoryList(JcKbCategory category);

    /** 查询分类树（含 docCount） */
    List<JcKbCategory> selectCategoryTree();

    /** 查询分类详情 */
    JcKbCategory selectJcKbCategoryById(Long categoryId);

    /** 新增分类（自动补 ancestors） */
    int insertJcKbCategory(JcKbCategory category);

    /** 修改分类（父级变更时同步子级 ancestors） */
    int updateJcKbCategory(JcKbCategory category);

    /** 删除分类（存在子分类或文档时拒绝） */
    int deleteJcKbCategoryById(Long categoryId);

    /** 批量删除分类 */
    int deleteJcKbCategoryByIds(Long[] categoryIds);
}
