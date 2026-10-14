package com.jiancha.biz.rectify.service;

import java.util.List;

import com.jiancha.biz.rectify.domain.JcRectifyIssue;
import com.jiancha.biz.rectify.domain.JcRectifyReview;
import com.jiancha.biz.rectify.domain.JcRectifyTask;
import com.jiancha.biz.rectify.domain.RectifyActionRequest;
import com.jiancha.biz.rectify.domain.RectifyStat;

/**
 * 整改闭环台账 Service（E3）
 *
 * <p>问题登记（审计/检查/巡察/预警/举报/线索）→ 整改措施 → 提交复核 → 销号 → 回头看。
 * 预警类来源由 M10 风控模块写入、举报/线索类可由 M3 举报一键登记，source_ref 全程回指。</p>
 *
 * @author 小标
 */
public interface IJcRectifyService {

    // ═══════════════════════ 问题登记 ═══════════════════════

    /** 登记问题（自动生成编号，幂等：同 source_ref 已存在则直接返回既有问题） */
    JcRectifyIssue register(JcRectifyIssue issue);

    /** 从舞弊举报一键登记整改问题（预填标题/描述/来源，幂等） */
    JcRectifyIssue registerFromPetition(Long petitionId, Long targetDeptId, String problemType, String severity);

    /** 分页列表 */
    List<JcRectifyIssue> selectJcRectifyIssueList(JcRectifyIssue jcRectifyIssue);

    /** 详情 */
    JcRectifyIssue selectJcRectifyIssueByIssueId(Long issueId);

    /** 修改问题登记信息 */
    int updateJcRectifyIssue(JcRectifyIssue issue);

    /** 逻辑删除（级联措施与复核记录） */
    int deleteJcRectifyIssueByIssueId(Long issueId);

    // ═══════════════════════ 整改流转 ═══════════════════════

    /**
     * 状态机流转。
     *
     * <p>start 开始整改(0→1)｜submit 提交复核(1→2，至少一条措施已完成)｜pass 销号(2→3)
     * ｜reopen 复核不通过退回(2→1)｜revisit 回头看(3→1 重开 / 通过仅留痕)｜void 作废(→4)</p>
     */
    JcRectifyIssue handleAction(Long issueId, RectifyActionRequest req);

    // ═══════════════════════ 整改措施 ═══════════════════════

    /** 按问题查询措施 */
    List<JcRectifyTask> selectTaskByIssueId(Long issueId);

    /** 新增措施 */
    int insertTask(JcRectifyTask task);

    /** 更新措施（完成时同步 done_date） */
    int updateTask(JcRectifyTask task);

    /** 删除措施 */
    int deleteTaskByTaskId(Long taskId);

    // ═══════════════════════ 复核记录 ═══════════════════════

    /** 按问题查询复核/回头看记录 */
    List<JcRectifyReview> selectReviewByIssueId(Long issueId);

    // ═══════════════════════ 预警与统计 ═══════════════════════

    /** 刷新红黄灯（Quartz 日调度调用） */
    int refreshWarnLevel();

    /** 统计汇总 */
    RectifyStat stat();
}
