-- ============================================================
-- 纪检监察助手 · 整改闭环台账模块 SQL（E3 整改闭环，企业版口径）
-- 追加执行，可重复运行（表结构 DROP+CREATE 会清空数据；有数据时改用文末 ALTER 方案）
-- 前提：jiancha_init.sql 已导入（sys_menu / sys_role_menu / sys_dict_type / sys_dict_data 已存在）
-- 段位：2600-2699 专供整改闭环台账
-- ============================================================

-- ------------------------------------------------------------
-- 1. 清理旧菜单（幂等：2600-2699 专供整改闭环）
-- ------------------------------------------------------------
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 2600 AND 2699;
DELETE FROM sys_menu      WHERE menu_id BETWEEN 2600 AND 2699;

-- ------------------------------------------------------------
-- 2. 整改闭环目录 + 菜单
--    2600 整改闭环（目录，挂在纪检监察 2000 下，排序 7）
--    2601 问题台账  2602 整改统计
-- ------------------------------------------------------------
INSERT INTO sys_menu VALUES (2600, '整改闭环', '2000', '7', 'rectify', null, '', '', 1, 0, 'M', '0', '0', '', 'form', 'admin', sysdate(), '', null, '整改闭环台账业务目录（E3）');

-- 2601 问题台账
INSERT INTO sys_menu VALUES (2601, '问题台账', '2600', '1', 'rectifyissue', 'jiancha/rectify/issue/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:rectify:list', 'list', 'admin', sysdate(), '', null, '问题登记/整改措施/流转销号/回头看');
INSERT INTO sys_menu VALUES (2611, '问题查询', '2601', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:rectify:list',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2612, '问题详情', '2601', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:rectify:query', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2613, '问题登记', '2601', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:rectify:add',   '#', 'admin', sysdate(), '', null, '含举报一键转整改');
INSERT INTO sys_menu VALUES (2614, '问题修改', '2601', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:rectify:edit',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2615, '办理流转', '2601', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:rectify:action','#', 'admin', sysdate(), '', null, '开始/提交复核/销号/退回/回头看/作废');
INSERT INTO sys_menu VALUES (2616, '措施管理', '2601', '6', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:rectify:task',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2617, '问题删除', '2601', '7', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:rectify:remove','#', 'admin', sysdate(), '', null, '级联清理措施与复核记录');

-- 2602 整改统计
INSERT INTO sys_menu VALUES (2602, '整改统计', '2600', '2', 'rectifystat', 'jiancha/rectify/stat/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:rectify:stat', 'chart', 'admin', sysdate(), '', null, '来源/类型/严重度/时效/部门对比');
INSERT INTO sys_menu VALUES (2621, '统计查询', '2602', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:rectify:stat', '#', 'admin', sysdate(), '', null, '');

-- ------------------------------------------------------------
-- 3. 授予超级管理员（role_id=1）全部新菜单
-- ------------------------------------------------------------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 2600 AND 2699;

-- ------------------------------------------------------------
-- 4. 数据字典
-- ------------------------------------------------------------
DELETE FROM sys_dict_data WHERE dict_type IN
    ('jc_rectify_source','jc_rectify_type','jc_rectify_severity','jc_rectify_status');
DELETE FROM sys_dict_type WHERE dict_type IN
    ('jc_rectify_source','jc_rectify_type','jc_rectify_severity','jc_rectify_status');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
 ('问题来源',   'jc_rectify_source',   '0', 'admin', sysdate(), '整改问题来源（与各模块 source_ref 对应）'),
 ('问题类型',   'jc_rectify_type',     '0', 'admin', sysdate(), '整改问题类型'),
 ('严重程度',   'jc_rectify_severity', '0', 'admin', sysdate(), '问题严重程度'),
 ('整改状态',   'jc_rectify_status',   '0', 'admin', sysdate(), '整改闭环流程状态（严格单向+回头看重开）');

-- 问题来源（value 与后端 registerFromPetition / 风控模块写入对应）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '内部审计',   'audit',      'jc_rectify_source', '', 'primary', 'Y', '0', 'admin', sysdate(), '审计发现问题'),
 (2, '专项检查',   'check',      'jc_rectify_source', '', 'info',    'N', '0', 'admin', sysdate(), '专项/合规检查发现'),
 (3, '巡察移交',   'inspection', 'jc_rectify_source', '', 'info',    'N', '0', 'admin', sysdate(), '内部巡察移交'),
 (4, '风险预警',   'warn',       'jc_rectify_source', '', 'danger',  'N', '0', 'admin', sysdate(), 'M10 风控引擎命中转入'),
 (5, '舞弊举报',   'petition',   'jc_rectify_source', '', 'warning', 'N', '0', 'admin', sysdate(), 'M3 举报办理后转入'),
 (6, '问题线索',   'clue',       'jc_rectify_source', '', 'warning', 'N', '0', 'admin', sysdate(), 'M4 线索处置后转入');

-- 问题类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '制度缺失',   '1', 'jc_rectify_type', '', 'info',    'Y', '0', 'admin', sysdate(), '缺少制度或制度不完善'),
 (2, '执行不到位', '2', 'jc_rectify_type', '', 'primary', 'N', '0', 'admin', sysdate(), '有制度未执行或执行走样'),
 (3, '廉洁风险',   '3', 'jc_rectify_type', '', 'danger',  'N', '0', 'admin', sysdate(), '利益冲突、舞弊风险'),
 (4, '财务违规',   '4', 'jc_rectify_type', '', 'warning', 'N', '0', 'admin', sysdate(), '虚报冒领、费用异常'),
 (5, '其他',       '5', 'jc_rectify_type', '', 'info',    'N', '0', 'admin', sysdate(), '其他问题');

