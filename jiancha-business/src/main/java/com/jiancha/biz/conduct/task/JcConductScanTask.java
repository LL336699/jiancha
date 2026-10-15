package com.jiancha.biz.conduct.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jiancha.biz.conduct.service.IJcConductService;

/**
 * 作风建设专项调度任务（E5）
 *
 * <p>核查期限红黄灯日刷新（口径同线索/整改：核查超期=红灯，临期=黄灯）。
 * 每日 07:55 执行。</p>
 *
 * @author 小标
 */
@Component("jcConductScanTask")
public class JcConductScanTask {

    @Autowired
    private IJcConductService conductService;

    /**
     * 刷新红黄灯
     *
     * @return 灯色刷新涉及的问题条数（日志可观测）
     */
    public int scan() {
        int changed = conductService.refreshWarnLevel();
        if (changed > 0) {
            System.out.println("[jcConductScanTask] warn level refreshed issues: " + changed);
        }
        return changed;
    }

    /** 供调度无参调用（invokeTarget: jcConductScanTask.run） */
    public void run() {
        scan();
    }
}
