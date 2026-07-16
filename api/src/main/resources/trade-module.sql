-- 交易管理模块表结构
CREATE TABLE IF NOT EXISTS `refund` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '售后ID',
  `refund_no` VARCHAR(50) NOT NULL COMMENT '售后单号',
  `order_id` BIGINT COMMENT '关联订单ID',
  `order_no` VARCHAR(50) COMMENT '订单编号',
  `user_id` BIGINT COMMENT '用户ID',
  `user_name` VARCHAR(50) COMMENT '用户姓名',
  `user_phone` VARCHAR(20) COMMENT '用户电话',
  `refund_amount` DECIMAL(10,2) NOT NULL COMMENT '申请退款金额',
  `actual_amount` DECIMAL(10,2) COMMENT '实际退款金额',
  `refund_reason` VARCHAR(255) COMMENT '退款原因',
  `refund_description` TEXT COMMENT '退款说明',
  `refund_type` VARCHAR(20) COMMENT '退款类型:整单退款/部分退款',
  `status` TINYINT DEFAULT 0 COMMENT '状态:0处理中1售后完成2售后关闭',
  `handle_time` DATETIME COMMENT '处理时间',
  `handle_remark` VARCHAR(255) COMMENT '处理备注',
  `handler_id` BIGINT COMMENT '处理人ID',
  `handler_name` VARCHAR(50) COMMENT '处理人姓名',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款售后';

CREATE TABLE IF NOT EXISTS `order_evaluation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(50) COMMENT '订单编号',
  `user_id` BIGINT COMMENT '评价用户ID',
  `user_name` VARCHAR(50) COMMENT '评价用户',
  `worker_id` BIGINT COMMENT '服务人员ID',
  `worker_name` VARCHAR(50) COMMENT '服务人员',
  `score` INT NOT NULL COMMENT '评分1-5',
  `content` TEXT COMMENT '评价内容',
  `reply` TEXT COMMENT '商家回复',
  `reply_time` DATETIME COMMENT '回复时间',
  `status` TINYINT DEFAULT 1 COMMENT '状态:1显示0隐藏',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单评价';

CREATE TABLE IF NOT EXISTS `withdrawal` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '提现ID',
  `withdraw_no` VARCHAR(50) NOT NULL COMMENT '提现单号',
  `worker_id` BIGINT NOT NULL COMMENT '服务人员ID',
  `worker_name` VARCHAR(50) COMMENT '服务人员姓名',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '提现金额',
  `withdraw_type` VARCHAR(20) DEFAULT '银行卡' COMMENT '提现方式',
  `bank_card` VARCHAR(30) COMMENT '银行卡号',
  `bank_name` VARCHAR(100) COMMENT '开户行',
  `status` TINYINT DEFAULT 0 COMMENT '状态:0待审核1已通过2已拒绝',
  `reject_reason` VARCHAR(255) COMMENT '拒绝原因',
  `audit_time` DATETIME COMMENT '审核时间',
  `audit_remark` VARCHAR(255) COMMENT '审核备注',
  `auditor_id` BIGINT COMMENT '审核人ID',
  `auditor_name` VARCHAR(50) COMMENT '审核人',
  `complete_time` DATETIME COMMENT '完成时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提现记录';

CREATE TABLE IF NOT EXISTS `refund_reason` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `reason` VARCHAR(100) NOT NULL COMMENT '退款原因',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态:1启用0禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款原因';

CREATE TABLE IF NOT EXISTS `transaction_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(50) COMMENT '关联订单号',
  `type` VARCHAR(20) COMMENT '类型:收入/支出',
  `category` VARCHAR(50) COMMENT '类别:服务费/退款/佣金/提现',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '金额',
  `balance` DECIMAL(10,2) COMMENT '余额',
  `remark` VARCHAR(255) COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收支明细';

-- 示例数据
INSERT INTO `refund_reason` VALUES (1,'未按时上门',1,1,NOW());
INSERT INTO `refund_reason` VALUES (2,'未完成约定的工作',2,1,NOW());
INSERT INTO `refund_reason` VALUES (3,'服务质量不佳',3,1,NOW());
INSERT INTO `refund_reason` VALUES (4,'超出规定的服务时间',4,1,NOW());
INSERT INTO `refund_reason` VALUES (5,'未经同意的额外收费',5,1,NOW());
INSERT INTO `refund_reason` VALUES (6,'服务欺诈',6,1,NOW());

INSERT INTO `withdrawal` VALUES (1,'WD202507010001',1,'王芳',500.00,'银行卡','6222021234567890','中国工商银行',1,NULL,NOW(),'审核通过',1,'管理员',NOW(),NOW());
INSERT INTO `withdrawal` VALUES (2,'WD202507020001',2,'李强',300.00,'银行卡','6222021234567891','中国建设银行',0,NULL,NULL,NULL,NULL,NULL,NOW(),NOW());

INSERT INTO `order_evaluation` VALUES (1,1,'SO202507010001',1,'张三',1,'王芳',5,'服务非常满意，王阿姨非常专业，态度也很好！',NULL,NULL,1,NOW());
INSERT INTO `order_evaluation` VALUES (2,2,'SO202507010002',2,'李四',2,'李强',4,'手法不错，就是时间有点短',NULL,NULL,1,NOW());

INSERT INTO `refund` VALUES (1,'RF202507030001',3,'SO202507030001',1,'张三','13800138001',200.00,200.00,'服务不满意','服务人员迟到2小时且态度差','整单退款',1,NOW(),'已退款',1,'管理员',NOW(),NOW());
INSERT INTO `refund` VALUES (2,'RF202507050001',5,'SO202507050001',2,'李四','13800138002',150.00,NULL,'未完成服务','只服务了一半就走了','整单退款',0,NULL,NULL,NULL,NULL,NOW(),NOW());
