package com.jiancha.biz.asset.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jiancha.biz.asset.domain.AssetActionRequest;
import com.jiancha.biz.asset.domain.AssetStat;
import com.jiancha.biz.asset.domain.JcAssetFlow;
import com.jiancha.biz.asset.domain.JcAssetInfo;
import com.jiancha.biz.asset.mapper.JcAssetFlowMapper;
import com.jiancha.biz.asset.mapper.JcAssetInfoMapper;
import com.jiancha.biz.asset.mapper.JcAssetScanMapper;
import com.jiancha.biz.asset.service.IJcAssetService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysConfigService;

/**
 * 涉案财物服务实现（段11 E6）
 */
@Service
public class JcAssetServiceImpl implements IJcAssetService
{
    @Autowired
    private JcAssetInfoMapper assetMapper;

    @Autowired
    private JcAssetFlowMapper flowMapper;

    @Autowired
    private JcAssetScanMapper scanMapper;

    @Autowired
    private ISysConfigService configService;

    /** 处置方式合法值（字典 jc_asset_dispose） */
    private static final String[] DISPOSE_TYPES = {"退还", "移交", "销毁", "收归", "暂存待处"};

    // ═══════════════════════ 查询 ═══════════════════════

    @Override
    public JcAssetInfo selectAssetById(Long assetId)
    {
        return assetMapper.selectJcAssetInfoByAssetId(assetId);
    }

    @Override
    public List<JcAssetInfo> selectAssetList(JcAssetInfo jcAssetInfo)
    {
        return assetMapper.selectJcAssetInfoList(jcAssetInfo);
    }

    @Override
    public JcAssetInfo selectByAssetNo(String assetNo)
    {
        return assetMapper.selectAssetNoByAssetNo(assetNo);
    }

    @Override
    public List<JcAssetFlow> selectFlows(Long assetId)
    {
        return flowMapper.selectByAssetId(assetId);
    }

    // ═══════════════════════ 登记与维护 ═══════════════════════

    @Override
    @Transactional
    public JcAssetInfo register(JcAssetInfo asset)
    {
        if (asset == null)
        {
            throw new ServiceException("请求体不能为空");
        }
        return doRegister(asset);
    }

    @Override
    @Transactional
    public List<JcAssetInfo> registerBatch(List<JcAssetInfo> assets)
    {
        if (assets == null || assets.isEmpty())
        {
            throw new ServiceException("批量登记列表不能为空");
        }
        if (assets.size() > 50)
        {
            throw new ServiceException("单批最多登记 50 件财物");
        }
        List<JcAssetInfo> result = new java.util.ArrayList<>();
        for (JcAssetInfo asset : assets)
        {
            result.add(doRegister(asset));
        }
        return result;
    }

