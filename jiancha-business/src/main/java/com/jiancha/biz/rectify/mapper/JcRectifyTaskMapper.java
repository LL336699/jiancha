package com.jiancha.biz.rectify.mapper;

import java.util.List;
import java.util.Map;

import com.jiancha.biz.rectify.domain.JcRectifyTask;

/**
 * 整改措施 Mapper
 *
 * @author 小标
 */
public interface JcRectifyTaskMapper {

    /** 按问题查询措施列表 */
    List<JcRectifyTask> selectTaskByIssueId(Long issueId);

    /** 按问题统计措施数与已完成数 */
    Map<String, Object> countByIssueId(Long issueId);

    /** 新增措施 */
    int insertTask(JcRectifyTask task);

    /** 更新措施 */
    int updateTask(JcRectifyTask task);

    /** 删除措施 */
    int deleteTaskByTaskId(Long taskId);

    /** 删除问题时级联清理措施 */
    int deleteTaskByIssueId(Long issueId);
}
