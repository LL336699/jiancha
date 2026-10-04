package com.jiancha.biz.archive.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 干部廉政档案主表 jc_person
 *
 * <p>一人一行，关联信息拆子表（任职/亲属/处分/标签），避免大宽表。</p>
 *
 * @author 小标
 */
public class JcPerson extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 人员ID */
    private Long personId;

    /** 人员编号 */
    private String personCode;

    /** 姓名 */
    @Excel(name = "姓名")
    private String name;

    /** 身份证号（加密存储，列表页脱敏） */
    @JsonFormat
    @Excel(name = "身份证号")
    private String idCardEnc;

    /** 性别 */
    private String gender;

    /** 出生日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "出生日期")
    private Date birthDate;

    /** 所属部门ID */
    private Long deptId;

    /** 部门路径（数据权限过滤用） */
    private String deptPath;

    /** 职务 */
    @Excel(name = "职务")
    private String position;

    /** 政治面貌 */
    private String politicalStatus;

    /** 入职日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "入职日期")
    private Date entryDate;

    /** 在职状态（在岗/退休/离职/调出） */
    @Excel(name = "在职状态")
    private String status;

    /** 风险等级（高/中/低/正常） */
    @Excel(name = "风险等级")
    private String riskLevel;

    /** 廉政评分 */
    private Integer integrityScore;

    /** 照片路径 */
    private String avatar;

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public String getPersonCode() {
        return personCode;
    }

    public void setPersonCode(String personCode) {
        this.personCode = personCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdCardEnc() {
        return idCardEnc;
    }

    public void setIdCardEnc(String idCardEnc) {
        this.idCardEnc = idCardEnc;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getDeptPath() {
        return deptPath;
    }

    public void setDeptPath(String deptPath) {
        this.deptPath = deptPath;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getPoliticalStatus() {
        return politicalStatus;
    }

    public void setPoliticalStatus(String politicalStatus) {
        this.politicalStatus = politicalStatus;
    }

    public Date getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(Date entryDate) {
        this.entryDate = entryDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Integer getIntegrityScore() {
        return integrityScore;
    }

    public void setIntegrityScore(Integer integrityScore) {
        this.integrityScore = integrityScore;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
