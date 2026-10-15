-- ============================================================
-- 纪检监察助手 · 段12（纪检综合看板）数据库脚本
--
-- 执行前提：ry_20260417.sql + quartz.sql + jiancha_init.sql 已导入
-- 幂等性  ：可重复执行（快照表 CREATE TABLE IF NOT EXISTS；定时任务先删后插）
-- 说明：综合看板为只读聚合 + 每日快照。菜单复用 P6「数据看板」(menu_id 2401)，
--       本脚本仅建快照表并注册每日快照定时任务。
-- ============================================================

-- ============================================================
-- 1. 每日快照表（全模块指标汇总，按 snapshot_date 唯一，用于趋势与环比）
-- ============================================================
CREATE TABLE IF NOT EXISTS jc_dashboard_snapshot (
    snapshot_id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '快照主键',
    snapshot_date         DATE         NOT NULL COMMENT '快照日期（唯一）',
    petition_total        BIGINT       DEFAULT 0 COMMENT '信访举报-总量',
    petition_processing   BIGINT       DEFAULT 0 COMMENT '信访举报-在办',
    petition_overdue      BIGINT       DEFAULT 0 COMMENT '信访举报-超期',
    rectify_total         BIGINT       DEFAULT 0 COMMENT '整改闭环-总量',
    rectify_processing    BIGINT       DEFAULT 0 COMMENT '整改闭环-在办',
    rectify_overdue       BIGINT       DEFAULT 0 COMMENT '整改闭环-超期',
    risk_total            BIGINT       DEFAULT 0 COMMENT '廉政风控-总量',
    risk_processing       BIGINT       DEFAULT 0 COMMENT '廉政风控-在办',
    supplier_total        BIGINT       DEFAULT 0 COMMENT '供应商廉洁-总量',
    supplier_black        BIGINT       DEFAULT 0 COMMENT '供应商廉洁-黑名单(含禁入)',
    edu_total             BIGINT       DEFAULT 0 COMMENT '廉政教育-总量',
    edu_done              BIGINT       DEFAULT 0 COMMENT '廉政教育-已完卷',
    reply_total           BIGINT       DEFAULT 0 COMMENT '廉政意见回复-总量',
    reply_processing      BIGINT       DEFAULT 0 COMMENT '廉政意见回复-在办',
    reply_overdue         BIGINT       DEFAULT 0 COMMENT '廉政意见回复-超期',
    conduct_total         BIGINT       DEFAULT 0 COMMENT '作风建设-总量',
    conduct_processing    BIGINT       DEFAULT 0 COMMENT '作风建设-在办',
    conduct_overdue       BIGINT       DEFAULT 0 COMMENT '作风建设-超期',
    asset_total           BIGINT       DEFAULT 0 COMMENT '涉案财物-总量',
    asset_processing      BIGINT       DEFAULT 0 COMMENT '涉案财物-在办',
    asset_overdue         BIGINT       DEFAULT 0 COMMENT '涉案财物-超期',
    total_processing      BIGINT       DEFAULT 0 COMMENT '在办合计',
    total_overdue         BIGINT       DEFAULT 0 COMMENT '超期合计',
    light_level           VARCHAR(10)  DEFAULT 'green' COMMENT '整体灯色 red/yellow/green',
    create_by             VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    create_time           DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_by             VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    update_time           DATETIME     DEFAULT NULL COMMENT '更新时间',
    remark                VARCHAR(500) DEFAULT '' COMMENT '备注',
    PRIMARY KEY (snapshot_id),
    UNIQUE KEY uk_snapshot_date (snapshot_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='综合看板每日快照';

-- ============================================================
-- 2. 每日快照定时任务（默认暂停，可在「系统监控 → 定时任务」启用）
-- ============================================================
DELETE FROM sys_job WHERE invoke_target = 'jcDashboardSnapshotTask.run()';

INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
VALUES ('综合看板每日快照', 'DEFAULT', 'jcDashboardSnapshotTask.run()', '0 5 8 * * ?', '3', '1', '0', 'admin', sysdate(), '每日8:05：聚合全模块指标生成快照，按日幂等更新（默认暂停，启用后生效）');

-- ============================================================
-- 3. 执行结果自检
-- ============================================================
SELECT '快照表' AS item, COUNT(1) AS cnt FROM information_schema.tables WHERE table_name = 'jc_dashboard_snapshot';
SELECT '快照任务' AS item, COUNT(1) AS cnt FROM sys_job WHERE invoke_target = 'jcDashboardSnapshotTask.run()';
