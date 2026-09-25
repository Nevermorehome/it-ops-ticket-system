-- =====================================================================
-- 信息部内部 IT 运维工单记录系统 - MySQL 8 初始化脚本
-- 字符集: utf8mb4 / 排序: utf8mb4_general_ci
-- 说明: 由 docker-compose 挂载到 /docker-entrypoint-initdb.d 自动执行
--       本地开发亦可: mysql -uroot -p < init.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS `itops` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `itops`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================================
-- 一、系统管理域 (sys_*)
-- =====================================================================

-- 部门表
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
  `dept_id`     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `parent_id`   BIGINT       NOT NULL DEFAULT 0  COMMENT '父部门ID(0为根)',
  `ancestors`   VARCHAR(255) DEFAULT ''         COMMENT '祖级列表(逗号分隔)',
  `dept_name`   VARCHAR(64)  NOT NULL            COMMENT '部门名称',
  `order_num`   INT          NOT NULL DEFAULT 0  COMMENT '显示顺序',
  `leader`      VARCHAR(32)  DEFAULT NULL        COMMENT '负责人',
  `phone`       VARCHAR(20)  DEFAULT NULL        COMMENT '联系电话',
  `status`      CHAR(1)      NOT NULL DEFAULT '0' COMMENT '状态(0正常 1停用)',
  `create_by`   VARCHAR(64)  DEFAULT ''          COMMENT '创建者',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`   VARCHAR(64)  DEFAULT ''          COMMENT '更新者',
  `update_time` DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT(1)   NOT NULL DEFAULT 0  COMMENT '删除标志(0存在 1删除)',
  PRIMARY KEY (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `user_id`     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `dept_id`     BIGINT       DEFAULT NULL        COMMENT '部门ID',
  `username`    VARCHAR(64)  NOT NULL            COMMENT '登录账号',
  `password`    VARCHAR(128) NOT NULL            COMMENT '密码(BCrypt)',
  `real_name`   VARCHAR(32)  NOT NULL            COMMENT '真实姓名',
  `nick_name`   VARCHAR(32)  DEFAULT NULL        COMMENT '昵称',
  `phone`       VARCHAR(20)  DEFAULT NULL        COMMENT '手机号',
  `email`       VARCHAR(64)  DEFAULT NULL        COMMENT '邮箱',
  `sex`         CHAR(1)      DEFAULT '0'         COMMENT '性别(0男 1女 2未知)',
  `avatar`      VARCHAR(255) DEFAULT NULL        COMMENT '头像URL',
  `status`      CHAR(1)      NOT NULL DEFAULT '0' COMMENT '状态(0正常 1停用)',
  `login_ip`    VARCHAR(64)  DEFAULT NULL        COMMENT '最后登录IP',
  `login_date`  DATETIME     DEFAULT NULL        COMMENT '最后登录时间',
  `remark`      VARCHAR(255) DEFAULT NULL        COMMENT '备注',
  `create_by`   VARCHAR(64)  DEFAULT ''          COMMENT '创建者',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`   VARCHAR(64)  DEFAULT ''          COMMENT '更新者',
  `update_time` DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT(1)   NOT NULL DEFAULT 0  COMMENT '删除标志',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `role_id`     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name`   VARCHAR(32)  NOT NULL            COMMENT '角色名称',
  `role_key`    VARCHAR(64)  NOT NULL            COMMENT '角色权限字符串',
  `data_scope`  CHAR(1)      NOT NULL DEFAULT '1' COMMENT '数据范围(1全部 2自定义 3本部门 4本部门及以下 5仅本人)',
  `order_num`   INT          NOT NULL DEFAULT 0  COMMENT '显示顺序',
  `status`      CHAR(1)      NOT NULL DEFAULT '0' COMMENT '状态(0正常 1停用)',
  `remark`      VARCHAR(255) DEFAULT NULL,
  `create_by`   VARCHAR(64)  DEFAULT '',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`   VARCHAR(64)  DEFAULT '',
  `update_time` DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `uk_role_key` (`role_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 菜单/权限表
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `menu_id`     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id`   BIGINT       NOT NULL DEFAULT 0  COMMENT '父菜单ID',
  `menu_name`   VARCHAR(32)  NOT NULL            COMMENT '菜单名称',
  `menu_type`   CHAR(1)      NOT NULL            COMMENT '类型(M目录 C菜单 F按钮)',
  `path`        VARCHAR(200) DEFAULT ''          COMMENT '路由路径',
  `component`   VARCHAR(255) DEFAULT NULL        COMMENT '前端组件路径',
  `perms`       VARCHAR(100) DEFAULT NULL        COMMENT '权限标识',
  `icon`        VARCHAR(64)  DEFAULT ''          COMMENT '图标',
  `order_num`   INT          NOT NULL DEFAULT 0  COMMENT '显示顺序',
  `visible`     CHAR(1)      NOT NULL DEFAULT '0' COMMENT '是否可见(0显示 1隐藏)',
  `status`      CHAR(1)      NOT NULL DEFAULT '0' COMMENT '状态(0正常 1停用)',
  `create_by`   VARCHAR(64)  DEFAULT '',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`   VARCHAR(64)  DEFAULT '',
  `update_time` DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- 用户-角色
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色-菜单
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`,`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 角色-部门(数据范围=自定义时使用)
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept` (
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `dept_id` BIGINT NOT NULL COMMENT '部门ID',
  PRIMARY KEY (`role_id`,`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色部门关联表';

-- 字典类型
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type` (
  `dict_id`     BIGINT       NOT NULL AUTO_INCREMENT,
  `dict_name`   VARCHAR(64)  NOT NULL COMMENT '字典名称',
  `dict_type`   VARCHAR(64)  NOT NULL COMMENT '字典类型(英文)',
  `status`      CHAR(1)      NOT NULL DEFAULT '0',
  `remark`      VARCHAR(255) DEFAULT NULL,
  `create_by`   VARCHAR(64)  DEFAULT '',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`   VARCHAR(64)  DEFAULT '',
  `update_time` DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`dict_id`),
  UNIQUE KEY `uk_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

-- 字典数据
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
  `dict_code`   BIGINT       NOT NULL AUTO_INCREMENT,
  `dict_type`   VARCHAR(64)  NOT NULL COMMENT '字典类型',
  `dict_label`  VARCHAR(64)  NOT NULL COMMENT '字典标签',
  `dict_value`  VARCHAR(64)  NOT NULL COMMENT '字典键值',
  `list_class`  VARCHAR(32)  DEFAULT '' COMMENT '标签样式(default/primary/success/info/warning/danger)',
  `order_num`   INT          NOT NULL DEFAULT 0,
  `status`      CHAR(1)      NOT NULL DEFAULT '0',
  `remark`      VARCHAR(255) DEFAULT NULL,
  `create_by`   VARCHAR(64)  DEFAULT '',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`   VARCHAR(64)  DEFAULT '',
  `update_time` DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`dict_code`),
  KEY `idx_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

-- 系统参数配置
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `config_id`    BIGINT       NOT NULL AUTO_INCREMENT,
  `config_name`  VARCHAR(64)  DEFAULT NULL COMMENT '参数名称',
  `config_key`   VARCHAR(128) NOT NULL COMMENT '参数键',
  `config_value` VARCHAR(512) DEFAULT NULL COMMENT '参数值',
  `config_type`  CHAR(1)      DEFAULT 'Y' COMMENT '系统内置(Y是 N否)',
  `remark`       VARCHAR(255) DEFAULT NULL,
  `create_by`    VARCHAR(64)  DEFAULT '',
  `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`    VARCHAR(64)  DEFAULT '',
  `update_time`  DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `deleted`      TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统参数配置表';

-- 登录日志
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log` (
  `info_id`      BIGINT       NOT NULL AUTO_INCREMENT,
  `username`     VARCHAR(64)  NOT NULL COMMENT '登录账号',
  `ipaddr`       VARCHAR(64)  DEFAULT '' COMMENT 'IP地址',
  `login_location` VARCHAR(255) DEFAULT '' COMMENT '登录地点',
  `browser`      VARCHAR(64)  DEFAULT '' COMMENT '浏览器',
  `os`           VARCHAR(64)  DEFAULT '' COMMENT '操作系统',
  `status`       CHAR(1)      DEFAULT '0' COMMENT '状态(0成功 1失败)',
  `msg`          VARCHAR(255) DEFAULT '' COMMENT '提示消息',
  `login_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  PRIMARY KEY (`info_id`),
  KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- 操作日志
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log` (
  `oper_id`        BIGINT       NOT NULL AUTO_INCREMENT,
  `title`          VARCHAR(64)  DEFAULT '' COMMENT '模块名称',
  `business_type`  INT          DEFAULT 0 COMMENT '业务类型(0其它 1新增 2修改 3删除 4导出 5导入)',
  `method`         VARCHAR(255) DEFAULT '' COMMENT '方法名称',
  `request_method` VARCHAR(16)  DEFAULT '' COMMENT '请求方式',
  `oper_name`      VARCHAR(64)  DEFAULT '' COMMENT '操作人员',
  `dept_name`      VARCHAR(64)  DEFAULT '' COMMENT '部门名称',
  `oper_url`       VARCHAR(255) DEFAULT '' COMMENT '请求URL',
  `oper_ip`        VARCHAR(64)  DEFAULT '' COMMENT '主机地址',
  `oper_param`     TEXT         COMMENT '请求参数',
  `json_result`    TEXT         COMMENT '返回结果',
  `status`         CHAR(1)      DEFAULT '0' COMMENT '状态(0正常 1异常)',
  `error_msg`      TEXT         COMMENT '错误消息',
  `cost_time`      BIGINT       DEFAULT 0 COMMENT '消耗时长(毫秒)',
  `oper_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`oper_id`),
  KEY `idx_oper_time` (`oper_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- =====================================================================
-- 二、业务域 (biz_*)
-- =====================================================================

-- 工单分类(树形)
DROP TABLE IF EXISTS `biz_category`;
CREATE TABLE `biz_category` (
  `category_id`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id`     BIGINT       NOT NULL DEFAULT 0 COMMENT '父分类ID',
  `ancestors`     VARCHAR(255) DEFAULT '' COMMENT '祖级列表',
  `category_name` VARCHAR(64)  NOT NULL COMMENT '分类名称',
  `order_num`     INT          NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `status`        CHAR(1)      NOT NULL DEFAULT '0' COMMENT '状态(0正常 1停用)',
  `create_by`     VARCHAR(64)  DEFAULT '',
  `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`     VARCHAR(64)  DEFAULT '',
  `update_time`   DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单分类表';

-- 常用地点
DROP TABLE IF EXISTS `biz_location`;
CREATE TABLE `biz_location` (
  `location_id`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '地点ID',
  `location_name` VARCHAR(128) NOT NULL COMMENT '地点名称(如:1号楼3层信息部)',
  `building`      VARCHAR(64)  DEFAULT '' COMMENT '楼栋',
  `floor`         VARCHAR(32)  DEFAULT '' COMMENT '楼层',
  `room`          VARCHAR(64)  DEFAULT '' COMMENT '房间',
  `address`       VARCHAR(255) DEFAULT '' COMMENT '详细地址',
  `longitude`     DECIMAL(10,6) DEFAULT NULL COMMENT '经度(gcj02)',
  `latitude`      DECIMAL(10,6) DEFAULT NULL COMMENT '纬度(gcj02)',
  `status`        CHAR(1)      NOT NULL DEFAULT '0' COMMENT '状态(0正常 1停用)',
  `remark`        VARCHAR(255) DEFAULT NULL,
  `create_by`     VARCHAR(64)  DEFAULT '',
  `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`     VARCHAR(64)  DEFAULT '',
  `update_time`   DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='常用地点表';

-- 资产设备
DROP TABLE IF EXISTS `biz_asset`;
CREATE TABLE `biz_asset` (
  `asset_id`      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '资产ID',
  `asset_no`      VARCHAR(64)  NOT NULL COMMENT '资产编号',
  `asset_name`    VARCHAR(128) NOT NULL COMMENT '资产名称',
  `category`      VARCHAR(64)  DEFAULT '' COMMENT '资产类别(电脑/打印机/服务器/网络设备等)',
  `brand`         VARCHAR(64)  DEFAULT '' COMMENT '品牌',
  `model`         VARCHAR(128) DEFAULT '' COMMENT '规格型号',
  `user_id`       BIGINT       DEFAULT NULL COMMENT '使用人ID',
  `user_name`     VARCHAR(32)  DEFAULT '' COMMENT '使用人姓名',
  `dept_id`       BIGINT       DEFAULT NULL COMMENT '使用部门ID',
  `location_text` VARCHAR(255) DEFAULT '' COMMENT '存放位置',
  `status`        CHAR(1)      NOT NULL DEFAULT '0' COMMENT '状态(0在用 1闲置 2维修 3报废)',
  `buy_date`      DATE         DEFAULT NULL COMMENT '购置日期',
  `remark`        VARCHAR(255) DEFAULT NULL,
  `create_by`     VARCHAR(64)  DEFAULT '',
  `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`     VARCHAR(64)  DEFAULT '',
  `update_time`   DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`asset_id`),
  UNIQUE KEY `uk_asset_no` (`asset_no`),
  KEY `idx_asset_dept` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产设备表';

-- 工单主表
DROP TABLE IF EXISTS `biz_ticket`;
CREATE TABLE `biz_ticket` (
  `ticket_id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '工单ID',
  `ticket_no`           VARCHAR(32)  NOT NULL COMMENT '工单编号(WO+日期+序列)',
  `title`               VARCHAR(200) NOT NULL COMMENT '标题',
  `category_id`         BIGINT       DEFAULT NULL COMMENT '分类ID',
  `description`         TEXT         COMMENT '问题描述',
  `reporter_id`         BIGINT       DEFAULT NULL COMMENT '报修人ID(匿名代报可为空)',
  `reporter_name`       VARCHAR(32)  NOT NULL COMMENT '报修人姓名',
  `dept_id`             BIGINT       DEFAULT NULL COMMENT '报修部门ID',
  `phone`               VARCHAR(20)  DEFAULT '' COMMENT '联系电话',
  `source`              VARCHAR(32)  NOT NULL DEFAULT 'self' COMMENT '来源(dict ticket_source)',
  `priority`            TINYINT      NOT NULL DEFAULT 1 COMMENT '优先级(0低 1中 2高 3紧急)',
  `status`              VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT '状态(PENDING/ASSIGNED/PROCESSING/SUSPENDED/RESOLVED/CLOSED/CANCELLED/MERGED)',
  `location_id`         BIGINT       DEFAULT NULL COMMENT '常用地点ID',
  `location_text`       VARCHAR(255) DEFAULT '' COMMENT '地点描述',
  `longitude`           DECIMAL(10,6) DEFAULT NULL COMMENT '经度',
  `latitude`            DECIMAL(10,6) DEFAULT NULL COMMENT '纬度',
  `asset_id`            BIGINT       DEFAULT NULL COMMENT '关联资产ID',
  `handler_id`          BIGINT       DEFAULT NULL COMMENT '主处理人ID',
  `handler_name`        VARCHAR(32)  DEFAULT '' COMMENT '主处理人姓名',
  `suspend_reason`      VARCHAR(255) DEFAULT '' COMMENT '挂起原因',
  `handle_summary`      TEXT         COMMENT '最新处理情况摘要',
  `solution`            TEXT         COMMENT '解决方案',
  `satisfaction`        TINYINT      DEFAULT NULL COMMENT '满意度(1-5)',
  `satisfaction_comment` VARCHAR(255) DEFAULT '' COMMENT '评价内容',
  `merged_to`           BIGINT       DEFAULT NULL COMMENT '被合并到的目标工单ID',
  `assign_time`         DATETIME     DEFAULT NULL COMMENT '指派时间',
  `accept_time`         DATETIME     DEFAULT NULL COMMENT '受理时间',
  `suspend_time`        DATETIME     DEFAULT NULL COMMENT '挂起时间',
  `resolve_time`        DATETIME     DEFAULT NULL COMMENT '解决时间',
  `close_time`          DATETIME     DEFAULT NULL COMMENT '关闭时间',
  `finish_time`         DATETIME     DEFAULT NULL COMMENT '实际完成时间(解决时间)',
  `duration_seconds`    BIGINT       DEFAULT 0 COMMENT '受理到解决耗时(秒)',
  `create_by`           VARCHAR(64)  DEFAULT '',
  `create_time`         DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`           VARCHAR(64)  DEFAULT '',
  `update_time`         DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `deleted`             TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`ticket_id`),
  UNIQUE KEY `uk_ticket_no` (`ticket_no`),
  KEY `idx_ticket_status`   (`status`),
  KEY `idx_ticket_priority` (`priority`),
  KEY `idx_ticket_handler`  (`handler_id`),
  KEY `idx_ticket_reporter` (`reporter_id`),
  KEY `idx_ticket_dept`     (`dept_id`),
  KEY `idx_ticket_category` (`category_id`),
  KEY `idx_ticket_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单主表';

-- 工单协同人
DROP TABLE IF EXISTS `biz_ticket_collaborator`;
CREATE TABLE `biz_ticket_collaborator` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `ticket_id`   BIGINT      NOT NULL COMMENT '工单ID',
  `user_id`     BIGINT      NOT NULL COMMENT '协同人ID',
  `user_name`   VARCHAR(32) NOT NULL COMMENT '协同人姓名',
  `create_by`   VARCHAR(64) DEFAULT '',
  `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ticket_user` (`ticket_id`,`user_id`),
  KEY `idx_collab_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单协同人表';

-- 工单时间线(操作/处理记录)
DROP TABLE IF EXISTS `biz_ticket_timeline`;
CREATE TABLE `biz_ticket_timeline` (
  `timeline_id`   BIGINT       NOT NULL AUTO_INCREMENT,
  `ticket_id`     BIGINT       NOT NULL COMMENT '工单ID',
  `action`        VARCHAR(32)  NOT NULL COMMENT '动作枚举(CREATE/EDIT/ASSIGN/TRANSFER/COLLABORATE/ACCEPT/HANDLE/SUSPEND/RESUME/RESOLVE/CLOSE/REOPEN/CANCEL/MERGE/MERGED)',
  `action_name`   VARCHAR(32)  NOT NULL COMMENT '动作中文名',
  `content`       TEXT         COMMENT '处理情况/备注内容',
  `from_status`   VARCHAR(16)  DEFAULT NULL COMMENT '操作前状态',
  `to_status`     VARCHAR(16)  DEFAULT NULL COMMENT '操作后状态',
  `target_user_id`   BIGINT    DEFAULT NULL COMMENT '动作目标人(指派/转派/协同对象)',
  `target_user_name` VARCHAR(32) DEFAULT '' COMMENT '目标人姓名',
  `operate_id`    BIGINT       DEFAULT NULL COMMENT '操作人ID',
  `operate_name`  VARCHAR(32)  DEFAULT '' COMMENT '操作人姓名',
  `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`timeline_id`),
  KEY `idx_tl_ticket` (`ticket_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单时间线表';

-- 工单附件(建单附件/处理附件/现场记录图片统一存放)
DROP TABLE IF EXISTS `biz_ticket_attachment`;
CREATE TABLE `biz_ticket_attachment` (
  `attachment_id` BIGINT       NOT NULL AUTO_INCREMENT,
  `ticket_id`     BIGINT       NOT NULL COMMENT '工单ID',
  `timeline_id`   BIGINT       DEFAULT NULL COMMENT '关联时间线ID(处理记录图片)',
  `field_record_id` BIGINT     DEFAULT NULL COMMENT '关联现场记录ID',
  `file_name`     VARCHAR(255) NOT NULL COMMENT '文件名',
  `file_url`      VARCHAR(500) NOT NULL COMMENT '访问URL',
  `file_size`     BIGINT       DEFAULT 0 COMMENT '文件大小(字节)',
  `file_type`     VARCHAR(32)  DEFAULT '' COMMENT '文件扩展名',
  `create_by`    VARCHAR(64)  DEFAULT '',
  `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`attachment_id`),
  KEY `idx_att_ticket` (`ticket_id`),
  KEY `idx_att_record` (`field_record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单附件表';

-- 现场记录
DROP TABLE IF EXISTS `biz_field_record`;
CREATE TABLE `biz_field_record` (
  `record_id`    BIGINT       NOT NULL AUTO_INCREMENT,
  `ticket_id`    BIGINT       NOT NULL COMMENT '工单ID',
  `longitude`    DECIMAL(10,6) DEFAULT NULL COMMENT '经度(gcj02)',
  `latitude`     DECIMAL(10,6) DEFAULT NULL COMMENT '纬度(gcj02)',
  `address`      VARCHAR(255) DEFAULT '' COMMENT '现场地址(地图选点/手工)',
  `content`      VARCHAR(1000) DEFAULT '' COMMENT '现场情况说明',
  `record_time`  DATETIME     NOT NULL COMMENT '记录时间(服务器接收时间)',
  `create_by_id`   BIGINT     DEFAULT NULL COMMENT '记录人ID',
  `create_by`    VARCHAR(64)  DEFAULT '' COMMENT '记录人姓名',
  `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`record_id`),
  KEY `idx_fr_ticket` (`ticket_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='现场记录表';

-- 站内消息
DROP TABLE IF EXISTS `biz_message`;
CREATE TABLE `biz_message` (
  `message_id`   BIGINT       NOT NULL AUTO_INCREMENT,
  `receiver_id`  BIGINT       NOT NULL COMMENT '接收人ID',
  `message_type` VARCHAR(32)  NOT NULL DEFAULT 'TICKET' COMMENT '消息类型(TICKET/SYSTEM)',
  `title`        VARCHAR(128) NOT NULL COMMENT '标题',
  `content`      VARCHAR(500) DEFAULT '' COMMENT '内容',
  `biz_type`     VARCHAR(32)  DEFAULT '' COMMENT '业务动作(ASSIGN/RESOLVE等)',
  `biz_id`       BIGINT       DEFAULT NULL COMMENT '业务ID(工单ID)',
  `is_read`      TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已读(0否 1是)',
  `read_time`    DATETIME     DEFAULT NULL,
  `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`message_id`),
  KEY `idx_msg_receiver` (`receiver_id`,`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息表';

-- Webhook 配置(企业微信/钉钉/飞书)
DROP TABLE IF EXISTS `biz_webhook_config`;
CREATE TABLE `biz_webhook_config` (
  `webhook_id`   BIGINT       NOT NULL AUTO_INCREMENT,
  `name`         VARCHAR(64)  NOT NULL COMMENT '配置名称',
  `type`         VARCHAR(16)  NOT NULL COMMENT '类型(wechat企微 dingtalk钉钉 feishu飞书)',
  `webhook_url`  VARCHAR(500) NOT NULL COMMENT '机器人地址',
  `secret`       VARCHAR(255) DEFAULT '' COMMENT '签名密钥(钉钉加签等,可选)',
  `events`       VARCHAR(255) DEFAULT '*' COMMENT '订阅事件(*全部或逗号分隔动作)',
  `status`       CHAR(1)      NOT NULL DEFAULT '0' COMMENT '状态(0启用 1停用)',
  `remark`       VARCHAR(255) DEFAULT NULL,
  `create_by`    VARCHAR(64)  DEFAULT '',
  `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_by`    VARCHAR(64)  DEFAULT '',
  `update_time`  DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `deleted`      TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`webhook_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Webhook通知配置表';

-- =====================================================================
-- 三、种子数据
-- =====================================================================

-- ---------- 部门 ----------
INSERT INTO `sys_dept`(`dept_id`,`parent_id`,`ancestors`,`dept_name`,`order_num`,`leader`,`phone`,`status`) VALUES
(1,0,'0','某某单位',0,'王局长','000-12345678','0'),
(2,1,'0,1','信息部',1,'吴主管','000-86543210','0'),
(3,1,'0,1','办公室',2,'李主任',NULL,'0'),
(4,1,'0,1','财务科',3,'赵科长',NULL,'0'),
(5,2,'0,1,2','运维组',1,'吴主管',NULL,'0'),
(6,2,'0,1,2','开发组',2,'孙组长',NULL,'0');

-- ---------- 用户(密码均为 admin123 的 BCrypt 值) ----------
-- $2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2 = admin123
INSERT INTO `sys_user`(`user_id`,`dept_id`,`username`,`password`,`real_name`,`nick_name`,`phone`,`status`,`remark`) VALUES
(1,2,'admin','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','系统管理员','admin','13800000001','0','超级管理员'),
(2,5,'wangwu','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','吴主管','吴主管','13800000002','0','调度主管'),
(3,5,'lisi','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','李工','工程师李四','13800000003','0','运维工程师'),
(4,5,'zhaoliu','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','赵工','工程师赵六','13800000004','0','运维工程师'),
(5,3,'zhangsan','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','张三','报修人张三','13800000005','0','办公室报修人'),
(6,4,'qianqi','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','钱七','报修人钱七','13800000006','0','财务科报修人');

-- ---------- 角色 ----------
INSERT INTO `sys_role`(`role_id`,`role_name`,`role_key`,`data_scope`,`order_num`,`status`,`remark`) VALUES
(1,'超级管理员','admin','1',1,'0','拥有全部权限'),
(2,'调度主管','supervisor','4',2,'0','本部门及以下数据,可派单/合并'),
(3,'运维工程师','engineer','5',3,'0','仅本人相关工单'),
(4,'报修用户','reporter','5',4,'0','仅本人提交的工单');

INSERT INTO `sys_user_role`(`user_id`,`role_id`) VALUES
(1,1),(2,2),(3,3),(4,3),(5,4),(6,4);

-- ---------- 菜单 ----------
-- 一级目录
INSERT INTO `sys_menu`(`menu_id`,`parent_id`,`menu_name`,`menu_type`,`path`,`component`,`perms`,`icon`,`order_num`) VALUES
(1, 0,'工作台',  'C','/dashboard',          'dashboard/index',                 NULL,                   'Odometer', 1),
(10,0,'工单中心','M','/ticket',             NULL,                              NULL,                   'Tickets',  2),
(20,0,'基础数据','M','/base',               NULL,                              NULL,                   'Files',    3),
(30,0,'统计报表','C','/report',             'report/index',                    'report:view',         'TrendCharts',4),
(40,0,'消息通知','M','/message',            NULL,                              NULL,                   'Bell',     5),
(50,0,'系统管理','M','/system',             NULL,                              NULL,                   'Setting',  6);

-- 工单中心
INSERT INTO `sys_menu`(`menu_id`,`parent_id`,`menu_name`,`menu_type`,`path`,`component`,`perms`,`icon`,`order_num`) VALUES
(11,10,'工单列表','C','list',     'ticket/index',     'ticket:list',    'Document', 1),
(12,10,'分类管理','C','category', 'ticket/category',  'ticket:category:list','Menu',   2);
-- 工单按钮
INSERT INTO `sys_menu`(`menu_id`,`parent_id`,`menu_name`,`menu_type`,`perms`,`order_num`) VALUES
(101,11,'工单创建','F','ticket:add',1),
(102,11,'工单编辑','F','ticket:edit',2),
(103,11,'工单删除','F','ticket:remove',3),
(104,11,'工单指派','F','ticket:assign',4),
(105,11,'工单转派','F','ticket:transfer',5),
(106,11,'设置协同','F','ticket:collaborate',6),
(107,11,'工单受理','F','ticket:accept',7),
(108,11,'提交处理','F','ticket:handle',8),
(109,11,'挂起恢复','F','ticket:suspend',9),
(110,11,'解决工单','F','ticket:resolve',10),
(111,11,'关闭工单','F','ticket:close',11),
(112,11,'重开工单','F','ticket:reopen',12),
(113,11,'取消工单','F','ticket:cancel',13),
(114,11,'合并工单','F','ticket:merge',14),
(115,11,'现场记录','F','ticket:field',15),
(116,11,'导出工单','F','ticket:export',16);

-- 基础数据
INSERT INTO `sys_menu`(`menu_id`,`parent_id`,`menu_name`,`menu_type`,`path`,`component`,`perms`,`icon`,`order_num`) VALUES
(21,20,'地点管理','C','location','base/location','base:location:list','Location',1),
(22,20,'资产管理','C','asset',   'base/asset',   'base:asset:list',  'Monitor', 2);
INSERT INTO `sys_menu`(`menu_id`,`parent_id`,`menu_name`,`menu_type`,`perms`,`order_num`) VALUES
(201,21,'地点新增','F','base:location:add',1),
(202,21,'地点修改','F','base:location:edit',2),
(203,21,'地点删除','F','base:location:remove',3),
(211,22,'资产新增','F','base:asset:add',1),
(212,22,'资产修改','F','base:asset:edit',2),
(213,22,'资产删除','F','base:asset:remove',3);

-- 报表按钮
INSERT INTO `sys_menu`(`menu_id`,`parent_id`,`menu_name`,`menu_type`,`perms`,`order_num`) VALUES
(31,30,'报表导出','F','report:export',1);

-- 消息通知
INSERT INTO `sys_menu`(`menu_id`,`parent_id`,`menu_name`,`menu_type`,`path`,`component`,`perms`,`icon`,`order_num`) VALUES
(41,40,'站内消息','C','list',    'message/index',   'message:list',           'ChatDotRound',1),
(42,40,'推送配置','C','webhook', 'message/webhook', 'message:webhook:list',    'Promotion',   2);

-- 系统管理
INSERT INTO `sys_menu`(`menu_id`,`parent_id`,`menu_name`,`menu_type`,`path`,`component`,`perms`,`icon`,`order_num`) VALUES
(51,50,'用户管理','C','user',      'system/user',     'system:user:list',     'User',      1),
(52,50,'角色管理','C','role',      'system/role',     'system:role:list',     'UserFilled',2),
(53,50,'菜单管理','C','menu',      'system/menu',     'system:menu:list',     'Menu',      3),
(54,50,'部门管理','C','dept',      'system/dept',     'system:dept:list',     'OfficeBuilding',4),
(55,50,'字典管理','C','dict',      'system/dict',     'system:dict:list',     'Collection',5),
(56,50,'参数配置','C','config',    'system/config',   'system:config:list',   'Tools',     6),
(57,50,'登录日志','C','login-log', 'system/loginLog', 'system:loginlog:list', 'Document',  7),
(58,50,'操作日志','C','oper-log',  'system/operLog',  'system:operlog:list',  'Tickets',   8);

INSERT INTO `sys_menu`(`menu_id`,`parent_id`,`menu_name`,`menu_type`,`perms`,`order_num`) VALUES
(501,51,'用户新增','F','system:user:add',1),
(502,51,'用户修改','F','system:user:edit',2),
(503,51,'用户删除','F','system:user:remove',3),
(504,51,'重置密码','F','system:user:reset',4),
(511,52,'角色新增','F','system:role:add',1),
(512,52,'角色修改','F','system:role:edit',2),
(513,52,'角色删除','F','system:role:remove',3),
(521,53,'菜单新增','F','system:menu:add',1),
(522,53,'菜单修改','F','system:menu:edit',2),
(523,53,'菜单删除','F','system:menu:remove',3),
(531,54,'部门新增','F','system:dept:add',1),
(532,54,'部门修改','F','system:dept:edit',2),
(533,54,'部门删除','F','system:dept:remove',3),
(541,55,'字典新增','F','system:dict:add',1),
(542,55,'字典修改','F','system:dict:edit',2),
(543,55,'字典删除','F','system:dict:remove',3),
(551,56,'参数新增','F','system:config:add',1),
(552,56,'参数修改','F','system:config:edit',2),
(553,56,'参数删除','F','system:config:remove',3);

-- 角色-菜单: admin/主管/工程师/报修人 授权(主管:工单全部+报表+消息+基础数据;工程师:工单查看处理+消息;报修人:工单创建查看+消息)
-- 超级管理员在后端走超管放行, 此处仍补齐菜单便于界面一致
INSERT INTO `sys_role_menu`(`role_id`,`menu_id`)
SELECT 1, m.menu_id FROM `sys_menu` m;

INSERT INTO `sys_role_menu`(`role_id`,`menu_id`) VALUES
(2,1),(2,10),(2,11),(2,12),(2,20),(2,21),(2,22),(2,30),(2,31),(2,40),(2,41),(2,42),
(2,101),(2,102),(2,103),(2,104),(2,105),(2,106),(2,107),(2,108),(2,109),(2,110),(2,111),(2,112),(2,113),(2,114),(2,115),(2,116),
(2,201),(2,202),(2,203),(2,211),(2,212),(2,213);

INSERT INTO `sys_role_menu`(`role_id`,`menu_id`) VALUES
(3,1),(3,10),(3,11),(3,21),(3,22),(3,40),(3,41),
(3,101),(3,102),(3,105),(3,106),(3,107),(3,108),(3,109),(3,110),(3,112),(3,115);

INSERT INTO `sys_role_menu`(`role_id`,`menu_id`) VALUES
(4,1),(4,10),(4,11),(4,40),(4,41),
(4,101),(4,102),(4,111),(4,113);

-- ---------- 字典 ----------
INSERT INTO `sys_dict_type`(`dict_id`,`dict_name`,`dict_type`,`remark`) VALUES
(1,'工单来源','ticket_source','工单报修渠道'),
(2,'工单优先级','ticket_priority','优先级'),
(3,'工单状态','ticket_status','生命周期状态'),
(4,'挂起原因','suspend_reason','挂起原因分类'),
(5,'资产状态','asset_status','资产生命周期状态');

INSERT INTO `sys_dict_data`(`dict_type`,`dict_label`,`dict_value`,`list_class`,`order_num`) VALUES
('ticket_source','电话报修','phone','primary',1),
('ticket_source','微信/企微','wechat','success',2),
('ticket_source','现场报障','onsite','warning',3),
('ticket_source','自助提单','self','info',4),
('ticket_source','其他','other','default',5),
('ticket_priority','低','0','info',1),
('ticket_priority','中','1','primary',2),
('ticket_priority','高','2','warning',3),
('ticket_priority','紧急','3','danger',4),
('ticket_status','待受理','PENDING','info',1),
('ticket_status','已指派','ASSIGNED','primary',2),
('ticket_status','处理中','PROCESSING','warning',3),
('ticket_status','已挂起','SUSPENDED','danger',4),
('ticket_status','已解决','RESOLVED','success',5),
('ticket_status','已关闭','CLOSED','success',6),
('ticket_status','已取消','CANCELLED','default',7),
('ticket_status','已合并','MERGED','default',8),
('suspend_reason','等待备件','wait_part','warning',1),
('suspend_reason','等待用户配合','wait_user','warning',2),
('suspend_reason','等待审批','wait_approval','warning',3),
('suspend_reason','其他原因','other','info',4),
('asset_status','在用','0','success',1),
('asset_status','闲置','1','info',2),
('asset_status','维修','2','warning',3),
('asset_status','报废','3','danger',4);

-- ---------- 系统参数 ----------
INSERT INTO `sys_config`(`config_name`,`config_key`,`config_value`,`config_type`,`remark`) VALUES
('主框架页-标题','sys.title','信息部IT运维工单系统','Y','系统显示名称'),
('上传附件大小上限(MB)','sys.upload.maxSize','10','Y','单文件大小限制'),
('上传附件允许类型','sys.upload.allowedTypes','jpg,jpeg,png,gif,webp,bmp,pdf,doc,docx,xls,xlsx','Y','扩展名白名单'),
('现场图片水印开关','sys.watermark.enabled','true','Y','移动端拍照水印'),
('Web 端访问地址','sys.web.url','http://localhost:8088','Y','webhook 消息中工单链接的基址'),
('H5 端访问地址','sys.h5.url','http://localhost:8088/h5','Y','H5 工单链接基址');

-- ---------- 工单分类(示例) ----------
INSERT INTO `biz_category`(`category_id`,`parent_id`,`ancestors`,`category_name`,`order_num`) VALUES
(1,0,'0','硬件故障',1),
(2,0,'0','软件问题',2),
(3,0,'0','网络故障',3),
(4,0,'0','账号与权限',4),
(5,0,'0','会议与电话',5),
(6,0,'0','其他服务',6),
(11,1,'0,1','台式机/笔记本',1),
(12,1,'0,1','打印/复印/扫描',2),
(13,1,'0,1','服务器/存储',3),
(14,1,'0,1','外设(投影/大屏等)',4),
(21,2,'0,2','操作系统',1),
(22,2,'0,2','办公软件',2),
(23,2,'0,2','业务系统',3),
(31,3,'0,3','内网/WiFi',1),
(32,3,'0,3','VPN/互联网',2),
(41,4,'0,4','账号开通/重置',1),
(42,4,'0,4','权限申请/变更',2),
(51,5,'0,5','会议室设备',1),
(52,5,'0,5','固定电话',2);

-- ---------- 常用地点(示例) ----------
INSERT INTO `biz_location`(`location_name`,`building`,`floor`,`room`,`address`,`longitude`,`latitude`) VALUES
('1号楼3层信息部','1号楼','3层','301','机关大院1号楼',116.397428,39.90923),
('1号楼5层大会议室','1号楼','5层','501会议室','机关大院1号楼',116.397520,39.90930),
('2号楼2层财务科','2号楼','2层','208','机关大院2号楼',116.398100,39.90960),
('3号楼1层大厅','3号楼','1层','大厅','机关大院3号楼',116.396900,39.90890);

-- ---------- 资产(示例) ----------
INSERT INTO `biz_asset`(`asset_no`,`asset_name`,`category`,`brand`,`model`,`user_id`,`user_name`,`dept_id`,`location_text`,`status`,`buy_date`) VALUES
('ZC-2023-0001','ThinkCentre M7600t','台式机','联想','M7600t i5/16G',5,'张三',3,'2号楼2层办公室','0','2023-03-10'),
('ZC-2023-0002','HP LaserJet M437n','打印机','惠普','M437n',NULL,'',3,'2号楼2层打印区','0','2023-04-01'),
('ZC-2022-0011','Dell PowerEdge R750','服务器','戴尔','R750 4314/64G',3,'李工',5,'1号楼3层机房','0','2022-11-20');

SET FOREIGN_KEY_CHECKS = 1;
