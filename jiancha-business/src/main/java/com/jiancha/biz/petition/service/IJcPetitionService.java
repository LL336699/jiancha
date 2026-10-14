package com.jiancha.biz.petition.service;

import java.util.List;

import com.jiancha.biz.petition.domain.JcPetition;
import com.jiancha.biz.petition.domain.JcPetitionTrace;
import com.jiancha.biz.petition.domain.PetitionActionRequest;
import com.jiancha.biz.petition.domain.PetitionStat;
import com.jiancha.biz.petition.domain.PetitionSubmitRequest;

/**
 * 舞弊举报 Service（M3）
 *
 * <p>三条主线：免登录举报箱（提交/回执跟踪/补充材料）、管理端办理（状态机流转/分办转线索/
 * 解密查看）、时限预警与统计。</p>
 *
 * @author 小标
 */
public interface IJcPetitionService {

    // ═══════════════════════ 举报箱（免登录） ═══════════════════════

    /**
     * 提交举报。
     *
     * @param req      提交要素
     * @param clientIp 客户端 IP（限流键；不落库，满足"匿名不记录 IP"要求）
     * @return 已落库的举报（含 receiptCode，仅此一次返回）
     */
    JcPetition submit(PetitionSubmitRequest req, String clientIp);

    /**
     * 凭回执码查询办理进度（举报人视角：状态/反馈/材料，不含轨迹与敏感字段）。
     */
    JcPetition queryByReceipt(String receiptCode);

    /**
     * 举报人凭回执码补充材料。
     */
    int appendMaterialByReceipt(String receiptCode, String fileName, String filePath, Long fileSize);

    // ═══════════════════════ 管理端 ═══════════════════════

    /** 分页列表（不含举报内容与敏感字段） */
    List<JcPetition> selectJcPetitionList(JcPetition jcPetition);

    /** 详情（敏感字段按权限解密，由 Controller 层装配） */
    JcPetition selectJcPetitionByPetitionId(Long petitionId);

    /** 办理轨迹 */
    List<JcPetitionTrace> selectTraceByPetitionId(Long petitionId);

    /** 状态机流转（accept/review/dispatch/handle/feedback/archive/reject） */
    JcPetition handleAction(Long petitionId, PetitionActionRequest req);

    /** 逻辑删除（级联清理轨迹与材料） */
    int deleteJcPetitionByPetitionId(Long petitionId);

    /** 同一被举报对象近 90 天的历史举报（重复提示） */
    List<JcPetition> detectDuplicate(String targetName);

    // ═══════════════════════ 预警与统计 ═══════════════════════

    /** 刷新红黄灯预警（Quartz 日调度调用） */
    int refreshWarnLevel();

    /** 统计汇总 */
    PetitionStat stat();
}
