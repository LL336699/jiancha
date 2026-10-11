package com.jiancha.biz.knowledge.domain;

import java.util.ArrayList;
import java.util.List;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 知识库分类 jc_kb_category（树形）
 *
 * <p>用于组织法规 / 制度 / 案例等知识文档。分类树按 cat_type 区分知识域，
 * 检索时按分类过滤，保证"外部法规"与"内部制度"两类知识互不污染。</p>
 *
 * @author 小标
 */
public class JcKbCategory extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 分类ID */
    private Long categoryId;

    /** 父分类ID */
    private Long parentId;

    /** 祖级列表（如 0,100,1000） */
    private String ancestors;

    /** 分类名称 */
    @Excel(name = "分类名称")
    private String catName;

    /** 分类类型（字典 jc_kb_cat_type） */
    @Excel(name = "分类类型")
    private String catType;

    /** 显示顺序 */
    private Integer orderNum;

    /** 状态（0正常 1停用） */
    private String status;

    /** 该分类下的文档数量（非表字段，列表展示用） */
    private Integer docCount;

    /** 子分类（构建树用，非表字段） */
    private List<JcKbCategory> children = new ArrayList<>();

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getAncestors() {
        return ancestors;
    }

    public void setAncestors(String ancestors) {
        this.ancestors = ancestors;
    }

    public String getCatName() {
        return catName;
    }

    public void setCatName(String catName) {
        this.catName = catName;
    }

    public String getCatType() {
        return catType;
    }

    public void setCatType(String catType) {
        this.catType = catType;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getDocCount() {
        return docCount;
    }

    public void setDocCount(Integer docCount) {
        this.docCount = docCount;
    }

    public List<JcKbCategory> getChildren() {
        return children;
    }

    public void setChildren(List<JcKbCategory> children) {
        this.children = children;
    }
}
