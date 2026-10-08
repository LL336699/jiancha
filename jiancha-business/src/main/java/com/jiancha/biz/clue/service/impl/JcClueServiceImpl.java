package com.jiancha.biz.clue.service.impl;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysConfigService;

import com.jiancha.biz.clue.domain.ClueActionRequest;
import com.jiancha.biz.clue.domain.ClueStatItem;
import com.jiancha.biz.clue.domain.JcClue;
import com.jiancha.biz.clue.domain.JcClueTrace;
import com.jiancha.biz.clue.mapper.JcClueMapper;
import com.jiancha.biz.clue.mapper.JcClueTraceMapper;
import com.jiancha.biz.clue.service.IJcClueService;

/**
 * 问题线索 Service 实现
 *
 * <p>设计要点（设计文档 §M4）：</p>
 * <ul>
 *   <li>办理时限阈值一律读 {@code sys_config}，代码不硬编码，业务可自行调整；</li>
 *   <li>红黄灯预警：黄灯 = 已用时长达到阈值比例（默认 1/2），红灯 = 已超期；</li>
 *   <li>每一次流转写一条轨迹，全程留痕；</li>
 *   <li>重复线索只做"提示"，不自动合并，合并须人工确认。</li>
 * </ul>
 *
 * @author 小标
 */
@Service
public class JcClueServiceImpl implements IJcClueService {

    /** 办理状态：待受理 */
    public static final String STATUS_PENDING   = "0";
    /** 办理状态：已受理 */
    public static final String STATUS_ACCEPTED  = "1";
    /** 办理状态：核查中 */
    public static final String STATUS_REVIEWING = "2";
    /** 办理状态：处置中 */
    public static final String STATUS_DISPOSING = "3";
    /** 办理状态：已办结 */
    public static final String STATUS_CLOSED    = "4";
    /** 办理状态：不予受理 */
    public static final String STATUS_REJECTED  = "5";

    /** 动作：登记 */
    public static final String ACT_REGISTER = "register";
    /** 动作：受理 */
    public static final String ACT_ACCEPT   = "accept";
    /** 动作：研判 */
    public static final String ACT_REVIEW   = "review";
    /** 动作：分办 */
    public static final String ACT_ASSIGN   = "assign";
    /** 动作：处置 */
    public static final String ACT_DISPOSE  = "dispose";
    /** 动作：办结 */
    public static final String ACT_CLOSE    = "close";
    /** 动作：不予受理 */
    public static final String ACT_REJECT   = "reject";
    /** 动作：退回 */
    public static final String ACT_RETURN   = "return";

    /** 预警：黄灯 */
    public static final String WARN_YELLOW = "黄灯";
    /** 预警：红灯 */
    public static final String WARN_RED    = "红灯";

    /** sys_config：办理时限键前缀（拼接来源类型，如 jiancha.clue.deadline.audit） */
    private static final String CFG_DEADLINE_PREFIX  = "jiancha.clue.deadline.";
    /** sys_config：默认办理时限（日） */
    private static final String CFG_DEADLINE_DEFAULT = "jiancha.clue.deadline.default";
    /** sys_config：黄灯阈值比例 */
    private static final String CFG_WARN_RATIO       = "jiancha.clue.warn.ratio";
    /** 默认时限（日），配置缺失时兜底 */
    private static final int    DEFAULT_DEADLINE_DAYS = 30;
    /** 默认黄灯比例 */
    private static final double DEFAULT_WARN_RATIO    = 0.5;
    /** 编号前缀 */
    private static final String NO_PREFIX = "JC";

    /** 动作 → 目标状态 */
    private static final Map<String, String> ACTION_TARGET = new LinkedHashMap<>();
    /** 动作 → 中文名 */
    private static final Map<String, String> ACTION_LABEL  = new LinkedHashMap<>();

