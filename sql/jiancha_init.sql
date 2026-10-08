-- ============================================================
-- 纪检监察助手（JIANCHA）业务库初始化脚本
-- 目标库：ry-vue（与 RuoYi 主库同库，遵循 RuoYi 规范）
-- 依赖：先执行 RuoYi 自带 sql/ry_20260417.sql
-- 规范：所有业务表必含 RuoYi 通用字段
--       create_by / create_time / update_by / update_time / del_flag / remark
-- ============================================================

-- ------------------------------------------------------------
-- 一期模块：M5 廉政档案 / M4 问题线索 / M1 知识库 / M2 文档 / M6 文书 / M8 看板
-- 二期模块：M3 信访举报 / M10 风险预警（预警表一期先建，供M4/M5 使用）
-- ------------------------------------------------------------

-- ============================================================
-- M5 廉政档案域
-- ============================================================

-- 干部廉政档案主表（一人一行）
DROP TABLE IF EXISTS jc_person;
CREATE TABLE jc_person (
  person_id         BIGINT(20)   NOT NULL AUTO_INCREMENT    COMMENT '人员ID',
  person_code       VARCHAR(64)  NOT NULL                COMMENT '人员编号',
  name              VARCHAR(64)  NOT NULL                COMMENT '姓名',
  id_card_enc       VARCHAR(256) DEFAULT ''              COMMENT '身份证号(加密存储)',
  gender            CHAR(1)      DEFAULT '0'             COMMENT '性别(字典 jc_gender)',
  birth_date        DATE         DEFAULT NULL           COMMENT '出生日期',
  dept_id           BIGINT(20)   DEFAULT NULL            COMMENT '所属部门ID',
  dept_path         VARCHAR(255) DEFAULT ''              COMMENT '部门路径(数据权限过滤用)',
  position          VARCHAR(64)  DEFAULT ''              COMMENT '职务',
  political_status  VARCHAR(32)  DEFAULT ''              COMMENT '政治面貌',
  entry_date        DATE         DEFAULT NULL           COMMENT '入职日期',
  status            VARCHAR(16)  DEFAULT '在岗'           COMMENT '在职状态(字典 jc_person_status)',
  risk_level        VARCHAR(16)  DEFAULT '正常'           COMMENT '风险等级(字典 jc_risk_level)',
  integrity_score   INT(11)      DEFAULT 100            COMMENT '廉政评分',
  avatar            VARCHAR(500) DEFAULT ''              COMMENT '照片路径',
  create_by         VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
  create_time       DATETIME     DEFAULT NULL           COMMENT '创建时间',
  update_by         VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
  update_time       DATETIME     DEFAULT NULL           COMMENT '更新时间',
  del_flag          CHAR(1)      DEFAULT '0'             COMMENT '删除标志(0存在 2删除)',
  remark            VARCHAR(500) DEFAULT NULL           COMMENT '备注',
  PRIMARY KEY (person_id),
  UNIQUE KEY uk_person_code (person_code),
  KEY idx_person_name (name),
  KEY idx_person_dept (dept_id),
  KEY idx_person_risk (risk_level)
) ENGINE=InnoDB AUTO_INCREMENT=1000 COMMENT = '干部廉政档案主表';

-- 任职经历
DROP TABLE IF EXISTS jc_person_career;
CREATE TABLE jc_person_career (
  career_id     BIGINT(20)   NOT NULL AUTO_INCREMENT    COMMENT '任职ID',
  person_id     BIGINT(20)   NOT NULL                COMMENT '人员ID',
  position      VARCHAR(64)  DEFAULT ''              COMMENT '职务',
  dept_name     VARCHAR(128) DEFAULT ''              COMMENT '部门名称',
  start_date    DATE         DEFAULT NULL           COMMENT '起始日期',
  end_date      DATE         DEFAULT NULL           COMMENT '结束日期',
  duty_desc     VARCHAR(500) DEFAULT NULL           COMMENT '职责描述',
  create_by     VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
  create_time   DATETIME     DEFAULT NULL           COMMENT '创建时间',
  update_by     VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
  update_time   DATETIME     DEFAULT NULL           COMMENT '更新时间',
  del_flag      CHAR(1)      DEFAULT '0'             COMMENT '删除标志(0存在 2删除)',
  remark        VARCHAR(500) DEFAULT NULL           COMMENT '备注',
  PRIMARY KEY (career_id),
  KEY idx_career_person (person_id)
) ENGINE=InnoDB AUTO_INCREMENT=1000 COMMENT = '干部任职经历';

-- 亲属关系及经商办企业情况
DROP TABLE IF EXISTS jc_person_relation;
CREATE TABLE jc_person_relation (
  relation_id      BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  person_id        BIGINT(20)   NOT NULL               COMMENT '人员ID',
  relation_type    VARCHAR(16)  DEFAULT ''             COMMENT '关系(字典 jc_relation_type: 本人/配偶/子女/父母/兄弟姐妹/其他亲属/同学/朋友)',
  relation_name    VARCHAR(64)  DEFAULT ''             COMMENT '关联人姓名',
  relation_work    VARCHAR(255) DEFAULT ''             COMMENT '任职单位/经营主体',
  relation_duty    VARCHAR(128) DEFAULT ''             COMMENT '职务',
  ent_name         VARCHAR(200) DEFAULT ''             COMMENT '关联企业/经营主体名称',
  ent_credit_code  VARCHAR(32)  DEFAULT ''             COMMENT '统一社会信用代码',
  relation_kind    VARCHAR(16)  DEFAULT ''             COMMENT '关联性质(字典 jc_relation_kind: 持股/任职/兼职取酬/实际控制/劳务报酬/其他)',
  hold_ratio       DECIMAL(6,2) DEFAULT NULL           COMMENT '持股/出资比例(%)',
  is_supplier      CHAR(1)      DEFAULT '0'            COMMENT '是否本公司供应商(0否 1是)',
  is_customer      CHAR(1)      DEFAULT '0'            COMMENT '是否本公司客户(0否 1是)',
  partner_type     VARCHAR(16)  DEFAULT ''             COMMENT '合作方类型(字典 jc_partner_type: 供应商/客户/承包商/服务商/其他)',
  start_date       DATE         DEFAULT NULL          COMMENT '关联起始日期',
  end_date         DATE         DEFAULT NULL          COMMENT '关联结束日期',
  is_abroad        CHAR(1)      DEFAULT '0'            COMMENT '是否境外(0否 1是)',
  is_key_position  CHAR(1)      DEFAULT '0'            COMMENT '本人是否关键岗位',
  verify_status    VARCHAR(16)  DEFAULT '未核实'        COMMENT '核实状态(字典 jc_verify_status: 未核实/已核实/存疑)',
  data_source      VARCHAR(16)  DEFAULT '人工录入'      COMMENT '数据来源(字典 jc_data_source: 申报/排查/举报/人工录入)',
  create_by        VARCHAR(64)  DEFAULT ''             COMMENT '创建者',
  create_time      DATETIME     DEFAULT NULL          COMMENT '创建时间',
  update_by        VARCHAR(64)  DEFAULT ''             COMMENT '更新者',
  update_time      DATETIME     DEFAULT NULL          COMMENT '更新时间',
  del_flag         CHAR(1)      DEFAULT '0'            COMMENT '删除标志(0存在 2删除)',
  remark           VARCHAR(500) DEFAULT NULL          COMMENT '备注',
  PRIMARY KEY (relation_id),
  KEY idx_relation_person (person_id),
  KEY idx_relation_name (relation_name),
  KEY idx_relation_ent (ent_name),
  KEY idx_relation_supplier (is_supplier)
) ENGINE=InnoDB AUTO_INCREMENT=1000 COMMENT = '关联关系及经商办企业/合作方任职情况(企业口径)';

