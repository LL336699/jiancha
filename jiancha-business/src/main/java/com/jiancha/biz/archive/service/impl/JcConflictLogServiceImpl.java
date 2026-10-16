package com.jiancha.biz.archive.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jiancha.biz.archive.domain.JcConflictLog;
import com.jiancha.biz.archive.mapper.JcConflictLogMapper;
import com.jiancha.biz.archive.service.IJcConflictLogService;

/**
 * 利益冲突申报变更留痕 Service 实现（段15）
 *
 * @author 小标
 */
@Service
public class JcConflictLogServiceImpl implements IJcConflictLogService
{
    @Autowired
    private JcConflictLogMapper logMapper;

    @Override
    public List<JcConflictLog> selectByDeclareId(Long declareId)
    {
        return logMapper.selectByDeclareId(declareId);
    }

    @Override
    public int record(Long declareId, String action, String detail, String operator)
    {
        JcConflictLog log = new JcConflictLog();
        log.setDeclareId(declareId);
        log.setAction(action);
        log.setDetail(detail);
        log.setOperator(operator);
        return logMapper.insertJcConflictLog(log);
    }
}