    static {
        ACTION_TARGET.put(ACT_REGISTER, STATUS_PENDING);
        ACTION_TARGET.put(ACT_ACCEPT,   STATUS_ACCEPTED);
        ACTION_TARGET.put(ACT_REVIEW,   STATUS_REVIEWING);
        ACTION_TARGET.put(ACT_ASSIGN,   STATUS_REVIEWING);
        ACTION_TARGET.put(ACT_DISPOSE,  STATUS_DISPOSING);
        ACTION_TARGET.put(ACT_CLOSE,    STATUS_CLOSED);
        ACTION_TARGET.put(ACT_REJECT,   STATUS_REJECTED);
        ACTION_TARGET.put(ACT_RETURN,   STATUS_PENDING);

        ACTION_LABEL.put(ACT_REGISTER, "登记");
        ACTION_LABEL.put(ACT_ACCEPT,   "受理");
        ACTION_LABEL.put(ACT_REVIEW,   "研判");
        ACTION_LABEL.put(ACT_ASSIGN,   "分办");
        ACTION_LABEL.put(ACT_DISPOSE,  "处置");
        ACTION_LABEL.put(ACT_CLOSE,    "办结");
        ACTION_LABEL.put(ACT_REJECT,   "不予受理");
        ACTION_LABEL.put(ACT_RETURN,   "退回");
    }

    @Autowired
    private JcClueMapper clueMapper;

    @Autowired
    private JcClueTraceMapper traceMapper;

    @Autowired
    private ISysConfigService configService;

    // ═══════════════════════ 基础 CRUD ═══════════════════════

    @Override
    public List<JcClue> selectJcClueList(JcClue jcClue) {
        return clueMapper.selectJcClueList(jcClue);
    }

    @Override
    public JcClue selectJcClueByClueId(Long clueId) {
        return clueMapper.selectJcClueByClueId(clueId);
    }

    @Override
    @Transactional
    public int insertJcClue(JcClue jcClue) {
        if (StringUtils.isEmpty(jcClue.getClueNo())) {
            jcClue.setClueNo(generateClueNo());
        }
        if (StringUtils.isEmpty(jcClue.getStatus())) {
            jcClue.setStatus(STATUS_PENDING);
        }
        if (StringUtils.isEmpty(jcClue.getIsDuplicate())) {
            jcClue.setIsDuplicate("0");
        }
        Date base = jcClue.getCreateTime() != null ? jcClue.getCreateTime() : new Date();
        if (jcClue.getDeadline() == null) {
            jcClue.setDeadline(plusDays(base, resolveDeadlineDays(jcClue.getSourceType())));
        }
        jcClue.setWarnLevel(calcWarnLevel(jcClue.getDeadline(), jcClue.getStatus(), base));

        int rows = clueMapper.insertJcClue(jcClue);

        // 登记轨迹
        writeTrace(jcClue.getClueId(), ACT_REGISTER,
                "线索登记，来源：" + nvl(jcClue.getSourceType()),
                "", jcClue.getStatus(), currentNickName());
        return rows;
    }

    @Override
    public int updateJcClue(JcClue jcClue) {
        Date base = jcClue.getCreateTime() != null ? jcClue.getCreateTime() : new Date();
        jcClue.setWarnLevel(calcWarnLevel(jcClue.getDeadline(), jcClue.getStatus(), base));
        return clueMapper.updateJcClue(jcClue);
    }

    @Override
    @Transactional
    public int deleteJcClueByClueId(Long clueId) {
        traceMapper.deleteTraceByClueId(clueId);
        return clueMapper.deleteJcClueByClueId(clueId);
    }

    // ═══════════════════════ 流转 ═══════════════════════