-- 处分记录
DROP TABLE IF EXISTS jc_person_punish;
CREATE TABLE jc_person_punish (
  punish_id      BIGINT(20)   NOT NULL AUTO_INCREMENT   COMMENT '处分ID',
  person_id      BIGINT(20)   NOT NULL               COMMENT '人员ID',
  punish_type    VARCHAR(32)  DEFAULT ''             COMMENT '处分种类(字典 jc_punish_type)',
  punish_reason  VARCHAR(255) DEFAULT ''             COMMENT '处分事由',
  punish_date    DATE         DEFAULT NULL          COMMENT '处分日期',
  effective_date DATE         DEFAULT NULL          COMMENT '生效日期',
  related_case_no VARCHAR(64) DEFAULT ''             COMMENT '关联案件编号',
  create_by      VARCHAR(64)  DEFAULT ''             COMMENT '创建者',
  create_time    DATETIME     DEFAULT NULL          COMMENT '创建时间',
  update_by      VARCHAR(64)  DEFAULT ''             COMMENT '更新者',
  update_time    DATETIME     DEFAULT NULL          COMMENT '更新时间',
  del_flag       CHAR(1)      DEFAULT '0'            COMMENT '删除标志(0存在 2删除)',
  remark         VARCHAR(500) DEFAULT NULL          COMMENT '备注',
  PRIMARY KEY (punish_id),
  KEY idx_punish_person (person_id),
  KEY idx_punish_date (punish_date)
) ENGINE=InnoDB AUTO_INCREMENT=1000 COMMENT = '干部处分记录';

-- 廉政画像标签（全部可溯源）
DROP TABLE IF EXISTS jc_person_tag;
CREATE TABLE jc_person_tag (
  tag_id        BIGINT(20)   NOT NULL AUTO_INCREMENT   COMMENT '标签ID',
  person_id     BIGINT(20)   NOT NULL               COMMENT '人员ID',
  tag_type      VARCHAR(32)  DEFAULT ''             COMMENT '标签类型(字典 jc_tag_type)',
  tag_name      VARCHAR(64)  NOT NULL               COMMENT '标签名称',
  tag_level     VARCHAR(8)   DEFAULT '低'            COMMENT '严重度:高/中/低',
  tag_source    VARCHAR(255) DEFAULT ''             COMMENT '产生来源描述',
  source_table  VARCHAR(64)  DEFAULT ''             COMMENT '溯源表名',
  source_id     BIGINT(20)   DEFAULT NULL          COMMENT '溯源记录ID',
  create_by     VARCHAR(64)  DEFAULT ''             COMMENT '创建者',
  create_time   DATETIME     DEFAULT NULL          COMMENT '创建时间',
  update_by     VARCHAR(64)  DEFAULT ''             COMMENT '更新者',
  update_time   DATETIME     DEFAULT NULL          COMMENT '更新时间',
  del_flag      CHAR(1)      DEFAULT '0'            COMMENT '删除标志(0存在 2删除)',
  remark        VARCHAR(500) DEFAULT NULL          COMMENT '备注',
  PRIMARY KEY (tag_id),
  KEY idx_tag_person (person_id),
  KEY idx_tag_type (tag_type)
) ENGINE=InnoDB AUTO_INCREMENT=1000 COMMENT = '廉政画像标签(可溯源)';

-- ============================================================
-- M5-E1 利益冲突申报（企业版新增）
-- ============================================================

DROP TABLE IF EXISTS jc_conflict_declare;
CREATE TABLE jc_conflict_declare (
  declare_id     BIGINT(20)   NOT NULL AUTO_INCREMENT   COMMENT '申报ID',
  person_id      BIGINT(20)   NOT NULL               COMMENT '申报人ID',
  declare_type   VARCHAR(16)  DEFAULT '年度'          COMMENT '申报类型(字典 jc_declare_type: 年度/事项/专项)',
  declare_year   INT(11)      DEFAULT NULL           COMMENT '申报年度',
  declare_date   DATE         DEFAULT NULL          COMMENT '申报日期',
  status         VARCHAR(16)  DEFAULT '待核实'        COMMENT '状态(字典 jc_declare_status: 草稿/待核实/已核实/存疑)',
  verify_by      VARCHAR(64)  DEFAULT ''             COMMENT '核实人',
  verify_time    DATETIME     DEFAULT NULL          COMMENT '核实时间',
  verify_result  VARCHAR(500) DEFAULT NULL          COMMENT '核实结论',
  create_by      VARCHAR(64)  DEFAULT ''             COMMENT '创建者',
  create_time    DATETIME     DEFAULT NULL          COMMENT '创建时间',
  update_by      VARCHAR(64)  DEFAULT ''             COMMENT '更新者',
  update_time    DATETIME     DEFAULT NULL          COMMENT '更新时间',
  del_flag       CHAR(1)      DEFAULT '0'            COMMENT '删除标志(0存在 2删除)',
  remark         VARCHAR(500) DEFAULT NULL          COMMENT '备注',
  PRIMARY KEY (declare_id),
  KEY idx_declare_person (person_id),
  KEY idx_declare_status (status)
) ENGINE=InnoDB AUTO_INCREMENT=1000 COMMENT = '利益冲突申报单';

DROP TABLE IF EXISTS jc_conflict_item;
CREATE TABLE jc_conflict_item (
  item_id         BIGINT(20)   NOT NULL AUTO_INCREMENT   COMMENT '明细ID',
  declare_id      BIGINT(20)   NOT NULL               COMMENT '申报单ID',
  item_type       VARCHAR(16)  DEFAULT ''             COMMENT '事项类型(字典 jc_conflict_item_type: 经商办企业/兼职取酬/亲属从业/对外投资/关联交易/劳务报酬/其他)',
  item_desc       VARCHAR(500) DEFAULT ''             COMMENT '事项描述',
  ent_name        VARCHAR(200) DEFAULT ''             COMMENT '关联企业/经营主体名称',
  ent_credit_code VARCHAR(32)  DEFAULT ''             COMMENT '统一社会信用代码',
  hold_ratio      DECIMAL(6,2) DEFAULT NULL           COMMENT '持股/出资比例(%)',
  relation_person VARCHAR(64)  DEFAULT ''             COMMENT '涉及关联人',
  is_conflict     CHAR(1)      DEFAULT '0'            COMMENT '是否构成利益冲突(0否 1是)',
  handle_advice   VARCHAR(500) DEFAULT NULL          COMMENT '处置建议',
  create_by       VARCHAR(64)  DEFAULT ''             COMMENT '创建者',
  create_time     DATETIME     DEFAULT NULL          COMMENT '创建时间',
  update_by       VARCHAR(64)  DEFAULT ''             COMMENT '更新者',
  update_time     DATETIME     DEFAULT NULL          COMMENT '更新时间',
  del_flag        CHAR(1)      DEFAULT '0'            COMMENT '删除标志(0存在 2删除)',
  remark          VARCHAR(500) DEFAULT NULL          COMMENT '备注',
  PRIMARY KEY (item_id),
  KEY idx_item_declare (declare_id),
  KEY idx_item_ent (ent_name)
) ENGINE=InnoDB AUTO_INCREMENT=1000 COMMENT = '利益冲突申报事项明细';

