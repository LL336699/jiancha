package com.jiancha.biz.reply.domain;

import java.io.Serializable;

/**
 * 廉政意见回复·出具结论请求（E2）
 *
 * @author 小标
 */
public class ReplyActionRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 正式结论（无违纪/暂缓/否决） */
    private String finalResult;

    /** 回复内容（正式意见正文，必填） */
    private String replyContent;

    /** 处理意见（核实过程说明） */
    private String opinion;

    public String getFinalResult() {
        return finalResult;
    }

    public void setFinalResult(String finalResult) {
        this.finalResult = finalResult;
    }

    public String getReplyContent() {
        return replyContent;
    }

    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }
}
