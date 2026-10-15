package com.jiancha.biz.dashboard.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 模块健康评分（段12 纪检综合看板 · 健康度）
 *
 * <p>综合「超期占比」「在办积压」「黑名单」等因素对单模块打出 0-100 分，
 * 并映射为 优/良/中/差 四档，便于纪委一眼掌握各业务模块运行健康度。</p>
 *
 * @author 小标
 */
@Data
public class ModuleHealth implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 模块标识 */
    private String moduleKey;

    /** 模块名称 */
    private String moduleName;

    /** 健康评分 0-100（越高越健康） */
    private Integer score;

    /** 评级：优(>=90) / 良(>=75) / 中(>=60) / 差(<60) */
    private String level;

    /** 评分理由（扣分点说明） */
    private String reason;
}
