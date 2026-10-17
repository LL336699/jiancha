package com.jiancha.biz.notify.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jiancha.biz.notify.domain.JcNotifyMessage;
import com.jiancha.biz.notify.service.INotifyService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;

/**
 * 站内消息通知 接口层（段17 消息通知中心）
 *
 * <p>铃铛与消息中心均只操作「本人」消息（receiver = 当前登录用户），
 * 未读数/标记已读登录即可用（全员基础能力），列表与删除挂权限点便于审计。</p>
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/notify")
public class JcNotifyController extends BaseController {

    @Autowired
    private INotifyService notifyService;

    /**
     * 本人消息列表（消息中心页，支持类型/已读筛选）
     */
    @PreAuthorize("@ss.hasPermi('jiancha:notify:list')")
    @GetMapping("/list")
    public TableDataInfo list(JcNotifyMessage query) {
        query.setReceiver(SecurityUtils.getUsername());
        startPage();
        List<JcNotifyMessage> list = notifyService.selectMyList(query);
        return getDataTable(list);
    }

    /**
     * 未读数（顶栏铃铛轮询，30s 间隔）
     */
    @GetMapping("/unreadCount")
    public AjaxResult unreadCount() {
        return AjaxResult.success(notifyService.countUnread(SecurityUtils.getUsername()));
    }

    /**
     * 未读数按类型分布（消息中心统计条）
     */
    @GetMapping("/unreadStat")
    public AjaxResult unreadStat() {
        return AjaxResult.success(notifyService.statUnreadByType(SecurityUtils.getUsername()));
    }

    /**
     * 最近未读预览（铃铛下拉，最多 10 条）
     */
    @GetMapping("/recent")
    public AjaxResult recent() {
        return AjaxResult.success(notifyService.selectRecentUnread(SecurityUtils.getUsername(), 10));
    }

    /**
     * 标记单条已读
     */
    @PreAuthorize("@ss.hasPermi('jiancha:notify:read')")
    @PutMapping("/read/{notifyId}")
    public AjaxResult markRead(@PathVariable Long notifyId) {
        return toAjax(notifyService.markRead(notifyId, SecurityUtils.getUsername()));
    }

    /**
     * 全部已读
     */
    @PreAuthorize("@ss.hasPermi('jiancha:notify:read')")
    @PutMapping("/readAll")
    public AjaxResult markAllRead() {
        return toAjax(notifyService.markAllRead(SecurityUtils.getUsername()));
    }

    /**
     * 删除本人单条消息
     */
    @PreAuthorize("@ss.hasPermi('jiancha:notify:remove')")
    @DeleteMapping("/{notifyId}")
    public AjaxResult remove(@PathVariable Long notifyId) {
        return toAjax(notifyService.remove(notifyId, SecurityUtils.getUsername()));
    }

    /**
     * 清空本人已读消息
     */
    @PreAuthorize("@ss.hasPermi('jiancha:notify:remove')")
    @DeleteMapping("/cleanRead")
    public AjaxResult cleanRead() {
        return toAjax(notifyService.cleanRead(SecurityUtils.getUsername()));
    }

    /**
     * 导出本人消息清单（当前筛选条件，xlsx）
     */
    @PreAuthorize("@ss.hasPermi('jiancha:notify:list')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, JcNotifyMessage query) {
        query.setReceiver(SecurityUtils.getUsername());
        List<JcNotifyMessage> list = notifyService.selectMyList(query);
        ExcelUtil<JcNotifyMessage> util = new ExcelUtil<>(JcNotifyMessage.class);
        util.exportExcel(response, list, "消息通知");
    }
}
