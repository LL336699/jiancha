package com.jiancha.biz.dashboard.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 综合看板每日快照（段12 纪检综合看板）
 *
 * <p>由 {@code JcDashboardSnapshotTask} 每日聚合全模块指标后落库，
 * 用于历史趋势对比与跨日环比。{@code snapshotDate} 唯一，重复执行按日更新。</p>
 *
 * @author 小标
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OverviewSnapshot extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 快照主键 */
    private Long snapshotId;

    /** 快照日期（唯一） */
    private Date snapshotDate;

    /** 信访举报：总量 / 在办 / 超期 */
    private Long petitionTotal;
    private Long petitionProcessing;
    private Long petitionOverdue;

    /** 整改闭环：总量 / 在办 / 超期 */
    private Long rectifyTotal;
    private Long rectifyProcessing;
    private Long rectifyOverdue;

    /** 廉政风控：总量 / 在办 */
    private Long riskTotal;
    private Long riskProcessing;

    /** 供应商廉洁：总量 / 黑名单(含禁入) */
    private Long supplierTotal;
    private Long supplierBlack;

    /** 廉政教育：总量 / 已完卷 */
    private Long eduTotal;
    private Long eduDone;

    /** 廉政意见回复：总量 / 在办 / 超期 */
    private Long replyTotal;
    private Long replyProcessing;
    private Long replyOverdue;

    /** 作风建设：总量 / 在办 / 超期 */
    private Long conductTotal;
    private Long conductProcessing;
    private Long conductOverdue;

    /** 涉案财物：总量 / 在办 / 超期 */
    private Long assetTotal;
    private Long assetProcessing;
    private Long assetOverdue;

    /** 汇总：在办合计 / 超期合计 */
    private Long totalProcessing;
    private Long totalOverdue;

    /** 整体灯色：red / yellow / green */
    private String lightLevel;
}
