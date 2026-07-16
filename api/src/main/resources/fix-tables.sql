USE elderly_care;

ALTER TABLE health_record ADD COLUMN elder_name VARCHAR(100) COMMENT 'elder name' AFTER elder_id;

ALTER TABLE video ADD COLUMN category VARCHAR(100) COMMENT 'category' AFTER title;
ALTER TABLE video ADD COLUMN description TEXT COMMENT 'description' AFTER duration;
ALTER TABLE video ADD COLUMN like_count INT DEFAULT 0 COMMENT 'like count' AFTER view_count;
ALTER TABLE video CHANGE COLUMN video_url url VARCHAR(500) COMMENT 'url';

ALTER TABLE recipe ADD COLUMN category VARCHAR(100) COMMENT 'category' AFTER title;
ALTER TABLE recipe ADD COLUMN image VARCHAR(500) COMMENT 'image' AFTER category;
ALTER TABLE recipe ADD COLUMN steps TEXT COMMENT 'steps' AFTER ingredients;
ALTER TABLE recipe ADD COLUMN nutrition TEXT COMMENT 'nutrition' AFTER steps;
ALTER TABLE recipe ADD COLUMN protein DECIMAL(5,2) COMMENT 'protein' AFTER calories;
ALTER TABLE recipe ADD COLUMN fat DECIMAL(5,2) COMMENT 'fat' AFTER protein;
ALTER TABLE recipe ADD COLUMN carbs DECIMAL(5,2) COMMENT 'carbs' AFTER fat;
ALTER TABLE recipe ADD COLUMN remark VARCHAR(500) COMMENT 'remark' AFTER status;
ALTER TABLE recipe CHANGE COLUMN title name VARCHAR(200) COMMENT 'name';
ALTER TABLE recipe CHANGE COLUMN cover image VARCHAR(500) COMMENT 'image';

ALTER TABLE dynamic ADD COLUMN title VARCHAR(200) COMMENT 'title' AFTER user_name;
ALTER TABLE dynamic ADD COLUMN view_count INT DEFAULT 0 COMMENT 'view count' AFTER status;
ALTER TABLE dynamic ADD COLUMN like_count INT DEFAULT 0 COMMENT 'like count' AFTER view_count;
ALTER TABLE dynamic ADD COLUMN audit_remark VARCHAR(500) COMMENT 'audit remark' AFTER like_count;

ALTER TABLE disease ADD COLUMN symptoms TEXT COMMENT 'symptoms' AFTER type;
ALTER TABLE disease ADD COLUMN treatment TEXT COMMENT 'treatment' AFTER symptoms;
ALTER TABLE disease CHANGE COLUMN type category VARCHAR(50) COMMENT 'category';

ALTER TABLE institution ADD COLUMN code VARCHAR(50) COMMENT 'code' AFTER name;
ALTER TABLE institution ADD COLUMN contact VARCHAR(100) COMMENT 'contact' AFTER address;
ALTER TABLE institution ADD COLUMN email VARCHAR(100) COMMENT 'email' AFTER phone;
ALTER TABLE institution ADD COLUMN license VARCHAR(200) COMMENT 'license' AFTER email;
ALTER TABLE institution ADD COLUMN capacity INT COMMENT 'capacity' AFTER license;
ALTER TABLE institution ADD COLUMN staff_count INT COMMENT 'staff count' AFTER capacity;
ALTER TABLE institution ADD COLUMN rating DECIMAL(3,1) COMMENT 'rating' AFTER staff_count;
ALTER TABLE institution ADD COLUMN description TEXT COMMENT 'description' AFTER rating;

INSERT INTO dynamic (user_id, user_name, title, content, images, status, view_count, like_count) VALUES
(1, '李明华', '今天天气真好', '今天天气很不错，出去散步了一圈，感觉精神好多了！', '', 1, 120, 15),
(2, '王秀兰', '康复训练日记', '今天进行了康复训练，感觉身体状况有所改善，继续加油！', '', 1, 89, 23),
(3, '张德福', '分享养生心得', '每天坚持早睡早起，饮食清淡，身体越来越好了。', '', 1, 203, 45);

