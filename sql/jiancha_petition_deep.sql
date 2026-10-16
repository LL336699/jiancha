-- ============================================================
-- 纪检监察助手 · 舞弊举报深化 SQL（M3 段16：留言/满意度/督办/并案）
-- 追加执行，可重复运行（菜单 DELETE+INSERT 幂等；加列用存储过程幂等包裹）
-- 前提：jiancha_petition.sql 已导入；菜单 2500-2531 已占用，本段用 2532-2533
-- 部署提示：导入后建议在「系统监控→定时任务」注册 jcPetitionWarnTask.autoUrgeOverdue（每日 8:30）启用超期自动催办
-- ============================================================

-- ------------------------------------------------------------
-- 1. jc_petition 满意度评价列（已反馈后举报人凭回执码评价）
-- ------------------------------------------------------------
-- MySQL 8.0 不支持 ADD COLUMN IF NOT EXISTS，用存储过程幂等包裹
DROP PROCEDURE IF EXISTS jc_petition_deep_migrate;
DELIMITER $$
CREATE PROCEDURE jc_petition_deep_migrate()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'jc_petition' AND COLUMN_NAME = 'satisfaction') THEN
        ALTER TABLE jc_petition ADD COLUMN satisfaction TINYINT DEFAULT NULL COMMENT '举报人满意度(1-5，已反馈后评价)' AFTER feedback;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'jc_petition' AND COLUMN_NAME = 'satisfaction_comment') THEN
        ALTER TABLE jc_petition ADD COLUMN satisfaction_comment VARCHAR(500) DEFAULT NULL COMMENT '评价说明' AFTER satisfaction;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'jc_petition' AND COLUMN_NAME = 'satisfaction_time') THEN
        ALTER TABLE jc_petition ADD COLUMN satisfaction_time DATETIME DEFAULT NULL COMMENT '评价时间' AFTER satisfaction_comment;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'jc_petition' AND COLUMN_NAME = 'urge_count') THEN
        ALTER TABLE jc_petition ADD COLUMN urge_count INT NOT NULL DEFAULT 0 COMMENT '催办次数' AFTER satisfaction_time;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'jc_petition' AND COLUMN_NAME = 'last_urge_time') THEN
        ALTER TABLE jc_petition ADD COLUMN last_urge_time DATETIME DEFAULT NULL COMMENT '最近催办时间' AFTER urge_count;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'jc_petition' AND COLUMN_NAME = 'merged_into') THEN
        ALTER TABLE jc_petition ADD COLUMN merged_into BIGINT DEFAULT NULL COMMENT '并案主件举报ID（NULL=独立件）' AFTER last_urge_time;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'jc_petition' AND COLUMN_NAME = 'merged_remark') THEN
        ALTER TABLE jc_petition ADD COLUMN merged_remark VARCHAR(500) DEFAULT NULL COMMENT '并案说明（为何并入主件）' AFTER merged_into;
    END IF;
END$$
DELIMITER ;
CALL jc_petition_deep_migrate();
DROP PROCEDURE IF EXISTS jc_petition_deep_migrate;

-- ------------------------------------------------------------
-- 2. 举报留言表 jc_petition_message（举报人与受理人双向沟通）
--    匿名保护：不留 IP；content 全文落库（留言非敏感身份字段，
--    举报人姓名等仍走 jc_petition 的 AES-GCM 加密）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS jc_petition_message (
  message_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '留言ID',
  petition_id    BIGINT       NOT NULL                COMMENT '举报ID',
  receipt_lookup VARCHAR(64)  DEFAULT NULL            COMMENT '回执码检索哈希（举报人侧校验用）',
  sender         VARCHAR(20)  NOT NULL                COMMENT '发言方（reporter举报人/staff受理人）',
  sender_name    VARCHAR(64)  DEFAULT ''              COMMENT '发言方显示名（staff 为用户昵称，举报人固定「我」不落真名）',
  content        VARCHAR(2000) NOT NULL               COMMENT '留言内容',
  read_flag      CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '对方已读标记（0未读 1已读）',
  create_time    DATETIME     DEFAULT NULL            COMMENT '发言时间',
  PRIMARY KEY (message_id),
  KEY idx_message_petition (petition_id),
  KEY idx_message_receipt (receipt_lookup)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='舞弊举报双向留言表';

-- ------------------------------------------------------------
-- 3. 权限点（挂在 举报箱管理 2501 下；催办并入办理流转权限，
--    留言查看并入举报详情权限，不新增菜单）
-- ------------------------------------------------------------
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 2532 AND 2534;
DELETE FROM sys_menu      WHERE menu_id BETWEEN 2532 AND 2534;
INSERT INTO sys_menu VALUES (2532, '留言回复', '2501', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:petition:message', '#', 'admin', sysdate(), '', null, '查看与回复举报人留言（双向沟通）');
INSERT INTO sys_menu VALUES (2533, '举报催办', '2501', '6', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:petition:urge',    '#', 'admin', sysdate(), '', null, '对超期未办结举报催办（写轨迹留痕）');

-- ------------------------------------------------------------
-- 4. 授予超级管理员（role_id=1）新权限点
-- ------------------------------------------------------------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 2532 AND 2533;
