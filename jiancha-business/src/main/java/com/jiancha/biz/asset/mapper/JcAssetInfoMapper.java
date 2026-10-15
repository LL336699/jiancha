package com.jiancha.biz.asset.mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.jiancha.biz.asset.domain.JcAssetInfo;

/**
 * 涉案财物 Mapper（段11 E6）
 *
 * 说明：业务表 del_flag='0' 正常 / '1' 删除，查询一律显式过滤。
 */
public interface JcAssetInfoMapper
{
    /** 按财物ID查详情（含 remainDays 聚合字段） */
    JcAssetInfo selectJcAssetInfoByAssetId(@Param("assetId") Long assetId);

    /** 条件查列表（含 remainDays / flowCount 聚合字段） */
    List<JcAssetInfo> selectJcAssetInfoList(JcAssetInfo jcAssetInfo);

    /** 按财物编号查（返回 null 表示可用） */
    JcAssetInfo selectAssetNoByAssetNo(@Param("assetNo") String assetNo);

    /** 编号最大前缀（生成 assetNo 用） */
    @Select("select max(asset_no) from jc_asset_info where asset_no like concat(#{prefix}, '%')")
    String selectMaxNoByPrefix(@Param("prefix") String prefix);

    /** 新增财物登记（扣押登记，状态 0 待入库） */
    @Insert("insert into jc_asset_info (asset_no, source_type, source_no, person_id, person_name, person_dept, "
            + "item_name, item_category, item_qty, unit, estimated_value, keeper, storage_place, seize_date, "
            + "dispose_deadline, status, create_by, create_time, del_flag) "
            + "values (#{assetNo}, #{sourceType}, #{sourceNo}, #{personId}, #{personName}, #{personDept}, "
            + "#{itemName}, #{itemCategory}, #{itemQty}, #{unit}, #{estimatedValue}, #{keeper}, #{storagePlace}, "
            + "#{seizeDate}, #{disposeDeadline}, '0', #{createBy}, now(), '0')")
    @Options(useGeneratedKeys = true, keyProperty = "assetId")
    int insertJcAssetInfo(JcAssetInfo jcAssetInfo);

    /** 修改财物登记（仅待入库状态允许） */
    int updateJcAssetInfo(JcAssetInfo jcAssetInfo);

    /** 逻辑删除（仅待入库状态允许） */
    @Update("update jc_asset_info set del_flag = '1', update_by = #{updateBy}, update_time = now() "
            + "where asset_id = #{assetId} and del_flag = '0' and status = '0'")
    int deleteJcAssetInfoById(@Param("assetId") Long assetId, @Param("updateBy") String updateBy);

    /** 入库暂存（0→1：登记保管人/存放地/入库日期） */
    @Update("update jc_asset_info set status = '1', keeper = #{keeper}, storage_place = #{storagePlace}, "
            + "store_date = #{storeDate}, update_by = #{updateBy}, update_time = now() "
            + "where asset_id = #{assetId} and del_flag = '0' and status = '0'")
    int updateStore(@Param("assetId") Long assetId, @Param("keeper") String keeper,
                    @Param("storagePlace") String storagePlace, @Param("storeDate") Date storeDate,
                    @Param("updateBy") String updateBy);

    /** 移交保管（1→1：换保管人，状态不变、流水记录） */
    @Update("update jc_asset_info set keeper = #{keeper}, storage_place = #{storagePlace}, "
            + "update_by = #{updateBy}, update_time = now() "
            + "where asset_id = #{assetId} and del_flag = '0' and status in ('1','2')")
    int updateHandover(@Param("assetId") Long assetId, @Param("keeper") String keeper,
                       @Param("storagePlace") String storagePlace, @Param("updateBy") String updateBy);

    /** 处置申请（1→2：定处置方式与期限） */
    @Update("update jc_asset_info set status = '2', dispose_type = #{disposeType}, dispose_deadline = #{disposeDeadline}, "
            + "update_by = #{updateBy}, update_time = now() "
            + "where asset_id = #{assetId} and del_flag = '0' and status = '1'")
    int updateDisposeApply(@Param("assetId") Long assetId, @Param("disposeType") String disposeType,
                           @Param("disposeDeadline") Date disposeDeadline, @Param("updateBy") String updateBy);

