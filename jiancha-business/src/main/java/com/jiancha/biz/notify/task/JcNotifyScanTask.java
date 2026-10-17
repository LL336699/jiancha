package com.jiancha.biz.notify.task;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jiancha.biz.archive.domain.JcDeclareConfig;
import com.jiancha.biz.archive.service.IJcDeclareConfigService;
import com.jiancha.biz.knowledge.domain.JcKbDocument;
import com.jiancha.biz.knowledge.service.IJcKbDocumentService;
import com.jiancha.biz.notify.service.INotifyService;
import com.jiancha.biz.petition.domain.JcPetition;
import com.jiancha.biz.petition.service.IJcPetitionService;
import com.jiancha.biz.rectify.domain.JcRectifyIssue;
import com.jiancha.biz.rectify.service.IJcRectifyService;
import com.ruoyi.common.utils.DateUtils;

/**
 * 消息通知聚合扫描定时任务（段17 消息通知中心）
 *
 * <p>按日兜底扫描各模块临期/异常事项并生成站内提醒，与业务事件即时通知互补：
 * 即时通知覆盖「发生了什么」，本任务覆盖「什么快要过期/什么还没处理」。
 * 消息发送幂等（同接收人+同标题当日只落一条），重复调度不刷屏；
 * 事项状态恶化后标题变化（如「3日内到期」→「已超期」），会作为新消息再次提醒。</p>
 *
 * <p>调度方式（系统监控 → 定时任务）：
 * <ul>
 *   <li>{@code jcNotifyScanTask.dailyScan()} —— 全量聚合扫描，建议每日 8:00</li>
 *   <li>{@code jcNotifyScanTask.cleanExpired(30)} —— 清理 30 天前已读消息，建议每周</li>
 * </ul></p>
 *
 * @author 小标
 */
@Component("jcNotifyScanTask")
public class JcNotifyScanTask {

    private static final Logger log = LoggerFactory.getLogger(JcNotifyScanTask.class);

    /** 提醒接收角色：纪检岗位（自用/企业部署默认 admin 承担纪检职能） */
    private static final String ROLE_ADMIN = "admin";

    /** 整改临期提醒提前量（天） */
    private static final int RECTIFY_AHEAD_DAYS = 3;

    /** 申报截止提醒节点（天） */
    private static final int[] DECLARE_AHEAD_DAYS = {14, 7, 1};

    @Autowired
    private INotifyService notifyService;

    @Autowired
    private IJcPetitionService petitionService;

    @Autowired
    private IJcRectifyService rectifyService;

    @Autowired
    private IJcDeclareConfigService declareConfigService;

    @Autowired
    private IJcKbDocumentService kbDocumentService;

    /**
     * 全量聚合扫描（举报超期 / 整改临期 / 申报截止 / 知识库解析失败）。
     */
    public void dailyScan() {
        int count = 0;
        count += scanPetitionOverdue();
        count += scanRectifyDeadline();
        count += scanDeclareDeadline();
        count += scanKbParseFailed();
        log.info("[消息聚合扫描] 完成，本次生成提醒 {} 条", count);
    }

    /**
     * 举报超期未办结提醒：selectOverdue 已含超期口径（办理截止 < 当前且未办结）。
     */
    public int scanPetitionOverdue() {
        List<JcPetition> overdue = petitionService.selectOverdue();
        if (overdue == null || overdue.isEmpty()) {
            return 0;
        }
        String title = "有 " + overdue.size() + " 件举报已超期未办结";
        return notifyService.sendToRoleKey(ROLE_ADMIN, "举报", "petition", null, title,
                "以下举报已超过办理时限，请尽快催办或研判：" + joinPetitionTitles(overdue, 5));
    }

    /**
     * 整改问题临期提醒：待整改/整改中且截止日在 RECTIFY_AHEAD_DAYS 天内或已超期。
     */
    public int scanRectifyDeadline() {
        int count = 0;
        count += scanRectifyByStatus("0", "待整改");
        count += scanRectifyByStatus("1", "整改中");
        return count;
    }

