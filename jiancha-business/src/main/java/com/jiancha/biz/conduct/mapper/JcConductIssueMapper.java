package com.jiancha.biz.conduct.mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import com.jiancha.biz.conduct.domain.JcConductIssue;

/**
 * 作风建设问题台账 Mapper
 *
 * @author 小标
 */
public interface JcConductIssueMapper {

    /** 分页列表（含剩余天数、关联整改状态） */
    List<JcConductIssue> selectJcConductIssueList(JcConductIssue jcConductIssue);

    /** 详情（含剩余天数、关联整改状态） */
    JcConductIssue selectJcConductIssueByIssueId(Long issueId);

    /** 按问题编号查 */
    JcConductIssue selectByIssueNo(String issueNo);

    /** 编号最大前缀（生成 issueNo 用） */
    String selectMaxNoByPrefix(String prefix);

    /** 新增问题登记 */
    int insertJcConductIssue(JcConductIssue jcConductIssue);

    /** 修改登记信息（仅待核查状态，XML 动态 set） */
    int updateJcConductIssue(JcConductIssue jcConductIssue);

    /** 逻辑删除（仅待核查状态） */
    int deleteJcConductIssueByIssueId(Long issueId);

    /** 核查登记（0→1） */
    @Update("update jc_conduct_issue set violation_desc = #{violationDesc}, amount = #{amount}, "
            + "severity = #{severity}, status = '1', update_by = #{updateBy}, update_time = now() "
            + "where issue_id = #{issueId} and del_flag = '0' and status = '0'")
    int verifyDone(@Param("issueId") Long issueId, @Param("violationDesc") String violationDesc,
                   @Param("amount") java.math.BigDecimal amount, @Param("severity") String severity,
                   @Param("updateBy") String updateBy);

    /** 处置登记（1→2，退缴金额一并留痕，处置即核查预警熄灯） */
    @Update("update jc_conduct_issue set handle_type = #{handleType}, handle_date = #{handleDate}, "
            + "handle_desc = #{handleDesc}, amount_recovered = #{amountRecovered}, status = '2', "
            + "warn_level = '', update_by = #{updateBy}, update_time = now() "
            + "where issue_id = #{issueId} and del_flag = '0' and status = '1'")
    int handleDone(@Param("issueId") Long issueId, @Param("handleType") String handleType,
                   @Param("handleDate") Date handleDate, @Param("handleDesc") String handleDesc,
                   @Param("amountRecovered") java.math.BigDecimal amountRecovered,
                   @Param("updateBy") String updateBy);

    /** 回填转处分关联 */
    @Update("update jc_conduct_issue set punish_id = #{punishId}, update_by = #{updateBy}, update_time = now() "
            + "where issue_id = #{issueId} and del_flag = '0'")
    int linkPunish(@Param("issueId") Long issueId, @Param("punishId") Long punishId,
                   @Param("updateBy") String updateBy);

    /** 回填转整改关联 */
    @Update("update jc_conduct_issue set rectify_id = #{rectifyId}, update_by = #{updateBy}, update_time = now() "
            + "where issue_id = #{issueId} and del_flag = '0'")
    int linkRectify(@Param("issueId") Long issueId, @Param("rectifyId") Long rectifyId,
                    @Param("updateBy") String updateBy);

    /** 回写退缴合计（退缴明细增删后重算，通用 update 不清空故专用语句） */
    @Update("update jc_conduct_issue set amount_recovered = #{amount}, update_by = #{updateBy}, update_time = now() "
            + "where issue_id = #{issueId} and del_flag = '0'")
    int updateRecovered(@Param("issueId") Long issueId, @Param("amount") java.math.BigDecimal amount,
                        @Param("updateBy") String updateBy);

    /** 关闭（0/1/2 → 3） */
    @Update("update jc_conduct_issue set status = '3', warn_level = '', close_time = now(), "
            + "update_by = #{updateBy}, update_time = now() "
            + "where issue_id = #{issueId} and del_flag = '0' and status in ('0','1','2')")
    int closeIssue(@Param("issueId") Long issueId, @Param("updateBy") String updateBy);

