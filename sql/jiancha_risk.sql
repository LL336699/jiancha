-- ============================================================
-- 纪检监察助手 · 廉政风险防控模块 SQL（M10 风险防控，企业版口径）
-- 追加执行，可重复运行（表结构 DROP+CREATE 会清空数据；有数据时改用文末 ALTER 方案）
-- 前提：jiancha_init.sql 已导入（sys_menu / sys_role_menu / sys_dict_type / sys_dict_data 已存在）
-- 段位：2700-2799 专供廉政风险防控
-- ============================================================

-- ------------------------------------------------------------
-- 1. 清理旧菜单（幂等：2700-2799 专供风险防控）
-- ------------------------------------------------------------
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 2700 AND 2799;
DELETE FROM sys_menu      WHERE menu_id BETWEEN 2700 AND 2799;

-- ------------------------------------------------------------
-- 2. 风险防控目录 + 菜单
--    2700 风险防控（目录，挂在纪检监察 2000 下，排序 8）
--    2701 预警管理  2702 规则配置  2703 岗位模型  2704 风险看板
-- ------------------------------------------------------------
INSERT INTO sys_menu VALUES (2700, '风险防控', '2000', '8', 'risk', null, '', '', 1, 0, 'M', '0', '0', '', 'chart', 'admin', sysdate(), '', null, '廉政风险防控业务目录（M10）');

