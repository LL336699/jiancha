package com.jiancha.biz.supplier.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

import com.jiancha.biz.rectify.domain.JcRectifyIssue;
import com.jiancha.biz.rectify.service.IJcRectifyService;
import com.jiancha.biz.supplier.domain.JcSupplier;
import com.jiancha.biz.supplier.domain.JcSupplierLink;
import com.jiancha.biz.supplier.domain.SupplierActionRequest;
import com.jiancha.biz.supplier.mapper.JcSupplierLinkMapper;
import com.jiancha.biz.supplier.mapper.JcSupplierMapper;
import com.jiancha.biz.supplier.mapper.JcSupplierScanMapper;
import com.jiancha.biz.supplier.service.IJcSupplierLinkService;

/**
 * 供应商关联排查 Service 实现（E4）
 *
 * <p>设计要点：</p>
 * <ul>
 *   <li>系统比对（scanMatch）：聚合 E1 利益冲突申报 / 关联关系中员工申报的关联企业，
 *      按信用代码优先、名称兜底与供应商库匹配，命中即落 jc_supplier_link
 *      （source_type=系统比对），供应商自动转「观察」；</li>
 *   <li>比对去重：同供应商同人员同类型同来源的比对记录不重复落库；</li>
 *   <li>一键转整改：调 E3 整改模块登记 source_type=supplier 的问题（幂等），
 *      issue_id 回填关联记录，形成「供应商关联 → 核实 → 整改 → 复核」闭环。</li>
 * </ul>
 *
 * @author 小标
 */
@Service
public class JcSupplierLinkServiceImpl implements IJcSupplierLinkService {

    /** 发现渠道-系统比对 */
    private static final String SOURCE_AUTO = "系统比对";

    /** 档案状态-观察 */
    private static final String STATUS_OBSERVE = "2";

    /** 处置结果 */
    private static final String HANDLE_PENDING = "0"; // 待处置
    private static final String HANDLE_NORMAL  = "1"; // 正常
    private static final String HANDLE_OBSERVE = "2"; // 观察
    private static final String HANDLE_DENY    = "3"; // 禁入
    private static final String HANDLE_CLEAR   = "4"; // 解除关联

    @Autowired
    private JcSupplierLinkMapper linkMapper;

    @Autowired
    private JcSupplierMapper supplierMapper;

    @Autowired
    private JcSupplierScanMapper scanMapper;

    @Autowired
    private IJcRectifyService rectifyService;

    // ═══════════════════════ 查询 ═══════════════════════

    @Override
    public List<JcSupplierLink> selectJcSupplierLinkList(JcSupplierLink jcSupplierLink) {
        return linkMapper.selectJcSupplierLinkList(jcSupplierLink);
    }

    @Override
    public JcSupplierLink selectJcSupplierLinkByLinkId(Long linkId) {
        return linkMapper.selectJcSupplierLinkByLinkId(linkId);
    }

    @Override
    public List<JcSupplierLink> selectBySupplierId(Long supplierId) {
        return linkMapper.selectBySupplierId(supplierId);
    }

    // ═══════════════════════ 人工登记 ═══════════════════════

    @Override
    public JcSupplierLink add(JcSupplierLink link) {
        if (link == null || link.getSupplierId() == null) {
            throw new ServiceException("供应商不能为空");
        }
        if (link.getPersonId() == null) {
            throw new ServiceException("关联员工不能为空");
        }
        if (StringUtils.isEmpty(link.getLinkType())) {
            throw new ServiceException("关联类型不能为空");
        }
        JcSupplier supplier = supplierMapper.selectJcSupplierBySupplierId(link.getSupplierId());
        if (supplier == null) {
            throw new ServiceException("供应商不存在");
        }
        if (linkMapper.countActiveBySupplierAndPerson(link.getSupplierId(), link.getPersonId(),
                link.getLinkType()) > 0) {
            throw new ServiceException("该员工与供应商的同类型关联已在排查中，请勿重复登记");
        }
        if (StringUtils.isEmpty(link.getSourceType())) {
            link.setSourceType("人工录入");
        }
        if (StringUtils.isEmpty(link.getVerifyStatus())) {
            link.setVerifyStatus("未核实");
        }
        if (StringUtils.isEmpty(link.getHandleResult())) {
            link.setHandleResult(HANDLE_PENDING);
        }
        link.setCreateBy(SecurityUtils.getUsername());
        linkMapper.insertJcSupplierLink(link);
        return linkMapper.selectJcSupplierLinkByLinkId(link.getLinkId());
    }

    // ═══════════════════════ 关联处置 ═══════════════════════

