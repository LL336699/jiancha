package com.jiancha.biz.conduct.mapper;

import java.util.List;
import java.util.Map;

/**
 * 作风建设跨模块只读聚合 Mapper
 *
 * <p>消费方模块内写 SQL，不反向依赖他模块 Mapper（项目约定）。</p>
 *
 * @author 小标
 */
public interface JcConductScanMapper {

    /** 人员档案简况（姓名/部门，登记时快照） */
    Map<String, Object> selectPersonBrief(Long personId);

    /** 人员检索（登记选人，姓名/工号模糊，档案域 del_flag='0'） */
    List<Map<String, Object>> searchPersons(String keyword);

    /** 该人员在办作风建设问题数（重复登记提示用） */
    long countActiveByPerson(Long personId);

    /** 转整改查重：同一 conduct 单号是否已立整改项（jc_rectify_issue.source_ref） */
    Long selectRectifyIdBySourceRef(String sourceRef);

    /** 整改单号最大前缀（生成 RX+日期+序号 用，与 E3 模块同规则） */
    String selectMaxRectifyNoByPrefix(String prefix);
}
