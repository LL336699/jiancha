package com.jiancha.biz.clue.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.jiancha.biz.clue.domain.ClueStatItem;
import com.jiancha.biz.clue.domain.JcClue;

/**
 * 问题线索 Mapper
 *
 * @author 小标
 */
public interface JcClueMapper {

    /** 查询线索列表（含关联的人员 / 部门 / 处理人名称） */
    List<JcClue> selectJcClueList(JcClue jcClue);

    /** 查询线索详情 */
    JcClue selectJcClueByClueId(Long clueId);

    /** 新增线索 */
    int insertJcClue(JcClue jcClue);

    /** 修改线索 */
    int updateJcClue(JcClue jcClue);

    /** 删除线索（逻辑删除） */
    int deleteJcClueByClueId(Long clueId);

    /** 查询指定前缀下最大线索编号（编号流水生成用，含已删除记录避免撞唯一索引） */
    String selectMaxNoByPrefix(@Param("prefix") String prefix);

    /** 重复线索候选：同一被反映人在时间窗口内的其它线索 */
    List<JcClue> selectDuplicateCandidates(@Param("personId") Long personId,
                                           @Param("excludeClueId") Long excludeClueId,
                                           @Param("days") int days);

    /** 未办结线索（预警扫描对象） */
    List<JcClue> selectActiveClues();

    /** 更新预警级别 */
    int updateWarnLevel(@Param("clueId") Long clueId, @Param("warnLevel") String warnLevel);

    // ───────────── 统计 ─────────────

    List<ClueStatItem> statBySource();

    List<ClueStatItem> statByViolationType();

    List<ClueStatItem> statByStatus();

    List<ClueStatItem> statByWarnLevel();

    List<ClueStatItem> statByDept();

    List<ClueStatItem> statMonthly(@Param("months") int months);

    /** 未办结总数 */
    int countActive();

    /** 已超期总数 */
    int countOverdue();

    /** 本月新增 */
    int countMonth();

    /** 全部线索数 */
    int countAll();
}
