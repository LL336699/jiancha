package com.jiancha.biz.reply.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jiancha.biz.reply.service.IJcReplyService;

/**
 * 廉政意见回复调度任务（E2）
 *
 * <p>办理时限红黄灯日刷新（口径同线索/整改：超期=红灯，窗口过半=黄灯）。
 * 每日 07:50 执行。</p>
 *
 * @author 小标
 */
@Component("jcReplyScanTask")
public class JcReplyScanTask {

    @Autowired
    private IJcReplyService replyService;

    /**
     * 刷新红黄灯
     *
     * @return 灯色发生变化的申请条数（日志可观测）
     */
    public int scan() {
        int changed = replyService.refreshWarnLevel();
        if (changed > 0) {
            System.out.println("[jcReplyScanTask] warn level changed requests: " + changed);
        }
        return changed;
    }

    /** 供调度无参调用（invokeTarget: jcReplyScanTask.run） */
    public void run() {
        scan();
    }
}
