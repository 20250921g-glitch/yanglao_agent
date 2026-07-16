-- =====================================================================
-- 演示种子数据 (2026-07-16 schema drift 补齐后写入)
-- 仅作用于自有库 elderly_care_20250921，所有表此前均为空（count=0）。
-- 重部署说明：若库已存在数据，请先 TRUNCATE 对应表再执行本文件。
-- =====================================================================

-- 1) 养老服务 service_project（用户端"养老服务"界面）
INSERT INTO service_project (id, name, category, duration, price, method, status) VALUES
(1, '居家保洁服务', '生活照料', 120, 99.00, '上门服务', 1),
(2, '助餐配送服务', '生活照料', 60, 39.00, '上门服务', 1),
(3, '健康体检套餐', '健康管理', 180, 299.00, '到店服务', 1),
(4, '康复理疗', '健康管理', 90, 159.00, '上门服务', 1),
(5, '陪同就医', '生活照料', 240, 199.00, '陪同服务', 1),
(6, '智能穿戴设备租借', '智慧养老', 30, 49.00, '到店服务', 1);

-- 2) 消息通知 app_message（用户端"消息通知"界面）
INSERT INTO app_message (id, title, content, type, status, target, send_time) VALUES
(1, '系统通知', '欢迎使用智慧养老系统，您的健康档案已创建成功。', 'system', 1, 'all', '2026-07-16 09:00:00'),
(2, '活动提醒', '本周六上午9点将在社区活动中心举办健康讲座，欢迎报名参加。', 'activity', 1, 'all', '2026-07-16 10:00:00'),
(3, '服务提醒', '您预约的居家保洁服务将于明日上门，请保持电话畅通。', 'service', 1, 'all', '2026-07-16 11:00:00'),
(4, '健康建议', '根据您的健康档案，建议本周增加两次散步，注意低盐饮食。', 'health', 1, 'all', '2026-07-16 12:00:00');

-- 3) 会话 conversation（消息通知-会话列表）
INSERT INTO conversation (id, user_id, user_name, user_avatar, phone, last_message, unread_count, msg_count, status) VALUES
(1, 1, '张大爷', 'https://api.dicebear.com/7.x/initials/svg?seed=张', '13800001234', '感谢护理员的悉心照料', 2, 5, 1),
(2, 2, '李奶奶', 'https://api.dicebear.com/7.x/initials/svg?seed=李', '13800005678', '请问明天还能预约体检吗？', 1, 3, 1);

-- 4) 会话消息 conversation_message
INSERT INTO conversation_message (conversation_id, sender_type, content) VALUES
(1, 1, '您好，我是您的专属护理员小王。'),
(1, 0, '谢谢你们，老人家很满意。'),
(1, 1, '不客气，这是我们应该做的，有需要随时联系。'),
(2, 0, '请问明天还能预约体检吗？'),
(2, 1, '可以的，我来帮您安排，稍后确认时间。');

-- 5) 消息已读 app_message_read
INSERT INTO app_message_read (message_id, user_id, read_time, hidden, create_time) VALUES
(1, 1, '2026-07-16 09:30:00', 0, '2026-07-16 09:30:00'),
(1, 2, '2026-07-16 09:35:00', 0, '2026-07-16 09:35:00'),
(2, 1, '2026-07-16 10:15:00', 0, '2026-07-16 10:15:00'),
(3, 1, '2026-07-16 11:20:00', 0, '2026-07-16 11:20:00');

-- 6) 邻里圈互动：点赞 / 评论 / 收藏（引用已有 dynamic 的第一条）
INSERT INTO dynamic_like (dynamic_id, user_id) VALUES
((SELECT MIN(id) FROM dynamic), 1),
((SELECT MIN(id) FROM dynamic), 2),
((SELECT MIN(id) FROM dynamic), 3);
INSERT INTO dynamic_comment (dynamic_id, user_id, user_name, content) VALUES
((SELECT MIN(id) FROM dynamic), 1, '王阿姨', '说得太好了，点赞支持！'),
((SELECT MIN(id) FROM dynamic), 2, '李叔叔', '这个活动我也想参加。'),
((SELECT MIN(id) FROM dynamic), 4, '赵奶奶', '感谢分享，很实用。');
INSERT INTO dynamic_favorite (dynamic_id, user_id) VALUES
((SELECT MIN(id) FROM dynamic), 3),
((SELECT MIN(id) FROM dynamic), 5);

-- 7) 活动报名字段 activity_field
INSERT INTO activity_field (label, type, options, required, status) VALUES
('姓名', 'input', NULL, 1, 1),
('手机号', 'input', NULL, 1, 1),
('参与人数', 'number', NULL, 1, 1),
('备注', 'textarea', NULL, 0, 1);

-- 8) 药品单位 medicine_unit
INSERT INTO medicine_unit (name, description, status) VALUES
('片', '片剂', 1),
('粒', '胶囊', 1),
('毫升', '液体剂型', 1),
('支', '注射剂', 1);

-- 9) 成长规则 growth_rule（rule_name NOT NULL）
INSERT INTO growth_rule (rule_name, rule_code, action_type, growth_value, limit_count, status, remark) VALUES
('每日签到', 'daily_sign', 'sign', 5, 1, 1, '每日首次签到获得成长值'),
('完善健康档案', 'complete_profile', 'profile', 20, 1, 1, '完善个人健康档案'),
('发布动态', 'publish_dynamic', 'dynamic', 10, 0, 1, '发布一条邻里圈动态');

-- 10) 积分规则 points_rule（rule_name NOT NULL）
INSERT INTO points_rule (rule_name, rule_code, action_type, points, limit_count, status, remark) VALUES
('每日签到', 'daily_sign', 'sign', 5, 1, 1, '每日首次签到获得积分'),
('参与活动', 'join_activity', 'activity', 15, 0, 1, '报名并参加一次活动'),
('邀请好友', 'invite_friend', 'invite', 50, 0, 1, '成功邀请一位好友注册');

-- 11) 工资记录 salary_record（worker_id / month NOT NULL）
INSERT INTO salary_record (worker_id, worker_name, month, base_salary, commission_total, bonus, deduction, total_salary, status, pay_time) VALUES
(1, '护理员小王', '2026-07', 5000.00, 800.00, 500.00, 100.00, 6200.00, 1, '2026-07-10 00:00:00'),
(2, '护理员小李', '2026-07', 5000.00, 600.00, 300.00, 0.00, 5900.00, 1, '2026-07-10 00:00:00');

-- 12) 健康服务订单 health_service_order
INSERT INTO health_service_order (elder_id, elder_name, service_name, worker_id, worker_name, service_time, duration, status, remark) VALUES
(1, '张大爷', '居家保洁服务', 1, '护理员小王', '2026-07-17 09:00:00', 120, 'pending', '需要打扫客厅和卧室'),
(2, '李奶奶', '健康体检套餐', 2, '护理员小李', '2026-07-18 14:00:00', 180, 'assigned', '需陪同前往社区医院');

-- 13) 操作日志 sys_operation_log
INSERT INTO sys_operation_log (user_id, user_name, module, operation, method, request_uri, ip, params, status) VALUES
(1, 'admin', '用户管理', '查询用户列表', 'GET', '/api/user/page', '127.0.0.1', 'current=1&size=10', 1),
(1, 'admin', '养老服务', '查询服务项目', 'GET', '/api/product/service-project/page', '127.0.0.1', 'current=1&size=10', 1),
(1, 'admin', '消息管理', '查询消息列表', 'GET', '/api/user/message/page', '127.0.0.1', 'current=1&size=10', 1);

SELECT 'SEED_DONE' AS result;