-- ============================================================
-- M4 问题线索域
-- ============================================================

DROP TABLE IF EXISTS jc_clue;
CREATE TABLE jc_clue (
  clue_id         BIGINT(20)   NOT NULL AUTO_INCREMENT   COMMENT '线索ID',
  clue_no         VARCHAR(32)  NOT NULL               COMMENT '线索编号',
  source_type     VARCHAR(32)  NOT NULL               COMMENT '来源类型(字典 jc_clue_source)',
  violation_type  VARCHAR(32)  DEFAULT ''             COMMENT '问题类型(字典 jc_violation_type)',
  person_id       BIGINT(20)   DEFAULT NULL          COMMENT '涉及人员ID',
  related_persons VARCHAR(1000) DEFAULT ''            COMMENT '关联人员(多人,逗号分隔)',
  summary         VARCHAR(1000) NOT NULL              COMMENT '线索摘要',
  detail          TEXT                                 COMMENT '详细情况',
  dept_id         BIGINT(20)   NOT NULL               COMMENT '承办部门ID',
  handler_id      BIGINT(20)   DEFAULT NULL          COMMENT '当前处理人ID',
  status          VARCHAR(16)  NOT NULL               COMMENT '办理状态(字典 jc_clue_status)',
  disposition     VARCHAR(32)  DEFAULT ''             COMMENT '处置方式(字典 jc_clue_disposition)',
  deadline        DATE         DEFAULT NULL          COMMENT '办理截止日期',
  warn_level      VARCHAR(8)   DEFAULT ''             COMMENT '预警级别(字典 jc_warn_level)',
  is_duplicate    CHAR(1)      DEFAULT '0'            COMMENT '是否重复线索(0否 1是)',
  duplicate_of    BIGINT(20)   DEFAULT NULL          COMMENT '重复指向的线索ID',
  source_ref      VARCHAR(255) DEFAULT ''             COMMENT '来源文号/编号',
  create_by       VARCHAR(64)  DEFAULT ''             COMMENT '创建者',
  create_time     DATETIME     DEFAULT NULL          COMMENT '创建时间',
  update_by       VARCHAR(64)  DEFAULT ''             COMMENT '更新者',
  update_time     DATETIME     DEFAULT NULL          COMMENT '更新时间',
  del_flag        CHAR(1)      DEFAULT '0'            COMMENT '删除标志(0存在 2删除)',
  remark          VARCHAR(500) DEFAULT NULL          COMMENT '备注',
  PRIMARY KEY (clue_id),
  UNIQUE KEY uk_clue_no (clue_no),
  KEY idx_clue_dept (dept_id),
  KEY idx_clue_status (status),
  KEY idx_clue_deadline (deadline, status),
  KEY idx_clue_person (person_id),
  KEY idx_clue_source (source_type)
) ENGINE=InnoDB AUTO_INCREMENT=1000 COMMENT = '问题线索主表';

DROP TABLE IF EXISTS jc_clue_trace;
CREATE TABLE jc_clue_trace (
  trace_id      BIGINT(20)  NOT NULL AUTO_INCREMENT    COMMENT '轨迹ID',
  clue_id       BIGINT(20)  NOT NULL                COMMENT '线索ID',
  action        VARCHAR(32) NOT NULL                COMMENT '动作(字典 jc_clue_action)',
  action_desc   VARCHAR(1000) DEFAULT ''            COMMENT '动作说明',
  before_status VARCHAR(16) DEFAULT ''              COMMENT '变更前状态',
  after_status  VARCHAR(16) DEFAULT ''              COMMENT '变更后状态',
  operator_id   BIGINT(20)  NOT NULL                COMMENT '操作人ID',
  operator_name VARCHAR(64) DEFAULT ''              COMMENT '操作人姓名(冗余)',
  operate_time  DATETIME    NOT NULL                COMMENT '操作时间',
  create_by     VARCHAR(64) DEFAULT ''              COMMENT '创建者',
  create_time   DATETIME    DEFAULT NULL           COMMENT '创建时间',
  update_by     VARCHAR(64) DEFAULT ''              COMMENT '更新者',
  update_time   DATETIME    DEFAULT NULL           COMMENT '更新时间',
  del_flag      CHAR(1)     DEFAULT '0'             COMMENT '删除标志(0存在 2删除)',
  remark        VARCHAR(500) DEFAULT NULL           COMMENT '备注',
  PRIMARY KEY (trace_id),
  KEY idx_trace_clue (clue_id),
  KEY idx_trace_time (operate_time)
) ENGINE=InnoDB AUTO_INCREMENT=1000 COMMENT = '问题线索办理轨迹(全程留痕)';

-- ============================================================
-- M1 知识库域
-- ============================================================

DROP TABLE IF EXISTS jc_kb_category;
CREATE TABLE jc_kb_category (
  category_id  BIGINT(20)   NOT NULL AUTO_INCREMENT    COMMENT '分类ID',
  parent_id    BIGINT(20)   DEFAULT 0                COMMENT '父分类ID',
  ancestors    VARCHAR(255) DEFAULT ''               COMMENT '祖级列表',
  cat_name     VARCHAR(64)  NOT NULL                COMMENT '分类名称',
  cat_type     VARCHAR(16)  DEFAULT ''              COMMENT '分类类型(字典 jc_kb_cat_type)',
  order_num    INT(4)       DEFAULT 0                COMMENT '显示顺序',
  status       CHAR(1)      DEFAULT '0'             COMMENT '状态(0正常 1停用)',
  create_by    VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
  create_time  DATETIME     DEFAULT NULL           COMMENT '创建时间',
  update_by    VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
  update_time  DATETIME     DEFAULT NULL           COMMENT '更新时间',
  del_flag     CHAR(1)      DEFAULT '0'             COMMENT '删除标志(0存在 2删除)',
  remark       VARCHAR(500) DEFAULT NULL           COMMENT '备注',
  PRIMARY KEY (category_id),
  KEY idx_kbcat_parent (parent_id)
) ENGINE=InnoDB AUTO_INCREMENT=1000 COMMENT = '知识库分类(树形)';

