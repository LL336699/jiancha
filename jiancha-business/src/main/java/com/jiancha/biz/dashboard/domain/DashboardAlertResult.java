package com.jiancha.biz.dashboard.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 预警命中结果（段12 纪检综合看板 · 预警规则引擎）
 *
 * <p>由 {@link DashboardAlertRule} 与当前实时指标比对后产生，供前端高亮与督办。</p>
 *
 * @author 小标
 */
@Data
public class DashboardAlertResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 命中的规则名称 */
    private String ruleName;

    /** 命中模块标识 */
    private String moduleKey;

    /** 命中模块名称 */
    private String moduleName;

    /** 指标类型 */
    private String metricType;

    /** 规则阈值 */
    private Integer threshold;

    /** 当前实际值 */
    private Long actual;

    /** 预警级别：red/yellow */
    private String alertLevel;

    /** 命中说明 */
    private String message;
}
