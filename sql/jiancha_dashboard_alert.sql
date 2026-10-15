-- 段12 纪检综合看板 · 预警阈值规则表
CREATE TABLE IF NOT EXISTS jc_dashboard_alert_rule (
    rule_id      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '规则主键',
    rule_name    VARCHAR(100) NOT NULL COMMENT '规则名称',
    module_key   VARCHAR(32)  NOT NULL COMMENT '适用模块：petition/rectify/risk/supplier/edu/reply/conduct/asset/all',
    metric_type  VARCHAR(16)  NOT NULL COMMENT '指标类型：overdue/processing/total',
    threshold    INT          NOT NULL DEFAULT 0 COMMENT '触发阈值（实际值 >= 阈值即预警）',
    alert_level  VARCHAR(8)   NOT NULL COMMENT '预警级别：red/yellow',
    enabled      CHAR(1)      NOT NULL DEFAULT '1' COMMENT '是否启用：1启用 0停用',
    remark       VARCHAR(255) DEFAULT NULL COMMENT '备注',
    create_by    VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    create_time  DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_by    VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    update_time  DATETIME     DEFAULT NULL COMMENT '更新时间',
    del_flag     CHAR(1)      NOT NULL DEFAULT '0' COMMENT '删除标志：0正常 2删除',
    PRIMARY KEY (rule_id),
    KEY idx_module (module_key),
    KEY idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='看板预警阈值规则';

-- 默认规则（幂等：重复执行跳过）
INSERT IGNORE INTO jc_dashboard_alert_rule
    (rule_id, rule_name, module_key, metric_type, threshold, alert_level, enabled, remark, create_by, create_time, del_flag)
VALUES
    (1, '信访举报超期预警',   'petition', 'overdue',     3,   'red',    '1', '信访举报超期≥3件触发红色预警',     'system', NOW(), '0'),
    (2, '整改闭环超期预警',   'rectify',  'overdue',     5,   'yellow', '1', '整改事项超期≥5件触发黄色预警',     'system', NOW(), '0'),
    (3, '涉案财物超期预警',   'asset',    'overdue',     2,   'red',    '1', '涉案财物超期≥2件触发红色预警',     'system', NOW(), '0'),
    (4, '供应商黑名单预警',   'supplier', 'overdue',     0,   'red',    '1', '存在黑名单/禁入供应商即红色预警',  'system', NOW(), '0'),
    (5, '全局在办积压预警',   'all',      'processing',  20,  'yellow', '1', '全局在办事项≥20件触发黄色预警',    'system', NOW(), '0');
