package com.jiancha.biz.dashboard.mapper;

import com.jiancha.biz.dashboard.domain.OverviewSnapshot;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 综合看板每日快照 Mapper（段12 纪检综合看板）
 *
 * @author 小标
 */
@Mapper
public interface JcOverviewSnapshotMapper {

    /** 插入（或按 snapshot_date 更新）一条每日快照 */
    int insertOrUpdate(OverviewSnapshot snapshot);

    /** 最新一条快照（按日期倒序取首条） */
    OverviewSnapshot selectLatest();

    /** 最近 N 条快照（按日期倒序） */
    List<OverviewSnapshot> selectHistory();
}
