package com.jiancha.biz.report.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * 文书生成记录（M6 文书辅助）
 *
 * <p>每次生成均落库留痕：谁（operator）、何时（createTime）、基于哪个模板、
 * 输入了什么要素、产出了什么草稿。满足设计要点"量纪建议仅作提示，
 * 最终定性必须人工签字——流程上强制留痕"。</p>
 *
 * @author 小标
 */
@Data
public class JcReportRecord extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long recordId;

    /** 模板ID */
    private Long templateId;

    /** 文书类型 */
    private String docType;

    /** 生成的文书标题 */
    private String title;

    /** 用户输入的案情要素（JSON） */
    private String inputJson;

    /** 生成的文书草稿 */
    private String generatedContent;

    /** 结构化量纪建议（含依据条款与免责说明） */
    private String penaltyAdvice;

    /** 操作人（水印溯源） */
    private String operator;

    /** 操作人部门（水印溯源） */
    private String deptName;
}