DROP TABLE IF EXISTS jc_kb_document;
CREATE TABLE jc_kb_document (
  doc_id         BIGINT(20)   NOT NULL AUTO_INCREMENT   COMMENT '文档ID',
  doc_name       VARCHAR(255) NOT NULL               COMMENT '文档名称',
  category_id    BIGINT(20)   NOT NULL               COMMENT '分类ID',
  file_path      VARCHAR(500) DEFAULT ''             COMMENT '文件存储路径',
  file_hash      VARCHAR(64)  DEFAULT ''             COMMENT '文件哈希(去重用)',
  file_size      BIGINT(20)   DEFAULT 0              COMMENT '文件大小(字节)',
  doc_type       VARCHAR(16)  DEFAULT ''             COMMENT '文件类型(PDF/DOCX/TXT/MD)',
  version        VARCHAR(16)  DEFAULT 'v1'           COMMENT '版本号',
  issuer         VARCHAR(128) DEFAULT ''             COMMENT '发文机关/来源',
  doc_no         VARCHAR(64)  DEFAULT ''             COMMENT '文号',
  effective_date DATE         DEFAULT NULL          COMMENT '生效日期',
  expire_date    DATE         DEFAULT NULL          COMMENT '失效日期',
  chunk_count    INT(11)      DEFAULT 0              COMMENT '分块总数',
  status         VARCHAR(16)  DEFAULT '待处理'        COMMENT '状态(字典 jc_kb_doc_status)',
  parse_error    VARCHAR(1000) DEFAULT NULL         COMMENT '解析错误信息',
  create_by      VARCHAR(64)  DEFAULT ''             COMMENT '创建者',
  create_time    DATETIME     DEFAULT NULL          COMMENT '创建时间',
  update_by      VARCHAR(64)  DEFAULT ''             COMMENT '更新者',
  update_time    DATETIME     DEFAULT NULL          COMMENT '更新时间',
  del_flag       CHAR(1)      DEFAULT '0'            COMMENT '删除标志(0存在 2删除)',
  remark         VARCHAR(500) DEFAULT NULL          COMMENT '备注',
  PRIMARY KEY (doc_id),
  KEY idx_kbdoc_category (category_id),
  KEY idx_kbdoc_hash (file_hash),
  KEY idx_kbdoc_date (effective_date, expire_date),
  KEY idx_kbdoc_status (status)
) ENGINE=InnoDB AUTO_INCREMENT=1000 COMMENT = '知识库文档';

DROP TABLE IF EXISTS jc_kb_chunk;
CREATE TABLE jc_kb_chunk (
  chunk_id     BIGINT(20)   NOT NULL AUTO_INCREMENT    COMMENT '分块ID',
  doc_id       BIGINT(20)   NOT NULL                COMMENT '文档ID',
  chunk_index  INT(11)      NOT NULL DEFAULT 0      COMMENT '分块序号',
  content      MEDIUMTEXT   NOT NULL                COMMENT '分块内容',
  content_hash VARCHAR(64)  DEFAULT ''              COMMENT '内容哈希',
  page_no      INT(11)      DEFAULT NULL           COMMENT '页码',
  clause_no    VARCHAR(64)  DEFAULT ''              COMMENT '法规条款号',
  char_start   INT(11)      DEFAULT NULL           COMMENT '起始字符位置',
  char_end     INT(11)      DEFAULT NULL           COMMENT '结束字符位置',
  create_by    VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
  create_time  DATETIME     DEFAULT NULL           COMMENT '创建时间',
  update_by    VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
  update_time  DATETIME     DEFAULT NULL           COMMENT '更新时间',
  del_flag     CHAR(1)      DEFAULT '0'             COMMENT '删除标志(0存在 2删除)',
  remark       VARCHAR(500) DEFAULT NULL           COMMENT '备注',
  PRIMARY KEY (chunk_id),
  KEY idx_chunk_doc (doc_id),
  KEY idx_chunk_clause (clause_no),
  KEY idx_chunk_hash (content_hash)
) ENGINE=InnoDB AUTO_INCREMENT=100000 COMMENT = '知识库文档分块(带出处锚点)';

-- ============================================================
-- M2 文档中心域
-- ============================================================

DROP TABLE IF EXISTS jc_document;
CREATE TABLE jc_document (
  doc_id          BIGINT(20)   NOT NULL AUTO_INCREMENT  COMMENT '文档ID',
  doc_name        VARCHAR(255) NOT NULL              COMMENT '文档名称',
  doc_type        VARCHAR(16)  DEFAULT ''            COMMENT '文件类型',
  file_path       VARCHAR(500) DEFAULT ''            COMMENT '文件存储路径',
  file_size       BIGINT(20)   DEFAULT 0             COMMENT '文件大小',
  file_hash       VARCHAR(64)  DEFAULT ''            COMMENT '文件哈希',
  biz_type        VARCHAR(32)  DEFAULT ''            COMMENT '业务类型(字典 jc_doc_biz_type)',
  related_case_no VARCHAR(64)  DEFAULT ''            COMMENT '关联案件编号',
  related_person_id BIGINT(20) DEFAULT NULL         COMMENT '关联人员ID',
  parse_status    VARCHAR(16)  DEFAULT '待解析'       COMMENT '解析状态',
  summary         MEDIUMTEXT                          COMMENT '结构化摘要',
  key_info        TEXT                                COMMENT '抽取的关键信息(JSON)',
  parse_error     VARCHAR(1000) DEFAULT NULL        COMMENT '解析错误信息',
  create_by       VARCHAR(64)  DEFAULT ''            COMMENT '创建者',
  create_time     DATETIME     DEFAULT NULL         COMMENT '创建时间',
  update_by       VARCHAR(64)  DEFAULT ''            COMMENT '更新者',
  update_time     DATETIME     DEFAULT NULL         COMMENT '更新时间',
  del_flag        CHAR(1)      DEFAULT '0'           COMMENT '删除标志(0存在 2删除)',
  remark          VARCHAR(500) DEFAULT NULL         COMMENT '备注',
  PRIMARY KEY (doc_id),
  KEY idx_doc_hash (file_hash),
  KEY idx_doc_biz (biz_type),
  KEY idx_doc_related (related_case_no, related_person_id),
  KEY idx_doc_status (parse_status)
) ENGINE=InnoDB AUTO_INCREMENT=1000 COMMENT = '业务文档中心';

-- ============================================================
-- M3 信访举报域（二期）
-- ============================================================

DROP TABLE IF EXISTS jc_petition;
CREATE TABLE jc_petition (
  petition_id         BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  petition_no         VARCHAR(32)  NOT NULL             COMMENT '受理编号',
  receipt_hash        VARCHAR(128) NOT NULL             COMMENT '回执码(Argon2id哈希,不存明文)',
  receipt_salt        VARCHAR(64)  NOT NULL             COMMENT '回执码盐值',
  is_anonymous        CHAR(1)      DEFAULT '1'          COMMENT '是否匿名(0实名 1匿名)',
  reporter_name_enc   VARCHAR(256) DEFAULT ''           COMMENT '举报人姓名(加密)',
  reporter_phone_enc  VARCHAR(256) DEFAULT ''           COMMENT '举报人电话(加密)',
  reporter_idcard_enc VARCHAR(256) DEFAULT ''           COMMENT '举报人身份证(加密)',
  reporter_relation   VARCHAR(64)  DEFAULT ''           COMMENT '与被举报人关系',
  target_person_id    BIGINT(20)   DEFAULT NULL        COMMENT '被举报人ID',
  target_desc         VARCHAR(1000) DEFAULT ''          COMMENT '被举报对象描述(匿名时填)',
  violation_type      VARCHAR(32)  DEFAULT ''           COMMENT '违纪类型',
  content             TEXT         NOT NULL             COMMENT '举报内容',
  source_channel      VARCHAR(16)  DEFAULT '网站'        COMMENT '来源渠道(字典 jc_petition_channel)',
  status              VARCHAR(16)  NOT NULL             COMMENT '办理状态(字典 jc_petition_status)',
  dept_id             BIGINT(20)   DEFAULT NULL        COMMENT '承办部门ID',
  deadline            DATE         DEFAULT NULL        COMMENT '办理截止日期',
  warn_level          VARCHAR(8)   DEFAULT ''           COMMENT '预警级别',
  create_by           VARCHAR(64)  DEFAULT ''           COMMENT '创建者',
  create_time         DATETIME     DEFAULT NULL        COMMENT '创建时间',
  update_by           VARCHAR(64)  DEFAULT ''           COMMENT '更新者',
  update_time         DATETIME     DEFAULT NULL        COMMENT '更新时间',
  del_flag            CHAR(1)      DEFAULT '0'          COMMENT '删除标志(0存在 2归档 禁止物理删除)',
  remark              VARCHAR(500) DEFAULT NULL        COMMENT '备注',
  PRIMARY KEY (petition_id),
  UNIQUE KEY uk_petition_receipt (receipt_hash),
  KEY idx_petition_no (petition_no),
  KEY idx_petition_target (target_person_id),
  KEY idx_petition_status (status),
  KEY idx_petition_dept (dept_id)
) ENGINE=InnoDB AUTO_INCREMENT=1000 COMMENT = '信访举报件(禁止物理删除)';

