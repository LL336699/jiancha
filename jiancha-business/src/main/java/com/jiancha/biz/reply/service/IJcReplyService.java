package com.jiancha.biz.reply.service;

import java.util.List;
import java.util.Map;

import com.jiancha.biz.reply.domain.JcReplyRequest;
import com.jiancha.biz.reply.domain.JcReplyScreenItem;
import com.jiancha.biz.reply.domain.ReplyActionRequest;
import com.jiancha.biz.reply.domain.ReplyScreenResult;
import com.jiancha.biz.reply.domain.ReplyStat;

/**
 * 廉政意见回复 Service（E2）
 *
 * <p>业务口径（企业版）：提拔/任用/评优/出国境/职称评聘前，纪检部门应函件要求
 * 对相关人员出具廉洁意见。流程：申请登记 → 系统自动筛查（跨六大模块聚合：
 * 问题线索/舞弊举报/整改问题/风险预警/供应商关联/廉政处分）→ 人工核实 →
 * 出具正式结论（无违纪/暂缓/否决）→ 归档。</p>
 *
 * <ul>
 *   <li>系统建议结论仅供参考，正式结论必须人工出具并留痕；</li>
 *   <li>办理时限（收到日期 + deadline_days）驱动红黄灯，Quartz 日调度刷新；</li>
 *   <li>同一人员同一事项在进行中不允许重复登记。</li>
 * </ul>
 *
 * @author 小标
 */
public interface IJcReplyService {

    // ═══════════════════════ 申请登记 ═══════════════════════

    /**
     * 登记申请（自动生成编号 RQ+yyyyMMdd+seq）。
     *
     * <p>同一人员同一事项存在进行中申请（状态 0/1）时拒绝重复登记。</p>
     */
    JcReplyRequest register(JcReplyRequest request);

    /** 分页列表 */
    List<JcReplyRequest> selectJcReplyRequestList(JcReplyRequest query);

    /** 详情 */
    JcReplyRequest selectJcReplyRequestByRequestId(Long requestId);

    /** 修改登记信息（仅待筛查状态允许） */
    int updateJcReplyRequest(JcReplyRequest request);

    /** 逻辑删除（级联筛查明细） */
    int deleteJcReplyRequestByRequestId(Long requestId);

    // ═══════════════════════ 筛查与回复 ═══════════════════════

    /**
     * 系统自动筛查（0 → 1）。
     *
     * <p>跨六大模块只读聚合，命中间明细落库（先清后写），
     * 按风险权重汇总系统建议结论（建议通过/建议暂缓/建议否决）。</p>
     */
    ReplyScreenResult screenRequest(Long requestId);

    /**
     * 出具正式结论（1 → 2）。
     *
     * <p>finalResult 限字典 jc_reply_conclusion：无违纪/暂缓/否决；replyContent 必填。</p>
     */
    JcReplyRequest reply(Long requestId, ReplyActionRequest req);

    /** 关闭申请（0/1 → 3，事项取消或来文撤回） */
    JcReplyRequest close(Long requestId);

    /** 按申请查筛查明细 */
    List<JcReplyScreenItem> selectScreenItems(Long requestId);

    /**
     * 批量系统筛查（成批征求意见场景，如集中提拔季）。
     *
     * <p>逐条执行筛查，单条失败（非待筛查/已删除）不中断批次，
     * 记入 skipped 列表返回。</p>
     *
     * @return {total, success, results:[ReplyScreenResult], skipped:[{requestId, reason}]}
     */
    Map<String, Object> screenBatch(List<Long> requestIds);

    /** 人员检索（登记时选人：姓名/编号模糊） */
    List<Map<String, Object>> searchPersons(String keyword);

    // ═══════════════════════ 预警与统计 ═══════════════════════

    /** 刷新红黄灯（Quartz 日调度调用；口径同线索/整改：超期=红灯，过半=黄灯） */
    int refreshWarnLevel();

    /** 统计汇总 */
    ReplyStat stat();
}
