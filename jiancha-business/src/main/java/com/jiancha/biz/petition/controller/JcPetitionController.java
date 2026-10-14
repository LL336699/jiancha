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

import com.jiancha.biz.petition.crypto.JcFieldCrypto;
import com.jiancha.biz.petition.domain.JcPetition;
import com.jiancha.biz.petition.domain.JcPetitionMaterial;
import com.jiancha.biz.petition.domain.JcPetitionTrace;
import com.jiancha.biz.petition.domain.PetitionActionRequest;
import com.jiancha.biz.petition.domain.PetitionStat;
import com.jiancha.biz.petition.service.IJcPetitionMaterialService;
import com.jiancha.biz.petition.service.IJcPetitionService;
import com.jiancha.biz.petition.service.IJcPetitionTraceService;

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
}
