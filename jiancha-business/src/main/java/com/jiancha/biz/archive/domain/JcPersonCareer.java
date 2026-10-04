package com.jiancha.biz.archive.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 任职经历 jc_person_career
 *
 * @author 小标
 */
public class JcPersonCareer extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long careerId;
    private Long personId;
    private String position;
    private String deptName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    private String dutyDesc;

    public Long getCareerId() { return careerId; }
    public void setCareerId(Long careerId) { this.careerId = careerId; }
    public Long getPersonId() { return personId; }
    public void setPersonId(Long personId) { this.personId = personId; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public String getDutyDesc() { return dutyDesc; }
    public void setDutyDesc(String dutyDesc) { this.dutyDesc = dutyDesc; }
}
