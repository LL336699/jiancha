package com.jiancha.biz.supplier.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

import com.jiancha.biz.supplier.domain.JcSupplier;
import com.jiancha.biz.supplier.domain.JcSupplierAgreement;
import com.jiancha.biz.supplier.domain.JcSupplierBlack;
import com.jiancha.biz.supplier.domain.SupplierActionRequest;
import com.jiancha.biz.supplier.domain.SupplierStat;
import com.jiancha.biz.supplier.mapper.JcSupplierAgreementMapper;
import com.jiancha.biz.supplier.mapper.JcSupplierBlackMapper;
import com.jiancha.biz.supplier.mapper.JcSupplierLinkMapper;
import com.jiancha.biz.supplier.mapper.JcSupplierMapper;
import com.jiancha.biz.supplier.service.IJcSupplierService;

/**
 * 供应商廉洁管理 Service 实现（E4：档案 / 准入审查 / 黑名单 / 统计）
 *
 * <p>设计要点：</p>
 * <ul>
 *   <li>档案状态机：待审查 → 正常 / 观察 / 禁入 / 黑名单，恢复类动作走专用语句
 *       updateSupplierNormal 清空 black_until（通用 update 的 <if> 会跳过置空）；</li>
 *   <li>拉黑必填原因与期限（可空=永久），到期由 Quartz 自动解除；</li>
 *   <li>准入审查 / 拉黑 / 解除全部联动档案状态，全程 create_by/update_by 留痕。</li>
 * </ul>
 *
 * @author 小标
 */
@Service
public class JcSupplierServiceImpl implements IJcSupplierService {

    /** 档案状态（字典 jc_supplier_status 顺序值） */
    private static final String STATUS_PENDING  = "0"; // 待审查
    private static final String STATUS_NORMAL   = "1"; // 正常
    private static final String STATUS_OBSERVE  = "2"; // 观察
    private static final String STATUS_DENY     = "3"; // 禁入
    private static final String STATUS_BLACK    = "4"; // 黑名单

    /** 黑名单状态 */
    private static final String BLACK_ACTIVE = "0";
    private static final String BLACK_LIFTED = "1";

    /** 审查结果 */
    private static final String AUDIT_PASS    = "pass";
    private static final String AUDIT_OBSERVE = "observe";
    private static final String AUDIT_DENY    = "deny";

    @Autowired
    private JcSupplierMapper supplierMapper;

    @Autowired
    private JcSupplierBlackMapper blackMapper;

    @Autowired
    private JcSupplierLinkMapper linkMapper;

    @Autowired
    private JcSupplierAgreementMapper agreementMapper;

    // ═══════════════════════ 档案管理 ═══════════════════════

    @Override
    public List<JcSupplier> selectJcSupplierList(JcSupplier jcSupplier) {
        return supplierMapper.selectJcSupplierList(jcSupplier);
    }

    @Override
    public JcSupplier selectJcSupplierBySupplierId(Long supplierId) {
        return supplierMapper.selectJcSupplierBySupplierId(supplierId);
    }

    @Override
    public JcSupplier insertJcSupplier(JcSupplier jcSupplier) {
        if (jcSupplier == null || StringUtils.isEmpty(jcSupplier.getSupplierName())) {
            throw new ServiceException("供应商名称不能为空");
        }
        if (StringUtils.isEmpty(jcSupplier.getCreditCode())) {
            throw new ServiceException("统一社会信用代码不能为空（关联比对依据）");
        }
        if (supplierMapper.countByName(jcSupplier.getSupplierName().trim()) > 0) {
            throw new ServiceException("同名供应商已存在，请核实后登记");
        }
        JcSupplier dup = supplierMapper.selectJcSupplierByCreditCode(jcSupplier.getCreditCode().trim());
        if (dup != null) {
            throw new ServiceException("信用代码 " + jcSupplier.getCreditCode() + " 已被供应商「" + dup.getSupplierName() + "」登记");
        }
        jcSupplier.setSupplierCode(generateSupplierCode());
        if (StringUtils.isEmpty(jcSupplier.getStatus())) {
            jcSupplier.setStatus(STATUS_PENDING);
        }
        jcSupplier.setCreateBy(SecurityUtils.getUsername());
        supplierMapper.insertJcSupplier(jcSupplier);
        return supplierMapper.selectJcSupplierBySupplierId(jcSupplier.getSupplierId());
    }

    @Override
    public int updateJcSupplier(JcSupplier jcSupplier) {
        if (jcSupplier == null || jcSupplier.getSupplierId() == null) {
            throw new ServiceException("供应商ID不能为空");
        }
        jcSupplier.setUpdateBy(SecurityUtils.getUsername());
        return supplierMapper.updateJcSupplier(jcSupplier);
    }

