package com.jiancha.biz.supplier.mapper;

import java.util.List;

import com.jiancha.biz.supplier.domain.JcSupplierBlack;

/**
 * 供应商黑名单 Mapper
 *
 * @author 小标
 */
public interface JcSupplierBlackMapper {

    /** 分页列表（含供应商名称，JOIN jc_supplier） */
    List<JcSupplierBlack> selectJcSupplierBlackList(JcSupplierBlack jcSupplierBlack);

    /** 详情 */
    JcSupplierBlack selectJcSupplierBlackByBlackId(Long blackId);

    /** 按供应商查黑名单记录 */
    List<JcSupplierBlack> selectBySupplierId(Long supplierId);

    /** 当前生效黑名单（status=0） */
    JcSupplierBlack selectActiveBySupplierId(Long supplierId);

    /** 新增黑名单 */
    int insertJcSupplierBlack(JcSupplierBlack jcSupplierBlack);

    /** 更新黑名单（人工解除/自动解除） */
    int updateJcSupplierBlack(JcSupplierBlack jcSupplierBlack);

    /** 逻辑删除 */
    int deleteJcSupplierBlackByBlackId(Long blackId);

    /** Quartz：到期且仍生效的黑名单（联动档案恢复） */
    List<JcSupplierBlack> selectExpiredActive();

    /** 统计：近12个月黑名单新增趋势（月份→数量） */
    List<java.util.Map<String, Object>> statMonthlyTrend();
}
