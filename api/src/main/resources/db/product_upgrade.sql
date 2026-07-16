-- 商品分类表结构优化
ALTER TABLE product_category 
ADD COLUMN service_type VARCHAR(50) COMMENT '服务类型：家政护理/康复理疗/上门体检' AFTER name,
ADD COLUMN icon VARCHAR(255) COMMENT '图标' AFTER sort,
ADD COLUMN description VARCHAR(500) COMMENT '描述' AFTER icon,
ADD COLUMN update_time DATETIME COMMENT '更新时间' AFTER create_time;

-- 商品表结构优化
ALTER TABLE product
ADD COLUMN code VARCHAR(100) COMMENT '商品编码' AFTER name,
ADD COLUMN service_type VARCHAR(50) COMMENT '服务类型' AFTER category_name,
ADD COLUMN original_price DECIMAL(10,2) COMMENT '原价' AFTER price,
ADD COLUMN sales INT DEFAULT 0 COMMENT '销量' AFTER stock,
ADD COLUMN images TEXT COMMENT '图片列表JSON' AFTER image,
ADD COLUMN detail TEXT COMMENT '详情' AFTER description,
ADD COLUMN unit VARCHAR(20) COMMENT '单位' AFTER detail,
ADD COLUMN recommend TINYINT DEFAULT 0 COMMENT '是否推荐' AFTER status,
ADD COLUMN sort INT DEFAULT 0 COMMENT '排序' AFTER recommend;

-- 清空并重新插入商品分类数据
TRUNCATE TABLE product_category;

-- 家政护理分类
INSERT INTO product_category (name, service_type, sort, status, description, create_time) VALUES
('生活照料', '家政护理', 1, 1, '日常生活起居照料服务', NOW()),
('临床护理', '家政护理', 2, 1, '专业临床护理服务', NOW()),
('康复护理', '家政护理', 3, 1, '康复训练护理服务', NOW()),
('心理关怀', '家政护理', 4, 1, '心理疏导关怀服务', NOW()),
('上门做饭', '家政护理', 5, 1, '上门做饭送餐服务', NOW()),
('健康管理', '家政护理', 6, 1, '健康管理咨询服务', NOW()),
('陪同就医', '家政护理', 7, 1, '陪同就医陪诊服务', NOW()),
('日常清洁', '家政护理', 8, 1, '家庭清洁卫生服务', NOW());

-- 康复理疗分类
INSERT INTO product_category (name, service_type, sort, status, description, create_time) VALUES
('中医理疗', '康复理疗', 1, 1, '中医推拿针灸理疗', NOW()),
('康复训练', '康复理疗', 2, 1, '康复运动训练指导', NOW()),
('物理治疗', '康复理疗', 3, 1, '物理因子治疗服务', NOW()),
('作业治疗', '康复理疗', 4, 1, '日常生活能力训练', NOW());

-- 上门体检分类
INSERT INTO product_category (name, service_type, sort, status, description, create_time) VALUES
('常规体检', '上门体检', 1, 1, '基础健康体检项目', NOW()),
('慢性病筛查', '上门体检', 2, 1, '慢性疾病专项筛查', NOW()),
('专项检查', '上门体检', 3, 1, '专项医学检查服务', NOW()),
('健康评估', '上门体检', 4, 1, '健康状态综合评估', NOW());

-- 更新现有商品的分类和服务类型
UPDATE product SET service_type = '家政护理' WHERE id IN (1, 2, 3, 4);
UPDATE product SET category_id = 1 WHERE id = 1;
UPDATE product SET category_id = 5 WHERE id = 2;
UPDATE product SET category_id = 6 WHERE id = 3;
UPDATE product SET category_id = 7 WHERE id = 4;
