package com.jiancha.biz.rectify.service.impl;

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

import com.jiancha.biz.petition.domain.JcPetition;
import com.jiancha.biz.petition.service.IJcPetitionService;
import com.jiancha.biz.rectify.domain.JcRectifyIssue;
import com.jiancha.biz.rectify.domain.JcRectifyReview;
import com.jiancha.biz.rectify.domain.JcRectifyTask;
import com.jiancha.biz.rectify.domain.RectifyActionRequest;
import com.jiancha.biz.rectify.domain.RectifyStat;
import com.jiancha.biz.rectify.domain.RectifyStatItem;
import com.jiancha.biz.rectify.mapper.JcRectifyIssueMapper;
import com.jiancha.biz.rectify.mapper.JcRectifyReviewMapper;
import com.jiancha.biz.rectify.mapper.JcRectifyTaskMapper;
import com.jiancha.biz.rectify.service.IJcRectifyService;

/**
 * 整改闭环台账 Service 实现（E3）
 *
 * <p>设计要点（设计文档 §E3，企业版口径）：</p>
 * <ul>
 *   <li>问题登记幂等：同一来源单号（source_ref）只立一项，防止举报/预警重复立案；</li>
 *   <li>措施未完成不准提交复核（至少一条已完成），销号必须走复核留痕；</li>
 *   <li>销号后仍可回头看：不通过/复发即重开，闭环不是「一销了之」；</li>
 *   <li>整改时限读 {@code sys_config}，红黄灯与线索/举报同口径，Quartz 日调度刷新。</li>
 * </ul>
 *
 * @author 小标
 */
@Service
public class JcRectifyServiceImpl implements IJcRectifyService {

    // ═══════════════════════ 状态机 ═══════════════════════

    /** 待整改 */
    public static final String STATUS_PENDING    = "0";
    /** 整改中 */
    public static final String STATUS_RECTIFYING = "1";
    /** 待复核 */
    public static final String STATUS_REVIEWING  = "2";
    /** 已销号（终态） */
    public static final String STATUS_CLOSED     = "3";
    /** 已作废（终态） */
    public static final String STATUS_VOID       = "4";

    /** 动作：开始整改 */
    public static final String ACT_START   = "start";
    /** 动作：提交复核 */
    public static final String ACT_SUBMIT  = "submit";
    /** 动作：销号 */
    public static final String ACT_PASS    = "pass";
    /** 动作：复核不通过退回 */
    public static final String ACT_REOPEN  = "reopen";
    /** 动作：回头看 */
    public static final String ACT_REVISIT = "revisit";
    /** 动作：作废 */
    public static final String ACT_VOID    = "void";

    /** 动作 -> 目标状态（严格单向） */
    private static final Map<String, String> ACTION_TARGET = new LinkedHashMap<>();
    static {
        ACTION_TARGET.put(ACT_START,  STATUS_RECTIFYING);
        ACTION_TARGET.put(ACT_SUBMIT, STATUS_REVIEWING);
        ACTION_TARGET.put(ACT_PASS,   STATUS_CLOSED);
        ACTION_TARGET.put(ACT_REOPEN, STATUS_RECTIFYING);
        ACTION_TARGET.put(ACT_VOID,   STATUS_VOID);
    }

    /** 复核类型 */
    private static final String REVIEW_CLOSE   = "1"; // 销号复核
    private static final String REVIEW_REVISIT = "2"; // 回头看

    /** 预警级别 */
    private static final String WARN_YELLOW = "黄灯";
    private static final String WARN_RED    = "红灯";

    /** sys_config 键：整改时限（日） */
    private static final String CFG_DEADLINE_DAYS = "jiancha.rectify.deadline.days";
    /** sys_config 键：黄灯阈值比例 */
    private static final String CFG_WARN_RATIO    = "jiancha.rectify.warn.ratio";

