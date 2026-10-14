package com.jiancha.biz.risk.mapper;

import java.util.List;

import com.jiancha.biz.risk.domain.JcRiskModel;

/**
 * 岗位风险模型 Mapper
 *
 * @author 小标
 */
public interface JcRiskModelMapper {

    /** 模型行列表（可按岗位过滤） */
    List<JcRiskModel> selectJcRiskModelList(JcRiskModel jcRiskModel);

    /** 岗位权重汇总（三级视图-岗位层） */
    List<java.util.Map<String, Object>> sumWeightByPosition();

    /** 详情 */
    JcRiskModel selectJcRiskModelByModelId(Long modelId);

    /** 新增模型行 */
    int insertJcRiskModel(JcRiskModel jcRiskModel);

    /** 更新模型行 */
    int updateJcRiskModel(JcRiskModel jcRiskModel);

    /** 删除模型行 */
    int deleteJcRiskModelByModelId(Long modelId);
}