DROP TABLE IF EXISTS jc_petition_trace;
CREATE TABLE jc_petition_trace (
  trace_id      BIGINT(20)  NOT NULL AUTO_INCREMENT    COMMENT '轨迹ID',
  petition_id   BIGINT(20)  NOT NULL                COMMENT '举报ID',
  action        VARCHAR(32) NOT NULL                COMMENT '动作',
  action_desc   VARCHAR(1000) DEFAULT ''            COMMENT '动作说明',
  before_status VARCHAR(16) DEFAULT ''              COMMENT '变更前状态',
  after_status  VARCHAR(16) DEFAULT ''              COMMENT '变更后状态',
  operator_id   BIGINT(20)  DEFAULT NULL           COMMENT '操作人ID',
  operate_time  DATETIME    NOT NULL                COMMENT '操作时间',
  create_by     VARCHAR(64) DEFAULT ''              COMMENT '创建者',
  create_time   DATETIME    DEFAULT NULL           COMMENT '创建时间',
  update_by     VARCHAR(64) DEFAULT ''              COMMENT '更新者',
  update_time   DATETIME    DEFAULT NULL           COMMENT '更新时间',
  del_flag      CHAR(1)     DEFAULT '0'             COMMENT '删除标志(0存在 2删除)',
  remark        VARCHAR(500) DEFAULT NULL           COMMENT '备注',
  PRIMARY KEY (trace_id),
  KEY idx_ptrace_petition (petition_id)
) ENGINE=InnoDB AUTO_INCREMENT=1000 COMMENT = '信访举报办理轨迹';

DROP TABLE IF EXISTS jc_petition_attach;
CREATE TABLE jc_petition_attach (
  attach_id    BIGINT(20)   NOT NULL AUTO_INCREMENT   COMMENT '附件ID',
  petition_id  BIGINT(20)   NOT NULL               COMMENT '举报ID',
  file_name    VARCHAR(255) DEFAULT ''             COMMENT '文件名',
  file_path    VARCHAR(500) DEFAULT ''             COMMENT '文件存储路径',
  file_hash    VARCHAR(64)  NOT NULL               COMMENT '文件哈希(防篡改)',
  file_size    BIGINT(20)   DEFAULT 0              COMMENT '文件大小',
  uploader_type VARCHAR(16) DEFAULT '举报人'        COMMENT '上传人类型',
  create_by    VARCHAR(64)  DEFAULT ''             COMMENT '创建者',
  create_time  DATETIME     DEFAULT NULL          COMMENT '创建时间',
  update_by    VARCHAR(64)  DEFAULT ''             COMMENT '更新者',
  update_time  DATETIME     DEFAULT NULL          COMMENT '更新时间',
  del_flag     CHAR(1)      DEFAULT '0'            COMMENT '删除标志(0存在 2删除)',
  remark       VARCHAR(500) DEFAULT NULL          COMMENT '备注',
  PRIMARY KEY (attach_id),
  KEY idx_pattach_petition (petition_id),
  KEY idx_pattach_hash (file_hash)
) ENGINE=InnoDB AUTO_INCREMENT=1000 COMMENT = '信访举报附件';

-- ============================================================
-- 预警与 AI 日志（一期先建，供 M4/M5/M8 使用）
-- ============================================================

DROP TABLE IF EXISTS jc_warn_task;
CREATE TABLE jc_warn_task (
  warn_id       BIGINT(20)   NOT NULL AUTO_INCREMENT   COMMENT '预警ID',
  warn_type     VARCHAR(32)  NOT NULL               COMMENT '预警类型(对应12类,字典 jc_warn_type)',
  warn_level    VARCHAR(8)   NOT NULL               COMMENT '预警级别(黄灯/红灯)',
  biz_type      VARCHAR(32)  DEFAULT ''             COMMENT '业务类型(字典 jc_warn_biz_type)',
  biz_id        BIGINT(20)   DEFAULT NULL          COMMENT '关联业务ID',
  person_id     BIGINT(20)   DEFAULT NULL          COMMENT '涉及人员ID',
  dept_id       BIGINT(20)   NOT NULL               COMMENT '责任部门ID',
  title         VARCHAR(255) NOT NULL               COMMENT '预警标题',
  content       VARCHAR(2000) DEFAULT ''            COMMENT '预警内容',
  status        VARCHAR(16)  DEFAULT '待处理'        COMMENT '处理状态(字典 jc_warn_status)',
  handler_id    BIGINT(20)   DEFAULT NULL          COMMENT '处理人ID',
  handle_time   DATETIME     DEFAULT NULL          COMMENT '处理时间',
  handle_result VARCHAR(2000) DEFAULT ''            COMMENT '处理结果',
  create_by     VARCHAR(64)  DEFAULT ''             COMMENT '创建者',
  create_time   DATETIME     DEFAULT NULL          COMMENT '创建时间',
  update_by     VARCHAR(64)  DEFAULT ''             COMMENT '更新者',
  update_time   DATETIME     DEFAULT NULL          COMMENT '更新时间',
  del_flag      CHAR(1)      DEFAULT '0'            COMMENT '删除标志(0存在 2删除)',
  remark        VARCHAR(500) DEFAULT NULL          COMMENT '备注',
  PRIMARY KEY (warn_id),
  KEY idx_warn_dept (dept_id),
  KEY idx_warn_status (status),
  KEY idx_warn_level (warn_level),
  KEY idx_warn_type (warn_type)
) ENGINE=InnoDB AUTO_INCREMENT=1000 COMMENT = '预警任务(12类风险)';

DROP TABLE IF EXISTS jc_ai_chat_log;
CREATE TABLE jc_ai_chat_log (
  log_id      BIGINT(20)   NOT NULL AUTO_INCREMENT    COMMENT '日志ID',
  session_id  VARCHAR(64)  NOT NULL                COMMENT '会话ID',
  user_id     BIGINT(20)   NOT NULL                COMMENT '用户ID',
  user_name   VARCHAR(64)  DEFAULT ''              COMMENT '用户姓名',
  biz_module  VARCHAR(32)  DEFAULT '知识库问答'       COMMENT '业务模块',
  question    VARCHAR(2000) NOT NULL               COMMENT '问题内容',
  answer      MEDIUMTEXT                          COMMENT '回答内容',
  citations   TEXT                COMMENT '引用出处列表(JSON)',
  model_name  VARCHAR(64)  DEFAULT ''              COMMENT '模型名称',
  provider    VARCHAR(32)  DEFAULT ''              COMMENT '供应商(openai/ollama)',
  token_used  INT(11)      DEFAULT 0               COMMENT '消耗token数',
  cost        DECIMAL(12,6) DEFAULT 0.000000       COMMENT '费用',
  status      VARCHAR(16)  DEFAULT '成功'            COMMENT '状态(成功/失败)',
  error_msg   VARCHAR(1000) DEFAULT NULL         COMMENT '错误信息',
  elapsed_ms  INT(11)      DEFAULT 0               COMMENT '耗时(毫秒)',
  create_by   VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
  create_time DATETIME     DEFAULT NULL           COMMENT '创建时间',
  update_by   VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
  update_time DATETIME     DEFAULT NULL           COMMENT '更新时间',
  del_flag    CHAR(1)      DEFAULT '0'             COMMENT '删除标志(0存在 2删除)',
  remark      VARCHAR(500) DEFAULT NULL           COMMENT '备注',
  PRIMARY KEY (log_id),
  KEY idx_chat_session (session_id),
  KEY idx_chat_user (user_id),
  KEY idx_chat_time (create_time),
  KEY idx_chat_biz (biz_module)
) ENGINE=InnoDB AUTO_INCREMENT=1000 COMMENT = 'AI问答日志(审计与成本核算)';

