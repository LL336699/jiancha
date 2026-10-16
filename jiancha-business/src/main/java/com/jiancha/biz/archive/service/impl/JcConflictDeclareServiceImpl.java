package com.jiancha.biz.archive.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jiancha.biz.archive.domain.JcConflictDeclare;
import com.jiancha.biz.archive.domain.JcDeclareConfig;
import com.jiancha.biz.archive.domain.JcPerson;
import com.jiancha.biz.archive.mapper.JcConflictDeclareMapper;
import com.jiancha.biz.archive.service.IJcConflictDeclareService;
import com.jiancha.biz.archive.service.IJcDeclareConfigService;

/**
 * 利益冲突申报单 Service 实现
 *
 * @author 小标
 */
@Service
public class JcConflictDeclareServiceImpl implements IJcConflictDeclareService
{
    @Autowired
    private JcConflictDeclareMapper declareMapper;

    @Autowired
    private IJcDeclareConfigService configService;

    @Override
    public List<JcConflictDeclare> selectJcConflictDeclareList(JcConflictDeclare declare)
    {
        return declareMapper.selectJcConflictDeclareList(declare);
    }

    @Override
    public List<JcConflictDeclare> selectByPersonId(Long personId)
    {
        return declareMapper.selectByPersonId(personId);
    }

    @Override
    public JcConflictDeclare selectByDeclareId(Long declareId)
    {
        return declareMapper.selectByDeclareId(declareId);
    }

    @Override
    public int insertJcConflictDeclare(JcConflictDeclare declare)
    {
        return declareMapper.insertJcConflictDeclare(declare);
    }

    @Override
    public int updateJcConflictDeclare(JcConflictDeclare declare)
    {
        return declareMapper.updateJcConflictDeclare(declare);
    }

    @Override
    public int deleteJcConflictDeclareByDeclareId(Long declareId)
    {
        return declareMapper.deleteJcConflictDeclareByDeclareId(declareId);
    }

    @Override
    public Map<String, Object> statByYear(Integer year)
    {
        Map<String, Object> stat = new HashMap<>();
        stat.put("total", declareMapper.countByYear(year));
        stat.put("byStatus", declareMapper.statByStatus(year));
        stat.put("byType", declareMapper.statByType(year));
        stat.put("monthly", declareMapper.statByMonth(year));
        stat.put("conflictDeclares", declareMapper.countConflictDeclares(year));
        stat.put("blacklistItems", declareMapper.countBlacklistItems(year));
        // 段15：融合年度集中申报截止配置（临期/超期督办）
        JcDeclareConfig config = configService.selectByYear(year);
        if (config != null && config.getDeadline() != null)
        {
            stat.put("deadline", config.getDeadline());
            long diffMs = config.getDeadline().getTime() - System.currentTimeMillis();
            long days = TimeUnit.MILLISECONDS.toDays(diffMs);
            stat.put("daysLeft", days);
            stat.put("expired", days < 0);
        }
        else
        {
            stat.put("deadline", null);
            stat.put("daysLeft", null);
            stat.put("expired", false);
        }
        return stat;
    }

    @Override
    public List<JcPerson> selectUnDeclared(Integer year)
    {
        return declareMapper.selectUnDeclared(year);
    }

    @Override
    public List<Map<String, Object>> statByPerson(Integer year)
    {
        return declareMapper.statByPerson(year);
    }

    @Override
    public List<JcConflictDeclare> selectDeclareForExport(Integer year)
    {
        return declareMapper.selectDeclareForExport(year);
    }
}
