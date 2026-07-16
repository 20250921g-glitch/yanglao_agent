-- 补齐 elderly_care_20250921 中代码引用但建库时缺失的 14 张表
-- 仅作用于自己的库，符合老师红线。所有列按对应实体类反推，保证与代码一致。
USE elderly_care_20250921;

CREATE TABLE IF NOT EXISTS `activity_field` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `label` VARCHAR(100) DEFAULT NULL COMMENT '字段标签',
  `type` VARCHAR(50) DEFAULT NULL COMMENT '字段类型',
  `options` TEXT COMMENT '选项(JSON)',
  `required` INT DEFAULT 0 COMMENT '1必填0选填',
  `status` INT DEFAULT 1 COMMENT '1启用0停用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动报名字段';

CREATE TABLE IF NOT EXISTS `app_message_read` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `message_id` BIGINT NOT NULL COMMENT 'app_message.id',
  `user_id` BIGINT NOT NULL COMMENT 'app_user.id',
  `read_time` DATETIME DEFAULT NULL COMMENT '已读时间,空=未读',
  `hidden` TINYINT DEFAULT 0 COMMENT '1=用户已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_msg_user` (`message_id`,`user_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='C端消息已读/隐藏状态';

CREATE TABLE IF NOT EXISTS `conversation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
  `user_name` VARCHAR(50) DEFAULT NULL COMMENT '用户姓名',
  `user_avatar` VARCHAR(255) DEFAULT NULL COMMENT '用户头像',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `last_message` TEXT COMMENT '最后一条消息',
  `unread_count` INT DEFAULT 0 COMMENT '未读消息数',
  `msg_count` INT DEFAULT 0 COMMENT '消息总数',
  `status` TINYINT DEFAULT 1 COMMENT '状态:1正常0禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话管理';

CREATE TABLE IF NOT EXISTS `conversation_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `conversation_id` BIGINT DEFAULT NULL COMMENT '会话ID',
  `sender_type` INT DEFAULT NULL COMMENT '1用户2客服',
  `content` TEXT COMMENT '消息内容',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_conversation` (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话消息';

CREATE TABLE IF NOT EXISTS `dynamic_comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `dynamic_id` BIGINT DEFAULT NULL COMMENT '邻里圈动态ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '评论用户ID',
  `user_name` VARCHAR(50) DEFAULT NULL COMMENT '评论用户姓名',
  `content` TEXT COMMENT '评论内容',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_dynamic` (`dynamic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邻里圈评论';

CREATE TABLE IF NOT EXISTS `dynamic_favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `dynamic_id` BIGINT DEFAULT NULL COMMENT '邻里圈动态ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '收藏用户ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_dynamic` (`dynamic_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邻里圈收藏';

CREATE TABLE IF NOT EXISTS `dynamic_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `dynamic_id` BIGINT DEFAULT NULL COMMENT '邻里圈动态ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '点赞用户ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_dynamic` (`dynamic_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邻里圈点赞';

CREATE TABLE IF NOT EXISTS `growth_rule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
  `rule_code` VARCHAR(50) DEFAULT NULL COMMENT '规则编码',
  `action_type` VARCHAR(50) DEFAULT NULL COMMENT '触发动作类型',
  `growth_value` INT DEFAULT 0 COMMENT '成长值',
  `limit_count` INT DEFAULT 0 COMMENT '每日上限次数',
  `status` TINYINT DEFAULT 1 COMMENT '状态:1启用0禁用',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_code` (`rule_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成长值规则';

CREATE TABLE IF NOT EXISTS `health_service_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `elder_id` BIGINT DEFAULT NULL COMMENT '老人ID',
  `elder_name` VARCHAR(50) DEFAULT NULL COMMENT '老人姓名',
  `service_name` VARCHAR(100) DEFAULT NULL COMMENT '服务名称',
  `worker_id` BIGINT DEFAULT NULL COMMENT '服务人员ID',
  `worker_name` VARCHAR(50) DEFAULT NULL COMMENT '服务人员姓名',
  `service_time` DATETIME DEFAULT NULL COMMENT '服务时间',
  `duration` INT DEFAULT NULL COMMENT '时长(分钟)',
  `status` VARCHAR(20) DEFAULT NULL COMMENT '订单状态',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康服务订单';

CREATE TABLE IF NOT EXISTS `medicine_unit` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) DEFAULT NULL COMMENT '单位名称',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
  `status` INT DEFAULT 1 COMMENT '1启用0停用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品单位';

CREATE TABLE IF NOT EXISTS `points_rule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
  `rule_code` VARCHAR(50) DEFAULT NULL COMMENT '规则编码',
  `action_type` VARCHAR(50) DEFAULT NULL COMMENT '触发动作类型',
  `points` INT DEFAULT 0 COMMENT '积分值',
  `limit_count` INT DEFAULT 0 COMMENT '每日上限次数',
  `status` TINYINT DEFAULT 1 COMMENT '状态:1启用0禁用',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_code` (`rule_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分规则';

CREATE TABLE IF NOT EXISTS `salary_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `worker_id` BIGINT NOT NULL COMMENT '服务人员ID',
  `worker_name` VARCHAR(50) DEFAULT NULL COMMENT '服务人员姓名',
  `month` VARCHAR(7) NOT NULL COMMENT '月份: 2026-07',
  `base_salary` DECIMAL(10,2) DEFAULT 0 COMMENT '基本工资',
  `commission_total` DECIMAL(10,2) DEFAULT 0 COMMENT '佣金合计',
  `bonus` DECIMAL(10,2) DEFAULT 0 COMMENT '奖金',
  `deduction` DECIMAL(10,2) DEFAULT 0 COMMENT '扣款',
  `total_salary` DECIMAL(10,2) DEFAULT NULL COMMENT '应发工资',
  `status` TINYINT DEFAULT 0 COMMENT '状态: 0待发放 1已发放',
  `pay_time` DATETIME DEFAULT NULL COMMENT '发放时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_worker_month` (`worker_id`,`month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工资记录表';

CREATE TABLE IF NOT EXISTS `service_project` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) DEFAULT NULL COMMENT '项目名称',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '分类',
  `duration` INT DEFAULT NULL COMMENT '时长(分钟)',
  `price` DECIMAL(10,2) DEFAULT NULL COMMENT '价格',
  `method` VARCHAR(50) DEFAULT NULL COMMENT '服务方式',
  `status` INT DEFAULT 0 COMMENT '1上架0下架',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='养老服务项目';

CREATE TABLE IF NOT EXISTS `sys_operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
  `user_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人',
  `module` VARCHAR(100) DEFAULT NULL COMMENT '模块',
  `operation` VARCHAR(200) DEFAULT NULL COMMENT '操作描述',
  `method` VARCHAR(10) DEFAULT NULL COMMENT 'HTTP方法',
  `request_uri` VARCHAR(255) DEFAULT NULL COMMENT '请求URI',
  `ip` VARCHAR(64) DEFAULT NULL COMMENT '操作IP',
  `params` TEXT COMMENT '请求参数',
  `status` INT DEFAULT 1 COMMENT '1成功0失败',
  `error_msg` VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志';

SELECT 'DONE: missing tables created' AS result;
