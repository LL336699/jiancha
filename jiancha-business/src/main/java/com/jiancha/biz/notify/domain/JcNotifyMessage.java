package com.jiancha.biz.notify.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 站内消息通知 jc_notify_message（段17 消息通知中心）
 *
 * <p>全局统一的用户级消息通道：业务事件（新举报/催办/整改临期/考试发布/风控预警等）
 * 由各模块通过 {@link com.jiancha.biz.notify.service.INotifyService} 触达指定用户，
 * 定时聚合扫描（{@link com.jiancha.biz.notify.task.JcNotifyScanTask}）按日兜底提醒。
 * 消息属于个人数据：接收人只能读/删自己的消息；顶栏铃铛轮询未读数，消息中心页可筛选。</p>
 *
 * <p>去重约定：同接收人 + 同标题当日只落一条（定时任务每日重复扫描不刷屏）。</p>
 *
 * @author 小标
 */
public class JcNotifyMessage extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 消息ID */
    private Long notifyId;

    /** 消息标题 */
    @Excel(name = "标题")
    private String title;

    /** 消息内容 */
    @Excel(name = "内容")
    private String content;

    /** 消息类型（字典 jc_notify_type：举报/整改/教育/风控/申报/系统） */
    @Excel(name = "类型")
    private String notifyType;

    /** 业务模块（petition/rectify/edu/risk/conflict/clue/supplier） */
    private String bizModule;

    /** 业务主键（前端跳转定位用） */
    private Long bizId;

    /** 接收人用户名 */
    @Excel(name = "接收人")
    private String receiver;

    /** 接收人昵称（冗余便于展示） */
    private String receiverName;

    /** 是否已读（0未读 1已读） */
    @Excel(name = "已读", readConverterExp = "0=未读,1=已读")
    private String isRead;

    /** 已读时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date readTime;

    /** 创建时间（触发时间） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 查询辅助：是否已读筛选（0未读 1已读，空为全部） */
    private String readFlagQuery;

    public Long getNotifyId() {
        return notifyId;
    }

    public void setNotifyId(Long notifyId) {
        this.notifyId = notifyId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getNotifyType() {
        return notifyType;
    }

    public void setNotifyType(String notifyType) {
        this.notifyType = notifyType;
    }

    public String getBizModule() {
        return bizModule;
    }

    public void setBizModule(String bizModule) {
        this.bizModule = bizModule;
    }

    public Long getBizId() {
        return bizId;
    }

    public void setBizId(Long bizId) {
        this.bizId = bizId;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getIsRead() {
        return isRead;
    }

    public void setIsRead(String isRead) {
        this.isRead = isRead;
    }

    public Date getReadTime() {
        return readTime;
    }

    public void setReadTime(Date readTime) {
        this.readTime = readTime;
    }

    @Override
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getCreateTime() {
        return createTime;
    }

    @Override
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getReadFlagQuery() {
        return readFlagQuery;
    }

    public void setReadFlagQuery(String readFlagQuery) {
        this.readFlagQuery = readFlagQuery;
    }
}
