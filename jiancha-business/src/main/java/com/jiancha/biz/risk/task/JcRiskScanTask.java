package com.jiancha.biz.risk.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jiancha.biz.risk.service.IJcRiskService;

/**
 * 风险预警扫描定时任务
 *
 * <p>由 RuoYi 定时任务（Quartz）按日调度，跑一遍规则引擎并落警。
 * 在「系统监控 → 定时任务」中新增任务，调用目标字符串填
 * {@code jcRiskScanTask.scan()}，建议 cron 为 {@code 0 10 8 * * ?}（每日 8:10）。</p>
 *
 * @author 小标
 */
@Component("jcRiskScanTask")
public class JcRiskScanTask {

    private static final Logger log = LoggerFactory.getLogger(JcRiskScanTask.class);

    @Autowired
    private IJcRiskService riskService;

    /**
     * 执行规则扫描并生成预警。
     */
    public void scan() {
        int created = riskService.scanAndGenerate();
        log.info("[风险预警扫描] 完成，新增预警 {} 条", created);
    }
}
