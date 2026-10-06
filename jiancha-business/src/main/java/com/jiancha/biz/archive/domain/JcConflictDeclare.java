package com.jiancha.biz.archive.domain;

import java.util.Date;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 利益冲突申报单 jc_conflict_declare（企业版新增 E1）
 *
 * <p>支撑年度集中申报、发生事项即时申报与专项治理申报，
 * 明细见 {@link JcConflictItem}。核实结论用于与关联关系库比对，
 * 是风险引擎规则 8（申报矛盾/漏报）的数据来源。</p>
 *
 * @author 小标
 */
public class JcConflictDeclare extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long declareId;
    private Long personId;
    /** 申报类型（字典 jc_declare_type：年度/事项/专项） */
    private String declareType;
    /** 申报年度 */
    private Integer declareYear;
    /** 申报日期 */
    private Date declareDate;
    /** 状态（字典 jc_declare_status：草稿/待核实/已核实/存疑） */
    private String status;
    /** 核实人 */
    private String verifyBy;
    /** 核实时间 */
    private Date verifyTime;
    /** 核实结论 */
    private String verifyResult;

    public Long getDeclareId() { return declareId; }
    public void setDeclareId(Long declareId) { this.declareId = declareId; }
    public Long getPersonId() { return personId; }
    public void setPersonId(Long personId) { this.personId = personId; }
    public String getDeclareType() { return declareType; }
    public void setDeclareType(String declareType) { this.declareType = declareType; }
    public Integer getDeclareYear() { return declareYear; }
    public void setDeclareYear(Integer declareYear) { this.declareYear = declareYear; }
    public Date getDeclareDate() { return declareDate; }
    public void setDeclareDate(Date declareDate) { this.declareDate = declareDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getVerifyBy() { return verifyBy; }
    public void setVerifyBy(String verifyBy) { this.verifyBy = verifyBy; }
    public Date getVerifyTime() { return verifyTime; }
    public void setVerifyTime(Date verifyTime) { this.verifyTime = verifyTime; }
    public String getVerifyResult() { return verifyResult; }
    public void setVerifyResult(String verifyResult) { this.verifyResult = verifyResult; }
}
