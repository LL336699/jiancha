package com.jiancha.biz.conduct.service;

import java.util.List;
import java.util.Map;

import com.jiancha.biz.conduct.domain.ConductActionRequest;
import com.jiancha.biz.conduct.domain.ConductStat;
import com.jiancha.biz.conduct.domain.JcConductIssue;
import com.jiancha.biz.conduct.domain.JcConductRecovery;

/**
 * 作风建设专项 Service（E5）
 *
 * <p>业务口径（企业版）：落实中央八项规定精神（企业版对应违规吃喝、
 * 违规收送礼品礼金、公车私用、公款旅游、大办婚丧喜庆、违规发放津补贴、
 * 楼堂馆所违规等）问题专项台账，走「登记 → 核查 → 处置（退缴/谈话/诫勉/
 * 处分）→ 转整改/转处分 → 关闭」闭环。</p>
 *
 * <ul>
 *   <li>登记时档案快照姓名/部门，核查期限（默认30天）驱动红黄灯，Quartz 日调度刷新；</li>
 *   <li>转整改回填 rectify_id 关联 E3 整改台账（source_ref 回指本单号，防重复转办）；</li>
 *   <li>转处分写入干部处分记录（jc_person_punish），回填 punish_id；</li>
 *   <li>同一人员存在在办问题（状态 0/1）时提示（预警性，不强制拦截，一事一议）。</li>
 * </ul>
 *
 * @author 小标
 */
public interface IJcConductService {

    // ═══════════════════════ 问题登记 ═══════════════════════

    /**
     * 登记问题（自动生成编号 CF+yyyyMMdd+seq，档案快照补全姓名/部门，
     * 核查截止日期默认登记日+30天，可用系统参数覆盖）。
     */
    JcConductIssue register(JcConductIssue issue);

    /** 分页列表 */
    List<JcConductIssue> selectJcConductIssueList(JcConductIssue query);

    /** 详情 */
    JcConductIssue selectJcConductIssueByIssueId(Long issueId);

    /** 按编号查详情 */
    JcConductIssue selectByIssueNo(String issueNo);

    /** 修改登记信息（仅待核查状态允许） */
    int updateJcConductIssue(JcConductIssue issue);

    /** 逻辑删除（仅待核查状态允许） */
    int deleteJcConductIssueByIssueId(Long issueId);

    // ═══════════════════════ 核查 / 处置 / 转办 ═══════════════════════

    /** 核查登记（0→1：问题描述/涉及金额/严重程度） */
    JcConductIssue verify(Long issueId, ConductActionRequest req);

    /** 处置登记（1→2：处置方式/日期/说明/退缴金额） */
    JcConductIssue handle(Long issueId, ConductActionRequest req);

    /**
     * 转整改（任意非终态 → 关联 E3 整改台账）。
     *
     * <p>同一问题只允许转一次（查重 jc_rectify_issue.source_ref）；返回携带新整改单号。</p>
     */
    JcConductIssue transferRectify(Long issueId, ConductActionRequest req);

    /**
     * 转处分（1/2 → 写干部处分记录 jc_person_punish）。
     *
     * <p>同一问题只允许转一次；punishType 限字典 jc_punish_type。</p>
     */
    JcConductIssue transferPunish(Long issueId, ConductActionRequest req);

    /** 关闭（0/1/2 → 3） */
    JcConductIssue close(Long issueId, ConductActionRequest req);

    /** 作废（0/1/2 → 4） */
    JcConductIssue cancel(Long issueId, ConductActionRequest req);

    // ═══════════════════════ 辅助与统计 ═══════════════════════

    /**
     * 登记退缴明细（违规收送礼品礼金等多笔退缴场景）。
     *
     * <p>登记后自动重算主表 amount_recovered（= 该问题有效退缴明细合计）。</p>
     */
    JcConductRecovery addRecovery(Long issueId, JcConductRecovery recovery);

    /** 按问题查退缴明细 */
    List<JcConductRecovery> listRecoveries(Long issueId);

    /** 删除退缴登记（同步重算主表 amount_recovered） */
    int deleteRecovery(Long recoveryId);

    /** 人员检索（登记时选人：姓名/编号模糊） */
    List<Map<String, Object>> searchPersons(String keyword);

    /** 刷新红黄灯（Quartz 日调度调用；口径同线索/整改：核查超期=红灯，临期=黄灯） */
    int refreshWarnLevel();

    /** 统计汇总 */
    ConductStat stat();
}
