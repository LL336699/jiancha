package com.jiancha.biz.archive.domain;

import java.util.Date;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 年度集中申报截止配置 jc_declare_config（段15）
 *
 * <p>纪检年度集中申报（如每年 3 月 31 日截止）的截止日与开关配置，
 * 看板据此计算临期/超期天数并变色督办。</p>
 *
 * @author 小标
 */
public class JcDeclareConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long configId;
    /** 申报年度 */
    private Integer declareYear;
    /** 集中申报截止日 */
    private Date deadline;
    /** 是否启用集中申报（1启用/0关闭） */
    private String enabled;
    /** 备注 */
    private String remark;

    public Long getConfigId() { return configId; }
    public void setConfigId(Long configId) { this.configId = configId; }
    public Integer getDeclareYear() { return declareYear; }
    public void setDeclareYear(Integer declareYear) { this.declareYear = declareYear; }
    public Date getDeadline() { return deadline; }
    public void setDeadline(Date deadline) { this.deadline = deadline; }
    public String getEnabled() { return enabled; }
    public void setEnabled(String enabled) { this.enabled = enabled; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
