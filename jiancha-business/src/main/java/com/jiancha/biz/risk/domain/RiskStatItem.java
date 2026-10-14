package com.jiancha.biz.risk.domain;

/**
 * 风控统计项（名称-数量对）
 *
 * @author 小标
 */
public class RiskStatItem {

    /** 维度名称 */
    private String name;

    /** 数量 */
    private Long value;

    public RiskStatItem() {
    }

    public RiskStatItem(String name, Long value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
}
