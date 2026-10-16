package com.jiancha.biz.petition.service;

import java.util.List;

import com.jiancha.biz.petition.domain.JcPetitionMessage;

/**
 * 舞弊举报双向留言 Service（段16）
 *
 * <p>举报人凭回执码留言（匿名保护：不落真名/IP），受理人在管理端回复；
 * 双向消息均写入轨迹留痕（留言本身可沟通，轨迹记录「何时有留言」这一事实）。</p>
 *
 * @author 小标
 */
public interface IJcPetitionMessageService {

    /** 举报人凭回执码留言（校验回执码与办结状态） */
    int postByReceipt(String receiptCode, String content);

    /** 举报人视角：按回执码拉取往来留言（已读标记刷新对方留言） */
    List<JcPetitionMessage> listByReceipt(String receiptCode);

    /** 管理端：按举报查询全部留言（含双方），并刷新举报人留言为已读 */
    List<JcPetitionMessage> listByPetitionId(Long petitionId);

    /** 管理端：受理人回复（staff 发言，写轨迹留痕） */
    int reply(Long petitionId, String content, String operator);

    /** 管理端：各举报的举报人留言未读数（petitionId -> unread） */
    java.util.Map<Long, Long> unreadCountMap();

    /** 管理端：某举报的举报人留言未读数 */
    Long countUnreadReporter(Long petitionId);
}
