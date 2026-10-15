package com.jiancha.biz.reply.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysConfigService;

import com.jiancha.biz.reply.domain.JcReplyRequest;
import com.jiancha.biz.reply.domain.JcReplyScreenItem;
import com.jiancha.biz.reply.domain.ReplyActionRequest;
import com.jiancha.biz.reply.domain.ReplyScreenResult;
import com.jiancha.biz.reply.domain.ReplyStat;
import com.jiancha.biz.reply.mapper.JcReplyRequestMapper;
import com.jiancha.biz.reply.mapper.JcReplyScanMapper;
import com.jiancha.biz.reply.mapper.JcReplyScreenItemMapper;
import com.jiancha.biz.reply.service.IJcReplyService;

/**
 * 廉政意见回复 Service 实现（E2）
 *
 * <p>设计要点（设计文档 §E2，企业版口径）：</p>
 * <ul>
 *   <li>系统筛查只读聚合六大业务模块（线索/举报/整改/预警/供应商关联/处分），
 *       不反向依赖他模块 Mapper，全部 SQL 收敛在本模块 {@link JcReplyScanMapper}；</li>
 *   <li>风险权重按「来源模块 × 来源状态」量纲折算（0-100），汇总得出系统建议结论；
 *       建议结论仅供参考，正式结论必须人工出具；</li>
 *   <li>办理时限 = 收到日期 + deadline_days，红黄灯口径与线索/整改一致
 *       （超期红灯、过半黄灯），Quartz 日调度刷新；</li>
 *   <li>同一人员同一事项在进行中（0/1）不允许重复登记。</li>
 * </ul>
 *
 * @author 小标
 */
@Service
public class JcReplyServiceImpl implements IJcReplyService {

    // ═══════════════════════ 状态机 ═══════════════════════

    /** 待筛查 */
    public static final String STATUS_PENDING  = "0";
    /** 已筛查待核实 */
    public static final String STATUS_SCREENED = "1";
    /** 已回复（终态） */
    public static final String STATUS_REPLIED  = "2";
    /** 已关闭（终态） */
    public static final String STATUS_CLOSED   = "3";

    // ═══════════════════════ 来源模块 ═══════════════════════

    /** 问题线索 */
    public static final String SRC_CLUE     = "问题线索";
    /** 舞弊举报 */
    public static final String SRC_PETITION = "舞弊举报";
    /** 整改问题 */
    public static final String SRC_RECTIFY  = "整改问题";
    /** 风险预警 */
    public static final String SRC_RISK     = "风险预警";
    /** 供应商关联 */
    public static final String SRC_SUPPLIER = "供应商关联";
    /** 廉政处分 */
    public static final String SRC_PUNISH   = "廉政处分";

    // ═══════════════════════ 结论口径 ═══════════════════════

    /** 系统建议：通过 */
    public static final String SUGGEST_PASS   = "建议通过";
    /** 系统建议：暂缓 */
    public static final String SUGGEST_DEFER  = "建议暂缓";
    /** 系统建议：否决 */
    public static final String SUGGEST_REJECT = "建议否决";

    /** 正式结论集合（字典 jc_reply_conclusion） */
    private static final Set<String> CONCLUSION_SET = Set.of("无违纪", "暂缓", "否决");

    /** 权重阈值：达到即建议否决 */
    private static final int WEIGHT_REJECT = 80;
    /** 权重阈值：达到即建议暂缓 */
    private static final int WEIGHT_DEFER  = 50;

    /** 预警级别 */
    private static final String WARN_YELLOW = "黄灯";
    private static final String WARN_RED    = "红灯";

    /** sys_config 键：默认办理时限（日） */
    private static final String CFG_DEADLINE_DAYS = "jiancha.reply.deadline.days";
    /** 默认办理时限（日） */
    private static final int DEFAULT_DEADLINE_DAYS = 5;

    /** 编号前缀 */
    private static final String NO_PREFIX = "RQ";

    @Autowired
    private JcReplyRequestMapper requestMapper;

    @Autowired
    private JcReplyScreenItemMapper screenItemMapper;

    @Autowired
    private JcReplyScanMapper scanMapper;

    @Autowired
    private ISysConfigService configService;

