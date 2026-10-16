-- ============================================================
-- 纪检监察助手 · 年度集中申报截止配置 SQL（E1 段15 补充）
-- 追加执行，可重复运行（DELETE+CREATE 幂等，会清空配置数据）
-- 段位：3225-3229（3222-3224 已由 jiancha_conflict_stat.sql 占用）
-- ============================================================

-- ------------------------------------------------------------
-- 1. 截止配置表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS jc_declare_config;
CREATE TABLE jc_declare_config (
    config_id    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    declare_year INT          NOT NULL                COMMENT '申报年度',
    deadline     DATE         DEFAULT NULL            COMMENT '集中申报截止日',
    enabled      CHAR(1)      DEFAULT '1'             COMMENT '是否启用(1启用/0关闭)',
    create_by    VARCHAR(64)  DEFAULT ''              COMMENT '创建人',
    create_time  DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by    VARCHAR(64)  DEFAULT ''              COMMENT '更新人',
    update_time  DATETIME     DEFAULT NULL            COMMENT '更新时间',
    remark       VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    PRIMARY KEY (config_id),
    UNIQUE KEY uk_declare_year (declare_year)
) ENGINE = InnoDB COMMENT = '年度集中申报截止配置';

-- ------------------------------------------------------------
-- 2. 种子数据：2026 年度集中申报 3 月 31 日截止
-- ------------------------------------------------------------
INSERT INTO jc_declare_config (declare_year, deadline, enabled, create_by, create_time, remark)
 VALUES (2026, '2026-03-31', '1', 'admin', sysdate(), '年度集中申报截止日，逾期未报纳入督办');

-- ------------------------------------------------------------
-- 3. 菜单（截止配置页，挂在 利益冲突申报 2002 下）
-- ------------------------------------------------------------
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 3225 AND 3229;
DELETE FROM sys_menu      WHERE menu_id BETWEEN 3225 AND 3229;

INSERT INTO sys_menu VALUES (3225, '截止配置', '2002', '2', 'declareconfig', 'jiancha/archive/declare/config/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:archive:list', 'date', 'admin', sysdate(), '', null, '年度集中申报截止日与开关配置');
INSERT INTO sys_menu VALUES (3226, '配置查询', '3225', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:archive:list',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (3227, '配置新增', '3225', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:archive:add',   '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (3228, '配置修改', '3225', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:archive:edit',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (3229, '配置删除', '3225', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:archive:remove','#', 'admin', sysdate(), '', null, '');

-- ------------------------------------------------------------
-- 4. 授予超级管理员（role_id=1）
-- ------------------------------------------------------------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 3225 AND 3229;
