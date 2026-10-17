package com.jiancha.biz.notify.service;

import java.util.List;

import com.jiancha.biz.notify.domain.JcNotifyMessage;

/**
 * 站内消息通知 服务接口（段17 消息通知中心）
 *
 * <p>各业务模块通过本接口触达用户；事件发送与本人消息管理均在此收口。</p>
 *
 * @author 小标
 */
public interface INotifyService {

    /**
     * 发送消息给单个用户（幂等：同接收人+同标题当日只落一条）
     *
     * @param receiver     接收人用户名
     * @param notifyType   消息类型（字典 jc_notify_type）
     * @param bizModule    业务模块标识
     * @param bizId        业务主键（可空）
     * @param title        标题
     * @param content      内容（可空）
     * @return 实际落库条数（0=当日重复被跳过）
     */
    public int send(String receiver, String notifyType, String bizModule, Long bizId, String title, String content);

    /**
     * 发送消息给多个用户
     *
     * @return 实际落库条数
     */
    public int sendToUsers(List<String> receivers, String notifyType, String bizModule, Long bizId, String title, String content);

    /**
     * 按角色 key 群发（正常状态用户）
     *
     * @return 实际落库条数
     */
    public int sendToRoleKey(String roleKey, String notifyType, String bizModule, Long bizId, String title, String content);

    /**
     * 查询本人消息列表（分页由控制器 startPage 控制）
     */
    public List<JcNotifyMessage> selectMyList(JcNotifyMessage query);

    /**
     * 本人未读数（顶栏铃铛轮询）
     */
    public long countUnread(String receiver);

    /**
     * 本人未读数按类型分布（消息中心统计条）
     */
    public List<java.util.Map<String, Object>> statUnreadByType(String receiver);

    /**
     * 本人最近未读预览（铃铛下拉）
     */
    public List<JcNotifyMessage> selectRecentUnread(String receiver, int limit);

    /**
     * 标记单条已读（校验归属）
     */
    public int markRead(Long notifyId, String receiver);

    /**
     * 全部已读
     */
    public int markAllRead(String receiver);

    /**
     * 删除本人单条消息（校验归属）
     */
    public int remove(Long notifyId, String receiver);

    /**
     * 清空本人已读消息
     */
    public int cleanRead(String receiver);

    /**
     * 清理 N 天前已读消息（定时任务调用）
     */
    public int cleanExpired(int days);
}
