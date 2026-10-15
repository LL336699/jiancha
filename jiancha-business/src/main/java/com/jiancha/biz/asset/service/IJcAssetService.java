package com.jiancha.biz.asset.service;

import java.util.List;
import java.util.Map;

import com.jiancha.biz.asset.domain.AssetActionRequest;
import com.jiancha.biz.asset.domain.AssetStat;
import com.jiancha.biz.asset.domain.JcAssetFlow;
import com.jiancha.biz.asset.domain.JcAssetInfo;

/**
 * 涉案财物服务（段11 E6）
 *
 * 主链路：扣押登记 → 入库暂存 → （移交保管）→ 处置申请 → 处置执行 → 关闭；
 * 暂存中可经审批直接退还。每步落流转流水，超期未处置由 Quartz 日刷红黄灯。
 */
public interface IJcAssetService
{
    // ═══════════════════════ 查询 ═══════════════════════

    /** 按财物ID查详情（含 remainDays / flowCount） */
    JcAssetInfo selectAssetById(Long assetId);

    /** 条件查列表（含 remainDays / flowCount） */
    List<JcAssetInfo> selectAssetList(JcAssetInfo jcAssetInfo);

    /** 按财物编号查 */
    JcAssetInfo selectByAssetNo(String assetNo);

    /** 按财物查流转记录（全程保管链，时间倒序） */
    List<JcAssetFlow> selectFlows(Long assetId);

    // ═══════════════════════ 登记与维护 ═══════════════════════

    /**
     * 扣押登记（状态 0 待入库）
     * 传来源类型+单号时自动带出涉事人（作风建设问题/问题线索），
     * 再以人员档案快照补全姓名/部门；落首条「扣押登记」流水。
     */
    JcAssetInfo register(JcAssetInfo asset);

    /**
     * 批量扣押登记（现场一次扣押多件财物，共用同一来源与涉事人）。
     * 单批最多 50 件；任一不合规则整批回滚。
     */
    List<JcAssetInfo> registerBatch(List<JcAssetInfo> assets);

    /** 修改财物登记（仅待入库状态允许） */
    int updateAsset(JcAssetInfo asset);

    /** 删除财物登记（仅待入库状态允许） */
    int removeAsset(Long assetId);

    // ═══════════════════════ 状态流转 ═══════════════════════

    /** 入库暂存（0→1：登记保管人/存放地/入库日期） */
    JcAssetInfo store(Long assetId, AssetActionRequest request);

    /** 移交保管（保管人变更，状态不变，流水留痕） */
    JcAssetInfo handover(Long assetId, AssetActionRequest request);

    /** 处置申请（1→2：定处置方式与期限） */
    JcAssetInfo disposeApply(Long assetId, AssetActionRequest request);

    /** 处置执行（2→3：退还/移交/销毁/收归，落处置日期与说明） */
    JcAssetInfo disposeDone(Long assetId, AssetActionRequest request);

    /** 直接退还（1→4：暂存中经审批免处置流程退还） */
    JcAssetInfo refund(Long assetId, AssetActionRequest request);

    // ═══════════════════════ 辅助与统计 ═══════════════════════

    /** 人员检索（登记时选人：姓名/编号模糊） */
    List<Map<String, Object>> searchPersons(String keyword);

    /** 统计（卡片 + 五组分布） */
    AssetStat stat();

    /** 红黄灯批量刷新（Quartz 日刷） */
    int refreshWarnLevels();
}
