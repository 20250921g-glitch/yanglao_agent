-- ============================================================
-- P4-3 后端数据层补齐脚本
-- 作用：为 6 个核心业务页补齐缺失字段与种子数据，消除页面「—」
-- 特点：ALTER 通过条件存储过程实现，可重复执行；种子数据幂等
-- 说明：本脚本直接作用于运行库，不 DROP 任何表，风险可控
-- ============================================================

USE elderly_care;

-- 条件加列存储过程（重复执行安全）
DROP PROCEDURE IF EXISTS p4_add_col;
DELIMITER //
CREATE PROCEDURE p4_add_col(IN tbl VARCHAR(64), IN col VARCHAR(64), IN def VARCHAR(255))
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'elderly_care' AND table_name = tbl AND column_name = col
    ) THEN
        SET @sql = CONCAT('ALTER TABLE ', tbl, ' ADD COLUMN ', col, ' ', def);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

-- 1. dynamic 表补齐字段（话题 / 发布人手机号 / 收藏 / 分享 / 评论）
CALL p4_add_col('dynamic', 'topic', 'VARCHAR(200) DEFAULT NULL');
CALL p4_add_col('dynamic', 'user_phone', 'VARCHAR(20) DEFAULT NULL');
CALL p4_add_col('dynamic', 'collect_count', 'INT DEFAULT 0');
CALL p4_add_col('dynamic', 'share_count', 'INT DEFAULT 0');
CALL p4_add_col('dynamic', 'comment_count', 'INT DEFAULT 0');

-- 2. product / activity 补齐「最后更新人」
CALL p4_add_col('product', 'update_by', 'VARCHAR(100) DEFAULT NULL');
CALL p4_add_col('activity', 'update_by', 'VARCHAR(100) DEFAULT NULL');

DROP PROCEDURE p4_add_col;

-- ============================================================
-- 种子数据（幂等：关系表先清空再插入）
-- ============================================================

-- 1) 用户标签关系：让「用户列表」标签列不再显示 —
DELETE FROM user_tag_relation;
INSERT INTO user_tag_relation (user_id, tag_id)
SELECT 1, id FROM user_tag WHERE tag_name = '高血压'
UNION ALL SELECT 1, id FROM user_tag WHERE tag_name = '运动疗法'
UNION ALL SELECT 2, id FROM user_tag WHERE tag_name = '高血糖'
UNION ALL SELECT 2, id FROM user_tag WHERE tag_name = '心理疗法'
UNION ALL SELECT 3, id FROM user_tag WHERE tag_name = '高血脂'
UNION ALL SELECT 4, id FROM user_tag WHERE tag_name = '慢性病'
UNION ALL SELECT 4, id FROM user_tag WHERE tag_name = '高血压'
UNION ALL SELECT 5, id FROM user_tag WHERE tag_name = '潜在客户';

-- 2) 服务人员标签关系：让「服务人员」标签列不再显示 —
DELETE FROM service_worker_tag_relation;
INSERT INTO service_worker_tag_relation (worker_id, tag_id)
SELECT 1, id FROM service_worker_tag WHERE tag_name = '经验丰富'
UNION ALL SELECT 1, id FROM service_worker_tag WHERE tag_name = '持证上岗'
UNION ALL SELECT 2, id FROM service_worker_tag WHERE tag_name = '专业理疗'
UNION ALL SELECT 2, id FROM service_worker_tag WHERE tag_name = '针灸推拿'
UNION ALL SELECT 3, id FROM service_worker_tag WHERE tag_name = '经验丰富'
UNION ALL SELECT 3, id FROM service_worker_tag WHERE tag_name = '耐心细致'
UNION ALL SELECT 4, id FROM service_worker_tag WHERE tag_name = '专业理疗'
UNION ALL SELECT 4, id FROM service_worker_tag WHERE tag_name = '中医调理'
UNION ALL SELECT 5, id FROM service_worker_tag WHERE tag_name = '专业医师'
UNION ALL SELECT 5, id FROM service_worker_tag WHERE tag_name = '经验丰富';

