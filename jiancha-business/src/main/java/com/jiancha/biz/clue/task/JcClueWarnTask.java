package com.jiancha.biz.clue.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jiancha.biz.clue.service.IJcClueService;

/**
 * 问题线索时限预警定时任务
 *
 * <p>由 RuoYi 定时任务（Quartz）按日调度，扫描未办结线索并刷新红黄灯。
 * 在「系统监控 → 定时任务」中新增任务，调用目标字符串填
 * {@code jcClueWarnTask.refreshWarnLevel()}，建议 cron 为 {@code 0 0 8 * * ?}（每日 8:00）。</p>
 *
 * @author 小标
 */
@Component("jcClueWarnTask")
public class JcClueWarnTask {

    private static final Logger log = LoggerFactory.getLogger(JcClueWarnTask.class);

    @Autowired
    private IJcClueService clueService;

    /**
     * 刷新线索红黄灯预警。
     */
    public void refreshWarnLevel() {
        int changed = clueService.refreshWarnLevel();
        log.info("[线索预警扫描] 完成，预警级别变更 {} 条", changed);
    }
}
