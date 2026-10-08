package com.jiancha.biz.clue.mapper;

import java.util.List;
import com.jiancha.biz.clue.domain.JcClueTrace;

/**
 * 问题线索办理轨迹 Mapper
 *
 * @author 小标
 */
public interface JcClueTraceMapper {

    /** 按线索查询轨迹（时间正序） */
    List<JcClueTrace> selectTraceByClueId(Long clueId);

    /** 追加轨迹 */
    int insertTrace(JcClueTrace trace);

    /** 删除线索时同步清理轨迹（逻辑删除，保留留痕） */
    int deleteTraceByClueId(Long clueId);
}