-- 严重程度
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '一般', '一般', 'jc_rectify_severity', '', 'info',    'Y', '0', 'admin', sysdate(), '一般问题'),
 (2, '较大', '较大', 'jc_rectify_severity', '', 'warning', 'N', '0', 'admin', sysdate(), '较严重问题'),
 (3, '重大', '重大', 'jc_rectify_severity', '', 'danger',  'N', '0', 'admin', sysdate(), '重大问题');

-- 整改状态（value 为数字码，与后端状态机一致）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '待整改', '0', 'jc_rectify_status', '', 'info',    'Y', '0', 'admin', sysdate(), '已登记未开始整改'),
 (2, '整改中', '1', 'jc_rectify_status', '', 'primary', 'N', '0', 'admin', sysdate(), '整改措施进行中'),
 (3, '待复核', '2', 'jc_rectify_status', '', 'warning', 'N', '0', 'admin', sysdate(), '已提交复核'),
 (4, '已销号', '3', 'jc_rectify_status', '', 'success', 'N', '0', 'admin', sysdate(), '复核通过，闭环'),
 (5, '已作废', '4', 'jc_rectify_status', '', 'danger',  'N', '0', 'admin', sysdate(), '问题不成立作废');

-- ------------------------------------------------------------
-- 5. 业务参数（时限/阈值一律走配置，代码不硬编码）
-- ------------------------------------------------------------
DELETE FROM sys_config WHERE config_key LIKE 'jiancha.rectify.%';

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark) VALUES
 ('整改时限(日)',   'jiancha.rectify.deadline.days', '30', 'N', 'admin', sysdate(), '问题登记后默认整改截止天数，一般问题30日'),
 ('整改黄灯阈值比例','jiancha.rectify.warn.ratio',   '0.5','N', 'admin', sysdate(), '已用时长达到整改时限的比例即亮黄灯，默认0.5');

-- ------------------------------------------------------------
-- 6. 预警定时任务（默认暂停，可在「系统监控 → 定时任务」启用）
-- ------------------------------------------------------------
DELETE FROM sys_job WHERE invoke_target = 'jcRectifyWarnTask.refreshWarnLevel()';

INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
VALUES ('整改时限预警扫描', 'DEFAULT', 'jcRectifyWarnTask.refreshWarnLevel()', '0 5 8 * * ?', '3', '1', '1', 'admin', sysdate(), '每日8:05扫描未销号问题并刷新红黄灯（默认暂停）');

-- ------------------------------------------------------------
-- 7. 业务表
--    注意：del_flag 只作查询过滤，不映射进实体（BaseEntity 无该字段）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS jc_rectify_review;
DROP TABLE IF EXISTS jc_rectify_task;
DROP TABLE IF EXISTS jc_rectify_issue;

