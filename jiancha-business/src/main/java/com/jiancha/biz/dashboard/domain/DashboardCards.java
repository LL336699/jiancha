package com.jiancha.biz.dashboard.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 数据看板核心指标卡（M8 数据看板）
 *
 * @author 小标
 */
@Data
public class DashboardCards implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 线索总量 */
    private Long clueTotal;

    /** 在办线索（待受理/已受理/核查中/处置中） */
    private Long clueProcessing;

    /** 已办结线索 */
    private Long clueDone;

    /** 超期线索（未办结且已超过截止日期） */
    private Long clueOverdue;

    /** 廉政档案人员数 */
    private Long personTotal;

    /** 文书生成数 */
    private Long reportTotal;

    /** 知识库已入库文档数 */
    private Long kbDocTotal;

    /** 知识库分块总数 */
    private Long kbChunkTotal;

    /** 利益冲突申报数 */
    private Long conflictTotal;

    /* ===================== 段12 纪检综合看板：P8 八大模块 ===================== */

    /** 信访举报：总量 / 在办 / 已办结 / 超期 */
    private Long petitionTotal;
    private Long petitionProcessing;
    private Long petitionDone;
    private Long petitionOverdue;

    /** 整改闭环：总量 / 在办 / 已办结 / 超期 */
    private Long rectifyTotal;
    private Long rectifyProcessing;
    private Long rectifyDone;
    private Long rectifyOverdue;

    /** 廉政风控：总量 / 在办 / 已办结 */
    private Long riskTotal;
    private Long riskProcessing;
    private Long riskDone;

    /** 供应商廉洁：总量 / 黑名单(含禁入) */
    private Long supplierTotal;
    private Long supplierBlack;

    /** 廉政教育：总量 / 已完卷(已交卷+已评分+弃考) */
    private Long eduTotal;
    private Long eduDone;

    /** 廉政意见回复：总量 / 在办 / 已办结 / 超期 */
    private Long replyTotal;
    private Long replyProcessing;
    private Long replyDone;
    private Long replyOverdue;

    /** 作风建设：总量 / 在办 / 已办结 / 超期 */
    private Long conductTotal;
    private Long conductProcessing;
    private Long conductDone;
    private Long conductOverdue;

    /** 涉案财物：总量 / 在办 / 已办结 / 超期 */
    private Long assetTotal;
    private Long assetProcessing;
    private Long assetDone;
    private Long assetOverdue;

    /** 在办合计（跨模块） */
    private Long totalProcessing;

    /** 超期合计（跨模块） */
    private Long totalOverdue;

    /** 整体灯色：red / yellow / green */
    private String lightLevel;
}
