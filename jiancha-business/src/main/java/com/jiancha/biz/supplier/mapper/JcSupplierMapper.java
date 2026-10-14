package com.jiancha.biz.supplier.mapper;

import java.util.Date;
import java.util.List;

import com.jiancha.biz.supplier.domain.JcSupplier;

/**
 * 供应商廉洁档案 Mapper
 *
 * @author 小标
 */
public interface JcSupplierMapper {

    /** 分页列表（可按名称/编号/信用代码/类别/状态过滤） */
    List<JcSupplier> selectJcSupplierList(JcSupplier jcSupplier);

    /** 详情 */
    JcSupplier selectJcSupplierBySupplierId(Long supplierId);

    /** 按编号查（唯一） */
    JcSupplier selectJcSupplierByCode(String supplierCode);

    /** 按信用代码查（唯一） */
    JcSupplier selectJcSupplierByCreditCode(String creditCode);

    /** 查重：同名称是否已存在（含逻辑删除外） */
    int countByName(String supplierName);

    /** 新增供应商 */
    int insertJcSupplier(JcSupplier jcSupplier);

    /** 更新供应商 */
    int updateJcSupplier(JcSupplier jcSupplier);

    /** 逻辑删除 */
    int deleteJcSupplierBySupplierId(Long supplierId);

    /**
     * 更新状态与准入日期（准入审查/禁入/拉黑联动）。
     *
     * <p>专用语句：不经过通用 update 的 <if> 拼接，保证 status 必写；
     * 恢复「正常」场景请用 updateSupplierNormal（需要把 black_until 置空，
     * 通用 update 的 <if test="blackUntil != null"> 会跳过置空）。</p>
     */
    int updateSupplierStatus(Long supplierId, String status, Date admitDate, String updateBy);

    /** 恢复正常状态（清空 black_until，用于黑名单解除/禁入解除） */
    int updateSupplierNormal(Long supplierId, String status, String updateBy);

    /** 编号最大前缀（生成 supplierCode 用） */
    String selectMaxCodeByPrefix(String prefix);

    /** 统计：总量/待审查/正常/观察/禁入/黑名单 */
    java.util.Map<String, Object> statCounts();

    /** 统计：状态分布（字典值→数量） */
    List<java.util.Map<String, Object>> statByStatus();

    /** 统计：类别分布 */
    List<java.util.Map<String, Object>> statByCategory();

    /** 读取 sys_config 参数值（跨模块只读，避免依赖 sys 模块 Mapper） */
    String selectConfigValue(String configKey);
}
