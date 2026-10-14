package com.jiancha.biz.risk.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 岗位风险模型行 jc_risk_model
 *
 * <p>设计文档 §M10：按岗位配置风险因子与权重，模型可配置、不写死算法。
 * 一行 = 一个岗位的一个因子；岗位风险分 = 该岗位全部因子权重合计（封顶 100）。
 * 三级风险视图（部门/岗位/个人）中的「岗位」层即由本表汇总。</p>
 *
 * @author 小标
 */
public class JcRiskModel extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 模型行ID */
    private Long modelId;

    /** 岗位名称（如 采购经理 / 招投标专员） */
    private String positionName;

    /** 因子编码（F01-F12） */
    private String factorCode;

    /** 因子名称（如 供应商接触度 / 资金审批权） */
    private String factorName;

    /** 因子权重（0-100） */
    private Double weight;

    /** 因子说明 */
    private String description;

    /** 状态（0启用 1停用） */
    private String status;

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public String getFactorCode() {
        return factorCode;
    }

    public void setFactorCode(String factorCode) {
        this.factorCode = factorCode;
    }

    public String getFactorName() {
        return factorName;
    }

    public void setFactorName(String factorName) {
        this.factorName = factorName;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
