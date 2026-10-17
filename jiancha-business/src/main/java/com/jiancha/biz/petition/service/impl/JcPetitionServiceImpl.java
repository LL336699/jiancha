package com.jiancha.biz.petition.service.impl;

import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysConfigService;

import com.jiancha.biz.clue.domain.JcClue;
import com.jiancha.biz.clue.service.IJcClueService;
import com.jiancha.biz.petition.crypto.JcFieldCrypto;
import com.jiancha.biz.petition.domain.JcPetition;
import com.jiancha.biz.petition.domain.JcPetitionMaterial;
import com.jiancha.biz.petition.domain.JcPetitionTrace;
import com.jiancha.biz.petition.domain.PetitionActionRequest;
import com.jiancha.biz.petition.domain.PetitionStat;
import com.jiancha.biz.petition.domain.PetitionStatItem;
import com.jiancha.biz.petition.domain.PetitionSubmitRequest;
import com.jiancha.biz.petition.mapper.JcPetitionMapper;
import com.jiancha.biz.petition.mapper.JcPetitionMaterialMapper;
import com.jiancha.biz.petition.mapper.JcPetitionMessageMapper;
import com.jiancha.biz.petition.mapper.JcPetitionTraceMapper;
import com.jiancha.biz.petition.security.PetitionRateLimiter;
import com.jiancha.biz.petition.service.IJcPetitionService;

/**
 * 舞弊举报 Service 实现（M3）
 *
 * <p>设计要点（设计文档 §M3，企业版口径）：</p>
 * <ul>
 *   <li>举报箱免登录（{@code @Anonymous}），敏感字段 AES-GCM 加密，回执码双哈希
 *       （SHA-256 检索 + BCrypt 校验），举报人 IP 只做限流键不落库；</li>
 *   <li>状态机严格单向，每次流转写轨迹，不可跳步；</li>
 *   <li>分办即转线索：复用 M4 线索模块落一条「匿名/员工举报」来源线索，source_ref 回指
 *       {@code petition:{id}}，全程可回溯；</li>
 *   <li>办理时限读 {@code sys_config}，红黄灯与线索同口径，Quartz 日调度刷新。</li>
 * </ul>
 *
 * @author 小标
 */
@Service
public class JcPetitionServiceImpl implements IJcPetitionService {

    // ═══════════════════════ 状态机 ═══════════════════════

    /** 待受理 */
    public static final String STATUS_PENDING    = "0";
    /** 已受理 */
    public static final String STATUS_ACCEPTED   = "1";
    /** 研判中 */
    public static final String STATUS_REVIEWING  = "2";
    /** 已分办 */
    public static final String STATUS_DISPATCHED = "3";
    /** 办理中 */
    public static final String STATUS_HANDLING   = "4";
    /** 已反馈 */
    public static final String STATUS_FEEDBACK   = "5";
    /** 已归档（终态） */
    public static final String STATUS_ARCHIVED   = "6";
    /** 不予受理（终态） */
    public static final String STATUS_REJECTED   = "7";

    /** 动作：提交 */
    public static final String ACT_SUBMIT    = "submit";
    /** 动作：受理 */
    public static final String ACT_ACCEPT    = "accept";
    /** 动作：研判 */
    public static final String ACT_REVIEW    = "review";
    /** 动作：分办 */
    public static final String ACT_DISPATCH  = "dispatch";
    /** 动作：办理登记 */
    public static final String ACT_HANDLE    = "handle";
    /** 动作：反馈 */
    public static final String ACT_FEEDBACK  = "feedback";
    /** 动作：归档 */
    public static final String ACT_ARCHIVE   = "archive";
    /** 动作：不予受理 */
    public static final String ACT_REJECT    = "reject";
    /** 动作：催办（段16，事实型留痕，不改变状态） */
    public static final String ACT_URGE      = "urge";
    /** 动作：举报人满意度评价（段16，事实型留痕） */
    public static final String ACT_RATE      = "rate";
    /** 动作：举报人留言（段16，事实型留痕） */
    public static final String ACT_MESSAGE   = "message";
    /** 动作：受理人回复留言（段16，事实型留痕） */
    public static final String ACT_REPLY     = "reply";
    /** 动作：并案（段16，事实型留痕） */
    public static final String ACT_MERGE     = "merge";
    /** 动作：解除并案（段16，事实型留痕） */
    public static final String ACT_UNMERGE   = "unmerge";