-- 2701 预警管理
INSERT INTO sys_menu VALUES (2701, '预警管理', '2700', '1', 'riskalert', 'jiancha/risk/alert/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:risk:list', 'list', 'admin', sysdate(), '', null, '预警核实闭环：确认转整改/排除误报/归档');
INSERT INTO sys_menu VALUES (2711, '预警查询', '2701', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:risk:list',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2712, '预警详情', '2701', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:risk:query', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2713, '手动登记', '2701', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:risk:add',   '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2714, '预警核实', '2701', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:risk:verify','#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2715, '触发扫描', '2701', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:risk:scan',  '#', 'admin', sysdate(), '', null, '手动跑一遍规则引擎');

-- 2702 规则配置
INSERT INTO sys_menu VALUES (2702, '规则配置', '2700', '2', 'riskrule', 'jiancha/risk/rule/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:risk:rule:list', 'table', 'admin', sysdate(), '', null, '规则与阈值可配，不写死算法');
INSERT INTO sys_menu VALUES (2721, '规则查询', '2702', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:risk:rule:list',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2722, '规则新增', '2702', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:risk:rule:add',   '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2723, '规则修改', '2702', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:risk:rule:edit',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2724, '规则删除', '2702', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:risk:rule:remove','#', 'admin', sysdate(), '', null, '');

-- 2703 岗位模型
INSERT INTO sys_menu VALUES (2703, '岗位模型', '2700', '3', 'riskmodel', 'jiancha/risk/model/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:risk:model:list', 'table', 'admin', sysdate(), '', null, '按岗位配置风险因子与权重');
INSERT INTO sys_menu VALUES (2731, '模型查询', '2703', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:risk:model:list',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2732, '模型新增', '2703', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:risk:model:add',   '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2733, '模型修改', '2703', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:risk:model:edit',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2734, '模型删除', '2703', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:risk:model:remove','#', 'admin', sysdate(), '', null, '');

-- 2704 风险看板
INSERT INTO sys_menu VALUES (2704, '风险看板', '2700', '4', 'riskstat', 'jiancha/risk/stat/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:risk:stat', 'chart', 'admin', sysdate(), '', null, '三级风险视图（部门/岗位/个人）+ 趋势');
INSERT INTO sys_menu VALUES (2741, '统计查询', '2704', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:risk:stat', '#', 'admin', sysdate(), '', null, '');

-- ------------------------------------------------------------
-- 3. 授予超级管理员（role_id=1）全部新菜单
-- ------------------------------------------------------------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 2700 AND 2799;

-- ------------------------------------------------------------
-- 4. 数据字典
-- ------------------------------------------------------------
DELETE FROM sys_dict_data WHERE dict_type IN
    ('jc_risk_rule_type','jc_risk_alert_level','jc_risk_alert_status','jc_risk_object_type');
DELETE FROM sys_dict_type WHERE dict_type IN
    ('jc_risk_rule_type','jc_risk_alert_level','jc_risk_alert_status','jc_risk_object_type');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
 ('规则类型',   'jc_risk_rule_type',   '0', 'admin', sysdate(), '风险预警规则类型'),
 ('预警等级',   'jc_risk_alert_level', '0', 'admin', sysdate(), '风险预警等级'),
 ('预警状态',   'jc_risk_alert_status','0', 'admin', sysdate(), '风险预警闭环状态'),
 ('预警对象类型','jc_risk_object_type', '0', 'admin', sysdate(), '风险预警对象类型');

-- 规则类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '利益冲突', 'conflict',    'jc_risk_rule_type', '', 'danger',  'N', '0', 'admin', sysdate(), '利益冲突申报类'),
 (2, '举报集中', 'petition',    'jc_risk_rule_type', '', 'warning', 'N', '0', 'admin', sysdate(), '举报集中反映类'),
 (3, '整改闭环', 'rectify',     'jc_risk_rule_type', '', 'primary', 'N', '0', 'admin', sysdate(), '整改超期/复发类'),
 (4, '线索办理', 'clue',        'jc_risk_rule_type', '', 'info',    'N', '0', 'admin', sysdate(), '线索超期类'),
 (5, '岗位模型', 'position',    'jc_risk_rule_type', '', 'success', 'N', '0', 'admin', sysdate(), '岗位风险模型类'),
 (6, '其他',     'other',       'jc_risk_rule_type', '', 'info',    'N', '0', 'admin', sysdate(), '其他');

-- 预警等级
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '低', '0', 'jc_risk_alert_level', '', 'info',    'Y', '0', 'admin', sysdate(), '关注即可'),
 (2, '中', '1', 'jc_risk_alert_level', '', 'warning', 'N', '0', 'admin', sysdate(), '需核实'),
 (3, '高', '2', 'jc_risk_alert_level', '', 'danger',  'N', '0', 'admin', sysdate(), '优先核实');

-- 预警状态（与后端状态机一致）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '待核实',     '0', 'jc_risk_alert_status', '', 'info',    'Y', '0', 'admin', sysdate(), '引擎生成待人工核实'),
 (2, '核实中',     '1', 'jc_risk_alert_status', '', 'primary', 'N', '0', 'admin', sysdate(), '人工核实中'),
 (3, '已确认转整改','2', 'jc_risk_alert_status', '', 'warning', 'N', '0', 'admin', sysdate(), '确认并转 E3 整改'),
 (4, '已排除误报', '3', 'jc_risk_alert_status', '', 'success', 'N', '0', 'admin', sysdate(), '核实为误报'),
 (5, '已归档',     '4', 'jc_risk_alert_status', '', 'success', 'N', '0', 'admin', sysdate(), '终结');

-- 预警对象类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '人员', '0', 'jc_risk_object_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '自然人'),
 (2, '部门', '1', 'jc_risk_object_type', '', 'info',    'N', '0', 'admin', sysdate(), '责任部门'),
 (3, '岗位', '2', 'jc_risk_object_type', '', 'success', 'N', '0', 'admin', sysdate(), '岗位（模型层）');

-- ------------------------------------------------------------
-- 5. 业务参数（统计窗口可配，与举报重复窗口同思路）
-- ------------------------------------------------------------
DELETE FROM sys_config WHERE config_key LIKE 'jiancha.risk.%';

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark) VALUES
 ('举报集中统计窗口(日)', 'jiancha.risk.window.days', '90', 'N', 'admin', sysdate(), 'R01 规则统计同一被举报对象的举报次数所用的时间窗(日)');

-- ------------------------------------------------------------
-- 6. 扫描定时任务（默认暂停，可在「系统监控 → 定时任务」启用）
-- ------------------------------------------------------------
DELETE FROM sys_job WHERE invoke_target = 'jcRiskScanTask.scan()';

INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
VALUES ('风险预警引擎扫描', 'DEFAULT', 'jcRiskScanTask.scan()', '0 10 8 * * ?', '3', '1', '1', 'admin', sysdate(), '每日8:10跑规则引擎并落警，去重后生成未闭环预警（默认暂停）');

-- ------------------------------------------------------------
-- 7. 业务表 + 初始规则与岗位模型
-- ------------------------------------------------------------
DROP TABLE IF EXISTS jc_risk_alert;
DROP TABLE IF EXISTS jc_risk_model;
DROP TABLE IF EXISTS jc_risk_rule;

