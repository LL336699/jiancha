package com.jiancha.biz.supplier.mapper;

import java.util.Date;
import java.util.List;

import com.jiancha.biz.supplier.domain.JcSupplierAgreement;

/**
 * 供应商廉洁协议 Mapper
 *
 * @author 小标
 */
public interface JcSupplierAgreementMapper {

    /** 分页列表（含供应商名称，JOIN jc_supplier） */
    List<JcSupplierAgreement> selectJcSupplierAgreementList(JcSupplierAgreement jcSupplierAgreement);

    /** 详情 */
    JcSupplierAgreement selectJcSupplierAgreementByAgreementId(Long agreementId);

    /** 按供应商查协议（最新在前） */
    List<JcSupplierAgreement> selectBySupplierId(Long supplierId);

    /** 按编号查（唯一） */
    JcSupplierAgreement selectByAgreementNo(String agreementNo);

    /** 新增协议 */
    int insertJcSupplierAgreement(JcSupplierAgreement jcSupplierAgreement);

    /** 更新协议 */
    int updateJcSupplierAgreement(JcSupplierAgreement jcSupplierAgreement);

    /** 续签专用语句（sign_date=当天，expire_date 顺延，renew_count+1，last_renew_date=当天，status 回已签署） */
    int renewAgreement(Long agreementId, Date newExpireDate, String updateBy);

    /** 解除专用语句（status=已解除） */
    int liftAgreement(Long agreementId, String updateBy);

    /** 逻辑删除 */
    int deleteJcSupplierAgreementByAgreementId(Long agreementId);

    /** Quartz：刷新协议状态（临期/已过期），返回受影响行数 */
    int refreshStatus(int warnDays);

    /** 编号最大前缀（生成 agreementNo 用） */
    String selectMaxNoByPrefix(String prefix);

    /** 统计：有效协议数（已签署+临期）/临期数 */
    java.util.Map<String, Object> statCounts();
}
