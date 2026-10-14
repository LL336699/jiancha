-- ============================================================
-- 纪检监察助手 · 供应商廉洁管理模块 SQL（E4 供应商廉洁管理，企业版口径）
-- 追加执行，可重复运行（表结构 DROP+CREATE 会清空数据；有数据时改用文末 ALTER 方案）
-- 前提：jiancha_init.sql 已导入（sys_menu / sys_role_menu / sys_dict_type / sys_dict_data 已存在）
-- 段位：2800-2899 专供供应商廉洁管理
-- 依赖：E1 利益冲突申报（jc_conflict_declare / jc_conflict_item，删除标记 '2'）、
--       jc_person_relation（删除标记 '2'）、E3 整改（jc_rectify_issue）用于一键转整改
-- ============================================================

-- ------------------------------------------------------------
-- 1. 清理旧菜单（幂等：2800-2899 专供供应商廉洁管理）
-- ------------------------------------------------------------
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 2800 AND 2899;
DELETE FROM sys_menu      WHERE menu_id BETWEEN 2800 AND 2899;

-- ------------------------------------------------------------
-- 2. 供应商管理目录 + 菜单
--    2800 供应商管理（目录，挂在纪检监察 2000 下，排序 9）
--    2801 供应商档案  2802 关联排查  2803 廉洁协议  2804 黑名单  2805 廉洁看板
-- ------------------------------------------------------------
INSERT INTO sys_menu VALUES (2800, '供应商管理', '2000', '9', 'supplier', null, '', '', 1, 0, 'M', '0', '0', '', 'peoples', 'admin', sysdate(), '', null, '供应商廉洁管理业务目录（E4）');

