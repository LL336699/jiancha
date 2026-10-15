package com.jiancha.biz.asset.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 涉案财物跨模块聚合 Mapper（段11 E6）
 *
 * 只读聚合：人员档案快照 / 人员检索 / 来源单号带出（线索、作风问题）。
 */
public interface JcAssetScanMapper
{
    /** 人员档案快照（姓名/部门） */
    @Select("select name as personName, "
            + "(select d.dept_name from sys_dept d where d.dept_id = p.dept_id) as personDept "
            + "from jc_person p where p.person_id = #{personId} and p.del_flag = '2'")
    Map<String, Object> selectPersonBrief(@Param("personId") Long personId);

    /** 人员检索（登记时选人：姓名/编号模糊，档案域 del_flag='2'） */
    List<Map<String, Object>> searchPersons(@Param("keyword") String keyword);

    /** 来源单号带出——作风建设问题（jc_conduct_issue，业务域 del_flag='0'） */
    @Select("select person_id as personId, person_name as personName, person_dept as personDept, "
            + "violation_type as summary, issue_no as sourceNo "
            + "from jc_conduct_issue where issue_no = #{sourceNo} and del_flag = '0' limit 1")
    Map<String, Object> findConductByNo(@Param("sourceNo") String sourceNo);

    /** 来源单号带出——问题线索（jc_clue，档案域 del_flag='2'） */
    @Select("select c.person_id as personId, c.summary as summary, c.clue_no as sourceNo "
            + "from jc_clue c where c.clue_no = #{sourceNo} and c.del_flag = '2' limit 1")
    Map<String, Object> findClueByNo(@Param("sourceNo") String sourceNo);
}
