package com.jiancha.biz.knowledge.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.jiancha.biz.knowledge.domain.JcKbDocument;
import com.jiancha.biz.knowledge.service.IJcKbDocumentService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

/**
 * 知识库文档 Controller（M1 + M2）
 *
 * <p>权限前缀 jiancha:kb:doc:*。上传即触发"解析 → 分块 → 向量化"全链路，
 * 解析与向量化耗时较长，接口为同步返回（单文档，一般 1-30 秒）。</p>
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/kb/doc")
public class JcKbDocumentController extends BaseController {

    @Autowired
    private IJcKbDocumentService documentService;

    /** 文档列表（分页） */
    @PreAuthorize("@ss.hasPermi('jiancha:kb:doc:list')")
    @GetMapping("/list")
    public TableDataInfo list(JcKbDocument document) {
        startPage();
        List<JcKbDocument> list = documentService.selectJcKbDocumentList(document);
        return getDataTable(list);
    }

    /** 知识库概览统计（文档数 / 分块数 / 失效数 / 解析失败数） */
    @PreAuthorize("@ss.hasPermi('jiancha:kb:doc:list')")
    @GetMapping("/overview")
    public AjaxResult overview() {
        return success(documentService.statOverview());
    }

    /** 文档详情 */
    @PreAuthorize("@ss.hasPermi('jiancha:kb:doc:query')")
    @GetMapping(value = "/{docId}")
    public AjaxResult getInfo(@PathVariable Long docId) {
        return success(documentService.selectJcKbDocumentById(docId));
    }

    /** 查看文档分块（核对分块质量与出处） */
    @PreAuthorize("@ss.hasPermi('jiancha:kb:doc:query')")
    @GetMapping(value = "/{docId}/chunks")
    public AjaxResult chunks(@PathVariable Long docId) {
        return success(documentService.selectChunksByDocId(docId));
    }

    /** 上传并入库（解析 + 分块 + 向量化） */
    @PreAuthorize("@ss.hasPermi('jiancha:kb:doc:upload')")
    @Log(title = "知识库文档", businessType = BusinessType.INSERT)
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file,
                             @RequestParam(value = "categoryId", required = false) Long categoryId,
                             JcKbDocument doc) {
        Map<String, Object> result = documentService.uploadAndIngest(file, categoryId, doc, getUsername());
        return AjaxResult.success(result);
    }

    /** 修改文档元数据 */
    @PreAuthorize("@ss.hasPermi('jiancha:kb:doc:edit')")
    @Log(title = "知识库文档", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody JcKbDocument document) {
        document.setUpdateBy(getUsername());
        return toAjax(documentService.updateJcKbDocument(document));
    }

    /** 重新解析入库（解析失败重试 / 重建向量） */
    @PreAuthorize("@ss.hasPermi('jiancha:kb:doc:reingest')")
    @Log(title = "知识库文档", businessType = BusinessType.UPDATE)
    @PostMapping("/reingest/{docId}")
    public AjaxResult reingest(@PathVariable Long docId) {
        return AjaxResult.success(documentService.reingest(docId, getUsername()));
    }

    /** 删除文档（联动删分块与向量） */
    @PreAuthorize("@ss.hasPermi('jiancha:kb:doc:remove')")
    @Log(title = "知识库文档", businessType = BusinessType.DELETE)
    @DeleteMapping("/{docId}")
    public AjaxResult remove(@PathVariable Long docId) {
        return toAjax(documentService.deleteJcKbDocumentById(docId));
    }

    /** 批量删除文档 */
    @PreAuthorize("@ss.hasPermi('jiancha:kb:doc:remove')")
    @Log(title = "知识库文档", businessType = BusinessType.DELETE)
    @DeleteMapping("/batch/{docIds}")
    public AjaxResult removeBatch(@PathVariable Long[] docIds) {
        return toAjax(documentService.deleteJcKbDocumentByIds(docIds));
    }
}
