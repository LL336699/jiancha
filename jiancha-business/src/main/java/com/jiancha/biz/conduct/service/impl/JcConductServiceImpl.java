package com.jiancha.biz.conduct.service.impl;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import com.jiancha.biz.conduct.domain.ConductActionRequest;
import com.jiancha.biz.conduct.domain.ConductStat;
import com.jiancha.biz.conduct.domain.JcConductIssue;
import com.jiancha.biz.conduct.domain.JcConductRecovery;
import com.jiancha.biz.conduct.mapper.JcConductIssueMapper;
import com.jiancha.biz.conduct.mapper.JcConductRecoveryMapper;
import com.jiancha.biz.conduct.mapper.JcConductScanMapper;
import com.jiancha.biz.conduct.service.IJcConductService;

/**
 * 作风建设问题台账 Service 实现（E5）
 *
 * @author 小标
 */
@Service
public class JcConductServiceImpl implements IJcConductService {

    /** 默认核查期限（天） */
    private static final String CFG_CHECK_DAYS = "jiancha.conduct.checkDays";

    /** 合法处置方式（字典 jc_conduct_handle） */
    private static final Set<String> HANDLE_TYPES = Set.of("提醒谈话", "诫勉谈话", "组织处理", "纪律处分", "移送司法");

    /** 合法处分种类（字典 jc_punish_type，企业内部分档） */
    private static final Set<String> PUNISH_TYPES = Set.of("警告", "记过", "记大过", "降级", "撤职", "开除");

    @Autowired
    private JcConductIssueMapper issueMapper;

    @Autowired
    private JcConductRecoveryMapper recoveryMapper;

    @Autowired
    private JcConductScanMapper scanMapper;

    @Autowired
    private ISysConfigService configService;

    // ═══════════════════════ 问题登记 ═══════════════════════

    @Override
    @Transactional
    public JcConductIssue register(JcConductIssue issue) {
        if (issue == null) {
            throw new ServiceException("请求不能为空");
        }
        if (issue.getPersonId() == null) {
            throw new ServiceException("必须关联人员档案（personId）");
        }
        if (StringUtils.isEmpty(issue.getViolationType())) {
            throw new ServiceException("问题类型不能为空");
        }
        // 人员档案快照补全（姓名/部门以档案为准）
        Map<String, Object> person = scanMapper.selectPersonBrief(issue.getPersonId());
        if (person == null) {
            throw new ServiceException("人员档案不存在或已删除");
        }
        issue.setPersonName(StringUtils.nvl(String.valueOf(person.get("personName")), ""));
        if (StringUtils.isEmpty(issue.getPersonName())) {
            throw new ServiceException("对象姓名不能为空");
        }
        issue.setPersonDept(StringUtils.nvl(String.valueOf(person.get("personDept")), issue.getPersonDept()));

        // 编号：CF+yyyyMMdd+seq
        String day = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String prefix = "CF" + day;
        String maxNo = issueMapper.selectMaxNoByPrefix(prefix);
        int seq = 1;
        if (StringUtils.isNotEmpty(maxNo)) {
            try {
                seq = Integer.parseInt(maxNo.substring(prefix.length())) + 1;
            } catch (NumberFormatException ignored) {
                seq = 1;
            }
        }
        issue.setIssueNo(prefix + String.format("%03d", seq));
        issue.setStatus("0");
        // 核查截止日期：默认登记日+30天（参数可覆盖）
        issue.setCheckDeadline(plusDays(new Date(), readCheckDays()));
        issue.setCreateBy(SecurityUtils.getUsername());
        issueMapper.insertJcConductIssue(issue);
        return issueMapper.selectJcConductIssueByIssueId(issue.getIssueId());
    }

    @Override
    public List<JcConductIssue> selectJcConductIssueList(JcConductIssue query) {
        return issueMapper.selectJcConductIssueList(query);
    }

