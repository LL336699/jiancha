-- ============================================================
-- 纪检监察助手 · 段17 全局打磨 SQL（权限细化 + 字典补录 + 消息通知中心）
-- 追加执行，可重复运行（字典/菜单幂等）
-- 前提：jiancha_init.sql 及各模块 SQL 已导入
-- 菜单 ID 占用：jiancha 区间 2000-2533 / 3202-3224，本段用 2534-2536（权限补录）、3230-3233（消息中心+系统巡检）
-- ============================================================

-- ------------------------------------------------------------
-- 1. 字典类型补录（9 个类型漏登记：sys_dict_data 有数据但 sys_dict_type 无记录，
--    导致「字典管理」页看不到这些字典、无法维护；部分前端下拉依赖类型校验）
-- 幂等：已存在则跳过
-- ------------------------------------------------------------
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT * FROM (
    SELECT '利益冲突事项类型' AS dict_name, 'jc_conflict_item_type' AS dict_type, '0' AS status, 'admin' AS create_by, NOW() AS create_time, '利益冲突申报事项分类' AS remark
    UNION ALL SELECT '申报单状态','jc_declare_status','0','admin',NOW(),'利益冲突申报单办理状态'
    UNION ALL SELECT '申报类型','jc_declare_type','0','admin',NOW(),'年度/事项/专项申报'
    UNION ALL SELECT '知识库分类类型','jc_kb_cat_type','0','admin',NOW(),'党纪法规/国家法律等资料分类'
    UNION ALL SELECT '知识库文档状态','jc_kb_doc_status','0','admin',NOW(),'文档解析入库流程状态'
    UNION ALL SELECT '在职状态','jc_person_status','0','admin',NOW(),'人员档案在职状态'
    UNION ALL SELECT '处分种类','jc_punish_type','0','admin',NOW(),'企业内部处分分档'
    UNION ALL SELECT '风险等级','jc_risk_level','0','admin',NOW(),'人员廉政风险等级'
    UNION ALL SELECT '预警灯色','jc_warn_level','0','admin',NOW(),'时限预警黄灯/红灯'
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type d WHERE d.dict_type = t.dict_type);

-- ------------------------------------------------------------
-- 2. 处分种类字典企业口径修正（jc_punish_type 原为党政口径：
--    严重警告/撤销党内职务/留党察看/开除党籍 不适用企业版；
--    企业版六档 = 警告/记过/记大过/降级/撤职/开除 + 经济处理）
-- 2.1 停用党政专有口径项（保留数据可追溯，不停用直接删）
-- ------------------------------------------------------------
UPDATE sys_dict_data SET status = '1',
       remark = CONCAT(IFNULL(remark,''), '；段17停用：党政处分口径，企业版不适用')
WHERE dict_type = 'jc_punish_type'
  AND dict_value IN ('严重警告', '撤销党内职务', '留党察看', '开除党籍')
  AND status = '0';

-- 2.2 补录企业分档项（记过/记大过/经济处理），幂等
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT * FROM (
    SELECT 2 AS dict_sort,'记过' AS dict_label,'记过' AS dict_value,'jc_punish_type' AS dict_type,'' AS css_class,'warning' AS list_class,'N' AS is_default,'0' AS status,'admin' AS create_by,NOW() AS create_time,'段17企业口径补录' AS remark
    UNION ALL SELECT 3,'记大过','记大过','jc_punish_type','','warning','N','0','admin',NOW(),'段17企业口径补录'
    UNION ALL SELECT 7,'经济处理','经济处理','jc_punish_type','','info','N','0','admin',NOW(),'扣减绩效/追缴违规所得'
) t
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_data d WHERE d.dict_type = 'jc_punish_type' AND d.dict_value = t.dict_value
);

