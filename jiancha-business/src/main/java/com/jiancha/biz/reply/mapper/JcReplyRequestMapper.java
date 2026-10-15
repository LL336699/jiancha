package com.jiancha.biz.reply.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.jiancha.biz.reply.domain.JcReplyRequest;

/**
 * 廉政意见回复申请 Mapper（E2）
 *
 * @author 小标
 */
public interface JcReplyRequestMapper {

    /** 列表 */
    List<JcReplyRequest> selectJcReplyRequestList(JcReplyRequest query);

    /** 详情 */
    JcReplyRequest selectJcReplyRequestByRequestId(Long requestId);

    /** 新增（登记） */
    int insertJcReplyRequest(JcReplyRequest request);

    /** 修改 */
    int updateJcReplyRequest(JcReplyRequest request);

    /** 筛查完成：写入建议结论 + 状态推进 */
    int screenDone(@Param("requestId") Long requestId,
                   @Param("suggestResult") String suggestResult,
                   @Param("hitCount") Integer hitCount,
                   @Param("updateBy") String updateBy);

    /** 出具正式结论（回复） */
    int replyDone(@Param("requestId") Long requestId,
                  @Param("finalResult") String finalResult,
                  @Param("replyContent") String replyContent,
                  @Param("replyDate") java.util.Date replyDate,
                  @Param("updateBy") String updateBy);

    /** 关闭 */
    int closeRequest(@Param("requestId") Long requestId, @Param("updateBy") String updateBy);

    /** 逻辑删除 */
    int deleteJcReplyRequestByRequestId(Long requestId);

    /** 编号最大前缀（生成 requestNo 用） */
    String selectMaxNoByPrefix(String prefix);

    /** 进行中申请（状态 0/1，红黄灯刷新用） */
    List<JcReplyRequest> selectActiveRequests();

    /** 刷新红黄灯 */
    int updateWarnLevel(@Param("requestId") Long requestId, @Param("warnLevel") String warnLevel);

    /** 统计：申请总量 */
    long countTotal();

    /** 统计：已回复量 */
    long countReplied();

    /** 统计：超期未回复量（收到日期+时限 < 今天 且 状态非终态） */
    long countOverdue();

    /** 统计：按状态计数 */
    long countByStatus(String status);

    /** 统计：已回复平均办理天数 */
    Double statAvgReplyDays();

    /** 统计：按事项类型分布（字典 jc_reply_matter） */
    List<Map<String, Object>> statByMatter();

    /** 统计：按正式结论分布（字典 jc_reply_conclusion） */
    List<Map<String, Object>> statByResult();

    /** 统计：近12个月申请量 */
    List<Map<String, Object>> statMonthlyRequests();

    /** 统计：按对象部门分布（Top10） */
    List<Map<String, Object>> statByDept();
}
