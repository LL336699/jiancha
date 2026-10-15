-- ============================================================
-- 纪检监察助手 · 段10：作风建设专项（E5）
-- 菜单段位 3100-3199；表 jc_conduct_issue
-- 前提：jiancha_init.sql / jiancha_menu.sql 已导入
-- 闭环：登记 → 核查 → 处置（退缴/谈话/诫勉/处分）→ 转整改/转处分 → 关闭
-- ============================================================

-- ------------------------------------------------------------
-- 1. 菜单（先删后插，幂等）
-- ------------------------------------------------------------
DELETE FROM sys_menu WHERE menu_id BETWEEN 3100 AND 3199;

INSERT INTO sys_menu VALUES (3100, '作风建设专项', '2000', '10', 'conduct', null, '', '', 1, 0, 'M', '0', '0', '', 'peoples', 'admin', sysdate(), '', null, '作风建设问题台账（E5）：违规吃喝/收送礼品礼金/公车私用等中央八项规定精神问题专项治理');

INSERT INTO sys_menu VALUES (3101, '作风问题办理', '3100', '1', 'conduct', 'jiancha/conduct/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:conduct:list', 'peoples', 'admin', sysdate(), '', null, '登记 → 核查 → 处置 → 转整改/转处分 → 关闭');

INSERT INTO sys_menu VALUES (3111, '问题查询', '3101', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:conduct:list',   '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (3112, '问题详情', '3101', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:conduct:query',  '#', 'admin', sysdate(), '', null, '含转整改关联状态');
INSERT INTO sys_menu VALUES (3113, '问题登记', '3101', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:conduct:add',    '#', 'admin', sysdate(), '', null, '自动编号 CF+日期+序号，档案快照姓名/部门');
INSERT INTO sys_menu VALUES (3114, '登记修改', '3101', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:conduct:edit',   '#', 'admin', sysdate(), '', null, '仅待核查状态');
INSERT INTO sys_menu VALUES (3115, '核查登记', '3101', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:conduct:verify', '#', 'admin', sysdate(), '', null, '0→1：核查情况/涉及金额/严重程度');
INSERT INTO sys_menu VALUES (3116, '处置登记', '3101', '6', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:conduct:handle', '#', 'admin', sysdate(), '', null, '1→2：谈话/诫勉/处分/移送，退缴金额留痕');
INSERT INTO sys_menu VALUES (3117, '转整改',   '3101', '7', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:conduct:transfer','#', 'admin', sysdate(), '', null, '关联 E3 整改台账，一问题只转一次');
INSERT INTO sys_menu VALUES (3118, '转处分',   '3101', '8', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:conduct:transfer','#', 'admin', sysdate(), '', null, '写干部处分记录 jc_person_punish');
INSERT INTO sys_menu VALUES (3119, '关闭问题', '3101', '9', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:conduct:close',  '#', 'admin', sysdate(), '', null, '0/1/2→3');
INSERT INTO sys_menu VALUES (3120, '作废问题', '3101', '10','', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:conduct:cancel', '#', 'admin', sysdate(), '', null, '0/1/2→4，误登记纠正');
INSERT INTO sys_menu VALUES (3121, '问题删除', '3101', '11','', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:conduct:remove', '#', 'admin', sysdate(), '', null, '仅待核查状态');
INSERT INTO sys_menu VALUES (3123, '退缴登记', '3101', '12','', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:conduct:recovery','#', 'admin', sysdate(), '', null, '多笔退缴明细，自动汇总主表退缴额');

INSERT INTO sys_menu VALUES (3102, '作风问题看板', '3100', '2', 'conductstat', 'jiancha/conduct/stat/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:conduct:stat', 'chart', 'admin', sysdate(), '', null, '问题量/退缴金额/类型分布/严重程度/来源/部门/处置方式/月度趋势');

INSERT INTO sys_menu VALUES (3122, '统计查询', '3102', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:conduct:stat', '#', 'admin', sysdate(), '', null, '');

-- ------------------------------------------------------------
-- 2. 授予超级管理员（role_id=1）全部新菜单（先清后授，保证幂等）
-- ------------------------------------------------------------
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 3100 AND 3199;

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 3100 AND 3199;

-- ------------------------------------------------------------
-- 3. 数据字典
-- ------------------------------------------------------------
DELETE FROM sys_dict_data WHERE dict_type IN
    ('jc_conduct_type','jc_conduct_source','jc_conduct_severity','jc_conduct_handle');
DELETE FROM sys_dict_type WHERE dict_type IN
    ('jc_conduct_type','jc_conduct_source','jc_conduct_severity','jc_conduct_handle');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
 ('作风问题类型', 'jc_conduct_type',     '0', 'admin', sysdate(), '中央八项规定精神（企业版口径）问题类型'),
 ('作风问题来源', 'jc_conduct_source',   '0', 'admin', sysdate(), '问题发现来源'),
 ('严重程度',     'jc_conduct_severity', '0', 'admin', sysdate(), '作风问题严重程度'),
 ('处置方式',     'jc_conduct_handle',   '0', 'admin', sysdate(), '处置方式（与后端 HANDLE_TYPES 常量一致）');

-- 问题类型（八类 + 其他）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '违规吃喝',     '违规吃喝',     'jc_conduct_type', '', 'danger',  'Y', '0', 'admin', sysdate(), '公款吃喝/接受管理对象宴请等'),
 (2, '违规收送礼品礼金', '违规收送礼品礼金', 'jc_conduct_type', '', 'danger',  'N', '0', 'admin', sysdate(), '收受或赠送礼品、礼金、消费卡等'),
 (3, '公车私用',     '公车私用',     'jc_conduct_type', '', 'warning', 'N', '0', 'admin', sysdate(), '公务用车私用/公车加油卡乱用等'),
 (4, '公款旅游',     '公款旅游',     'jc_conduct_type', '', 'warning', 'N', '0', 'admin', sysdate(), '借考察培训之名公款旅游'),
 (5, '大办婚丧喜庆', '大办婚丧喜庆', 'jc_conduct_type', '', 'warning', 'N', '0', 'admin', sysdate(), '大操大办婚丧喜庆借机敛财'),
 (6, '违规发放津补贴', '违规发放津补贴', 'jc_conduct_type', '', 'info',    'N', '0', 'admin', sysdate(), '超标准超范围发放津贴补贴奖金'),
 (7, '楼堂馆所违规', '楼堂馆所违规', 'jc_conduct_type', '', 'info',    'N', '0', 'admin', sysdate(), '违规兴建装修楼堂馆所'),
 (8, '其他',         '其他',         'jc_conduct_type', '', 'info',    'N', '0', 'admin', sysdate(), '其他作风建设问题');

-- 来源
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '自查自纠', 'self_check', 'jc_conduct_source', '', 'primary', 'Y', '0', 'admin', sysdate(), '单位/个人自查发现'),
 (2, '监督检查', 'check',      'jc_conduct_source', '', 'success', 'N', '0', 'admin', sysdate(), '纪检监督检查发现'),
 (3, '审计移交', 'audit',      'jc_conduct_source', '', 'warning', 'N', '0', 'admin', sysdate(), '内外部审计移交'),
 (4, '举报反映', 'petition',   'jc_conduct_source', '', 'danger',  'N', '0', 'admin', sysdate(), '群众举报反映'),
 (5, '线索延伸', 'clue',       'jc_conduct_source', '', 'info',    'N', '0', 'admin', sysdate(), '问题线索核查延伸发现');

-- 严重程度
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '一般', '一般', 'jc_conduct_severity', '', 'info',    'Y', '0', 'admin', sysdate(), '情节较轻'),
 (2, '较大', '较大', 'jc_conduct_severity', '', 'warning', 'N', '0', 'admin', sysdate(), '情节较重'),
 (3, '重大', '重大', 'jc_conduct_severity', '', 'danger',  'N', '0', 'admin', sysdate(), '情节严重');

-- 处置方式
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '提醒谈话',   '提醒谈话',   'jc_conduct_handle', '', 'info',    'Y', '0', 'admin', sysdate(), '咬耳扯袖、红脸出汗'),
 (2, '诫勉谈话',   '诫勉谈话',   'jc_conduct_handle', '', 'warning', 'N', '0', 'admin', sysdate(), '诫勉处理'),
 (3, '组织处理',   '组织处理',   'jc_conduct_handle', '', 'warning', 'N', '0', 'admin', sysdate(), '调整岗位/取消评优等组织措施'),
 (4, '纪律处分',   '纪律处分',   'jc_conduct_handle', '', 'danger',  'N', '0', 'admin', sysdate(), '给予企业内部分档处分（转处分）'),
 (5, '移送司法',   '移送司法',   'jc_conduct_handle', '', 'danger',  'N', '0', 'admin', sysdate(), '涉嫌违法犯罪移送');

-- ------------------------------------------------------------
-- 4. 业务参数
-- ------------------------------------------------------------
DELETE FROM sys_config WHERE config_key LIKE 'jiancha.conduct.%';

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark) VALUES
 ('作风问题默认核查期限(天)', 'jiancha.conduct.checkDays', '30', 'Y', 'admin', sysdate(), '登记日期+该天数=核查截止，驱动红黄灯（超期红灯/临期黄灯）');

-- ------------------------------------------------------------
-- 5. 红黄灯日刷新定时任务（默认暂停，可在「系统监控 → 定时任务」启用）
-- ------------------------------------------------------------
DELETE FROM sys_job WHERE invoke_target = 'jcConductScanTask.run()';

INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
VALUES ('作风建设红黄灯刷新', 'DEFAULT', 'jcConductScanTask.run()', '0 55 7 * * ?', '3', '1', '1', 'admin', sysdate(), '每日7:55：按核查期限刷新红黄灯（超期=红灯，临期=黄灯，默认暂停）');

-- ------------------------------------------------------------
-- 6. 业务表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS jc_conduct_recovery;
DROP TABLE IF EXISTS jc_conduct_issue;

-- 作风建设问题台账主表
CREATE TABLE jc_conduct_issue (
    issue_id        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '问题ID',
    issue_no        VARCHAR(32)  NOT NULL                COMMENT '问题编号(CF+yyyyMMdd+seq)',
    person_id       BIGINT       NOT NULL                COMMENT '问题人员ID(jc_person.person_id)',
    person_name     VARCHAR(64)  DEFAULT ''              COMMENT '问题人员姓名(登记时档案快照)',
    person_dept     VARCHAR(100) DEFAULT ''              COMMENT '问题人员部门(登记时档案快照)',
    violation_type  VARCHAR(32)  NOT NULL                COMMENT '问题类型(字典 jc_conduct_type)',
    violation_date  DATE         DEFAULT NULL            COMMENT '问题发生日期',
    violation_desc  VARCHAR(2000) DEFAULT ''             COMMENT '问题描述/核查情况说明',
    amount          DECIMAL(12,2) DEFAULT NULL           COMMENT '涉及金额/礼品价值(元)',
    amount_recovered DECIMAL(12,2) DEFAULT NULL          COMMENT '退缴金额(元)',
    source_type     VARCHAR(32)  DEFAULT 'self_check'    COMMENT '来源类型(字典 jc_conduct_source)',
    severity        VARCHAR(16)  DEFAULT '一般'           COMMENT '严重程度(字典 jc_conduct_severity)',
    check_deadline  DATE         DEFAULT NULL            COMMENT '核查截止日期(登记日+checkDays，红黄灯口径)',
    status          CHAR(1)      DEFAULT '0'             COMMENT '状态(0待核查 1已核查待处理 2已处理 3已关闭 4已作废)',
    warn_level      VARCHAR(8)   DEFAULT ''              COMMENT '预警级别(空/黄灯/红灯，Quartz日刷新)',
    handle_type     VARCHAR(32)  DEFAULT ''              COMMENT '处置方式(字典 jc_conduct_handle)',
    handle_date     DATE         DEFAULT NULL            COMMENT '处置日期',
    handle_desc     VARCHAR(1000) DEFAULT ''             COMMENT '处置说明',
    punish_id       BIGINT       DEFAULT NULL            COMMENT '转处分关联(jc_person_punish.punish_id)',
    rectify_id      BIGINT       DEFAULT NULL            COMMENT '转整改关联(jc_rectify_issue.issue_id)',
    close_time      DATETIME     DEFAULT NULL            COMMENT '关闭时间',
    remark          VARCHAR(500) DEFAULT ''              COMMENT '备注',
    create_by       VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time     DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time     DATETIME     DEFAULT NULL            COMMENT '更新时间',
    del_flag        CHAR(1)      DEFAULT '0'             COMMENT '删除标记(0正常 1删除)',
    PRIMARY KEY (issue_id),
    UNIQUE KEY uk_conduct_no (issue_no),
    KEY idx_conduct_person (person_id),
    KEY idx_conduct_status (status),
    KEY idx_conduct_type (violation_type),
    KEY idx_conduct_date (violation_date)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='作风建设问题台账';

-- 退缴明细表（一案多笔退缴，主表 amount_recovered 由明细汇总）
CREATE TABLE jc_conduct_recovery (
    recovery_id   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '退缴明细ID',
    issue_id      BIGINT       NOT NULL                COMMENT '问题ID(jc_conduct_issue.issue_id)',
    item_name     VARCHAR(200) NOT NULL                COMMENT '退缴物品名称',
    item_qty      INT          DEFAULT 1               COMMENT '数量',
    unit          VARCHAR(20)  DEFAULT ''              COMMENT '单位(张/盒/条/件等)',
    amount        DECIMAL(12,2) DEFAULT NULL           COMMENT '金额(元，单笔)',
    recovery_date DATE         DEFAULT NULL            COMMENT '退缴日期',
    receiver      VARCHAR(64)  DEFAULT ''              COMMENT '接收人(纪检收缴登记人)',
    remark        VARCHAR(500) DEFAULT ''              COMMENT '备注',
    create_by     VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time   DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by     VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time   DATETIME     DEFAULT NULL            COMMENT '更新时间',
    del_flag      CHAR(1)      DEFAULT '0'             COMMENT '删除标记(0正常 1删除)',
    PRIMARY KEY (recovery_id),
    KEY idx_recovery_issue (issue_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='作风建设问题·退缴明细';
