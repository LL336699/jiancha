package com.jiancha.biz.rectify.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jiancha.biz.rectify.service.IJcRectifyService;

/**
 * 整改时限预警定时任务
 *
 * <p>由 RuoYi 定时任务（Quartz）按日调度，扫描未销号问题并刷新红黄灯。
 * 在「系统监控 → 定时任务」中新增任务，调用目标字符串填
 * {@code jcRectifyWarnTask.refreshWarnLevel()}，建议 cron 为 {@code 0 5 8 * * ?}（每日 8:05）。</p>
 *
 * @author 小标
 */
@Component("jcRectifyWarnTask")
public class JcRectifyWarnTask {

    private static final Logger log = LoggerFactory.getLogger(JcRectifyWarnTask.class);

    @Autowired
    private IJcRectifyService rectifyService;

    /**
     * 刷新整改问题红黄灯预警。
     */
    public void refreshWarnLevel() {
        int changed = rectifyService.refreshWarnLevel();
        log.info("[整改预警扫描] 完成，预警级别变更 {} 条", changed);
    }
}
