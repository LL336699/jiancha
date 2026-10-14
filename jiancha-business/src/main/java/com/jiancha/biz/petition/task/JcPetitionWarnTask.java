package com.jiancha.biz.petition.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jiancha.biz.petition.service.IJcPetitionService;

/**
 * 舞弊举报时限预警定时任务
 *
 * <p>由 RuoYi 定时任务（Quartz）按日调度，扫描已受理未办结的举报并刷新红黄灯。
 * 在「系统监控 → 定时任务」中新增任务，调用目标字符串填
 * {@code jcPetitionWarnTask.refreshWarnLevel()}，建议 cron 为 {@code 0 0 8 * * ?}（每日 8:00）。</p>
 *
 * @author 小标
 */
@Component("jcPetitionWarnTask")
public class JcPetitionWarnTask {

    private static final Logger log = LoggerFactory.getLogger(JcPetitionWarnTask.class);

    @Autowired
    private IJcPetitionService petitionService;

    /**
     * 刷新举报红黄灯预警。
     */
    public void refreshWarnLevel() {
        int changed = petitionService.refreshWarnLevel();
        log.info("[举报预警扫描] 完成，预警级别变更 {} 条", changed);
    }
}
