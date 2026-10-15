package com.jiancha.biz.reply.mapper;

import java.util.List;

import com.jiancha.biz.reply.domain.JcReplyScreenItem;

/**
 * 廉政意见回复·筛查明细 Mapper（E2）
 *
 * @author 小标
 */
public interface JcReplyScreenItemMapper {

    /** 按申请查明细 */
    List<JcReplyScreenItem> selectByRequestId(Long requestId);

    /** 按申请查分来源计数 */
    java.util.List<java.util.Map<String, Object>> countByModule(Long requestId);

    /** 按申请删除（重新筛查前清空） */
    int deleteByRequestId(Long requestId);

    /** 批量写入 */
    int batchInsert(List<JcReplyScreenItem> items);

    /** 统计：某来源模块的总命中数（全局） */
    long countBySourceModule(String sourceModule);

    /** 统计：分来源命中分布（字典 jc_reply_source） */
    java.util.List<java.util.Map<String, Object>> statHitsByModule();
}
