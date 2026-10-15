package com.jiancha.biz.dashboard.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 快照环比对比（段12 纪检综合看板 · 趋势）
 *
 * <p>对比最新一日快照与约一周前快照的「在办/超期」增量，判断整体督办态势
 * 是改善(improved)/持平(flat)/恶化(worse)，供纪委周报与态势研判使用。</p>
 *
 * @author 小标
 */
@Data
public class SnapshotCompare implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 最新快照日期 */
    private Date newDate;

    /** 对比基准快照日期 */
    private Date oldDate;

    /** 最新在办数 */
    private Long newProcessing;

    /** 基准在办数 */
    private Long oldProcessing;

    /** 在办增量（新 - 旧） */
    private Long deltaProcessing;

    /** 最新超期数 */
    private Long newOverdue;

    /** 基准超期数 */
    private Long oldOverdue;

    /** 超期增量（新 - 旧） */
    private Long deltaOverdue;

    /** 最新整体灯色 */
    private String newLight;

    /** 基准整体灯色 */
    private String oldLight;

    /** 态势趋势：improved（改善）/ flat（持平）/ worse（恶化） */
    private String trend;
}
