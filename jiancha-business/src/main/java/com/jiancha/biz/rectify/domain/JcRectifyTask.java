package com.jiancha.biz.rectify.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 整改措施 jc_rectify_task
 *
 * <p>一个问题可拆多条整改措施，逐条登记计划与完成情况；佐证材料走通用上传。
 * 提交复核时要求至少一条措施已完成（防止「零措施直接销号」）。</p>
 *
 * @author 小标
 */
public class JcRectifyTask extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 措施ID */
    private Long taskId;

    /** 问题ID */
    private Long issueId;

    /** 整改措施内容 */
    private String measure;

    /** 责任人姓名 */
    private String ownerName;

    /** 计划完成日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date planDate;

    /** 实际完成日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date doneDate;

    /** 佐证材料路径 */
    private String evidencePath;

    /** 状态（0未开始 1进行中 2已完成） */
    private String status;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public Date getPlanDate() {
        return planDate;
    }

    public void setPlanDate(Date planDate) {
        this.planDate = planDate;
    }

    public Date getDoneDate() {
        return doneDate;
    }

    public void setDoneDate(Date doneDate) {
        this.doneDate = doneDate;
    }

    public String getEvidencePath() {
        return evidencePath;
    }

    public void setEvidencePath(String evidencePath) {
        this.evidencePath = evidencePath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
