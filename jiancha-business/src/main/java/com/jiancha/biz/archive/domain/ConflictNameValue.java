package com.jiancha.biz.archive.domain;

import java.io.Serializable;

/**
 * 利益冲突申报统计通用名值对（段15 申报看板）
 *
 * <p>状态分布 / 类型分布 / 月度趋势等聚合行统一载体：
 * name 为维度名称（状态、类型或 YYYY-MM），value 为计数。</p>
 *
 * @author 小标
 */
public class ConflictNameValue implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 维度名称 */
    private String name;
    /** 计数值 */
    private Long value;

    public ConflictNameValue()
    {
    }

    public ConflictNameValue(String name, Long value)
    {
        this.name = name;
        this.value = value;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getValue() { return value; }
    public void setValue(Long value) { this.value = value; }
}
