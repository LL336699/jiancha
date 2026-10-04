package com.jiancha.biz.archive.service;

import java.util.List;
import com.jiancha.biz.archive.domain.JcPersonTag;

/**
 * 廉政画像标签 Service（全部可溯源）
 *
 * @author 小标
 */
public interface IJcPersonTagService
{
    List<JcPersonTag> selectJcPersonTagList(JcPersonTag jcPersonTag);

    List<JcPersonTag> selectByPersonId(Long personId);

    JcPersonTag selectByTagId(Long tagId);

    int insertJcPersonTag(JcPersonTag jcPersonTag);

    int updateJcPersonTag(JcPersonTag jcPersonTag);

    int deleteJcPersonTagByTagId(Long tagId);

    int deleteByPersonId(Long personId);
}