-- ============================================================
-- M6 文书辅助域
-- ============================================================

DROP TABLE IF EXISTS jc_doc_template;
CREATE TABLE jc_doc_template (
  template_id   BIGINT(20)   NOT NULL AUTO_INCREMENT  COMMENT '模板ID',
  template_name VARCHAR(128) NOT NULL              COMMENT '模板名称',
  doc_type      VARCHAR(32)  DEFAULT ''            COMMENT '文书类型(字典 jc_writ_type)',
  file_path     VARCHAR(500) DEFAULT ''            COMMENT '模板文件路径',
  content       MEDIUMTEXT                         COMMENT '模板内容(结构化)',
  version       VARCHAR(16)  DEFAULT 'v1'          COMMENT '版本号',
  status        CHAR(1)      DEFAULT '0'           COMMENT '状态(0正常 1停用)',
  create_by     VARCHAR(64)  DEFAULT ''            COMMENT '创建者',
  create_time   DATETIME     DEFAULT NULL         COMMENT '创建时间',
  update_by     VARCHAR(64)  DEFAULT ''            COMMENT '更新者',
  update_time   DATETIME     DEFAULT NULL         COMMENT '更新时间',
  del_flag      CHAR(1)      DEFAULT '0'           COMMENT '删除标志(0存在 2删除)',
  remark        VARCHAR(500) DEFAULT NULL         COMMENT '备注',
  PRIMARY KEY (template_id),
  KEY idx_tpl_type (doc_type)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT '纪检文书模板';

DROP TABLE IF EXISTS jc_doc_generated;
CREATE TABLE jc_doc_generated (
  gen_id        BIGINT(20)   NOT NULL AUTO_INCREMENT   COMMENT '生成记录ID',
  template_id   BIGINT(20)   DEFAULT NULL           COMMENT '模板ID',
  doc_title     VARCHAR(255) NOT NULL               COMMENT '文书标题',
  doc_type      VARCHAR(32)  DEFAULT ''             COMMENT '文书类型',
  content       MEDIUMTEXT                          COMMENT '文书内容',
  file_path     VARCHAR(500) DEFAULT ''             COMMENT '导出文件路径',
  related_case_no VARCHAR(64) DEFAULT ''            COMMENT '关联案件编号',
  related_person_id BIGINT(20) DEFAULT NULL        COMMENT '关联人员ID',
  gen_type      VARCHAR(16)  DEFAULT 'AI生成'        COMMENT '生成方式(AI生成/人工填报)',
  is_confirmed  CHAR(1)      DEFAULT '0'            COMMENT '是否人工确认(1是 0否)',
  confirm_by    VARCHAR(64)  DEFAULT ''             COMMENT '确认人',
  confirm_time  DATETIME     DEFAULT NULL          COMMENT '确认时间',
  create_by     VARCHAR(64)  DEFAULT ''             COMMENT '创建者',
  create_time   DATETIME     DEFAULT NULL          COMMENT '创建时间',
  update_by     VARCHAR(64)  DEFAULT ''             COMMENT '更新者',
  update_time   DATETIME     DEFAULT NULL          COMMENT '更新时间',
  del_flag      CHAR(1)      DEFAULT '0'            COMMENT '删除标志(0存在 2删除)',
  remark        VARCHAR(500) DEFAULT NULL          COMMENT '备注',
  PRIMARY KEY (gen_id),
  KEY idx_gen_type (doc_type),
  KEY idx_gen_case (related_case_no)
) ENGINE=InnoDB AUTO_INCREMENT=1000 COMMENT = 'AI生成文书记录(需人工确认)';

-- ============================================================
-- 数据字典初始化
-- ============================================================

-- 字典类型登记（P2 遗漏，P3 补齐；RuoYi 字典管理页依赖此表）
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('线索来源','jc_clue_source','0','admin',NOW(),'企业内部线索来源'),
('问题类型','jc_violation_type','0','admin',NOW(),'企业违规问题类型'),
('线索办理状态','jc_clue_status','0','admin',NOW(),'线索办理流程状态'),
('线索办理动作','jc_clue_action','0','admin',NOW(),'线索流转动作'),
('线索处置方式','jc_clue_disposition','0','admin',NOW(),'线索最终处置方式');

-- 问题类型（企业口径，value 1-8，替换原党政六大纪律）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (1,'商业贿赂','1','jc_violation_type','','danger','Y','0','admin',NOW(),'收受回扣、好处费等');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (2,'职务侵占','2','jc_violation_type','','danger','N','0','admin',NOW(),'侵占公司财物');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (3,'利益输送','3','jc_violation_type','','danger','N','0','admin',NOW(),'为关联方输送利益');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (4,'采购舞弊','4','jc_violation_type','','warning','N','0','admin',NOW(),'围标串标、虚假采购等');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (5,'财务违规','5','jc_violation_type','','warning','N','0','admin',NOW(),'虚报冒领、费用异常');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (6,'滥用职权','6','jc_violation_type','','warning','N','0','admin',NOW(),'越权审批、违规决策');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (7,'失职渎职','7','jc_violation_type','','info','N','0','admin',NOW(),'造成公司损失');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (8,'违反公司制度','8','jc_violation_type','','info','N','0','admin',NOW(),'违反内部管理制度');

-- 线索来源（企业口径，value 为英文码；时限参数 key = jiancha.clue.deadline.<value>）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (1,'员工举报','report','jc_clue_source','Y','0','admin',NOW(),'员工实名反映');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (2,'匿名举报','anonymous','jc_clue_source','N','0','admin',NOW(),'匿名渠道反映');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (3,'内部审计','audit','jc_clue_source','N','0','admin',NOW(),'内部审计发现问题');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (4,'专项检查','special','jc_clue_source','N','0','admin',NOW(),'专项检查发现问题');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (5,'合规检查','compliance','jc_clue_source','N','0','admin',NOW(),'合规检查发现问题');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (6,'上级单位交办','superior','jc_clue_source','N','0','admin',NOW(),'上级单位/集团交办');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (7,'AI预警','ai','jc_clue_source','N','0','admin',NOW(),'风险预警引擎自动生成');

-- 关联性质（企业版新增）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (1,'持股','持股','jc_relation_kind','Y','0','admin',NOW(),'持有企业股权/出资');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (2,'任职','任职','jc_relation_kind','N','0','admin',NOW(),'在企业担任职务');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (3,'兼职取酬','兼职取酬','jc_relation_kind','N','0','admin',NOW(),'兼职并领取报酬');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (4,'实际控制','实际控制','jc_relation_kind','N','0','admin',NOW(),'实际控制经营主体');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (5,'劳务报酬','劳务报酬','jc_relation_kind','N','0','admin',NOW(),'提供劳务取得报酬');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (6,'其他','其他','jc_relation_kind','N','0','admin',NOW(),'其他关联性质');

-- 合作方类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (1,'供应商','供应商','jc_partner_type','Y','0','admin',NOW(),'向本公司供货');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (2,'客户','客户','jc_partner_type','N','0','admin',NOW(),'采购本公司产品/服务');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (3,'承包商','承包商','jc_partner_type','N','0','admin',NOW(),'承接工程/项目');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (4,'服务商','服务商','jc_partner_type','N','0','admin',NOW(),'提供专业服务');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (5,'其他','其他','jc_partner_type','N','0','admin',NOW(),'其他合作方');

-- 核实状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (1,'未核实','未核实','jc_verify_status','Y','0','admin',NOW(),'尚未核实');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (2,'已核实','已核实','jc_verify_status','N','0','admin',NOW(),'已完成核实');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (3,'存疑','存疑','jc_verify_status','N','0','admin',NOW(),'核实中发现疑点');

-- 数据来源
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (1,'申报','申报','jc_data_source','N','0','admin',NOW(),'本人利益冲突申报');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (2,'排查','排查','jc_data_source','N','0','admin',NOW(),'公司主动排查');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (3,'举报','举报','jc_data_source','N','0','admin',NOW(),'举报线索反映');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (4,'人工录入','人工录入','jc_data_source','Y','0','admin',NOW(),'管理人员录入');

-- 申报类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (1,'年度','年度','jc_declare_type','Y','0','admin',NOW(),'年度集中申报');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (2,'事项','事项','jc_declare_type','N','0','admin',NOW(),'发生事项即时申报');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (3,'专项','专项','jc_declare_type','N','0','admin',NOW(),'专项治理申报');

-- 申报状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (1,'草稿','草稿','jc_declare_status','N','0','admin',NOW(),'尚未提交');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (2,'待核实','待核实','jc_declare_status','Y','0','admin',NOW(),'已提交待核实');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (3,'已核实','已核实','jc_declare_status','N','0','admin',NOW(),'核实完成');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (4,'存疑','存疑','jc_declare_status','N','0','admin',NOW(),'核实存疑待处理');

-- 申报事项类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (1,'经商办企业','经商办企业','jc_conflict_item_type','Y','0','admin',NOW(),'本人或亲属经商办企业');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (2,'兼职取酬','兼职取酬','jc_conflict_item_type','N','0','admin',NOW(),'外部兼职并取酬');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (3,'亲属从业','亲属从业','jc_conflict_item_type','N','0','admin',NOW(),'亲属在合作方从业');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (4,'对外投资','对外投资','jc_conflict_item_type','N','0','admin',NOW(),'对外持股/理财投资');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (5,'关联交易','关联交易','jc_conflict_item_type','N','0','admin',NOW(),'与本公司发生关联交易');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (6,'劳务报酬','劳务报酬','jc_conflict_item_type','N','0','admin',NOW(),'取得外部劳务报酬');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time, remark)
VALUES (7,'其他','其他','jc_conflict_item_type','N','0','admin',NOW(),'其他申报事项');
-- （原 AI预警 条目已合并到上方「线索来源」段，此处移除）

