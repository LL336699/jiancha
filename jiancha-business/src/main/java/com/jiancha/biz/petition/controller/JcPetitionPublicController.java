package com.jiancha.biz.petition.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import org.apache.commons.io.FilenameUtils;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;

import com.jiancha.biz.petition.domain.JcPetition;
import com.jiancha.biz.petition.domain.PetitionSubmitRequest;
import com.jiancha.biz.petition.service.IJcPetitionService;

/**
 * 舞弊举报箱 Controller（免登录）
 *
 * <p>设计文档 §M3：举报入口独立、免登录、支持匿名与实名；本 Controller 全类标注
 * {@code @Anonymous}，由框架自动注册为匿名可访问路径，无需改安全配置。</p>
 *
 * <p>匿名保护：客户端 IP 仅作限流键，不落库、不写日志正文；管理端反代层另需关闭
 * 举报路径的访问日志（部署层要求，见设计文档安全设计）。</p>
 *
 * @author 小标
 */
@Anonymous
@RestController
@RequestMapping("/public/petition")
public class JcPetitionPublicController {

    /** 补充材料允许的扩展名（白名单，拒 exe/jsp/sh 等危险类型） */
    private static final Set<String> ALLOWED_EXT = new HashSet<>(Arrays.asList(
            "pdf", "doc", "docx", "txt", "md", "rtf", "png", "jpg", "jpeg", "zip"));

    /** 材料大小上限（10MB） */
    private static final long MAX_MATERIAL_SIZE = 10L * 1024 * 1024;

    @Autowired
    private IJcPetitionService petitionService;

    /**
     * 提交举报。
     *
     * @param req      举报要素
     * @param request  取客户端 IP 作限流键（不落库）
     * @return 回执码（仅此一次返回，请举报人妥善保存）
     */
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody PetitionSubmitRequest req, HttpServletRequest request) {
        JcPetition petition = petitionService.submit(req, clientIp(request));
        AjaxResult ajax = AjaxResult.success("提交成功，请保存回执码");
        ajax.put("petitionId", petition.getPetitionId());
        ajax.put("receiptCode", petition.getReceiptCode());
        return ajax;
    }

    /**
     * 凭回执码查询办理进度（举报人视角）。
     */
    @GetMapping("/receipt/{receiptCode}")
    public AjaxResult queryByReceipt(@PathVariable String receiptCode) {
        JcPetition petition = petitionService.queryByReceipt(receiptCode);
        return AjaxResult.success(petition);
    }

    /**
     * 举报人凭回执码补充材料。
     */
    @PostMapping("/material")
    public AjaxResult appendMaterial(@RequestParam String receiptCode,
                                     @RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return AjaxResult.error("上传文件不能为空");
        }
        if (file.getSize() > MAX_MATERIAL_SIZE) {
            return AjaxResult.error("文件大小超过 10MB 限制");
        }
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        if (StringUtils.isEmpty(ext) || !ALLOWED_EXT.contains(ext.toLowerCase())) {
            return AjaxResult.error("不支持的文件类型，仅允许 " + String.join("/", ALLOWED_EXT));
        }
        // 先落盘再入库：FileUploadUtils 内部还会按白名单二次断言
        String filePath;
        try {
            filePath = FileUploadUtils.upload(RuoYiConfig.getUploadPath(), file, ALLOWED_EXT.toArray(new String[0]));
        } catch (Exception e) {
            return AjaxResult.error("文件保存失败：" + e.getMessage());
        }
        // Service 内完成回执码校验 + 办结状态校验 + 落库
        int rows = petitionService.appendMaterialByReceipt(receiptCode, file.getOriginalFilename(), filePath, file.getSize());
        if (rows <= 0) {
            return AjaxResult.error("材料保存失败");
        }
        return AjaxResult.success("材料上传成功");
    }

    /**
     * 客户端 IP：优先取反代透传的 X-Forwarded-For 首段，回退 remoteAddr。
     * 仅作限流键使用，不落库、不暴露。
     */
    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotEmpty(forwarded)) {
            String first = forwarded.split(",")[0].trim();
            if (StringUtils.isNotEmpty(first)) {
                return first;
            }
        }
        return request.getRemoteAddr();
    }
}
