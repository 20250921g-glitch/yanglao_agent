USE `elderly_care`;

-- 用户管理模块表结构
CREATE TABLE IF NOT EXISTS `app_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) COMMENT '用户名/昵称',
  `real_name` VARCHAR(50) COMMENT '真实姓名',
  `phone` VARCHAR(20) COMMENT '手机号',
  `avatar` VARCHAR(255) COMMENT '头像URL',
  `gender` TINYINT COMMENT '性别:0女1男',
  `birth_date` DATE COMMENT '出生日期',
  `id_card` VARCHAR(18) COMMENT '身份证号',
  `level_id` BIGINT COMMENT '会员等级ID',
  `address` VARCHAR(255) COMMENT '家庭住址',
  `emergency_contact` VARCHAR(50) COMMENT '紧急联系人',
  `emergency_phone` VARCHAR(20) COMMENT '紧急联系电话',
  `status` TINYINT DEFAULT 1 COMMENT '状态:1正常0禁用',
  `source` VARCHAR(20) COMMENT '注册来源:APP注册/服务端注册',
  `role` VARCHAR(20) DEFAULT 'FAMILY' COMMENT '用户类型:ELDER老人/FAMILY家属/VOLUNTEER志愿者/STAFF工作人员',
  `remark` TEXT COMMENT '备注',
  `deleted` TINYINT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='APP用户';

CREATE TABLE IF NOT EXISTS `user_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `tag_name` VARCHAR(50) NOT NULL COMMENT '标签名称',
  `tag_type` VARCHAR(20) COMMENT '标签类型:客户类型/健康标签',
  `color` VARCHAR(20) DEFAULT '#409EFF' COMMENT '标签颜色',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户标签';

