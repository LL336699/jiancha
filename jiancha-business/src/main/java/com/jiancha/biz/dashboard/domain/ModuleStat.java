package com.jiancha.biz.dashboard.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 模块运行统计（段12 纪检综合看板）
 *
 * <p>每个业务模块的汇总指标：总量 / 在办（未终态）/ 已办结（终态）/ 超期，
 * 以及由超期与在办推导出的红黄绿灯 {@code light}（red / yellow / green）。</p>
 *
 * @author 小标
 */
@Data
public class ModuleStat implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 模块标识：petition/rectify/risk/supplier/edu/reply/conduct/asset */
    private String moduleKey;

    /** 模块中文名 */
    private String moduleName;

    /** 总量 */
    private Long total;

    /** 在办数量（尚未到达终态） */
    private Long processing;

    /** 已办结数量（已到达终态） */
    private Long done;

    /** 超期数量（未终态且已超过期限日） */
    private Long overdue;

    /** 红黄绿灯：red / yellow / green（由 service 推导后回填） */
    private String light;
}
