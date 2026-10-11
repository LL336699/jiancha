package com.jiancha.biz.report.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * 文书模板（M6 文书辅助）
 *
 * <p>模板骨架以 {@code ${变量名}} 形式占位，生成时由用户填写的案情要素替换；
 * 大模型仅负责"基于已检索到的法条 + 填充后的事实"生成文书草稿，不凭空编造。</p>
 *
 * @author 小标
 */
@Data
public class JcReportTemplate extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 模板ID */
    private Long templateId;

    /** 文书类型：立案报告 / 笔录模板 / 调查终结报告 */
    private String docType;

    /** 模板名称 */
    private String templateName;

    /** 模板骨架（含 ${变量} 占位，生成时替换为案情要素） */
    private String templateContent;

    /** 表单字段定义（JSON 数组：[{key,label,type,required}]），前端据此渲染生成表单 */
    private String fieldsMeta;

    /** 状态：启用 / 停用（字典 jc_report_template_status） */
    private String status;

    /** 备注 */
    private String remark;
}
