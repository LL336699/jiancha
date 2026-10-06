package com.jiancha.biz.archive.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.SecurityUtils;
import com.jiancha.biz.archive.domain.JcConflictDeclare;
import com.jiancha.biz.archive.domain.JcConflictItem;
import com.jiancha.biz.archive.domain.JcPerson;
import com.jiancha.biz.archive.domain.JcPersonCareer;
import com.jiancha.biz.archive.domain.JcPersonPunish;
import com.jiancha.biz.archive.domain.JcPersonRelation;
import com.jiancha.biz.archive.domain.JcPersonTag;
import com.jiancha.biz.archive.domain.RiskEvaluationResult;
import com.jiancha.biz.archive.domain.RiskHit;
import com.jiancha.biz.archive.mapper.JcConflictDeclareMapper;
import com.jiancha.biz.archive.mapper.JcConflictItemMapper;
import com.jiancha.biz.archive.mapper.JcPersonCareerMapper;
import com.jiancha.biz.archive.mapper.JcPersonMapper;
import com.jiancha.biz.archive.mapper.JcPersonPunishMapper;
import com.jiancha.biz.archive.mapper.JcPersonRelationMapper;
import com.jiancha.biz.archive.mapper.JcPersonTagMapper;
import com.jiancha.biz.archive.service.IJcPersonService;
import com.jiancha.biz.archive.service.IJcPersonTagService;
import com.jiancha.biz.archive.service.IRiskWarnService;

/**
 * 企业版风险预警引擎（12 类 + 处分执行跟踪）。
 *
 * <p>规则口径已由党政版切换为<b>企业 / 自用版</b>：移除"裸官、子女经商、频繁调动、突击提拔"
 * 等党政专属规则，改为企业真实场景的利益冲突与舞弊风险规则。</p>
 *
 * <p>实现方式分两类：
 * <ul>
 *   <li>数据可派生（1/2/7/8/11/13）：直接基于档案子表与利益冲突申报数据计算；</li>
 *   <li>标签兜底（3/4/5/6/9/10/12）：依赖外部业务系统数据（采购、报销、考勤等），
 *       本期通过"风险"类廉政画像标签识别，待对应数据表建成后自动转为数据派生。</li>
 * </ul>
 * 所有命中均写出证据，保证可溯源。</p>
 *
 * @author 小标
 */
@Service
public class RiskWarnServiceImpl implements IRiskWarnService
{
    private static final int PENALTY_HIGH = 30;
    private static final int PENALTY_MID = 15;
    private static final int PENALTY_LOW = 5;

    /** 关键岗位未轮岗预警年限阈值 */
    private static final int ROTATE_WARN_YEARS = 5;
    private static final int ROTATE_HIGH_YEARS = 8;

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
    private JcConflictDeclareMapper declareMapper;
    @Autowired
    private JcConflictItemMapper itemMapper;
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

        // 利益冲突申报：已申报的企业主体名称集合，用于漏报比对
        Set<String> declaredEnts = new HashSet<>();
        boolean hasDoubtDeclare = false;
        List<JcConflictDeclare> declares = declareMapper.selectByPersonId(personId);
        for (JcConflictDeclare d : declares)
        {
            if ("存疑".equals(d.getStatus()))
            {
                hasDoubtDeclare = true;
            }
            List<JcConflictItem> items = itemMapper.selectByDeclareId(d.getDeclareId());
            for (JcConflictItem it : items)
            {
                if (hasText(it.getEntName()))
                {
                    declaredEnts.add(it.getEntName().trim());
                }
            }
        }

        boolean keyPosition = false;
        for (JcPersonRelation r : relations)
        {
            if ("1".equals(r.getIsKeyPosition()))
            {
                keyPosition = true;
                break;
            }
        }

        List<RiskHit> hits = new ArrayList<>();

