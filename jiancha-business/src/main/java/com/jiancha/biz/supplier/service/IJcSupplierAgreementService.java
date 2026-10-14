package com.jiancha.biz.supplier.service;

import java.util.List;

import com.jiancha.biz.supplier.domain.JcSupplierAgreement;

/**
 * 供应商廉洁协议 Service（E4）
 *
 * @author 小标
 */
public interface IJcSupplierAgreementService {

    /** 分页列表 */
    List<JcSupplierAgreement> selectJcSupplierAgreementList(JcSupplierAgreement jcSupplierAgreement);

    /** 详情 */
    JcSupplierAgreement selectJcSupplierAgreementByAgreementId(Long agreementId);

    /** 按供应商查协议沿革 */
    List<JcSupplierAgreement> selectBySupplierId(Long supplierId);

    /** 签署协议（编号自动生成；到期日必填且须晚于签署日） */
    JcSupplierAgreement sign(JcSupplierAgreement agreement);

    /** 续签（新到期日必填；renew_count+1，status 回已签署） */
    JcSupplierAgreement renew(Long agreementId, java.util.Date newExpireDate, String opinion);

    /** 解除协议（status=已解除） */
    int lift(Long agreementId);

    /** 逻辑删除 */
    int deleteJcSupplierAgreementByAgreementId(Long agreementId);
}
