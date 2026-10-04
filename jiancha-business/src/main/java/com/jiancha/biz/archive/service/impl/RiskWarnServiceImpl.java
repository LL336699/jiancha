package com.jiancha.biz.archive.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.SecurityUtils;
import com.jiancha.biz.archive.domain.JcPerson;
import com.jiancha.biz.archive.domain.JcPersonCareer;
import com.jiancha.biz.archive.domain.JcPersonPunish;
import com.jiancha.biz.archive.domain.JcPersonRelation;
import com.jiancha.biz.archive.domain.JcPersonTag;
import com.jiancha.biz.archive.domain.RiskEvaluationResult;
import com.jiancha.biz.archive.domain.RiskHit;
import com.jiancha.biz.archive.mapper.JcPersonCareerMapper;
import com.jiancha.biz.archive.mapper.JcPersonMapper;
import com.jiancha.biz.archive.mapper.JcPersonPunishMapper;
import com.jiancha.biz.archive.mapper.JcPersonRelationMapper;
import com.jiancha.biz.archive.mapper.JcPersonTagMapper;
import com.jiancha.biz.archive.service.IJcPersonService;
import com.jiancha.biz.archive.service.IJcPersonTagService;
import com.jiancha.biz.archive.service.IRiskWarnService;

/**
 * 廉政风险预警（12 类）服务实现。
 *
 * <p>规则实现说明（参考设计文档 M5 12 类预警）：
 * <ul>
 *   <li>数据可派生的规则（1/2/3/4/5/10/11）直接基于档案子表计算；</li>
 *   <li>依赖未建表的规则（6 频繁请假 / 7 财产申报异常 / 8 涉标关联 / 9 审批跳跃 / 12 信访集中反映）
 *       本期通过已有的"风险"类廉政画像标签兜底识别，待 M4/M10 相关数据表建成后自动转为数据派生。</li>
 * </ul>
 * 所有命中均写出证据，保证可溯源。
 * </p>
 *
 * @author 小标
 */
@Service
public class RiskWarnServiceImpl implements IRiskWarnService
{
    private static final int PENALTY_HIGH = 30;
    private static final int PENALTY_MID = 15;
    private static final int PENALTY_LOW = 5;

    @Autowired
    private JcPersonMapper personMapper;
    @Autowired
    private JcPersonCareerMapper careerMapper;
    @Autowired
    private JcPersonRelationMapper relationMapper;
    @Autowired
    private JcPersonPunishMapper punishMapper;
    @Autowired
    private JcPersonTagMapper tagMapper;
    @Autowired
    private IJcPersonService personService;
    @Autowired
    private IJcPersonTagService tagService;