    @Override
    @Transactional
    public JcSupplierLink handle(Long linkId, SupplierActionRequest req) {
        JcSupplierLink link = linkMapper.selectJcSupplierLinkByLinkId(linkId);
        if (link == null) {
            throw new ServiceException("关联记录不存在");
        }
        if (!HANDLE_PENDING.equals(link.getHandleResult())) {
            throw new ServiceException("该关联已处置，不能重复处置");
        }
        if (req == null || StringUtils.isEmpty(req.getHandleResult())) {
            throw new ServiceException("处置结果不能为空");
        }
        if (StringUtils.isEmpty(req.getOpinion())) {
            throw new ServiceException("处置意见不能为空");
        }
        String operator = SecurityUtils.getUsername();
        String handleResult;
        switch (req.getHandleResult()) {
            case "normal":
                handleResult = HANDLE_NORMAL;
                break;
            case "observe":
                handleResult = HANDLE_OBSERVE;
                // 联动档案转观察（观察期内限制新业务准入）
                supplierMapper.updateSupplierStatus(link.getSupplierId(), STATUS_OBSERVE, null, operator);
                break;
            case "deny":
                handleResult = HANDLE_DENY;
                // 联动档案转禁入
                supplierMapper.updateSupplierStatus(link.getSupplierId(), "3", null, operator);
                break;
            case "clear":
                handleResult = HANDLE_CLEAR;
                break;
            default:
                throw new ServiceException("未知处置结果：" + req.getHandleResult());
        }
        linkMapper.updateLinkHandle(linkId, handleResult, "已核实", req.getOpinion(), operator, null);
        return linkMapper.selectJcSupplierLinkByLinkId(linkId);
    }

    // ═══════════════════════ 一键转整改 ═══════════════════════

    @Override
    @Transactional
    public JcSupplierLink toRectify(Long linkId, SupplierActionRequest req) {
        JcSupplierLink link = linkMapper.selectJcSupplierLinkByLinkId(linkId);
        if (link == null) {
            throw new ServiceException("关联记录不存在");
        }
        if (link.getRectifyIssueId() != null) {
            throw new ServiceException("该关联已转整改（问题编号 " + link.getIssueNo() + "），不能重复转办");
        }
        if (req == null || req.getTargetDeptId() == null) {
            throw new ServiceException("转整改必须指定整改责任部门");
        }
        if (StringUtils.isEmpty(req.getOpinion())) {
            throw new ServiceException("核实意见不能为空");
        }
        // E3 登记问题（source_ref 幂等，重复调用返回既有问题）
        JcRectifyIssue issue = new JcRectifyIssue();
        issue.setSourceType("supplier");
        issue.setSourceRef("supplier:" + linkId);
        issue.setTitle("【供应商关联排查转整改】关联员工 " + StringUtils.nvl(link.getPersonName(), "")
                + " × 供应商（关联ID " + linkId + "）");
        issue.setDescription("关联类型：" + StringUtils.nvl(link.getLinkType(), "")
                + "；证据：" + StringUtils.nvl(link.getEvidence(), ""));
        issue.setProblemType("3"); // 廉洁风险
        issue.setSeverity(StringUtils.isEmpty(req.getSeverity()) ? "较大" : req.getSeverity());
        issue.setTargetDeptId(req.getTargetDeptId());
        JcRectifyIssue created = rectifyService.register(issue);
        // 回填 issue_id（独立专用语句，issue_no 一并回写便于展示）
        linkMapper.updateLinkHandle(linkId, link.getHandleResult(), link.getVerifyStatus(),
                req.getOpinion(), SecurityUtils.getUsername(), created.getIssueId());
        link.setRectifyIssueId(created.getIssueId());
        link.setIssueNo(created.getIssueNo());
        return link;
    }

    // ═══════════════════════ 系统比对 ═══════════════════════

    @Override
    @Transactional
    public int scanMatch() {
        int created = 0;
        // 1. 关联关系（jc_person_relation：员工申报的关联企业）
        for (Map<String, Object> row : scanMapper.matchFromPersonRelation()) {
            if (absorb(row)) {
                created++;
            }
        }
        // 2. 利益冲突申报明细（jc_conflict_item）
        for (Map<String, Object> row : scanMapper.matchFromConflictItem()) {
            if (absorb(row)) {
                created++;
            }
        }
        return created;
    }

    /**
     * 吸收一条比对命中：去重 → 落库 → 供应商转观察。
     *
     * @return true=新增了一条比对记录
     */
    private boolean absorb(Map<String, Object> row) {
        Long supplierId = toLong(row.get("supplier_id"));
        Long personId = toLong(row.get("person_id"));
        String linkType = str(row.get("link_type"));
        String source = StringUtils.isEmpty(str(row.get("source_type"))) ? SOURCE_AUTO : str(row.get("source_type"));
        if (supplierId == null || personId == null || StringUtils.isEmpty(linkType)) {
            return false;
        }
        if (linkMapper.countAutoBySupplierAndPerson(supplierId, personId, linkType, source) > 0) {
            return false;
        }
        JcSupplierLink link = new JcSupplierLink();
        link.setSupplierId(supplierId);
        link.setPersonId(personId);
        link.setPersonName(str(row.get("person_name")));
        link.setLinkType(linkType);
        Object ratio = row.get("hold_ratio");
        if (ratio instanceof Number) {
            link.setHoldRatio(new java.math.BigDecimal(ratio.toString()));
        }
        link.setSourceType(source);
        link.setEvidence(str(row.get("evidence")));
        link.setVerifyStatus("未核实");
        link.setHandleResult(HANDLE_PENDING);
        link.setCreateBy("system");
        linkMapper.insertJcSupplierLink(link);
        // 命中即转观察（不阻断既有业务，提示人工排查）
        supplierMapper.updateSupplierStatus(supplierId, STATUS_OBSERVE, null, "system");
        return true;
    }

    private static String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }
}
