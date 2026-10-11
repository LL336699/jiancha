-- ============================================================
-- 纪检监察助手 · 文书辅助模块 SQL（M6 文书辅助）
-- 追加执行，可重复运行（先按段位清理再插入）
-- 前提：jiancha_init.sql 已导入（sys_menu / sys_role_menu / sys_dict_type / sys_dict_data 已存在）
-- 段位：2300-2399 专供文书辅助
-- ============================================================

-- ------------------------------------------------------------
-- 1. 清理旧菜单（幂等：2300-2399 专供文书辅助）
-- ------------------------------------------------------------
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 2300 AND 2399;
DELETE FROM sys_menu      WHERE menu_id BETWEEN 2300 AND 2399;

-- ------------------------------------------------------------
-- 2. 文书辅助目录 + 菜单
--    2300 文书辅助（目录，挂在纪检监察 2000 下，排序 5）
--    2301 文书生成  2302 生成记录
-- ------------------------------------------------------------
INSERT INTO sys_menu VALUES (2300, '文书辅助', '2000', '5', 'report', null, '', '', 1, 0, 'M', '0', '0', '', 'form', 'admin', sysdate(), '', null, '文书辅助业务目录（M6）');

-- 2301 文书生成
INSERT INTO sys_menu VALUES (2301, '文书生成', '2300', '1', 'reportgen', 'jiancha/report/generate/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:report:generate', 'edit', 'admin', sysdate(), '', null, '选模板→填要素→生成文书草稿+量纪建议');
INSERT INTO sys_menu VALUES (2311, '模板查询', '2301', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:report:template:list',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2312, '模板详情', '2301', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:report:template:query', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2313, '文书生成', '2301', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:report:generate',       '#', 'admin', sysdate(), '', null, '');

-- 2302 生成记录
INSERT INTO sys_menu VALUES (2302, '生成记录', '2300', '2', 'reportrec', 'jiancha/report/record/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:report:record:list', 'list', 'admin', sysdate(), '', null, '文书生成留痕（含操作人+时间，满足人工签字合规）');
INSERT INTO sys_menu VALUES (2321, '记录查询', '2302', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:report:record:query', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2322, '记录删除', '2302', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:report:record:remove', '#', 'admin', sysdate(), '', null, '');

-- ------------------------------------------------------------
-- 3. 授予超级管理员（role_id=1）全部新菜单
-- ------------------------------------------------------------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 2300 AND 2399;

-- ------------------------------------------------------------
-- 4. 业务表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS jc_report_template;
CREATE TABLE jc_report_template (
    template_id      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    doc_type         VARCHAR(50)  DEFAULT ''   COMMENT '文书类型：立案报告/笔录模板/调查终结报告',
    template_name    VARCHAR(100) DEFAULT ''   COMMENT '模板名称',
    template_content LONGTEXT              COMMENT '模板骨架（${变量}占位，生成时替换）',
    fields_meta      LONGTEXT              COMMENT '表单字段定义（JSON 数组）',
    status           CHAR(1)      DEFAULT '0' COMMENT '0启用 1停用',
    remark           VARCHAR(500) DEFAULT ''  COMMENT '备注',
    create_by        VARCHAR(64)  DEFAULT ''   COMMENT '创建者',
    del_flag         CHAR(1)      DEFAULT '0',
    create_time      DATETIME     DEFAULT NULL,
    update_time      DATETIME     DEFAULT NULL,
    PRIMARY KEY (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文书模板（M6）';

DROP TABLE IF EXISTS jc_report_record;
CREATE TABLE jc_report_record (
    record_id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    template_id       BIGINT       DEFAULT NULL COMMENT '模板ID',
    doc_type          VARCHAR(50)  DEFAULT ''   COMMENT '文书类型',
    title             VARCHAR(200) DEFAULT ''   COMMENT '文书标题',
    input_json        LONGTEXT              COMMENT '用户输入要素（JSON）',
    generated_content LONGTEXT              COMMENT '生成的文书草稿',
    penalty_advice    LONGTEXT              COMMENT '结构化量纪建议（含免责说明）',
    operator          VARCHAR(50)  DEFAULT ''   COMMENT '操作人（水印溯源）',
    dept_name         VARCHAR(100) DEFAULT ''   COMMENT '操作人部门（水印溯源）',
    create_by         VARCHAR(64)  DEFAULT ''   COMMENT '创建者',
    del_flag          CHAR(1)      DEFAULT '0',
    create_time       DATETIME     DEFAULT NULL,
    PRIMARY KEY (record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文书生成记录（M6，留痕）';

-- ------------------------------------------------------------
-- 5. 字典：模板状态
-- ------------------------------------------------------------
DELETE FROM sys_dict_data WHERE dict_type = 'jc_report_template_status';
DELETE FROM sys_dict_type WHERE dict_type = 'jc_report_template_status';
INSERT INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark)
VALUES (23001, '文书模板状态', 'jc_report_template_status', '0', 'admin', sysdate(), '文书模板启用/停用');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (1, '启用', '0', 'jc_report_template_status', '', 'primary', 'Y', '0', 'admin', sysdate(), ''),
       (2, '停用', '1', 'jc_report_template_status', '', 'danger',  'N', '0', 'admin', sysdate(), '');

-- ------------------------------------------------------------
-- 6. 初始化文书模板（立案报告 / 笔录模板 / 调查终结报告骨架）
--    模板变量 ${key} 与 fields_meta 的 key 一一对应
-- ------------------------------------------------------------
DELETE FROM jc_report_template WHERE template_id BETWEEN 200 AND 299;
INSERT INTO jc_report_template (template_id, doc_type, template_name, template_content, fields_meta, status, create_by, create_time)
VALUES
(201, '立案报告', '问题线索立案报告',
 '${被反映人}（${单位职务}）：\n\n根据${线索来源}反映，现对有关问题线索予以立案审查。\n\n一、被反映人基本情况\n${基本情况}\n\n二、反映的主要问题\n${主要问题}\n\n三、立案依据\n依据《中国共产党纪律检查机关监督执纪工作规则》及公司内部规定，经研究决定对${被反映人}涉嫌违纪问题立案审查。\n\n（落款与日期留空，定稿时由承办人填写）',
 '[{"key":"被反映人","label":"被反映人","type":"input","required":true},{"key":"单位职务","label":"单位及职务","type":"input","required":true},{"key":"线索来源","label":"线索来源","type":"input","required":true},{"key":"基本情况","label":"基本情况","type":"textarea","required":false},{"key":"主要问题","label":"反映的主要问题","type":"textarea","required":true}]',
 '0', 'admin', sysdate()),

(202, '笔录模板', '谈话笔录',
 '谈话笔录\n\n时间：${谈话时间}　地点：${谈话地点}\n谈话人：${谈话人}　记录人：${记录人}\n被谈话人：${被谈话人}（${单位职务}）\n\n一、告知权利义务\n${权利义务告知}\n\n二、谈话内容\n${谈话内容}\n\n三、被谈话人核对意见\n${核对意见}\n\n（被谈话人签名、按指印、日期留空，定稿时填写）',
 '[{"key":"谈话时间","label":"谈话时间","type":"date","required":true},{"key":"谈话地点","label":"谈话地点","type":"input","required":true},{"key":"谈话人","label":"谈话人","type":"input","required":true},{"key":"记录人","label":"记录人","type":"input","required":true},{"key":"被谈话人","label":"被谈话人","type":"input","required":true},{"key":"单位职务","label":"单位及职务","type":"input","required":true},{"key":"权利义务告知","label":"权利义务告知","type":"textarea","required":false},{"key":"谈话内容","label":"谈话内容","type":"textarea","required":true},{"key":"核对意见","label":"核对意见","type":"textarea","required":false}]',
 '0', 'admin', sysdate()),

(203, '调查终结报告', '调查终结报告',
 '关于${被反映人}（${单位职务}）违纪问题的调查终结报告\n\n一、线索来源及立案情况\n${线索来源立案}\n\n二、被反映人基本情况\n${基本情况}\n\n三、经查证认定的事实\n${认定事实}\n\n四、定性及量纪建议\n（本部分由系统结合知识库法条辅助生成，详见生成结果中的“量纪建议”，须人工复核后认定）\n${量纪建议占位}\n\n五、处理建议\n${处理建议}\n\n（落款、署名、日期留空，定稿时填写）',
 '[{"key":"被反映人","label":"被反映人","type":"input","required":true},{"key":"单位职务","label":"单位及职务","type":"input","required":true},{"key":"线索来源立案","label":"线索来源及立案情况","type":"textarea","required":true},{"key":"基本情况","label":"基本情况","type":"textarea","required":false},{"key":"认定事实","label":"经查证认定的事实","type":"textarea","required":true},{"key":"量纪建议占位","label":"量纪建议（占位，详见系统生成）","type":"textarea","required":false},{"key":"处理建议","label":"处理建议","type":"textarea","required":false}]',
 '0', 'admin', sysdate());

-- ------------------------------------------------------------
-- 7. 校验
-- ------------------------------------------------------------
SELECT '文书辅助菜单' AS item, COUNT(1) AS cnt FROM sys_menu      WHERE menu_id BETWEEN 2300 AND 2399
UNION ALL SELECT '文书模板表', COUNT(1) FROM information_schema.tables
         WHERE table_schema = DATABASE() AND table_name = 'jc_report_template'
UNION ALL SELECT '文书记录表', COUNT(1) FROM information_schema.tables
         WHERE table_schema = DATABASE() AND table_name = 'jc_report_record'
UNION ALL SELECT '初始模板数', COUNT(1) FROM jc_report_template WHERE template_id BETWEEN 200 AND 299;

SELECT menu_id, menu_name, parent_id, path, perms FROM sys_menu WHERE menu_id BETWEEN 2300 AND 2399 ORDER BY menu_id;