-- 问题台账主表
CREATE TABLE jc_rectify_issue (
    issue_id        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '问题ID',
    issue_no        VARCHAR(32)  NOT NULL                COMMENT '问题编号(RX+yyyyMMdd+seq)',
    source_type     VARCHAR(32)  DEFAULT ''              COMMENT '来源类型(字典 jc_rectify_source)',
    source_ref      VARCHAR(255) DEFAULT NULL            COMMENT '来源单号(如 petition:12/clue:JC.../warn:35)，同号只立一项；手工登记留空(NULL)，唯一索引不拦截多空值',
    title           VARCHAR(200) NOT NULL                COMMENT '问题标题',
    description     LONGTEXT                          COMMENT '问题描述',
    problem_type    VARCHAR(32)  DEFAULT ''              COMMENT '问题类型(字典 jc_rectify_type)',
    severity        VARCHAR(16)  DEFAULT '一般'           COMMENT '严重程度(字典 jc_rectify_severity)',
    found_date      DATE         DEFAULT NULL            COMMENT '发现日期',
    found_dept      VARCHAR(100) DEFAULT ''              COMMENT '发现问题单位/部门',
    target_dept_id  BIGINT       DEFAULT NULL            COMMENT '整改责任部门ID(sys_dept)',
    target_person_id BIGINT      DEFAULT NULL            COMMENT '整改责任人ID(jc_person)',
    deadline        DATE         DEFAULT NULL            COMMENT '整改截止日期',
    status          CHAR(1)      DEFAULT '0'             COMMENT '状态(0待整改 1整改中 2待复核 3已销号 4已作废)',
    warn_level      VARCHAR(8)   DEFAULT ''              COMMENT '预警级别(空/黄灯/红灯)',
    rectify_summary VARCHAR(1000) DEFAULT ''             COMMENT '整改情况综述(销号复核时留痕)',
    close_time      DATETIME     DEFAULT NULL            COMMENT '销号时间',
    remark          VARCHAR(500) DEFAULT ''              COMMENT '备注',
    create_by       VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time     DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time     DATETIME     DEFAULT NULL            COMMENT '更新时间',
    del_flag        CHAR(1)      DEFAULT '0'             COMMENT '删除标记(0正常 1删除)',
    PRIMARY KEY (issue_id),
    UNIQUE KEY uk_rectify_no (issue_no),
    UNIQUE KEY uk_rectify_source (source_ref),
    KEY idx_rectify_status (status),
    KEY idx_rectify_dept (target_dept_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='整改问题台账主表';

-- 整改措施表
CREATE TABLE jc_rectify_task (
    task_id       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '措施ID',
    issue_id      BIGINT       NOT NULL                COMMENT '问题ID',
    measure       VARCHAR(1000) NOT NULL               COMMENT '整改措施内容',
    owner_name    VARCHAR(100) DEFAULT ''              COMMENT '责任人姓名',
    plan_date     DATE         DEFAULT NULL            COMMENT '计划完成日期',
    done_date     DATE         DEFAULT NULL            COMMENT '实际完成日期',
    evidence_path VARCHAR(500) DEFAULT ''              COMMENT '佐证材料路径',
    status        CHAR(1)      DEFAULT '0'             COMMENT '状态(0未开始 1进行中 2已完成)',
    create_by     VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time   DATETIME     DEFAULT NULL            COMMENT '创建时间',
    PRIMARY KEY (task_id),
    KEY idx_task_issue (issue_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='整改措施';

-- 复核/回头看记录表
CREATE TABLE jc_rectify_review (
    review_id   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    issue_id    BIGINT       NOT NULL                COMMENT '问题ID',
    review_type CHAR(1)      NOT NULL                COMMENT '记录类型(1销号复核 2回头看)',
    result      CHAR(1)      DEFAULT '0'             COMMENT '结论(0不通过 1通过)',
    opinion     VARCHAR(1000) DEFAULT ''             COMMENT '复核意见',
    reviewer    VARCHAR(64)  DEFAULT ''              COMMENT '复核人',
    review_time DATETIME     DEFAULT NULL            COMMENT '复核时间',
    PRIMARY KEY (review_id),
    KEY idx_review_issue (issue_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='整改复核/回头看记录';

-- ------------------------------------------------------------
-- 8. 自检
-- ------------------------------------------------------------
SELECT '整改菜单'   AS item, COUNT(1) AS cnt FROM sys_menu      WHERE menu_id BETWEEN 2600 AND 2699
UNION ALL SELECT '整改字典项', COUNT(1) FROM sys_dict_data WHERE dict_type LIKE 'jc_rectify%'
UNION ALL SELECT '整改参数',   COUNT(1) FROM sys_config   WHERE config_key LIKE 'jiancha.rectify.%'
UNION ALL SELECT '整改任务',   COUNT(1) FROM sys_job      WHERE invoke_target = 'jcRectifyWarnTask.refreshWarnLevel()'
UNION ALL SELECT '整改表',     COUNT(1) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name IN ('jc_rectify_issue','jc_rectify_task','jc_rectify_review');

-- ------------------------------------------------------------
-- 【已有数据时改用以下 ALTER，避免 DROP 丢数据】
-- ALTER TABLE jc_rectify_issue ADD COLUMN rectify_summary VARCHAR(1000) DEFAULT '' COMMENT '整改情况综述' AFTER warn_level;
-- ALTER TABLE jc_rectify_issue ADD COLUMN close_time DATETIME DEFAULT NULL COMMENT '销号时间' AFTER rectify_summary;
-- ------------------------------------------------------------