    /** 默认整改时限（日） */
    private static final int DEFAULT_DEADLINE_DAYS = 30;
    /** 默认黄灯比例 */
    private static final double DEFAULT_WARN_RATIO = 0.5;

    /** 编号前缀 */
    private static final String NO_PREFIX = "RX";

    @Autowired
    private JcRectifyIssueMapper issueMapper;

    @Autowired
    private JcRectifyTaskMapper taskMapper;

    @Autowired
    private JcRectifyReviewMapper reviewMapper;

    @Autowired
    private IJcPetitionService petitionService;

    /** 消息通知（段17）：新问题登记即时提醒纪检岗 */
    @Autowired
    private com.jiancha.biz.notify.service.INotifyService notifyService;

    @Autowired
    private ISysConfigService configService;

    // ═══════════════════════ 问题登记 ═══════════════════════

    @Override
    @Transactional
    public JcRectifyIssue register(JcRectifyIssue issue) {
        if (issue == null || StringUtils.isEmpty(issue.getTitle())) {
            throw new ServiceException("问题标题不能为空");
        }
        // 幂等：同一来源单号只立一项
        if (StringUtils.isNotEmpty(issue.getSourceRef())) {
            JcRectifyIssue exist = issueMapper.selectBySourceRef(issue.getSourceRef());
            if (exist != null) {
                return exist;
            }
        }
        if (StringUtils.isEmpty(issue.getIssueNo())) {
            issue.setIssueNo(generateIssueNo());
        }
        if (StringUtils.isEmpty(issue.getStatus())) {
            issue.setStatus(STATUS_PENDING);
        }
        if (StringUtils.isEmpty(issue.getSeverity())) {
            issue.setSeverity("一般");
        }
        if (issue.getFoundDate() == null) {
            issue.setFoundDate(new Date());
        }
        if (issue.getDeadline() == null) {
            issue.setDeadline(plusDays(issue.getFoundDate(), readDeadlineDays()));
        }
        issue.setWarnLevel(calcWarnLevel(issue.getDeadline(), issue.getStatus(), issue.getFoundDate()));
        issue.setCreateBy(SecurityUtils.getUsername());
        int rows = issueMapper.insertJcRectifyIssue(issue);
        if (rows <= 0) {
            throw new ServiceException("问题登记失败");
        }
        // 即时通知纪检岗（段17 消息通知中心；举报转整改同样经过本方法，两个入口都覆盖）
        notifyService.sendToRoleKey("admin", "整改", "rectify", issue.getIssueId(),
                "新整改问题待分解措施：" + issue.getTitle(),
                "编号 " + issue.getIssueNo() + "，严重程度 " + issue.getSeverity()
                        + "，整改截止 " + com.ruoyi.common.utils.DateUtils.parseDateToStr("yyyy-MM-dd", issue.getDeadline()) + "。");
        return issueMapper.selectJcRectifyIssueByIssueId(issue.getIssueId());
    }

    @Override
    @Transactional
    public JcRectifyIssue registerFromPetition(Long petitionId, Long targetDeptId,
                                               String problemType, String severity) {
        if (petitionId == null) {
            throw new ServiceException("举报ID不能为空");
        }
        if (targetDeptId == null) {
            throw new ServiceException("必须指定整改责任部门");
        }
        // 幂等：同一举报已登记过整改则直接返回
        JcRectifyIssue exist = issueMapper.selectBySourceRef("petition:" + petitionId);
        if (exist != null) {
            return exist;
        }
        JcPetition petition = petitionService.selectJcPetitionByPetitionId(petitionId);
        if (petition == null) {
            throw new ServiceException("举报不存在或已删除");
        }
        JcRectifyIssue issue = new JcRectifyIssue();
        issue.setSourceType("petition");
        issue.setSourceRef("petition:" + petitionId);
        issue.setTitle("【举报转整改】" + petition.getTitle());
        issue.setDescription(petition.getContent());
        issue.setProblemType(StringUtils.isEmpty(problemType) ? "廉洁风险" : problemType);
        issue.setSeverity(StringUtils.isEmpty(severity) ? "较大" : severity);
        issue.setFoundDept(petition.getTargetDept());
        issue.setTargetDeptId(targetDeptId);
        return register(issue);
    }

