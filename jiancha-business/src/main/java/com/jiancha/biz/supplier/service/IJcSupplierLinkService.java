package com.jiancha.biz.supplier.service;

import java.util.List;

import com.jiancha.biz.supplier.domain.JcSupplierLink;
import com.jiancha.biz.supplier.domain.SupplierActionRequest;

/**
 * 供应商关联排查 Service（E4）
 *
 * <p>两类入口：人工排查登记（add）与系统自动比对（scanMatch，Quartz 日调度）。
 * 比对数据来自 E1 利益冲突申报与关联关系（跨模块只读聚合），命中即落记录，
 * 供应商自动转「观察」；处置 observe/deny 时联动档案状态，toRectify 一键转
 * E3 整改问题并回填 issue_id。</p>
 *
 * @author 小标
 */
public interface IJcSupplierLinkService {

    /** 分页列表（含供应商名称） */
    List<JcSupplierLink> selectJcSupplierLinkList(JcSupplierLink jcSupplierLink);

    /** 详情 */
    JcSupplierLink selectJcSupplierLinkByLinkId(Long linkId);

    /** 按供应商查关联记录 */
    List<JcSupplierLink> selectBySupplierId(Long supplierId);

    /** 人工登记关联（同供应商同人员同类型未解除记录不允许重复登记） */
    JcSupplierLink add(JcSupplierLink link);

    /**
     * 关联处置。
     *
     * <p>handleResult：normal 正常（供应商不受影响）｜observe 观察（联动档案转观察）
     * ｜deny 禁入（联动档案转禁入）｜clear 解除关联（仅留痕，不动档案）。</p>
     */
    JcSupplierLink handle(Long linkId, SupplierActionRequest req);

    /** 一键转整改（E3 登记问题，source_type=supplier，幂等；回填 issue_id） */
    JcSupplierLink toRectify(Long linkId, SupplierActionRequest req);

    /** 系统比对：扫描 E1 申报/关联关系中命中供应商库的记录并落库，返回新增条数 */
    int scanMatch();
}
