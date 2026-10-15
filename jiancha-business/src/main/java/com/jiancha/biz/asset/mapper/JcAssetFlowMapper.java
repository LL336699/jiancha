package com.jiancha.biz.asset.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.jiancha.biz.asset.domain.JcAssetFlow;

/**
 * 涉案财物流转记录 Mapper（段11 E6）
 */
public interface JcAssetFlowMapper
{
    /** 新增流转记录（自增主键回填） */
    @Insert("insert into jc_asset_flow (asset_id, flow_type, from_status, to_status, operator, flow_date, "
            + "flow_desc, voucher_no, create_by, create_time, del_flag) "
            + "values (#{assetId}, #{flowType}, #{fromStatus}, #{toStatus}, #{operator}, #{flowDate}, "
            + "#{flowDesc}, #{voucherNo}, #{createBy}, now(), '0')")
    @Options(useGeneratedKeys = true, keyProperty = "flowId")
    int insertFlow(JcAssetFlow flow);

    /** 按财物ID查流转记录（时间倒序，全程保管链） */
    List<JcAssetFlow> selectByAssetId(@Param("assetId") Long assetId);

    /** 统计某财物的流转次数 */
    @Select("select count(1) from jc_asset_flow where asset_id = #{assetId} and del_flag = '0'")
    long countByAssetId(@Param("assetId") Long assetId);
}