-- ------------------------------------------------------------
-- 3. 权限细化补录（代码 @PreAuthorize 已引用但 sys_menu 未登记的 4 个权限点，
--    缺失会导致非 admin 角色无法在角色管理中勾选授权）
--    2534 风控规则详情 / 2535 协议详情 / 2536 合作方事件详情
--    教育统计走对齐：既有按钮 2951 的 perms 与代码不一致，直接改 perms 对齐
-- ------------------------------------------------------------
UPDATE sys_menu SET perms = 'jiancha:edu:stat:list' WHERE menu_id = 2951 AND perms = 'jiancha:edu:stat';

DELETE FROM sys_role_menu WHERE menu_id BETWEEN 2534 AND 2537;
DELETE FROM sys_menu      WHERE menu_id BETWEEN 2534 AND 2537;
INSERT INTO sys_menu VALUES (2534, '规则详情', '2702', '7', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:risk:rule:query',          '#', 'admin', sysdate(), '', null, '风控规则详情查询');
INSERT INTO sys_menu VALUES (2535, '协议详情', '2803', '7', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:supplier:agreement:query', '#', 'admin', sysdate(), '', null, '廉洁协议详情查询');
INSERT INTO sys_menu VALUES (2536, '事件详情', '2802', '7', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:supplier:link:query',      '#', 'admin', sysdate(), '', null, '合作方事件详情查询');

-- ------------------------------------------------------------
-- 4. 消息通知中心
-- ------------------------------------------------------------
-- 4.1 站内消息表
CREATE TABLE IF NOT EXISTS jc_notify_message (
  notify_id     BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '消息ID',
  title         VARCHAR(128) NOT NULL                 COMMENT '消息标题',
  content       VARCHAR(512) DEFAULT ''               COMMENT '消息内容',
  notify_type   VARCHAR(16)  NOT NULL DEFAULT '系统'  COMMENT '消息类型(字典 jc_notify_type: 举报/整改/教育/风控/申报/系统)',
  biz_module    VARCHAR(32)  DEFAULT ''               COMMENT '业务模块(petition/rectify/edu/risk/conflict/clue/supplier)',
  biz_id        BIGINT       DEFAULT NULL             COMMENT '业务主键(前端跳转定位用)',
  receiver      VARCHAR(64)  NOT NULL                 COMMENT '接收人用户名(sys_user.user_name)',
  receiver_name VARCHAR(64)  DEFAULT ''               COMMENT '接收人昵称(冗余便于展示)',
  is_read       CHAR(1)      DEFAULT '0'              COMMENT '是否已读(0未读 1已读)',
  read_time     DATETIME     DEFAULT NULL             COMMENT '已读时间',
  create_by     VARCHAR(64)  DEFAULT ''               COMMENT '创建者(系统触发为 Quartz/Auto)',
  create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  remark        VARCHAR(255) DEFAULT ''               COMMENT '备注',
  PRIMARY KEY (notify_id),
  KEY idx_notify_receiver (receiver, is_read),
  KEY idx_notify_type (notify_type),
  KEY idx_notify_time (create_time)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='站内消息通知(段17 消息通知中心)';

-- 4.2 消息类型字典（注册 + 数据，均幂等）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '消息类型','jc_notify_type','0','admin',NOW(),'站内消息业务类型'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'jc_notify_type');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT * FROM (
    SELECT 1 AS dict_sort,'举报' AS dict_label,'举报' AS dict_value,'jc_notify_type' AS dict_type,'' AS css_class,'danger'  AS list_class,'N' AS is_default,'0' AS status,'admin' AS create_by,NOW() AS create_time,'舞弊举报相关' AS remark
    UNION ALL SELECT 2,'整改','整改','jc_notify_type','','warning','N','0','admin',NOW(),'整改闭环相关'
    UNION ALL SELECT 3,'教育','教育','jc_notify_type','','primary','N','0','admin',NOW(),'廉政教育相关'
    UNION ALL SELECT 4,'风控','风控','jc_notify_type','','danger','N','0','admin',NOW(),'廉政风险预警'
    UNION ALL SELECT 5,'申报','申报','jc_notify_type','','primary','N','0','admin',NOW(),'利益冲突申报相关'
    UNION ALL SELECT 6,'系统','系统','jc_notify_type','','info','N','0','admin',NOW(),'系统自动提醒'
) t
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_data d WHERE d.dict_type = 'jc_notify_type' AND d.dict_value = t.dict_value
);

-- 4.3 消息中心与系统巡检菜单（C 页面 + F 按钮，挂一级业务目录 2000 纪检监察下）
--    铃铛未读数/标记已读接口登录即可用（本人消息），不设独立权限点；
--    list/remove 控制消息中心页的管理动作；巡检独立权限便于授予运维岗
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 3230 AND 3233;
DELETE FROM sys_menu      WHERE menu_id BETWEEN 3230 AND 3233;
INSERT INTO sys_menu VALUES (3230, '消息中心', '2000', '9',  'notify', 'jiancha/notify/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:notify:list',   'message', 'admin', sysdate(), '', null, '站内消息通知中心（段17）');
INSERT INTO sys_menu VALUES (3231, '标记已读', '3230', '1',  '',       '',                     '', '', 1, 0, 'F', '0', '0', 'jiancha:notify:read',   '#',       'admin', sysdate(), '', null, '标记消息已读/全部已读');
INSERT INTO sys_menu VALUES (3232, '删除消息', '3230', '2',  '',       '',                     '', '', 1, 0, 'F', '0', '0', 'jiancha:notify:remove', '#',       'admin', sysdate(), '', null, '删除本人或全部消息');
INSERT INTO sys_menu VALUES (3233, '系统巡检', '2000', '10', 'ops',    'jiancha/ops/index',    '', '', 1, 0, 'C', '0', '0', 'jiancha:ops:health',    'monitor', 'admin', sysdate(), '', null, '数据层/缓存/AI/定时任务一键自检（段17）');

-- ------------------------------------------------------------
-- 5. 授予超级管理员（role_id=1）新权限点（admin 为超管默认全量，此处同步 role_menu 便于审计）
-- ------------------------------------------------------------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 2534 AND 2536 OR menu_id BETWEEN 3230 AND 3233;

-- ------------------------------------------------------------
-- 5.5 聚合扫描定时任务种子（每日 8:05 全量扫描；status=1 暂停，由管理员在定时任务页启用）
-- ------------------------------------------------------------
DELETE FROM sys_job WHERE invoke_target = 'jcNotifyScanTask.dailyScan()';
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
VALUES ('消息聚合扫描', 'DEFAULT', 'jcNotifyScanTask.dailyScan()', '0 5 8 * * ?', '3', '1', '1', 'admin', sysdate(), '段17：举报超期/整改临期/申报截止/知识库失败聚合提醒；启用后生效');

-- 5.6 已读消息清理种子（每周一 6:00 清理 30 天前已读；status=1 暂停）
DELETE FROM sys_job WHERE invoke_target = 'jcNotifyScanTask.cleanExpired(30)';
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
VALUES ('消息过期清理', 'DEFAULT', 'jcNotifyScanTask.cleanExpired(30)', '0 0 6 ? * MON', '3', '1', '1', 'admin', sysdate(), '段17：清理30天前已读消息防表膨胀');

-- ------------------------------------------------------------
-- 6. 验证 SQL（导入后人工核对）
-- ------------------------------------------------------------
-- SELECT COUNT(*) FROM sys_dict_type WHERE dict_type LIKE 'jc_%';          -- 预期 42+9+1=52
-- SELECT dict_value, status FROM sys_dict_data WHERE dict_type='jc_punish_type' ORDER BY dict_sort;  -- 党政项已停用
-- SELECT menu_id, menu_name, perms FROM sys_menu WHERE menu_id IN (2534,2535,2536,3230,3231,3232);
-- SHOW CREATE TABLE jc_notify_message;
