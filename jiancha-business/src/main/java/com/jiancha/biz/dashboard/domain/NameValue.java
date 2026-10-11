package com.jiancha.biz.dashboard.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 名称-数值对（用于饼图/柱状图的分布数据）
 *
 * @author 小标
 */
@Data
public class NameValue implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 维度名称（已映射为字典标签） */
    private String name;

    /** 计数 */
    private Long value;
}