    // ═══════════════════════ 申请登记 ═══════════════════════

    @Override
    @Transactional
    public JcReplyRequest register(JcReplyRequest request) {
        if (request == null) {
            throw new ServiceException("申请信息不能为空");
        }
        if (request.getPersonId() == null) {
            throw new ServiceException("必须关联人员档案（personId）");
        }
        if (StringUtils.isEmpty(request.getMatterType())) {
            throw new ServiceException("事项类型不能为空");
        }
        // 人员档案快照补全（姓名/部门以档案为准）
        Map<String, Object> person = scanMapper.selectPersonBrief(request.getPersonId());
        if (person == null) {
            throw new ServiceException("人员档案不存在或已删除");
        }
        request.setPersonName(StringUtils.nvl(String.valueOf(person.get("personName")), ""));
        if (StringUtils.isEmpty(request.getPersonName())) {
            throw new ServiceException("对象姓名不能为空");
        }
        Object deptObj = person.get("personDept");
        request.setPersonDept(deptObj == null ? StringUtils.nvl(request.getPersonDept(), "") : String.valueOf(deptObj));
        // 重复登记校验：同人同事项在进行中
        JcReplyRequest probe = new JcReplyRequest();
        probe.setPersonId(request.getPersonId());
        probe.setMatterType(request.getMatterType());
        for (JcReplyRequest active : requestMapper.selectJcReplyRequestList(probe)) {
            if (STATUS_PENDING.equals(active.getStatus()) || STATUS_SCREENED.equals(active.getStatus())) {
                throw new ServiceException("该人员同一事项的廉政意见正在办理中（编号 "
                        + active.getRequestNo() + "），不能重复登记");
            }
        }
        if (StringUtils.isEmpty(request.getRequestNo())) {
            request.setRequestNo(generateRequestNo());
        }
        if (StringUtils.isEmpty(request.getStatus())) {
            request.setStatus(STATUS_PENDING);
        }
        if (request.getReceiveDate() == null) {
            request.setReceiveDate(new Date());
        }
        if (request.getDeadlineDays() == null) {
            request.setDeadlineDays(readDeadlineDays());
        }
        request.setCreateBy(SecurityUtils.getUsername());
        int rows = requestMapper.insertJcReplyRequest(request);
        if (rows <= 0) {
            throw new ServiceException("申请登记失败");
        }
        return requestMapper.selectJcReplyRequestByRequestId(request.getRequestId());
    }

    @Override
    public List<JcReplyRequest> selectJcReplyRequestList(JcReplyRequest query) {
        return requestMapper.selectJcReplyRequestList(query);
    }

    @Override
    public JcReplyRequest selectJcReplyRequestByRequestId(Long requestId) {
        return requestMapper.selectJcReplyRequestByRequestId(requestId);
    }

    @Override
    public int updateJcReplyRequest(JcReplyRequest request) {
        if (request == null || request.getRequestId() == null) {
            throw new ServiceException("申请ID不能为空");
        }
        JcReplyRequest exist = requestMapper.selectJcReplyRequestByRequestId(request.getRequestId());
        if (exist == null) {
            throw new ServiceException("申请不存在或已删除");
        }
        if (!STATUS_PENDING.equals(exist.getStatus())) {
            throw new ServiceException("仅待筛查状态允许修改登记信息");
        }
        request.setUpdateBy(SecurityUtils.getUsername());
        return requestMapper.updateJcReplyRequest(request);
    }

    @Override
    @Transactional
    public int deleteJcReplyRequestByRequestId(Long requestId) {
        screenItemMapper.deleteByRequestId(requestId);
        return requestMapper.deleteJcReplyRequestByRequestId(requestId);
    }

    // ═══════════════════════ 筛查与回复 ═══════════════════════

