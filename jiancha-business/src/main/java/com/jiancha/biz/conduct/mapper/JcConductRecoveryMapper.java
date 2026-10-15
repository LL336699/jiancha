package com.jiancha.biz.conduct.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.jiancha.biz.conduct.domain.JcConductRecovery;

/**
 * 作风建设问题退缴明细 Mapper
 *
 * @author 小标
 */
public interface JcConductRecoveryMapper {

    /** 按问题查退缴明细 */
    List<JcConductRecovery> selectByIssueId(Long issueId);

    /** 按ID查 */
    JcConductRecovery selectByRecoveryId(Long recoveryId);

    /** 新增退缴登记 */
    @Insert("insert into jc_conduct_recovery (issue_id, item_name, item_qty, unit, amount, "
            + "recovery_date, receiver, create_by, create_time, del_flag) "
            + "values (#{issueId}, #{itemName}, #{itemQty}, #{unit}, #{amount}, "
            + "#{recoveryDate}, #{receiver}, #{createBy}, now(), '0')")
    @org.apache.ibatis.annotations.Options(useGeneratedKeys = true, keyProperty = "recoveryId")
    int insertRecovery(JcConductRecovery recovery);

    /** 删除退缴登记 */
    @Update("update jc_conduct_recovery set del_flag = '1', update_by = #{updateBy}, update_time = now() "
            + "where recovery_id = #{recoveryId} and del_flag = '0'")
    int deleteByRecoveryId(@Param("recoveryId") Long recoveryId, @Param("updateBy") String updateBy);

    /** 某问题退缴合计（主表 amount_recovered 汇总口径） */
    @Select("select coalesce(sum(amount), 0) from jc_conduct_recovery "
            + "where issue_id = #{issueId} and del_flag = '0'")
    java.math.BigDecimal sumAmountByIssueId(Long issueId);
}