    @Override
    public JcConductIssue selectJcConductIssueByIssueId(Long issueId) {
        return issueMapper.selectJcConductIssueByIssueId(issueId);
    }

    @Override
    public JcConductIssue selectByIssueNo(String issueNo) {
        return issueMapper.selectByIssueNo(issueNo);
    }

    @Override
    public int updateJcConductIssue(JcConductIssue issue) {
        if (issue == null || issue.getIssueId() == null) {
            throw new ServiceException("问题ID不能为空");
        }
        JcConductIssue db = issueMapper.selectJcConductIssueByIssueId(issue.getIssueId());
        if (db == null) {
            throw new ServiceException("问题不存在或已删除");
        }
        if (!"0".equals(db.getStatus())) {
            throw new ServiceException("仅待核查状态允许修改登记信息");
        }
        issue.setUpdateBy(SecurityUtils.getUsername());
        return issueMapper.updateJcConductIssue(issue);
    }

    @Override
    public int deleteJcConductIssueByIssueId(Long issueId) {
        JcConductIssue db = issueMapper.selectJcConductIssueByIssueId(issueId);
        if (db == null) {
            throw new ServiceException("问题不存在或已删除");
        }
        if (!"0".equals(db.getStatus())) {
            throw new ServiceException("仅待核查状态允许删除");
        }
        return issueMapper.deleteJcConductIssueByIssueId(issueId);
    }

    // ═══════════════════════ 核查 / 处置 / 转办 ═══════════════════════

    @Override
    @Transactional
    public JcConductIssue verify(Long issueId, ConductActionRequest req) {
        JcConductIssue db = requireIssue(issueId);
        if (!"0".equals(db.getStatus())) {
            throw new ServiceException("仅待核查状态允许登记核查结果");
        }
        if (req == null || StringUtils.isEmpty(req.getViolationDesc())) {
            throw new ServiceException("核查情况说明不能为空");
        }
        issueMapper.verifyDone(issueId, req.getViolationDesc(), req.getAmount(), req.getSeverity(),
                SecurityUtils.getUsername());
        return issueMapper.selectJcConductIssueByIssueId(issueId);
    }

    @Override
    @Transactional
    public JcConductIssue handle(Long issueId, ConductActionRequest req) {
        JcConductIssue db = requireIssue(issueId);
        if (!"1".equals(db.getStatus())) {
            throw new ServiceException("仅已核查待处理状态允许登记处置");
        }
        if (req == null || StringUtils.isEmpty(req.getHandleType())) {
            throw new ServiceException("处置方式不能为空");
        }
        if (!HANDLE_TYPES.contains(req.getHandleType())) {
            throw new ServiceException("处置方式不合法：" + req.getHandleType());
        }
        Date handleDate = parseDate(req.getHandleDate());
        issueMapper.handleDone(issueId, req.getHandleType(), handleDate, req.getHandleDesc(),
                req.getAmountRecovered(), SecurityUtils.getUsername());
        return issueMapper.selectJcConductIssueByIssueId(issueId);
    }

    @Override
    @Transactional
    public JcConductIssue transferRectify(Long issueId, ConductActionRequest req) {
        JcConductIssue db = requireIssue(issueId);
        if (db.getRectifyId() != null) {
            throw new ServiceException("该问题已转整改（整改单号关联已存在），不允许重复转办");
        }
        if ("3".equals(db.getStatus()) || "4".equals(db.getStatus())) {
            throw new ServiceException("终态问题不允许转整改");
        }
        String sourceRef = "conduct:" + db.getIssueNo();
        Long exist = scanMapper.selectRectifyIdBySourceRef(sourceRef);
        if (exist != null) {
            // 幂等保护：整改台账已存在（如历史导入），直接回填关联
            issueMapper.linkRectify(issueId, exist, SecurityUtils.getUsername());
            return issueMapper.selectJcConductIssueByIssueId(issueId);
        }
        String title = (req != null && StringUtils.isNotEmpty(req.getRectifyTitle()))
                ? req.getRectifyTitle() : "作风建设问题整改：" + db.getIssueNo();
        String rectifyNo = nextRectifyNo();
        issueMapper.insertRectify(rectifyNo, sourceRef, title, db.getViolationDesc(), db.getSeverity(),
                db.getViolationDate(), db.getPersonDept(), null, db.getPersonId(),
                parseDate(req != null ? req.getRectifyDeadline() : null), SecurityUtils.getUsername());
        Long rectifyId = scanMapper.selectRectifyIdBySourceRef(sourceRef);
        issueMapper.linkRectify(issueId, rectifyId, SecurityUtils.getUsername());
        return issueMapper.selectJcConductIssueByIssueId(issueId);
    }