    @Override
    @Transactional
    public JcClue handleAction(Long clueId, ClueActionRequest req) {
        if (req == null || StringUtils.isEmpty(req.getAction())) {
            throw new ServiceException("流转动作不能为空");
        }
        JcClue clue = clueMapper.selectJcClueByClueId(clueId);
        if (clue == null) {
            throw new ServiceException("线索不存在或已删除");
        }
        String action = req.getAction();
        String target = ACTION_TARGET.get(action);
        if (target == null) {
            throw new ServiceException("未知的流转动作：" + action);
        }
        String before = clue.getStatus();
        if (STATUS_CLOSED.equals(before) || STATUS_REJECTED.equals(before)) {
            throw new ServiceException("线索已办结或不予受理，不能再流转");
        }
        clue.setStatus(target);

        // 分办：指定承办部门 / 处理人
        if (ACT_ASSIGN.equals(action)) {
            if (req.getDeptId() != null) {
                clue.setDeptId(req.getDeptId());
            }
            if (req.getHandlerId() != null) {
                clue.setHandlerId(req.getHandlerId());
            }
        }
        // 研判：允许修正问题类型
        if (ACT_REVIEW.equals(action) && StringUtils.isNotEmpty(req.getViolationType())) {
            clue.setViolationType(req.getViolationType());
        }
        // 处置 / 办结：记录处置方式
        if ((ACT_DISPOSE.equals(action) || ACT_CLOSE.equals(action))
                && StringUtils.isNotEmpty(req.getDisposition())) {
            clue.setDisposition(req.getDisposition());
        }
        // 终态清空预警
        if (STATUS_CLOSED.equals(target) || STATUS_REJECTED.equals(target)) {
            clue.setWarnLevel("");
        }
        clue.setUpdateBy(SecurityUtils.getUsername());
        clueMapper.updateJcClue(clue);

        // 写轨迹
        writeTrace(clueId, action, buildActionDesc(action, req, clue),
                before, target, currentNickName());
        return clueMapper.selectJcClueByClueId(clueId);
    }

    // ═══════════════════════ 编号 / 时限 / 预警 ═══════════════════════

    @Override
    public String generateClueNo() {
        String prefix = NO_PREFIX + java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String max = clueMapper.selectMaxNoByPrefix(prefix);
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

    @Override
    public int refreshWarnLevel() {
        List<JcClue> list = clueMapper.selectActiveClues();
        int changed = 0;
        for (JcClue c : list) {
            Date base = c.getCreateTime() != null ? c.getCreateTime() : new Date();
            String level = calcWarnLevel(c.getDeadline(), c.getStatus(), base);
            if (!level.equals(nvl(c.getWarnLevel()))) {
                clueMapper.updateWarnLevel(c.getClueId(), level);
                changed++;
            }
        }
        return changed;
    }

    /**
     * 计算预警级别。
     *
     * @param deadline 办理截止日期
     * @param status   当前状态
     * @param base     起算日（一般取线索创建时间）
     * @return "" / 黄灯 / 红灯
     */
    private String calcWarnLevel(Date deadline, String status, Date base) {
        if (deadline == null || STATUS_CLOSED.equals(status) || STATUS_REJECTED.equals(status)) {
            return "";
        }
        long total = daysBetween(base, deadline);
        if (total <= 0) {
            total = 1;
        }
        long used = daysBetween(base, new Date());
        if (used >= total) {
            return WARN_RED;
        }
        double ratio = (double) used / (double) total;
        if (ratio >= readWarnRatio()) {
            return WARN_YELLOW;
        }
        return "";
    }

    /** 读取某来源类型的办理时限（日）：优先 jiancha.clue.deadline.<sourceType>，回退 default */
    private int resolveDeadlineDays(String sourceType) {
        int days = 0;
        if (StringUtils.isNotEmpty(sourceType)) {
            days = readInt(CFG_DEADLINE_PREFIX + sourceType, 0);
        }
        if (days <= 0) {
            days = readInt(CFG_DEADLINE_DEFAULT, DEFAULT_DEADLINE_DAYS);
        }
        return days <= 0 ? DEFAULT_DEADLINE_DAYS : days;
    }

    private int readInt(String key, int def) {
        try {
            String v = configService.selectConfigByKey(key);
            if (StringUtils.isNotEmpty(v)) {
                return (int) Double.parseDouble(v.trim());
            }
        } catch (Exception ignored) {
            // 配置缺失或缓存不可用时走兜底值
        }
        return def;
    }

    private double readWarnRatio() {
        try {
            String v = configService.selectConfigByKey(CFG_WARN_RATIO);
            if (StringUtils.isNotEmpty(v)) {
                double d = Double.parseDouble(v.trim());
                if (d > 0 && d <= 1) {
                    return d;
                }
            }
        } catch (Exception ignored) {
            // 同上
        }
        return DEFAULT_WARN_RATIO;
    }

    // ═══════════════════════ 重复检测 ═══════════════════════

    @Override
    public List<JcClue> detectDuplicate(Long personId, Long excludeClueId, int days) {
        if (personId == null) {
            return Collections.emptyList();
        }
        return clueMapper.selectDuplicateCandidates(personId, excludeClueId, days <= 0 ? 90 : days);
    }

    // ═══════════════════════ 统计 ═══════════════════════

    @Override
    public List<ClueStatItem> statBySource() {
        return clueMapper.statBySource();
    }

    @Override
    public List<ClueStatItem> statByViolationType() {
        return clueMapper.statByViolationType();
    }

    @Override
    public List<ClueStatItem> statByStatus() {
        return clueMapper.statByStatus();
    }

    @Override
    public List<ClueStatItem> statByWarnLevel() {
        return clueMapper.statByWarnLevel();
    }

    @Override
    public List<ClueStatItem> statByDept() {
        return clueMapper.statByDept();
    }

    @Override
    public List<ClueStatItem> statMonthly(int months) {
        return clueMapper.statMonthly(months <= 0 ? 6 : months);
    }

    @Override
    public Map<String, Object> statOverview() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("total", clueMapper.countAll());
        m.put("active", clueMapper.countActive());
        m.put("overdue", clueMapper.countOverdue());
        m.put("month", clueMapper.countMonth());
        m.put("bySource", clueMapper.statBySource());
        m.put("byViolationType", clueMapper.statByViolationType());
        m.put("byStatus", clueMapper.statByStatus());
        m.put("byWarnLevel", clueMapper.statByWarnLevel());
        m.put("byDept", clueMapper.statByDept());
        m.put("monthly", clueMapper.statMonthly(6));
        return m;
    }

