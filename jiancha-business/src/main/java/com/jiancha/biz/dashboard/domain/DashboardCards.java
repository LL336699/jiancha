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
}