    @Override
    @Transactional
    public JcConductIssue transferPunish(Long issueId, ConductActionRequest req) {
        JcConductIssue db = requireIssue(issueId);
        if (db.getPunishId() != null) {
            throw new ServiceException("该问题已转处分，不允许重复转办");
        }
        if ("3".equals(db.getStatus()) || "4".equals(db.getStatus())) {
            throw new ServiceException("终态问题不允许转处分");
        }
        if (req == null || StringUtils.isEmpty(req.getPunishType())) {
            throw new ServiceException("处分种类不能为空");
        }
        if (!PUNISH_TYPES.contains(req.getPunishType())) {
            throw new ServiceException("处分种类不合法：" + req.getPunishType());
        }
        Date punishDate = parseDate(req.getHandleDate());
        if (punishDate == null) {
            punishDate = new Date();
        }
        String reason = StringUtils.isNotEmpty(req.getPunishReason())
                ? req.getPunishReason() : "作风建设问题：" + db.getIssueNo();
        issueMapper.insertPunish(db.getPersonId(), req.getPunishType(), reason, punishDate,
                db.getIssueNo(), SecurityUtils.getUsername());
        Long punishId = issueMapper.selectPunishIdByCaseNo(db.getIssueNo());
        issueMapper.linkPunish(issueId, punishId, SecurityUtils.getUsername());
        return issueMapper.selectJcConductIssueByIssueId(issueId);
    }

    @Override
    @Transactional
    public JcConductIssue close(Long issueId, ConductActionRequest req) {
        JcConductIssue db = requireIssue(issueId);
        int rows = issueMapper.closeIssue(issueId, SecurityUtils.getUsername());
        if (rows == 0) {
            throw new ServiceException("当前状态不允许关闭");
        }
        return issueMapper.selectJcConductIssueByIssueId(issueId);
    }

    @Override
    @Transactional
    public JcConductIssue cancel(Long issueId, ConductActionRequest req) {
        JcConductIssue db = requireIssue(issueId);
        int rows = issueMapper.cancelIssue(issueId, SecurityUtils.getUsername());
        if (rows == 0) {
            throw new ServiceException("当前状态不允许作废");
        }
        return issueMapper.selectJcConductIssueByIssueId(issueId);
    }

    // ═══════════════════════ 辅助与统计 ═══════════════════════

    @Override
    @Transactional
    public JcConductRecovery addRecovery(Long issueId, JcConductRecovery recovery) {
        JcConductIssue db = requireIssue(issueId);
        if (recovery == null || StringUtils.isEmpty(recovery.getItemName())) {
            throw new ServiceException("退缴物品名称不能为空");
        }
        if (recovery.getAmount() == null || recovery.getAmount().signum() <= 0) {
            throw new ServiceException("退缴金额必须大于 0");
        }
        recovery.setIssueId(issueId);
        recovery.setCreateBy(SecurityUtils.getUsername());
        recoveryMapper.insertRecovery(recovery);
        // 重算主表退缴合计
        BigDecimal sum = recoveryMapper.sumAmountByIssueId(issueId);
        issueMapper.updateRecovered(issueId, sum, SecurityUtils.getUsername());
        return recoveryMapper.selectByRecoveryId(recovery.getRecoveryId());
    }

