-- ============================================================
-- 纪检监察助手 · 舞弊举报受理模块 SQL（M3 信访/检举举报，企业版口径）
-- 追加执行，可重复运行（表结构 DROP+CREATE 会清空数据；有数据时改用文末 ALTER 方案）
-- 前提：jiancha_init.sql 已导入（sys_menu / sys_role_menu / sys_dict_type / sys_dict_data 已存在）
-- 段位：2500-2599 专供舞弊举报受理
-- ============================================================

-- ------------------------------------------------------------
-- 1. 清理旧菜单（幂等：2500-2599 专供举报受理）
-- ------------------------------------------------------------
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 2500 AND 2599;
DELETE FROM sys_menu      WHERE menu_id BETWEEN 2500 AND 2599;

-- ------------------------------------------------------------
-- 2. 举报受理目录 + 菜单
--    2500 举报受理（目录，挂在纪检监察 2000 下，排序 6）
--    2501 举报箱管理  2502 举报箱（举报入口）  2503 办理统计
-- ------------------------------------------------------------
INSERT INTO sys_menu VALUES (2500, '举报受理', '2000', '6', 'petition', null, '', '', 1, 0, 'M', '0', '0', '', 'form', 'admin', sysdate(), '', null, '舞弊举报受理业务目录（M3）');

-- 2501 举报箱管理（管理端办理页）
INSERT INTO sys_menu VALUES (2501, '举报箱管理', '2500', '1', 'petitionmanage', 'jiancha/petition/manage/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:petition:list', 'list', 'admin', sysdate(), '', null, '举报列表/办理流转/分办转线索/解密查看');
INSERT INTO sys_menu VALUES (2511, '举报查询', '2501', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:petition:list',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2512, '举报详情', '2501', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:petition:query', '#', 'admin', sysdate(), '', null, '敏感字段解密+轨迹+材料，全程审计');
INSERT INTO sys_menu VALUES (2513, '办理流转', '2501', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:petition:action', '#', 'admin', sysdate(), '', null, '受理/研判/分办/办理/反馈/归档');
INSERT INTO sys_menu VALUES (2514, '举报删除', '2501', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:petition:remove', '#', 'admin', sysdate(), '', null, '级联清理轨迹与材料');

-- 2502 举报箱（举报入口，免登录前端页）
INSERT INTO sys_menu VALUES (2502, '举报箱', '2500', '2', 'petitionbox', 'jiancha/petition/box/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:petition:list', 'form', 'admin', sysdate(), '', null, '免登录举报入口（匿名/实名），后端 /public/petition/**');

-- 2503 办理统计
INSERT INTO sys_menu VALUES (2503, '办理统计', '2500', '3', 'petitionstat', 'jiancha/petition/stat/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:petition:stat', 'chart', 'admin', sysdate(), '', null, '来源/类型/状态/时效统计');
INSERT INTO sys_menu VALUES (2531, '统计查询', '2503', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:petition:stat', '#', 'admin', sysdate(), '', null, '');

-- ------------------------------------------------------------
-- 3. 授予超级管理员（role_id=1）全部新菜单
-- ------------------------------------------------------------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 2500 AND 2599;

-- ------------------------------------------------------------
-- 4. 数据字典
--    jc_petition_status    办理状态（与后端状态机一致）
--    jc_petition_category  舞弊类型（企业版）
--    jc_petition_channel   提交渠道
-- ------------------------------------------------------------
DELETE FROM sys_dict_data WHERE dict_type IN
    ('jc_petition_status','jc_petition_category','jc_petition_channel');
DELETE FROM sys_dict_type WHERE dict_type IN
    ('jc_petition_status','jc_petition_category','jc_petition_channel');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
 ('举报办理状态','jc_petition_status',   '0', 'admin', sysdate(), '舞弊举报流程状态（严格单向）'),
 ('舞弊类型',    'jc_petition_category', '0', 'admin', sysdate(), '企业版舞弊举报类型'),
 ('举报渠道',    'jc_petition_channel',  '0', 'admin', sysdate(), '举报提交渠道');

-- 办理状态（value 为数字码，与后端状态机一致；统计 SQL 按此 JOIN 映射中文）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '待受理', '0', 'jc_petition_status', '', 'info',    'Y', '0', 'admin', sysdate(), '举报提交成功'),
 (2, '已受理', '1', 'jc_petition_status', '', 'primary', 'N', '0', 'admin', sysdate(), '已受理，起算办理时限'),
 (3, '研判中', '2', 'jc_petition_status', '', 'warning', 'N', '0', 'admin', sysdate(), '研判是否分办'),
 (4, '已分办', '3', 'jc_petition_status', '', 'warning', 'N', '0', 'admin', sysdate(), '已转线索并指定承办部门'),
 (5, '办理中', '4', 'jc_petition_status', '', 'warning', 'N', '0', 'admin', sysdate(), '承办部门办理登记'),
 (6, '已反馈', '5', 'jc_petition_status', '', 'success', 'N', '0', 'admin', sysdate(), '已向举报人反馈'),
 (7, '已归档', '6', 'jc_petition_status', '', 'success', 'N', '0', 'admin', sysdate(), '终态'),
 (8, '不予受理','7', 'jc_petition_status', '', 'danger',  'N', '0', 'admin', sysdate(), '不属受理范围，终态');

-- 舞弊类型（value 与后端 CATEGORY_TO_VIOLATION 映射对应）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '采购回扣', '1', 'jc_petition_category', '', 'danger',  'Y', '0', 'admin', sysdate(), '收受回扣、好处费'),
 (2, '串标围标', '2', 'jc_petition_category', '', 'danger',  'N', '0', 'admin', sysdate(), '围标、串标、陪标'),
 (3, '虚报冒领', '3', 'jc_petition_category', '', 'warning', 'N', '0', 'admin', sysdate(), '虚假报销、冒领费用'),
 (4, '吃拿卡要', '4', 'jc_petition_category', '', 'warning', 'N', '0', 'admin', sysdate(), '办事刁难、索要好处'),
 (5, '利益冲突', '5', 'jc_petition_category', '', 'info',    'N', '0', 'admin', sysdate(), '关联交易、未申报利益冲突'),
 (6, '其他',     '6', 'jc_petition_category', '', 'info',    'N', '0', 'admin', sysdate(), '其他舞弊行为');

-- 提交渠道
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '网页举报箱', 'web',   'jc_petition_channel', '', 'primary', 'Y', '0', 'admin', sysdate(), '系统举报入口提交'),
 (2, '邮件转入',   'email', 'jc_petition_channel', '', 'info',    'N', '0', 'admin', sysdate(), '举报邮箱人工录入'),
 (3, '电话记录',   'phone', 'jc_petition_channel', '', 'info',    'N', '0', 'admin', sysdate(), '举报电话人工录入');