    /** 单条登记逻辑（登记/批量共用） */
    private JcAssetInfo doRegister(JcAssetInfo asset)
    {
        if (asset == null || StringUtils.isEmpty(asset.getItemName()))
        {
            throw new ServiceException("物品名称不能为空");
        }
        if (asset.getItemQty() == null || asset.getItemQty() <= 0)
        {
            throw new ServiceException("物品数量必须大于 0");
        }
        // 来源单号带出涉事人（传了来源才校验并带出）
        if (StringUtils.isNotEmpty(asset.getSourceNo()))
        {
            if (StringUtils.isEmpty(asset.getSourceType()))
            {
                throw new ServiceException("选择来源单号时必须指定来源类型");
            }
            Map<String, Object> src = null;
            if ("conduct".equals(asset.getSourceType()))
            {
                src = scanMapper.findConductByNo(asset.getSourceNo());
            }
            else if ("clue".equals(asset.getSourceType()))
            {
                src = scanMapper.findClueByNo(asset.getSourceNo());
            }
            if (src == null)
            {
                throw new ServiceException("来源单号不存在：" + asset.getSourceNo());
            }
            if (src.get("personId") != null && asset.getPersonId() == null)
            {
                asset.setPersonId(Long.valueOf(String.valueOf(src.get("personId"))));
            }
            if (StringUtils.isEmpty(asset.getPersonName()) && src.get("personName") != null)
            {
                asset.setPersonName(String.valueOf(src.get("personName")));
            }
            if (StringUtils.isEmpty(asset.getPersonDept()) && src.get("personDept") != null)
            {
                asset.setPersonDept(String.valueOf(src.get("personDept")));
            }
        }
        // 人员档案快照补全（姓名/部门以档案为准）
        if (asset.getPersonId() != null)
        {
            Map<String, Object> person = scanMapper.selectPersonBrief(asset.getPersonId());
            if (person != null)
            {
                asset.setPersonName(StringUtils.nvl(String.valueOf(person.get("personName")), asset.getPersonName()));
                asset.setPersonDept(StringUtils.nvl(String.valueOf(person.get("personDept")), asset.getPersonDept()));
            }
        }
        if (StringUtils.isEmpty(asset.getPersonName()))
        {
            throw new ServiceException("必须关联人员档案或填写涉事人姓名");
        }
        if (StringUtils.isEmpty(asset.getItemCategory()))
        {
            throw new ServiceException("物品类别不能为空");
        }
        if (asset.getSeizeDate() == null)
        {
            asset.setSeizeDate(new Date());
        }
        // 处置期限缺省：扣押日 + 系统参数天数（jiancha.asset.disposeDays）
        if (asset.getDisposeDeadline() == null)
        {
            asset.setDisposeDeadline(plusDays(asset.getSeizeDate(), disposeDaysConfig()));
        }
        if (StringUtils.isEmpty(asset.getSourceType()))
        {
            asset.setSourceType("manual");
        }
        asset.setAssetNo(nextAssetNo());
        asset.setStatus("0");
        asset.setCreateBy(SecurityUtils.getUsername());
        assetMapper.insertJcAssetInfo(asset);
        // 首条流水：扣押登记
        writeFlow(asset.getAssetId(), "seize", null, "0", asset.getSeizeDate(),
                StringUtils.nvl(asset.getRemark(), "扣押登记"), null);
        return assetMapper.selectJcAssetInfoByAssetId(asset.getAssetId());
    }

    @Override
    public int updateAsset(JcAssetInfo asset)
    {
        if (asset == null || asset.getAssetId() == null)
        {
            throw new ServiceException("财物ID不能为空");
        }
        asset.setUpdateBy(SecurityUtils.getUsername());
        return assetMapper.updateJcAssetInfo(asset);
    }

    @Override
    public int removeAsset(Long assetId)
    {
        JcAssetInfo db = assetMapper.selectJcAssetInfoByAssetId(assetId);
        if (db == null)
        {
            throw new ServiceException("财物不存在");
        }
        if (!"0".equals(db.getStatus()))
        {
            throw new ServiceException("仅待入库状态的财物允许删除");
        }
        return assetMapper.deleteJcAssetInfoById(assetId, SecurityUtils.getUsername());
    }

    // ═══════════════════════ 状态流转 ═══════════════════════

    @Override
    @Transactional
    public JcAssetInfo store(Long assetId, AssetActionRequest request)
    {
        JcAssetInfo db = requireAsset(assetId);
        if (!"0".equals(db.getStatus()))
        {
            throw new ServiceException("仅待入库状态的财物允许办理入库");
        }
        if (StringUtils.isEmpty(request.getKeeper()))
        {
            throw new ServiceException("保管人不能为空");
        }
        if (StringUtils.isEmpty(request.getStoragePlace()))
        {
            throw new ServiceException("存放地点不能为空");
        }
        Date storeDate = request.getActionDate() == null ? new Date() : request.getActionDate();
        assetMapper.updateStore(assetId, request.getKeeper(), request.getStoragePlace(), storeDate,
                SecurityUtils.getUsername());
        writeFlow(assetId, "store", "0", "1", storeDate,
                "入库暂存：" + request.getKeeper() + " / " + request.getStoragePlace(), request.getVoucherNo());
        return assetMapper.selectJcAssetInfoByAssetId(assetId);
    }

