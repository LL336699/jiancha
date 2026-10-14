package com.jiancha.biz.supplier.service;

import java.util.List;

import com.jiancha.biz.supplier.domain.JcSupplier;
import com.jiancha.biz.supplier.domain.JcSupplierBlack;
import com.jiancha.biz.supplier.domain.SupplierActionRequest;
import com.jiancha.biz.supplier.domain.SupplierStat;

/**
 * 供应商廉洁管理 Service（E4 档案 / 准入审查 / 黑名单 / 统计）
 *
 * @author 小标
 */
public interface IJcSupplierService {

    // ═══════════════════════ 档案管理 ═══════════════════════

    /** 分页列表 */
    List<JcSupplier> selectJcSupplierList(JcSupplier jcSupplier);

    /** 详情 */
    JcSupplier selectJcSupplierBySupplierId(Long supplierId);

    /** 新增供应商（编号自动生成，初始状态=待审查） */
    JcSupplier insertJcSupplier(JcSupplier jcSupplier);

    /** 修改供应商 */
    int updateJcSupplier(JcSupplier jcSupplier);

    /** 逻辑删除（待审查状态才允许删除，留痕后不可恢复） */
    int deleteJcSupplierBySupplierId(Long supplierId);

    // ═══════════════════════ 准入审查 ═══════════════════════

    /**
     * 准入审查。
     *
     * <p>auditResult：pass 转正常（记准入日期）｜observe 转观察｜deny 转禁入。
     * 仅「待审查/观察」状态可审查；正常/禁入/黑名单需走黑名单或解除流程。</p>
     */
    JcSupplier audit(Long supplierId, SupplierActionRequest req);

    // ═══════════════════════ 黑名单 ═══════════════════════

    /** 拉黑（必填原因；blackDays 可空=永久；联动档案转黑名单） */
    JcSupplierBlack black(Long supplierId, SupplierActionRequest req);

    /** 分页列表 */
    List<JcSupplierBlack> selectJcSupplierBlackList(JcSupplierBlack jcSupplierBlack);

    /** 详情 */
    JcSupplierBlack selectJcSupplierBlackByBlackId(Long blackId);

    /** 按供应商查黑名单沿革 */
    List<JcSupplierBlack> selectBlackBySupplierId(Long supplierId);

    /**
     * 解除黑名单。
     *
     * <p>人工提前解除（必填意见）或到期确认解除；解除后档案恢复「正常」，
     * 联动 updateSupplierNormal 清空 black_until。</p>
     */
    JcSupplierBlack lift(Long blackId, SupplierActionRequest req);

    // ═══════════════════════ 定时刷新与统计 ═══════════════════════

    /** Quartz 日调度：协议临期/过期刷新 + 黑名单到期自动解除，返回处理说明 */
    String dailyRefresh();

    /** 统计汇总 */
    SupplierStat stat();
}