    @Override
    public List<JcConductRecovery> listRecoveries(Long issueId) {
        return recoveryMapper.selectByIssueId(issueId);
    }

    @Override
    @Transactional
    public int deleteRecovery(Long recoveryId) {
        JcConductRecovery db = recoveryMapper.selectByRecoveryId(recoveryId);
        if (db == null) {
            throw new ServiceException("退缴记录不存在或已删除");
        }
        int rows = recoveryMapper.deleteByRecoveryId(recoveryId, SecurityUtils.getUsername());
        // 重算主表退缴合计
        BigDecimal sum = recoveryMapper.sumAmountByIssueId(db.getIssueId());
        issueMapper.updateRecovered(db.getIssueId(), sum, SecurityUtils.getUsername());
        return rows;
    }

    @Override
    public List<Map<String, Object>> searchPersons(String keyword) {
        return scanMapper.searchPersons(keyword);
    }

    @Override
    public int refreshWarnLevel() {
        return issueMapper.refreshWarnLevel();
    }

    @Override
    public ConductStat stat() {
        ConductStat stat = new ConductStat();
        List<Map<String, Object>> cards = new ArrayList<>();
        cards.add(card("问题总量", issueMapper.countTotal()));
        cards.add(card("待核查", issueMapper.countPending()));
        cards.add(card("已处理", issueMapper.countHandled()));
        cards.add(card("已关闭", issueMapper.countClosed()));
        cards.add(card("退缴总额(元)", issueMapper.sumRecovered()));
        stat.setCards(cards);
        stat.setByType(issueMapper.statByType());
        stat.setBySeverity(issueMapper.statBySeverity());
        stat.setBySource(issueMapper.statBySource());
        stat.setByDept(issueMapper.statByDept());
        stat.setByHandle(issueMapper.statByHandle());
        stat.setMonthlyIssues(issueMapper.statMonthlyIssues());
        return stat;
    }

    // ═══════════════════════ 私有方法 ═══════════════════════

    private JcConductIssue requireIssue(Long issueId) {
        if (issueId == null) {
            throw new ServiceException("问题ID不能为空");
        }
        JcConductIssue db = issueMapper.selectJcConductIssueByIssueId(issueId);
        if (db == null) {
            throw new ServiceException("问题不存在或已删除");
        }
        return db;
    }

    /** 生成整改单号 RX+yyyyMMdd+seq（与 E3 模块同规则，转办写入 jc_rectify_issue） */
    private String nextRectifyNo() {
        String day = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String prefix = "RX" + day;
        String maxNo = scanMapper.selectMaxRectifyNoByPrefix(prefix);
        int seq = 1;
        if (StringUtils.isNotEmpty(maxNo)) {
            try {
                seq = Integer.parseInt(maxNo.substring(prefix.length())) + 1;
            } catch (NumberFormatException ignored) {
                seq = 1;
            }
        }
        return prefix + String.format("%03d", seq);
    }

    private int readCheckDays() {
        try {
            String v = configService.selectConfigByKey(CFG_CHECK_DAYS);
            if (StringUtils.isNotEmpty(v)) {
                return Integer.parseInt(v.trim());
            }
        } catch (Exception ignored) {
            // 参数缺失或非法时走默认
        }
        return 30;
    }

    private Date plusDays(Date base, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(base);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return cal.getTime();
    }

    private Date parseDate(String s) {
        if (StringUtils.isEmpty(s)) {
            return null;
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(s.trim());
        } catch (ParseException e) {
            throw new ServiceException("日期格式应为 yyyy-MM-dd：" + s);
        }
    }

    private Map<String, Object> card(String name, Object value) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name);
        m.put("value", value);
        return m;
    }
}