        // 规则1 员工亲属设立/持股供应商
        for (JcPersonRelation r : relations)
        {
            boolean kin = !"本人".equals(orEmpty(r.getRelationType()));
            boolean holder = "持股".equals(r.getRelationKind()) || "实际控制".equals(r.getRelationKind());
            boolean supplier = "1".equals(r.getIsSupplier());
            if (kin && (supplier || holder))
            {
                hits.add(hit(1, "亲属设立/持股供应商", "高",
                        "亲属（" + orEmpty(r.getRelationType()) + "）" + (supplier ? "为本公司供应商" : "")
                                + (holder ? "持股/实际控制经营主体" : "") + "，存在利益输送风险",
                        "relation_id=" + r.getRelationId() + " ent=" + orEmpty(r.getEntName())
                                + " ratio=" + r.getHoldRatio() + " kind=" + orEmpty(r.getRelationKind()),
                        "核查该供应商准入与交易公允性，必要时回避或调整岗位"));
            }
        }
        // 规则2 本人或亲属在合作方任职/兼职取酬（含本人经商办企业）
        for (JcPersonRelation r : relations)
        {
            String kind = orEmpty(r.getRelationKind());
            boolean employed = "任职".equals(kind) || "兼职取酬".equals(kind) || "劳务报酬".equals(kind);
            boolean partner = "1".equals(r.getIsSupplier()) || "1".equals(r.getIsCustomer());
            boolean selfBiz = "本人".equals(orEmpty(r.getRelationType()))
                    && ("持股".equals(kind) || "任职".equals(kind) || "实际控制".equals(kind));
            if (employed && partner)
            {
                hits.add(hit(2, "合作方任职/兼职取酬", "高",
                        "本人或关联人在本公司合作方任职/兼职取酬",
                        "relation_id=" + r.getRelationId() + " ent=" + orEmpty(r.getEntName())
                                + " duty=" + orEmpty(r.getRelationDuty()) + " kind=" + kind,
                        "按利益冲突管理要求责令整改或停止兼职"));
            }
            else if (selfBiz)
            {
                hits.add(hit(2, "本人经商办企业", "高",
                        "本人存在经商办企业/持股/实际控制经营主体记录",
                        "relation_id=" + r.getRelationId() + " ent=" + orEmpty(r.getEntName())
                                + " ratio=" + r.getHoldRatio(),
                        "核实是否违反公司兼职与经商禁止性规定"));
            }
        }
        // 规则7 关键岗位长期未轮岗
        Date latestStart = null;
        for (JcPersonCareer c : careers)
        {
            if (c.getStartDate() != null && (latestStart == null || c.getStartDate().after(latestStart)))
            {
                latestStart = c.getStartDate();
            }
        }
        if (latestStart != null)
        {
            long days = LocalDate.now().toEpochDay() - toLocalDate(latestStart).toEpochDay();
            int years = (int) (days / 365);
            if (years >= ROTATE_HIGH_YEARS)
            {
                hits.add(hit(7, "关键岗位长期未轮岗", "高",
                        "现岗位连续任职约 " + years + " 年，超过 " + ROTATE_HIGH_YEARS + " 年阈值",
                        "career 最近起始日期 " + toLocalDate(latestStart) + " keyPosition=" + keyPosition,
                        "纳入轮岗计划，必要时开展离任/在任审计"));
            }
            else if (years >= ROTATE_WARN_YEARS && keyPosition)
            {
                hits.add(hit(7, "关键岗位长期未轮岗", "中",
                        "关键岗位现职约 " + years + " 年，达到 " + ROTATE_WARN_YEARS + " 年预警阈值",
                        "career 最近起始日期 " + toLocalDate(latestStart),
                        "纳入轮岗观察名单"));
            }
        }
        // 规则8 申报矛盾/漏报
        if (hasDoubtDeclare)
        {
            hits.add(hit(8, "申报核实存疑", "中",
                    "存在核实结论为【存疑】的利益冲突申报",
                    "person_id=" + personId + " declares=" + declares.size(),
                    "跟进存疑事项核实与处置"));
        }
        for (JcPersonRelation r : relations)
        {
            String ent = r.getEntName();
            if (!hasText(ent))
            {
                continue;
            }
            boolean related = "1".equals(r.getIsSupplier()) || "1".equals(r.getIsCustomer())
                    || "持股".equals(r.getRelationKind()) || "任职".equals(r.getRelationKind())
                    || "实际控制".equals(r.getRelationKind());
            if (related && !declaredEnts.contains(ent.trim()))
            {
                hits.add(hit(8, "关联主体漏报", "中",
                        "关联档案中存在经营主体「" + ent + "」，但历次申报均未填报",
                        "relation_id=" + r.getRelationId() + " declared=" + declaredEnts,
                        "要求本人补充申报并说明情况"));
            }
        }
        // 规则11 旋转门：离职后任职合作方
        if ("离职".equals(person.getStatus()))
        {
            for (JcPersonRelation r : relations)
            {
                if ("1".equals(r.getIsSupplier()) || "1".equals(r.getIsCustomer()))
                {
                    hits.add(hit(11, "旋转门", "中",
                            "离职人员仍与本公司合作方存在任职/持股关联",
                            "relation_id=" + r.getRelationId() + " ent=" + orEmpty(r.getEntName())
                                    + " start=" + r.getStartDate(),
                            "核查离职后从业限制与竞业限制执行情况"));
                    break;
                }
            }
        }
        // 规则13 处分执行异常（企业版保留：内部处分执行跟踪）
        for (JcPersonPunish p : punishes)
        {
            if (p.getEffectiveDate() == null || !hasText(p.getRelatedCaseNo()))
            {
                hits.add(hit(13, "处分执行异常", "中",
                        "处分记录缺少生效日期或关联案件编号，执行闭环不完整",
                        "punish_id=" + p.getPunishId(),
                        "补全处分执行与案件关联记录"));
            }
        }
        // 规则3/4/5/6/9/10/12 依赖外部业务数据，经风险类标签兜底识别
        Map<String, Integer> tagRule = new HashMap<>();
        tagRule.put("采购审批跳跃", 3);
        tagRule.put("单一来源", 4);
        tagRule.put("围标", 5);
        tagRule.put("串标", 5);
        tagRule.put("费用报销", 6);
        tagRule.put("举报", 9);
        tagRule.put("审批权限", 10);
        tagRule.put("频繁请假", 12);
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
                    String name = tagRuleName(e.getValue());
                    hits.add(hit(e.getValue(), name, orDefault(t.getTagLevel(), "中"),
                            "根据廉政画像标签识别：" + t.getTagName(),
                            "tag_id=" + t.getTagId() + " source=" + t.getSourceTable() + "#" + t.getSourceId(),
                            "结合原始业务记录核实"));
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

    /** 标签兜底规则的名称（用于展示） */
    private String tagRuleName(int no)
    {
        switch (no)
        {
            case 3: return "采购审批跳跃";
            case 4: return "单一来源采购异常集中";
            case 5: return "围标串标特征";
            case 6: return "费用报销异常";
            case 9: return "被举报集中反映";
            case 10: return "审批权限与金额不匹配";
            case 12: return "频繁请假等弱信号";
            default: return "其他风险";
        }
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

    private String orEmpty(String s)
    {
        return s == null ? "" : s;
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
