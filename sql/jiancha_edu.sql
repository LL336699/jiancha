-- ============================================================
-- 纪检监察助手 · 廉政教育题库模块 SQL（M9 廉政教育，企业版口径）
-- 追加执行，可重复运行（表结构 DROP+CREATE 会清空数据；有数据时改用文末 ALTER 方案）
-- 前提：jiancha_init.sql 已导入（sys_menu / sys_role_menu / sys_dict_type / sys_dict_data 已存在）
-- 段位：2900-2999 专供廉政教育题库
-- 依赖：jc_person（档案域人员，删除标记 '2'）用于考生与签收人
-- ============================================================

-- ------------------------------------------------------------
-- 1. 清理旧菜单（幂等：2900-2999 专供廉政教育）
-- ------------------------------------------------------------
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 2900 AND 2999;
DELETE FROM sys_menu      WHERE menu_id BETWEEN 2900 AND 2999;

-- ------------------------------------------------------------
-- 2. 廉政教育目录 + 菜单
--    2900 廉政教育（目录，挂在纪检监察 2000 下，排序 10）
--    2901 题库管理  2902 试卷管理  2903 考试管理  2904 承诺书签收  2905 教育看板
-- ------------------------------------------------------------
INSERT INTO sys_menu VALUES (2900, '廉政教育', '2000', '10', 'education', null, '', '', 1, 0, 'M', '0', '0', '', 'education', 'admin', sysdate(), '', null, '廉政教育题库业务目录（M9）');

-- 2901 题库管理
INSERT INTO sys_menu VALUES (2901, '题库管理', '2900', '1', 'eduquestion', 'jiancha/edu/question/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:edu:question:list', 'education', 'admin', sysdate(), '', null, '单选/多选/判断题维护，列表不返回答案防泄题');
INSERT INTO sys_menu VALUES (2911, '题库查询', '2901', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:edu:question:list',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2912, '题库详情', '2901', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:edu:question:query', '#', 'admin', sysdate(), '', null, '维护端查看答案与解析');
INSERT INTO sys_menu VALUES (2913, '题库新增', '2901', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:edu:question:add',   '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2914, '题库修改', '2901', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:edu:question:edit',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2915, '题库删除', '2901', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:edu:question:remove','#', 'admin', sysdate(), '', null, '');