-- 2801 供应商档案
INSERT INTO sys_menu VALUES (2801, '供应商档案', '2800', '1', 'supplierfile', 'jiancha/supplier/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:supplier:list', 'peoples', 'admin', sysdate(), '', null, '建档/准入审查/状态流转（待审查→正常/观察/禁入/黑名单）');
INSERT INTO sys_menu VALUES (2811, '档案查询', '2801', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:supplier:list',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2812, '档案详情', '2801', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:supplier:query', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2813, '档案新增', '2801', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:supplier:add',   '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2814, '档案修改', '2801', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:supplier:edit',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2815, '档案删除', '2801', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:supplier:remove','#', 'admin', sysdate(), '', null, '仅待审查状态可删');
INSERT INTO sys_menu VALUES (2816, '准入审查', '2801', '6', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:supplier:audit', '#', 'admin', sysdate(), '', null, 'pass/observe/deny');

-- 2802 关联排查
INSERT INTO sys_menu VALUES (2802, '关联排查', '2800', '2', 'supplierlink', 'jiancha/supplier/link/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:supplier:link:list', 'connection', 'admin', sysdate(), '', null, '员工申报关联企业 × 供应商库系统比对 + 人工排查');
INSERT INTO sys_menu VALUES (2821, '排查查询', '2802', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:supplier:link:list',   '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2822, '排查登记', '2802', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:supplier:link:add',    '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2823, '排查处置', '2802', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:supplier:link:handle', '#', 'admin', sysdate(), '', null, 'normal/observe/deny/clear');
INSERT INTO sys_menu VALUES (2824, '转整改',   '2802', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:supplier:link:rectify','#', 'admin', sysdate(), '', null, '一键转 E3 整改问题');
INSERT INTO sys_menu VALUES (2825, '系统比对', '2802', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:supplier:link:scan',   '#', 'admin', sysdate(), '', null, '手动触发比对');

-- 2803 廉洁协议
INSERT INTO sys_menu VALUES (2803, '廉洁协议', '2800', '3', 'supplieragreement', 'jiancha/supplier/agreement/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:supplier:agreement:list', 'documentation', 'admin', sysdate(), '', null, '签署/续签/解除 + 临期过期自动刷新');
INSERT INTO sys_menu VALUES (2831, '协议查询', '2803', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:supplier:agreement:list',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2832, '协议签署', '2803', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:supplier:agreement:add',   '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2833, '协议续签', '2803', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:supplier:agreement:renew', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2834, '协议解除', '2803', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:supplier:agreement:lift',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2835, '协议删除', '2803', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:supplier:agreement:remove','#', 'admin', sysdate(), '', null, '');

-- 2804 黑名单
INSERT INTO sys_menu VALUES (2804, '黑名单', '2800', '4', 'supplierblack', 'jiancha/supplier/black/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:supplier:black:list', 'delete', 'admin', sysdate(), '', null, '拉黑/到期自动解除/人工提前解除');
INSERT INTO sys_menu VALUES (2841, '黑名单查询', '2804', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:supplier:black:list',  '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2842, '黑名单详情', '2804', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:supplier:black:query', '#', 'admin', sysdate(), '', null, '');
INSERT INTO sys_menu VALUES (2843, '拉黑',       '2804', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:supplier:black',       '#', 'admin', sysdate(), '', null, '原因必填，期限可空=永久');
INSERT INTO sys_menu VALUES (2844, '解除黑名单', '2804', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:supplier:black:lift',  '#', 'admin', sysdate(), '', null, '意见必填');

-- 2805 廉洁看板
INSERT INTO sys_menu VALUES (2805, '廉洁看板', '2800', '5', 'supplierstat', 'jiancha/supplier/stat/index', '', '', 1, 0, 'C', '0', '0', 'jiancha:supplier:stat', 'chart', 'admin', sysdate(), '', null, '协议签署率/关联命中/黑名单趋势');
INSERT INTO sys_menu VALUES (2851, '统计查询', '2805', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'jiancha:supplier:stat', '#', 'admin', sysdate(), '', null, '');

-- ------------------------------------------------------------
-- 3. 授予超级管理员（role_id=1）全部新菜单
-- ------------------------------------------------------------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 2800 AND 2899;

-- ------------------------------------------------------------
-- 4. 数据字典
-- ------------------------------------------------------------
DELETE FROM sys_dict_data WHERE dict_type IN
    ('jc_supplier_status','jc_supplier_category','jc_supplier_link_type','jc_supplier_source','jc_agreement_status','jc_link_handle');
DELETE FROM sys_dict_type WHERE dict_type IN
    ('jc_supplier_status','jc_supplier_category','jc_supplier_link_type','jc_supplier_source','jc_agreement_status','jc_link_handle');

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
 ('供应商状态',   'jc_supplier_status',     '0', 'admin', sysdate(), '供应商廉洁档案状态'),
 ('供应商类别',   'jc_supplier_category',   '0', 'admin', sysdate(), '供应商类别'),
 ('供应商关联类型','jc_supplier_link_type',  '0', 'admin', sysdate(), '员工与供应商关联性质'),
 ('关联发现渠道', 'jc_supplier_source',     '0', 'admin', sysdate(), '关联排查发现渠道'),
 ('廉洁协议状态', 'jc_agreement_status',    '0', 'admin', sysdate(), '廉洁协议生命周期状态'),
 ('关联处置结果', 'jc_link_handle',         '0', 'admin', sysdate(), '关联排查处置结果');

-- 供应商状态（与后端状态机常量一致）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '待审查', '0', 'jc_supplier_status', '', 'info',    'Y', '0', 'admin', sysdate(), '建档初始状态'),
 (2, '正常',   '1', 'jc_supplier_status', '', 'success', 'N', '0', 'admin', sysdate(), '准入审查通过'),
 (3, '观察',   '2', 'jc_supplier_status', '', 'warning', 'N', '0', 'admin', sysdate(), '关联命中/审查观察期'),
 (4, '禁入',   '3', 'jc_supplier_status', '', 'danger',  'N', '0', 'admin', sysdate(), '准入审查否决'),
 (5, '黑名单', '4', 'jc_supplier_status', '', 'danger',  'N', '0', 'admin', sysdate(), '拉黑冻结合作');

-- 供应商类别
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '物资',   '物资', 'jc_supplier_category', '', 'primary', 'Y', '0', 'admin', sysdate(), '原材料/耗材'),
 (2, '设备',   '设备', 'jc_supplier_category', '', 'success', 'N', '0', 'admin', sysdate(), '生产设备'),
 (3, '工程',   '工程', 'jc_supplier_category', '', 'warning', 'N', '0', 'admin', sysdate(), '工程建设'),
 (4, '服务',   '服务', 'jc_supplier_category', '', 'info',    'N', '0', 'admin', sysdate(), '咨询/运维等服务'),
 (5, '物流',   '物流', 'jc_supplier_category', '', 'danger',  'N', '0', 'admin', sysdate(), '运输仓储'),
 (6, '其他',   '其他', 'jc_supplier_category', '', 'info',    'N', '0', 'admin', sysdate(), '其他');

-- 关联类型（系统比对 link_type 归一化目标）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '本人持股',   '本人持股',   'jc_supplier_link_type', '', 'danger',  'N', '0', 'admin', sysdate(), '员工本人持股/投资'),
 (2, '亲属持股',   '亲属持股',   'jc_supplier_link_type', '', 'danger',  'N', '0', 'admin', sysdate(), '亲属持股/投资'),
 (3, '本人任职',   '本人任职',   'jc_supplier_link_type', '', 'warning', 'N', '0', 'admin', sysdate(), '本人在供应商任职'),
 (4, '亲属任职',   '亲属任职',   'jc_supplier_link_type', '', 'warning', 'N', '0', 'admin', sysdate(), '亲属在供应商任职'),
 (5, '兼职取酬',   '兼职取酬',   'jc_supplier_link_type', '', 'warning', 'N', '0', 'admin', sysdate(), '兼职/劳务报酬'),
 (6, '实际控制',   '实际控制',   'jc_supplier_link_type', '', 'danger',  'N', '0', 'admin', sysdate(), '实际控制供应商'),
 (7, '其他',       '其他',       'jc_supplier_link_type', '', 'info',    'N', '0', 'admin', sysdate(), '其他关联');

-- 关联发现渠道
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '申报',     '申报',     'jc_supplier_source', '', 'primary', 'Y', '0', 'admin', sysdate(), '利益冲突申报带出'),
 (2, '排查',     '排查',     'jc_supplier_source', '', 'success', 'N', '0', 'admin', sysdate(), '专项排查'),
 (3, '举报',     '举报',     'jc_supplier_source', '', 'danger',  'N', '0', 'admin', sysdate(), '举报线索'),
 (4, '系统比对', '系统比对', 'jc_supplier_source', '', 'warning', 'N', '0', 'admin', sysdate(), 'Quartz 自动比对'),
 (5, '人工录入', '人工录入', 'jc_supplier_source', '', 'info',    'N', '0', 'admin', sysdate(), '人工登记');

-- 协议状态（与后端常量一致：0已签署 1临期 2已过期 3已解除）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '已签署', '0', 'jc_agreement_status', '', 'success', 'Y', '0', 'admin', sysdate(), '有效期内'),
 (2, '临期',   '1', 'jc_agreement_status', '', 'warning', 'N', '0', 'admin', sysdate(), '预警天数内到期'),
 (3, '已过期', '2', 'jc_agreement_status', '', 'danger',  'N', '0', 'admin', sysdate(), '超过到期日'),
 (4, '已解除', '3', 'jc_agreement_status', '', 'info',    'N', '0', 'admin', sysdate(), '办理解除');

-- 关联处置结果（0待处置 1正常 2观察 3禁入 4解除）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
 (1, '待处置', '0', 'jc_link_handle', '', 'info',    'Y', '0', 'admin', sysdate(), '新登记待处置'),
 (2, '正常',   '1', 'jc_link_handle', '', 'success', 'N', '0', 'admin', sysdate(), '核实无风险'),
 (3, '观察',   '2', 'jc_link_handle', '', 'warning', 'N', '0', 'admin', sysdate(), '联动档案转观察'),
 (4, '禁入',   '3', 'jc_link_handle', '', 'danger',  'N', '0', 'admin', sysdate(), '联动档案转禁入'),
 (5, '解除',   '4', 'jc_link_handle', '', 'info',    'N', '0', 'admin', sysdate(), '关联已解除');

-- ------------------------------------------------------------
-- 5. 业务参数
-- ------------------------------------------------------------
DELETE FROM sys_config WHERE config_key LIKE 'jiancha.supplier.%';

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark) VALUES
 ('廉洁协议临期预警天数', 'jiancha.supplier.agreement.warn.days', '30', 'N', 'admin', sysdate(), '协议距到期 ≤ 该天数转「临期」，Quartz 日调度刷新');

-- ------------------------------------------------------------
-- 6. 扫描定时任务（默认暂停，可在「系统监控 → 定时任务」启用）
-- ------------------------------------------------------------
DELETE FROM sys_job WHERE invoke_target = 'jcSupplierScanTask.scan()';

INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
VALUES ('供应商廉洁日扫描', 'DEFAULT', 'jcSupplierScanTask.scan()', '0 20 7 * * ?', '3', '1', '1', 'admin', sysdate(), '每日7:20：E1申报×供应商库比对 + 协议临期/过期刷新 + 黑名单到期自动解除（默认暂停）');

-- ------------------------------------------------------------
-- 7. 业务表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS jc_supplier_link;
DROP TABLE IF EXISTS jc_supplier_agreement;
DROP TABLE IF EXISTS jc_supplier_black;
DROP TABLE IF EXISTS jc_supplier;

-- 供应商档案表
CREATE TABLE jc_supplier (
    supplier_id    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '供应商ID',
    supplier_code  VARCHAR(32)  NOT NULL                COMMENT '供应商编号(SUP+yyyyMMdd+seq)',
    supplier_name  VARCHAR(200) NOT NULL                COMMENT '供应商名称',
    credit_code    VARCHAR(32)  NOT NULL                COMMENT '统一社会信用代码(关联比对依据)',
    category       VARCHAR(32)  DEFAULT ''              COMMENT '类别(字典 jc_supplier_category)',
    contact_name   VARCHAR(64)  DEFAULT ''              COMMENT '联系人',
    contact_phone  VARCHAR(32)  DEFAULT ''              COMMENT '联系电话',
    contact_email  VARCHAR(128) DEFAULT ''              COMMENT '电子邮箱',
    address        VARCHAR(255) DEFAULT ''              COMMENT '地址',
    status         CHAR(1)      DEFAULT '0'             COMMENT '状态(0待审查 1正常 2观察 3禁入 4黑名单)',
    admit_date     DATETIME     DEFAULT NULL            COMMENT '准入审查日期',
    black_until    DATETIME     DEFAULT NULL            COMMENT '禁入/黑名单到期日(NULL=永久)',
    remark         VARCHAR(500) DEFAULT ''              COMMENT '备注',
    create_by      VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time    DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by      VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time    DATETIME     DEFAULT NULL            COMMENT '更新时间',
    del_flag       CHAR(1)      DEFAULT '0'             COMMENT '删除标记(0正常 1删除)',
    PRIMARY KEY (supplier_id),
    UNIQUE KEY uk_supplier_code (supplier_code),
    UNIQUE KEY uk_supplier_name (supplier_name),
    UNIQUE KEY uk_supplier_credit (credit_code),
    KEY idx_supplier_status (status)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='供应商廉洁档案';

-- 关联排查记录表
CREATE TABLE jc_supplier_link (
    link_id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    supplier_id      BIGINT        NOT NULL                COMMENT '供应商ID',
    person_id        BIGINT        NOT NULL                COMMENT '关联员工ID(jc_person.person_id)',
    person_name      VARCHAR(64)   DEFAULT ''              COMMENT '关联员工姓名(冗余展示)',
    link_type        VARCHAR(32)   DEFAULT ''              COMMENT '关联类型(字典 jc_supplier_link_type)',
    hold_ratio       DECIMAL(6,2)  DEFAULT NULL            COMMENT '持股/出资比例(%)',
    source_type      VARCHAR(32)   DEFAULT '人工录入'      COMMENT '发现渠道(字典 jc_supplier_source)',
    evidence         VARCHAR(1000) DEFAULT ''              COMMENT '证据说明(申报/比对命中依据)',
    verify_status    VARCHAR(16)   DEFAULT '未核实'        COMMENT '核实状态(字典 jc_verify_status)',
    handle_result    CHAR(1)       DEFAULT '0'             COMMENT '处置结果(0待处置 1正常 2观察 3禁入 4解除)',
    handle_time      DATETIME      DEFAULT NULL            COMMENT '处置时间',
    handle_by        VARCHAR(64)   DEFAULT ''              COMMENT '处置人',
    handle_opinion   VARCHAR(1000) DEFAULT ''              COMMENT '处置意见',
    rectify_issue_id BIGINT        DEFAULT NULL            COMMENT '转整改问题ID(回指 E3)',
    remark           VARCHAR(500)  DEFAULT ''              COMMENT '备注',
    create_by        VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
    create_time      DATETIME      DEFAULT NULL            COMMENT '创建时间',
    update_by        VARCHAR(64)   DEFAULT ''              COMMENT '更新者',
    update_time      DATETIME      DEFAULT NULL            COMMENT '更新时间',
    del_flag         CHAR(1)       DEFAULT '0'             COMMENT '删除标记(0正常 1删除)',
    PRIMARY KEY (link_id),
    KEY idx_link_supplier (supplier_id),
    KEY idx_link_person (person_id),
    KEY idx_link_handle (handle_result),
    KEY idx_link_issue (rectify_issue_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='供应商关联排查记录';

-- 廉洁协议表
CREATE TABLE jc_supplier_agreement (
    agreement_id    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '协议ID',
    supplier_id     BIGINT       NOT NULL                COMMENT '供应商ID',
    agreement_no    VARCHAR(32)  NOT NULL                COMMENT '协议编号(HA+yyyyMMdd+seq)',
    sign_date       DATETIME     DEFAULT NULL            COMMENT '签署日期',
    expire_date     DATETIME     DEFAULT NULL            COMMENT '到期日期',
    file_name       VARCHAR(255) DEFAULT ''              COMMENT '协议文件名',
    file_url        VARCHAR(500) DEFAULT ''              COMMENT '协议文件URL',
    status          CHAR(1)      DEFAULT '0'             COMMENT '状态(0已签署 1临期 2已过期 3已解除)',
    renew_count     INT          DEFAULT 0               COMMENT '续签次数',
    last_renew_date DATETIME     DEFAULT NULL            COMMENT '最近续签日期',
    remark          VARCHAR(500) DEFAULT ''              COMMENT '备注',
    create_by       VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time     DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time     DATETIME     DEFAULT NULL            COMMENT '更新时间',
    del_flag        CHAR(1)      DEFAULT '0'             COMMENT '删除标记(0正常 1删除)',
    PRIMARY KEY (agreement_id),
    UNIQUE KEY uk_agreement_no (agreement_no),
    KEY idx_agreement_supplier (supplier_id),
    KEY idx_agreement_status (status),
    KEY idx_agreement_expire (expire_date)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='供应商廉洁协议';

-- 黑名单记录表
CREATE TABLE jc_supplier_black (
    black_id      BIGINT        NOT NULL AUTO_INCREMENT COMMENT '黑名单ID',
    supplier_id   BIGINT        NOT NULL                COMMENT '供应商ID',
    black_reason  VARCHAR(1000) NOT NULL                COMMENT '拉黑原因',
    black_date    DATETIME      DEFAULT NULL            COMMENT '拉黑日期',
    black_until   DATETIME      DEFAULT NULL            COMMENT '解除到期日(NULL=永久)',
    status        CHAR(1)       DEFAULT '0'             COMMENT '状态(0生效 1已解除)',
    lift_date     DATETIME      DEFAULT NULL            COMMENT '解除日期',
    lift_by       VARCHAR(64)   DEFAULT ''              COMMENT '解除操作人',
    lift_opinion  VARCHAR(1000) DEFAULT ''              COMMENT '解除意见',
    remark        VARCHAR(500)  DEFAULT ''              COMMENT '备注',
    create_by     VARCHAR(64)   DEFAULT ''              COMMENT '创建者',
    create_time   DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by     VARCHAR(64)   DEFAULT ''              COMMENT '更新者',
    update_time   DATETIME     DEFAULT NULL            COMMENT '更新时间',
    del_flag      CHAR(1)       DEFAULT '0'             COMMENT '删除标记(0正常 1删除)',
    PRIMARY KEY (black_id),
    KEY idx_black_supplier (supplier_id),
    KEY idx_black_status (status),
    KEY idx_black_until (black_until)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='供应商黑名单记录';

-- ------------------------------------------------------------
-- 8. 自检
-- ------------------------------------------------------------
SELECT '供应商菜单'   AS item, COUNT(1) AS cnt FROM sys_menu      WHERE menu_id BETWEEN 2800 AND 2899
UNION ALL SELECT '供应商字典项', COUNT(1) FROM sys_dict_data WHERE dict_type LIKE 'jc_supplier%' OR dict_type IN ('jc_agreement_status','jc_link_handle')
UNION ALL SELECT '供应商参数',   COUNT(1) FROM sys_config   WHERE config_key LIKE 'jiancha.supplier.%'
UNION ALL SELECT '供应商任务',   COUNT(1) FROM sys_job      WHERE invoke_target = 'jcSupplierScanTask.scan()'
UNION ALL SELECT '供应商表',     COUNT(1) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name IN ('jc_supplier','jc_supplier_link','jc_supplier_agreement','jc_supplier_black');

-- ------------------------------------------------------------
-- 【已有数据时改用以下 ALTER，避免 DROP 丢数据】
-- ALTER TABLE jc_supplier      ADD COLUMN black_until DATETIME DEFAULT NULL COMMENT '禁入/黑名单到期日(NULL=永久)' AFTER admit_date;
-- ALTER TABLE jc_supplier_link ADD COLUMN rectify_issue_id BIGINT DEFAULT NULL COMMENT '转整改问题ID(回指 E3)' AFTER handle_opinion;
-- ------------------------------------------------------------