    @Override
    public List<JcRectifyIssue> selectJcRectifyIssueList(JcRectifyIssue jcRectifyIssue) {
        return issueMapper.selectJcRectifyIssueList(jcRectifyIssue);
    }

    @Override
    public JcRectifyIssue selectJcRectifyIssueByIssueId(Long issueId) {
        return issueMapper.selectJcRectifyIssueByIssueId(issueId);
    }

    @Override
    public int updateJcRectifyIssue(JcRectifyIssue issue) {
        if (issue == null || issue.getIssueId() == null) {
            throw new ServiceException("问题ID不能为空");
        }
        issue.setUpdateBy(SecurityUtils.getUsername());
        return issueMapper.updateJcRectifyIssue(issue);
    }

    @Override
    @Transactional
    public int deleteJcRectifyIssueByIssueId(Long issueId) {
        taskMapper.deleteTaskByIssueId(issueId);
        reviewMapper.deleteReviewByIssueId(issueId);
        return issueMapper.deleteJcRectifyIssueByIssueId(issueId);
    }

    // ═══════════════════════ 整改流转 ═══════════════════════

    @Override
    @Transactional
    public JcRectifyIssue handleAction(Long issueId, RectifyActionRequest req) {
        if (req == null || StringUtils.isEmpty(req.getAction())) {
            throw new ServiceException("流转动作不能为空");
        }
        JcRectifyIssue issue = issueMapper.selectJcRectifyIssueByIssueId(issueId);
        if (issue == null) {
            throw new ServiceException("问题不存在或已删除");
        }
        String action = req.getAction();
        String before = issue.getStatus();
        if (STATUS_CLOSED.equals(before) || STATUS_VOID.equals(before)) {
            // 回头看不依赖状态机前驱（从已销号重开），单独放行
            if (!ACT_REVISIT.equals(action)) {
                throw new ServiceException("问题已销号或已作废，不能再流转");
            }
        } else {
            String target = ACTION_TARGET.get(action);
            if (target == null) {
                throw new ServiceException("未知的流转动作：" + action);
            }
            String prev = prevStatus(target);
            if (prev != null && !before.equals(prev)) {
                throw new ServiceException("流程不可跳步：当前状态不允许执行该动作");
            }
        }

        String operator = SecurityUtils.getUsername();
        switch (action) {
            case ACT_START:
                issue.setStatus(STATUS_RECTIFYING);
                break;
            case ACT_SUBMIT:
                // 至少一条措施已完成才准提交复核
                Map<String, Object> cnt = taskMapper.countByIssueId(issueId);
                long total = toLong(cnt.get("task_count"));
                long done = toLong(cnt.get("task_done_count"));
                if (total <= 0 || done <= 0) {
                    throw new ServiceException("尚无已完成的整改措施，不能提交复核");
                }
                issue.setStatus(STATUS_REVIEWING);
                break;
            case ACT_PASS:
                if (StringUtils.isEmpty(req.getOpinion())) {
                    throw new ServiceException("销号复核意见不能为空");
                }
                issue.setStatus(STATUS_CLOSED);
                issue.setCloseTime(new Date());
                issue.setWarnLevel("");
                issue.setRectifySummary(req.getOpinion());
                writeReview(issueId, REVIEW_CLOSE, "1", req.getOpinion(), operator);
                break;
            case ACT_REOPEN:
                if (StringUtils.isEmpty(req.getOpinion())) {
                    throw new ServiceException("退回整改意见不能为空");
                }
                issue.setStatus(STATUS_RECTIFYING);
                writeReview(issueId, REVIEW_CLOSE, "0", req.getOpinion(), operator);
                break;
            case ACT_REVISIT: {
                String result = StringUtils.isEmpty(req.getResult()) ? "0" : req.getResult();
                writeReview(issueId, REVIEW_REVISIT, result, req.getOpinion(), operator);
                if ("1".equals(result)) {
                    // 回头看通过：仅留痕，状态不变
                    return issueMapper.selectJcRectifyIssueByIssueId(issueId);
                }
                if (!STATUS_CLOSED.equals(before)) {
                    throw new ServiceException("回头看不通过仅适用于已销号问题");
                }
                // 复发/整改不实：重开（专用语句清空销号时间，并重算预警）
                Date base = issue.getFoundDate() != null ? issue.getFoundDate() : issue.getCreateTime();
                String level = calcWarnLevel(issue.getDeadline(), STATUS_RECTIFYING, base);
                issueMapper.reopenIssue(issueId, level, operator);
                return issueMapper.selectJcRectifyIssueByIssueId(issueId);
            }
            case ACT_VOID:
                if (StringUtils.isEmpty(req.getOpinion())) {
                    throw new ServiceException("作废原因不能为空");
                }
                issue.setStatus(STATUS_VOID);
                issue.setWarnLevel("");
                break;
            default:
                throw new ServiceException("未知的流转动作：" + action);
        }

        issue.setUpdateBy(operator);
        issueMapper.updateJcRectifyIssue(issue);
        return issueMapper.selectJcRectifyIssueByIssueId(issueId);
    }

