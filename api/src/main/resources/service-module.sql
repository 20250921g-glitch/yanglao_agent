-- 服务管理模块表结构
CREATE TABLE IF NOT EXISTS `service_worker` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '服务人员ID',
  `name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `avatar` VARCHAR(255) COMMENT '头像',
  `gender` TINYINT COMMENT '性别:0女1男',
  `id_card` VARCHAR(18) COMMENT '身份证号',
  `bank_card` VARCHAR(30) COMMENT '银行卡号',
  `bank_name` VARCHAR(100) COMMENT '开户行',
  `service_type` VARCHAR(50) COMMENT '服务类型:家政护工/康复理疗/上门体检',
  `region` VARCHAR(100) COMMENT '负责区域',
  `intro` TEXT COMMENT '个人简介',
  `certificate` VARCHAR(255) COMMENT '职业证书URL',
  `join_type` VARCHAR(20) COMMENT '加入方式:服务端注册/APP注册',
  `audit_status` TINYINT DEFAULT 0 COMMENT '审核状态:0待审核1已通过2已拒绝',
  `audit_remark` VARCHAR(255) COMMENT '审核备注',
  `allow_tip` TINYINT DEFAULT 1 COMMENT '允许打赏:1是0否',
  `status` TINYINT DEFAULT 1 COMMENT '状态:1正常0禁用',
  `last_login_time` DATETIME COMMENT '最近登录时间',
  `deleted` TINYINT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务人员';

CREATE TABLE IF NOT EXISTS `service_worker_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `tag_name` VARCHAR(50) NOT NULL COMMENT '标签名称',
  `service_type` VARCHAR(50) COMMENT '所属服务类型',
  `color` VARCHAR(20) DEFAULT '#409EFF',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务人员标签';

CREATE TABLE IF NOT EXISTS `service_worker_tag_relation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `worker_id` BIGINT NOT NULL,
  `tag_id` BIGINT NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_worker_tag` (`worker_id`,`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务人员标签关系';

CREATE TABLE IF NOT EXISTS `audit_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `worker_id` BIGINT NOT NULL COMMENT '服务人员ID',
  `audit_type` VARCHAR(20) DEFAULT '入驻审核' COMMENT '审核类型',
  `status` TINYINT NOT NULL COMMENT '状态:0待审核1通过2拒绝',
  `reject_reason` VARCHAR(255) COMMENT '拒绝原因',
  `auditor_id` BIGINT COMMENT '审核人ID',
  `auditor_name` VARCHAR(50) COMMENT '审核人姓名',
  `audit_time` DATETIME COMMENT '审核时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核记录';

CREATE TABLE IF NOT EXISTS `commission_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `worker_id` BIGINT NOT NULL COMMENT '服务人员ID',
  `worker_name` VARCHAR(50) COMMENT '服务人员姓名',
  `order_id` BIGINT COMMENT '关联工单ID',
  `order_no` VARCHAR(50) COMMENT '工单编号',
  `commission_amount` DECIMAL(10,2) NOT NULL COMMENT '佣金金额',
  `service_type` VARCHAR(50) COMMENT '服务类型',
  `order_amount` DECIMAL(10,2) COMMENT '订单金额',
  `commission_rate` DECIMAL(4,3) COMMENT '佣金比例',
  `status` TINYINT DEFAULT 0 COMMENT '状态:0待结算1已结算',
  `settle_time` DATETIME COMMENT '结算时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='佣金记录';

CREATE TABLE IF NOT EXISTS `tip_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `worker_id` BIGINT NOT NULL COMMENT '服务人员ID',
  `worker_name` VARCHAR(50) COMMENT '服务人员姓名',
  `user_id` BIGINT COMMENT '打赏用户ID',
  `order_id` BIGINT COMMENT '关联工单ID',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '打赏金额',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打赏记录';

CREATE TABLE IF NOT EXISTS `service_order_setting` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `service_type` VARCHAR(50) NOT NULL COMMENT '服务类型',
  `commission_rate` DECIMAL(4,3) NOT NULL COMMENT '默认佣金比例',
  `min_withdraw` DECIMAL(10,2) DEFAULT 100.00 COMMENT '最低提现金额',
  `remark` VARCHAR(255) COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单设置';

-- 示例数据
INSERT INTO `service_worker_tag` VALUES (1,'金牌家政','家政护工','#FFD700',NOW());
INSERT INTO `service_worker_tag` VALUES (2,'普通护工','家政护工','#909399',NOW());
INSERT INTO `service_worker_tag` VALUES (3,'高级理疗师','康复理疗','#F56C6C',NOW());
INSERT INTO `service_worker_tag` VALUES (4,'中级理疗师','康复理疗','#E6A23C',NOW());
INSERT INTO `service_worker_tag` VALUES (5,'初级理疗师','康复理疗','#67C23A',NOW());
INSERT INTO `service_worker_tag` VALUES (6,'专业护士','上门体检','#409EFF',NOW());

INSERT INTO `service_worker` VALUES (1,'王芳','13900001111','https://api.dicebear.com/7.x/avataaars/svg?seed=wangfang',0,'310101198001011234','6222021234567890','中国工商银行','家政护工','浦东新区','10年养老护理经验，持有护理员证书','/certs/nurse1.jpg','APP注册',1,NULL,1,1,NOW(),0,NOW(),NOW());
INSERT INTO `service_worker` VALUES (2,'李强','13900002222','https://api.dicebear.com/7.x/avataaars/svg?seed=liqiang',1,'310101198501011234','6222021234567891','中国建设银行','康复理疗','徐汇区','康复治疗师，擅长中医理疗','/certs/therapy1.jpg','服务端注册',1,NULL,1,1,NOW(),0,NOW(),NOW());
INSERT INTO `service_worker` VALUES (3,'张红','13900003333','https://api.dicebear.com/7.x/avataaars/svg?seed=zhanghong',0,'310101199001011234',NULL,NULL,'上门体检','静安区','全科医生，退休主治医师','/certs/doctor1.jpg','APP注册',0,'提交资料审核中',1,1,NOW(),0,NOW(),NOW());

INSERT INTO `service_worker_tag_relation` VALUES (1,1,1,NOW()),(2,1,2,NOW());
INSERT INTO `service_worker_tag_relation` VALUES (3,2,3,NOW());
INSERT INTO `audit_record` VALUES (1,1,'入驻审核',1,NULL,1,'管理员',NOW(),NOW());
INSERT INTO `audit_record` VALUES (2,2,'入驻审核',1,NULL,1,'管理员',NOW(),NOW());
INSERT INTO `audit_record` VALUES (3,3,'入驻审核',0,NULL,NULL,NULL,NOW(),NOW());

INSERT INTO `service_order_setting` VALUES (1,'家政护工',0.150,100.00,'默认15%佣金比例',NOW(),NOW());
INSERT INTO `service_order_setting` VALUES (2,'康复理疗',0.200,100.00,'默认20%佣金比例',NOW(),NOW());
INSERT INTO `service_order_setting` VALUES (3,'上门体检',0.180,100.00,'默认18%佣金比例',NOW(),NOW());

INSERT INTO `commission_record` VALUES (1,1,'王芳',1,'WO202507010001',75.00,'家政护工',500.00,0.150,1,NOW(),NOW());
INSERT INTO `commission_record` VALUES (2,2,'李强',2,'WO202507010002',80.00,'康复理疗',400.00,0.200,1,NOW(),NOW());
