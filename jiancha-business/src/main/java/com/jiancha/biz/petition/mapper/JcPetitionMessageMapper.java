package com.jiancha.biz.petition.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.jiancha.biz.petition.domain.JcPetitionMessage;

/**
 * 舞弊举报留言 数据层（段16）
 *
 * @author 小标
 */
public interface JcPetitionMessageMapper {

    /** 新增留言 */
    int insertJcPetitionMessage(JcPetitionMessage message);

    /** 按举报查询全部留言（时间正序，管理端视角含双方） */
    List<JcPetitionMessage> selectByPetitionId(Long petitionId);

    /** 按举报查询留言（sender 过滤：null/空 = 全部） */
    List<JcPetitionMessage> selectByPetitionAndSender(@Param("petitionId") Long petitionId, @Param("sender") String sender);

    /** 举报人视角：按回执码检索哈希查往来留言（时间正序） */
    List<JcPetitionMessage> selectByReceiptLookup(String receiptLookup);

    /** 标记某举报下指定发言方的留言为已读（读方为对方） */
    int markRead(@Param("petitionId") Long petitionId, @Param("sender") String sender);

    /** 统计某举报下指定发言方的未读留言数（管理端列表提示） */
    Long countUnread(@Param("petitionId") Long petitionId, @Param("sender") String sender);

    /** 汇总各举报的 staff 留言未读数（key=petitionId/value=未读数） */
    List<java.util.Map<String, Object>> countUnreadGrouped();

    /** 按举报删除留言（级联清理） */
    int deleteMessageByPetitionId(Long petitionId);
}
