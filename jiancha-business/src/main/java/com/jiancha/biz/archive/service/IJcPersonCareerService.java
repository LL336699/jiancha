package com.jiancha.biz.archive.service;

import java.util.List;
import com.jiancha.biz.archive.domain.JcPersonCareer;

/**
 * 任职经历 Service
 *
 * @author 小标
 */
public interface IJcPersonCareerService
{
    List<JcPersonCareer> selectJcPersonCareerList(JcPersonCareer jcPersonCareer);

    List<JcPersonCareer> selectByPersonId(Long personId);

    JcPersonCareer selectByCareerId(Long careerId);

    int insertJcPersonCareer(JcPersonCareer jcPersonCareer);

    int updateJcPersonCareer(JcPersonCareer jcPersonCareer);

    int deleteJcPersonCareerByCareerId(Long careerId);

    int deleteByPersonId(Long personId);
}
