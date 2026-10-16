package com.jiancha.biz.archive.domain;

import java.util.Date;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 利益冲突申报变更留痕 jc_conflict_log（段15）
 *
 * <p>申报单的新增/修改/核实/删除均留痕，满足纪检审计"谁在何时改过什么"的追溯要求。</p>
 *
 * @author 小标
 */
public class JcConflictLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long logId;
    /** 申报单ID */
    private Long declareId;
    /** 动作（add/edit/verify/delete） */
    private String action;
    /** 变更内容摘要 */
    private String detail;
    /** 操作人 */
    private String operator;
    /** 操作时间 */
    private Date operateTime;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public Long getDeclareId() { return declareId; }
    public void setDeclareId(Long declareId) { this.declareId = declareId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public Date getOperateTime() { return operateTime; }
    public void setOperateTime(Date operateTime) { this.operateTime = operateTime; }
}
