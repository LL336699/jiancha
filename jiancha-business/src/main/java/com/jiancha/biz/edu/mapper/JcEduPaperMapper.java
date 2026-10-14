package com.jiancha.biz.edu.mapper;

import java.util.List;

import com.jiancha.biz.edu.domain.JcEduPaper;

/**
 * 试卷 Mapper（M9）
 *
 * @author 小标
 */
public interface JcEduPaperMapper {

    /** 列表 */
    List<JcEduPaper> selectJcEduPaperList(JcEduPaper jcEduPaper);

    /** 详情 */
    JcEduPaper selectJcEduPaperByPaperId(Long paperId);

    /** 已发布试卷（考生入口：仅返回状态=已发布） */
    List<JcEduPaper> selectPublishedPapers();

    /** 编号最大前缀（生成 paperCode 用） */
    String selectMaxCodeByPrefix(String prefix);

    /** 新增 */
    int insertJcEduPaper(JcEduPaper jcEduPaper);

    /** 修改 */
    int updateJcEduPaper(JcEduPaper jcEduPaper);

    /** 发布（草稿→已发布，固化 publish_time） */
    int publishPaper(Long paperId, String updateBy);

    /** 关闭（已发布→已关闭，不可再考） */
    int closePaper(Long paperId, String updateBy);

    /** 逻辑删除 */
    int deleteJcEduPaperByPaperId(Long paperId);

    /** 统计：试卷总数 */
    long countTotal();
}
