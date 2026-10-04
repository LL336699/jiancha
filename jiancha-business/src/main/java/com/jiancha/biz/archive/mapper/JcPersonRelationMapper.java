package com.jiancha.biz.archive.mapper;

import java.util.List;
import com.jiancha.biz.archive.domain.JcPersonRelation;

/**
 * 亲属关系及经商办企业情况 Mapper
 *
 * @author 小标
 */
public interface JcPersonRelationMapper
{
    List<JcPersonRelation> selectJcPersonRelationList(JcPersonRelation jcPersonRelation);

    List<JcPersonRelation> selectByPersonId(Long personId);

    JcPersonRelation selectByRelationId(Long relationId);

    int insertJcPersonRelation(JcPersonRelation jcPersonRelation);

    int updateJcPersonRelation(JcPersonRelation jcPersonRelation);

    int deleteJcPersonRelationByRelationId(Long relationId);

    int deleteByPersonId(Long personId);
}