-- 规则表
CREATE TABLE jc_risk_rule (
    rule_id        BIGINT        NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    rule_code      VARCHAR(32)   NOT NULL                COMMENT '规则编码(R01-R12，与引擎分支对应)',
    rule_name      VARCHAR(100)  NOT NULL                COMMENT '规则名称',
    rule_type      VARCHAR(32)   DEFAULT ''              COMMENT '规则类型(字典 jc_risk_rule_type)',
    description    VARCHAR(500)  DEFAULT ''              COMMENT '规则说明(命中逻辑)',
    threshold      DECIMAL(10,2) DEFAULT '1'             COMMENT '阈值(次/条/分)',
    threshold_unit VARCHAR(16)   DEFAULT ''              COMMENT '阈值单位',
    alert_level    CHAR(1)       DEFAULT '1'             COMMENT '命中后预警等级(0低 1中 2高)',
    status         CHAR(1)       DEFAULT '0'             COMMENT '状态(0启用 1停用)',
    remark         VARCHAR(500)  DEFAULT ''              COMMENT '备注',
    create_by      VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
    create_time    DATETIME      DEFAULT NULL            COMMENT '创建时间',
    update_by      VARCHAR(64)   DEFAULT ''              COMMENT '更新者',
    update_time    DATETIME      DEFAULT NULL            COMMENT '更新时间',
    PRIMARY KEY (rule_id),
    UNIQUE KEY uk_risk_rule_code (rule_code)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='风险预警规则';

-- 预警记录表
CREATE TABLE jc_risk_alert (
    alert_id       BIGINT        NOT NULL AUTO_INCREMENT COMMENT '预警ID',
    alert_no       VARCHAR(32)   NOT NULL                COMMENT '预警编号(RA+yyyyMMdd+seq)',
    rule_id        BIGINT        NOT NULL                COMMENT '命中规则ID',
    rule_code      VARCHAR(32)   DEFAULT ''              COMMENT '命中规则编码(冗余)',
    rule_name      VARCHAR(100)  DEFAULT ''              COMMENT '命中规则名称(冗余)',
    object_type    CHAR(1)       DEFAULT '0'             COMMENT '预警对象类型(0人员 1部门 2岗位)',
    object_id      BIGINT        DEFAULT NULL            COMMENT '预警对象ID(person_id/dept_id，岗位类为空)',
    object_name    VARCHAR(100)  DEFAULT ''              COMMENT '预警对象名称(冗余)',
    alert_level    CHAR(1)       DEFAULT '1'             COMMENT '预警等级(0低 1中 2高)',
    score          DECIMAL(5,2)  DEFAULT '0'             COMMENT '风险分(0-100)',
    evidence       VARCHAR(2000) DEFAULT ''              COMMENT '命中证据(含来源单号)',
    status         CHAR(1)       DEFAULT '0'             COMMENT '状态(0待核实 1核实中 2已确认转整改 3已排除误报 4已归档)',
    verify_opinion VARCHAR(1000) DEFAULT ''              COMMENT '核实意见',
    verify_by      VARCHAR(64)   DEFAULT ''              COMMENT '核实人',
    verify_time    DATETIME      DEFAULT NULL            COMMENT '核实时间',
    issue_id       BIGINT        DEFAULT NULL            COMMENT '转整改后关联的问题ID',
    remark         VARCHAR(500)  DEFAULT ''              COMMENT '备注',
    create_by      VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
    create_time    DATETIME      DEFAULT NULL            COMMENT '创建时间',
    update_by      VARCHAR(64)   DEFAULT ''              COMMENT '更新者',
    update_time    DATETIME      DEFAULT NULL            COMMENT '更新时间',
    del_flag       CHAR(1)       DEFAULT '0'             COMMENT '删除标记(0正常 1删除)',
    PRIMARY KEY (alert_id),
    UNIQUE KEY uk_risk_alert_no (alert_no),
    KEY idx_alert_rule (rule_id),
    KEY idx_alert_status (status),
    KEY idx_alert_issue (issue_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='风险预警记录';

-- 岗位风险模型表
CREATE TABLE jc_risk_model (
    model_id      BIGINT        NOT NULL AUTO_INCREMENT COMMENT '模型行ID',
    position_name VARCHAR(100)  NOT NULL                COMMENT '岗位名称',
    factor_code   VARCHAR(32)   DEFAULT ''              COMMENT '因子编码(F01-F12)',
    factor_name   VARCHAR(100)  DEFAULT ''              COMMENT '因子名称',
    weight        DECIMAL(5,2)  DEFAULT '0'             COMMENT '因子权重(0-100)',
    description   VARCHAR(500)  DEFAULT ''              COMMENT '因子说明',
    status        CHAR(1)       DEFAULT '0'             COMMENT '状态(0启用 1停用)',
    remark        VARCHAR(500)  DEFAULT ''              COMMENT '备注',
    create_by     VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
    create_time   DATETIME      DEFAULT NULL            COMMENT '创建时间',
    PRIMARY KEY (model_id),
    KEY idx_model_position (position_name)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='岗位风险模型';

-- 初始规则（覆盖企业版规则池中当前数据可支撑的 6 条）
INSERT INTO jc_risk_rule (rule_code, rule_name, rule_type, description, threshold, threshold_unit, alert_level, status, remark, create_by, create_time) VALUES
 ('R01', '举报集中反映',   'petition', '统计窗口内同一被举报对象的举报次数达到阈值', 2, '次',  '1', '0', '设计文档规则池:被举报集中反映', 'admin', sysdate()),
 ('R02', '整改超期',       'rectify',  '责任部门超期未销号的整改问题数达到阈值',     1, '条',  '1', '0', '设计文档规则池:整改不及时',     'admin', sysdate()),
 ('R03', '整改复发',       'rectify',  '责任部门整改回头看不通过次数达到阈值',       1, '次',  '2', '0', '设计文档规则池:整改不实',       'admin', sysdate()),
 ('R04', '线索超期未办结', 'clue',     '承办部门超期未办结线索数达到阈值',           2, '条',  '1', '0', '设计文档规则池:办理超期',       'admin', sysdate()),
 ('R05', '利益冲突申报存疑','conflict', '人员利益冲突申报存疑笔数达到阈值',           1, '笔',  '1', '0', '设计文档规则池:申报矛盾/漏报',  'admin', sysdate()),
 ('R06', '岗位高风险',     'position', '岗位风险模型权重合计达到阈值',               60, '分', '2', '0', '设计文档§M10:岗位风险模型',     'admin', sysdate());

-- 初始岗位模型（采购经理 / 招投标专员，合计均超 R06 阈值 60）
INSERT INTO jc_risk_model (position_name, factor_code, factor_name, weight, description, status, remark, create_by, create_time) VALUES
 ('采购经理',   'F01', '供应商接触度', 30, '直接接触供应商的频度与主导权', '0', '', 'admin', sysdate()),
 ('采购经理',   'F02', '采购决策权',   30, '采购立项/定标决策权',           '0', '', 'admin', sysdate()),
 ('采购经理',   'F03', '资金审批权',   25, '付款审批权',                     '0', '', 'admin', sysdate()),
 ('采购经理',   'F04', '合同变更权',   15, '合同变更主导权',                 '0', '', 'admin', sysdate()),
 ('招投标专员', 'F01', '供应商接触度', 30, '直接接触供应商的频度与主导权', '0', '', 'admin', sysdate()),
 ('招投标专员', 'F05', '评标参与度',   25, '参与评标程度',                   '0', '', 'admin', sysdate()),
 ('招投标专员', 'F06', '文件保管权',   20, '招投标文件保管权',               '0', '', 'admin', sysdate());

-- ------------------------------------------------------------
-- 8. 自检
-- ------------------------------------------------------------
SELECT '风控菜单'   AS item, COUNT(1) AS cnt FROM sys_menu      WHERE menu_id BETWEEN 2700 AND 2799
UNION ALL SELECT '风控字典项', COUNT(1) FROM sys_dict_data WHERE dict_type LIKE 'jc_risk%'
UNION ALL SELECT '风控参数',   COUNT(1) FROM sys_config   WHERE config_key LIKE 'jiancha.risk.%'
UNION ALL SELECT '风控任务',   COUNT(1) FROM sys_job      WHERE invoke_target = 'jcRiskScanTask.scan()'
UNION ALL SELECT '风控规则',   COUNT(1) FROM jc_risk_rule
UNION ALL SELECT '岗位模型行', COUNT(1) FROM jc_risk_model
UNION ALL SELECT '风控表',     COUNT(1) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name IN ('jc_risk_rule','jc_risk_alert','jc_risk_model');

-- ------------------------------------------------------------
-- 【已有数据时改用以下 ALTER，避免 DROP 丢数据】
-- ALTER TABLE jc_risk_alert ADD COLUMN issue_id BIGINT DEFAULT NULL COMMENT '转整改后关联的问题ID' AFTER verify_time;
-- ------------------------------------------------------------