    @Override
    public int deleteJcSupplierBySupplierId(Long supplierId) {
        JcSupplier supplier = supplierMapper.selectJcSupplierBySupplierId(supplierId);
        if (supplier == null) {
            throw new ServiceException("供应商不存在");
        }
        if (!STATUS_PENDING.equals(supplier.getStatus())) {
            throw new ServiceException("仅「待审查」状态的供应商允许删除，请走禁入/黑名单流程留痕");
        }
        return supplierMapper.deleteJcSupplierBySupplierId(supplierId);
    }

    // ═══════════════════════ 准入审查 ═══════════════════════

    @Override
    @Transactional
    public JcSupplier audit(Long supplierId, SupplierActionRequest req) {
        JcSupplier supplier = supplierMapper.selectJcSupplierBySupplierId(supplierId);
        if (supplier == null) {
            throw new ServiceException("供应商不存在");
        }
        if (!STATUS_PENDING.equals(supplier.getStatus()) && !STATUS_OBSERVE.equals(supplier.getStatus())) {
            throw new ServiceException("仅「待审查/观察」状态可准入审查");
        }
        if (req == null || StringUtils.isEmpty(req.getAuditResult())) {
            throw new ServiceException("审查结果不能为空");
        }
        if (StringUtils.isEmpty(req.getOpinion())) {
            throw new ServiceException("审查意见不能为空");
        }
        String operator = SecurityUtils.getUsername();
        String status;
        Date admitDate = null;
        switch (req.getAuditResult()) {
            case AUDIT_PASS:
                status = STATUS_NORMAL;
                admitDate = new Date();
                break;
            case AUDIT_OBSERVE:
                status = STATUS_OBSERVE;
                admitDate = new Date();
                break;
            case AUDIT_DENY:
                status = STATUS_DENY;
                break;
            default:
                throw new ServiceException("未知审查结果：" + req.getAuditResult());
        }
        supplierMapper.updateSupplierStatus(supplierId, status, admitDate, operator);
        return supplierMapper.selectJcSupplierBySupplierId(supplierId);
    }

    // ═══════════════════════ 黑名单 ═══════════════════════

