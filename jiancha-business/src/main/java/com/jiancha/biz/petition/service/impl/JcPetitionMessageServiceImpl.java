package com.jiancha.biz.petition.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

import com.jiancha.biz.petition.domain.JcPetition;
import com.jiancha.biz.petition.domain.JcPetitionMessage;
import com.jiancha.biz.petition.mapper.JcPetitionMessageMapper;
import com.jiancha.biz.petition.mapper.JcPetitionMapper;
import com.jiancha.biz.petition.security.PetitionRateLimiter;
import com.jiancha.biz.petition.service.IJcPetitionMessageService;
import com.jiancha.biz.petition.service.IJcPetitionService;

/**
 * 舞弊举报双向留言 Service 实现（段16）
 *
 * <p>安全要点：</p>
 * <ul>
 *   <li>举报人侧一切操作以回执码为凭证：SHA-256 检索哈希定位 + BCrypt 二次校验
 *       （复用 {@link IJcPetitionService#queryByReceipt}），杜绝枚举 ID 越权；</li>
 *   <li>举报人留言落库不落真名（sender_name 固定「举报人」），不落 IP；</li>
 *   <li>已办结（归档/不予受理）举报关闭留言，防止办结后钓鱼沟通；</li>
 *   <li>受理人回复写轨迹留痕（action=reply），满足全程可审计。</li>
 * </ul>
 *
 * @author 小标
 */
@Service
public class JcPetitionMessageServiceImpl implements IJcPetitionMessageService {

    /** 发言方：举报人 */
    public static final String SENDER_REPORTER = "reporter";
    /** 发言方：受理人 */
    public static final String SENDER_STAFF = "staff";

    /** 留言长度上限 */
    private static final int MAX_CONTENT_LEN = 2000;

    @Autowired
    private JcPetitionMessageMapper messageMapper;

    @Autowired
    private JcPetitionMapper petitionMapper;

    @Autowired
    private IJcPetitionService petitionService;

    /**
     * 留言限流（复用滑动窗口限流器）：键取「msg:回执码」，
     * 与提交限流（IP 键）相互独立；同一举报的留言频率受限，防恶意刷屏。
     */
    @Autowired
    private PetitionRateLimiter rateLimiter;

    @Override
    @Transactional
    public int postByReceipt(String receiptCode, String content) {
        if (StringUtils.isEmpty(content)) {
            throw new ServiceException("留言内容不能为空");
        }
        if (StringUtils.isEmpty(receiptCode)) {
            throw new ServiceException("回执码不能为空");
        }
        String text = content.trim();
        if (text.length() > MAX_CONTENT_LEN) {
            throw new ServiceException("留言不能超过 " + MAX_CONTENT_LEN + " 字");
        }
        // 免登录留言防刷频：同一回执码窗口内限次
        if (!rateLimiter.tryAcquire("msg:" + receiptCode.trim())) {
            throw new ServiceException("留言过于频繁，请稍后再试");
        }
        // 凭回执码定位举报（内部完成哈希校验与敏感字段清洗）
        JcPetition petition = petitionService.queryByReceipt(receiptCode);
        if (petition == null) {
            throw new ServiceException("回执码不存在，请核对后重试");
        }
        if ("6".equals(petition.getStatus()) || "7".equals(petition.getStatus())) {
            throw new ServiceException("该举报已办结，留言通道已关闭");
        }
        JcPetitionMessage message = new JcPetitionMessage();
        message.setPetitionId(petition.getPetitionId());
        message.setSender(SENDER_REPORTER);
        message.setSenderName("举报人");
        message.setContent(text);
        int rows = messageMapper.insertJcPetitionMessage(message);
        if (rows > 0) {
            // 轨迹留痕：只记「举报人留言」事实，正文不进轨迹（轨迹管理端可见，正文双方可见）
            petitionService.appendTrace(petition.getPetitionId(), "message",
                    "举报人留言 " + text.length() + " 字", petition.getStatus(), petition.getStatus(), "举报人");
        }
        return rows;
    }

    @Override
    @Transactional
    public List<JcPetitionMessage> listByReceipt(String receiptCode) {
        JcPetition petition = petitionService.queryByReceipt(receiptCode);
        if (petition == null) {
            throw new ServiceException("回执码不存在，请核对后重试");
        }
        List<JcPetitionMessage> list = messageMapper.selectByPetitionAndSender(petition.getPetitionId(), null);
        // 举报人打开对话即视为已读 staff 回复
        messageMapper.markRead(petition.getPetitionId(), SENDER_STAFF);
        return list;
    }

    @Override
    @Transactional
    public List<JcPetitionMessage> listByPetitionId(Long petitionId) {
        List<JcPetitionMessage> list = messageMapper.selectByPetitionAndSender(petitionId, null);
        // 管理端打开对话即视为已读举报人留言
        messageMapper.markRead(petitionId, SENDER_REPORTER);
        return list;
    }

    @Override
    @Transactional
    public int reply(Long petitionId, String content, String operator) {
        if (StringUtils.isEmpty(content)) {
            throw new ServiceException("回复内容不能为空");
        }
        String text = content.trim();
        if (text.length() > MAX_CONTENT_LEN) {
            throw new ServiceException("回复不能超过 " + MAX_CONTENT_LEN + " 字");
        }
        JcPetition petition = petitionMapper.selectJcPetitionByPetitionId(petitionId);
        if (petition == null) {
            throw new ServiceException("举报不存在或已删除");
        }
        if ("6".equals(petition.getStatus()) || "7".equals(petition.getStatus())) {
            throw new ServiceException("举报已办结，不能再回复");
        }
        JcPetitionMessage message = new JcPetitionMessage();
        message.setPetitionId(petitionId);
        message.setSender(SENDER_STAFF);
        message.setSenderName(operator);
        message.setContent(text);
        int rows = messageMapper.insertJcPetitionMessage(message);
        if (rows > 0) {
            petitionService.appendTrace(petitionId, "reply",
                    "受理人回复留言 " + text.length() + " 字", petition.getStatus(), petition.getStatus(), operator);
        }
        return rows;
    }

    @Override
    public Map<Long, Long> unreadCountMap() {
        Map<Long, Long> result = new HashMap<>();
        for (Map<String, Object> row : messageMapper.countUnreadGrouped()) {
            Object key = row.get("petitionId");
            if (key instanceof Number) {
                result.put(((Number) key).longValue(), toLong(row.get("unread")));
            }
        }
        return result;
    }

    @Override
    public Long countUnreadReporter(Long petitionId) {
        Long cnt = messageMapper.countUnread(petitionId, SENDER_REPORTER);
        return cnt == null ? 0L : cnt;
    }

    private Long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }
}
