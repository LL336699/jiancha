package com.jiancha.biz.petition.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

import jakarta.servlet.http.HttpServletResponse;

import com.jiancha.biz.petition.crypto.JcFieldCrypto;
import com.jiancha.biz.petition.domain.JcPetition;
import com.jiancha.biz.petition.domain.JcPetitionMaterial;
import com.jiancha.biz.petition.domain.JcPetitionMessage;
import com.jiancha.biz.petition.domain.JcPetitionTrace;
import com.jiancha.biz.petition.domain.PetitionActionRequest;
import com.jiancha.biz.petition.domain.PetitionStat;
import com.jiancha.biz.petition.service.IJcPetitionMaterialService;
import com.jiancha.biz.petition.service.IJcPetitionMessageService;
import com.jiancha.biz.petition.service.IJcPetitionService;
import com.jiancha.biz.petition.service.IJcPetitionTraceService;

import java.util.HashMap;

/**
 * 舞弊举报管理端 Controller（M3）
 *
 * <p>权限前缀 jiancha:petition:*。列表不返回举报内容与敏感字段；详情按权限解密展示
 * （AES-GCM），查看动作全程审计留痕。</p>
 *
 * @author 小标
 */
@RestController
@RequestMapping("/jiancha/petition")
public class JcPetitionController extends BaseController {

    @Autowired
    private IJcPetitionService petitionService;

    @Autowired
    private IJcPetitionTraceService traceService;

    @Autowired
    private IJcPetitionMaterialService materialService;

    @Autowired
    private IJcPetitionMessageService messageService;

    @Autowired
    private JcFieldCrypto fieldCrypto;

    /** 举报列表（分页，不含内容与敏感字段） */
    @PreAuthorize("@ss.hasPermi('jiancha:petition:list')")
    @GetMapping("/list")
    public TableDataInfo list(JcPetition jcPetition) {
        startPage();
        List<JcPetition> list = petitionService.selectJcPetitionList(jcPetition);
        return getDataTable(list);
    }

    /** 举报详情（敏感字段解密，审计留痕） */
    @PreAuthorize("@ss.hasPermi('jiancha:petition:query')")
    @Log(title = "舞弊举报", businessType = BusinessType.OTHER)
    @GetMapping(value = "/{petitionId}")
    public AjaxResult getInfo(@PathVariable Long petitionId) {
        JcPetition petition = petitionService.selectJcPetitionByPetitionId(petitionId);
        if (petition == null) {
            return error("举报不存在或已删除");
        }
        // 解密敏感字段（密文直接暴露无意义；解密失败时按原值展示，兼容历史明文）
        petition.setReporterName(fieldCrypto.decrypt(petition.getReporterName()));
        petition.setReporterPhone(fieldCrypto.decrypt(petition.getReporterPhone()));
        petition.setReporterIdcard(fieldCrypto.decrypt(petition.getReporterIdcard()));
        return success(petition);
    }

    /** 办理轨迹 */
    @PreAuthorize("@ss.hasPermi('jiancha:petition:query')")
    @GetMapping("/trace/{petitionId}")
    public AjaxResult trace(@PathVariable Long petitionId) {
        List<JcPetitionTrace> list = traceService.selectTraceByPetitionId(petitionId);
        return success(list);
    }

    /** 补充材料列表 */
    @PreAuthorize("@ss.hasPermi('jiancha:petition:query')")
    @GetMapping("/material/{petitionId}")
    public AjaxResult material(@PathVariable Long petitionId) {
        List<JcPetitionMaterial> list = materialService.selectMaterialByPetitionId(petitionId);
        return success(list);
    }

    /** 办理流转（状态机） */
    @PreAuthorize("@ss.hasPermi('jiancha:petition:action')")
    @Log(title = "舞弊举报", businessType = BusinessType.UPDATE)
    @PostMapping("/action")
    public AjaxResult action(@Validated @RequestBody PetitionActionRequest req,
                             @RequestParam Long petitionId) {
        JcPetition petition = petitionService.handleAction(petitionId, req);
        return success(petition);
    }

    /** 删除举报（级联轨迹与材料；正式数据建议只归档不删除） */
    @PreAuthorize("@ss.hasPermi('jiancha:petition:remove')")
    @Log(title = "舞弊举报", businessType = BusinessType.DELETE)
    @DeleteMapping("/{petitionId}")
    public AjaxResult remove(@PathVariable Long petitionId) {
        return toAjax(petitionService.deleteJcPetitionByPetitionId(petitionId));
    }

    /** 同一被举报对象的历史举报（重复提示，不自动合并） */
    @PreAuthorize("@ss.hasPermi('jiancha:petition:query')")
    @GetMapping("/duplicate")
    public AjaxResult duplicate(@RequestParam String targetName) {
        List<JcPetition> list = petitionService.detectDuplicate(targetName);
        return success(list);
    }

    /** 办理统计 */
    @PreAuthorize("@ss.hasPermi('jiancha:petition:stat')")
    @GetMapping("/stat")
    public AjaxResult stat() {
        PetitionStat stat = petitionService.stat();
        return success(stat);
    }

    /** 脱敏演示（供前端联调：不解密也能看到掩码效果） */
    @PreAuthorize("@ss.hasPermi('jiancha:petition:query')")
    @GetMapping("/mask")
    public AjaxResult mask(@RequestParam String plain) {
        return success(Map.of("masked", JcFieldCrypto.mask(plain)));
    }

