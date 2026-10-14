package com.jiancha.biz.petition.service;

import java.util.List;

import com.jiancha.biz.petition.domain.JcPetitionMaterial;

/**
 * 舞弊举报补充材料 Service
 *
 * @author 小标
 */
public interface IJcPetitionMaterialService {

    /** 按举报查询材料 */
    List<JcPetitionMaterial> selectMaterialByPetitionId(Long petitionId);

    /** 新增材料 */
    int insertMaterial(JcPetitionMaterial material);

    /** 删除材料 */
    int deleteMaterialByMaterialId(Long materialId);
}