    @Override
    public RiskEvaluationResult evaluate(Long personId)
    {
        JcPerson person = personMapper.selectJcPersonByPersonId(personId);
        if (person == null)
        {
            return null;
        }
        List<JcPersonCareer> careers = careerMapper.selectByPersonId(personId);
        List<JcPersonRelation> relations = relationMapper.selectByPersonId(personId);
        List<JcPersonPunish> punishes = punishMapper.selectByPersonId(personId);
        List<JcPersonTag> tags = tagMapper.selectByPersonId(personId);

        List<RiskHit> hits = new ArrayList<>();

        // 规则1 兼职取酬
        for (JcPersonRelation r : relations)
        {
            if ("本人".equals(r.getRelationType()) && hasText(r.getRelationWork()))
            {
                hits.add(hit(1, "兼职取酬", "高",
                        "本人存在经商办企业/兼职取酬记录",
                        "relation_id=" + r.getRelationId() + " work=" + r.getRelationWork(),
                        "核实是否违规兼职取酬，按《公务员法》处理"));
            }
        }
        // 规则2 裸官
        for (JcPersonRelation r : relations)
        {
            if ("1".equals(r.getIsAbroad()) && "1".equals(r.getIsKeyPosition()))
            {
                hits.add(hit(2, "裸官", "高",
                        "配偶/子女在境外且本人任关键岗位",
                        "relation_id=" + r.getRelationId(),
                        "按裸官管理规定调整岗位"));
            }
        }
        // 规则3 子女经商办企业
        for (JcPersonRelation r : relations)
        {
            if (("配偶".equals(r.getRelationType()) || "子女".equals(r.getRelationType()) || "父母".equals(r.getRelationType()))
                    && hasText(r.getRelationWork()))
            {
                hits.add(hit(3, "子女经商办企业", "中",
                        "直系亲属经商办企业，需关注与本人职权关联",
                        "relation_id=" + r.getRelationId() + " work=" + r.getRelationWork(),
                        "核查经营范围与职权是否存在利益冲突"));
            }
        }
        // 规则4 频繁调动（近3年调动>=3次）
        long recentMoves = careers.stream()
                .filter(c -> c.getStartDate() != null)
                .filter(c -> !toLocalDate(c.getStartDate()).isBefore(LocalDate.now().minusYears(3)))
                .count();
        if (recentMoves >= 3)
        {
            hits.add(hit(4, "频繁调动", "中",
                    "近3年内调动 " + recentMoves + " 次",
                    "career 记录近3年 " + recentMoves + " 条",
                    "关注调动合理性，排查突击调整"));
        }
        // 规则5 突击提拔
        for (JcPersonCareer c : careers)
        {
            if (hasText(c.getDutyDesc()) && (c.getDutyDesc().contains("破格") || c.getDutyDesc().contains("突击")))
            {
                hits.add(hit(5, "突击提拔", "中",
                        "任职描述含破格/突击提拔字样",
                        "career_id=" + c.getCareerId(),
                        "复核提拔程序合规性"));
            }
        }
        // 规则10 离职后异常
        if ("离职".equals(person.getStatus()))
        {
            for (JcPersonRelation r : relations)
            {
                if (hasText(r.getRelationWork()))
                {
                    hits.add(hit(10, "离职后异常", "中",
                            "离职后本人/亲属仍关联经营主体",
                            "relation_id=" + r.getRelationId(),
                            "核查是否存在离职后利益输送"));
                    break;
                }
            }
        }
        // 规则11 处分执行异常
        for (JcPersonPunish p : punishes)
        {
            if (p.getEffectiveDate() == null || !hasText(p.getRelatedCaseNo()))
            {
                hits.add(hit(11, "处分执行异常", "中",
                        "处分记录缺少生效日期或关联案件编号",
                        "punish_id=" + p.getPunishId(),
                        "补全处分执行与案件关联记录"));
            }
        }
        // 规则6/7/8/9/12 依赖未建表，经已有的风险类标签兜底识别
        Map<String, Integer> tagRule = new HashMap<>();
        tagRule.put("频繁请假", 6);
        tagRule.put("财产申报异常", 7);
        tagRule.put("涉标关联", 8);
        tagRule.put("审批跳跃", 9);
        tagRule.put("信访集中反映", 12);
        for (JcPersonTag t : tags)
        {
            if (!"风险".equals(t.getTagType()) || t.getTagName() == null)
            {
                continue;
            }
            for (Map.Entry<String, Integer> e : tagRule.entrySet())
            {
                if (t.getTagName().contains(e.getKey()))
                {
                    hits.add(hit(e.getValue(), e.getKey(), orDefault(t.getTagLevel(), "中"),
                            "根据廉政画像标签识别：" + t.getTagName(),
                            "tag_id=" + t.getTagId() + " source=" + t.getSourceTable() + "#" + t.getSourceId(),
                            "结合原始记录核实"));
                }
            }
        }

        // 汇总等级与评分
        String level = "正常";
        int score = 100;
        for (RiskHit h : hits)
        {
            level = maxLevel(level, h.getLevel());
            score -= penalty(h.getLevel());
        }
        if (score < 0)
        {
            score = 0;
        }

        RiskEvaluationResult res = new RiskEvaluationResult();
        res.setPersonId(personId);
        res.setRiskLevel(level);
        res.setIntegrityScore(score);
        res.setHits(hits);
        return res;
    }

    @Override
    public RiskEvaluationResult applyToPerson(Long personId)
    {
        RiskEvaluationResult res = evaluate(personId);
        if (res == null)
        {
            return null;
        }
        JcPerson person = personMapper.selectJcPersonByPersonId(personId);
        String oper = SecurityUtils.getUsername();
        person.setRiskLevel(res.getRiskLevel());
        person.setIntegrityScore(res.getIntegrityScore());
        person.setUpdateBy(oper);
        personMapper.updateJcPerson(person);

        // 刷新由本引擎派生的廉政画像标签（先清后写，避免重复）
        tagMapper.deleteBySourceTable(personId, "risk-engine");
        for (RiskHit h : res.getHits())
        {
            JcPersonTag tag = new JcPersonTag();
            tag.setPersonId(personId);
            tag.setTagType("风险");
            tag.setTagName("[预警]" + h.getCategoryName());
            tag.setTagLevel(h.getLevel());
            tag.setTagSource(h.getDescription() + "；证据：" + h.getEvidence());
            tag.setSourceTable("risk-engine");
            tag.setSourceId(personId);
            tag.setCreateBy(oper);
            tagMapper.insertJcPersonTag(tag);
        }
        return res;
    }

    private RiskHit hit(int no, String name, String level, String desc, String ev, String sug)
    {
        RiskHit h = new RiskHit();
        h.setCategoryNo(no);
        h.setCategoryName(name);
        h.setLevel(level);
        h.setDescription(desc);
        h.setEvidence(ev);
        h.setSuggestion(sug);
        return h;
    }

    private String maxLevel(String a, String b)
    {
        return rank(a) >= rank(b) ? a : b;
    }

    private int rank(String l)
    {
        if ("高".equals(l)) return 3;
        if ("中".equals(l)) return 2;
        if ("低".equals(l)) return 1;
        return 0;
    }

    private int penalty(String l)
    {
        if ("高".equals(l)) return PENALTY_HIGH;
        if ("中".equals(l)) return PENALTY_MID;
        if ("低".equals(l)) return PENALTY_LOW;
        return 0;
    }

    private boolean hasText(String s)
    {
        return s != null && !s.trim().isEmpty();
    }

    private String orDefault(String v, String d)
    {
        return hasText(v) ? v : d;
    }

    private LocalDate toLocalDate(Date d)
    {
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
