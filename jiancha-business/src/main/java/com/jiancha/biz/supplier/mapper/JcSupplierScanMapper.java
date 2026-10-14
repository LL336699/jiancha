package com.jiancha.biz.supplier.mapper;

import java.util.List;
import java.util.Map;

/**
 * 供应商廉洁扫描 Mapper（跨模块只读聚合）
 *
 * <p>与 M10 风控同思路：关联比对需要的员工申报数据（E1 利益冲突申报 /
 * 关联关系）在供应商模块内以只读 SQL 聚合，不反向依赖档案模块的 Mapper。</p>
 *
 * <p>比对口径（与供应商建档信息匹配即命中）：</p>
 * <ul>
 *   <li>优先按统一社会信用代码精确匹配（jc_person_relation.ent_credit_code /
 *       jc_conflict_item.ent_credit_code = jc_supplier.credit_code）</li>
 *   <li>信用代码缺失时按企业名称精确匹配（ent_name = supplier_name）</li>
 * </ul>
 *
 * <p>注意：jc_conflict_declare 的删除标记是 '2'（与其他业务表的 '1' 不同），
 * 关联子表 jc_conflict_item 本身 del_flag='0'，但其所属申报需校验未删除。</p>
 *
 * @author 小标
 */
public interface JcSupplierScanMapper {

    /**
     * 系统比对：员工关联关系中与供应商库匹配的记录（isSupplier='1' 或 ent_credit_code/ent_name 命中）
     *
     * <p>返回字段：supplier_id, supplier_name, person_id, relation_id, link_type,
     * hold_ratio, evidence（含 person_name 便于留痕）。</p>
     */
    List<Map<String, Object>> matchFromPersonRelation();

    /**
     * 系统比对：利益冲突申报明细中与供应商库匹配的记录
     *
     * <p>返回字段：supplier_id, supplier_name, person_id, declare_id, item_id,
     * link_type, hold_ratio, evidence。</p>
     */
    List<Map<String, Object>> matchFromConflictItem();
}