    private int scanRectifyByStatus(String status, String statusLabel) {
        JcRectifyIssue query = new JcRectifyIssue();
        query.setStatus(status);
        List<JcRectifyIssue> list = rectifyService.selectJcRectifyIssueList(query);
        if (list == null || list.isEmpty()) {
            return 0;
        }
        Date now = new Date();
        Date ahead = DateUtils.addDays(new Date(), RECTIFY_AHEAD_DAYS);
        int due = 0;
        StringBuilder sb = new StringBuilder();
        for (JcRectifyIssue issue : list) {
            Date deadline = issue.getDeadline();
            if (deadline == null) {
                continue;
            }
            boolean dueSoon = deadline.before(ahead);
            if (!dueSoon) {
                continue;
            }
            due++;
            if (sb.length() < 600) {
                sb.append(issue.getIssueNo()).append(" ").append(issue.getTitle())
                  .append("（截止 ").append(DateUtils.parseDateToStr("MM-dd", deadline)).append("）");
            }
        }
        if (due == 0) {
            return 0;
        }
        String title = "有 " + due + " 项" + statusLabel + "问题临近或已超整改期限";
        return notifyService.sendToRoleKey(ROLE_ADMIN, "整改", "rectify", null, title, sb.toString());
    }

    /**
     * 年度集中申报截止提醒：距截止日 14/7/1 天时提醒。
     */
    public int scanDeclareDeadline() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        JcDeclareConfig config = declareConfigService.selectByYear(year);
        if (config == null || config.getDeadline() == null || !"1".equals(config.getEnabled())) {
            return 0;
        }
        long daysLeft = daysBetween(new Date(), config.getDeadline());
        for (int ahead : DECLARE_AHEAD_DAYS) {
            if (daysLeft == ahead) {
                String title = "年度利益冲突申报将于 " + ahead + " 天后截止";
                return notifyService.sendToRoleKey(ROLE_ADMIN, "申报", "conflict", null, title,
                        year + " 年度集中申报截止日为 " + DateUtils.parseDateToStr("yyyy-MM-dd", config.getDeadline())
                                + "，请关注未申报人员督办清单。");
            }
        }
        if (daysLeft < 0) {
            String title = year + " 年度申报期已截止，请处理逾期未报";
            return notifyService.sendToRoleKey(ROLE_ADMIN, "申报", "conflict", null, title,
                    "截止日 " + DateUtils.parseDateToStr("yyyy-MM-dd", config.getDeadline())
                            + " 已过，逾期未报人员按制度纳入督办。");
        }
        return 0;
    }

    /**
     * 知识库解析失败文档提醒：提示人工重传或重新解析。
     */
    public int scanKbParseFailed() {
        JcKbDocument query = new JcKbDocument();
        query.setStatus("解析失败");
        List<JcKbDocument> failed = kbDocumentService.selectJcKbDocumentList(query);
        if (failed == null || failed.isEmpty()) {
            return 0;
        }
        String title = "知识库有 " + failed.size() + " 篇文档解析失败";
        return notifyService.sendToRoleKey(ROLE_ADMIN, "系统", "kb", null, title,
                "解析失败文档不参与检索问答，请在知识库中重新解析或删除重传。");
    }

    /**
     * 清理 N 天前的已读消息（防表膨胀），建议每周调度。
     */
    public void cleanExpired(Integer days) {
        int d = days == null ? 30 : days;
        int removed = notifyService.cleanExpired(d);
        log.info("[消息清理] 删除 {} 天前已读消息 {} 条", d, removed);
    }

    private String joinPetitionTitles(List<JcPetition> list, int max) {
        StringBuilder sb = new StringBuilder();
        int n = Math.min(list.size(), max);
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                sb.append("；");
            }
            sb.append(list.get(i).getTitle());
        }
        if (list.size() > max) {
            sb.append(" 等");
        }
        return sb.toString();
    }

    private long daysBetween(Date from, Date to) {
        Date a = DateUtils.parseDate(DateUtils.parseDateToStr("yyyy-MM-dd", from));
        Date b = DateUtils.parseDate(DateUtils.parseDateToStr("yyyy-MM-dd", to));
        long ms = b.getTime() - a.getTime();
        return ms / (24L * 3600L * 1000L);
    }
}
