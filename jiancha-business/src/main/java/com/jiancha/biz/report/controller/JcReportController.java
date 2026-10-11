package com.jiancha.biz.report.controller;

import com.jiancha.biz.report.domain.JcReportRecord;
import com.jiancha.biz.report.domain.JcReportTemplate;
import com.jiancha.biz.report.service.IJcReportService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 文书辅助 Controller（M6 文书辅助）
 *
 * <p>接口前缀 {@code /jiancha/report}，权限前缀 {@code jiancha:report:*}。</p>
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/report")
public class JcReportController extends BaseController {

    @Autowired
    private IJcReportService reportService;

    /** 模板列表 */
    @PreAuthorize("@ss.hasPermi('jiancha:report:template:list')")
    @GetMapping("/template/list")
    public TableDataInfo templateList(JcReportTemplate template) {
        startPage();
        List<JcReportTemplate> list = reportService.listTemplates(template);
        return getDataTable(list);
    }

    /** 模板详情 */
    @PreAuthorize("@ss.hasPermi('jiancha:report:template:query')")
    @GetMapping("/template/{templateId}")
    public AjaxResult getTemplate(@PathVariable Long templateId) {
        return success(reportService.getTemplate(templateId));
    }

    /** 生成文书草稿 + 量纪建议 */
    @PreAuthorize("@ss.hasPermi('jiancha:report:generate')")
    @PostMapping("/generate")
    public AjaxResult generate(@RequestBody Map<String, Object> body) {
        Long templateId = parseLong(body.get("templateId"));
        if (templateId == null) {
            return error("templateId 不能为空");
        }
        @SuppressWarnings("unchecked")
        Map<String, String> params = (Map<String, String>) body.get("params");
        Map<String, Object> result = reportService.generate(templateId, params);
        return success(result);
    }

    /** 生成记录列表 */
    @PreAuthorize("@ss.hasPermi('jiancha:report:record:list')")
    @GetMapping("/record/list")
    public TableDataInfo recordList(JcReportRecord record) {
        startPage();
        List<JcReportRecord> list = reportService.listRecords(record);
        return getDataTable(list);
    }

    /** 生成记录详情 */
    @PreAuthorize("@ss.hasPermi('jiancha:report:record:query')")
    @GetMapping("/record/{recordId}")
    public AjaxResult getRecord(@PathVariable Long recordId) {
        return success(reportService.getRecord(recordId));
    }

    @PreAuthorize("@ss.hasPermi('jiancha:report:record:remove')")
    @DeleteMapping("/record/{recordId}")
    public AjaxResult removeRecord(@PathVariable Long recordId) {
        return toAjax(reportService.removeRecord(recordId));
    }

    private Long parseLong(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Number) {
            return ((Number) o).longValue();
        }
        try {
            return Long.parseLong(o.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
