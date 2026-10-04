package com.jiancha.biz.archive.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 廉政画像标签 jc_person_tag
 *
 * <p>设计要点：标签全部可溯源——每个标签记录 {@code sourceTable}+{@code sourceId}，
 * 可回查产生该标签的原始业务记录，防止"AI 结论不可解释"（设计文档 §M5）。</p>
 *
 * @author 小标
 */
public class JcPersonTag extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 标签ID */
    private Long tagId;

    /** 人员ID */
    private Long personId;

    /** 标签类型（字典 jc_tag_type：风险/关注/关联/历史） */
    @Excel(name = "标签类型")
    private String tagType;

    /** 标签名称 */
    @Excel(name = "标签名称")
    private String tagName;

    /** 严重度（高/中/低） */
    @Excel(name = "严重度")
    private String tagLevel;

    /** 产生来源描述（人工可读的溯源说明） */
    private String tagSource;

    /** 溯源表名（如 jc_person_punish / jc_person_relation） */
    private String sourceTable;

    /** 溯源记录ID */
    private Long sourceId;

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public String getTagType() {
        return tagType;
    }

    public void setTagType(String tagType) {
        this.tagType = tagType;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTagLevel() {
        return tagLevel;
    }

    public void setTagLevel(String tagLevel) {
        this.tagLevel = tagLevel;
    }

    public String getTagSource() {
        return tagSource;
    }

    public void setTagSource(String tagSource) {
        this.tagSource = tagSource;
    }

    public String getSourceTable() {
        return sourceTable;
    }

    public void setSourceTable(String sourceTable) {
        this.sourceTable = sourceTable;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }
}