-- 线索办理状态（企业口径，value 数字码 0-5，与后端状态机一致）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time)
VALUES (1,'待受理','0','jc_clue_status','info','Y','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time)
VALUES (2,'已受理','1','jc_clue_status','primary','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time)
VALUES (3,'核查中','2','jc_clue_status','warning','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time)
VALUES (4,'处置中','3','jc_clue_status','warning','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time)
VALUES (5,'已办结','4','jc_clue_status','success','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time)
VALUES (6,'不予受理','5','jc_clue_status','danger','N','0','admin',NOW());

-- 预警级别
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (1,'黄灯','黄灯','jc_warn_level','warning','warning','N','0','admin',NOW(),'超期1/2时限');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES (2,'红灯','红灯','jc_warn_level','danger','danger','N','0','admin',NOW(),'已超期');

-- 在职状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
VALUES (1,'在岗','在岗','jc_person_status','Y','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
VALUES (2,'退休','退休','jc_person_status','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
VALUES (3,'离职','离职','jc_person_status','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
VALUES (4,'调出','调出','jc_person_status','N','0','admin',NOW());

-- 风险等级
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time)
VALUES (1,'高','高','jc_risk_level','danger','danger','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time)
VALUES (2,'中','中','jc_risk_level','warning','warning','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time)
VALUES (3,'低','低','jc_risk_level','info','info','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time)
VALUES (4,'正常','正常','jc_risk_level','primary','success','Y','0','admin',NOW());

-- 亲属关系
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
VALUES (1,'配偶','配偶','jc_relation_type','Y','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
VALUES (2,'子女','子女','jc_relation_type','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
VALUES (3,'父母','父母','jc_relation_type','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
VALUES (4,'兄弟姐妹','兄弟姐妹','jc_relation_type','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
VALUES (5,'其他','其他','jc_relation_type','N','0','admin',NOW());

-- 处分种类
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
VALUES (1,'警告','警告','jc_punish_type','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
VALUES (2,'严重警告','严重警告','jc_punish_type','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
VALUES (3,'撤销党内职务','撤销党内职务','jc_punish_type','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
VALUES (4,'留党察看','留党察看','jc_punish_type','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
VALUES (5,'开除党籍','开除党籍','jc_punish_type','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
VALUES (6,'降级','降级','jc_punish_type','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
VALUES (7,'撤职','撤职','jc_punish_type','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
VALUES (8,'开除','开除','jc_punish_type','N','0','admin',NOW());

-- 画像标签类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, is_default, status, create_by, create_time)
VALUES (1,'风险','风险','jc_tag_type','danger','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, is_default, status, create_by, create_time)
VALUES (2,'关注','关注','jc_tag_type','warning','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, is_default, status, create_by, create_time)
VALUES (3,'关联','关联','jc_tag_type','primary','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, is_default, status, create_by, create_time)
VALUES (4,'历史','历史','jc_tag_type','info','N','0','admin',NOW());

-- 知识库分类类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
VALUES (1,'党纪法规','党纪','jc_kb_cat_type','Y','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
VALUES (2,'国家法律','国法','jc_kb_cat_type','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
VALUES (3,'企业规章','规章','jc_kb_cat_type','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
VALUES (4,'内部制度','制度','jc_kb_cat_type','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
VALUES (5,'典型案例','案例','jc_kb_cat_type','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
VALUES (6,'警示教育','警示','jc_kb_cat_type','N','0','admin',NOW());

-- 知识库文档状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time)
VALUES (1,'待处理','待处理','jc_kb_doc_status','info','Y','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time)
VALUES (2,'解析中','解析中','jc_kb_doc_status','warning','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time)
VALUES (3,'已入库','已入库','jc_kb_doc_status','success','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time)
VALUES (4,'解析失败','解析失败','jc_kb_doc_status','danger','N','0','admin',NOW());

-- 业务文档类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
VALUES (1,'审查调查报告','调查报告','jc_doc_biz_type','Y','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
VALUES (2,'谈话函询','谈话函询','jc_doc_biz_type','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
VALUES (3,'合同协议','合同','jc_doc_biz_type','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
VALUES (4,'其他','其他','jc_doc_biz_type','N','0','admin',NOW());

-- 举报来源渠道
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
VALUES (1,'网站举报','网站','jc_petition_channel','Y','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
VALUES (2,'电话举报','电话','jc_petition_channel','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
VALUES (3,'信箱举报','信箱','jc_petition_channel','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time)
VALUES (4,'上级转办','转办','jc_petition_channel','N','0','admin',NOW());

-- 举报办理状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time)
VALUES (1,'待受理','待受理','jc_petition_status','info','Y','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time)
VALUES (2,'已受理','已受理','jc_petition_status','primary','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time)
VALUES (3,'研判中','研判中','jc_petition_status','warning','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time)
VALUES (4,'已分办','已分办','jc_petition_status','warning','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time)
VALUES (5,'办理中','办理中','jc_petition_status','warning','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time)
VALUES (6,'已办结','已办结','jc_petition_status','success','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time)
VALUES (7,'已归档','已归档','jc_petition_status','info','N','0','admin',NOW());

-- 预警任务状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time)
VALUES (1,'待处理','待处理','jc_warn_status','danger','Y','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time)
VALUES (2,'处理中','处理中','jc_warn_status','warning','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time)
VALUES (3,'已核实','已核实','jc_warn_status','info','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time)
VALUES (4,'已整改','已整改','jc_warn_status','success','N','0','admin',NOW());
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, list_class, is_default, status, create_by, create_time)
VALUES (5,'已归档','已归档','jc_warn_status','info','N','0','admin',NOW());

-- 12类风险预警类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time) VALUES
(1,'兼职取酬','兼职取酬','jc_warn_type','N','0','admin',NOW()),
(2,'裸官','裸官','jc_warn_type','N','0','admin',NOW()),
(3,'子女经商办企业','子女经商','jc_warn_type','N','0','admin',NOW()),
(4,'频繁调动','频繁调动','jc_warn_type','N','0','admin',NOW()),
(5,'突击提拔','突击提拔','jc_warn_type','N','0','admin',NOW()),
(6,'频繁请假','频繁请假','jc_warn_type','N','0','admin',NOW()),
(7,'财产申报异常','申报异常','jc_warn_type','N','0','admin',NOW()),
(8,'涉标关联','涉标关联','jc_warn_type','N','0','admin',NOW()),
(9,'审批跳跃','审批跳跃','jc_warn_type','N','0','admin',NOW()),
(10,'离职后异常','离职异常','jc_warn_type','N','0','admin',NOW()),
(11,'处分执行异常','处分异常','jc_warn_type','N','0','admin',NOW()),
(12,'信访集中反映','信访集中','jc_warn_type','N','0','admin',NOW());

-- 线索办理动作（企业口径，value 为英文码）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time) VALUES
(1,'登记','register','jc_clue_action','Y','0','admin',NOW()),
(2,'受理','accept','jc_clue_action','N','0','admin',NOW()),
(3,'研判','review','jc_clue_action','N','0','admin',NOW()),
(4,'分办','assign','jc_clue_action','N','0','admin',NOW()),
(5,'处置','dispose','jc_clue_action','N','0','admin',NOW()),
(6,'办结','close','jc_clue_action','N','0','admin',NOW()),
(7,'不予受理','reject','jc_clue_action','N','0','admin',NOW()),
(8,'退回','return','jc_clue_action','N','0','admin',NOW());

-- 线索处置方式（企业口径）
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time) VALUES
(1,'澄清了结','1','jc_clue_disposition','Y','0','admin',NOW()),
(2,'谈话提醒','2','jc_clue_disposition','N','0','admin',NOW()),
(3,'批评教育','3','jc_clue_disposition','N','0','admin',NOW()),
(4,'责令整改','4','jc_clue_disposition','N','0','admin',NOW()),
(5,'内部处分','5','jc_clue_disposition','N','0','admin',NOW()),
(6,'经济处理','6','jc_clue_disposition','N','0','admin',NOW()),
(7,'移交司法','7','jc_clue_disposition','N','0','admin',NOW()),
(8,'其他','8','jc_clue_disposition','N','0','admin',NOW());

-- 纪检文书类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, is_default, status, create_by, create_time) VALUES
(1,'审查调查报告','调查报告','jc_writ_type','Y','0','admin',NOW()),
(2,'立案呈批表','立案呈批','jc_writ_type','N','0','admin',NOW()),
(3,'谈话笔录','谈话笔录','jc_writ_type','N','0','admin',NOW()),
(4,'审理报告','审理报告','jc_writ_type','N','0','admin',NOW()),
(5,'处分决定书','处分决定','jc_writ_type','N','0','admin',NOW());

-- 系统参数（线索时限规则，放配置不硬编码；key = jiancha.clue.deadline.<来源value>）
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark) VALUES
('线索办理时限-默认','jiancha.clue.deadline.default','30','N','admin',NOW(),'来源未单独配置时的默认办理时限(日)'),
('线索办理时限-员工举报','jiancha.clue.deadline.report','30','N','admin',NOW(),'员工举报办理时限(日)'),
('线索办理时限-匿名举报','jiancha.clue.deadline.anonymous','30','N','admin',NOW(),'匿名举报办理时限(日)'),
('线索办理时限-内部审计','jiancha.clue.deadline.audit','20','N','admin',NOW(),'内部审计移交线索办理时限(日)'),
('线索办理时限-专项检查','jiancha.clue.deadline.special','15','N','admin',NOW(),'专项检查发现线索办理时限(日)'),
('线索办理时限-合规检查','jiancha.clue.deadline.compliance','15','N','admin',NOW(),'合规检查发现线索办理时限(日)'),
('线索办理时限-上级交办','jiancha.clue.deadline.superior','30','N','admin',NOW(),'上级单位交办线索办理时限(日)'),
('线索办理时限-AI预警','jiancha.clue.deadline.ai','10','N','admin',NOW(),'AI预警生成线索办理时限(日)'),
('线索黄灯阈值比例','jiancha.clue.warn.ratio','0.5','N','admin',NOW(),'已用时长达到时限的比例即亮黄灯'),
('线索重复判定窗口(日)','jiancha.clue.duplicate.days','90','N','admin',NOW(),'同一被反映人在该天数内的多条线索提示疑似重复');
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark) VALUES
('巡察移交办理时限(天)','jc.clue.deadline.inspection','15','Y','admin',NOW(),'问题线索时限规则');
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark) VALUES
('审计发现办理时限(天)','jc.clue.deadline.audit','20','Y','admin',NOW(),'问题线索时限规则');
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark) VALUES
('AI预警办理时限(天)','jc.clue.deadline.ai','10','Y','admin',NOW(),'问题线索时限规则');
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark) VALUES
('黄灯预警阈值比例','jc.warn.yellow.ratio','0.5','Y','admin',NOW(),'超期1/2时限触发黄灯');
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark) VALUES
('AI模型温度','jiancha.ai.temperature','0.2','Y','admin',NOW(),'纪检场景要求严谨输出');
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark) VALUES
('AI强制带出处','jiancha.ai.enable.citation','true','Y','admin',NOW(),'回答必须带文件名+条款号');