    // ═══════════════════════ 段16：留言 / 催办 / 督办 ═══════════════════════

    /** 举报留言列表（双方往来；打开即把举报人留言置为已读） */
    @PreAuthorize("@ss.hasPermi('jiancha:petition:query')")
    @GetMapping("/message/{petitionId}")
    public AjaxResult messages(@PathVariable Long petitionId) {
        java.util.List<JcPetitionMessage> list = messageService.listByPetitionId(petitionId);
        return success(list);
    }

    /** 受理人回复举报留言（写轨迹留痕；办结举报不可回复） */
    @PreAuthorize("@ss.hasPermi('jiancha:petition:message')")
    @Log(title = "舞弊举报留言", businessType = BusinessType.INSERT)
    @PostMapping("/message/{petitionId}")
    public AjaxResult replyMessage(@PathVariable Long petitionId, @RequestBody Map<String, String> body) {
        String content = body == null ? null : body.get("content");
        return toAjax(messageService.reply(petitionId, content, getUsername()));
    }

    /** 各举报的举报人留言未读数（列表红点提示，key=petitionId） */
    @PreAuthorize("@ss.hasPermi('jiancha:petition:list')")
    @GetMapping("/messageUnread")
    public AjaxResult messageUnread() {
        return success(messageService.unreadCountMap());
    }

    /** 催办：urge_count+1 并写轨迹（不改变状态机；办结举报不可催办） */
    @PreAuthorize("@ss.hasPermi('jiancha:petition:urge')")
    @Log(title = "舞弊举报", businessType = BusinessType.UPDATE)
    @PostMapping("/urge/{petitionId}")
    public AjaxResult urge(@PathVariable Long petitionId, @RequestBody(required = false) PetitionActionRequest req) {
        String opinion = req == null ? null : req.getOpinion();
        return success(petitionService.urge(petitionId, opinion, getUsername()));
    }

    /** 超期督办清单（未办结且超办理截止，按超期时长升序） */
    @PreAuthorize("@ss.hasPermi('jiancha:petition:stat')")
    @GetMapping("/overdue")
    public AjaxResult overdue() {
        return success(petitionService.selectOverdue());
    }

    // ═══════════════════════ 段16：并案 / 导出 / 低分复核 ═══════════════════════

    /** 并案：把从件并入主件（同被举报对象，双侧写轨迹） */
    @PreAuthorize("@ss.hasPermi('jiancha:petition:action')")
    @Log(title = "舞弊举报", businessType = BusinessType.UPDATE)
    @PostMapping("/merge/{childId}/{masterId}")
    public AjaxResult merge(@PathVariable Long childId, @PathVariable Long masterId,
                            @RequestBody(required = false) PetitionActionRequest req) {
        String remark = req == null ? null : req.getOpinion();
        return success(petitionService.merge(childId, masterId, remark, getUsername()));
    }

    /** 解除并案：从件恢复独立办理 */
    @PreAuthorize("@ss.hasPermi('jiancha:petition:action')")
    @Log(title = "舞弊举报", businessType = BusinessType.UPDATE)
    @PostMapping("/unmerge/{childId}")
    public AjaxResult unmerge(@PathVariable Long childId) {
        return success(petitionService.unmerge(childId, getUsername()));
    }

    /** 并案候选清单（同被举报对象的其他非终态独立件） */
    @PreAuthorize("@ss.hasPermi('jiancha:petition:query')")
    @GetMapping("/mergeCandidates/{petitionId}")
    public AjaxResult mergeCandidates(@PathVariable Long petitionId) {
        return success(petitionService.selectMergeCandidates(petitionId));
    }

    /** 已并入指定主件的从件清单 */
    @PreAuthorize("@ss.hasPermi('jiancha:petition:query')")
    @GetMapping("/mergedChildren/{masterId}")
    public AjaxResult mergedChildren(@PathVariable Long masterId) {
        return success(petitionService.selectMergedChildren(masterId));
    }

    /** 举报清单导出（Excel，复用列表查询与权限） */
    @PreAuthorize("@ss.hasPermi('jiancha:petition:list')")
    @Log(title = "舞弊举报", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, JcPetition jcPetition) {
        List<JcPetition> list = petitionService.selectJcPetitionList(jcPetition);
        ExcelUtil<JcPetition> util = new ExcelUtil<>(JcPetition.class);
        util.exportExcel(response, list, "舞弊举报清单");
    }

    /** 低分满意度复核清单（1-2 星，触发人工回访复核） */
    @PreAuthorize("@ss.hasPermi('jiancha:petition:stat')")
    @GetMapping("/dissatisfied")
    public AjaxResult dissatisfied() {
        return success(petitionService.selectDissatisfied());
    }

    /**
     * 手动触发超期自动催办（与 Quartz 任务 jcPetitionWarnTask.autoUrgeOverdue 同逻辑，
     * 供管理员在督办页即时执行，无需等待日调度）。
     * @return 本轮系统催办件数
     */
    @PreAuthorize("@ss.hasPermi('jiancha:petition:urge')")
    @Log(title = "舞弊举报", businessType = BusinessType.UPDATE)
    @PostMapping("/autoUrge")
    public AjaxResult autoUrge() {
        return success(petitionService.autoUrgeOverdue());
    }
}
