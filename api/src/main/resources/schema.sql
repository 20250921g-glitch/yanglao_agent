-- ============================================
-- 智慧养老后台管理系统 - 数据库建表脚本
-- 数据库: elderly_care
-- ============================================

USE elderly_care;

-- 1. 系统用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像',
    status TINYINT DEFAULT 1 COMMENT '状态 1启用 0禁用',
    dept_id BIGINT COMMENT '部门ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 2. 系统角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_key VARCHAR(50) NOT NULL COMMENT '角色标识',
    description VARCHAR(255) COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '状态 1启用 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_role_key (role_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- 3. 系统菜单表
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    path VARCHAR(200) COMMENT '路由路径',
    component VARCHAR(200) COMMENT '组件路径',
    perms VARCHAR(100) COMMENT '权限标识',
    icon VARCHAR(50) COMMENT '图标',
    type TINYINT DEFAULT 1 COMMENT '类型 1菜单 2按钮',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统菜单表';

-- 4. 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 5. 角色菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_role_id (role_id),
    INDEX idx_menu_id (menu_id),
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 6. 老人档案表
CREATE TABLE IF NOT EXISTS elder (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender TINYINT COMMENT '性别 1男 2女',
    age INT COMMENT '年龄',
    id_card VARCHAR(18) COMMENT '身份证号',
    phone VARCHAR(20) COMMENT '联系电话',
    address VARCHAR(255) COMMENT '居住地址',
    health_level TINYINT DEFAULT 1 COMMENT '健康等级 1自理 2半失能 3失能',
    emergency_contact VARCHAR(50) COMMENT '紧急联系人',
    emergency_phone VARCHAR(20) COMMENT '紧急联系电话',
    avatar VARCHAR(255) COMMENT '头像',
    status TINYINT DEFAULT 1 COMMENT '状态 1正常 0离世/退出',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_name (name),
    INDEX idx_phone (phone),
    INDEX idx_health_level (health_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='老人档案表';

-- 5. 家属关联表
CREATE TABLE IF NOT EXISTS elder_family (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    elder_id BIGINT NOT NULL COMMENT '老人ID',
    app_user_id BIGINT COMMENT '关联家属用户ID(app_user, FAMILY角色)',
    family_name VARCHAR(50) COMMENT '家属姓名',
    relation VARCHAR(20) COMMENT '与老人关系',
    phone VARCHAR(20) COMMENT '联系电话',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_elder_id (elder_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家属关联表';

-- 6. 健康记录表
CREATE TABLE IF NOT EXISTS health_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    elder_id BIGINT NOT NULL COMMENT '老人ID',
    record_type VARCHAR(50) NOT NULL COMMENT '记录类型 血压/血糖/心率/体温',
    record_value VARCHAR(50) COMMENT '记录值',
    record_time DATETIME COMMENT '记录时间',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_elder_id (elder_id),
    INDEX idx_record_type (record_type),
    INDEX idx_record_time (record_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康记录表';

-- 7. 服务订单表
CREATE TABLE IF NOT EXISTS service_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号',
    elder_id BIGINT COMMENT '老人ID',
    elder_name VARCHAR(50) COMMENT '老人姓名',
    service_type VARCHAR(50) COMMENT '服务类型',
    service_name VARCHAR(100) COMMENT '服务名称',
    nurse_id BIGINT COMMENT '护理员ID',
    nurse_name VARCHAR(50) COMMENT '护理员姓名',
    status TINYINT DEFAULT 1 COMMENT '状态 1待支付 2已支付 3服务中 4已完成 5已取消',
    price DECIMAL(10,2) COMMENT '价格',
    appointment_time DATETIME COMMENT '预约时间',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_order_no (order_no),
    INDEX idx_elder_id (elder_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务订单表';

-- 8. 商品表
CREATE TABLE IF NOT EXISTS product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    category_id BIGINT COMMENT '分类ID',
    category_name VARCHAR(50) COMMENT '分类名称',
    price DECIMAL(10,2) COMMENT '价格',
    stock INT DEFAULT 0 COMMENT '库存',
    image VARCHAR(255) COMMENT '图片',
    description TEXT COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '状态 1上架 0下架',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_name (name),
    INDEX idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 9. 商品分类表
CREATE TABLE IF NOT EXISTS product_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 1启用 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 10. 商品订单表
CREATE TABLE IF NOT EXISTS product_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号',
    elder_id BIGINT COMMENT '购买老人ID',
    elder_name VARCHAR(50) COMMENT '老人姓名',
    address VARCHAR(255) COMMENT '收货地址',
    contact_name VARCHAR(50) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    total_price DECIMAL(10,2) COMMENT '总金额',
    status TINYINT DEFAULT 1 COMMENT '状态 1待支付 2已支付 3已发货 4已完成 5已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_order_no (order_no),
    INDEX idx_elder_id (elder_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品订单表';

-- 11. 商品订单明细表
CREATE TABLE IF NOT EXISTS product_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT COMMENT '商品ID',
    product_name VARCHAR(100) COMMENT '商品名称',
    price DECIMAL(10,2) COMMENT '单价',
    quantity INT DEFAULT 1 COMMENT '数量',
    subtotal DECIMAL(10,2) COMMENT '小计',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品订单明细表';

-- 12. 测评表
CREATE TABLE IF NOT EXISTS evaluation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) COMMENT '测评标题',
    type VARCHAR(50) COMMENT '测评类型',
    content TEXT COMMENT '测评内容',
    score INT COMMENT '评分',
    elder_id BIGINT COMMENT '老人ID',
    elder_name VARCHAR(50) COMMENT '老人姓名',
    evaluator VARCHAR(100) COMMENT '评估人',
    conclusion TEXT COMMENT '结论',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_elder_id (elder_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测评表';

-- 13. 食物营养表
CREATE TABLE IF NOT EXISTS food (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '食物名称',
    category VARCHAR(50) COMMENT '分类',
    calories INT COMMENT '热量(千卡)',
    protein DECIMAL(5,2) COMMENT '蛋白质(克)',
    fat DECIMAL(5,2) COMMENT '脂肪(克)',
    carbohydrate DECIMAL(5,2) COMMENT '碳水化合物(克)',
    status TINYINT DEFAULT 1 COMMENT '状态 1启用 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_name (name),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食物营养表';

-- ============================================
-- 初始化数据
-- ============================================

-- 插入默认管理员 (密码: 123456, BCrypt加密)
INSERT INTO sys_user (username, password, real_name, phone, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', '13800138000', 1),
('operator', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '运营人员', '13800138001', 1);

-- 插入角色
INSERT INTO sys_role (role_name, role_key, description, status) VALUES
('超级管理员', 'SUPER_ADMIN', '拥有所有权限', 1),
('运营人员', 'OPERATOR', '日常运营管理', 1);

-- 插入菜单
INSERT INTO sys_menu (parent_id, name, path, component, icon, type, sort) VALUES
(0, '工作台', 'dashboard', '/dashboard/index', 'el-icon-monitor', 1, 1),
(0, '健康管理', '', '', 'el-icon-user', 1, 2),
(2, '老人档案', 'elder', '/health/elder/index', '', 1, 0),
(2, '家属管理', 'family', '/health/family/index', '', 1, 0),
(2, '健康记录', 'health-record', '/health/record/index', '', 1, 0),
(0, '服务管理', '', '', 'el-icon-suitcase', 1, 3),
(5, '服务订单', 'service-order', '/service/order/index', '', 1, 0),
(0, '商品管理', 'product', '/product/index', 'el-icon-goods', 1, 4),
(0, '交易管理', '', '', 'el-icon-tickets', 1, 5),
(8, '商品订单', 'product-order', '/trade/product-order/index', '', 1, 0),
(0, '运营管理', '', '', 'el-icon-data-analysis', 1, 6),
(10, '测评管理', 'evaluation', '/operation/evaluation/index', '', 1, 0),
(10, '食物管理', 'food', '/operation/food/index', '', 1, 0),
(0, '系统管理', '', '', 'el-icon-setting', 1, 99),
(13, '用户管理', 'sys-user', '/system/user/index', '', 1, 0),
(13, '角色管理', 'sys-role', '/system/role/index', '', 1, 0),
(13, '菜单管理', 'sys-menu', '/system/menu/index', '', 1, 0);

-- 插入商品分类
INSERT INTO product_category (name, sort, status) VALUES
('保健品', 1, 1),
('康复器材', 2, 1),
('护理用品', 3, 1),
('营养食品', 4, 1);

-- 插入商品示例
INSERT INTO product (name, category_id, category_name, price, stock, status) VALUES
('钙片', 1, '保健品', 68.00, 100, 1),
('轮椅', 2, '康复器材', 899.00, 20, 1),
('纸尿裤', 3, '护理用品', 128.00, 500, 1),
('蛋白粉', 4, '营养食品', 198.00, 80, 1);

-- 插入食物示例
INSERT INTO food (name, category, calories, protein, fat, carbohydrate, status) VALUES
('鸡蛋', '蛋白质类', 144, 13.3, 9.0, 1.2, 1),
('米饭', '主食类', 116, 2.6, 0.3, 25.9, 1),
('白菜', '蔬菜类', 17, 1.5, 0.1, 3.2, 1),
('苹果', '水果类', 52, 0.3, 0.2, 13.7, 1);

-- 机构表
CREATE TABLE IF NOT EXISTS institution (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) COMMENT '机构名称',
    code VARCHAR(50) COMMENT '机构编码',
    type VARCHAR(50) COMMENT '机构类型',
    address VARCHAR(500) COMMENT '地址',
    contact VARCHAR(100) COMMENT '联系人',
    phone VARCHAR(20) COMMENT '电话',
    email VARCHAR(100) COMMENT '邮箱',
    license VARCHAR(200) COMMENT '执照编号',
    capacity INT COMMENT '床位容量',
    staff_count INT COMMENT '员工数',
    rating DECIMAL(3,1) COMMENT '评分',
    description TEXT COMMENT '简介',
    status TINYINT DEFAULT 1 COMMENT '状态 1启用 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='养老机构';

INSERT INTO institution (name, code, type, address, contact, phone, email, license, capacity, staff_count, rating, description, status) VALUES
('阳光康泰养老中心', 'YL-001', '综合养老院', '北京市朝阳区安立路 88 号', '王院长', '010-88886666', 'yangguang@example.com', '京民证字第 1101052026001 号', 200, 45, 4.5, '集生活照料、康复护理、医疗照护于一体的综合性养老服务机构。', 1),
('爱龄家护理院', 'YL-002', '护理院', '北京市海淀区复兴路 66 号', '李主任', '010-66668888', 'ailing@example.com', '京卫证字第 1101082026002 号', 120, 32, 4.2, '专注老年慢病管理和术后康复护理。', 1);

-- 邻里圈评论表
CREATE TABLE IF NOT EXISTS dynamic_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dynamic_id BIGINT COMMENT '动态ID',
    user_id BIGINT COMMENT '评论用户ID',
    user_name VARCHAR(50) COMMENT '评论用户姓名',
    content TEXT COMMENT '评论内容',
    status TINYINT DEFAULT 1 COMMENT '状态 1正常 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_dynamic (dynamic_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邻里圈评论';

INSERT INTO dynamic_comment (dynamic_id, user_id, user_name, content, status) VALUES
(1, 1, '王阿姨', '说得太好了，点赞支持！', 1),
(1, 2, '李叔叔', '这个活动我也想参加。', 1),
(1, 4, '赵奶奶', '感谢分享，很实用。', 1);
