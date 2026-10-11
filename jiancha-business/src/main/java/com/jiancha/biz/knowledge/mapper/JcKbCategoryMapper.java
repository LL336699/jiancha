package com.jiancha.biz.knowledge.mapper;

import java.util.List;

import com.jiancha.biz.knowledge.domain.JcKbCategory;

/**
 * 知识库分类 Mapper
 *
 * @author 小标
 */
public interface JcKbCategoryMapper {

    /** 查询分类列表（支持按名称 / 类型 / 状态过滤） */
    List<JcKbCategory> selectJcKbCategoryList(JcKbCategory category);

    /** 查询全部正常分类（建树用） */
    List<JcKbCategory> selectJcKbCategoryAll();

    /** 查询分类详情 */
    JcKbCategory selectJcKbCategoryById(Long categoryId);

    /** 查询子分类数量（删除前校验） */
    int selectChildCount(Long categoryId);

    /** 查询分类下文档数量（删除前校验 / 列表展示） */
    int selectDocCount(Long categoryId);

    /** 新增分类 */
    int insertJcKbCategory(JcKbCategory category);

    /** 修改分类 */
    int updateJcKbCategory(JcKbCategory category);

    /** 删除分类（逻辑删除） */
    int deleteJcKbCategoryById(Long categoryId);

    /** 批量删除分类（逻辑删除） */
    int deleteJcKbCategoryByIds(Long[] categoryIds);
}
