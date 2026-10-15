package com.jiancha.biz.dashboard.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 部门办理效能（段12 纪检综合看板 · 部门维度）
 *
 * <p>按「业务模块 + 责任部门」聚合事项数量，用于识别办理压力集中部门，
 * 支撑纪委督办资源调配。仅统计带有责任部门字段的 5 个模块。</p>
 *
 * @author 小标
 */
@Data
public class DeptWorkload implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 所属模块标识 */
    private String moduleKey;

    /** 所属模块名称 */
    private String moduleName;

    /** 责任部门 */
    private String deptName;

    /** 该部门该模块事项总量 */
    private Long total;

    /** 在办数量 */
    private Long processing;

    /** 超期数量 */
    private Long overdue;
}
