-- ============================================================
-- 纪检监察助手 · 段9：廉政意见回复（E2）
-- 菜单段位 3000-3099；表 jc_reply_request / jc_reply_screen_item
-- 前提：jiancha_init.sql / jiancha_menu.sql 已导入
-- ============================================================

-- ------------------------------------------------------------
-- 1. 菜单（先删后插，幂等）
-- ------------------------------------------------------------
DELETE FROM sys_menu WHERE menu_id BETWEEN 3000 AND 3099;

INSERT INTO sys_menu VALUES (3000, '廉政意见回复', '2000', '9', 'reply', null, '', '', 1, 0, 'M', '0', '0', '', 'education', 'admin', sysdate(), '', null, '廉政意见回复业务目录（E2）：出具廉洁意见，提拔/评优/出国境/职称评聘前置');

INSERT INTO sys_menu VALUES (3001, '意见回复办理', '3000', '1', 'reply', 'jiancha/reply/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:reply:list', 'education', 'admin', sysdate(), '', null, '申请登记 → 系统筛查（六大模块聚合）→ 人工核实 → 出具正式结论');

INSERT INTO sys_menu VALUES (3011, '回复查询', '3001', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:reply:list',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (3012, '回复详情', '3001', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:reply:query', '#', 'admin', sysdate(), '', null, '含筛查明细');
INSERT INTO sys_menu VALUES (3013, '申请登记', '3001', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:reply:add',   '#', 'admin', sysdate(), '', null, '自动编号 RQ+日期+序号，同人同事项进行中不可重复');
INSERT INTO sys_menu VALUES (3014, '登记修改', '3001', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:reply:edit',  '#', 'admin', sysdate(), '', null, '仅待筛查状态');
INSERT INTO sys_menu VALUES (3015, '系统筛查', '3001', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:reply:screen','#', 'admin', sysdate(), '', null, '跨线索/举报/整改/预警/供应商关联/处分六源聚合');
INSERT INTO sys_menu VALUES (3016, '出具结论', '3001', '6', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:reply:reply', '#', 'admin', sysdate(), '', null, '无违纪/暂缓/否决，人工核实后出具');
INSERT INTO sys_menu VALUES (3017, '关闭申请', '3001', '7', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:reply:close', '#', 'admin', sysdate(), '', null, '事项取消/来文撤回');
INSERT INTO sys_menu VALUES (3018, '回复删除', '3001', '8', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:reply:remove','#', 'admin', sysdate(), '', null, '级联筛查明细');

INSERT INTO sys_menu VALUES (3002, '回复看板', '3000', '2', 'replystat', 'jiancha/reply/stat/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:reply:stat', 'chart', 'admin', sysdate(), '', null, '申请量/办理时长/结论分布/来源命中分布');

INSERT INTO sys_menu VALUES (3021, '统计查询', '3002', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:reply:stat', '#', 'admin', sysdate(), '', null, '');

-- ------------------------------------------------------------
-- 2. 授予超级管理员（role_id=1）全部新菜单
-- ------------------------------------------------------------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 3000 AND 3099;

-- ------------------------------------------------------------
-- 3. 数据字典
-- ------------------------------------------------------------
DELETE FROM sys_dict_data WHERE dict_type IN
    ('jc_reply_matter','jc_reply_source','jc_reply_conclusion');
DELETE FROM sys_dict_type WHERE dict_type IN
    ('jc_reply_matter','jc_reply_source','jc_reply_conclusion');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
 ('意见事项类型', 'jc_reply_matter',     '0', 'admin', sysdate(), '廉政意见回复的事项类型'),
 ('筛查来源模块', 'jc_reply_source',     '0', 'admin', sysdate(), '系统筛查聚合的六大来源模块'),
 ('意见结论',     'jc_reply_conclusion', '0', 'admin', sysdate(), '正式结论（人工出具）；系统建议结论文本自带「建议」前缀，不在此字典');

-- 事项类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '提拔任用', '提拔任用', 'jc_reply_matter', '', 'primary', 'Y', '0', 'admin', sysdate(), '拟提拔/任用前征求意见'),
 (2, '评优评先', '评优评先', 'jc_reply_matter', '', 'success', 'N', '0', 'admin', sysdate(), '评优评先推荐前征求意见'),
 (3, '出国境',   '出国境',   'jc_reply_matter', '', 'warning', 'N', '0', 'admin', sysdate(), '因私/因公出国境前征求意见'),
 (4, '职称评聘', '职称评聘', 'jc_reply_matter', '', 'info',    'N', '0', 'admin', sysdate(), '职称评审/聘用前征求意见'),
 (5, '其他',     '其他',     'jc_reply_matter', '', 'danger',  'N', '0', 'admin', sysdate(), '其他需要出具廉洁意见的事项');

-- 筛查来源模块（与后端 SRC_* 常量一致）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '问题线索',   '问题线索',   'jc_reply_source', '', 'primary', 'Y', '0', 'admin', sysdate(), 'M1 问题线索（未结）'),
 (2, '舞弊举报',   '舞弊举报',   'jc_reply_source', '', 'danger',  'N', '0', 'admin', sysdate(), 'M3 舞弊举报（未归档/未受理终结）'),
 (3, '整改问题',   '整改问题',   'jc_reply_source', '', 'warning', 'N', '0', 'admin', sysdate(), 'E3 整改问题（未闭环）'),
 (4, '风险预警',   '风险预警',   'jc_reply_source', '', 'danger',  'N', '0', 'admin', sysdate(), 'M10 风险预警（未闭环）'),
 (5, '供应商关联', '供应商关联', 'jc_reply_source', '', 'warning', 'N', '0', 'admin', sysdate(), 'E4 供应商关联（待处置/观察/禁入）'),
 (6, '廉政处分',   '廉政处分',   'jc_reply_source', '', 'info',    'N', '0', 'admin', sysdate(), '近3年处分记录');

-- 正式结论
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '无违纪', '无违纪', 'jc_reply_conclusion', '', 'success', 'Y', '0', 'admin', sysdate(), '未发现违纪问题，同意'),
 (2, '暂缓',   '暂缓',   'jc_reply_conclusion', '', 'warning', 'N', '0', 'admin', sysdate(), '有关问题正在核查，建议暂缓'),
 (3, '否决',   '否决',   'jc_reply_conclusion', '', 'danger',  'N', '0', 'admin', sysdate(), '存在严重问题，不同意');

-- ------------------------------------------------------------
-- 4. 业务参数
-- ------------------------------------------------------------
DELETE FROM sys_config WHERE config_key LIKE 'jiancha.reply.%';

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark) VALUES
 ('廉政意见默认办理时限(天)', 'jiancha.reply.deadline.days', '5', 'Y', 'admin', sysdate(), '收到日期+该天数=办理截止，驱动红黄灯（超期红灯/过半黄灯）');

-- ------------------------------------------------------------
-- 5. 红黄灯日刷新定时任务（默认暂停，可在「系统监控 → 定时任务」启用）
-- ------------------------------------------------------------
DELETE FROM sys_job WHERE invoke_target = 'jcReplyScanTask.run()';

INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
VALUES ('廉政意见回复红黄灯刷新', 'DEFAULT', 'jcReplyScanTask.run()', '0 50 7 * * ?', '3', '1', '1', 'admin', sysdate(), '每日7:50：按办理时限刷新红黄灯（超期=红灯，窗口过半=黄灯，默认暂停）');

-- ------------------------------------------------------------
-- 6. 业务表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS jc_reply_screen_item;
DROP TABLE IF EXISTS jc_reply_request;

-- 廉政意见回复申请表
CREATE TABLE jc_reply_request (
    request_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '申请ID',
    request_no     VARCHAR(32)  NOT NULL                COMMENT '申请编号(RQ+yyyyMMdd+seq)',
    person_id      BIGINT       NOT NULL                COMMENT '对象人员ID(jc_person.person_id)',
    person_name    VARCHAR(64)  DEFAULT ''              COMMENT '对象姓名(登记时档案快照)',
    person_dept    VARCHAR(100) DEFAULT ''              COMMENT '对象部门(登记时档案快照)',
    matter_type    VARCHAR(32)  NOT NULL                COMMENT '事项类型(字典 jc_reply_matter)',
    matter_desc    VARCHAR(1000) DEFAULT ''             COMMENT '事项说明(拟任岗位/评优名称等)',
    apply_dept     VARCHAR(200) DEFAULT ''              COMMENT '申请单位(来人/来函单位)',
    apply_contact  VARCHAR(64)  DEFAULT ''              COMMENT '申请联系人',
    apply_doc_no   VARCHAR(100) DEFAULT ''              COMMENT '申请来文号',
    receive_date   DATE         DEFAULT NULL            COMMENT '收到日期',
    deadline_days  INT          DEFAULT 5               COMMENT '办理时限(天)',
    status         CHAR(1)      DEFAULT '0'             COMMENT '状态(0待筛查 1已筛查待核实 2已回复 3已关闭)',
    suggest_result VARCHAR(32)  DEFAULT ''              COMMENT '系统建议结论(建议通过/建议暂缓/建议否决，仅供参考)',
    final_result   VARCHAR(32)  DEFAULT ''              COMMENT '正式结论(字典 jc_reply_conclusion：无违纪/暂缓/否决)',
    reply_content  VARCHAR(2000) DEFAULT ''             COMMENT '回复内容(正式意见正文)',
    reply_date     DATETIME     DEFAULT NULL            COMMENT '回复日期',
    reply_by       VARCHAR(64)  DEFAULT ''              COMMENT '回复人',
    hit_count      INT          DEFAULT 0               COMMENT '筛查命中数',
    warn_level     VARCHAR(8)   DEFAULT ''              COMMENT '预警级别(空/黄灯/红灯，Quartz日刷新)',
    remark         VARCHAR(500) DEFAULT ''              COMMENT '备注',
    create_by      VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time    DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by      VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time    DATETIME     DEFAULT NULL            COMMENT '更新时间',
    del_flag       CHAR(1)      DEFAULT '0'             COMMENT '删除标记(0正常 1删除)',
    PRIMARY KEY (request_id),
    UNIQUE KEY uk_reply_no (request_no),
    KEY idx_reply_person (person_id),
    KEY idx_reply_status (status),
    KEY idx_reply_matter (matter_type)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='廉政意见回复申请';

-- 筛查明细表（系统筛查命中的跨模块记录快照）
CREATE TABLE jc_reply_screen_item (
    item_id       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    request_id    BIGINT       NOT NULL                COMMENT '申请ID',
    source_module VARCHAR(32)  NOT NULL                COMMENT '来源模块(字典 jc_reply_source)',
    source_no     VARCHAR(64)  DEFAULT ''              COMMENT '来源单号(线索号/petition:id/问题编号/预警号/SUP:编码/处分:id)',
    summary       VARCHAR(1000) DEFAULT ''             COMMENT '摘要(标题或事项简述)',
    source_status VARCHAR(32)  DEFAULT ''              COMMENT '来源状态(快照标签)',
    risk_weight   INT          DEFAULT 0               COMMENT '风险权重(0-100，按模块×状态折算)',
    screen_time   DATETIME     DEFAULT NULL            COMMENT '筛查时间',
    remark        VARCHAR(500) DEFAULT ''              COMMENT '备注',
    create_by     VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time   DATETIME     DEFAULT NULL            COMMENT '创建时间',
    del_flag      CHAR(1)      DEFAULT '0'             COMMENT '删除标记(0正常 1删除)',
    PRIMARY KEY (item_id),
    KEY idx_screen_request (request_id),
    KEY idx_screen_module (source_module)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='廉政意见回复·筛查明细';