INSERT INTO recipe (name, category, image, ingredients, steps, nutrition, calories, protein, fat, carbs, status) VALUES
('清蒸鱼', '家常菜', '', '鱼、姜片、葱段、料酒、盐', '1.鱼处理干净；2.姜片铺底；3.蒸锅蒸10分钟；4.淋上热油', '富含蛋白质，低脂肪', 120, 20.5, 3.2, 2.1, 1),
('蔬菜沙拉', '健康餐', '', '生菜、番茄、黄瓜、沙拉酱', '1.蔬菜洗净切块；2.加入沙拉酱拌匀', '低脂高纤维', 85, 3.5, 4.2, 6.8, 1),
('小米粥', '主食', '', '小米、水', '1.小米淘洗干净；2.加水煮20分钟；3.焖10分钟', '易消化，养胃', 105, 2.8, 1.2, 21.6, 1);

INSERT INTO video (title, category, cover, url, duration, description, view_count, like_count, status) VALUES
('健康养生讲座', '养生', '', 'video_url_1', '01:23:45', '专业医生讲解健康养生知识', 520, 67, 1),
('康复训练教程', '康复', '', 'video_url_2', '00:45:30', '康复训练专业指导', 380, 45, 1),
('老年保健操', '运动', '', 'video_url_3', '00:30:00', '适合老年人的保健操', 890, 120, 1);

INSERT INTO disease (name, category, symptoms, treatment, precautions, status) VALUES
('高血压', '慢性病', '头痛、头晕、心悸、失眠', '药物治疗、饮食控制、适量运动', '低盐饮食、定期监测血压', 1),
('糖尿病', '慢性病', '多饮、多食、多尿、体重减轻', '药物治疗、胰岛素、饮食控制', '控制糖分摄入、定期监测血糖', 1),
('关节炎', '骨科', '关节疼痛、肿胀、僵硬', '药物治疗、物理治疗、康复训练', '避免过度运动、注意保暖', 1);

INSERT INTO institution (name, code, type, address, contact, phone, email, license, capacity, staff_count, rating, description, status) VALUES
('北京幸福养老院', 'BJXF001', '养老院', '北京市朝阳区幸福路100号', '王院长', '010-12345678', 'contact@bjxf.com', '京民养证字第001号', 200, 50, 4.8, '环境优美，服务周到，专业护理团队', 1),
('上海康乐护理中心', 'SHKL001', '护理中心', '上海市浦东新区康乐路88号', '李主任', '021-87654321', 'contact@shkl.com', '沪民养证字第002号', 150, 35, 4.6, '专业康复护理，医疗设备齐全', 1);

INSERT INTO activity (title, description, image, start_time, end_time, status) VALUES
('端午节活动', '组织老人包粽子、举办文艺表演', '', '2026-06-20 09:00:00', '2026-06-22 17:00:00', 1),
('健康体检', '免费为老人进行健康检查', '', '2026-07-15 08:00:00', '2026-07-15 17:00:00', 1);

INSERT INTO banner (title, image, link, sort, status) VALUES
('健康养生月', '', '/operation/activity', 1, 1),
('新用户优惠', '', '/user/coupon', 2, 1),
('服务升级', '', '/service/worker', 3, 1);

INSERT INTO medicine (name, category, dosage, indication, contraindication, status) VALUES
('阿司匹林肠溶片', '心血管', '每日1片', '用于预防心肌梗死、脑梗死', '对阿司匹林过敏者禁用', 1),
('二甲双胍', '降糖药', '每日2次，每次1片', '用于治疗2型糖尿病', '肾功能不全者禁用', 1),
('布洛芬缓释胶囊', '止痛药', '每日2次，每次1粒', '用于缓解轻至中度疼痛', '对布洛芬过敏者禁用', 1);

INSERT INTO coupon_record (coupon_id, user_id, status) VALUES
(1, 1, 0),
(2, 2, 0),
(3, 3, 0);

INSERT INTO app_message (title, content, type, status) VALUES
('系统维护通知', '系统将于今晚22:00-00:00进行维护，请提前保存数据。', 'system', 1),
('健康提醒', '请按时服药，注意休息。', 'health', 1);

INSERT INTO staff (name, phone, department, position, status) VALUES
('张三', '13800138101', '行政部', '主管', 1),
('李四', '13800138102', '护理部', '护士长', 1),
('王五', '13800138103', '财务部', '会计', 1);

UPDATE health_record SET elder_name = (SELECT name FROM elder WHERE elder.id = health_record.elder_id);

INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2);

INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, id FROM sys_menu;
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 2, id FROM sys_menu;