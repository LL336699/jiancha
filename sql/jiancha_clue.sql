-- ============================================================
-- 纪检监察助手 · P3（M4 问题线索管理）数据库脚本
--
-- 执行前提：ry_20260417.sql + quartz.sql + jiancha_init.sql 已导入
-- 幂等性  ：可重复执行（表重建、字典/菜单/参数先清后插）
--
-- 说明：jc_clue / jc_clue_trace 两表在 P2 建库时已创建但从未被业务写入，
--       本次按 P3 口径 DROP 重建（jc_clue 新增 disposition 处置方式字段）。
--       若库中已有线索数据，请勿执行本脚本第 1 节，改用文末注释的 ALTER 语句。
-- ============================================================

-- ============================================================
-- 1. 线索主表 / 轨迹表（企业版口径）
-- ============================================================
DROP TABLE IF EXISTS jc_clue;
CREATE TABLE jc_clue (
  clue_id         BIGINT(20)   NOT NULL AUTO_INCREMENT   COMMENT '线索ID',
  clue_no         VARCHAR(32)  NOT NULL                  COMMENT '线索编号',
  source_type     VARCHAR(32)  NOT NULL                  COMMENT '来源类型(字典 jc_clue_source)',
  violation_type  VARCHAR(32)  DEFAULT ''                COMMENT '问题类型(字典 jc_violation_type)',
  person_id       BIGINT(20)   DEFAULT NULL              COMMENT '涉及人员ID',
  related_persons VARCHAR(1000) DEFAULT ''               COMMENT '关联人员(多人,逗号分隔)',
  summary         VARCHAR(1000) NOT NULL                 COMMENT '线索摘要',
  detail          TEXT                                   COMMENT '详细情况',
  dept_id         BIGINT(20)   NOT NULL                  COMMENT '承办部门ID',
  handler_id      BIGINT(20)   DEFAULT NULL              COMMENT '当前处理人ID',
  status          VARCHAR(16)  NOT NULL                  COMMENT '办理状态(字典 jc_clue_status)',
  disposition     VARCHAR(32)  DEFAULT ''                COMMENT '处置方式(字典 jc_clue_disposition)',
  deadline        DATE         DEFAULT NULL              COMMENT '办理截止日期',
  warn_level      VARCHAR(8)   DEFAULT ''                COMMENT '预警级别(字典 jc_warn_level)',
  is_duplicate    CHAR(1)      DEFAULT '0'               COMMENT '是否重复线索(0否 1是)',
  duplicate_of    BIGINT(20)   DEFAULT NULL              COMMENT '重复指向的线索ID',
  source_ref      VARCHAR(255) DEFAULT ''                COMMENT '来源文号/编号',
  create_by       VARCHAR(64)  DEFAULT ''                COMMENT '创建者',
  create_time     DATETIME     DEFAULT NULL              COMMENT '创建时间',
  update_by       VARCHAR(64)  DEFAULT ''                COMMENT '更新者',
  update_time     DATETIME     DEFAULT NULL              COMMENT '更新时间',
  del_flag        CHAR(1)      DEFAULT '0'               COMMENT '删除标志(0存在 2删除)',
  remark          VARCHAR(500) DEFAULT NULL              COMMENT '备注',
  PRIMARY KEY (clue_id),
  UNIQUE KEY uk_clue_no (clue_no),
  KEY idx_clue_dept (dept_id),
  KEY idx_clue_status (status),
  KEY idx_clue_deadline (deadline, status),
  KEY idx_clue_person (person_id),
  KEY idx_clue_source (source_type)
) ENGINE=InnoDB AUTO_INCREMENT=1000 COMMENT = '问题线索主表';