    /** 动作 -> 目标状态（严格单向） */
    private static final Map<String, String> ACTION_TARGET = new LinkedHashMap<>();
    static {
        ACTION_TARGET.put(ACT_ACCEPT,   STATUS_ACCEPTED);
        ACTION_TARGET.put(ACT_REVIEW,   STATUS_REVIEWING);
        ACTION_TARGET.put(ACT_DISPATCH, STATUS_DISPATCHED);
        ACTION_TARGET.put(ACT_HANDLE,   STATUS_HANDLING);
        ACTION_TARGET.put(ACT_FEEDBACK, STATUS_FEEDBACK);
        ACTION_TARGET.put(ACT_ARCHIVE,  STATUS_ARCHIVED);
        ACTION_TARGET.put(ACT_REJECT,   STATUS_REJECTED);
    }

    /** 舞弊类型 -> 线索问题类型（字典 jc_violation_type） */
    private static final Map<String, String> CATEGORY_TO_VIOLATION = new LinkedHashMap<>();
    static {
        CATEGORY_TO_VIOLATION.put("1", "4"); // 采购回扣   -> 采购舞弊
        CATEGORY_TO_VIOLATION.put("2", "4"); // 串标围标   -> 采购舞弊
        CATEGORY_TO_VIOLATION.put("3", "5"); // 虚报冒领   -> 财务违规
        CATEGORY_TO_VIOLATION.put("4", "6"); // 吃拿卡要   -> 滥用职权
        CATEGORY_TO_VIOLATION.put("5", "3"); // 利益冲突   -> 利益输送
        CATEGORY_TO_VIOLATION.put("6", "8"); // 其他       -> 违反公司制度
    }

    /** 预警级别 */
    private static final String WARN_YELLOW = "黄灯";
    private static final String WARN_RED    = "红灯";

    /** sys_config 键：办理时限（小时） */
    private static final String CFG_DEADLINE_HOURS = "jiancha.petition.deadline.hours";
    /** sys_config 键：黄灯阈值比例 */
    private static final String CFG_WARN_RATIO     = "jiancha.petition.warn.ratio";
    /** sys_config 键：验证码开关 */
    private static final String CFG_CAPTCHA        = "jiancha.petition.captcha";
    /** sys_config 键：重复提示时间窗（天） */
    private static final String CFG_DUPLICATE_DAYS = "jiancha.petition.duplicate.days";

    /** 默认办理时限（小时） */
    private static final int DEFAULT_DEADLINE_HOURS = 72;
    /** 默认黄灯比例 */
    private static final double DEFAULT_WARN_RATIO = 0.5;
    /** 默认重复提示时间窗（天） */
    private static final int DEFAULT_DUPLICATE_DAYS = 90;

    @Autowired
    private JcPetitionMapper petitionMapper;

    @Autowired
    private JcPetitionTraceMapper traceMapper;

    @Autowired
    private JcPetitionMaterialMapper materialMapper;

    @Autowired
    private JcPetitionMessageMapper messageMapper;

    @Autowired
    private com.jiancha.biz.notify.service.INotifyService notifyService;

    @Autowired
    private IJcClueService clueService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private JcFieldCrypto fieldCrypto;

    @Autowired
    private PetitionRateLimiter rateLimiter;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    /** 回执码检索胡椒值（与 crypto-key 独立，泄漏其一不直接推出另一） */
    @Value("${jiancha.petition.receipt-pepper:jiancha-default-receipt-pepper-change-me}")
    private String receiptPepper;

    // ═══════════════════════ 举报箱（免登录） ═══════════════════════