    @Override
    @Transactional
    public JcAssetInfo handover(Long assetId, AssetActionRequest request)
    {
        JcAssetInfo db = requireAsset(assetId);
        if (!"1".equals(db.getStatus()) && !"2".equals(db.getStatus()))
        {
            throw new ServiceException("仅暂存中或处置中的财物允许移交保管");
        }
        if (StringUtils.isEmpty(request.getKeeper()))
        {
            throw new ServiceException("接收方（新保管人）不能为空");
        }
        Date handoverDate = request.getActionDate() == null ? new Date() : request.getActionDate();
        assetMapper.updateHandover(assetId, request.getKeeper(), request.getStoragePlace(),
                SecurityUtils.getUsername());
        writeFlow(assetId, "handover", db.getStatus(), db.getStatus(), handoverDate,
                "移交保管：" + StringUtils.nvl(db.getKeeper(), "无") + " → " + request.getKeeper(),
                request.getVoucherNo());
        return assetMapper.selectJcAssetInfoByAssetId(assetId);
    }

    @Override
    @Transactional
    public JcAssetInfo disposeApply(Long assetId, AssetActionRequest request)
    {
        JcAssetInfo db = requireAsset(assetId);
        if (!"1".equals(db.getStatus()))
        {
            throw new ServiceException("仅暂存中的财物允许申请处置");
        }
        if (StringUtils.isEmpty(request.getDisposeType()))
        {
            throw new ServiceException("处置方式不能为空");
        }
        if (!isValidDisposeType(request.getDisposeType()))
        {
            throw new ServiceException("处置方式不合法：" + request.getDisposeType());
        }
        if (request.getDisposeDeadline() == null)
        {
            throw new ServiceException("处置期限不能为空");
        }
        assetMapper.updateDisposeApply(assetId, request.getDisposeType(), request.getDisposeDeadline(),
                SecurityUtils.getUsername());
        writeFlow(assetId, "disposeApply", "1", "2", new Date(),
                "处置申请：" + request.getDisposeType() + "，期限 " + fmtDate(request.getDisposeDeadline()), null);
        return assetMapper.selectJcAssetInfoByAssetId(assetId);
    }

    @Override
    @Transactional
    public JcAssetInfo disposeDone(Long assetId, AssetActionRequest request)
    {
        JcAssetInfo db = requireAsset(assetId);
        if (!"2".equals(db.getStatus()))
        {
            throw new ServiceException("仅处置中的财物允许执行处置");
        }
        if (StringUtils.isEmpty(request.getDisposeType()))
        {
            throw new ServiceException("处置方式不能为空");
        }
        if (!isValidDisposeType(request.getDisposeType()))
        {
            throw new ServiceException("处置方式不合法：" + request.getDisposeType());
        }
        Date disposeDate = request.getActionDate() == null ? new Date() : request.getActionDate();
        assetMapper.updateDisposeDone(assetId, request.getDisposeType(), disposeDate,
                request.getDisposeDesc(), SecurityUtils.getUsername());
        writeFlow(assetId, "dispose", "2", "3", disposeDate,
                "处置执行：" + request.getDisposeType()
                        + (StringUtils.isNotEmpty(request.getDisposeDesc()) ? "，" + request.getDisposeDesc() : ""),
                request.getVoucherNo());
        return assetMapper.selectJcAssetInfoByAssetId(assetId);
    }

    @Override
    @Transactional
    public JcAssetInfo refund(Long assetId, AssetActionRequest request)
    {
        JcAssetInfo db = requireAsset(assetId);
        if (!"1".equals(db.getStatus()))
        {
            throw new ServiceException("仅暂存中的财物允许直接退还");
        }
        Date refundDate = request.getActionDate() == null ? new Date() : request.getActionDate();
        assetMapper.updateRefund(assetId, refundDate, request.getDisposeDesc(), SecurityUtils.getUsername());
        writeFlow(assetId, "refund", "1", "4", refundDate,
                "退还：" + (StringUtils.isNotEmpty(request.getDisposeDesc()) ? request.getDisposeDesc() : "审批后退还"),
                request.getVoucherNo());
        return assetMapper.selectJcAssetInfoByAssetId(assetId);
    }

    // ═══════════════════════ 辅助与统计 ═══════════════════════

