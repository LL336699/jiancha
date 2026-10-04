package com.jiancha.biz.archive.mapper;

import java.util.List;
import com.jiancha.biz.archive.domain.JcPerson;

/**
 * 干部廉政档案 Mapper
 *
 * @author 小标
 */
public interface JcPersonMapper {

    /**
     * 查询干部档案列表
     */
    List<JcPerson> selectJcPersonList(JcPerson jcPerson);

    /**
     * 查询干部档案详情
     */
    JcPerson selectJcPersonByPersonId(Long personId);

    /**
     * 新增干部档案
     */
    int insertJcPerson(JcPerson jcPerson);

    /**
     * 修改干部档案
     */
    int updateJcPerson(JcPerson jcPerson);

    /**
     * 删除干部档案（逻辑删除）
     */
    int deleteJcPersonByPersonId(Long personId);

    /**
     * 统计人员总数（看板用）
     */
    int countAll();
}