-- 3) 服务人员补充「加入方式 / 头像」
UPDATE service_worker SET join_type = '服务端注册', avatar = 'https://i.pravatar.cc/150?img=11' WHERE id = 1;
UPDATE service_worker SET join_type = '服务端注册', avatar = 'https://i.pravatar.cc/150?img=12' WHERE id = 2;
UPDATE service_worker SET join_type = 'APP注册',     avatar = 'https://i.pravatar.cc/150?img=13' WHERE id = 3;
UPDATE service_worker SET join_type = '服务端注册', avatar = 'https://i.pravatar.cc/150?img=14' WHERE id = 4;
UPDATE service_worker SET join_type = 'APP注册',     avatar = 'https://i.pravatar.cc/150?img=15' WHERE id = 5;

-- 4) 商品补充「最后更新人」
UPDATE product SET update_by = 'admin' WHERE update_by IS NULL OR update_by = '';

-- 5) 动态种子数据（原库无动态，列表为空）
INSERT INTO dynamic (user_id, user_name, title, content, status, view_count, like_count, topic, user_phone, collect_count, share_count, comment_count, create_time) VALUES
(1, '李明华', '今天和老伴去公园散步，空气真好', '分享一下今天的快乐时光，老年生活也可以很精彩。', 1, 320, 45, '日常分享', '13800138001', 28, 12, 0, '2026-07-10 09:12:00'),
(2, '王秀兰', '推荐一道适合高血压患者的家常菜', '芹菜炒百合，清淡爽口，对控制血压很有帮助。', 1, 210, 33, '健康饮食', '13800138002', 41, 18, 0, '2026-07-11 14:30:00'),
(3, '张德福', '我的康复训练打卡第30天', '坚持康复运动一个月，身体明显有劲了，分享给老伙计们。', 1, 156, 22, '康复训练', '13800138003', 19, 7, 0, '2026-07-12 08:45:00'),
(4, '刘淑珍', '社区义诊活动很贴心', '今天社区来了医生免费测血压，服务很到位，点赞。', 0, 0, 0, '社区活动', '13800138004', 0, 0, 0, '2026-07-12 16:20:00');

-- 6) 活动种子数据（原库无活动，列表为空）。type 用真实枚举值
INSERT INTO activity (name, type, location, description, image_url, start_time, end_time, status, participant_count, update_by, create_time) VALUES
('夏季健康养生讲座', '健康服务', '朝阳区老年活动中心', '邀请三甲医院专家为老年人讲解夏季养生知识，现场提供免费体检。', 'https://picsum.photos/seed/health1/400/300', '2026-07-20 09:00:00', '2026-07-20 11:30:00', 1, 86, 'admin', '2026-07-05 10:00:00'),
('重阳节敬老文艺汇演', '节日活动', '海淀区文化广场', '由社区老年人自编自演的文艺节目，欢庆重阳佳节。', 'https://picsum.photos/seed/festival1/400/300', '2026-10-15 14:00:00', '2026-10-15 16:30:00', 1, 120, 'admin', '2026-07-06 11:00:00'),
('智能手机使用培训', '技能培训', '西城区社区服务中心', '帮助老年人掌握智能手机基本操作，跨越数字鸿沟。', 'https://picsum.photos/seed/skill1/400/300', '2026-07-25 09:30:00', '2026-07-25 11:00:00', 1, 54, 'admin', '2026-07-07 15:00:00'),
('书法兴趣社团招募', '兴趣社团', '东城区文化活动室', '每周二下午开展书法交流活动，欢迎爱好书法的老年朋友加入。', 'https://picsum.photos/seed/club1/400/300', '2026-07-18 14:00:00', '2026-12-31 17:00:00', 1, 33, 'admin', '2026-07-08 09:00:00'),
('老年人防诈骗公益讲座', '公益讲座', '丰台区老年大学', '结合真实案例讲解常见诈骗手法，提升老年人防范意识。', 'https://picsum.photos/seed/lecture1/400/300', '2026-08-01 09:00:00', '2026-08-01 11:00:00', 0, 0, 'admin', '2026-07-09 10:00:00');

SELECT 'P4-3 data layer fill done' AS result;