DROP TABLE IF EXISTS jc_clue_trace;
CREATE TABLE jc_clue_trace (
  trace_id      BIGINT(20)  NOT NULL AUTO_INCREMENT    COMMENT '轨迹ID',
  clue_id       BIGINT(20)  NOT NULL                   COMMENT '线索ID',
  action        VARCHAR(32) NOT NULL                   COMMENT '动作(字典 jc_clue_action)',
  action_desc   VARCHAR(1000) DEFAULT ''               COMMENT '动作说明',
  before_status VARCHAR(16) DEFAULT ''                 COMMENT '变更前状态',
  after_status  VARCHAR(16) DEFAULT ''                 COMMENT '变更后状态',
  operator_id   BIGINT(20)  NOT NULL                   COMMENT '操作人ID',
  operator_name VARCHAR(64) DEFAULT ''                 COMMENT '操作人姓名(冗余)',
  operate_time  DATETIME    NOT NULL                   COMMENT '操作时间',
  create_by     VARCHAR(64) DEFAULT ''                 COMMENT '创建者',
  create_time   DATETIME    DEFAULT NULL               COMMENT '创建时间',
  update_by     VARCHAR(64) DEFAULT ''                 COMMENT '更新者',
  update_time   DATETIME    DEFAULT NULL               COMMENT '更新时间',
  del_flag      CHAR(1)     DEFAULT '0'                COMMENT '删除标志(0存在 2删除)',
  remark        VARCHAR(500) DEFAULT NULL              COMMENT '备注',
  PRIMARY KEY (trace_id),
  KEY idx_trace_clue (clue_id),
  KEY idx_trace_time (operate_time)
) ENGINE=InnoDB AUTO_INCREMENT=1000 COMMENT = '问题线索办理轨迹(全程留痕)';

-- ============================================================
-- 2. 企业口径字典（覆盖 P2 的党政口径）
-- ============================================================
DELETE FROM sys_dict_data WHERE dict_type IN
    ('jc_clue_source','jc_violation_type','jc_clue_status','jc_clue_action','jc_clue_disposition');
DELETE FROM sys_dict_type WHERE dict_type IN
    ('jc_clue_source','jc_violation_type','jc_clue_status','jc_clue_action','jc_clue_disposition');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
 ('线索来源',   'jc_clue_source',      '0', 'admin', sysdate(), '企业内部线索来源'),
 ('问题类型',   'jc_violation_type',   '0', 'admin', sysdate(), '企业违规问题类型'),
 ('线索办理状态','jc_clue_status',      '0', 'admin', sysdate(), '线索办理流程状态'),
 ('线索办理动作','jc_clue_action',      '0', 'admin', sysdate(), '线索流转动作'),
 ('线索处置方式','jc_clue_disposition', '0', 'admin', sysdate(), '线索最终处置方式');

-- 线索来源（value 为英文码，用于时限参数 key：jiancha.clue.deadline.<value>）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '员工举报',     'report',     'jc_clue_source', '', 'warning', 'Y', '0', 'admin', sysdate(), '员工实名反映'),
 (2, '匿名举报',     'anonymous',  'jc_clue_source', '', 'warning', 'N', '0', 'admin', sysdate(), '匿名渠道反映'),
 (3, '内部审计',     'audit',      'jc_clue_source', '', 'primary', 'N', '0', 'admin', sysdate(), '内部审计发现问题'),
 (4, '专项检查',     'special',    'jc_clue_source', '', 'primary', 'N', '0', 'admin', sysdate(), '专项检查发现问题'),
 (5, '合规检查',     'compliance', 'jc_clue_source', '', 'info',    'N', '0', 'admin', sysdate(), '合规检查发现问题'),
 (6, '上级单位交办', 'superior',   'jc_clue_source', '', 'info',    'N', '0', 'admin', sysdate(), '上级单位/集团交办'),
 (7, 'AI预警',       'ai',         'jc_clue_source', '', 'danger',  'N', '0', 'admin', sysdate(), '风险预警引擎自动生成');

