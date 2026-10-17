package com.jiancha.biz.notify.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jiancha.biz.notify.domain.JcNotifyMessage;

/**
 * 站内消息通知 数据层（段17）
 *
 * @author 小标
 */
public interface JcNotifyMessageMapper {

    /**
     * 新增消息
     */
    public int insertJcNotifyMessage(JcNotifyMessage message);

    /**
     * 按接收人分页查询（readFlagQuery 空查全部）
     */
    public List<JcNotifyMessage> selectListByReceiver(JcNotifyMessage query);

    /**
     * 按接收人查询未读数
     */
    public long countUnread(@Param("receiver") String receiver);

    /**
     * 查询接收人最近 N 条未读（铃铛下拉预览）
     */
    public List<JcNotifyMessage> selectRecentUnread(@Param("receiver") String receiver, @Param("limit") int limit);

    /**
     * 读取本人单条消息（校验归属）
     */
    public JcNotifyMessage selectByNotifyIdAndReceiver(@Param("notifyId") Long notifyId, @Param("receiver") String receiver);

    /**
     * 标记单条已读（限本人未读）
     */
    public int markRead(@Param("notifyId") Long notifyId, @Param("receiver") String receiver);

    /**
     * 全部已读（限本人）
     */
    public int markAllRead(@Param("receiver") String receiver);

    /**
     * 删除本人单条消息
     */
    public int deleteByNotifyIdAndReceiver(@Param("notifyId") Long notifyId, @Param("receiver") String receiver);

    /**
     * 清空本人已读消息
     */
    public int deleteReadByReceiver(@Param("receiver") String receiver);

    /**
     * 去重探测：同接收人 + 同标题当日是否已有消息
     */
    public int existsToday(@Param("receiver") String receiver, @Param("title") String title);

    /**
     * 清理 N 天前的已读消息（定时任务防膨胀）
     */
    public int deleteReadBefore(@Param("days") int days);

    /**
     * 按角色 key 查询正常状态用户的用户名列表（角色群发用）
     */
    public List<String> selectUserNamesByRoleKey(@Param("roleKey") String roleKey);

    /**
     * 查用户昵称（发送时冗余落库）
     */
    public String selectNickNameByUserName(@Param("userName") String userName);

    /**
     * 本人未读数按类型分布（消息中心统计条）
     */
    public List<java.util.Map<String, Object>> statUnreadByType(@Param("receiver") String receiver);
}
