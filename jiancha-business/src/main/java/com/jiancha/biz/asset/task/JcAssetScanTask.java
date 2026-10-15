package com.jiancha.biz.asset.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jiancha.biz.asset.service.IJcAssetService;

/**
 * 涉案财物预警刷新任务（段11 E6）
 *
 * 每日刷新红黄灯：处置期限已过且未到终态=红灯，7 日内到期=黄灯，终态熄灯。
 * 口径同线索/整改/作风/意见回复模块。
 */
@Component("jcAssetScanTask")
public class JcAssetScanTask
{
    @Autowired
    private IJcAssetService assetService;

    public void run()
    {
        int rows = assetService.refreshWarnLevels();
        if (rows > 0)
        {
            System.out.println("[jcAssetScanTask] 涉案财物红黄灯刷新完成，更新 " + rows + " 条");
        }
    }
}
