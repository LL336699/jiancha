package com.jiancha.biz.supplier.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jiancha.biz.supplier.service.IJcSupplierLinkService;
import com.jiancha.biz.supplier.service.IJcSupplierService;

/**
 * 供应商廉洁管理定时任务（E4）
 *
 * <p>Quartz 日调度（每日 07:00，cron 见 jiancha_supplier.sql 任务初始化）：</p>
 * <ol>
 *   <li>系统比对：E1 利益冲突申报 / 关联关系 × 供应商库，命中自动落排查记录；</li>
 *   <li>协议状态刷新：临期（≤ 预警天数）/ 已过期；</li>
 *   <li>黑名单到期自动解除（期限届满），并恢复档案状态。</li>
 * </ol>
 *
 * @author 小标
 */
@Component("jcSupplierScanTask")
public class JcSupplierScanTask {

    @Autowired
    private IJcSupplierLinkService linkService;

    @Autowired
    private IJcSupplierService supplierService;

    /** 日调度入口：比对 + 协议刷新 + 黑名单到期解除 */
    public void scan() {
        int matched = linkService.scanMatch();
        String refresh = supplierService.dailyRefresh();
        System.out.println("[jcSupplierScanTask] 系统比对新增 " + matched + " 条；" + refresh);
    }
}