    @Override
    @Transactional
    public JcSupplierBlack black(Long supplierId, SupplierActionRequest req) {
        JcSupplier supplier = supplierMapper.selectJcSupplierBySupplierId(supplierId);
        if (supplier == null) {
            throw new ServiceException("供应商不存在");
        }
        if (STATUS_BLACK.equals(supplier.getStatus())) {
            throw new ServiceException("供应商已在黑名单，无需重复拉黑");
        }
        if (req == null || StringUtils.isEmpty(req.getBlackReason())) {
            throw new ServiceException("拉黑原因不能为空");
        }
        String operator = SecurityUtils.getUsername();
        JcSupplierBlack black = new JcSupplierBlack();
        black.setSupplierId(supplierId);
        black.setBlackReason(req.getBlackReason());
        black.setBlackDate(new Date());
        if (req.getBlackDays() != null && req.getBlackDays() > 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, req.getBlackDays());
            black.setBlackUntil(calendar.getTime());
        }
        black.setStatus(BLACK_ACTIVE);
        black.setCreateBy(operator);
        blackMapper.insertJcSupplierBlack(black);
        // 联动档案转黑名单（黑名单期间禁入一切新业务）
        supplierMapper.updateSupplierStatus(supplierId, STATUS_BLACK, null, operator);
        return blackMapper.selectJcSupplierBlackByBlackId(black.getBlackId());
    }

    @Override
    public List<JcSupplierBlack> selectJcSupplierBlackList(JcSupplierBlack jcSupplierBlack) {
        return blackMapper.selectJcSupplierBlackList(jcSupplierBlack);
    }

    @Override
    public JcSupplierBlack selectJcSupplierBlackByBlackId(Long blackId) {
        return blackMapper.selectJcSupplierBlackByBlackId(blackId);
    }

    @Override
    public List<JcSupplierBlack> selectBlackBySupplierId(Long supplierId) {
        return blackMapper.selectBySupplierId(supplierId);
    }

    @Override
    @Transactional
    public JcSupplierBlack lift(Long blackId, SupplierActionRequest req) {
        JcSupplierBlack black = blackMapper.selectJcSupplierBlackByBlackId(blackId);
        if (black == null) {
            throw new ServiceException("黑名单记录不存在");
        }
        if (BLACK_LIFTED.equals(black.getStatus())) {
            throw new ServiceException("该黑名单已解除");
        }
        if (req == null || StringUtils.isEmpty(req.getOpinion())) {
            throw new ServiceException("解除意见不能为空");
        }
        String operator = SecurityUtils.getUsername();
        black.setStatus(BLACK_LIFTED);
        black.setLiftDate(new Date());
        black.setLiftBy(operator);
        black.setLiftOpinion(req.getOpinion());
        black.setUpdateBy(operator);
        blackMapper.updateJcSupplierBlack(black);
        // 解除后档案恢复正常（专用语句清空 black_until）
        supplierMapper.updateSupplierNormal(black.getSupplierId(), STATUS_NORMAL, operator);
        return blackMapper.selectJcSupplierBlackByBlackId(blackId);
    }

    // ═══════════════════════ 定时刷新与统计 ═══════════════════════

    @Override
    @Transactional
    public String dailyRefresh() {
        // 1. 协议状态刷新：临期（≤ warnDays）/已过期（> expire_date）
        int warnDays = 30;
        try {
            String cfg = configValue("jiancha.supplier.agreement.warn.days");
            if (StringUtils.isNotEmpty(cfg)) {
                warnDays = Integer.parseInt(cfg.trim());
            }
        } catch (NumberFormatException ignored) {
            // 参数配坏时回落默认 30 天
        }
        int agreementChanged = agreementMapper.refreshStatus(warnDays);

        // 2. 黑名单到期自动解除
        int autoLifted = 0;
        Date today = new Date();
        for (JcSupplierBlack black : blackMapper.selectExpiredActive()) {
            black.setStatus(BLACK_LIFTED);
            black.setLiftDate(today);
            black.setLiftBy("system");
            black.setLiftOpinion("期限届满自动解除");
            black.setUpdateBy("system");
            blackMapper.updateJcSupplierBlack(black);
            supplierMapper.updateSupplierNormal(black.getSupplierId(), STATUS_NORMAL, "system");
            autoLifted++;
        }
        return "协议状态刷新 " + agreementChanged + " 条；黑名单到期自动解除 " + autoLifted + " 条";
    }

    @Override
    public SupplierStat stat() {
        SupplierStat stat = new SupplierStat();
        Map<String, Object> supplierCounts = safeMap(supplierStatCounts());
        stat.setTotalSuppliers(toLong(supplierCounts.get("total")));
        stat.setPendingCount(toLong(supplierCounts.get("pending")));
        stat.setNormalCount(toLong(supplierCounts.get("normal")));
        stat.setObserveCount(toLong(supplierCounts.get("observe")));
        stat.setBlackCount(toLong(supplierCounts.get("deny")) + toLong(supplierCounts.get("black")));

        Map<String, Object> agreementCounts = safeMap(agreementMapper.statCounts());
        stat.setActiveAgreements(toLong(agreementCounts.get("active")));
        stat.setExpiringAgreements(toLong(agreementCounts.get("expiring")));
        long normal = stat.getNormalCount() == null ? 0L : stat.getNormalCount();
        long active = stat.getActiveAgreements() == null ? 0L : stat.getActiveAgreements();
        stat.setAgreementRate(normal == 0 ? 0d : Math.round(active * 1000d / normal) / 1000d);

        Map<String, Object> linkCounts = safeMap(linkMapper.statCounts());
        stat.setLinkTotal(toLong(linkCounts.get("total")));
        stat.setAutoLinkCount(toLong(linkCounts.get("auto")));
        stat.setLinkToRectifyCount(toLong(linkCounts.get("toRectify")));
        stat.setLinkPendingCount(toLong(linkCounts.get("pending")));

        stat.setByStatus(supplierStatByStatus());
        stat.setByCategory(supplierStatByCategory());
        stat.setByLinkType(linkMapper.statByLinkType());
        stat.setBlackTrend(blackTrend());
        stat.setTopHitSuppliers(linkMapper.statTopHitSuppliers());
        return stat;
    }

    // ═══════════════════════ 私有工具 ═══════════════════════

    /** 档案总量/分状态计数（一次 SQL 出行分组，这里用聚合查询） */
    private Map<String, Object> supplierStatCounts() {
        // 复用列表查询太重，直接走聚合 XML（statSupplierCounts）
        return supplierMapper.statCounts();
    }

    private List<Map<String, Object>> supplierStatByStatus() {
        return supplierMapper.statByStatus();
    }

    private List<Map<String, Object>> supplierStatByCategory() {
        return supplierMapper.statByCategory();
    }

    private List<Map<String, Object>> blackTrend() {
        return blackMapper.statMonthlyTrend();
    }

    private String configValue(String key) {
        // sys_config 查询下沉到 mapper 不跨模块，这里用 JcSupplierMapper 的轻量查询
        return supplierMapper.selectConfigValue(key);
    }

    /** 生成供应商编号：SUP+yyyyMMdd+seq */
    private String generateSupplierCode() {
        String prefix = "SUP" + java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String max = supplierMapper.selectMaxCodeByPrefix(prefix);
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

    private static Map<String, Object> safeMap(Map<String, Object> source) {
        return source == null ? new HashMap<>() : source;
    }

    private static Long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }
}
