-- ============================================================
-- 纪检监察助手 · 知识库模块 SQL（M1 知识库+AI问答 / M2 文档处理）
-- 追加执行，可重复运行（先按段位清理再插入）
-- 前提：jiancha_init.sql 已导入（jc_kb_category / jc_kb_document / jc_kb_chunk 及字典已存在）
-- ============================================================

-- ------------------------------------------------------------
-- 1. 清理旧菜单（幂等：2200-2299 专供知识库）
-- ------------------------------------------------------------
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 2200 AND 2299;
DELETE FROM sys_menu      WHERE menu_id BETWEEN 2200 AND 2299;

-- ------------------------------------------------------------
-- 2. 知识库目录 + 菜单
--    2200 知识库（目录，挂在纪检监察 2000 下，排序 4）
--    2201 知识库问答  2202 文档管理  2203 分类管理
-- ------------------------------------------------------------
INSERT INTO sys_menu VALUES (2200, '知识库', '2000', '4', 'kb', null, '', '', 1, 0, 'M', '0', '0', '', 'documentation', 'admin', sysdate(), '', null, '知识库业务目录（M1+M2）');

-- 2201 知识库问答
INSERT INTO sys_menu VALUES (2201, '知识库问答', '2200', '1', 'kbchat', 'jiancha/kb/chat/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:kb:chat:query', 'education', 'admin', sysdate(), '', null, '知识库 RAG 问答（回答必须带出处）');
INSERT INTO sys_menu VALUES (2211, '知识检索', '2201', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:kb:chat:query',   '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2212, '会话清空', '2201', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:kb:chat:clear',   '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2213, 'AI智能问答', '2201', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:ai:query',      '#', 'admin', sysdate(), '', null, 'AI 通用问答（/jiancha/ai/chat、/chat/stream）权限，后端 AiChatController 要求');

-- 2202 文档管理
INSERT INTO sys_menu VALUES (2202, '文档管理', '2200', '2', 'kbdoc', 'jiancha/kb/doc/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:kb:doc:list', 'upload', 'admin', sysdate(), '', null, '知识库文档管理（解析/分块/向量化）');
INSERT INTO sys_menu VALUES (2221, '文档查询', '2202', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:kb:doc:query',     '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2222, '文档上传', '2202', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:kb:doc:upload',    '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2223, '文档修改', '2202', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:kb:doc:edit',      '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2224, '文档删除', '2202', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:kb:doc:remove',    '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2225, '重新入库', '2202', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:kb:doc:reingest',  '#', 'admin', sysdate(), '', null, '解析失败重试 / 重建向量');

-- 2203 分类管理
INSERT INTO sys_menu VALUES (2203, '分类管理', '2200', '3', 'kbcat', 'jiancha/kb/category/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:kb:category:list', 'tree', 'admin', sysdate(), '', null, '知识库分类树管理');
INSERT INTO sys_menu VALUES (2231, '分类查询', '2203', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:kb:category:query',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2232, '分类新增', '2203', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:kb:category:add',    '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2233, '分类修改', '2203', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:kb:category:edit',   '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2234, '分类删除', '2203', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:kb:category:remove', '#', 'admin', sysdate(), '', null, '');

-- ------------------------------------------------------------
-- 3. 授予超级管理员（role_id=1）全部新菜单
-- ------------------------------------------------------------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 2200 AND 2299;

-- ------------------------------------------------------------
-- 4. 业务参数（走配置，代码不硬编码）
-- ------------------------------------------------------------
DELETE FROM sys_config WHERE config_key IN ('jiancha.kb.chunk.max-length', 'jiancha.kb.chunk.overlap');
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
VALUES ('知识库分块最大长度', 'jiancha.kb.chunk.max-length', '800', 'Y', 'admin', sysdate(), '结构感知分块：单块最大字符数，超长二次切分'),
       ('知识库分块重叠长度', 'jiancha.kb.chunk.overlap',    '100', 'Y', 'admin', sysdate(), '结构感知分块：相邻块重叠字符数，保证语义连续');

-- ------------------------------------------------------------
-- 5. 初始化分类树（外部法规库 / 内部制度库 两大知识域）
-- ------------------------------------------------------------
DELETE FROM jc_kb_category WHERE category_id BETWEEN 1000 AND 1099;
INSERT INTO jc_kb_category (category_id, parent_id, ancestors, cat_name, cat_type, order_num, status, create_by, create_time)
VALUES (1000, 0,    '0',           '外部法规库', '党纪', 1, '0', 'admin', sysdate()),
       (1001, 1000, '0,1000',      '党内法规',   '党纪', 1, '0', 'admin', sysdate()),
       (1002, 1000, '0,1000',      '国家法律',   '国法', 2, '0', 'admin', sysdate()),
       (1010, 0,    '0',           '内部制度库', '制度', 2, '0', 'admin', sysdate()),
       (1011, 1010, '0,1010',      '廉洁从业制度', '制度', 1, '0', 'admin', sysdate()),
       (1012, 1010, '0,1010',      '采购与招投标制度', '制度', 2, '0', 'admin', sysdate()),
       (1013, 1010, '0,1010',      '财务与费用制度', '制度', 3, '0', 'admin', sysdate()),
       (1020, 0,    '0',           '典型案例库', '案例', 3, '0', 'admin', sysdate()),
       (1021, 1020, '0,1020',      '舞弊案例',   '案例', 1, '0', 'admin', sysdate()),
       (1022, 1020, '0,1020',      '警示教育',   '警示', 2, '0', 'admin', sysdate());

-- ------------------------------------------------------------
-- 6. 校验
-- ------------------------------------------------------------
SELECT '知识库菜单' AS item, COUNT(1) AS cnt FROM sys_menu      WHERE menu_id BETWEEN 2200 AND 2299
UNION ALL SELECT '知识库参数', COUNT(1) FROM sys_config    WHERE config_key LIKE 'jiancha.kb.%'
UNION ALL SELECT '知识库分类', COUNT(1) FROM jc_kb_category WHERE category_id BETWEEN 1000 AND 1099
UNION ALL SELECT '知识库表',   COUNT(1) FROM information_schema.tables
         WHERE table_schema = DATABASE() AND table_name IN ('jc_kb_category','jc_kb_document','jc_kb_chunk');

SELECT menu_id, menu_name, parent_id, path, perms FROM sys_menu WHERE menu_id BETWEEN 2200 AND 2299 ORDER BY menu_id;
