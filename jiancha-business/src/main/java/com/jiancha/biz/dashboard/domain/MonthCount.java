package com.jiancha.biz.dashboard.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 月度计数（用于趋势折线图）
 *
 * @author 小标
 */
@Data
public class MonthCount implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 月份，格式 yyyy-MM */
    private String month;

    /** 计数 */
    private Long value;
}
