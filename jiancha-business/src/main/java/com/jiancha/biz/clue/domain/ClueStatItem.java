package com.jiancha.biz.clue.domain;

/**
 * 线索统计分组项（分组键 + 计数）
 *
 * @author 小标
 */
public class ClueStatItem {

    /** 分组键（字典值） */
    private String name;

    /** 计数 */
    private Long count;

    public ClueStatItem() {
    }

    public ClueStatItem(String name, Long count) {
        this.name = name;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
