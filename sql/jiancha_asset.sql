-- ============================================================
-- 纪检监察助手 · 段11：涉案财物管理（E6）
-- 菜单段位 3200-3299；表 jc_asset_info / jc_asset_flow
-- 前提：jiancha_init.sql / jiancha_menu.sql 已导入
-- 幂等：所有 INSERT 前先 DELETE 对应段位/键
-- ============================================================

-- ------------------------------------------------------------
-- 1. 菜单（先删后插，幂等）
-- ------------------------------------------------------------
DELETE FROM sys_menu WHERE menu_id BETWEEN 3200 AND 3299;

INSERT INTO sys_menu VALUES (3200, '涉案财物管理', '2000', '10', 'asset', null, '', '', 1, 0, 'M', '0', '0', '', 'education', 'admin', sysdate(), '', null, '涉案财物全程管理（E6）：扣押登记 → 入库暂存 → 移交保管 → 处置申请 → 处置执行，暂存中可审批退还');

INSERT INTO sys_menu VALUES (3201, '财物登记办理', '3200', '1', 'asset', 'jiancha/asset/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:asset:list', 'education', 'admin', sysdate(), '', null, '扣押登记（可关联线索/作风问题自动带出涉事人）→ 入库 → 处置 → 全程流转流水');

INSERT INTO sys_menu VALUES (3211, '财物查询', '3201', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:asset:list',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (3212, '财物详情', '3201', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:asset:query', '#', 'admin', sysdate(), '', null, '含全程流转记录');
INSERT INTO sys_menu VALUES (3213, '扣押登记', '3201', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:asset:add',   '#', 'admin', sysdate(), '', null, '自动编号 CA+日期+序号，来源单号自动带出涉事人');
INSERT INTO sys_menu VALUES (3214, '登记修改', '3201', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:asset:edit',  '#', 'admin', sysdate(), '', null, '仅待入库状态');
INSERT INTO sys_menu VALUES (3215, '登记删除', '3201', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:asset:remove','#', 'admin', sysdate(), '', null, '仅待入库状态');
INSERT INTO sys_menu VALUES (3216, '入库暂存', '3201', '6', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:asset:store', '#', 'admin', sysdate(), '', null, '登记保管人/存放地/入库日期，0→1');
INSERT INTO sys_menu VALUES (3217, '移交保管', '3201', '7', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:asset:store', '#', 'admin', sysdate(), '', null, '保管人变更，流水留痕');
INSERT INTO sys_menu VALUES (3218, '处置申请', '3201', '8', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:asset:dispose','#', 'admin', sysdate(), '', null, '定处置方式与期限，1→2');
INSERT INTO sys_menu VALUES (3219, '处置执行', '3201', '9', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:asset:dispose','#', 'admin', sysdate(), '', null, '退还/移交/销毁/收归，2→3');
INSERT INTO sys_menu VALUES (3220, '直接退还', '3201', '10', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:asset:dispose','#', 'admin', sysdate(), '', null, '暂存中经审批免处置流程退还，1→4');

INSERT INTO sys_menu VALUES (3202, '财物看板', '3200', '2', 'assetstat', 'jiancha/asset/stat/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:asset:stat', 'chart', 'admin', sysdate(), '', null, '财物总量/暂存中/处置中/超期未处置/估值总额/类别与保管分布');

INSERT INTO sys_menu VALUES (3221, '统计查询', '3202', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:asset:stat', '#', 'admin', sysdate(), '', null, '');

-- ------------------------------------------------------------
-- 2. 授予超级管理员（role_id=1）全部新菜单（先清后授，幂等）
-- ------------------------------------------------------------
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 3200 AND 3299;
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 3200 AND 3299;

-- ------------------------------------------------------------
-- 3. 数据字典
-- ------------------------------------------------------------
DELETE FROM sys_dict_data WHERE dict_type IN
    ('jc_asset_category','jc_asset_dispose','jc_asset_source','jc_asset_flow_type');
DELETE FROM sys_dict_type WHERE dict_type IN
    ('jc_asset_category','jc_asset_dispose','jc_asset_source','jc_asset_flow_type');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
 ('涉案物类别',   'jc_asset_category',  '0', 'admin', sysdate(), '涉案财物的物品类别'),
 ('涉案物处置方式', 'jc_asset_dispose',  '0', 'admin', sysdate(), '处置执行/直接退还的处置方式'),
 ('涉案物来源类型', 'jc_asset_source',   '0', 'admin', sysdate(), '财物扣押登记的来源模块'),
 ('涉案物流转类型', 'jc_asset_flow_type','0', 'admin', sysdate(), '财物流转流水类型（与后端 flow_type 常量一致）');

-- 物品类别
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '现金',       '现金',     'jc_asset_category', '', 'default', 'N', '0', 'admin', sysdate(), ''),
 (2, '礼品礼金',   '礼品礼金', 'jc_asset_category', '', 'default', 'N', '0', 'admin', sysdate(), '含购物卡/礼品卡'),
 (3, '有价证券',   '有价证券', 'jc_asset_category', '', 'default', 'N', '0', 'admin', sysdate(), '股票/债券/汇票'),
 (4, '电子设备',   '电子设备', 'jc_asset_category', '', 'default', 'N', '0', 'admin', sysdate(), '手机/电脑/存储介质'),
 (5, '贵重物品',   '贵重物品', 'jc_asset_category', '', 'default', 'N', '0', 'admin', sysdate(), '首饰/名表/字画'),
 (6, '烟酒茶卡',   '烟酒茶卡', 'jc_asset_category', '', 'default', 'N', '0', 'admin', sysdate(), '高档烟酒/茶叶/消费卡'),
 (7, '其他',       '其他',     'jc_asset_category', '', 'default', 'N', '0', 'admin', sysdate(), '');

-- 处置方式（与后端 DISPOSE_TYPES 一致）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '退还',     '退还',     'jc_asset_dispose', '', 'success', 'N', '0', 'admin', sysdate(), '退还当事人'),
 (2, '移交',     '移交',     'jc_asset_dispose', '', 'primary', 'N', '0', 'admin', sysdate(), '移交司法机关/上级'),
 (3, '销毁',     '销毁',     'jc_asset_dispose', '', 'danger',  'N', '0', 'admin', sysdate(), '假劣物品销毁'),
 (4, '收归',     '收归',     'jc_asset_dispose', '', 'warning', 'N', '0', 'admin', sysdate(), '收归企业/上缴'),
 (5, '暂存待处', '暂存待处', 'jc_asset_dispose', '', 'info',    'N', '0', 'admin', sysdate(), '暂缓处置继续保管');

-- 来源类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '问题线索', 'clue',    'jc_asset_source', '', 'default', 'N', '0', 'admin', sysdate(), 'M1 线索模块带出'),
 (2, '作风建设', 'conduct', 'jc_asset_source', '', 'default', 'N', '0', 'admin', sysdate(), 'E5 作风问题带出'),
 (3, '舞弊举报', 'petition','jc_asset_source', '', 'default', 'N', '0', 'admin', sysdate(), 'M3 举报模块带出'),
 (4, '手工登记', 'manual',  'jc_asset_source', '', 'default', 'N', '0', 'admin', sysdate(), '无来源单号直接登记');

-- 流转类型（与后端 writeFlow 的 flow_type 一致）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '扣押登记', 'seize',        'jc_asset_flow_type', '', 'default', 'N', '0', 'admin', sysdate(), '登记时自动落'),
 (2, '入库',     'store',        'jc_asset_flow_type', '', 'success', 'N', '0', 'admin', sysdate(), '0→1'),
 (3, '移交保管', 'handover',     'jc_asset_flow_type', '', 'primary', 'N', '0', 'admin', sysdate(), '保管人变更'),
 (4, '处置申请', 'disposeApply', 'jc_asset_flow_type', '', 'warning', 'N', '0', 'admin', sysdate(), '1→2'),
 (5, '处置执行', 'dispose',      'jc_asset_flow_type', '', 'danger',  'N', '0', 'admin', sysdate(), '2→3'),
 (6, '退还',     'refund',       'jc_asset_flow_type', '', 'info',    'N', '0', 'admin', sysdate(), '1→4');

-- ------------------------------------------------------------
-- 4. 业务参数
-- ------------------------------------------------------------
DELETE FROM sys_config WHERE config_key LIKE 'jiancha.asset.%';

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark) VALUES
 ('涉案物默认处置期限(天)', 'jiancha.asset.disposeDays', '30', 'Y', 'admin', sysdate(), '扣押登记未指定处置期限时，默认扣押日+该天数=处置截止，驱动红黄灯（超期红灯/临期黄灯）');

-- ------------------------------------------------------------
-- 5. 红黄灯日刷新定时任务（默认暂停，可在「系统监控 → 定时任务」启用）
-- ------------------------------------------------------------
DELETE FROM sys_job WHERE invoke_target = 'jcAssetScanTask.run()';

INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
VALUES ('涉案财物红黄灯刷新', 'DEFAULT', 'jcAssetScanTask.run()', '0 55 7 * * ?', '3', '1', '1', 'admin', sysdate(), '每日7:55：按处置期限刷新红黄灯（超期未处置=红灯，7日内到期=黄灯，终态熄灯，默认暂停）');


-- ------------------------------------------------------------
-- 6. 业务表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS jc_asset_flow;
DROP TABLE IF EXISTS jc_asset_info;

-- 涉案财物台账
CREATE TABLE jc_asset_info (
    asset_id         bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '财物主键',
    asset_no         varchar(32)  DEFAULT NULL COMMENT '财物编号 CA+日期+序号',
    source_type      varchar(32)  DEFAULT NULL COMMENT '来源类型 clue线索/conduct作风/petition举报/manual手工',
    source_no        varchar(64)  DEFAULT NULL COMMENT '来源单号快照',
    person_id        bigint(20)   DEFAULT NULL COMMENT '涉事人员ID',
    person_name      varchar(64)  DEFAULT NULL COMMENT '涉事人姓名（档案快照）',
    person_dept      varchar(128) DEFAULT NULL COMMENT '涉事人部门（档案快照）',
    item_name        varchar(255) DEFAULT NULL COMMENT '物品名称',
    item_category    varchar(32)  DEFAULT NULL COMMENT '物品类别（字典 jc_asset_category）',
    item_qty         int(11)      DEFAULT NULL COMMENT '数量',
    unit             varchar(16)  DEFAULT NULL COMMENT '单位（张/个/台/件/批）',
    estimated_value  decimal(14,2) DEFAULT NULL COMMENT '估值（元）',
    keeper           varchar(64)  DEFAULT NULL COMMENT '保管人',
    storage_place    varchar(255) DEFAULT NULL COMMENT '存放地点',
    seize_date       date         DEFAULT NULL COMMENT '扣押日期',
    store_date       date         DEFAULT NULL COMMENT '入库日期',
    dispose_deadline date         DEFAULT NULL COMMENT '处置期限',
    dispose_type     varchar(32)  DEFAULT NULL COMMENT '处置方式（字典 jc_asset_dispose）',
    dispose_date     date         DEFAULT NULL COMMENT '处置日期',
    dispose_desc     varchar(500) DEFAULT NULL COMMENT '处置说明',
    status           varchar(2)   DEFAULT '0' COMMENT '状态 0待入库/1暂存中/2处置中/3已处置/4已退还',
    warn_level       varchar(16)  DEFAULT '' COMMENT '预警灯色 red/yellow/空',
    create_by        varchar(64)  DEFAULT '' COMMENT '创建人',
    create_time      datetime     DEFAULT NULL COMMENT '创建时间',
    update_by        varchar(64)  DEFAULT '' COMMENT '更新人',
    update_time      datetime     DEFAULT NULL COMMENT '更新时间',
    remark           varchar(500) DEFAULT NULL COMMENT '备注',
    del_flag         char(1)      DEFAULT '0' COMMENT '删除标志 0正常 1删除',
    PRIMARY KEY (asset_id),
    UNIQUE KEY uk_asset_no (asset_no),
    KEY idx_asset_person (person_id),
    KEY idx_asset_status (status),
    KEY idx_asset_source (source_type, source_no),
    KEY idx_asset_warn (warn_level)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='涉案财物台账';

-- 涉案财物流转记录（全程保管链）
CREATE TABLE jc_asset_flow (
    flow_id      bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '流水主键',
    asset_id     bigint(20)   NOT NULL COMMENT '关联财物ID',
    flow_type    varchar(32)  DEFAULT NULL COMMENT '流转类型 seize扣押登记/store入库/handover移交/disposeApply处置申请/dispose处置执行/refund退还',
    from_status  varchar(2)   DEFAULT NULL COMMENT '流转前状态',
    to_status    varchar(2)   DEFAULT NULL COMMENT '流转后状态',
    operator     varchar(64)  DEFAULT NULL COMMENT '经办人',
    flow_date    date         DEFAULT NULL COMMENT '流转日期',
    flow_desc    varchar(500) DEFAULT NULL COMMENT '流转说明',
    voucher_no   varchar(64)  DEFAULT NULL COMMENT '凭据号',
    create_by    varchar(64)  DEFAULT '' COMMENT '创建人',
    create_time  datetime     DEFAULT NULL COMMENT '创建时间',
    del_flag     char(1)      DEFAULT '0' COMMENT '删除标志 0正常 1删除',
    PRIMARY KEY (flow_id),
    KEY idx_flow_asset (asset_id),
    KEY idx_flow_type (flow_type)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='涉案财物流转记录';
