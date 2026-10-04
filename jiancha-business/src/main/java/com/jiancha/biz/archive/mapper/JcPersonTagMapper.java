package com.jiancha.biz.archive.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.jiancha.biz.archive.domain.JcPersonTag;

/**
 * 廉政画像标签 Mapper（全部可溯源）
 *
 * @author 小标
 */
public interface JcPersonTagMapper
{
    /** 按条件查询标签列表 */
    List<JcPersonTag> selectJcPersonTagList(JcPersonTag jcPersonTag);

    /** 查询某人员的全部标签 */
    List<JcPersonTag> selectByPersonId(Long personId);

    /** 查询标签详情 */
    JcPersonTag selectByTagId(Long tagId);

    /** 新增标签 */
    int insertJcPersonTag(JcPersonTag jcPersonTag);

    /** 修改标签 */
    int updateJcPersonTag(JcPersonTag jcPersonTag);

    /** 逻辑删除单个标签 */
    int deleteJcPersonTagByTagId(Long tagId);

    /** 逻辑删除某人员全部标签（档案删除联级用） */
    int deleteByPersonId(Long personId);

    /** 逻辑删除某人员指定溯源来源的标签（风险引擎刷新派生标签用） */
    int deleteBySourceTable(@Param("personId") Long personId, @Param("sourceTable") String sourceTable);
}
