package com.jiancha.biz.petition.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jiancha.biz.petition.domain.JcPetitionMaterial;
import com.jiancha.biz.petition.mapper.JcPetitionMaterialMapper;
import com.jiancha.biz.petition.service.IJcPetitionMaterialService;

/**
 * 舞弊举报补充材料 Service 实现
 *
 * @author 小标
 */
@Service
public class JcPetitionMaterialServiceImpl implements IJcPetitionMaterialService {

    @Autowired
    private JcPetitionMaterialMapper materialMapper;

    @Override
    public List<JcPetitionMaterial> selectMaterialByPetitionId(Long petitionId) {
        return materialMapper.selectMaterialByPetitionId(petitionId);
    }

    @Override
    public int insertMaterial(JcPetitionMaterial material) {
        return materialMapper.insertMaterial(material);
    }

    @Override
    public int deleteMaterialByMaterialId(Long materialId) {
        return materialMapper.deleteMaterialByMaterialId(materialId);
    }
}