-- 2902 试卷管理
INSERT INTO sys_menu VALUES (2902, '试卷管理', '2900', '2', 'edupaper', 'jiancha/edu/paper/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:edu:paper:list', 'documentation', 'admin', sysdate(), '', null, '手工选题目录 + 随机策略组卷，发布固化题目快照');
INSERT INTO sys_menu VALUES (2921, '试卷查询', '2902', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:edu:paper:list',   '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2922, '试卷详情', '2902', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:edu:paper:query', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2923, '试卷新增', '2902', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:edu:paper:add',    '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2924, '随机组卷', '2902', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:edu:paper:random', '#', 'admin', sysdate(), '', null, '按分类×题型×数量抽题');
INSERT INTO sys_menu VALUES (2925, '试卷修改', '2902', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:edu:paper:edit',   '#', 'admin', sysdate(), '', null, '仅草稿可改');
INSERT INTO sys_menu VALUES (2926, '试卷发布', '2902', '6', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:edu:paper:publish','#', 'admin', sysdate(), '', null, '固化题目快照');
INSERT INTO sys_menu VALUES (2927, '试卷删除', '2902', '7', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:edu:paper:remove', '#', 'admin', sysdate(), '', null, '仅草稿可删');

-- 2903 考试管理
INSERT INTO sys_menu VALUES (2903, '考试管理', '2900', '3', 'eduexam', 'jiancha/edu/exam/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:edu:exam:list', 'skill', 'admin', sysdate(), '', null, '开考(断点续考)/交卷自动评分/错题本');
INSERT INTO sys_menu VALUES (2931, '考试查询', '2903', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:edu:exam:list',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2932, '考试详情', '2903', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:edu:exam:query', '#', 'admin', sysdate(), '', null, '含答题明细');
INSERT INTO sys_menu VALUES (2933, '开考',     '2903', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:edu:exam:start', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2934, '交卷',     '2903', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:edu:exam:submit','#', 'admin', sysdate(), '', null, '客观题自动评分');

-- 2904 承诺书签收
INSERT INTO sys_menu VALUES (2904, '承诺书签收', '2900', '4', 'educommitment', 'jiancha/edu/commitment/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:edu:commitment:list', 'peoples', 'admin', sysdate(), '', null, '承诺书发布/签收/进度，签收幂等');
INSERT INTO sys_menu VALUES (2941, '签收查询', '2904', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:edu:commitment:list',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2942, '签收详情', '2904', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:edu:commitment:query', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2943, '承诺书新增','2904', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:edu:commitment:add',   '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2944, '承诺书修改','2904', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:edu:commitment:edit',  '#', 'admin', sysdate(), '', null, '仅草稿可改');
INSERT INTO sys_menu VALUES (2945, '承诺书发布','2904', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:edu:commitment:publish','#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2946, '承诺书签收','2904', '6', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:edu:commitment:sign',  '#', 'admin', sysdate(), '', null, '幂等：重复签收返回既有记录');
INSERT INTO sys_menu VALUES (2947, '承诺书删除','2904', '7', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:edu:commitment:remove','#', 'admin', sysdate(), '', null, '仅草稿可删');

-- 2905 教育看板
INSERT INTO sys_menu VALUES (2905, '教育看板', '2900', '5', 'edustat', 'jiancha/edu/stat/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:edu:stat', 'chart', 'admin', sysdate(), '', null, '题库量/考试人次/通过率/成绩分布/错题TopN');
INSERT INTO sys_menu VALUES (2951, '统计查询', '2905', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:edu:stat', '#', 'admin', sysdate(), '', null, '');

-- ------------------------------------------------------------
-- 3. 授予超级管理员（role_id=1）全部新菜单
-- ------------------------------------------------------------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 2900 AND 2999;

-- ------------------------------------------------------------
-- 4. 数据字典
-- ------------------------------------------------------------
DELETE FROM sys_dict_data WHERE dict_type IN
    ('jc_edu_qtype','jc_edu_qlevel','jc_edu_paper_mode','jc_edu_paper_status','jc_edu_exam_status','jc_edu_commit_scope','jc_edu_commit_status');
DELETE FROM sys_dict_type WHERE dict_type IN
    ('jc_edu_qtype','jc_edu_qlevel','jc_edu_paper_mode','jc_edu_paper_status','jc_edu_exam_status','jc_edu_commit_scope','jc_edu_commit_status');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
 ('教育题型',     'jc_edu_qtype',        '0', 'admin', sysdate(), '廉政教育题目类型'),
 ('题目难度',     'jc_edu_qlevel',       '0', 'admin', sysdate(), '题目难度'),
 ('组卷方式',     'jc_edu_paper_mode',   '0', 'admin', sysdate(), '试卷组卷方式'),
 ('试卷状态',     'jc_edu_paper_status', '0', 'admin', sysdate(), '试卷生命周期状态'),
 ('考试状态',     'jc_edu_exam_status',  '0', 'admin', sysdate(), '考试记录状态'),
 ('承诺书适用范围','jc_edu_commit_scope','0', 'admin', sysdate(), '廉洁承诺书适用范围'),
 ('承诺书状态',   'jc_edu_commit_status','0', 'admin', sysdate(), '承诺书生命周期状态');

-- 题型（与后端评分引擎一致：单选/判断单字母比对，多选集合比对）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '单选', '单选', 'jc_edu_qtype', '', 'primary', 'Y', '0', 'admin', sysdate(), '四选一，单字母答案'),
 (2, '多选', '多选', 'jc_edu_qtype', '', 'success', 'N', '0', 'admin', sysdate(), '2-4 个字母集合答案，全对才得分'),
 (3, '判断', '判断', 'jc_edu_qtype', '', 'warning', 'N', '0', 'admin', sysdate(), 'A.正确 B.错误');

-- 难度
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '易', '易', 'jc_edu_qlevel', '', 'success', 'Y', '0', 'admin', sysdate(), ''),
 (2, '中', '中', 'jc_edu_qlevel', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
 (3, '难', '难', 'jc_edu_qlevel', '', 'danger',  'N', '0', 'admin', sysdate(), '');

-- 组卷方式
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '手工', '手工', 'jc_edu_paper_mode', '', 'primary', 'Y', '0', 'admin', sysdate(), '人工选题目录'),
 (2, '随机', '随机', 'jc_edu_paper_mode', '', 'success', 'N', '0', 'admin', sysdate(), '按策略抽题');

-- 试卷状态（与后端状态机常量一致）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '草稿',   '0', 'jc_edu_paper_status', '', 'info',    'Y', '0', 'admin', sysdate(), '可改可删'),
 (2, '已发布', '1', 'jc_edu_paper_status', '', 'success', 'N', '0', 'admin', sysdate(), '题目快照固化'),
 (3, '已关闭', '2', 'jc_edu_paper_status', '', 'danger',  'N', '0', 'admin', sysdate(), '不可开考');

-- 考试状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '进行中', '0', 'jc_edu_exam_status', '', 'primary', 'Y', '0', 'admin', sysdate(), '可断点续考'),
 (2, '已交卷', '1', 'jc_edu_exam_status', '', 'success', 'N', '0', 'admin', sysdate(), '已评分'),
 (3, '弃考',   '2', 'jc_edu_exam_status', '', 'info',    'N', '0', 'admin', sysdate(), '超时或主动弃考');

-- 承诺书适用范围
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '全员',     '全员',     'jc_edu_commit_scope', '', 'primary', 'Y', '0', 'admin', sysdate(), '全体在册人员，可预测应签人数'),
 (2, '关键岗位', '关键岗位', 'jc_edu_commit_scope', '', 'warning', 'N', '0', 'admin', sysdate(), '由部门自查'),
 (3, '新员工',   '新员工',   'jc_edu_commit_scope', '', 'success', 'N', '0', 'admin', sysdate(), '入职一年内'),
 (4, '指定人员', '指定人员', 'jc_edu_commit_scope', '', 'info',    'N', '0', 'admin', sysdate(), '按签收记录点名');

-- 承诺书状态（与后端状态机常量一致）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '草稿',   '0', 'jc_edu_commit_status', '', 'info',    'Y', '0', 'admin', sysdate(), ''),
 (2, '已发布', '1', 'jc_edu_commit_status', '', 'success', 'N', '0', 'admin', sysdate(), '开放签收'),
 (3, '已关闭', '2', 'jc_edu_commit_status', '', 'danger',  'N', '0', 'admin', sysdate(), '停止签收');

-- ------------------------------------------------------------
-- 5. 业务参数
-- ------------------------------------------------------------
DELETE FROM sys_config WHERE config_key LIKE 'jiancha.edu.%';

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark) VALUES
 ('考试通过后允许重考', 'jiancha.edu.retake', 'N', 'Y', 'admin', sysdate(), 'N=通过后不可重考；Y=放开重考（慎用）');

