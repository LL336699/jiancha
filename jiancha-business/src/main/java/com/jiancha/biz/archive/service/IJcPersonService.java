package com.jiancha.biz.archive.service;

import java.util.List;
import com.jiancha.biz.archive.domain.JcPerson;

/**
 * 干部廉政档案 Service
 *
 * @author 小标
 */
public interface IJcPersonService {

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
     *
     * <p>注意：档案涉及监督留痕，此处为逻辑删除，
     * 物理删除需走严格的审批流程（设计文档 §8.2）。</p>
     */
    int deleteJcPersonByPersonId(Long personId);

    /**
     * 统计人员总数
     */
    int countAll();
}
