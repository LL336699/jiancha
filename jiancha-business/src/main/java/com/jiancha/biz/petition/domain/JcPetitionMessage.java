package com.jiancha.biz.petition.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 舞弊举报双向留言 jc_petition_message（段16）
 *
 * <p>举报人与受理人的双向沟通通道：举报人凭回执码发言（sender=reporter，不落真名，
 * 显示名固定「举报人」），受理人在管理端回复（sender=staff，显示名为用户昵称）。
 * 匿名保护：不留 IP；双方均已读计数，用于列表未读提示。</p>
 *
 * <p>与轨迹的边界：留言是沟通内容（双方可见），轨迹是内部流转记录（仅管理端可见，
 * 举报人凭回执码查询进度时只暴露状态与时间）。</p>
 *
 * @author 小标
 */
public class JcPetitionMessage extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 留言ID */
    private Long messageId;

    /** 举报ID */
    private Long petitionId;

    /** 回执码检索哈希（举报人侧凭回执码访问时的归属校验） */
    private String receiptLookup;

    /** 发言方（reporter 举报人 / staff 受理人） */
    private String sender;

    /** 发言方显示名（staff 为用户昵称；举报人固定「举报人」，不落真名） */
    private String senderName;

    /** 留言内容 */
    private String content;

    /** 对方已读标记（0未读 1已读） */
    private String readFlag;

    /** 发言时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 事由标题（联查展示，不落库） */
    private String petitionTitle;

    /** 办理状态（联查展示，不落库） */
    private String petitionStatus;

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Long getPetitionId() {
        return petitionId;
    }

    public void setPetitionId(Long petitionId) {
        this.petitionId = petitionId;
    }

    public String getReceiptLookup() {
        return receiptLookup;
    }

    public void setReceiptLookup(String receiptLookup) {
        this.receiptLookup = receiptLookup;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getReadFlag() {
        return readFlag;
    }

    public void setReadFlag(String readFlag) {
        this.readFlag = readFlag;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getPetitionTitle() {
        return petitionTitle;
    }

    public void setPetitionTitle(String petitionTitle) {
        this.petitionTitle = petitionTitle;
    }

    public String getPetitionStatus() {
        return petitionStatus;
    }

    public void setPetitionStatus(String petitionStatus) {
        this.petitionStatus = petitionStatus;
    }
}