-- 问题类型（企业版，替换原党政六大纪律）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '商业贿赂',     '1', 'jc_violation_type', '', 'danger',  'Y', '0', 'admin', sysdate(), '收受回扣、好处费等'),
 (2, '职务侵占',     '2', 'jc_violation_type', '', 'danger',  'N', '0', 'admin', sysdate(), '侵占公司财物'),
 (3, '利益输送',     '3', 'jc_violation_type', '', 'danger',  'N', '0', 'admin', sysdate(), '为关联方输送利益'),
 (4, '采购舞弊',     '4', 'jc_violation_type', '', 'warning', 'N', '0', 'admin', sysdate(), '围标串标、虚假采购等'),
 (5, '财务违规',     '5', 'jc_violation_type', '', 'warning', 'N', '0', 'admin', sysdate(), '虚报冒领、费用异常'),
 (6, '滥用职权',     '6', 'jc_violation_type', '', 'warning', 'N', '0', 'admin', sysdate(), '越权审批、违规决策'),
 (7, '失职渎职',     '7', 'jc_violation_type', '', 'info',    'N', '0', 'admin', sysdate(), '造成公司损失'),
 (8, '违反公司制度', '8', 'jc_violation_type', '', 'info',    'N', '0', 'admin', sysdate(), '违反内部管理制度');

-- 办理状态（value 为数字码，与后端状态机一致）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '待受理',   '0', 'jc_clue_status', '', 'info',    'Y', '0', 'admin', sysdate(), '新登记待受理'),
 (2, '已受理',   '1', 'jc_clue_status', '', 'primary', 'N', '0', 'admin', sysdate(), '已受理待研判'),
 (3, '核查中',   '2', 'jc_clue_status', '', 'warning', 'N', '0', 'admin', sysdate(), '研判/分办后核查'),
 (4, '处置中',   '3', 'jc_clue_status', '', 'warning', 'N', '0', 'admin', sysdate(), '核查属实进入处置'),
 (5, '已办结',   '4', 'jc_clue_status', '', 'success', 'N', '0', 'admin', sysdate(), '办理完毕'),
 (6, '不予受理', '5', 'jc_clue_status', '', 'danger',  'N', '0', 'admin', sysdate(), '不属受理范围');