    // ═══════════════════════ 整改措施 ═══════════════════════

    @Override
    public List<JcRectifyTask> selectTaskByIssueId(Long issueId) {
        return taskMapper.selectTaskByIssueId(issueId);
    }

    @Override
    public int insertTask(JcRectifyTask task) {
        if (task == null || task.getIssueId() == null) {
            throw new ServiceException("措施必须归属问题");
        }
        if (StringUtils.isEmpty(task.getMeasure())) {
            throw new ServiceException("措施内容不能为空");
        }
        if (StringUtils.isEmpty(task.getStatus())) {
            task.setStatus("0");
        }
        // 完成时自动落实际完成日期
        if ("2".equals(task.getStatus()) && task.getDoneDate() == null) {
            task.setDoneDate(new Date());
        }
        task.setCreateBy(SecurityUtils.getUsername());
        return taskMapper.insertTask(task);
    }

    @Override
    public int updateTask(JcRectifyTask task) {
        if (task == null || task.getTaskId() == null) {
            throw new ServiceException("措施ID不能为空");
        }
        if ("2".equals(task.getStatus()) && task.getDoneDate() == null) {
            task.setDoneDate(new Date());
        }
        return taskMapper.updateTask(task);
    }

    @Override
    public int deleteTaskByTaskId(Long taskId) {
        return taskMapper.deleteTaskByTaskId(taskId);
    }

    // ═══════════════════════ 复核记录 ═══════════════════════

    @Override
    public List<JcRectifyReview> selectReviewByIssueId(Long issueId) {
        return reviewMapper.selectReviewByIssueId(issueId);
    }

    // ═══════════════════════ 预警与统计 ═══════════════════════

    @Override
    public int refreshWarnLevel() {
        List<JcRectifyIssue> list = issueMapper.selectActiveIssues();
        int changed = 0;
        for (JcRectifyIssue issue : list) {
            Date base = issue.getFoundDate() != null ? issue.getFoundDate() : issue.getCreateTime();
            String level = calcWarnLevel(issue.getDeadline(), issue.getStatus(), base);
            if (!level.equals(StringUtils.nvl(issue.getWarnLevel(), ""))) {
                issueMapper.updateWarnLevel(issue.getIssueId(), level);
                changed++;
            }
        }
        return changed;
    }

