package com.jiancha.biz.petition.service;

import java.util.List;

import com.jiancha.biz.petition.domain.JcPetitionTrace;

/**
 * 舞弊举报办理轨迹 Service
 *
 * @author 小标
 */
public interface IJcPetitionTraceService {

    /** 按举报查询轨迹 */
    List<JcPetitionTrace> selectTraceByPetitionId(Long petitionId);

    /** 追加轨迹 */
    int insertTrace(JcPetitionTrace trace);
}
