package com.jiancha.biz.petition.mapper;

import java.util.List;

import com.jiancha.biz.petition.domain.JcPetitionMaterial;

/**
 * 舞弊举报补充材料 Mapper
 *
 * @author 小标
 */
public interface JcPetitionMaterialMapper {

    /** 按举报查询材料列表 */
    List<JcPetitionMaterial> selectMaterialByPetitionId(Long petitionId);

    /** 新增材料 */
    int insertMaterial(JcPetitionMaterial material);

    /** 删除单条材料 */
    int deleteMaterialByMaterialId(Long materialId);

    /** 逻辑删除举报时同步清理材料 */
    int deleteMaterialByPetitionId(Long petitionId);
}