CREATE TABLE IF NOT EXISTS `user_tag_relation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `tag_id` BIGINT NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_tag` (`user_id`,`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户标签关系';

CREATE TABLE IF NOT EXISTS `member_level` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `level_name` VARCHAR(50) NOT NULL COMMENT '等级名称',
  `level_icon` VARCHAR(255) COMMENT '等级图标URL',
  `min_score` INT DEFAULT 0 COMMENT '最低成长值',
  `max_score` INT COMMENT '最高成长值',
  `discount` DECIMAL(4,2) DEFAULT 1.00 COMMENT '折扣率',
  `benefits` TEXT COMMENT '权益说明',
  `status` TINYINT DEFAULT 1 COMMENT '状态:1启用0禁用',
  `sort` INT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员等级';

CREATE TABLE IF NOT EXISTS `coupon` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL COMMENT '优惠券名称',
  `type` TINYINT COMMENT '类型:1满减券2折扣券',
  `denomination` DECIMAL(10,2) COMMENT '面额/折扣值',
  `min_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '使用门槛:满XX可用',
  `total_count` INT COMMENT '发放总量',
  `remain_count` INT COMMENT '剩余数量',
  `start_time` DATETIME COMMENT '开始时间',
  `end_time` DATETIME COMMENT '结束时间',
  `status` TINYINT DEFAULT 1 COMMENT '状态:1生效0失效',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券';

CREATE TABLE IF NOT EXISTS `coupon_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `coupon_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `order_id` BIGINT COMMENT '关联订单ID',
  `status` TINYINT DEFAULT 0 COMMENT '状态:0未使用1已使用2已过期',
  `receive_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `use_time` DATETIME COMMENT '使用时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券领取记录';

CREATE TABLE IF NOT EXISTS `app_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL COMMENT '消息标题',
  `content` TEXT COMMENT '消息内容',
  `type` VARCHAR(20) COMMENT '消息类型:系统通知/活动推送',
  `target` VARCHAR(20) DEFAULT 'all' COMMENT '发送对象:all全部指定用户',
  `send_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `status` TINYINT DEFAULT 1 COMMENT '状态:1已发送0草稿',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='APP消息';

-- C端消息已读/隐藏状态（app_message 为广播式，每用户对每条消息独立一行）
CREATE TABLE IF NOT EXISTS `app_message_read` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `message_id` BIGINT NOT NULL COMMENT 'app_message.id',
  `user_id` BIGINT NOT NULL COMMENT 'app_user.id',
  `read_time` DATETIME NULL COMMENT '已读时间,空=未读',
  `hidden` TINYINT DEFAULT 0 COMMENT '1=用户已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_msg_user` (`message_id`, `user_id`),
  INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='C端消息已读/隐藏状态';

-- C端演示消息（广播 target=all, status=1 已发送）
INSERT INTO `app_message` (`id`,`title`,`content`,`type`,`target`,`status`,`send_time`) VALUES
(1,'欢迎使用智慧养老平台','感谢您使用智慧养老服务平台，您可在此查看系统通知、活动推送与服务提醒。','系统通知','all',1,'2026-07-13 09:00:58'),
(2,'健康档案已更新','您关联老人的健康档案已同步最新体检数据，请前往健康档案查看。','系统通知','all',1,'2026-07-13 10:30:00'),
(3,'社区义诊活动报名开启','本周六上午9:00社区将举办免费义诊活动，名额有限，请尽快在活动报名中预约。','活动推送','all',1,'2026-07-13 14:00:00'),
(4,'养老服务上门提醒','您预约的助浴服务已由服务人员接单，将于明日上午10:00上门，请保持电话畅通。','服务提醒','all',1,'2026-07-14 08:15:00'),
(5,'商城新品上架','养生食品、康复辅具等新品已上架养老商城，会员享专属折扣，快去看看吧。','活动推送','all',1,'2026-07-14 09:40:00')
ON DUPLICATE KEY UPDATE `title`=VALUES(`title`),`content`=VALUES(`content`),`type`=VALUES(`type`),`status`=VALUES(`status`),`send_time`=VALUES(`send_time`);

-- 示例数据
INSERT INTO `member_level` VALUES (1,'普通会员','/icons/vip1.png',0,999,1.00,'享受基础服务','1',0,NOW());
INSERT INTO `member_level` VALUES (2,'银卡会员','/icons/vip2.png',1000,4999,0.95,'享受9.5折优惠','1',1,NOW());
INSERT INTO `member_level` VALUES (3,'金卡会员','/icons/vip3.png',5000,9999,0.90,'享受9折优惠+优先服务','1',2,NOW());
INSERT INTO `member_level` VALUES (4,'钻石会员','/icons/vip4.png',10000,99999999,0.85,'享受8.5折+专属客服','1',3,NOW());

INSERT INTO `user_tag` VALUES (1,'潜在客户','客户类型','#909399',NOW());
INSERT INTO `user_tag` VALUES (2,'重点客户','客户类型','#E6A23C',NOW());
INSERT INTO `user_tag` VALUES (3,'多次消费','客户类型','#67C23A',NOW());
INSERT INTO `user_tag` VALUES (4,'高血压','健康标签','#F56C6C',NOW());
INSERT INTO `user_tag` VALUES (5,'高血糖','健康标签','#F56C6C',NOW());
INSERT INTO `user_tag` VALUES (6,'高血脂','健康标签','#F56C6C',NOW());
INSERT INTO `user_tag` VALUES (7,'冠心病','健康标签','#F56C6C',NOW());
INSERT INTO `user_tag` VALUES (8,'慢性病','健康标签','#E6A23C',NOW());

INSERT INTO `coupon` VALUES (1,'新用户满100减20券',1,20.00,100.00,100,98,NOW(),DATE_ADD(NOW(),INTERVAL 30 DAY),1,NOW());
INSERT INTO `coupon` VALUES (2,'满200打8折折扣券',2,0.80,200.00,50,48,NOW(),DATE_ADD(NOW(),INTERVAL 15 DAY),1,NOW());

INSERT INTO `app_user` VALUES (1,'zhangsan','张三','13800138001','https://api.dicebear.com/7.x/avataaars/svg?seed=zhangsan',1,'1970-05-10','310101197005101234',2,'上海市浦东新区陆家嘴','李四','13900139001',1,'APP注册','FAMILY','高血压患者，定期体检',0,NOW(),NOW());
INSERT INTO `app_user` VALUES (2,'lisi','李四','13800138002','https://api.dicebear.com/7.x/avataaars/svg?seed=lisi',0,'1965-08-20','310101196508201234',3,'北京市朝阳区望京','王五','13900139002',1,'APP注册','FAMILY','糖尿病患者',0,NOW(),NOW());
INSERT INTO `app_user` VALUES (3,'wangwu','王五','13800138003','https://api.dicebear.com/7.x/avataaars/svg?seed=wangwu',1,'1958-12-05','310101195812051234',4,'广州市天河区珠江新城','赵六','13900139003',1,'服务端注册','FAMILY','冠心病+高血脂',0,NOW(),NOW());

INSERT INTO `user_tag_relation` VALUES (1,1,4,NOW()),(2,1,8,NOW());
INSERT INTO `user_tag_relation` VALUES (3,2,5,NOW()),(4,2,8,NOW());
INSERT INTO `user_tag_relation` VALUES (5,3,6,NOW()),(6,3,7,NOW()),(7,3,2,NOW());

INSERT INTO `coupon_record` VALUES (1,1,1,NULL,0,NOW(),NULL);
INSERT INTO `coupon_record` VALUES (2,1,2,NULL,0,NOW(),NULL);

CREATE TABLE IF NOT EXISTS `conversation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `user_name` VARCHAR(50) COMMENT '用户姓名',
  `user_avatar` VARCHAR(255) COMMENT '用户头像',
  `last_message` TEXT COMMENT '最后一条消息',
  `unread_count` INT DEFAULT 0 COMMENT '未读消息数',
  `status` TINYINT DEFAULT 1 COMMENT '状态:1正常0禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX idx_user_id (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话管理';

CREATE TABLE IF NOT EXISTS `points_rule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
  `rule_code` VARCHAR(50) COMMENT '规则编码',
  `action_type` VARCHAR(50) COMMENT '触发动作类型',
  `points` INT DEFAULT 0 COMMENT '积分值',
  `limit_count` INT DEFAULT 0 COMMENT '每日上限次数',
  `status` TINYINT DEFAULT 1 COMMENT '状态:1启用0禁用',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_rule_code (`rule_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分规则';

CREATE TABLE IF NOT EXISTS `growth_rule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
  `rule_code` VARCHAR(50) COMMENT '规则编码',
  `action_type` VARCHAR(50) COMMENT '触发动作类型',
  `growth_value` INT DEFAULT 0 COMMENT '成长值',
  `limit_count` INT DEFAULT 0 COMMENT '每日上限次数',
  `status` TINYINT DEFAULT 1 COMMENT '状态:1启用0禁用',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY uk_growth_code (`rule_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成长值规则';

INSERT INTO `conversation` VALUES (1,1,'张三','https://api.dicebear.com/7.x/avataaars/svg?seed=zhangsan','您好，请问有什么可以帮助您的？',2,1,NOW(),NOW());
INSERT INTO `conversation` VALUES (2,2,'李四','https://api.dicebear.com/7.x/avataaars/svg?seed=lisi','请问怎么预约服务？',0,1,NOW(),NOW());
INSERT INTO `conversation` VALUES (3,3,'王五','https://api.dicebear.com/7.x/avataaars/svg?seed=wangwu','我的订单什么时候发货？',1,1,NOW(),NOW());

INSERT INTO `points_rule` VALUES (1,'每日登录','LOGIN_DAILY','登录',10,1,1,'每日登录获得10积分',NOW(),NOW());
INSERT INTO `points_rule` VALUES (2,'完善个人信息','完善信息','完善信息',50,1,1,'完善个人信息获得50积分',NOW(),NOW());
INSERT INTO `points_rule` VALUES (3,'首次下单','FIRST_ORDER','下单',100,1,1,'首次下单获得100积分',NOW(),NOW());
INSERT INTO `points_rule` VALUES (4,'评价订单','EVALUATE','评价',20,3,1,'评价订单每次获得20积分，每日上限3次',NOW(),NOW());
INSERT INTO `points_rule` VALUES (5,'分享活动','SHARE','分享',15,5,1,'分享活动每次获得15积分，每日上限5次',NOW(),NOW());

INSERT INTO `growth_rule` VALUES (1,'每日登录','LOGIN_DAILY','登录',2,1,1,'每日登录获得2成长值',NOW(),NOW());
INSERT INTO `growth_rule` VALUES (2,'消费金额','CONSUME','消费',1,0,1,'每消费1元获得1成长值',NOW(),NOW());
INSERT INTO `growth_rule` VALUES (3,'完成服务','COMPLETE_SERVICE','服务',10,0,1,'完成一次服务获得10成长值',NOW(),NOW());
INSERT INTO `growth_rule` VALUES (4,'邀请好友','INVITE','邀请',50,0,1,'成功邀请好友注册获得50成长值',NOW(),NOW());
