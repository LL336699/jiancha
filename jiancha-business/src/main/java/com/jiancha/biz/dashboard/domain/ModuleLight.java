package com.jiancha.biz.dashboard.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 模块红黄绿灯（段12 纪检综合看板）
 *
 * <p>灯色规则：红灯=有超期事项（供应商模块为存在黑名单/禁入）；
 * 黄灯=有在办事项；绿灯=全部办结或暂无数据。</p>
 *
 * @author 小标
 */
@Data
public class ModuleLight implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 模块标识 */
    private String moduleKey;

    /** 模块中文名 */
    private String moduleName;

    /** 灯色：red / yellow / green */
    private String light;

    /** 超期数量 */
    private Long overdue;

    /** 在办数量 */
    private Long processing;

    /** 备注（如红灯原因） */
    private String note;
}