    @Override
    @Transactional
    public ReplyScreenResult screenRequest(Long requestId) {
        JcReplyRequest request = requestMapper.selectJcReplyRequestByRequestId(requestId);
        if (request == null) {
            throw new ServiceException("申请不存在或已删除");
        }
        if (!STATUS_PENDING.equals(request.getStatus())) {
            throw new ServiceException("仅待筛查状态可以执行系统筛查");
        }
        Long personId = request.getPersonId();
        if (personId == null) {
            throw new ServiceException("申请未关联人员档案，无法筛查");
        }

        // 六大模块只读聚合
        List<JcReplyScreenItem> items = new ArrayList<>();
        collect(items, SRC_CLUE,     scanMapper.scanClues(personId));
        collect(items, SRC_PETITION, scanMapper.scanPetitions(personId));
        collect(items, SRC_RECTIFY,  scanMapper.scanRectify(personId));
        collect(items, SRC_RISK,     scanMapper.scanRiskAlerts(personId));
        collect(items, SRC_SUPPLIER, scanMapper.scanSupplierLinks(personId));
        collect(items, SRC_PUNISH,   scanMapper.scanPunishments(personId));

        String operator = SecurityUtils.getUsername();
        // 先清后写（重筛保护）
        screenItemMapper.deleteByRequestId(requestId);
        if (!items.isEmpty()) {
            Date now = new Date();
            for (JcReplyScreenItem item : items) {
                item.setRequestId(requestId);
                item.setScreenTime(now);
                item.setCreateBy(operator);
            }
            screenItemMapper.batchInsert(items);
        }

        int hitCount = items.size();
        int maxWeight = 0;
        for (JcReplyScreenItem item : items) {
            maxWeight = Math.max(maxWeight, item.getRiskWeight() == null ? 0 : item.getRiskWeight());
        }
        String suggest;
        String reason;
        if (hitCount == 0) {
            suggest = SUGGEST_PASS;
            reason = "六大模块未检索到相关记录，系统建议通过";
        } else {
            if (maxWeight >= WEIGHT_REJECT) {
                suggest = SUGGEST_REJECT;
            } else if (maxWeight >= WEIGHT_DEFER) {
                suggest = SUGGEST_DEFER;
            } else {
                suggest = SUGGEST_PASS;
            }
            reason = String.format("共命中 %d 条记录，最高风险权重 %d，系统%s，请人工核实后出具正式结论",
                    hitCount, maxWeight, suggest);
        }
        requestMapper.screenDone(requestId, suggest, hitCount, operator);

        ReplyScreenResult result = new ReplyScreenResult();
        result.setRequestId(requestId);
        result.setItems(screenItemMapper.selectByRequestId(requestId));
        result.setHitCount(hitCount);
        result.setMaxWeight(maxWeight);
        result.setSuggestResult(suggest);
        result.setSuggestReason(reason);
        result.setByModule(toModuleMap(screenItemMapper.countByModule(requestId)));
        return result;
    }

    @Override
    @Transactional
    public JcReplyRequest reply(Long requestId, ReplyActionRequest req) {
        JcReplyRequest request = requestMapper.selectJcReplyRequestByRequestId(requestId);
        if (request == null) {
            throw new ServiceException("申请不存在或已删除");
        }
        if (!STATUS_SCREENED.equals(request.getStatus())) {
            throw new ServiceException("仅已筛查待核实状态可以出具正式结论");
        }
        if (req == null || StringUtils.isEmpty(req.getFinalResult())) {
            throw new ServiceException("正式结论不能为空");
        }
        if (StringUtils.isEmpty(req.getReplyContent())) {
            throw new ServiceException("回复内容不能为空");
        }
        if (!CONCLUSION_SET.contains(req.getFinalResult())) {
            throw new ServiceException("正式结论仅限：无违纪/暂缓/否决");
        }
        String operator = SecurityUtils.getUsername();
        requestMapper.replyDone(requestId, req.getFinalResult(), req.getReplyContent(), new Date(), operator);
        return requestMapper.selectJcReplyRequestByRequestId(requestId);
    }

    @Override
    @Transactional
    public JcReplyRequest close(Long requestId) {
        JcReplyRequest request = requestMapper.selectJcReplyRequestByRequestId(requestId);
        if (request == null) {
            throw new ServiceException("申请不存在或已删除");
        }
        if (STATUS_REPLIED.equals(request.getStatus()) || STATUS_CLOSED.equals(request.getStatus())) {
            throw new ServiceException("已回复或已关闭的申请不能再次关闭");
        }
        requestMapper.closeRequest(requestId, SecurityUtils.getUsername());
        return requestMapper.selectJcReplyRequestByRequestId(requestId);
    }

