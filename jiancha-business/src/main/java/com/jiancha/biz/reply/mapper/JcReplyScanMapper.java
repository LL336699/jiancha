package com.jiancha.biz.reply.mapper;

import java.util.List;
import java.util.Map;

/**
 * 廉政意见回复·跨模块筛查聚合 Mapper（E2）
 *
 * <p>六个数据源只读聚合：问题线索 / 舞弊举报 / 整改问题 / 风险预警 / 供应商关联 / 廉政处分。
 * 全部为跨模块只读 SQL（消费方模块内编写，不反向依赖他模块 Mapper）。</p>
 *
 * @author 小标
 */
public interface JcReplyScanMapper {

    /** 人员档案简要信息（姓名/部门，登记时快照补全；档案域 del_flag='0'） */
    Map<String, Object> selectPersonBrief(Long personId);

    /** 在办问题线索（person_id 命中，状态非办结/不予受理） */
    List<Map<String, Object>> scanClues(Long personId);

    /** 被举报记录（petition.target_name 命中姓名） */
    List<Map<String, Object>> scanPetitions(Long personId);

    /** 未闭环整改问题（target_person_id 命中，状态 0-2） */
    List<Map<String, Object>> scanRectify(Long personId);

    /** 未闭环风险预警（object_type=0 人员，状态 0/1） */
    List<Map<String, Object>> scanRiskAlerts(Long personId);

    /** 供应商关联（jc_supplier_link 命中，未处置或观察/禁入） */
    List<Map<String, Object>> scanSupplierLinks(Long personId);

    /** 廉政处分记录（jc_person_punish 命中，近3年） */
    List<Map<String, Object>> scanPunishments(Long personId);

    /** 人员检索（登记选人：姓名/编号模糊，档案域 del_flag='0'） */
    List<Map<String, Object>> searchPersons(String keyword);
}