    /** 作废（0/1/2 → 4） */
    @Update("update jc_conduct_issue set status = '4', warn_level = '', update_by = #{updateBy}, update_time = now() "
            + "where issue_id = #{issueId} and del_flag = '0' and status in ('0','1','2')")
    int cancelIssue(@Param("issueId") Long issueId, @Param("updateBy") String updateBy);

    /** 转处分：写干部处分记录（jc_person_punish，档案域显式 del_flag='0'） */
    @Insert("insert into jc_person_punish (person_id, punish_type, punish_reason, punish_date, "
            + "effective_date, related_case_no, create_by, create_time, del_flag) "
            + "values (#{personId}, #{punishType}, #{punishReason}, #{punishDate}, #{punishDate}, "
            + "#{issueNo}, #{createBy}, now(), '0')")
    int insertPunish(@Param("personId") Long personId, @Param("punishType") String punishType,
                     @Param("punishReason") String punishReason, @Param("punishDate") Date punishDate,
                     @Param("issueNo") String issueNo, @Param("createBy") String createBy);

    /** 按关联案件编号取处分记录ID（转处分后回填关联用） */
    @org.apache.ibatis.annotations.Select("select punish_id from jc_person_punish "
            + "where related_case_no = #{issueNo} and del_flag = '0' order by punish_id desc limit 1")
    Long selectPunishIdByCaseNo(@Param("issueNo") String issueNo);

    /** 转整改：写整改问题台账（jc_rectify_issue，source_ref 回指 conduct 单号） */
    @Insert("insert into jc_rectify_issue (issue_no, source_type, source_ref, title, description, "
            + "problem_type, severity, found_date, found_dept, target_dept_id, target_person_id, "
            + "deadline, status, create_by, create_time, del_flag) "
            + "values (#{issueNo}, 'conduct', #{sourceRef}, #{title}, #{description}, "
            + "'作风问题', #{severity}, #{foundDate}, #{foundDept}, #{deptId}, #{personId}, "
            + "#{deadline}, '0', #{createBy}, now(), '0')")
    int insertRectify(@Param("issueNo") String issueNo, @Param("sourceRef") String sourceRef,
                      @Param("title") String title, @Param("description") String description,
                      @Param("severity") String severity, @Param("foundDate") Date foundDate,
                      @Param("foundDept") String foundDept, @Param("deptId") Long deptId,
                      @Param("personId") Long personId, @Param("deadline") Date deadline,
                      @Param("createBy") String createBy);

    /** 红黄灯刷新（非终态：核查超期红灯 / 临期3天黄灯；终态熄灯） */
    @Update("update jc_conduct_issue set warn_level = case "
            + "when status in ('3','4') then '' "
            + "when check_deadline is not null and check_deadline < curdate() then '红灯' "
            + "when check_deadline is not null and check_deadline <= date_add(curdate(), interval 3 day) then '黄灯' "
            + "else '' end "
            + "where del_flag = '0'")
    int refreshWarnLevel();

    /** 统计：问题总量 */
    long countTotal();

    /** 统计：待核查量 */
    long countPending();

    /** 统计：已处理量 */
    long countHandled();

    /** 统计：已关闭量 */
    long countClosed();

    /** 统计：退缴总额（元） */
    java.math.BigDecimal sumRecovered();

    /** 统计：分问题类型 */
    List<Map<String, Object>> statByType();

    /** 统计：分严重程度 */
    List<Map<String, Object>> statBySeverity();

    /** 统计：分来源 */
    List<Map<String, Object>> statBySource();

    /** 统计：分部门 */
    List<Map<String, Object>> statByDept();

    /** 统计：分处置方式 */
    List<Map<String, Object>> statByHandle();

    /** 统计：近12个月问题量 */
    List<Map<String, Object>> statMonthlyIssues();
}
