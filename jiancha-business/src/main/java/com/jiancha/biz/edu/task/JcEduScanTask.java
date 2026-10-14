package com.jiancha.biz.edu.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jiancha.biz.edu.service.IJcEduExamService;

/**
 * 廉政教育调度任务（M9）
 *
 * <p>考试超时自动弃考。每日 07:40 执行。</p>
 *
 * @author 小标
 */
@Component("jcEduScanTask")
public class JcEduScanTask {

    @Autowired
    private IJcEduExamService examService;

    /**
     * 超时未交卷自动弃考
     *
     * @return 弃考条数（日志可观测）
     */
    public int scan() {
        int abandoned = examService.autoAbandonTimeout();
        if (abandoned > 0) {
            System.out.println("[jcEduScanTask] auto abandoned exams: " + abandoned);
        }
        return abandoned;
    }

    /** 供调度无参调用（invokeTarget: jcEduScanTask.run） */
    public void run() {
        scan();
    }
}