-- ------------------------------------------------------------
-- 6. 考试超时弃考定时任务（默认暂停，可在「系统监控 → 定时任务」启用）
-- ------------------------------------------------------------
DELETE FROM sys_job WHERE invoke_target = 'jcEduScanTask.run()';

INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
VALUES ('教育考试超时弃考', 'DEFAULT', 'jcEduScanTask.run()', '0 40 7 * * ?', '3', '1', '1', 'admin', sysdate(), '每日7:40：超过时长1.5倍未交卷的进行中考试自动置弃考（默认暂停）');

-- ------------------------------------------------------------
-- 7. 业务表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS jc_edu_exam_answer;
DROP TABLE IF EXISTS jc_edu_exam;
DROP TABLE IF EXISTS jc_edu_paper_question;
DROP TABLE IF EXISTS jc_edu_paper;
DROP TABLE IF EXISTS jc_edu_commitment;
DROP TABLE IF EXISTS jc_edu_question;

-- 题库表
CREATE TABLE jc_edu_question (
    question_id BIGINT       NOT NULL AUTO_INCREMENT COMMENT '题目ID',
    category    VARCHAR(32)  DEFAULT ''              COMMENT '分类(党纪/国法/规章/案例/警示)',
    qtype       VARCHAR(16)  NOT NULL                COMMENT '题型(字典 jc_edu_qtype：单选/多选/判断)',
    level       VARCHAR(8)   DEFAULT '中'            COMMENT '难度(字典 jc_edu_qlevel)',
    title       VARCHAR(1000) NOT NULL               COMMENT '题目标题',
    options     VARCHAR(2000) DEFAULT ''             COMMENT '选项(A.xxx|B.xxx|C.xxx|D.xxx)',
    answer      VARCHAR(32)  NOT NULL                COMMENT '答案(单选/判断单字母，多选逗号分隔如 A,B)',
    analysis    VARCHAR(1000) DEFAULT ''             COMMENT '解析',
    score       INT          DEFAULT 2               COMMENT '分值',
    status      CHAR(1)      DEFAULT '0'             COMMENT '状态(0启用 1停用)',
    use_count   INT          DEFAULT 0               COMMENT '被组卷引用次数',
    remark      VARCHAR(500) DEFAULT ''              COMMENT '备注',
    create_by   VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by   VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time DATETIME     DEFAULT NULL            COMMENT '更新时间',
    del_flag    CHAR(1)      DEFAULT '2'             COMMENT '删除标记(0正常 2删除)',
    PRIMARY KEY (question_id),
    KEY idx_question_cat (category, qtype),
    KEY idx_question_status (status)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='廉政教育题库';

-- 试卷表
CREATE TABLE jc_edu_paper (
    paper_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '试卷ID',
    paper_code   VARCHAR(32)  NOT NULL                COMMENT '试卷编号(PA+yyyyMMdd+seq)',
    paper_name   VARCHAR(200) NOT NULL                COMMENT '试卷名称',
    paper_mode   VARCHAR(16)  DEFAULT '手工'          COMMENT '组卷方式(字典 jc_edu_paper_mode)',
    total_score  INT          DEFAULT 0               COMMENT '总分(组卷时自动重算)',
    pass_score   INT          DEFAULT 0               COMMENT '及格分(总分60%)',
    duration_min INT          DEFAULT 30              COMMENT '考试时长(分钟)',
    status       CHAR(1)      DEFAULT '0'             COMMENT '状态(0草稿 1已发布 2已关闭)',
    publish_time DATETIME     DEFAULT NULL            COMMENT '发布时间',
    remark       VARCHAR(500) DEFAULT ''              COMMENT '备注',
    create_by    VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time  DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by    VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time  DATETIME     DEFAULT NULL            COMMENT '更新时间',
    del_flag     CHAR(1)      DEFAULT '0'             COMMENT '删除标记(0正常 1删除)',
    PRIMARY KEY (paper_id),
    UNIQUE KEY uk_paper_code (paper_code),
    KEY idx_paper_status (status)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='廉政教育试卷';

-- 组卷题目表（发布时固化快照）
CREATE TABLE jc_edu_paper_question (
    id             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    paper_id       BIGINT        NOT NULL                COMMENT '试卷ID',
    question_id    BIGINT        NOT NULL                COMMENT '题目ID',
    title_snapshot VARCHAR(1000) DEFAULT ''              COMMENT '题目标题快照(发布固化)',
    qtype          VARCHAR(16)   DEFAULT ''              COMMENT '题型快照',
    score          INT           DEFAULT 0               COMMENT '分值快照',
    sort_no        INT           DEFAULT 0               COMMENT '排序号',
    remark         VARCHAR(500)  DEFAULT ''              COMMENT '备注',
    create_by      VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
    create_time    DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by      VARCHAR(64)   DEFAULT ''              COMMENT '更新者',
    update_time    DATETIME     DEFAULT NULL            COMMENT '更新时间',
    del_flag       CHAR(1)      DEFAULT '0'             COMMENT '删除标记(0正常 1删除)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_paper_question (paper_id, question_id),
    KEY idx_pq_question (question_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='组卷题目(快照)';

-- 考试记录表
CREATE TABLE jc_edu_exam (
    exam_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '考试ID',
    paper_id    BIGINT       NOT NULL                COMMENT '试卷ID',
    person_id   BIGINT       NOT NULL                COMMENT '考生ID(jc_person.person_id)',
    person_name VARCHAR(64)  DEFAULT ''              COMMENT '考生姓名(快照)',
    start_time  DATETIME     DEFAULT NULL            COMMENT '开考时间',
    submit_time DATETIME     DEFAULT NULL            COMMENT '交卷时间',
    score       INT          DEFAULT NULL            COMMENT '得分',
    passed      CHAR(1)      DEFAULT '0'             COMMENT '是否通过(0否 1是)',
    status      CHAR(1)      DEFAULT '0'             COMMENT '状态(0进行中 1已交卷 2弃考)',
    remark      VARCHAR(500) DEFAULT ''              COMMENT '备注',
    create_by   VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by   VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time DATETIME     DEFAULT NULL            COMMENT '更新时间',
    del_flag    CHAR(1)      DEFAULT '0'             COMMENT '删除标记(0正常 1删除)',
    PRIMARY KEY (exam_id),
    KEY idx_exam_paper (paper_id),
    KEY idx_exam_person (person_id),
    KEY idx_exam_status (status)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='廉政教育考试记录';

-- 答题明细表
CREATE TABLE jc_edu_exam_answer (
    answer_id   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    exam_id     BIGINT       NOT NULL                COMMENT '考试ID',
    question_id BIGINT       NOT NULL                COMMENT '题目ID',
    my_answer   VARCHAR(200) DEFAULT ''              COMMENT '我的作答',
    correct     CHAR(1)      DEFAULT '0'             COMMENT '是否正确(0错 1对)',
    score       INT          DEFAULT 0               COMMENT '该题得分',
    remark      VARCHAR(500) DEFAULT ''              COMMENT '备注',
    create_by   VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by   VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time DATETIME     DEFAULT NULL            COMMENT '更新时间',
    del_flag    CHAR(1)      DEFAULT '0'             COMMENT '删除标记(0正常 1删除)',
    PRIMARY KEY (answer_id),
    KEY idx_answer_exam (exam_id),
    KEY idx_answer_question (question_id, correct)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='答题明细';

-- 廉洁承诺书签收表（合一表：record_type 0 本体 / 1 签收记录）
CREATE TABLE jc_edu_commitment (
    commit_id   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '承诺书ID',
    title       VARCHAR(200) NOT NULL                COMMENT '标题',
    content     VARCHAR(4000) DEFAULT ''             COMMENT '正文内容',
    scope_type  VARCHAR(32)  DEFAULT '全员'          COMMENT '适用范围(字典 jc_edu_commit_scope)',
    deadline    DATETIME     DEFAULT NULL            COMMENT '签收截止日期',
    status      CHAR(1)      DEFAULT '0'             COMMENT '状态(0草稿 1已发布 2已关闭)',
    person_id   BIGINT       DEFAULT NULL            COMMENT '签收人ID(签收记录)',
    person_name VARCHAR(64)  DEFAULT ''              COMMENT '签收人姓名(快照)',
    sign_type   VARCHAR(32)  DEFAULT ''              COMMENT '签收方式',
    sign_time   DATETIME     DEFAULT NULL            COMMENT '签收时间',
    record_type CHAR(1)      DEFAULT '0'             COMMENT '记录类型(0承诺书本体 1签收记录)',
    remark      VARCHAR(500) DEFAULT ''              COMMENT '备注',
    create_by   VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by   VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time DATETIME     DEFAULT NULL            COMMENT '更新时间',
    del_flag    CHAR(1)      DEFAULT '0'             COMMENT '删除标记(0正常 1删除)',
    PRIMARY KEY (commit_id),
    KEY idx_commit_type (record_type, status),
    KEY idx_commit_person (person_id),
    KEY idx_commit_parent (commit_id, person_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='廉洁承诺书签收';

-- ------------------------------------------------------------
-- 8. 自检
-- ------------------------------------------------------------
SELECT '教育菜单'   AS item, COUNT(1) AS cnt FROM sys_menu      WHERE menu_id BETWEEN 2900 AND 2999
UNION ALL SELECT '教育字典项', COUNT(1) FROM sys_dict_data WHERE dict_type LIKE 'jc_edu%'
UNION ALL SELECT '教育参数',   COUNT(1) FROM sys_config   WHERE config_key LIKE 'jiancha.edu.%'
UNION ALL SELECT '教育任务',   COUNT(1) FROM sys_job      WHERE invoke_target = 'jcEduScanTask.run()'
UNION ALL SELECT '教育表',     COUNT(1) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name LIKE 'jc_edu%';

-- ------------------------------------------------------------
-- 【已有数据时改用以下 ALTER，避免 DROP 丢数据】
-- ALTER TABLE jc_edu_paper ADD COLUMN duration_min INT DEFAULT 30 COMMENT '考试时长(分钟)' AFTER pass_score;
-- ------------------------------------------------------------