    @Override
    public RectifyStat stat() {
        RectifyStat stat = new RectifyStat();
        Map<String, Object> counts = issueMapper.statCounts();
        stat.setTotal(toLong(counts.get("total")));
        stat.setPending(toLong(counts.get("pending")));
        stat.setRectifying(toLong(counts.get("rectifying")));
        stat.setClosed(toLong(counts.get("closed")));
        stat.setOverdue(toLong(counts.get("overdue")));
        Double avg = issueMapper.statAvgCloseDays();
        stat.setAvgCloseDays(avg == null ? 0.0 : Math.round(avg * 10.0) / 10.0);
        Long revisit = issueMapper.statRevisitCount();
        stat.setRevisitCount(revisit == null ? 0L : revisit);
        stat.setBySource(toItems(issueMapper.statBySource()));
        stat.setByType(toItems(issueMapper.statByType()));
        stat.setBySeverity(toItems(issueMapper.statBySeverity()));
        stat.setByStatus(toItems(issueMapper.statByStatus()));
        stat.setMonthlyTrend(toItems(issueMapper.statMonthlyTrend()));
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -11);
        stat.setByTargetDept(toItems(issueMapper.statByTargetDept(cal.getTime())));
        return stat;
    }

    // ═══════════════════════ 内部方法 ═══════════════════════

    private void writeReview(Long issueId, String reviewType, String result, String opinion, String reviewer) {
        JcRectifyReview review = new JcRectifyReview();
        review.setIssueId(issueId);
        review.setReviewType(reviewType);
        review.setResult(result);
        review.setOpinion(StringUtils.isEmpty(opinion) ? "" : opinion);
        review.setReviewer(reviewer);
        review.setReviewTime(new Date());
        reviewMapper.insertReview(review);
    }

    /** 目标状态的前驱状态（状态机严格单向，用于跳步校验；null 表示多入口） */
    private String prevStatus(String target) {
        switch (target) {
            case STATUS_RECTIFYING: return null; // start(0→1) 与 reopen(2→1) 两个入口
            case STATUS_REVIEWING:  return STATUS_RECTIFYING;
            case STATUS_CLOSED:     return STATUS_REVIEWING;
            case STATUS_VOID:       return null; // 0/1/2 均可作废，单独放行
            default:                return null;
        }
    }

    /** 计算预警级别（口径与线索/举报一致：""/黄灯/红灯，时限按日，窗口=发现日→截止日） */
    private String calcWarnLevel(Date deadline, String status, Date base) {
        if (deadline == null || STATUS_CLOSED.equals(status) || STATUS_VOID.equals(status)) {
            return "";
        }
        Date from = base != null ? base : new Date();
        long remain = daysBetween(new Date(), deadline);
        if (remain <= 0) {
            return WARN_RED;
        }
        long total = Math.max(1, daysBetween(from, deadline));
        double ratio = (double) (total - remain) / (double) total;
        return ratio >= readWarnRatio() ? WARN_YELLOW : "";
    }

    /** 生成问题编号：RX+yyyyMMdd+seq */
    private String generateIssueNo() {
        String prefix = NO_PREFIX + java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String max = issueMapper.selectMaxNoByPrefix(prefix);
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
        return readInt(CFG_DEADLINE_DAYS, DEFAULT_DEADLINE_DAYS);
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
            // 配置缺失走兜底
        }
        return DEFAULT_WARN_RATIO;
    }

    private int readInt(String key, int def) {
        try {
            String v = configService.selectConfigByKey(key);
            if (StringUtils.isNotEmpty(v)) {
                return (int) Double.parseDouble(v.trim());
            }
        } catch (Exception ignored) {
            // 同上
        }
        return def;
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

    /** 统计值 -> Long（兼容 JDBC 返回的 BigInteger/BigDecimal/Long） */
    private Long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }

    /** 统计行 -> RectifyStatItem */
    private List<RectifyStatItem> toItems(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        List<RectifyStatItem> items = new java.util.ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            String name = row.get("name") == null ? "" : String.valueOf(row.get("name"));
            items.add(new RectifyStatItem(name, toLong(row.get("value"))));
        }
        return items;
    }
}
