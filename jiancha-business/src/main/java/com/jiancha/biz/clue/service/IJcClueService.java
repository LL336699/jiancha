package com.jiancha.biz.clue.service;

import java.util.List;
import java.util.Map;
import com.jiancha.biz.clue.domain.ClueActionRequest;
import com.jiancha.biz.clue.domain.ClueStatItem;
import com.jiancha.biz.clue.domain.JcClue;

/**
 * 问题线索 Service
 *
 * @author 小标
 */
public interface IJcClueService {

    /** 查询线索列表 */
    List<JcClue> selectJcClueList(JcClue jcClue);

    /** 查询线索详情 */
    JcClue selectJcClueByClueId(Long clueId);

    /** 新增线索（自动生成编号、计算时限、写受理轨迹） */
    int insertJcClue(JcClue jcClue);

    /** 修改线索 */
    int updateJcClue(JcClue jcClue);

    /** 删除线索（逻辑删除，同步清理轨迹） */
    int deleteJcClueByClueId(Long clueId);

    /**
     * 线索流转。受理 / 研判 / 分办 / 处置 / 办结 / 退回 / 不予受理，全程写轨迹。
     * 操作人取当前登录用户（SecurityUtils）。
     *
     * @param clueId 线索ID
     * @param req    流转请求
     * @return 流转后的线索
     */
    JcClue handleAction(Long clueId, ClueActionRequest req);

    /** 重复线索检测：同一被反映人 + 时间窗口内的其它线索 */
    List<JcClue> detectDuplicate(Long personId, Long excludeClueId, int days);

    /** 生成线索编号 */
    String generateClueNo();

    /** 刷新红黄灯预警（定时任务调用），返回变更条数 */
    int refreshWarnLevel();

    // ───────── 统计 ─────────

    List<ClueStatItem> statBySource();

    List<ClueStatItem> statByViolationType();

    List<ClueStatItem> statByStatus();

    List<ClueStatItem> statByWarnLevel();

    List<ClueStatItem> statByDept();

    List<ClueStatItem> statMonthly(int months);

    /** 总览：总量 / 在办 / 超期 / 本月 + 各类分布 */
    Map<String, Object> statOverview();
}
