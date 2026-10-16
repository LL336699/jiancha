-- ============================================================
-- 纪检监察助手 · 利益冲突申报看板 SQL（E1 段15 前端配套）
-- 追加执行，可重复运行（DELETE+INSERT 幂等）
-- 前提：jiancha_init.sql 已导入；菜单 2000-3221 区间已占用
-- 段位：3222-3229 专供利益冲突申报统计
-- ============================================================

-- ------------------------------------------------------------
-- 1. 清理旧菜单（幂等：3222-3229 专供申报统计）
-- ------------------------------------------------------------
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 3222 AND 3229;
DELETE FROM sys_menu      WHERE menu_id BETWEEN 3222 AND 3229;

-- ------------------------------------------------------------
-- 2. 申报统计菜单（挂在 利益冲突申报 2002 下）
--    3222 申报统计（C 型页面）
--    3223 统计查询（F）  3224 未申报督办（F）
-- ------------------------------------------------------------
INSERT INTO sys_menu VALUES (3222, '申报统计', '2002', '1', 'declarestat', 'jiancha/archive/declare/stat/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:archive:list', 'chart', 'admin', sysdate(), '', null, '年度集中申报进度/状态分布/类型分布/月度趋势/未申报督办');
INSERT INTO sys_menu VALUES (3223, '统计查询', '3222', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:archive:list',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (3224, '未申报督办', '3222', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:archive:query', '#', 'admin', sysdate(), '', null, '年度尚无申报的在岗人员清单');

-- ------------------------------------------------------------
-- 3. 授予超级管理员（role_id=1）全部新菜单
-- ------------------------------------------------------------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 3222 AND 3229;

-- 注：事项类型字典 jc_conflict_item_type（经商办企业/兼职取酬/亲属从业/对外投资/
-- 关联交易/劳务报酬/其他）已由 jiancha_init.sql 注册，此处不重复定义。

-- ------------------------------------------------------------
-- 4. 申报变更留痕表 jc_conflict_log（段15 追加）
--    申报单 新增/修改/核实/删除 全量留痕，满足"谁在何时改过什么"审计追溯
--    查询复用 jiancha:archive:query 权限，不新增菜单
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS jc_conflict_log (
  log_id        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '留痕ID',
  declare_id    BIGINT       NOT NULL                COMMENT '申报单ID',
  action        VARCHAR(20)  NOT NULL                COMMENT '动作（add/edit/verify/delete）',
  detail        VARCHAR(500) DEFAULT NULL            COMMENT '变更内容摘要',
  operator      VARCHAR(64)  DEFAULT ''              COMMENT '操作人',
  operate_time  DATETIME     DEFAULT NULL            COMMENT '操作时间',
  PRIMARY KEY (log_id),
  KEY idx_conflict_log_declare (declare_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='利益冲突申报变更留痕表';
