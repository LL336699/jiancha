package com.jiancha.biz.clue.service;

import java.util.List;
import com.jiancha.biz.clue.domain.JcClueTrace;

/**
 * 问题线索办理轨迹 Service
 *
 * @author 小标
 */
public interface IJcClueTraceService {

    /** 按线索查询轨迹（时间正序） */
    List<JcClueTrace> selectTraceByClueId(Long clueId);

    /** 追加轨迹 */
    int insertTrace(JcClueTrace trace);
}
