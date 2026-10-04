package com.jiancha.biz.archive.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 处分记录 jc_person_punish
 *
 * @author 小标
 */
public class JcPersonPunish extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long punishId;
    private Long personId;
    /** 处分种类 */
    private String punishType;
    /** 处分事由 */
    private String punishReason;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date punishDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date effectiveDate;

    /** 关联案件编号 */
    private String relatedCaseNo;

    public Long getPunishId() { return punishId; }
    public void setPunishId(Long punishId) { this.punishId = punishId; }
    public Long getPersonId() { return personId; }
    public void setPersonId(Long personId) { this.personId = personId; }
    public String getPunishType() { return punishType; }
    public void setPunishType(String punishType) { this.punishType = punishType; }
    public String getPunishReason() { return punishReason; }
    public void setPunishReason(String punishReason) { this.punishReason = punishReason; }
    public Date getPunishDate() { return punishDate; }
    public void setPunishDate(Date punishDate) { this.punishDate = punishDate; }
    public Date getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(Date effectiveDate) { this.effectiveDate = effectiveDate; }
    public String getRelatedCaseNo() { return relatedCaseNo; }
    public void setRelatedCaseNo(String relatedCaseNo) { this.relatedCaseNo = relatedCaseNo; }
}