    /** 处置执行（2→3：退还/移交/销毁/收归，落处置日期与说明） */
    @Update("update jc_asset_info set status = '3', dispose_type = #{disposeType}, dispose_date = #{disposeDate}, "
            + "dispose_desc = #{disposeDesc}, update_by = #{updateBy}, update_time = now() "
            + "where asset_id = #{assetId} and del_flag = '0' and status = '2'")
    int updateDisposeDone(@Param("assetId") Long assetId, @Param("disposeType") String disposeType,
                          @Param("disposeDate") Date disposeDate, @Param("disposeDesc") String disposeDesc,
                          @Param("updateBy") String updateBy);

    /** 直接退还（1→4：暂存中经审批直接退还，免处置流程） */
    @Update("update jc_asset_info set status = '4', dispose_type = '退还', dispose_date = #{disposeDate}, "
            + "dispose_desc = #{disposeDesc}, update_by = #{updateBy}, update_time = now() "
            + "where asset_id = #{assetId} and del_flag = '0' and status = '1'")
    int updateRefund(@Param("assetId") Long assetId, @Param("disposeDate") Date disposeDate,
                     @Param("disposeDesc") String disposeDesc, @Param("updateBy") String updateBy);

    /** 红黄灯批量刷新（Quartz 日刷）：超期未处置红灯、临期黄灯、终态熄灯 */
    @Update("update jc_asset_info set warn_level = "
            + "case when status in ('3','4') then '' "
            + "     when dispose_deadline is not null and dispose_deadline < curdate() then 'red' "
            + "     when dispose_deadline is not null and dispose_deadline <= date_add(curdate(), interval 7 day) then 'yellow' "
            + "     else '' end, "
            + "update_time = now() where del_flag = '0' and status in ('0','1','2')")
    int refreshWarnLevels();

    // ═══════════════════════ 统计 ═══════════════════════

    /** 统计：财物总量 */
    @Select("select count(1) from jc_asset_info where del_flag = '0'")
    long countTotal();

    /** 统计：指定状态的财物量 */
    @Select("select count(1) from jc_asset_info where del_flag = '0' and status = #{status}")
    long countByStatus(@Param("status") String status);

    /** 统计：超期未处置量（处置期限已过且未到终态） */
    @Select("select count(1) from jc_asset_info where del_flag = '0' and status in ('0','1','2') "
            + "and dispose_deadline is not null and dispose_deadline < curdate()")
    long countOverdue();

    /** 统计：暂存中财物估值总额 */
    @Select("select coalesce(sum(estimated_value), 0) from jc_asset_info where del_flag = '0' and status in ('0','1','2')")
    java.math.BigDecimal sumEstimatedValue();

    /** 统计：按物品类别分布 */
    @Select("select item_name as name, count(1) as value from jc_asset_info where del_flag = '0' "
            + "group by item_name order by value desc limit 10")
    List<Map<String, Object>> statByCategory();

    /** 统计：按保管状态分布 */
    @Select("select case status when '0' then '待入库' when '1' then '暂存中' when '2' then '处置中' "
            + "when '3' then '已处置' when '4' then '已退还' else '未知' end as name, count(1) as value "
            + "from jc_asset_info where del_flag = '0' group by status order by status")
    List<Map<String, Object>> statByStatus();

    /** 统计：按来源类型分布 */
    @Select("select case source_type when 'clue' then '问题线索' when 'conduct' then '作风建设' "
            + "when 'petition' then '舞弊举报' when 'manual' then '手工登记' else '其他' end as name, count(1) as value "
            + "from jc_asset_info where del_flag = '0' group by source_type order by value desc")
    List<Map<String, Object>> statBySource();

    /** 统计：按保管人分布（暂存中的财物在谁手上） */
    @Select("select keeper as name, count(1) as value from jc_asset_info where del_flag = '0' "
            + "and status in ('0','1','2') and keeper is not null and keeper <> '' "
            + "group by keeper order by value desc limit 10")
    List<Map<String, Object>> statByKeeper();

    /** 统计：近12个月扣押量趋势 */
    @Select("select date_format(seize_date, '%Y-%m') as name, count(1) as value from jc_asset_info "
            + "where del_flag = '0' and seize_date >= date_sub(curdate(), interval 11 month) "
            + "group by date_format(seize_date, '%Y-%m') order by name")
    List<Map<String, Object>> statMonthlySeize();
}