-- 办理动作
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '登记',     'register', 'jc_clue_action', '', 'info',    'Y', '0', 'admin', sysdate(), '线索录入'),
 (2, '受理',     'accept',   'jc_clue_action', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
 (3, '研判',     'review',   'jc_clue_action', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
 (4, '分办',     'assign',   'jc_clue_action', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
 (5, '处置',     'dispose',  'jc_clue_action', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
 (6, '办结',     'close',    'jc_clue_action', '', 'success', 'N', '0', 'admin', sysdate(), ''),
 (7, '不予受理', 'reject',   'jc_clue_action', '', 'danger',  'N', '0', 'admin', sysdate(), ''),
 (8, '退回',     'return',   'jc_clue_action', '', 'danger',  'N', '0', 'admin', sysdate(), '');

-- 处置方式
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '澄清了结', '1', 'jc_clue_disposition', '', 'info',    'Y', '0', 'admin', sysdate(), '核查失实予以澄清'),
 (2, '谈话提醒', '2', 'jc_clue_disposition', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
 (3, '批评教育', '3', 'jc_clue_disposition', '', 'primary', 'N', '0', 'admin', sysdate(), ''),
 (4, '责令整改', '4', 'jc_clue_disposition', '', 'warning', 'N', '0', 'admin', sysdate(), ''),
 (5, '内部处分', '5', 'jc_clue_disposition', '', 'danger',  'N', '0', 'admin', sysdate(), '警告/记过/降级/撤职/开除'),
 (6, '经济处理', '6', 'jc_clue_disposition', '', 'danger',  'N', '0', 'admin', sysdate(), '扣减绩效、追缴损失'),
 (7, '移交司法', '7', 'jc_clue_disposition', '', 'danger',  'N', '0', 'admin', sysdate(), '涉嫌犯罪移送司法机关'),
 (8, '其他',     '8', 'jc_clue_disposition', '', 'info',    'N', '0', 'admin', sysdate(), '');

-- ============================================================
-- 3. 菜单与权限（挂载于「纪检监察」目录 2000 下）
-- ============================================================
DELETE FROM sys_menu WHERE menu_id BETWEEN 2100 AND 2199;

INSERT INTO sys_menu VALUES (2101, '问题线索', '2000', '3', 'clue', 'jiancha/clue/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:clue:list', 'list', 'admin', sysdate(), '', null, '问题线索管理菜单');
INSERT INTO sys_menu VALUES (2102, '线索查询', '2101', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:clue:query',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2103, '线索新增', '2101', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:clue:add',    '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2104, '线索修改', '2101', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:clue:edit',   '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2105, '线索删除', '2101', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:clue:remove', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2106, '线索流转', '2101', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:clue:handle', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2107, '线索导出', '2101', '6', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:clue:export', '#', 'admin', sysdate(), '', null, '');

-- ============================================================
-- 4. 业务参数（办理时限一律走配置，代码不硬编码）
-- ============================================================
DELETE FROM sys_config WHERE config_key LIKE 'jiancha.clue.%';

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark) VALUES
 ('线索办理时限-默认',   'jiancha.clue.deadline.default',    '30',  'N', 'admin', sysdate(), '来源未单独配置时的默认办理时限(日)'),
 ('线索办理时限-员工举报','jiancha.clue.deadline.report',     '30',  'N', 'admin', sysdate(), '员工举报办理时限(日)'),
 ('线索办理时限-匿名举报','jiancha.clue.deadline.anonymous',  '30',  'N', 'admin', sysdate(), '匿名举报办理时限(日)'),
 ('线索办理时限-内部审计','jiancha.clue.deadline.audit',      '20',  'N', 'admin', sysdate(), '内部审计移交线索办理时限(日)'),
 ('线索办理时限-专项检查','jiancha.clue.deadline.special',    '15',  'N', 'admin', sysdate(), '专项检查发现线索办理时限(日)'),
 ('线索办理时限-合规检查','jiancha.clue.deadline.compliance', '15',  'N', 'admin', sysdate(), '合规检查发现线索办理时限(日)'),
 ('线索办理时限-上级交办','jiancha.clue.deadline.superior',   '30',  'N', 'admin', sysdate(), '上级单位交办线索办理时限(日)，按交办要求调整'),
 ('线索办理时限-AI预警', 'jiancha.clue.deadline.ai',         '10',  'N', 'admin', sysdate(), 'AI预警生成线索办理时限(日)'),
 ('线索黄灯阈值比例',    'jiancha.clue.warn.ratio',          '0.5', 'N', 'admin', sysdate(), '已用时长达到时限的比例即亮黄灯，默认0.5'),
 ('线索重复判定窗口(日)', 'jiancha.clue.duplicate.days',      '90',  'N', 'admin', sysdate(), '同一被反映人在该天数内的多条线索提示疑似重复');

-- ============================================================
-- 5. 预警定时任务（默认暂停，可在「系统监控 → 定时任务」启用）
-- ============================================================
DELETE FROM sys_job WHERE invoke_target = 'jcClueWarnTask.refreshWarnLevel()';

INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
VALUES ('线索时限预警扫描', 'DEFAULT', 'jcClueWarnTask.refreshWarnLevel()', '0 0 8 * * ?', '3', '1', '1', 'admin', sysdate(), '每日8:00扫描未办结线索并刷新红黄灯（默认暂停）');

-- ============================================================
-- 6. 执行结果自检
-- ============================================================
SELECT '线索表' AS item, COUNT(1) AS cnt FROM information_schema.TABLES
 WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME IN ('jc_clue','jc_clue_trace')
UNION ALL SELECT '线索字典项', COUNT(1) FROM sys_dict_data WHERE dict_type LIKE 'jc_clue%' OR dict_type = 'jc_violation_type'
UNION ALL SELECT '线索菜单', COUNT(1) FROM sys_menu WHERE menu_id BETWEEN 2100 AND 2199
UNION ALL SELECT '线索参数', COUNT(1) FROM sys_config WHERE config_key LIKE 'jiancha.clue.%'
UNION ALL SELECT '预警任务', COUNT(1) FROM sys_job WHERE invoke_target = 'jcClueWarnTask.refreshWarnLevel()';

-- ------------------------------------------------------------
-- 【已有数据时改用以下 ALTER，避免 DROP 丢数据】
-- ALTER TABLE jc_clue ADD COLUMN disposition VARCHAR(32) DEFAULT '' COMMENT '处置方式(字典 jc_clue_disposition)' AFTER status;
-- ALTER TABLE jc_clue MODIFY COLUMN source_type VARCHAR(32) NOT NULL COMMENT '来源类型(字典 jc_clue_source)';
-- ------------------------------------------------------------
