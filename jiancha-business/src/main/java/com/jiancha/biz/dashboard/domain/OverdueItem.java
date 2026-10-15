package com.jiancha.biz.dashboard.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 超期事项明细（段12 纪检综合看板 · 跨模块预警清单）
 *
 * <p>汇总各业务模块未办结且已超过期限的事项，供纪委督办盯办使用。</p>
 *
 * @author 小标
 */
@Data
public class OverdueItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 所属模块标识 */
    private String moduleKey;

    /** 所属模块名称 */
    private String moduleName;

    /** 业务单号（模块内主键） */
    private Long bizId;

    /** 事项标题/概要 */
    private String title;

    /** 责任部门 */
    private String ownerDept;

    /** 责任人 */
    private String ownerName;

    /** 期限日 */
    private Date deadline;

    /** 超期天数（负数=已超期） */
    private Long overdueDays;
}
