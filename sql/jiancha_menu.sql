-- ============================================================
-- 纪检监察助手 菜单初始化（追加执行，可重复：先清理同 ID 再插入）
-- 前提：ry_20260417.sql 已导入
-- ============================================================

-- 1. 隐藏若依默认的业务性目录（保留系统管理供管理员使用；需要时 visible 改回 '0' 即可恢复）
UPDATE sys_menu SET visible = '1' WHERE menu_id IN (2, 3, 4);  -- 系统监控 / 系统工具 / 若依官网

-- 2. 清理旧菜单（幂等）
DELETE FROM sys_menu WHERE menu_id BETWEEN 2000 AND 2099;

-- 3. 纪检监察目录
INSERT INTO sys_menu VALUES (2000, '纪检监察', '0', '1', 'jiancha', null, '', '', 1, 0, 'M', '0', '0', '', 'monitor', 'admin', sysdate(), '', null, '纪检监察业务目录');

-- 4. 廉洁档案
INSERT INTO sys_menu VALUES (2001, '廉洁档案', '2000', '1', 'person', 'jiancha/archive/person/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:archive:list', 'peoples', 'admin', sysdate(), '', null, '廉洁档案管理菜单');
INSERT INTO sys_menu VALUES (2003, '档案查询', '2001', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:archive:query', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2004, '档案新增', '2001', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:archive:add', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2005, '档案修改', '2001', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:archive:edit', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2006, '档案删除', '2001', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:archive:remove', '#', 'admin', sysdate(), '', null, '');

-- 5. 利益冲突申报
INSERT INTO sys_menu VALUES (2002, '利益冲突申报', '2000', '2', 'declare', 'jiancha/archive/declare/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:archive:list', 'form', 'admin', sysdate(), '', null, '利益冲突申报菜单');
INSERT INTO sys_menu VALUES (2007, '申报查询', '2002', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:archive:query', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2008, '申请新增', '2002', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:archive:add', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2009, '申报修改', '2002', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:archive:edit', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2010, '申报删除', '2002', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:archive:remove', '#', 'admin', sysdate(), '', null, '');

-- 6. 补充缺失的性别字典（jc_person.gender 引用但从未 seed）
DELETE FROM sys_dict_data WHERE dict_type = 'jc_gender';
DELETE FROM sys_dict_type WHERE dict_type = 'jc_gender';
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
VALUES ('人员性别', 'jc_gender', '0', 'admin', sysdate(), '廉洁档案人员性别');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (1,'男','0','jc_gender','Y','0','admin',NOW(),''),
       (2,'女','1','jc_gender','N','0','admin',NOW(),''),
       (3,'未知','2','jc_gender','N','0','admin',NOW(),'');

SELECT menu_id, menu_name, parent_id, path, visible FROM sys_menu WHERE menu_id BETWEEN 2000 AND 2099 OR menu_id IN (1,2,3,4) ORDER BY menu_id;
