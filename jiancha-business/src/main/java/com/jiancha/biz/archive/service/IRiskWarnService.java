package com.jiancha.biz.archive.service;

import com.jiancha.biz.archive.domain.RiskEvaluationResult;

/**
 * 廉政风险预警（12 类）服务
 *
 * @author 小标
 */
public interface IRiskWarnService
{
    /**
     * 评估某人员的 12 类风险预警命中情况（只读，不落库）
     */
    RiskEvaluationResult evaluate(Long personId);

    /**
     * 评估并将结果写回：更新 jc_person 的 risk_level / integrity_score，
     * 并刷新由本引擎派生的廉政画像标签（source_table='risk-engine'）。
     */
    RiskEvaluationResult applyToPerson(Long personId);
}