-- ------------------------------------------------------------
-- 5. 业务参数（时限/阈值/开关一律走配置，代码不硬编码）
-- ------------------------------------------------------------
DELETE FROM sys_config WHERE config_key LIKE 'jiancha.petition.%';

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark) VALUES
 ('举报办理时限(小时)', 'jiancha.petition.deadline.hours', '72', 'N', 'admin', sysdate(), '受理后起算的办理时限(小时)，默认72小时(3天)'),
 ('举报黄灯阈值比例',   'jiancha.petition.warn.ratio',     '0.5', 'N', 'admin', sysdate(), '已用时长达到时限的比例即亮黄灯，默认0.5'),
 ('举报箱验证码开关',   'jiancha.petition.captcha',        '0',  'N', 'admin', sysdate(), '1开启：免登录提交需验证码；0关闭'),
 ('举报重复判定窗口(日)','jiancha.petition.duplicate.days','90', 'N', 'admin', sysdate(), '同一被举报对象在该天数内的多条举报提示疑似重复');

-- ------------------------------------------------------------
-- 6. 预警定时任务（默认暂停，可在「系统监控 → 定时任务」启用）
-- ------------------------------------------------------------
DELETE FROM sys_job WHERE invoke_target = 'jcPetitionWarnTask.refreshWarnLevel()';

INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
VALUES ('举报时限预警扫描', 'DEFAULT', 'jcPetitionWarnTask.refreshWarnLevel()', '0 0 8 * * ?', '3', '1', '1', 'admin', sysdate(), '每日8:00扫描已受理未办结举报并刷新红黄灯（默认暂停）');

-- ------------------------------------------------------------
-- 7. 业务表
--    注意：del_flag 只作查询过滤，不映射进实体（BaseEntity 无该字段）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS jc_petition_material;
DROP TABLE IF EXISTS jc_petition_trace;
DROP TABLE IF EXISTS jc_petition;