    // ═══════════════════════ 内部工具 ═══════════════════════

    private void writeTrace(Long clueId, String action, String desc,
                            String before, String after, String operatorName) {
        JcClueTrace t = new JcClueTrace();
        t.setClueId(clueId);
        t.setAction(action);
        t.setActionDesc(desc);
        t.setBeforeStatus(before);
        t.setAfterStatus(after);
        t.setOperatorName(operatorName);
        try {
            t.setOperatorId(SecurityUtils.getUserId());
        } catch (Exception ignored) {
            // 定时任务等无登录上下文场景
        }
        traceMapper.insertTrace(t);
    }

    private String buildActionDesc(String action, ClueActionRequest req, JcClue clue) {
        StringBuilder sb = new StringBuilder(ACTION_LABEL.getOrDefault(action, action));
        if (ACT_ASSIGN.equals(action) && clue.getDeptId() != null) {
            sb.append("（承办部门ID：").append(clue.getDeptId()).append("）");
        }
        if (StringUtils.isNotEmpty(req.getDisposition())) {
            sb.append("（处置方式：").append(req.getDisposition()).append("）");
        }
        if (StringUtils.isNotEmpty(req.getOpinion())) {
            sb.append(" ").append(req.getOpinion());
        }
        return sb.toString();
    }

    private String currentNickName() {
        try {
            return SecurityUtils.getLoginUser().getUser().getNickName();
        } catch (Exception e) {
            try {
                return SecurityUtils.getUsername();
            } catch (Exception ignored) {
                return "system";
            }
        }
    }

    private long daysBetween(Date from, Date to) {
        if (from == null || to == null) {
            return 0L;
        }
        return (to.getTime() - from.getTime()) / 86400000L;
    }

    private Date plusDays(Date base, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(base);
        c.add(Calendar.DAY_OF_MONTH, days);
        return c.getTime();
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}