    @Override
    public List<JcReplyScreenItem> selectScreenItems(Long requestId) {
        return screenItemMapper.selectByRequestId(requestId);
    }

    @Override
    public Map<String, Object> screenBatch(List<Long> requestIds) {
        Map<String, Object> out = new LinkedHashMap<>();
        List<ReplyScreenResult> results = new ArrayList<>();
        List<Map<String, Object>> skipped = new ArrayList<>();
        if (requestIds != null) {
            for (Long id : requestIds) {
                try {
                    results.add(screenRequest(id));
                } catch (ServiceException e) {
                    // 批次容错：单条不合资格（非待筛查/已删除）跳过并记录原因
                    Map<String, Object> skip = new LinkedHashMap<>();
                    skip.put("requestId", id);
                    skip.put("reason", e.getMessage());
                    skipped.add(skip);
                }
            }
        }
        out.put("total", requestIds == null ? 0 : requestIds.size());
        out.put("success", results.size());
        out.put("results", results);
        out.put("skipped", skipped);
        return out;
    }

    @Override
    public List<Map<String, Object>> searchPersons(String keyword) {
        return scanMapper.searchPersons(keyword);
    }

    // ═══════════════════════ 预警与统计 ═══════════════════════

    @Override
    public int refreshWarnLevel() {
        List<JcReplyRequest> list = requestMapper.selectActiveRequests();
        int changed = 0;
        for (JcReplyRequest request : list) {
            String level = calcWarnLevel(request.getReceiveDate(), request.getDeadlineDays(), request.getStatus());
            if (!level.equals(StringUtils.nvl(request.getWarnLevel(), ""))) {
                requestMapper.updateWarnLevel(request.getRequestId(), level);
                changed++;
            }
        }
        return changed;
    }

    @Override
    public ReplyStat stat() {
        ReplyStat stat = new ReplyStat();
        Map<String, Object> cards = new LinkedHashMap<>();
        cards.put("total", requestMapper.countTotal());
        cards.put("pending", requestMapper.countByStatus(STATUS_PENDING));
        cards.put("screened", requestMapper.countByStatus(STATUS_SCREENED));
        cards.put("replied", requestMapper.countReplied());
        cards.put("overdue", requestMapper.countOverdue());
        Double avg = requestMapper.statAvgReplyDays();
        cards.put("avgReplyDays", avg == null ? 0.0 : Math.round(avg * 10.0) / 10.0);
        stat.setCards(cards);
        stat.setByMatter(requestMapper.statByMatter());
        stat.setByResult(requestMapper.statByResult());
        stat.setMonthlyRequests(requestMapper.statMonthlyRequests());
        stat.setByDept(requestMapper.statByDept());
        stat.setHitsByModule(screenItemMapper.statHitsByModule());
        return stat;
    }

    // ═══════════════════════ 内部方法 ═══════════════════════

    /** 汇总单条扫描结果为筛查明细（权重按「模块 × 状态」折算） */
    private void collect(List<JcReplyScreenItem> items, String module, List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        for (Map<String, Object> row : rows) {
            JcReplyScreenItem item = new JcReplyScreenItem();
            item.setSourceModule(module);
            item.setSourceNo(str(row.get("source_no")));
            item.setSummary(str(row.get("summary")));
            item.setSourceStatus(statusLabel(module, str(row.get("source_status"))));
            item.setRiskWeight(weightOf(module, str(row.get("source_status")), row));
            items.add(item);
        }
    }

