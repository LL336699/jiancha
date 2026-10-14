package com.jiancha.biz.supplier.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 廉洁协议续签请求 DTO
 *
 * @author 小标
 */
public class AgreementRenewRequest {

    /** 新到期日（必填，须晚于当前日期） */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date newExpireDate;

    /** 续签说明 */
    private String opinion;

    public Date getNewExpireDate() {
        return newExpireDate;
    }

    public void setNewExpireDate(Date newExpireDate) {
        this.newExpireDate = newExpireDate;
    }

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }
}
