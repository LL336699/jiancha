package com.jiancha.biz.supplier.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

import com.jiancha.biz.supplier.domain.JcSupplier;
import com.jiancha.biz.supplier.domain.JcSupplierAgreement;
import com.jiancha.biz.supplier.mapper.JcSupplierAgreementMapper;
import com.jiancha.biz.supplier.mapper.JcSupplierMapper;
import com.jiancha.biz.supplier.service.IJcSupplierAgreementService;

/**
 * 供应商廉洁协议 Service 实现（E4）
 *
 * <p>设计要点：</p>
 * <ul>
 *   <li>签署必填到期日且须晚于签署日；编号自动生成（HA+yyyyMMdd+seq）；</li>
 *   <li>续签走专用语句（sign_date/expire_date 覆盖、renew_count+1、status 回已签署），
 *      避免通用 update 的 <if> 跳过必填字段；</li>
 *   <li>临期/过期状态由 Quartz 日调度统一刷新（refreshStatus），页面不自行计算。</li>
 * </ul>
 *
 * @author 小标
 */
@Service
public class JcSupplierAgreementServiceImpl implements IJcSupplierAgreementService {

    /** 已签署 */
    private static final String STATUS_SIGNED  = "0";
    /** 临期 */
    private static final String STATUS_EXPIRING = "1";
    /** 已过期 */
    private static final String STATUS_EXPIRED  = "2";
    /** 已解除 */
    private static final String STATUS_LIFTED   = "3";

    @Autowired
    private JcSupplierAgreementMapper agreementMapper;

    @Autowired
    private JcSupplierMapper supplierMapper;

    @Override
    public List<JcSupplierAgreement> selectJcSupplierAgreementList(JcSupplierAgreement jcSupplierAgreement) {
        return agreementMapper.selectJcSupplierAgreementList(jcSupplierAgreement);
    }

    @Override
    public JcSupplierAgreement selectJcSupplierAgreementByAgreementId(Long agreementId) {
        return agreementMapper.selectJcSupplierAgreementByAgreementId(agreementId);
    }

    @Override
    public List<JcSupplierAgreement> selectBySupplierId(Long supplierId) {
        return agreementMapper.selectBySupplierId(supplierId);
    }

    @Override
    @Transactional
    public JcSupplierAgreement sign(JcSupplierAgreement agreement) {
        if (agreement == null || agreement.getSupplierId() == null) {
            throw new ServiceException("供应商不能为空");
        }
        JcSupplier supplier = supplierMapper.selectJcSupplierBySupplierId(agreement.getSupplierId());
        if (supplier == null) {
            throw new ServiceException("供应商不存在");
        }
        if (agreement.getSignDate() == null) {
            agreement.setSignDate(new Date());
        }
        if (agreement.getExpireDate() == null) {
            throw new ServiceException("到期日不能为空");
        }
        if (!agreement.getExpireDate().after(agreement.getSignDate())) {
            throw new ServiceException("到期日必须晚于签署日");
        }
        agreement.setAgreementNo(generateAgreementNo());
        agreement.setStatus(STATUS_SIGNED);
        agreement.setRenewCount(0);
        agreement.setCreateBy(SecurityUtils.getUsername());
        agreementMapper.insertJcSupplierAgreement(agreement);
        return agreementMapper.selectJcSupplierAgreementByAgreementId(agreement.getAgreementId());
    }

    @Override
    @Transactional
    public JcSupplierAgreement renew(Long agreementId, Date newExpireDate, String opinion) {
        JcSupplierAgreement agreement = agreementMapper.selectJcSupplierAgreementByAgreementId(agreementId);
        if (agreement == null) {
            throw new ServiceException("协议不存在");
        }
        if (STATUS_LIFTED.equals(agreement.getStatus())) {
            throw new ServiceException("协议已解除，不能续签，请重新签署");
        }
        if (newExpireDate == null) {
            throw new ServiceException("新到期日不能为空");
        }
        if (!newExpireDate.after(new Date())) {
            throw new ServiceException("新到期日必须晚于当前日期");
        }
        agreementMapper.renewAgreement(agreementId, newExpireDate, SecurityUtils.getUsername());
        return agreementMapper.selectJcSupplierAgreementByAgreementId(agreementId);
    }

    @Override
    public int lift(Long agreementId) {
        JcSupplierAgreement agreement = agreementMapper.selectJcSupplierAgreementByAgreementId(agreementId);
        if (agreement == null) {
            throw new ServiceException("协议不存在");
        }
        if (STATUS_LIFTED.equals(agreement.getStatus())) {
            throw new ServiceException("协议已解除");
        }
        return agreementMapper.liftAgreement(agreementId, SecurityUtils.getUsername());
    }

    @Override
    public int deleteJcSupplierAgreementByAgreementId(Long agreementId) {
        return agreementMapper.deleteJcSupplierAgreementByAgreementId(agreementId);
    }

    /** 生成协议编号：HA+yyyyMMdd+seq */
    private String generateAgreementNo() {
        String prefix = "HA" + java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String max = agreementMapper.selectMaxNoByPrefix(prefix);
        int seq = 1;
        if (StringUtils.isNotEmpty(max) && max.length() > prefix.length()) {
            try {
                seq = Integer.parseInt(max.substring(prefix.length())) + 1;
            } catch (NumberFormatException ignored) {
                // 编号被人工改坏时从 1 重排
            }
        }
        return prefix + String.format("%03d", seq);
    }
}
