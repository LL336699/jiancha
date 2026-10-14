package com.jiancha.biz.rectify.domain;

/**
 * 整改统计项（名称-数量对）
 *
 * @author 小标
 */
public class RectifyStatItem {

    /** 维度名称（已按字典映射为中文） */
    private String name;

    /** 数量 */
    private Long value;

    public RectifyStatItem() {
    }

    public RectifyStatItem(String name, Long value) {
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
