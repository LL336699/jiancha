package com.jiancha.biz.rectify.domain;

/**
 * 整改流转请求
 *
 * <p>动作：start（开始整改）/ submit（提交复核）/ pass（销号）/ reopen（复核不通过退回）/
 * revisit（回头看不通过重开）/ void（作废）。</p>
 *
 * @author 小标
 */
public class RectifyActionRequest {

    /** 动作类型 */
    private String action;

    /** 办理意见（写入复核记录/留痕） */
    private String opinion;

    /** 复核结论（revisit 时使用：0不通过 1通过；不传按不通过处理） */
    private String result;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
