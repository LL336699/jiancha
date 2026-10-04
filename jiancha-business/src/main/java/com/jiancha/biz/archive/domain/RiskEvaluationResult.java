package com.jiancha.biz.archive.domain;

import java.util.List;

/**
 * 廉政风险评估结果
 *
 * @author 小标
 */
public class RiskEvaluationResult
{
    /** 人员ID */
    private Long personId;
    /** 综合风险等级：高/中/低/正常 */
    private String riskLevel;
    /** 廉政评分（100 起扣） */
    private Integer integrityScore;
    /** 命中的预警清单 */
    private List<RiskHit> hits;

    public Long getPersonId() { return personId; }
    public void setPersonId(Long personId) { this.personId = personId; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public Integer getIntegrityScore() { return integrityScore; }
    public void setIntegrityScore(Integer integrityScore) { this.integrityScore = integrityScore; }
    public List<RiskHit> getHits() { return hits; }
    public void setHits(List<RiskHit> hits) { this.hits = hits; }
}