-- 举报主表
CREATE TABLE jc_petition (
    petition_id      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '举报ID',
    receipt_lookup   CHAR(64)     NOT NULL                COMMENT '回执码检索哈希 SHA-256(码+pepper)，唯一索引',
    receipt_hash     VARCHAR(100) NOT NULL                COMMENT '回执码 BCrypt 哈希（索引命中后二次校验）',
    title            VARCHAR(200) NOT NULL                COMMENT '事由标题',
    content          LONGTEXT     NOT NULL                COMMENT '举报内容',
    channel          VARCHAR(32)  DEFAULT 'web'           COMMENT '提交渠道(字典 jc_petition_channel)',
    category         VARCHAR(32)  NOT NULL                COMMENT '舞弊类型(字典 jc_petition_category)',
    anonymous_flag   CHAR(1)      DEFAULT '1'             COMMENT '是否匿名(0实名 1匿名)',
    reporter_name    VARCHAR(200) DEFAULT ''              COMMENT '举报人姓名(AES-GCM密文)',
    reporter_phone   VARCHAR(200) DEFAULT ''              COMMENT '举报人电话(AES-GCM密文)',
    reporter_idcard  VARCHAR(200) DEFAULT ''              COMMENT '举报人身份证号(AES-GCM密文)',
    target_name      VARCHAR(100) DEFAULT ''              COMMENT '被举报对象姓名',
    target_dept      VARCHAR(100) DEFAULT ''              COMMENT '被举报对象所在部门',
    amount           DECIMAL(14,2) DEFAULT NULL           COMMENT '涉及金额(元)',
    status           CHAR(1)      DEFAULT '0'             COMMENT '办理状态(0待受理 1已受理 2研判中 3已分办 4办理中 5已反馈 6已归档 7不予受理)',
    warn_level       VARCHAR(8)   DEFAULT ''              COMMENT '预警级别(空/黄灯/红灯)',
    handle_deadline  DATETIME     DEFAULT NULL            COMMENT '办理截止时间(受理时按sys_config时限计算)',
    clue_id          BIGINT       DEFAULT NULL            COMMENT '分办转线索后关联的线索ID',
    feedback         VARCHAR(2000) DEFAULT ''             COMMENT '反馈内容(状态≥5时举报人凭回执码可见)',
    remark           VARCHAR(500) DEFAULT ''              COMMENT '备注',
    create_by        VARCHAR(64)  DEFAULT ''              COMMENT '创建者(举报人匿名/实名)',
    create_time      DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by        VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time      DATETIME     DEFAULT NULL            COMMENT '更新时间',
    del_flag         CHAR(1)      DEFAULT '0'             COMMENT '删除标记(0正常 1删除)',
    PRIMARY KEY (petition_id),
    UNIQUE KEY uk_petition_receipt (receipt_lookup),
    KEY idx_petition_status (status),
    KEY idx_petition_target (target_name),
    KEY idx_petition_clue (clue_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='舞弊举报主表';

-- 办理轨迹表
CREATE TABLE jc_petition_trace (
    trace_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '轨迹ID',
    petition_id  BIGINT       NOT NULL                COMMENT '举报ID',
    action       VARCHAR(32)  NOT NULL                COMMENT '动作(submit/accept/review/dispatch/handle/feedback/archive/reject)',
    action_desc  VARCHAR(500) DEFAULT ''              COMMENT '动作说明',
    from_status  VARCHAR(8)   DEFAULT ''              COMMENT '流转前状态',
    to_status    VARCHAR(8)   DEFAULT ''              COMMENT '流转后状态',
    operator     VARCHAR(64)  DEFAULT ''              COMMENT '操作人(免登录提交记为举报人)',
    create_time  DATETIME     DEFAULT NULL            COMMENT '流转时间',
    PRIMARY KEY (trace_id),
    KEY idx_trace_petition (petition_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='舞弊举报办理轨迹';

-- 补充材料表
CREATE TABLE jc_petition_material (
    material_id   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '材料ID',
    petition_id   BIGINT       NOT NULL                COMMENT '举报ID',
    file_name     VARCHAR(255) DEFAULT ''              COMMENT '原始文件名',
    file_path     VARCHAR(500) DEFAULT ''              COMMENT '落盘路径',
    file_size     BIGINT       DEFAULT NULL            COMMENT '文件大小(字节)',
    uploader_type CHAR(1)      DEFAULT '0'             COMMENT '上传方(0举报人 1管理端)',
    create_by     VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time   DATETIME     DEFAULT NULL            COMMENT '上传时间',
    PRIMARY KEY (material_id),
    KEY idx_material_petition (petition_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='舞弊举报补充材料';

-- ------------------------------------------------------------
-- 8. 自检
-- ------------------------------------------------------------
SELECT '举报菜单'   AS item, COUNT(1) AS cnt FROM sys_menu      WHERE menu_id BETWEEN 2500 AND 2599
UNION ALL SELECT '举报字典项', COUNT(1) FROM sys_dict_data WHERE dict_type LIKE 'jc_petition%'
UNION ALL SELECT '举报参数',   COUNT(1) FROM sys_config   WHERE config_key LIKE 'jiancha.petition.%'
UNION ALL SELECT '举报任务',   COUNT(1) FROM sys_job      WHERE invoke_target = 'jcPetitionWarnTask.refreshWarnLevel()'
UNION ALL SELECT '举报表',     COUNT(1) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name IN ('jc_petition','jc_petition_trace','jc_petition_material');

-- ------------------------------------------------------------
-- 【已有数据时改用以下 ALTER，避免 DROP 丢数据】
-- ALTER TABLE jc_petition ADD COLUMN clue_id BIGINT DEFAULT NULL COMMENT '分办转线索后关联的线索ID' AFTER handle_deadline;
-- ALTER TABLE jc_petition ADD COLUMN feedback VARCHAR(2000) DEFAULT '' COMMENT '反馈内容' AFTER clue_id;
-- ------------------------------------------------------------