    @Override
    @Transactional
    public JcPetition submit(PetitionSubmitRequest req, String clientIp) {
        if (req == null) {
            throw new ServiceException("提交内容不能为空");
        }
        // 1. 频率限制（IP 只做限流键，不落库）
        if (!rateLimiter.tryAcquire(clientIp)) {
            throw new ServiceException("提交过于频繁，请稍后再试");
        }
        // 2. 验证码开关（默认关；开启时校验并消费）
        if (isCaptchaEnabled()) {
            if (StringUtils.isEmpty(req.getCaptchaCode()) || StringUtils.isEmpty(req.getCaptchaUuid())) {
                throw new ServiceException("请输入验证码");
            }
            String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + req.getCaptchaUuid();
            String captcha = redisCache.getCacheObject(verifyKey);
            if (captcha == null) {
                throw new ServiceException("验证码已过期，请刷新");
            }
            redisCache.deleteObject(verifyKey);
            if (!req.getCaptchaCode().equalsIgnoreCase(captcha)) {
                throw new ServiceException("验证码错误");
            }
        }
        // 3. 要素校验
        if (StringUtils.isEmpty(req.getTitle()) || StringUtils.isEmpty(req.getContent())
                || StringUtils.isEmpty(req.getCategory())) {
            throw new ServiceException("事由标题、举报内容、舞弊类型为必填项");
        }
        boolean anonymous = !"0".equals(req.getAnonymousFlag());
        if (!anonymous && StringUtils.isEmpty(req.getReporterName())) {
            throw new ServiceException("实名举报须填写举报人姓名");
        }
        if (!anonymous && StringUtils.isEmpty(req.getReporterPhone())) {
            throw new ServiceException("实名举报须填写联系电话");
        }

        // 4. 回执码：16 位随机数字（4-4-4-4，类电话号码格式，便于口头转述）
        String receiptCode = generateReceiptCode();

        // 5. 组装落库
        JcPetition petition = new JcPetition();
        petition.setTitle(req.getTitle().trim());
        petition.setContent(req.getContent());
        petition.setCategory(req.getCategory());
        petition.setChannel(StringUtils.isEmpty(req.getChannel()) ? "web" : req.getChannel());
        petition.setAnonymousFlag(anonymous ? "1" : "0");
        petition.setReporterName(fieldCrypto.encrypt(req.getReporterName()));
        petition.setReporterPhone(fieldCrypto.encrypt(req.getReporterPhone()));
        petition.setReporterIdcard(fieldCrypto.encrypt(req.getReporterIdcard()));
        petition.setTargetName(req.getTargetName());
        petition.setTargetDept(req.getTargetDept());
        petition.setAmount(req.getAmount());
        petition.setStatus(STATUS_PENDING);
        petition.setReceiptCode(receiptCode); // 仅本次响应返回
        petition.setReceiptLookup(receiptLookup(receiptCode));
        petition.setReceiptHash(bCryptPasswordEncoder.encode(receiptCode));
        petition.setCreateBy(anonymous ? "举报人(匿名)" : "举报人(实名)");

        int rows = petitionMapper.insertJcPetition(petition);
        if (rows <= 0) {
            throw new ServiceException("提交失败，请稍后重试");
        }

        // 6. 首条轨迹
        writeTrace(petition.getPetitionId(), ACT_SUBMIT, "举报提交成功，待受理",
                "", STATUS_PENDING, "举报人");

        // 7. 即时通知纪检岗（段17 消息通知中心）
        notifyService.sendToRoleKey("admin", "举报", "petition", petition.getPetitionId(),
                "收到新的舞弊举报：" + petition.getTitle(),
                "渠道：" + petition.getChannel() + (anonymous ? "，匿名提交" : "，实名提交")
                        + "，请及时受理。");
        return petition;
    }

    @Override
    public JcPetition queryByReceipt(String receiptCode) {
        if (StringUtils.isEmpty(receiptCode)) {
            throw new ServiceException("回执码不能为空");
        }
        JcPetition petition = petitionMapper.selectByReceiptLookup(receiptLookup(receiptCode.trim()));
        if (petition == null) {
            throw new ServiceException("回执码不存在，请核对后重试");
        }
        // 索引命中后二次校验（防哈希碰撞/彩虹表）
        if (!bCryptPasswordEncoder.matches(receiptCode.trim(), petition.getReceiptHash())) {
            throw new ServiceException("回执码不存在，请核对后重试");
        }
        // 举报人视角：只暴露状态与反馈，敏感字段与哈希一律清空
        petition.setReceiptCode(null);
        petition.setReceiptLookup(null);
        petition.setReceiptHash(null);
        petition.setReporterName(null);
        petition.setReporterPhone(null);
        petition.setReporterIdcard(null);
        petition.setCreateBy(null);
        petition.setUpdateBy(null);
        if (petition.getStatus().compareTo(STATUS_FEEDBACK) < 0) {
            petition.setFeedback(null);
        }
        return petition;
    }

