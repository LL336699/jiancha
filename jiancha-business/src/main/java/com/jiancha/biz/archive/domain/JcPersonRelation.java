package com.jiancha.biz.archive.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 亲属关系及经商办企业情况 jc_person_relation
 *
 * @author 小标
 */
public class JcPersonRelation extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long relationId;
    private Long personId;
    /** 关系（字典 jc_relation_type：配偶/子女/父母/其他） */
    private String relationType;
    private String relationName;
    /** 任职单位/经营主体 */
    private String relationWork;
    /** 职务 */
    private String relationDuty;
    /** 是否境外（0否 1是） */
    private String isAbroad;
    /** 本人是否关键岗位（0否 1是） */
    private String isKeyPosition;

    public Long getRelationId() { return relationId; }
    public void setRelationId(Long relationId) { this.relationId = relationId; }
    public Long getPersonId() { return personId; }
    public void setPersonId(Long personId) { this.personId = personId; }
    public String getRelationType() { return relationType; }
    public void setRelationType(String relationType) { this.relationType = relationType; }
    public String getRelationName() { return relationName; }
    public void setRelationName(String relationName) { this.relationName = relationName; }
    public String getRelationWork() { return relationWork; }
    public void setRelationWork(String relationWork) { this.relationWork = relationWork; }
    public String getRelationDuty() { return relationDuty; }
    public void setRelationDuty(String relationDuty) { this.relationDuty = relationDuty; }
    public String getIsAbroad() { return isAbroad; }
    public void setIsAbroad(String isAbroad) { this.isAbroad = isAbroad; }
    public String getIsKeyPosition() { return isKeyPosition; }
    public void setIsKeyPosition(String isKeyPosition) { this.isKeyPosition = isKeyPosition; }
}
