package com.jiancha.biz.notify.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jiancha.biz.notify.domain.JcNotifyMessage;
import com.jiancha.biz.notify.mapper.JcNotifyMessageMapper;
import com.jiancha.biz.notify.service.INotifyService;
import com.ruoyi.common.utils.StringUtils;

/**
 * 站内消息通知 服务实现（段17）
 *
 * <p>发送幂等策略：同接收人 + 同标题当日只落一条。业务事件（用户操作触发）天然低频，
 * 定时聚合任务每日扫描可能反复命中同一批临期事项，靠该策略防刷屏；
 * 事项状态变化后标题随之变化（如「临期」→「超期」），会作为新消息再次触达。</p>
 *
 * @author 小标
 */
@Service
public class NotifyServiceImpl implements INotifyService {

    private static final Logger log = LoggerFactory.getLogger(NotifyServiceImpl.class);

    @Autowired
    private JcNotifyMessageMapper notifyMapper;

    @Override
    public int send(String receiver, String notifyType, String bizModule, Long bizId, String title, String content) {
        if (StringUtils.isEmpty(receiver) || StringUtils.isEmpty(title)) {
            return 0;
        }
        try {
            if (notifyMapper.existsToday(receiver, title) > 0) {
                return 0;
            }
            JcNotifyMessage msg = new JcNotifyMessage();
            msg.setReceiver(receiver);
            msg.setReceiverName(notifyMapper.selectNickNameByUserName(receiver));
            msg.setNotifyType(StringUtils.isEmpty(notifyType) ? "系统" : notifyType);
            msg.setBizModule(bizModule);
            msg.setBizId(bizId);
            msg.setTitle(title);
            msg.setContent(content == null ? "" : content);
            msg.setCreateBy("Auto");
            return notifyMapper.insertJcNotifyMessage(msg);
        } catch (Exception e) {
            // 通知属于旁路能力，失败只记日志，绝不影响主业务事务
            log.warn("[消息通知] 发送失败 receiver={} title={}", receiver, title, e);
            return 0;
        }
    }

    @Override
    public int sendToUsers(List<String> receivers, String notifyType, String bizModule, Long bizId, String title, String content) {
        if (receivers == null || receivers.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (String receiver : receivers) {
            count += send(receiver, notifyType, bizModule, bizId, title, content);
        }
        return count;
    }

    @Override
    public int sendToRoleKey(String roleKey, String notifyType, String bizModule, Long bizId, String title, String content) {
        if (StringUtils.isEmpty(roleKey)) {
            return 0;
        }
        try {
            return sendToUsers(notifyMapper.selectUserNamesByRoleKey(roleKey), notifyType, bizModule, bizId, title, content);
        } catch (Exception e) {
            log.warn("[消息通知] 按角色群发失败 roleKey={}", roleKey, e);
            return 0;
        }
    }

    @Override
    public List<JcNotifyMessage> selectMyList(JcNotifyMessage query) {
        return notifyMapper.selectListByReceiver(query);
    }

    @Override
    public long countUnread(String receiver) {
        return notifyMapper.countUnread(receiver);
    }

    @Override
    public List<java.util.Map<String, Object>> statUnreadByType(String receiver) {
        return notifyMapper.statUnreadByType(receiver);
    }

    @Override
    public List<JcNotifyMessage> selectRecentUnread(String receiver, int limit) {
        return notifyMapper.selectRecentUnread(receiver, Math.min(Math.max(limit, 1), 20));
    }

    @Override
    public int markRead(Long notifyId, String receiver) {
        return notifyMapper.markRead(notifyId, receiver);
    }

    @Override
    public int markAllRead(String receiver) {
        return notifyMapper.markAllRead(receiver);
    }

    @Override
    public int remove(Long notifyId, String receiver) {
        return notifyMapper.deleteByNotifyIdAndReceiver(notifyId, receiver);
    }

    @Override
    public int cleanRead(String receiver) {
        return notifyMapper.deleteReadByReceiver(receiver);
    }

    @Override
    public int cleanExpired(int days) {
        return notifyMapper.deleteReadBefore(days);
    }
}
