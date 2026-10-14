package com.jiancha.biz.edu.service;

import java.util.Map;

/**
 * 廉政教育统计服务（M9）
 *
 * @author 小标
 */
public interface IJcEduStatService {

    /** 汇总统计（卡片 + 图表 + 明细视图） */
    Map<String, Object> stat();
}
