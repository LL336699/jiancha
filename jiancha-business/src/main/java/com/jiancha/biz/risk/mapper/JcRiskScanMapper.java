package com.jiancha.biz.risk.mapper;

import java.util.List;
import java.util.Map;

/**
 * 风控扫描 Mapper（跨模块只读聚合）
 *
 * <p>与 M8 看板同思路：风控引擎需要的跨模块计数在风险模块内以只读 SQL 聚合，
 * 不反向依赖举报 / 线索 / 整改 / 档案各模块的 Mapper，保持模块边界清晰。</p>
 *
 * <p>注意：jc_conflict_declare 的删除标记是 '2'（与其他业务表的 '1' 不同），
 * 查询必须按实库口径写 {@code del_flag = '0'}。</p>
 *
 * @author 小标
 */
public interface JcRiskScanMapper {

    /** R01 举报集中反映：近 N 天同一被举报对象的举报数（去重，不含不予受理） */
    List<Map<String, Object>> countPetitionByTarget(int days);

    /** R02 整改超期：各部门超期未销号问题数 */
    List<Map<String, Object>> countOverdueIssueByDept();

    /** R03 整改回头看不通过：各部门回头看不通过次数 */
    List<Map<String, Object>> countRevisitFailByDept();

    /** R04 线索超期未办结：各部门超期未办结线索数 */
    List<Map<String, Object>> countOverdueClueByDept();

    /** R05 利益冲突申报存疑：各人员存疑申报数 */
    List<Map<String, Object>> countConflictSuspectByPerson();

    /** R06 岗位高风险：各岗位模型权重合计 */
    List<Map<String, Object>> sumPositionWeight();

    /** 预警编号最大前缀（生成 alertNo 用） */
    String selectMaxNoByPrefix(String prefix);
}
