package com.jiancha.biz.clue.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jiancha.biz.clue.domain.JcClueTrace;
import com.jiancha.biz.clue.mapper.JcClueTraceMapper;
import com.jiancha.biz.clue.service.IJcClueTraceService;

/**
 * 问题线索办理轨迹 Service 实现
 *
 * @author 小标
 */
@Service
public class JcClueTraceServiceImpl implements IJcClueTraceService {

    @Autowired
    private JcClueTraceMapper traceMapper;

    @Override
    public List<JcClueTrace> selectTraceByClueId(Long clueId) {
        return traceMapper.selectTraceByClueId(clueId);
    }

    @Override
    public int insertTrace(JcClueTrace trace) {
        return traceMapper.insertTrace(trace);
    }
}
