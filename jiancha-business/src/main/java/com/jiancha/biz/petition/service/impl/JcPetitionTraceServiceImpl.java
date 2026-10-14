package com.jiancha.biz.petition.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jiancha.biz.petition.domain.JcPetitionTrace;
import com.jiancha.biz.petition.mapper.JcPetitionTraceMapper;
import com.jiancha.biz.petition.service.IJcPetitionTraceService;

/**
 * 舞弊举报办理轨迹 Service 实现
 *
 * @author 小标
 */
@Service
public class JcPetitionTraceServiceImpl implements IJcPetitionTraceService {

    @Autowired
    private JcPetitionTraceMapper traceMapper;

    @Override
    public List<JcPetitionTrace> selectTraceByPetitionId(Long petitionId) {
        return traceMapper.selectTraceByPetitionId(petitionId);
    }

    @Override
    public int insertTrace(JcPetitionTrace trace) {
        return traceMapper.insertTrace(trace);
    }
}
