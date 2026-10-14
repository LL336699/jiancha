package com.jiancha.biz.petition.mapper;

import java.util.List;

import com.jiancha.biz.petition.domain.JcPetitionTrace;

/**
 * 舞弊举报办理轨迹 Mapper
 *
 * @author 小标
 */
public interface JcPetitionTraceMapper {

    /** 按举报查询轨迹（时间正序，管理端） */
    List<JcPetitionTrace> selectTraceByPetitionId(Long petitionId);

    /** 追加轨迹 */
    int insertTrace(JcPetitionTrace trace);

    /** 逻辑删除举报时同步清理轨迹 */
    int deleteTraceByPetitionId(Long petitionId);
}
