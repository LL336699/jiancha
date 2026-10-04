package com.jiancha.biz.archive.mapper;

import java.util.List;
import com.jiancha.biz.archive.domain.JcPersonPunish;

/**
 * 处分记录 Mapper
 *
 * @author 小标
 */
public interface JcPersonPunishMapper
{
    List<JcPersonPunish> selectJcPersonPunishList(JcPersonPunish jcPersonPunish);

    List<JcPersonPunish> selectByPersonId(Long personId);

    JcPersonPunish selectByPunishId(Long punishId);

    int insertJcPersonPunish(JcPersonPunish jcPersonPunish);

    int updateJcPersonPunish(JcPersonPunish jcPersonPunish);

    int deleteJcPersonPunishByPunishId(Long punishId);

    int deleteByPersonId(Long personId);
}