    @Override
    public int appendMaterialByReceipt(String receiptCode, String fileName, String filePath, Long fileSize) {
        JcPetition petition = queryByReceipt(receiptCode);
        if (STATUS_ARCHIVED.equals(petition.getStatus()) || STATUS_REJECTED.equals(petition.getStatus())) {
            throw new ServiceException("该举报已办结，不再接收补充材料");
        }
        JcPetitionMaterial material = new JcPetitionMaterial();
        material.setPetitionId(petition.getPetitionId());
        material.setFileName(fileName);
        material.setFilePath(filePath);
        material.setFileSize(fileSize);
        material.setUploaderType("0");
        material.setCreateBy("举报人");
        return materialMapper.insertMaterial(material);
    }

    // ═══════════════════════ 管理端 ═══════════════════════

    @Override
    public List<JcPetition> selectJcPetitionList(JcPetition jcPetition) {
        return petitionMapper.selectJcPetitionList(jcPetition);
    }

    @Override
    public JcPetition selectJcPetitionByPetitionId(Long petitionId) {
        return petitionMapper.selectJcPetitionByPetitionId(petitionId);
    }

    @Override
    public List<JcPetitionTrace> selectTraceByPetitionId(Long petitionId) {
        return traceMapper.selectTraceByPetitionId(petitionId);
    }

    @Override
    @Transactional
    public JcPetition handleAction(Long petitionId, PetitionActionRequest req) {
        if (req == null || StringUtils.isEmpty(req.getAction())) {
            throw new ServiceException("流转动作不能为空");
        }
        JcPetition petition = petitionMapper.selectJcPetitionByPetitionId(petitionId);
        if (petition == null) {
            throw new ServiceException("举报不存在或已删除");
        }
        String action = req.getAction();
        String target = ACTION_TARGET.get(action);
        if (target == null) {
            throw new ServiceException("未知的流转动作：" + action);
        }
        String before = petition.getStatus();
        if (STATUS_ARCHIVED.equals(before) || STATUS_REJECTED.equals(before)) {
            throw new ServiceException("举报已办结或不予受理，不能再流转");
        }
        if (ACT_REJECT.equals(action)) {
            // 不予受理：仅允许研判前（已受理 / 研判中）
            if (!STATUS_ACCEPTED.equals(before) && !STATUS_REVIEWING.equals(before)) {
                throw new ServiceException("当前状态不允许执行该动作");
            }
        } else {
            // 其余动作严格按状态机前驱校验，不可跳步
            String prev = prevStatus(target);
            if (prev == null || !before.equals(prev)) {
                throw new ServiceException("流程不可跳步：当前状态不允许执行该动作");
            }
        }

        Date now = new Date();
        petition.setStatus(target);

        // 受理：起算办理时限
        if (ACT_ACCEPT.equals(action)) {
            petition.setHandleDeadline(plusHours(now, readDeadlineHours()));
            petition.setWarnLevel(calcWarnLevel(petition.getHandleDeadline(), target, now));
        }
        // 分办：必填承办部门，并转线索
        if (ACT_DISPATCH.equals(action)) {
            if (req.getDeptId() == null) {
                throw new ServiceException("分办必须指定承办部门");
            }
            JcClue clue = buildClueFromPetition(petition, req);
            clueService.insertJcClue(clue);
            petition.setClueId(clue.getClueId());
            writeTrace(petitionId, ACT_DISPATCH,
                    "分办并转线索 " + clue.getClueNo() + "（承办部门：" + req.getDeptId() + "）",
                    before, target, currentOperator());
        }
        // 反馈：必填反馈内容，记录办结时点
        if (ACT_FEEDBACK.equals(action)) {
            if (StringUtils.isEmpty(req.getFeedback())) {
                throw new ServiceException("反馈内容不能为空");
            }
            petition.setFeedback(req.getFeedback());
        }
        // 终态：清预警
        if (STATUS_ARCHIVED.equals(target) || STATUS_REJECTED.equals(target)) {
            petition.setWarnLevel("");
        }
        petition.setUpdateBy(currentOperator());
        petitionMapper.updateJcPetition(petition);

        if (!ACT_DISPATCH.equals(action)) {
            writeTrace(petitionId, action, buildActionDesc(action, req),
                    before, target, currentOperator());
        }
        return petitionMapper.selectJcPetitionByPetitionId(petitionId);
    }