    @Override
    public List<Map<String, Object>> searchPersons(String keyword)
    {
        return scanMapper.searchPersons(keyword);
    }

    @Override
    public AssetStat stat()
    {
        AssetStat stat = new AssetStat();
        List<Map<String, Object>> cards = new java.util.ArrayList<>();
        cards.add(card("财物总量", assetMapper.countTotal()));
        cards.add(card("暂存中", assetMapper.countByStatus("1")));
        cards.add(card("处置中", assetMapper.countByStatus("2")));
        cards.add(card("已处置", assetMapper.countByStatus("3")));
        cards.add(card("已退还", assetMapper.countByStatus("4")));
        cards.add(card("超期未处置", assetMapper.countOverdue()));
        cards.add(card("暂存估值总额(元)", assetMapper.sumEstimatedValue()));
        stat.setCards(cards);
        stat.setByCategory(assetMapper.statByCategory());
        stat.setByStatus(assetMapper.statByStatus());
        stat.setBySource(assetMapper.statBySource());
        stat.setByKeeper(assetMapper.statByKeeper());
        stat.setMonthlySeize(assetMapper.statMonthlySeize());
        return stat;
    }

    @Override
    public int refreshWarnLevels()
    {
        return assetMapper.refreshWarnLevels();
    }

    // ═══════════════════════ 私有方法 ═══════════════════════

    /** 取财物或抛异常 */
    private JcAssetInfo requireAsset(Long assetId)
    {
        JcAssetInfo db = assetMapper.selectJcAssetInfoByAssetId(assetId);
        if (db == null)
        {
            throw new ServiceException("财物不存在或已删除");
        }
        return db;
    }

    /** 生成财物编号 CA+yyyyMMdd+seq */
    private String nextAssetNo()
    {
        String day = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String prefix = "CA" + day;
        String maxNo = assetMapper.selectMaxNoByPrefix(prefix);
        int seq = 1;
        if (StringUtils.isNotEmpty(maxNo))
        {
            try
            {
                seq = Integer.parseInt(maxNo.substring(prefix.length())) + 1;
            }
            catch (NumberFormatException ignored)
            {
                seq = 1;
            }
        }
        return prefix + String.format("%03d", seq);
    }

    /** 处置期限默认天数（系统参数，解析失败兜底 30 天） */
    private int disposeDaysConfig()
    {
        try
        {
            String v = configService.selectConfigByKey("jiancha.asset.disposeDays");
            if (StringUtils.isNotEmpty(v))
            {
                return Integer.parseInt(v.trim());
            }
        }
        catch (Exception ignored)
        {
        }
        return 30;
    }

    /** 日期加天数 */
    private Date plusDays(Date date, int days)
    {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTime(date == null ? new Date() : date);
        c.add(java.util.Calendar.DAY_OF_MONTH, days);
        return c.getTime();
    }

    /** 写一条流转流水 */
    private void writeFlow(Long assetId, String flowType, String fromStatus, String toStatus,
                           Date flowDate, String flowDesc, String voucherNo)
    {
        JcAssetFlow flow = new JcAssetFlow();
        flow.setAssetId(assetId);
        flow.setFlowType(flowType);
        flow.setFromStatus(fromStatus);
        flow.setToStatus(toStatus);
        flow.setOperator(SecurityUtils.getUsername());
        flow.setFlowDate(flowDate == null ? new Date() : flowDate);
        flow.setFlowDesc(flowDesc);
        flow.setVoucherNo(voucherNo);
        flow.setCreateBy(SecurityUtils.getUsername());
        flowMapper.insertFlow(flow);
    }

    /** 处置方式合法性 */
    private boolean isValidDisposeType(String disposeType)
    {
        for (String t : DISPOSE_TYPES)
        {
            if (t.equals(disposeType))
            {
                return true;
            }
        }
        return false;
    }

    /** 统计卡片 */
    private Map<String, Object> card(String name, Object value)
    {
        Map<String, Object> m = new HashMap<>();
        m.put("name", name);
        m.put("value", value);
        return m;
    }

    /** 日期格式化 */
    private String fmtDate(Date date)
    {
        return date == null ? "" : new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
}
