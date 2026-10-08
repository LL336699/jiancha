-- ============================================================
-- 纪检监察助手 — 组织架构与账号去若依化（企业通用模板）
-- 生成日期：2026-10-08
--
-- 说明：
--   1. 将 RuoYi 默认示例组织（若依科技 / 深圳总公司 / 长沙分公司 …）
--      替换为企业通用组织模板；
--   2. dept_id / post_id 保持不变，避免破坏 sys_user / jc_person 等外键引用；
--   3. 清理默认账号与菜单中的原作者痕迹（若依官网外链、ry 账号）。
--
-- 执行： mysql -uroot ry-vue < jiancha_org.sql
-- 幂等： 是（全部为 UPDATE / 条件 DELETE）
-- ============================================================

USE `ry-vue`;

-- ------------------------------------------------------------
-- 1. 部门：替换为企业通用组织模板
--    层级保持原结构：100 顶级 → 101/102 二级 → 103~109 三级
-- ------------------------------------------------------------
UPDATE sys_dept SET dept_name='集团总部',     leader='', phone='', email='' WHERE dept_id=100;
UPDATE sys_dept SET dept_name='总部职能中心', leader='', phone='', email='' WHERE dept_id=101;
UPDATE sys_dept SET dept_name='区域运营中心', leader='', phone='', email='' WHERE dept_id=102;
UPDATE sys_dept SET dept_name='人力资源部',   leader='', phone='', email='' WHERE dept_id=103;
UPDATE sys_dept SET dept_name='财务部',       leader='', phone='', email='' WHERE dept_id=104;
UPDATE sys_dept SET dept_name='纪检监察室',   leader='', phone='', email='' WHERE dept_id=105;
UPDATE sys_dept SET dept_name='采购部',       leader='', phone='', email='' WHERE dept_id=106;
UPDATE sys_dept SET dept_name='信息科技部',   leader='', phone='', email='' WHERE dept_id=107;
UPDATE sys_dept SET dept_name='华东分公司',   leader='', phone='', email='' WHERE dept_id=108;
UPDATE sys_dept SET dept_name='华南分公司',   leader='', phone='', email='' WHERE dept_id=109;

-- ------------------------------------------------------------
-- 2. 岗位：替换为企业通用岗位
-- ------------------------------------------------------------
UPDATE sys_post SET post_code='gm',    post_name='总经理'       WHERE post_id=1;
UPDATE sys_post SET post_code='mgr',   post_name='部门经理'     WHERE post_id=2;
UPDATE sys_post SET post_code='hrm',   post_name='人力资源专员' WHERE post_id=3;
UPDATE sys_post SET post_code='staff', post_name='普通员工'     WHERE post_id=4;

-- ------------------------------------------------------------
-- 3. 角色名称
-- ------------------------------------------------------------
UPDATE sys_role SET role_name='普通用户' WHERE role_id=2 AND role_name='普通角色';

-- ------------------------------------------------------------
-- 4. 账号清理
-- ------------------------------------------------------------
-- 4.1 系统管理员：归档到纪检监察室，清理 RuoYi 默认联系方式
UPDATE sys_user SET dept_id=105, email='', phonenumber='' WHERE user_name='admin';

-- 4.2 演示账号（原 ry / 若依）：改用中性名称并停用，保留记录以免破坏关联表
UPDATE sys_user SET user_name='demo', nick_name='演示账号', email='', phonenumber='', status='1'
 WHERE user_name='ry';

-- ------------------------------------------------------------
-- 5. 移除原作者外链菜单（若依官网 http://ruoyi.vip）
--    menu_id=4 无子菜单
-- ------------------------------------------------------------
DELETE FROM sys_role_menu WHERE menu_id=4;
DELETE FROM sys_menu      WHERE menu_id=4 AND path='http://ruoyi.vip';
