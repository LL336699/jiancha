package com.jiancha.biz.supplier.mapper;

import java.util.List;

import com.jiancha.biz.supplier.domain.JcSupplierLink;

/**
 * 供应商关联排查记录 Mapper
 *
 * @author 小标
 */
public interface JcSupplierLinkMapper {

    /** 分页列表（含供应商名称，JOIN jc_supplier） */
    List<JcSupplierLink> selectJcSupplierLinkList(JcSupplierLink jcSupplierLink);

    /** 详情 */
    JcSupplierLink selectJcSupplierLinkByLinkId(Long linkId);

    /** 按供应商查关联记录 */
    List<JcSupplierLink> selectBySupplierId(Long supplierId);

    /** 查重：同供应商同人员同关联类型且未解除的记录是否存在（人工登记去重） */
    int countActiveBySupplierAndPerson(Long supplierId, Long personId, String linkType);

    /** 查重：系统比对记录是否已存在（同供应商同人员同类型同来源，避免重复落比对结果） */
    int countAutoBySupplierAndPerson(Long supplierId, Long personId, String linkType, String sourceType);

    /** 新增关联记录 */
    int insertJcSupplierLink(JcSupplierLink jcSupplierLink);

    /** 更新关联记录（核实/处置/转整改回填） */
    int updateJcSupplierLink(JcSupplierLink jcSupplierLink);

    /** 处置更新专用语句（handle_time/handle_by 必写，handle_opinion 可空由 <if> 处理） */
    int updateLinkHandle(Long linkId, String handleResult, String verifyStatus, String opinion,
                         String handleBy, Long rectifyIssueId);

    /** 统计：总量/系统比对/已转整改/待处置 */
    java.util.Map<String, Object> statCounts();

    /** 统计：关联类型分布 */
    List<java.util.Map<String, Object>> statByLinkType();

    /** 统计：关联命中供应商 TOP10 */
    List<java.util.Map<String, Object>> statTopHitSuppliers();
}