    @Override
    @Transactional
    public int deleteJcPetitionByPetitionId(Long petitionId) {
        traceMapper.deleteTraceByPetitionId(petitionId);
        materialMapper.deleteMaterialByPetitionId(petitionId);
        messageMapper.deleteMessageByPetitionId(petitionId); // 段16：级联清理留言
        return petitionMapper.deleteJcPetitionByPetitionId(petitionId);
    }

    @Override
    public List<JcPetition> detectDuplicate(String targetName) {
        if (StringUtils.isEmpty(targetName)) {
            return Collections.emptyList();
        }
        int days = readInt(CFG_DUPLICATE_DAYS, DEFAULT_DUPLICATE_DAYS);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -days);
        return petitionMapper.selectRecentByTarget(targetName.trim(), cal.getTime());
    }

    // ═══════════════════════ 段16：留言轨迹/催办/督办/评价 ═══════════════════════

    @Override
    public void appendTrace(Long petitionId, String action, String actionDesc,
                            String fromStatus, String toStatus, String operator) {
        writeTrace(petitionId, action, actionDesc, fromStatus, toStatus, operator);
    }

    @Override
    @Transactional
    public JcPetition urge(Long petitionId, String opinion, String operator) {
        JcPetition petition = petitionMapper.selectJcPetitionByPetitionId(petitionId);
        if (petition == null) {
            throw new ServiceException("举报不存在或已删除");
        }
        if (STATUS_ARCHIVED.equals(petition.getStatus()) || STATUS_REJECTED.equals(petition.getStatus())) {
            throw new ServiceException("举报已办结，无需催办");
        }
        Date now = new Date();
        petition.setUrgeCount(petition.getUrgeCount() == null ? 1 : petition.getUrgeCount() + 1);
        petition.setLastUrgeTime(now);
        petition.setUpdateBy(operator);
        petitionMapper.updateJcPetition(petition);
        writeTrace(petitionId, ACT_URGE,
                "催办" + (StringUtils.isEmpty(opinion) ? "" : "：" + opinion) + "（累计第 " + petition.getUrgeCount() + " 次）",
                petition.getStatus(), petition.getStatus(), operator);
        return petitionMapper.selectJcPetitionByPetitionId(petitionId);
    }

    @Override
    public List<JcPetition> selectOverdue() {
        return petitionMapper.selectOverdue();
    }

    @Override
    @Transactional
    public JcPetition rateByReceipt(String receiptCode, Integer satisfaction, String comment) {
        if (satisfaction == null || satisfaction < 1 || satisfaction > 5) {
            throw new ServiceException("满意度评分须为 1-5 星");
        }
        String text = StringUtils.isEmpty(comment) ? null : comment.trim();
        if (text != null && text.length() > 500) {
            throw new ServiceException("评价说明不能超过 500 字");
        }
        JcPetition petition = queryByReceipt(receiptCode);
        if (petition == null) {
            throw new ServiceException("回执码不存在，请核对后重试");
        }
        if (petition.getStatus().compareTo(STATUS_FEEDBACK) < 0) {
            throw new ServiceException("办理结果反馈后才能评价");
        }
        JcPetition upd = new JcPetition();
        upd.setPetitionId(petition.getPetitionId());
        upd.setSatisfaction(satisfaction);
        upd.setSatisfactionComment(text);
        upd.setSatisfactionTime(new Date());
        upd.setUpdateBy("举报人");
        petitionMapper.updateJcPetition(upd);
        writeTrace(petition.getPetitionId(), ACT_RATE,
                "举报人评价满意度 " + satisfaction + " 星（覆盖式更新）",
                petition.getStatus(), petition.getStatus(), "举报人");
        return queryByReceipt(receiptCode);
    }

    // ═══════════════════════ 段16：并案 / 自动催办 ═══════════════════════

    @Override
    @Transactional
    public JcPetition merge(Long childId, Long masterId, String remark, String operator) {
        if (childId == null || masterId == null) {
            throw new ServiceException("从件与主件举报ID不能为空");
        }
        if (childId.equals(masterId)) {
            throw new ServiceException("不能把举报并入自身");
        }
        JcPetition child = petitionMapper.selectJcPetitionByPetitionId(childId);
        JcPetition master = petitionMapper.selectMergeMaster(masterId);
        if (child == null || master == null) {
            throw new ServiceException("从件或主件举报不存在或已删除");
        }
        if (STATUS_ARCHIVED.equals(child.getStatus()) || STATUS_REJECTED.equals(child.getStatus())) {
            throw new ServiceException("从件已办结，无需并案");
        }
        // 同一被举报对象才可并案（对象为空时按标题人工判断，这里要求双方对象一致或均为空）
        String childTarget = StringUtils.nvl(child.getTargetName(), "");
        String masterTarget = StringUtils.nvl(master.getTargetName(), "");
        if (StringUtils.isNotEmpty(childTarget) && StringUtils.isNotEmpty(masterTarget)
                && !childTarget.equals(masterTarget)) {
            throw new ServiceException("被举报对象不一致（" + childTarget + " / " + masterTarget + "），不允许并案");
        }
        // 主件不能是从件（禁止并案链）
        if (master.getMergedInto() != null) {
            throw new ServiceException("目标举报已是并案从件（并入 ID " + master.getMergedInto() + "），请以其主件为并案对象");
        }
        String mergedRemark = StringUtils.isEmpty(remark) ? "与 ID " + masterId + " 并案办理" : remark.trim();
        petitionMapper.updateMerge(childId, masterId, mergedRemark, operator);
        // 双侧留痕：从件记并入事实，主件记收并事实
        writeTrace(childId, ACT_MERGE, "并案：并入主件 ID " + masterId + "（" + mergedRemark + "）",
                child.getStatus(), child.getStatus(), operator);
        writeTrace(masterId, ACT_MERGE, "收并从件 ID " + childId + "（" + child.getTitle() + "）",
                master.getStatus(), master.getStatus(), operator);
        return petitionMapper.selectJcPetitionByPetitionId(childId);
    }

    @Override
    @Transactional
    public JcPetition unmerge(Long childId, String operator) {
        JcPetition child = petitionMapper.selectJcPetitionByPetitionId(childId);
        if (child == null) {
            throw new ServiceException("举报不存在或已删除");
        }
        if (child.getMergedInto() == null) {
            throw new ServiceException("该举报不是并案从件");
        }
        Long masterId = child.getMergedInto();
        petitionMapper.updateUnmerge(childId, operator);
        writeTrace(childId, ACT_UNMERGE, "解除并案：恢复独立办理（原主件 ID " + masterId + "）",
                child.getStatus(), child.getStatus(), operator);
        writeTrace(masterId, ACT_UNMERGE, "从件 ID " + childId + " 解除并案，恢复独立",
                null, null, operator);
        return petitionMapper.selectJcPetitionByPetitionId(childId);
    }

    @Override
    public List<JcPetition> selectMergeCandidates(Long petitionId) {
        JcPetition petition = petitionMapper.selectJcPetitionByPetitionId(petitionId);
        if (petition == null) {
            throw new ServiceException("举报不存在或已删除");
        }
        if (StringUtils.isEmpty(petition.getTargetName())) {
            return Collections.emptyList();
        }
        return petitionMapper.selectMergeCandidates(petition.getTargetName(), petitionId);
    }

    @Override
    public List<JcPetition> selectMergedChildren(Long masterId) {
        return petitionMapper.selectMergedChildren(masterId);
    }

    @Override
    @Transactional
    public int autoUrgeOverdue() {
        List<JcPetition> overdue = petitionMapper.selectOverdueNeverUrged();
        int count = 0;
        for (JcPetition p : overdue) {
            try {
                Date now = new Date();
                JcPetition upd = new JcPetition();
                upd.setPetitionId(p.getPetitionId());
                upd.setUrgeCount(1);
                upd.setLastUrgeTime(now);
                upd.setUpdateBy("system");
                petitionMapper.updateJcPetition(upd);
                writeTrace(p.getPetitionId(), ACT_URGE,
                        "系统自动催办：办理已超期（截止 " + (p.getHandleDeadline() == null ? "-" : p.getHandleDeadline())
                                + "），请尽快办结并反馈举报人",
                        p.getStatus(), p.getStatus(), "system");
                count++;
            } catch (Exception e) {
                // 单件失败不阻断批次
            }
        }
        return count;
    }

    @Override
    public List<JcPetition> selectDissatisfied() {
        return petitionMapper.selectDissatisfied();
    }

    // ═══════════════════════ 预警与统计 ═══════════════════════

    @Override
    public int refreshWarnLevel() {
        List<JcPetition> list = petitionMapper.selectActivePetitions();
        int changed = 0;
        for (JcPetition p : list) {
            if (p.getHandleDeadline() == null) {
                continue; // 未受理不起算
            }
            Date base = p.getCreateTime() != null ? p.getCreateTime() : new Date();
            String level = calcWarnLevel(p.getHandleDeadline(), p.getStatus(), base);
            if (!level.equals(StringUtils.nvl(p.getWarnLevel(), ""))) {
                petitionMapper.updateWarnLevel(p.getPetitionId(), level);
                changed++;
            }
        }
        return changed;
    }

    @Override
    public PetitionStat stat() {
        PetitionStat stat = new PetitionStat();
        Map<String, Object> counts = petitionMapper.statCounts();
        stat.setTotal(toLong(counts.get("total")));
        stat.setPending(toLong(counts.get("pending")));
        stat.setHandling(toLong(counts.get("handling")));
        stat.setFinished(toLong(counts.get("finished")));
        stat.setOverdue(toLong(counts.get("overdue")));
        Double avg = petitionMapper.statAvgHandleHours();
        stat.setAvgHandleHours(avg == null ? 0.0 : Math.round(avg * 10.0) / 10.0);
        stat.setByStatus(toItems(petitionMapper.statByStatus()));
        stat.setByCategory(toItems(petitionMapper.statByCategory()));
        stat.setByChannel(toItems(petitionMapper.statByChannel()));
        stat.setMonthlyTrend(toItems(petitionMapper.statMonthlyTrend()));
        // 段16：满意度与催办统计
        Map<String, Object> sat = petitionMapper.statSatisfaction();
        if (sat != null) {
            Object satAvg = sat.get("satisfactionAvg");
            stat.setSatisfactionAvg(satAvg instanceof Number
                    ? Math.round(((Number) satAvg).doubleValue() * 10.0) / 10.0 : 0.0);
            stat.setSatisfactionCount(toLong(sat.get("satisfactionCount")));
        }
        stat.setSatisfactionDist(toItems(petitionMapper.statSatisfactionDist()));
        stat.setUrgeTotal(petitionMapper.statUrgeTotal());
        // 段16：热点对象 Top10 与低分复核计数
        stat.setTopTargets(petitionMapper.statTopTargets(10));
        List<JcPetition> dissatisfied = petitionMapper.selectDissatisfied();
        stat.setDissatisfiedCount((long) (dissatisfied == null ? 0 : dissatisfied.size()));
        return stat;
    }

    // ═══════════════════════ 内部方法 ═══════════════════════

    /** 分办转线索：复用 M4 线索模块，source_ref 回指举报 */
    private JcClue buildClueFromPetition(JcPetition petition, PetitionActionRequest req) {
        JcClue clue = new JcClue();
        clue.setSourceType("1".equals(petition.getAnonymousFlag()) ? "anonymous" : "report");
        clue.setViolationType(CATEGORY_TO_VIOLATION.getOrDefault(petition.getCategory(), "8"));
        clue.setSummary(petition.getTitle());
        clue.setDetail(petition.getContent());
        clue.setDeptId(req.getDeptId());
        clue.setHandlerId(req.getHandlerId());
        clue.setRelatedPersons(petition.getTargetName());
        clue.setSourceRef("petition:" + petition.getPetitionId());
        return clue;
    }

    private void writeTrace(Long petitionId, String action, String actionDesc,
                            String fromStatus, String toStatus, String operator) {
        JcPetitionTrace trace = new JcPetitionTrace();
        trace.setPetitionId(petitionId);
        trace.setAction(action);
        trace.setActionDesc(actionDesc);
        trace.setFromStatus(fromStatus);
        trace.setToStatus(toStatus);
        trace.setOperator(operator);
        trace.setCreateTime(new Date());
        traceMapper.insertTrace(trace);
    }

    private String buildActionDesc(String action, PetitionActionRequest req) {
        String opinion = req.getOpinion();
        switch (action) {
            case ACT_ACCEPT:
                return "受理举报" + suffix(opinion);
            case ACT_REVIEW:
                return "研判" + suffix(opinion);
            case ACT_HANDLE:
                return "办理登记" + suffix(opinion);
            case ACT_FEEDBACK:
                return "向举报人反馈办理结果";
            case ACT_ARCHIVE:
                return "归档" + suffix(opinion);
            case ACT_REJECT:
                return "不予受理" + suffix(opinion);
            default:
                return action;
        }
    }

    private String suffix(String opinion) {
        return StringUtils.isEmpty(opinion) ? "" : "：" + opinion;
    }

    /** 目标状态的前置状态（状态机严格单向，用于跳步校验） */
    private String prevStatus(String target) {
        switch (target) {
            case STATUS_ACCEPTED:   return STATUS_PENDING;
            case STATUS_REVIEWING:  return STATUS_ACCEPTED;
            case STATUS_DISPATCHED: return STATUS_REVIEWING;
            case STATUS_HANDLING:   return STATUS_DISPATCHED;
            case STATUS_FEEDBACK:   return STATUS_HANDLING;
            case STATUS_ARCHIVED:   return STATUS_FEEDBACK;
            case STATUS_REJECTED:   return null; // 1/2 均可不予受理，单独放行
            default:                return null;
        }
    }

    /** 计算预警级别（口径与线索一致：""/黄灯/红灯，时限按小时） */
    private String calcWarnLevel(Date deadline, String status, Date base) {
        if (deadline == null || STATUS_ARCHIVED.equals(status) || STATUS_REJECTED.equals(status)) {
            return "";
        }
        long total = Math.max(1, hoursBetween(base, deadline));
        long used = hoursBetween(base, new Date());
        if (used >= total) {
            return WARN_RED;
        }
        double ratio = (double) used / (double) total;
        return ratio >= readWarnRatio() ? WARN_YELLOW : "";
    }

    /** 回执码检索值：SHA-256(码 + pepper)，建唯一索引 */
    private String receiptLookup(String receiptCode) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((receiptCode + "|" + receiptPepper).getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("回执码检索值计算失败", e);
        }
    }

    /** 16 位回执码：4-4-4-4 随机数字 */
    private String generateReceiptCode() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder(19);
        for (int g = 0; g < 4; g++) {
            if (g > 0) {
                sb.append('-');
            }
            sb.append(String.format("%04d", rnd.nextInt(10000)));
        }
        return sb.toString();
    }

    private boolean isCaptchaEnabled() {
        try {
            return "1".equals(StringUtils.nvl(configService.selectConfigByKey(CFG_CAPTCHA), "").trim());
        } catch (Exception ignored) {
            return false;
        }
    }

    private int readDeadlineHours() {
        return readInt(CFG_DEADLINE_HOURS, DEFAULT_DEADLINE_HOURS);
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

    private static Date plusHours(Date base, int hours) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(base);
        cal.add(Calendar.HOUR_OF_DAY, hours);
        return cal.getTime();
    }

    private static long hoursBetween(Date from, Date to) {
        return (to.getTime() - from.getTime()) / (3600L * 1000L);
    }

    private static String currentOperator() {
        try {
            String username = SecurityUtils.getUsername();
            return StringUtils.isEmpty(username) ? "system" : username;
        } catch (Exception e) {
            return "system"; // 免登录上下文无登录人
        }
    }

    /** 统计行 -> PetitionStatItem */
    private List<PetitionStatItem> toItems(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        List<PetitionStatItem> items = new java.util.ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            String name = row.get("name") == null ? "" : String.valueOf(row.get("name"));
            items.add(new PetitionStatItem(name, toLong(row.get("value"))));
        }
        return items;
    }

    /** 统计值 -> Long（兼容 JDBC 返回的 BigInteger/BigDecimal/Long） */
    private Long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }
}