    /**
     * 风险权重量纲（0-100）：模块基础严重度 × 来源状态推进度。
     *
     * <p>线索：处置中90/核查中70/已受理50/待受理40；
     * 举报：办理中80/已分办70/研判中60/已受理50/已反馈20/待受理40；
     * 整改：整改中60/待整改50/待复核40；
     * 预警：按等级 高90/中60/低30（无等级时按状态 核实中60/待核实40）；
     * 供应商：禁入80/观察50/待处置40；
     * 处分：近3年固定70。</p>
     */
    private int weightOf(String module, String status, Map<String, Object> row) {
        switch (module) {
            case SRC_CLUE:
                if ("3".equals(status)) return 90;
                if ("2".equals(status)) return 70;
                if ("1".equals(status)) return 50;
                return 40;
            case SRC_PETITION:
                if ("4".equals(status)) return 80;
                if ("3".equals(status)) return 70;
                if ("2".equals(status)) return 60;
                if ("1".equals(status)) return 50;
                if ("5".equals(status)) return 20;
                return 40;
            case SRC_RECTIFY:
                if ("1".equals(status)) return 60;
                if ("0".equals(status)) return 50;
                return 40;
            case SRC_RISK: {
                String level = str(row.get("alert_level"));
                if ("2".equals(level)) return 90;
                if ("1".equals(level)) return 60;
                if ("0".equals(level)) return 30;
                return "1".equals(status) ? 60 : 40;
            }
            case SRC_SUPPLIER:
                if ("3".equals(status)) return 80;
                if ("2".equals(status)) return 50;
                return 40;
            case SRC_PUNISH:
                return 70;
            default:
                return 40;
        }
    }

    /** 来源状态标签（快照展示） */
    private String statusLabel(String module, String status) {
        switch (module) {
            case SRC_CLUE:
                switch (status) {
                    case "0": return "待受理";
                    case "1": return "已受理";
                    case "2": return "核查中";
                    case "3": return "处置中";
                    default:  return status;
                }
            case SRC_PETITION:
                switch (status) {
                    case "0": return "待受理";
                    case "1": return "已受理";
                    case "2": return "研判中";
                    case "3": return "已分办";
                    case "4": return "办理中";
                    case "5": return "已反馈";
                    default:  return status;
                }
            case SRC_RECTIFY:
                switch (status) {
                    case "0": return "待整改";
                    case "1": return "整改中";
                    case "2": return "待复核";
                    default:  return status;
                }
            case SRC_RISK:
                switch (status) {
                    case "0": return "待核实";
                    case "1": return "核实中";
                    default:  return status;
                }
            case SRC_SUPPLIER:
                switch (status) {
                    case "0": return "待处置";
                    case "1": return "正常";
                    case "2": return "观察";
                    case "3": return "禁入";
                    case "4": return "解除";
                    default:  return status;
                }
            default:
                return status;
        }
    }

    /** 分来源计数 List -> Map（sourceModule -> cnt） */
    private Map<String, Object> toModuleMap(List<Map<String, Object>> rows) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                map.put(str(row.get("sourceModule")), row.get("cnt"));
            }
        }
        return map;
    }

    /** 计算预警级别（口径同线索/整改：超期=红灯，窗口过半=黄灯；终态与无时限为空） */
    private String calcWarnLevel(Date receiveDate, Integer deadlineDays, String status) {
        if (STATUS_REPLIED.equals(status) || STATUS_CLOSED.equals(status)) {
            return "";
        }
        if (receiveDate == null || deadlineDays == null || deadlineDays <= 0) {
            return "";
        }
        Date deadline = plusDays(receiveDate, deadlineDays);
        long remain = daysBetween(new Date(), deadline);
        if (remain <= 0) {
            return WARN_RED;
        }
        double ratio = (double) (deadlineDays - remain) / (double) deadlineDays;
        return ratio >= 0.5 ? WARN_YELLOW : "";
    }

    /** 生成申请编号：RQ+yyyyMMdd+seq */
    private String generateRequestNo() {
        String prefix = NO_PREFIX + java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String max = requestMapper.selectMaxNoByPrefix(prefix);
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

    private int readDeadlineDays() {
        try {
            String v = configService.selectConfigByKey(CFG_DEADLINE_DAYS);
            if (StringUtils.isNotEmpty(v)) {
                int d = (int) Double.parseDouble(v.trim());
                if (d > 0) {
                    return d;
                }
            }
        } catch (Exception ignored) {
            // 配置缺失走兜底
        }
        return DEFAULT_DEADLINE_DAYS;
    }

    private static Date plusDays(Date base, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(base);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return cal.getTime();
    }

    private static long daysBetween(Date from, Date to) {
        return (to.getTime() - from.getTime()) / (86400L * 1000L);
    }

    /** Map 值 -> String（null 安全） */
    private static String str(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
