package com.jiancha.biz.knowledge.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jiancha.biz.knowledge.domain.JcKbCategory;
import com.jiancha.biz.knowledge.mapper.JcKbCategoryMapper;
import com.jiancha.biz.knowledge.service.IJcKbCategoryService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * 知识库分类 Service 实现
 *
 * @author 小标
 */
@Service
public class JcKbCategoryServiceImpl implements IJcKbCategoryService {

    /** 虚拟根节点ID */
    private static final Long ROOT_ID = 0L;

    @Autowired
    private JcKbCategoryMapper categoryMapper;

    @Override
    public List<JcKbCategory> selectJcKbCategoryList(JcKbCategory category) {
        return categoryMapper.selectJcKbCategoryList(category);
    }

    @Override
    public List<JcKbCategory> selectCategoryTree() {
        return buildTree(categoryMapper.selectJcKbCategoryAll(), ROOT_ID);
    }

    @Override
    public JcKbCategory selectJcKbCategoryById(Long categoryId) {
        return categoryMapper.selectJcKbCategoryById(categoryId);
    }

    @Override
    @Transactional
    public int insertJcKbCategory(JcKbCategory category) {
        if (category.getParentId() == null) {
            category.setParentId(ROOT_ID);
        }
        // 校验同级下名称唯一（同父级同名会造成分类树歧义）
        JcKbCategory query = new JcKbCategory();
        query.setParentId(category.getParentId());
        query.setCatName(category.getCatName());
        List<JcKbCategory> siblings = categoryMapper.selectJcKbCategoryList(query);
        if (siblings != null && !siblings.isEmpty()) {
            throw new ServiceException("同级分类下已存在同名分类：" + category.getCatName());
        }
        category.setAncestors(resolveAncestors(category.getParentId()));
        if (category.getStatus() == null) {
            category.setStatus("0");
        }
        return categoryMapper.insertJcKbCategory(category);
    }

    @Override
    @Transactional
    public int updateJcKbCategory(JcKbCategory category) {
        if (category.getCategoryId() == null) {
            throw new ServiceException("分类ID不能为空");
        }
        // 不允许把自己挂到自己的子孙下（会造成环）
        if (category.getParentId() != null) {
            if (category.getParentId().equals(category.getCategoryId())) {
                throw new ServiceException("上级分类不能是自己");
            }
            List<Long> descendants = collectDescendantIds(category.getCategoryId());
            if (descendants.contains(category.getParentId())) {
                throw new ServiceException("上级分类不能是自己的子分类");
            }
            category.setAncestors(resolveAncestors(category.getParentId()));
        }
        return categoryMapper.updateJcKbCategory(category);
    }

    @Override
    @Transactional
    public int deleteJcKbCategoryById(Long categoryId) {
        int childCount = categoryMapper.selectChildCount(categoryId);
        if (childCount > 0) {
            throw new ServiceException("存在子分类，不允许删除");
        }
        int docCount = categoryMapper.selectDocCount(categoryId);
        if (docCount > 0) {
            throw new ServiceException("分类下存在 " + docCount + " 篇文档，请先移除或转移文档");
        }
        return categoryMapper.deleteJcKbCategoryById(categoryId);
    }

    @Override
    @Transactional
    public int deleteJcKbCategoryByIds(Long[] categoryIds) {
        for (Long id : categoryIds) {
            deleteJcKbCategoryById(id);
        }
        return categoryIds.length;
    }

    // ══════════════════ 内部工具 ══════════════════

    /** 计算祖级列表：父级 ancestors + 父级ID */
    private String resolveAncestors(Long parentId) {
        if (parentId == null || ROOT_ID.equals(parentId)) {
            return String.valueOf(ROOT_ID);
        }
        JcKbCategory parent = categoryMapper.selectJcKbCategoryById(parentId);
        if (parent == null) {
            return String.valueOf(ROOT_ID);
        }
        String base = StringUtils.isEmpty(parent.getAncestors()) ? String.valueOf(ROOT_ID) : parent.getAncestors();
        return base + "," + parentId;
    }

    /** 收集子孙ID（防环校验用） */
    private List<Long> collectDescendantIds(Long categoryId) {
        List<Long> result = new java.util.ArrayList<>();
        List<JcKbCategory> all = categoryMapper.selectJcKbCategoryAll();
        collect(categoryId, all, result);
        return result;
    }

    private void collect(Long parentId, List<JcKbCategory> all, List<Long> acc) {
        for (JcKbCategory c : all) {
            if (parentId.equals(c.getParentId())) {
                acc.add(c.getCategoryId());
                collect(c.getCategoryId(), all, acc);
            }
        }
    }

    /** 由平铺列表构建树 */
    private List<JcKbCategory> buildTree(List<JcKbCategory> list, Long parentId) {
        List<JcKbCategory> tree = new java.util.ArrayList<>();
        for (JcKbCategory node : list) {
            if (parentId.equals(node.getParentId())) {
                node.setChildren(buildTree(list, node.getCategoryId()));
                tree.add(node);
            }
        }
        return tree;
    }
}
