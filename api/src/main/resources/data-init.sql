USE elderly_care;

DROP TABLE IF EXISTS product_order_item;
DROP TABLE IF EXISTS product_order;
DROP TABLE IF EXISTS service_order;
DROP TABLE IF EXISTS health_record;
DROP TABLE IF EXISTS elder_family;
DROP TABLE IF EXISTS elder;
DROP TABLE IF EXISTS food;
DROP TABLE IF EXISTS evaluation;
DROP TABLE IF EXISTS product_category;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS sys_role_menu;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_menu;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;

DROP TABLE IF EXISTS service_worker_tag_relation;
DROP TABLE IF EXISTS service_worker_tag;
DROP TABLE IF EXISTS service_worker;
DROP TABLE IF EXISTS audit_record;
DROP TABLE IF EXISTS tip_record;
DROP TABLE IF EXISTS service_order_setting;
DROP TABLE IF EXISTS commission_record;
DROP TABLE IF EXISTS refund;
DROP TABLE IF EXISTS refund_record;
DROP TABLE IF EXISTS refund_reason;
DROP TABLE IF EXISTS order_evaluation;
DROP TABLE IF EXISTS withdrawal;
DROP TABLE IF EXISTS transaction_record;
DROP TABLE IF EXISTS app_user;
DROP TABLE IF EXISTS user_tag_relation;
DROP TABLE IF EXISTS user_tag;
DROP TABLE IF EXISTS member_level;
DROP TABLE IF EXISTS coupon_record;
DROP TABLE IF EXISTS coupon;
DROP TABLE IF EXISTS points_record;
DROP TABLE IF EXISTS app_message;
DROP TABLE IF EXISTS staff;
DROP TABLE IF EXISTS institution;
DROP TABLE IF EXISTS activity;
DROP TABLE IF EXISTS banner;
DROP TABLE IF EXISTS video;
DROP TABLE IF EXISTS recipe;
DROP TABLE IF EXISTS dynamic;
DROP TABLE IF EXISTS disease;
DROP TABLE IF EXISTS medicine;
DROP TABLE IF EXISTS product_param;
DROP TABLE IF EXISTS product_setting;

CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT 'username',
    password VARCHAR(200) NOT NULL COMMENT 'password',
    real_name VARCHAR(100) COMMENT 'real name',
    phone VARCHAR(20) COMMENT 'phone',
    avatar VARCHAR(255) COMMENT 'avatar',
    status TINYINT DEFAULT 1 COMMENT 'status 1=on 0=off',
    dept_id BIGINT COMMENT 'dept id',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='system user';

CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL COMMENT 'role name',
    role_key VARCHAR(50) NOT NULL COMMENT 'role key',
    description VARCHAR(255) COMMENT 'description',
    status TINYINT DEFAULT 1 COMMENT 'status 1=on 0=off',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_role_key (role_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='system role';

CREATE TABLE sys_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT 0 COMMENT 'parent id',
    name VARCHAR(100) NOT NULL COMMENT 'menu name',
    path VARCHAR(200) COMMENT 'route path',
    component VARCHAR(200) COMMENT 'component path',
    perms VARCHAR(100) COMMENT 'permission',
    icon VARCHAR(100) COMMENT 'icon',
    type TINYINT DEFAULT 1 COMMENT 'type 1=menu 2=button',
    sort INT DEFAULT 0 COMMENT 'sort',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='system menu';

CREATE TABLE sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT 'user id',
    role_id BIGINT NOT NULL COMMENT 'role id',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='user role';

CREATE TABLE sys_role_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT 'role id',
    menu_id BIGINT NOT NULL COMMENT 'menu id',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_role_id (role_id),
    INDEX idx_menu_id (menu_id),
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='role menu';

CREATE TABLE elder (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT 'name',
    gender TINYINT COMMENT 'gender 1=男 2=女',
    `age` INT COMMENT 'age',
    id_card VARCHAR(50) COMMENT 'id card',
    phone VARCHAR(20) COMMENT 'phone',
    address VARCHAR(255) COMMENT 'address',
    health_level TINYINT DEFAULT 1 COMMENT 'health level 1=self 2=partial 3=disabled',
    emergency_contact VARCHAR(100) COMMENT 'emergency contact',
    emergency_phone VARCHAR(20) COMMENT 'emergency phone',
    avatar VARCHAR(255) COMMENT 'avatar',
    status TINYINT DEFAULT 1 COMMENT 'status 1=normal 0=left',
    app_user_id BIGINT COMMENT '关联的普通用户ID(C端健康档案归属)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_name (name),
    INDEX idx_phone (phone),
    INDEX idx_health_level (health_level),
    INDEX idx_app_user (app_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='elder';

CREATE TABLE elder_family (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    elder_id BIGINT NOT NULL COMMENT 'elder id',
    app_user_id BIGINT COMMENT '关联家属用户ID(app_user, FAMILY角色)',
    family_name VARCHAR(100) COMMENT 'family name',
    relation VARCHAR(50) COMMENT 'relation',
    phone VARCHAR(20) COMMENT 'phone',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_elder_id (elder_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='elder family';

CREATE TABLE health_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    elder_id BIGINT NOT NULL COMMENT 'elder id',
    elder_name VARCHAR(100) COMMENT 'elder name',
    record_type VARCHAR(50) NOT NULL COMMENT 'record type',
    record_value VARCHAR(100) COMMENT 'record value',
    record_time DATETIME COMMENT 'record time',
    remark VARCHAR(500) COMMENT 'remark',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_elder_id (elder_id),
    INDEX idx_record_type (record_type),
    INDEX idx_record_time (record_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='health record';

CREATE TABLE service_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT 'order no',
    elder_id BIGINT COMMENT 'elder id',
    elder_name VARCHAR(100) COMMENT 'elder name',
    service_type VARCHAR(50) COMMENT 'service type',
    service_name VARCHAR(200) COMMENT 'service name',
    nurse_id BIGINT COMMENT 'nurse id',
    nurse_name VARCHAR(100) COMMENT 'nurse name',
    status TINYINT DEFAULT 1 COMMENT 'status 1=pending 2=paid 3=serving 4=done 5=cancelled',
    price DECIMAL(10,2) COMMENT 'price',
    appointment_time DATETIME COMMENT 'appointment time',
    remark VARCHAR(500) COMMENT 'remark',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_order_no (order_no),
    INDEX idx_elder_id (elder_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='service order';

CREATE TABLE product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL COMMENT 'product name',
    code VARCHAR(50) COMMENT 'product code',
    category_id BIGINT COMMENT 'category id',
    category_name VARCHAR(100) COMMENT 'category name',
    service_type VARCHAR(50) COMMENT 'service type',
    price DECIMAL(10,2) COMMENT 'price',
    original_price DECIMAL(10,2) COMMENT 'original price',
    stock INT DEFAULT 0 COMMENT 'stock',
    sales INT DEFAULT 0 COMMENT 'sales',
    image VARCHAR(500) COMMENT 'image',
    images TEXT COMMENT 'images JSON',
    description TEXT COMMENT 'description',
    detail TEXT COMMENT 'detail',
    unit VARCHAR(20) COMMENT 'unit',
    status TINYINT DEFAULT 1 COMMENT 'status 1=on 0=off',
    recommend TINYINT DEFAULT 0 COMMENT 'recommend 1=yes 0=no',
    sort INT DEFAULT 0 COMMENT 'sort',
    update_by VARCHAR(100) DEFAULT '' COMMENT '最后更新人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_name (name),
    INDEX idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='product';

CREATE TABLE product_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT 'category name',
    parent_id BIGINT DEFAULT 0 COMMENT 'parent id',
    service_type VARCHAR(50) COMMENT 'service type',
    sort INT DEFAULT 0 COMMENT 'sort',
    status TINYINT DEFAULT 1 COMMENT 'status 1=on 0=off',
    icon VARCHAR(100) COMMENT 'icon',
    description VARCHAR(500) COMMENT 'description',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='product category';

CREATE TABLE product_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT 'order no',
    elder_id BIGINT COMMENT 'elder id',
    elder_name VARCHAR(100) COMMENT 'elder name',
    address VARCHAR(500) COMMENT 'address',
    contact_name VARCHAR(100) COMMENT 'contact name',
    contact_phone VARCHAR(20) COMMENT 'contact phone',
    total_price DECIMAL(10,2) COMMENT 'total price',
    status TINYINT DEFAULT 1 COMMENT 'status 1=pending 2=paid 3=shipped 4=done 5=cancelled',
    user_id BIGINT COMMENT '下单用户ID(app_user)',
    goods_amount DECIMAL(10,2) COMMENT '商品总价',
    discount_price DECIMAL(10,2) COMMENT '优惠金额',
    freight DECIMAL(10,2) COMMENT '运费',
    payable_price DECIMAL(10,2) COMMENT '应付款',
    pay_type VARCHAR(50) COMMENT '支付方式',
    pay_time DATETIME COMMENT '支付时间',
    order_source VARCHAR(50) COMMENT '订单来源',
    remark VARCHAR(500) COMMENT '备注',
    product_name VARCHAR(255) COMMENT '首商品名',
    product_image VARCHAR(500) COMMENT '首商品图',
    product_count INT COMMENT '商品件数',
    worker_id BIGINT COMMENT '派单服务人员ID',
    worker_name VARCHAR(50) COMMENT '派单服务人员姓名',
    appointment_time DATETIME COMMENT '预约上门时间',
    dispatch_time DATETIME COMMENT '派单时间',
    dispatch_user VARCHAR(50) COMMENT '派单人',
    service_order_no VARCHAR(50) COMMENT '关联工单编号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_order_no (order_no),
    INDEX idx_elder_id (elder_id),
    INDEX idx_status (status),
    INDEX idx_user (user_id),
    INDEX idx_worker (worker_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='product order';

CREATE TABLE product_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT 'order id',
    product_id BIGINT COMMENT 'product id',
    product_name VARCHAR(200) COMMENT 'product name',
    price DECIMAL(10,2) COMMENT 'price',
    quantity INT DEFAULT 1 COMMENT 'quantity',
    subtotal DECIMAL(10,2) COMMENT 'subtotal',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='product order item';

CREATE TABLE evaluation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) COMMENT 'title',
    type VARCHAR(50) COMMENT 'type',
    content TEXT COMMENT 'content',
    score INT COMMENT 'score',
    elder_id BIGINT COMMENT 'elder id',
    elder_name VARCHAR(100) COMMENT 'elder name',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_elder_id (elder_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='evaluation';

CREATE TABLE food (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL COMMENT 'food name',
    category VARCHAR(100) COMMENT 'category',
    calories INT COMMENT 'calories',
    protein DECIMAL(5,2) COMMENT 'protein',
    fat DECIMAL(5,2) COMMENT 'fat',
    carbohydrate DECIMAL(5,2) COMMENT 'carbohydrate',
    status TINYINT DEFAULT 1 COMMENT 'status 1=on 0=off',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_name (name),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='food';

CREATE TABLE service_worker (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) COMMENT 'name',
    phone VARCHAR(20) COMMENT 'phone',
    avatar VARCHAR(255) COMMENT 'avatar',
    gender TINYINT COMMENT 'gender 0=女 1=男',
    id_card VARCHAR(50) COMMENT 'id card',
    bank_card VARCHAR(50) COMMENT 'bank card',
    bank_name VARCHAR(100) COMMENT 'bank name',
    service_type VARCHAR(50) COMMENT 'service type',
    region VARCHAR(200) COMMENT 'region',
    intro TEXT COMMENT 'intro',
    certificate VARCHAR(500) COMMENT 'certificate',
    join_type VARCHAR(50) COMMENT 'join type',
    audit_status TINYINT DEFAULT 0 COMMENT 'audit status 0=pending 1=passed 2=rejected',
    audit_remark VARCHAR(500) COMMENT 'audit remark',
    allow_tip TINYINT DEFAULT 1 COMMENT 'allow tip 1=yes 0=no',
    status TINYINT DEFAULT 1 COMMENT 'status 1=normal 0=disabled',
    last_login_time DATETIME COMMENT 'last login time',
    deleted TINYINT DEFAULT 0 COMMENT 'deleted',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_phone (phone),
    INDEX idx_service_type (service_type),
    INDEX idx_audit_status (audit_status),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='service worker';

CREATE TABLE service_worker_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tag_name VARCHAR(100) COMMENT 'tag name',
    service_type VARCHAR(50) COMMENT 'service type',
    color VARCHAR(20) COMMENT 'color',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_service_type (service_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='service worker tag';

CREATE TABLE service_worker_tag_relation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    worker_id BIGINT COMMENT 'worker id',
    tag_id BIGINT COMMENT 'tag id',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_worker_id (worker_id),
    INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='service worker tag relation';

CREATE TABLE audit_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    worker_id BIGINT COMMENT 'worker id',
    audit_type VARCHAR(50) COMMENT 'audit type',
    status TINYINT DEFAULT 0 COMMENT 'status 0=pending 1=passed 2=rejected',
    reject_reason VARCHAR(500) COMMENT 'reject reason',
    auditor_id BIGINT COMMENT 'auditor id',
    auditor_name VARCHAR(100) COMMENT 'auditor name',
    audit_time DATETIME COMMENT 'audit time',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_worker_id (worker_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='audit record';

CREATE TABLE tip_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    worker_id BIGINT COMMENT 'worker id',
    worker_name VARCHAR(100) COMMENT 'worker name',
    user_id BIGINT COMMENT 'user id',
    order_id BIGINT COMMENT 'order id',
    amount DECIMAL(10,2) COMMENT 'amount',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_worker_id (worker_id),
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='tip record';

CREATE TABLE service_order_setting (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    service_type VARCHAR(50) COMMENT 'service type',
    commission_rate DECIMAL(5,2) COMMENT 'commission rate',
    min_withdraw DECIMAL(10,2) COMMENT 'min withdraw',
    remark VARCHAR(500) COMMENT 'remark',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_service_type (service_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='service order setting';

CREATE TABLE commission_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT COMMENT 'order id',
    order_no VARCHAR(50) COMMENT 'order no',
    worker_id BIGINT COMMENT 'worker id',
    worker_name VARCHAR(100) COMMENT 'worker name',
    order_amount DECIMAL(10,2) COMMENT 'order amount',
    commission_rate DECIMAL(5,2) COMMENT 'commission rate',
    commission_amount DECIMAL(10,2) COMMENT 'commission amount',
    status TINYINT DEFAULT 0 COMMENT 'status 0=pending 1=settled',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    settle_time DATETIME COMMENT 'settle time',
    INDEX idx_order_id (order_id),
    INDEX idx_worker_id (worker_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='commission record';

CREATE TABLE refund (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    refund_no VARCHAR(50) UNIQUE COMMENT 'refund no',
    order_id BIGINT COMMENT 'order id',
    order_no VARCHAR(50) COMMENT 'order no',
    user_id BIGINT COMMENT 'user id',
    user_name VARCHAR(100) COMMENT 'user name',
    user_phone VARCHAR(20) COMMENT 'user phone',
    refund_amount DECIMAL(10,2) COMMENT 'refund amount',
    actual_amount DECIMAL(10,2) COMMENT 'actual amount',
    refund_reason VARCHAR(500) COMMENT 'refund reason',
    refund_description TEXT COMMENT 'refund description',
    refund_type VARCHAR(50) COMMENT 'refund type',
    status TINYINT DEFAULT 0 COMMENT 'status',
    handle_time DATETIME COMMENT 'handle time',
    handle_remark VARCHAR(500) COMMENT 'handle remark',
    handler_id BIGINT COMMENT 'handler id',
    handler_name VARCHAR(100) COMMENT 'handler name',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='refund';

CREATE TABLE refund_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    refund_id BIGINT COMMENT 'refund id',
    order_id BIGINT COMMENT 'order id',
    amount DECIMAL(10,2) COMMENT 'amount',
    status TINYINT COMMENT 'status',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_refund_id (refund_id),
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='refund record';

CREATE TABLE refund_reason (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reason VARCHAR(200) COMMENT 'reason',
    sort INT DEFAULT 0 COMMENT 'sort',
    status TINYINT DEFAULT 1 COMMENT 'status',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_sort (sort),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='refund reason';

CREATE TABLE order_evaluation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT COMMENT 'order id',
    order_no VARCHAR(50) COMMENT 'order no',
    user_id BIGINT COMMENT 'user id',
    user_name VARCHAR(100) COMMENT 'user name',
    worker_id BIGINT COMMENT 'worker id',
    worker_name VARCHAR(100) COMMENT 'worker name',
    score INT COMMENT 'score',
    content TEXT COMMENT 'content',
    reply TEXT COMMENT 'reply',
    reply_time DATETIME COMMENT 'reply time',
    status TINYINT DEFAULT 1 COMMENT 'status',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='order evaluation';

CREATE TABLE withdrawal (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    withdraw_no VARCHAR(50) UNIQUE COMMENT 'withdraw no',
    worker_id BIGINT COMMENT 'worker id',
    worker_name VARCHAR(100) COMMENT 'worker name',
    amount DECIMAL(10,2) COMMENT 'amount',
    withdraw_type VARCHAR(50) COMMENT 'withdraw type',
    bank_card VARCHAR(50) COMMENT 'bank card',
    bank_name VARCHAR(100) COMMENT 'bank name',
    status TINYINT DEFAULT 0 COMMENT 'status 0=pending 1=approved 2=completed 3=rejected',
    reject_reason VARCHAR(500) COMMENT 'reject reason',
    audit_time DATETIME COMMENT 'audit time',
    audit_remark VARCHAR(500) COMMENT 'audit remark',
    auditor_id BIGINT COMMENT 'auditor id',
    auditor_name VARCHAR(100) COMMENT 'auditor name',
    complete_time DATETIME COMMENT 'complete time',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_worker_id (worker_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='withdrawal';

CREATE TABLE transaction_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT 'user id',
    user_name VARCHAR(100) COMMENT 'user name',
    type VARCHAR(50) COMMENT 'type',
    category VARCHAR(50) COMMENT 'category',
    amount DECIMAL(10,2) COMMENT 'amount',
    balance DECIMAL(10,2) COMMENT 'balance',
    order_no VARCHAR(50) COMMENT 'order no',
    remark VARCHAR(500) COMMENT 'remark',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='transaction record';

CREATE TABLE app_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    phone VARCHAR(20) UNIQUE COMMENT 'phone',
    nickname VARCHAR(100) COMMENT 'nickname',
    avatar VARCHAR(255) COMMENT 'avatar',
    password VARCHAR(200) COMMENT 'password',
    real_name VARCHAR(100) COMMENT 'real name',
    gender TINYINT COMMENT 'gender:0 female 1 male',
    birth_date DATE COMMENT 'birth date',
    id_card VARCHAR(18) COMMENT 'id card no',
    nation VARCHAR(50) COMMENT 'nation',
    native_place VARCHAR(100) COMMENT 'native place',
    marriage VARCHAR(20) COMMENT 'marriage status',
    education VARCHAR(50) COMMENT 'education',
    occupation VARCHAR(100) COMMENT 'occupation',
    work_unit VARCHAR(100) COMMENT 'work unit',
    height DECIMAL(10,2) COMMENT 'height cm',
    weight DECIMAL(10,2) COMMENT 'weight kg',
    address VARCHAR(255) COMMENT 'address',
    emergency_contact VARCHAR(50) COMMENT 'emergency contact',
    emergency_phone VARCHAR(20) COMMENT 'emergency phone',
    bio VARCHAR(500) COMMENT 'bio',
    source VARCHAR(50) COMMENT 'register source: APP注册/后台创建',
    role VARCHAR(20) DEFAULT 'FAMILY' COMMENT 'user type: ELDER/FAMILY/VOLUNTEER/STAFF',
    status TINYINT DEFAULT 1 COMMENT 'status',
    level_id BIGINT COMMENT 'level id',
    points INT DEFAULT 0 COMMENT 'points',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_phone (phone),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='app user';

CREATE TABLE user_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tag_name VARCHAR(100) COMMENT 'tag name',
    tag_type VARCHAR(50) COMMENT 'tag type: customer type/health tag',
    color VARCHAR(50) COMMENT 'color',
    status TINYINT DEFAULT 1 COMMENT 'status',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tag_name (tag_name),
    INDEX idx_tag_type (tag_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='user tag';

CREATE TABLE user_tag_relation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT 'user id',
    tag_id BIGINT COMMENT 'tag id',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='user tag relation';

CREATE TABLE member_level (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    level_name VARCHAR(100) COMMENT 'level name',
    min_score INT COMMENT 'min score',
    discount DECIMAL(3,2) COMMENT 'discount',
    status TINYINT DEFAULT 1 COMMENT 'status',
    sort INT DEFAULT 0 COMMENT 'sort',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    level_icon VARCHAR(255) COMMENT 'level icon',
    max_score INT COMMENT 'max score',
    benefits VARCHAR(500) COMMENT 'benefits',
    INDEX idx_sort (sort),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='member level';

CREATE TABLE coupon (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) COMMENT 'name',
    type TINYINT DEFAULT 0 COMMENT 'type 0=fixed 1=percent',
    denomination DECIMAL(10,2) COMMENT 'denomination',
    min_amount DECIMAL(10,2) COMMENT 'min amount',
    total_count INT COMMENT 'total count',
    remain_count INT COMMENT 'remain count',
    used_count INT DEFAULT 0 COMMENT 'used count',
    start_time DATETIME COMMENT 'start time',
    end_time DATETIME COMMENT 'end time',
    status TINYINT DEFAULT 1 COMMENT 'status',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='coupon';

CREATE TABLE coupon_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    coupon_id BIGINT COMMENT 'coupon id',
    user_id BIGINT COMMENT 'user id',
    status TINYINT DEFAULT 0 COMMENT 'status 0=unused 1=used 2=expired',
    use_time DATETIME COMMENT 'use time',
    order_no VARCHAR(50) COMMENT 'order no',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    order_id BIGINT COMMENT 'order id',
    receive_time DATETIME COMMENT 'receive time',
    INDEX idx_coupon_id (coupon_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='coupon record';

CREATE TABLE points_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT 'user id',
    user_name VARCHAR(100) COMMENT 'user name',
    type VARCHAR(50) COMMENT 'type',
    amount INT COMMENT 'amount',
    balance INT COMMENT 'balance',
    source VARCHAR(200) COMMENT 'source',
    remark VARCHAR(500) COMMENT 'remark',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='points record';

CREATE TABLE app_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) COMMENT 'title',
    content TEXT COMMENT 'content',
    type VARCHAR(50) COMMENT 'type',
    status TINYINT DEFAULT 1 COMMENT 'status',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    target VARCHAR(50) COMMENT 'target',
    send_time DATETIME COMMENT 'send time',
    INDEX idx_type (type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='app message';

CREATE TABLE staff (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) COMMENT 'name',
    staff_no VARCHAR(50) COMMENT 'staff no',
    phone VARCHAR(20) COMMENT 'phone',
    avatar VARCHAR(255) COMMENT 'avatar',
    dept VARCHAR(100) COMMENT 'department',
    position VARCHAR(100) COMMENT 'position',
    status TINYINT DEFAULT 1 COMMENT 'status',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    email VARCHAR(100) COMMENT 'email',
    id_card VARCHAR(50) COMMENT 'id card',
    gender VARCHAR(10) COMMENT 'gender',
    entry_date DATETIME COMMENT 'entry date',
    remark VARCHAR(500) COMMENT 'remark',
    password VARCHAR(255) COMMENT 'password',
    INDEX idx_name (name),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='staff';

CREATE TABLE institution (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) COMMENT 'name',
    code VARCHAR(50) COMMENT 'code',
    type VARCHAR(50) COMMENT 'type',
    address VARCHAR(500) COMMENT 'address',
    contact VARCHAR(100) COMMENT 'contact',
    phone VARCHAR(20) COMMENT 'phone',
    email VARCHAR(100) COMMENT 'email',
    license VARCHAR(200) COMMENT 'license',
    capacity INT COMMENT 'capacity',
    staff_count INT COMMENT 'staff count',
    rating DECIMAL(3,1) COMMENT 'rating',
    description TEXT COMMENT 'description',
    status TINYINT DEFAULT 1 COMMENT 'status',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='institution';

CREATE TABLE activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) COMMENT 'name',
    type VARCHAR(50) COMMENT 'type',
    location VARCHAR(200) COMMENT 'activity location',
    description TEXT COMMENT 'description',
    image_url VARCHAR(500) COMMENT 'image url',
    start_time DATETIME COMMENT 'start time',
    end_time DATETIME COMMENT 'end time',
    status TINYINT DEFAULT 0 COMMENT 'status',
    participant_count INT DEFAULT 0 COMMENT 'participant count',
    update_by VARCHAR(100) DEFAULT '' COMMENT '最后更新人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='activity';

CREATE TABLE banner (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) COMMENT 'title',
    image_url VARCHAR(500) COMMENT 'image url',
    link_url VARCHAR(500) COMMENT 'link url',
    position VARCHAR(50) COMMENT 'position',
    sort INT DEFAULT 0 COMMENT 'sort',
    status TINYINT DEFAULT 1 COMMENT 'status',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_sort (sort),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='banner';

CREATE TABLE video (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) COMMENT 'title',
    category VARCHAR(100) COMMENT 'category',
    cover VARCHAR(500) COMMENT 'cover',
    url VARCHAR(500) COMMENT 'url',
    duration VARCHAR(20) COMMENT 'duration',
    description TEXT COMMENT 'description',
    view_count INT DEFAULT 0 COMMENT 'view count',
    like_count INT DEFAULT 0 COMMENT 'like count',
    status TINYINT DEFAULT 1 COMMENT 'status',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='video';

CREATE TABLE recipe (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) COMMENT 'name',
    category VARCHAR(100) COMMENT 'category',
    image VARCHAR(500) COMMENT 'image',
    ingredients TEXT COMMENT 'ingredients',
    steps TEXT COMMENT 'steps',
    nutrition TEXT COMMENT 'nutrition',
    calories INT COMMENT 'calories',
    protein DECIMAL(5,2) COMMENT 'protein',
    fat DECIMAL(5,2) COMMENT 'fat',
    carbs DECIMAL(5,2) COMMENT 'carbs',
    status TINYINT DEFAULT 1 COMMENT 'status',
    remark VARCHAR(500) COMMENT 'remark',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='recipe';

CREATE TABLE dynamic (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT 'user id',
    user_name VARCHAR(100) COMMENT 'user name',
    title VARCHAR(200) COMMENT 'title',
    content TEXT COMMENT 'content',
    images TEXT COMMENT 'images',
    status TINYINT DEFAULT 0 COMMENT 'status 0=pending 1=approved 2=rejected',
    view_count INT DEFAULT 0 COMMENT 'view count',
    like_count INT DEFAULT 0 COMMENT 'like count',
    audit_remark VARCHAR(500) COMMENT 'audit remark',
    topic VARCHAR(200) DEFAULT NULL COMMENT '话题',
    user_phone VARCHAR(20) DEFAULT NULL COMMENT '发布人手机号',
    collect_count INT DEFAULT 0 COMMENT '收藏数',
    share_count INT DEFAULT 0 COMMENT '分享数',
    comment_count INT DEFAULT 0 COMMENT '评论数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='dynamic';

CREATE TABLE disease (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) COMMENT 'name',
    category VARCHAR(50) COMMENT 'category',
    symptoms TEXT COMMENT 'symptoms',
    treatment TEXT COMMENT 'treatment',
    precautions TEXT COMMENT 'precautions',
    status TINYINT DEFAULT 1 COMMENT 'status',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='disease';

CREATE TABLE medicine (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) COMMENT 'name',
    category VARCHAR(100) COMMENT 'category',
    dosage VARCHAR(200) COMMENT 'dosage',
    indication TEXT COMMENT 'indication',
    contraindication TEXT COMMENT 'contraindication',
    status TINYINT DEFAULT 1 COMMENT 'status',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='medicine';

INSERT INTO sys_user (username, password, real_name, phone, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin', '13800138000', 1),
('operator', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'operator', '13800138001', 1);

INSERT INTO sys_role (role_name, role_key, description, status) VALUES
('超级管理员', 'SUPER_ADMIN', '系统超级管理员，拥有所有权限', 1),
('运营管理员', 'OPERATOR', '运营管理员，负责日常运营管理', 1);

INSERT INTO sys_menu (parent_id, name, path, component, icon, type, sort) VALUES
(0, '工作台', '/dashboard', 'dashboard/index', 'Monitor', 1, 1),
(0, '健康管理', '', '', 'User', 1, 2),
(2, '老人档案', '/health/elder', 'health/elder', '', 1, 0),
(2, '家属管理', '/health/family', 'health/family', '', 1, 1),
(2, '健康记录', '/health/record', 'health/record', '', 1, 2),
(2, '健康数据', '/health/health-data', 'health/health-data', '', 1, 3),
(2, '疾病管理', '/health/disease', 'health/disease', '', 1, 4),
(0, '服务管理', '', '', 'Suitcase', 1, 3),
(7, '服务人员', '/service/worker', 'service/worker', '', 1, 0),
(7, '服务人员标签', '/service/worker-tag', 'service/worker-tag', '', 1, 1),
(7, '审核管理', '/service/audit', 'service/audit', '', 1, 2),
(7, '工单管理', '/service/order', 'service/order', '', 1, 3),
(7, '佣金记录', '/service/commission', 'service/commission', '', 1, 4),
(7, '打赏记录', '/service/tip', 'service/tip', '', 1, 5),
(7, '工单设置', '/service/work-setting', 'service/work-setting', '', 1, 6),
(0, '商品管理', '/product', 'product/index', 'Goods', 1, 4),
(14, '商品分类', '/product/category', 'product/category', '', 1, 0),
(14, '参数管理', '/product/params', 'product/params', '', 1, 1),
(14, '通用设置', '/product/settings', 'product/settings', '', 1, 2),
(0, '交易管理', '', '', 'Tickets', 1, 5),
(18, '商品订单', '/trade/product-order', 'trade/product-order', '', 1, 0),
(18, '售后管理', '/trade/refund', 'trade/refund', '', 1, 1),
(18, '评价管理', '/trade/order-evaluation', 'trade/order-evaluation', '', 1, 2),
(18, '提现记录', '/trade/withdrawal', 'trade/withdrawal', '', 1, 3),
(18, '退款原因', '/trade/refund-reason', 'trade/refund-reason', '', 1, 4),
(18, '支付配置', '/trade/payment-config', 'trade/payment-config', '', 1, 5),
(18, '配送管理', '/trade/delivery', 'trade/delivery', '', 1, 6),
(18, '财务明细', '/trade/finance', 'trade/finance', '', 1, 7),
(0, '运营管理', '', '', 'DataAnalysis', 1, 6),
(26, '测评管理', '/operation/evaluation', 'operation/evaluation', '', 1, 0),
(26, '食物管理', '/operation/food', 'operation/food', '', 1, 1),
(26, '话题管理', '/operation/topic', 'operation/topic', '', 1, 2),
(26, '生活圈', '/operation/community', 'operation/community', '', 1, 3),
(26, '动态管理', '/operation/dynamic', 'operation/dynamic', '', 1, 4),
(26, '食谱管理', '/operation/recipe', 'operation/recipe', '', 1, 5),
(26, '视频管理', '/operation/video', 'operation/video', '', 1, 6),
(26, '评论管理', '/operation/comment', 'operation/comment', '', 1, 7),
(26, '标签管理', '/operation/tag', 'operation/tag', '', 1, 8),
(26, '举报管理', '/operation/report', 'operation/report', '', 1, 9),
(26, '内容管理', '/operation/content', 'operation/content', '', 1, 10),
(26, '留言管理', '/operation/feedback', 'operation/feedback', '', 1, 11),
(26, '搜索记录', '/operation/search-log', 'operation/search-log', '', 1, 12),
(26, '活动管理', '/operation/activity', 'operation/activity', '', 1, 13),
(26, '轮播图', '/operation/banner', 'operation/banner', '', 1, 14),
(26, '消息通知', '/operation/notification', 'operation/notification', '', 1, 15),
(26, '意见反馈', '/operation/opinion', 'operation/opinion', '', 1, 16),
(26, '常见问题', '/operation/faq', 'operation/faq', '', 1, 17),
(26, '帮助中心', '/operation/help', 'operation/help', '', 1, 18),
(26, '药品管理', '/operation/medicine', 'operation/medicine', '', 1, 19),
(0, '用户管理', '', '', 'User', 1, 7),
(46, '用户列表', '/user/list', 'user/list', '', 1, 0),
(46, '报告管理', '/user/report', 'user/report', '', 1, 1),
(46, '用户标签', '/user/tag', 'user/tag', '', 1, 2),
(46, '会员等级', '/user/level', 'user/level', '', 1, 3),
(46, '优惠券', '/user/coupon', 'user/coupon', '', 1, 4),
(46, '积分管理', '/user/points', 'user/points', '', 1, 5),
(46, '消息群发', '/user/message', 'user/message', '', 1, 6),
(0, '数据分析', '', '', 'TrendCharts', 1, 8),
(53, '用户分析', '/data/user-analysis', 'data/user-analysis', '', 1, 0),
(53, '年龄分析', '/data/user-age-analysis', 'data/user-age-analysis', '', 1, 1),
(53, '性别分析', '/data/user-gender-analysis', 'data/user-gender-analysis', '', 1, 2),
(53, '复购分析', '/data/repurchase-analysis', 'data/repurchase-analysis', '', 1, 3),
(53, '评价统计', '/data/evaluation-analysis', 'data/evaluation-analysis', '', 1, 4),
(53, '交易分析', '/data/trade-analysis', 'data/trade-analysis', '', 1, 5),
(53, '服务分析', '/data/service-analysis', 'data/service-analysis', '', 1, 6),
(53, '商品分析', '/data/product-analysis', 'data/product-analysis', '', 1, 7),
(53, '营收分析', '/data/revenue', 'data/revenue', '', 1, 8),
(53, '业绩统计', '/data/performance', 'data/performance', '', 1, 9),
(53, '数据导出', '/data/export', 'data/export', '', 1, 10),
(53, '数据大屏', '/data/dashboard', 'data/dashboard', '', 1, 11),
(53, '埋点管理', '/data/tracking', 'data/tracking', '', 1, 12),
(53, '数据字典', '/data/dict', 'data/dict', '', 1, 13),
(53, '操作统计', '/data/action-log', 'data/action-log', '', 1, 14),
(0, '系统管理', '', '', 'Setting', 1, 99),
(64, '员工管理', '/system/staff', 'system/staff', '', 1, 0),
(64, '后台用户', '/system/user', 'system/user', '', 1, 1),
(64, '角色管理', '/system/role', 'system/role', '', 1, 2),
(64, '菜单管理', '/system/menu', 'system/menu', '', 1, 3),
(64, '个人中心', '/system/profile', 'system/profile', '', 1, 4),
(64, '操作日志', '/system/operation-log', 'system/operation-log', '', 1, 5),
(64, '协议管理', '/system/agreement', 'system/agreement', '', 1, 6),
(64, '短信配置', '/system/sms-config', 'system/sms-config', '', 1, 7),
(64, '系统配置', '/system/system-config', 'system/system-config', '', 1, 8),
(64, '关于系统', '/system/about', 'system/about', '', 1, 9),
(64, '机构管理', '/system/institution', 'system/institution', '', 1, 10);

INSERT INTO product_category (name, service_type, sort, status) VALUES
('保健用品', '康复理疗', 1, 1),
('康复器材', '康复理疗', 2, 1),
('护理用品', '家政护理', 3, 1),
('营养食品', '上门体检', 4, 1);

CREATE TABLE product_param (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '参数名',
    value VARCHAR(500) COMMENT '参数值',
    service_type VARCHAR(50) COMMENT '所属服务类型',
    category VARCHAR(100) COMMENT '所属分类',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_service_type (service_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品参数表';

CREATE TABLE product_setting (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    setting_key VARCHAR(100) NOT NULL COMMENT '配置键',
    setting_value VARCHAR(1000) COMMENT '配置值',
    description VARCHAR(500) COMMENT '配置描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX uk_setting_key (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品配置表';

INSERT INTO product_param (name, value, service_type, category, status) VALUES
('服务时长', '2小时', '家政护理', '家政清洁', 1),
('服务时长', '3小时', '家政护理', '深度清洁', 1),
('适用人群', '自理老人/半自理', '康复理疗', '康复护理', 1),
('是否含耗材', '含耗材', '康复理疗', '康复护理', 1),
('服务时长', '45分钟', '上门体检', '常规体检', 1),
('体检项目数', '12项', '上门体检', '全面体检', 1),
('是否含耗材', '不含耗材', '家政护理', '日常保洁', 0),
('适用人群', '全年龄段', '上门体检', '常规体检', 1),
('服务时段', '8:00-18:00', '家政护理', '日常保洁', 1),
('康复方案', '个性化定制', '康复理疗', '中医理疗', 1),
('报告形式', '电子报告+纸质', '上门体检', '全面体检', 1),
('服务人数', '1对1', '康复理疗', '运动康复', 1);

INSERT INTO product_setting (setting_key, setting_value, description) VALUES
('defaultStatus', '0', '默认商品状态：0待审核 1已上架 2已下架'),
('saleMode', '线上预约', '默认销售模式：线上预约/即时购买/混合模式'),
('autoDelistDays', '30', '自动下架天数'),
('needAudit', '1', '是否需要审核：0否 1是'),
('auditor', '管理员', '审核人'),
('lowStockThreshold', '10', '低库存预警阈值'),
('allowOversell', '0', '是否允许超卖：0否 1是'),
('afterSaleDays', '7', '默认售后期限（天）'),
('allowReturn', '1', '七天无理由退货：0否 1是');

INSERT INTO product (name, code, category_id, category_name, service_type, price, original_price, stock, sales, unit, status, recommend, sort) VALUES
('钙片', 'BJYP001', 1, '保健用品', '康复理疗', 68.00, 88.00, 100, 156, '瓶', 1, 1, 1),
('轮椅', 'KFQC001', 2, '康复器材', '康复理疗', 899.00, 1299.00, 20, 32, '台', 1, 1, 2),
('纸尿裤', 'HLYP001', 3, '护理用品', '家政护理', 128.00, 158.00, 500, 420, '包', 1, 1, 3),
('蛋白粉', 'YYSP001', 4, '营养食品', '上门体检', 198.00, 258.00, 80, 89, '罐', 1, 1, 4),
('血压计', 'KFQC002', 2, '康复器材', '康复理疗', 299.00, 399.00, 50, 67, '台', 1, 0, 5),
('血糖仪', 'KFQC003', 2, '康复器材', '康复理疗', 458.00, 598.00, 30, 45, '台', 1, 1, 6),
('维生素C', 'BJYP002', 1, '保健用品', '康复理疗', 35.00, 45.00, 200, 234, '瓶', 1, 0, 7),
('护理床', 'KFQC004', 2, '康复器材', '康复理疗', 2599.00, 3299.00, 10, 18, '张', 1, 1, 8),
('成人湿巾', 'HLYP002', 3, '护理用品', '家政护理', 45.00, 58.00, 300, 189, '包', 1, 0, 9),
('益生菌', 'YYSP002', 4, '营养食品', '上门体检', 168.00, 198.00, 60, 78, '盒', 1, 1, 10);

INSERT INTO food (name, category, calories, protein, fat, carbohydrate, status) VALUES
('鸡蛋', '蛋白质', 144, 13.3, 9.0, 1.2, 1),
('米饭', '主食', 116, 2.6, 0.3, 25.9, 1),
('白菜', '蔬菜', 17, 1.5, 0.1, 3.2, 1),
('苹果', '水果', 52, 0.3, 0.2, 13.7, 1),
('牛奶', '蛋白质', 54, 3.2, 3.2, 3.4, 1),
('豆腐', '蛋白质', 76, 8.1, 3.7, 4.2, 1),
('西红柿', '蔬菜', 18, 0.9, 0.2, 4.0, 1),
('香蕉', '水果', 91, 1.4, 0.2, 22.0, 1),
('瘦肉', '蛋白质', 143, 20.3, 6.2, 1.5, 1),
('黄瓜', '蔬菜', 15, 0.8, 0.2, 2.9, 1);

INSERT INTO elder (name, gender, age, id_card, phone, address, emergency_contact, emergency_phone, status) VALUES
('李明华', 1, 78, '110101194805123456', '13800138001', '北京市朝阳区幸福小区3号楼201室', '李晓', '13900139001', 1),
('王秀兰', 2, 82, '110102194403156789', '13800138002', '北京市海淀区阳光花园5号楼102室', '王芳', '13900139002', 1),
('张德福', 1, 75, '110103195108234567', '13800138003', '北京市西城区永安里小区2号楼301室', '张伟', '13900139003', 1),
('刘淑珍', 2, 85, '110104194102189012', '13800138004', '北京市东城区和平里小区8号楼402室', '刘洋', '13900139004', 1),
('陈建国', 1, 72, '110105195411253456', '13800138005', '北京市丰台区晓月苑小区1号楼101室', '陈明', '13900139005', 1);

-- 家属关联数据见文件末尾(app_user 插入之后)，通过子查询关联真实 FAMILY 账号

INSERT INTO health_record (elder_id, record_type, record_value, record_time, remark) VALUES
(1, '血压', '135/85', '2026-07-01 08:00:00', '正常'),
(1, '血糖', '6.2', '2026-07-01 08:00:00', '正常'),
(1, '血压', '138/88', '2026-07-05 08:00:00', '偏高'),
(1, '血糖', '6.5', '2026-07-05 08:00:00', '正常'),
(2, '血压', '142/90', '2026-07-02 08:00:00', '偏高'),
(2, '血糖', '7.1', '2026-07-02 08:00:00', '偏高'),
(2, '血压', '140/88', '2026-07-06 08:00:00', '偏高'),
(2, '血糖', '6.9', '2026-07-06 08:00:00', '偏高'),
(3, '血压', '128/78', '2026-07-03 08:00:00', '正常'),
(3, '血糖', '5.8', '2026-07-03 08:00:00', '正常'),
(4, '血压', '150/95', '2026-07-04 08:00:00', '高血压'),
(4, '血糖', '8.2', '2026-07-04 08:00:00', '糖尿病'),
(5, '血压', '130/80', '2026-07-07 08:00:00', '正常'),
(5, '血糖', '6.0', '2026-07-07 08:00:00', '正常');

INSERT INTO service_worker (name, phone, gender, id_card, service_type, region, audit_status, allow_tip, status) VALUES
('赵晓燕', '13700137001', 2, '110101199001056789', '家政护理', '朝阳区', 1, 1, 1),
('孙丽华', '13700137002', 2, '110102198803123456', '康复理疗', '海淀区', 1, 1, 1),
('周静', '13700137003', 2, '110103199205189012', '家政护理', '西城区', 1, 1, 1),
('吴强', '13700137004', 1, '110104198508253456', '康复理疗', '东城区', 1, 1, 1),
('郑芳', '13700137005', 2, '110105199111126789', '上门体检', '丰台区', 1, 1, 1);

INSERT INTO service_worker_tag (tag_name, service_type, color) VALUES
('经验丰富', '家政护理', '#00C4A1'),
('持证上岗', '家政护理', '#29B6F6'),
('耐心细致', '家政护理', '#66BB6A'),
('专业理疗', '康复理疗', '#00C4A1'),
('针灸推拿', '康复理疗', '#7E57C2'),
('中医调理', '康复理疗', '#FFA726'),
('专业医师', '上门体检', '#00C4A1'),
('经验丰富', '上门体检', '#29B6F6');

INSERT INTO service_order (order_no, elder_id, elder_name, service_type, service_name, nurse_id, nurse_name, status, price, appointment_time, create_time) VALUES
('SO20260701001', 1, '李明华', '家政护理', '居家护理', 1, '赵晓燕', 3, 200.00, '2026-07-01 09:00:00', '2026-07-01 09:00:00'),
('SO20260702002', 2, '王秀兰', '康复理疗', '康复按摩', 2, '孙丽华', 2, 350.00, '2026-07-02 10:00:00', '2026-07-02 10:00:00'),
('SO20260703003', 3, '张德福', '家政护理', '居家护理', 3, '周静', 1, 200.00, '2026-07-03 08:30:00', '2026-07-03 08:30:00'),
('SO20260704004', 4, '刘淑珍', '康复理疗', '康复按摩', 4, '吴强', 3, 350.00, '2026-07-04 14:00:00', '2026-07-04 14:00:00'),
('SO20260705005', 5, '陈建国', '上门体检', '健康体检', 5, '郑芳', 2, 150.00, '2026-07-05 09:30:00', '2026-07-05 09:30:00'),
('SO20260706006', 1, '李明华', '康复理疗', '康复按摩', 2, '孙丽华', 1, 350.00, '2026-07-06 11:00:00', '2026-07-06 11:00:00'),
('SO20260707007', 2, '王秀兰', '家政护理', '居家护理', 1, '赵晓燕', 4, 200.00, '2026-07-07 10:00:00', '2026-07-07 10:00:00');

INSERT INTO product_order (order_no, elder_id, elder_name, address, contact_name, contact_phone, total_price, status, create_time) VALUES
('PO20260701001', 1, '李明华', '北京市朝阳区幸福小区3号楼201室', '李晓', '13900139001', 266.00, 4, '2026-07-01 15:00:00'),
('PO20260702002', 2, '王秀兰', '北京市海淀区阳光花园5号楼102室', '王芳', '13900139002', 899.00, 3, '2026-07-02 16:00:00'),
('PO20260703003', 3, '张德福', '北京市西城区永安里小区2号楼301室', '张伟', '13900139003', 326.00, 2, '2026-07-03 14:30:00'),
('PO20260704004', 4, '刘淑珍', '北京市东城区和平里小区8号楼402室', '刘洋', '13900139004', 128.00, 1, '2026-07-04 10:00:00');

INSERT INTO product_order_item (order_id, product_id, product_name, price, quantity, subtotal) VALUES
(1, 1, '钙片', 68.00, 2, 136.00),
(1, 4, '蛋白粉', 198.00, 1, 198.00),
(2, 2, '轮椅', 899.00, 1, 899.00),
(3, 1, '钙片', 68.00, 1, 68.00),
(3, 7, '维生素C', 35.00, 2, 70.00),
(3, 4, '蛋白粉', 198.00, 1, 198.00),
(4, 3, '纸尿裤', 128.00, 1, 128.00);

INSERT INTO evaluation (title, type, content, score, elder_id, elder_name, create_time) VALUES
('服务非常专业', 'service', '赵阿姨服务态度很好，做事认真负责，老人很满意', 5, 1, '李明华', '2026-07-02 10:00:00'),
('商品质量不错', 'product', '钙片效果很好，老人吃了感觉不错', 5, 1, '李明华', '2026-07-03 15:00:00'),
('康复理疗效果明显', 'service', '孙医生的康复理疗很专业，老人身体状况有改善', 5, 2, '王秀兰', '2026-07-03 11:00:00'),
('轮椅很实用', 'product', '轮椅质量很好，老人使用方便', 4, 2, '王秀兰', '2026-07-04 09:00:00'),
('服务及时', 'service', '周阿姨准时到达，服务周到', 5, 3, '张德福', '2026-07-04 09:30:00');

INSERT INTO service_order_setting (service_type, commission_rate, min_withdraw) VALUES
('家政护理', 0.20, 100.00),
('康复理疗', 0.25, 100.00),
('上门体检', 0.15, 100.00);

INSERT INTO commission_record (order_id, order_no, worker_id, worker_name, order_amount, commission_rate, commission_amount, status) VALUES
(1, 'SO20260701001', 1, '赵晓燕', 200.00, 0.20, 40.00, 1),
(2, 'SO20260702002', 2, '孙丽华', 350.00, 0.25, 87.50, 0),
(3, 'SO20260703003', 3, '周静', 200.00, 0.20, 40.00, 0),
(4, 'SO20260704004', 4, '吴强', 350.00, 0.25, 87.50, 1),
(5, 'SO20260705005', 5, '郑芳', 150.00, 0.15, 22.50, 0);

INSERT INTO tip_record (worker_id, worker_name, user_id, order_id, amount) VALUES
(1, '赵晓燕', 1, 1, 50.00),
(2, '孙丽华', 2, 2, 30.00),
(4, '吴强', 4, 4, 20.00);

INSERT INTO refund_reason (reason, sort, status) VALUES
('商品质量问题', 1, 1),
('商品与描述不符', 2, 1),
('收到商品损坏', 3, 1),
('不需要了/拍错了', 4, 1),
('物流太慢', 5, 1);

INSERT INTO order_evaluation (order_id, order_no, user_id, user_name, worker_id, worker_name, score, content) VALUES
(1, 'SO20260701001', 1, '李明华', 1, '赵晓燕', 5, '服务态度非常好，工作认真负责'),
(2, 'SO20260702002', 2, '王秀兰', 2, '孙丽华', 5, '康复按摩效果很好，身体舒服多了'),
(4, 'SO20260704004', 4, '刘淑珍', 4, '吴强', 4, '服务不错，希望继续保持');

INSERT INTO withdrawal (withdraw_no, worker_id, worker_name, amount, withdraw_type, bank_card, bank_name, status) VALUES
('WD20260701001', 1, '赵晓燕', 200.00, '银行卡', '622848000000001', '工商银行', 2),
('WD20260705002', 4, '吴强', 150.00, '银行卡', '622848000000002', '建设银行', 1),
('WD20260707003', 2, '孙丽华', 100.00, '银行卡', '622848000000003', '农业银行', 0);

INSERT INTO transaction_record (user_id, user_name, type, amount, balance, order_no) VALUES
(1, '李明华', '充值', 500.00, 500.00, ''),
(1, '李明华', '消费', 266.00, 234.00, 'PO20260701001'),
(2, '王秀兰', '充值', 1000.00, 1000.00, ''),
(2, '王秀兰', '消费', 899.00, 101.00, 'PO20260702002'),
(3, '张德福', '充值', 500.00, 500.00, ''),
(3, '张德福', '消费', 326.00, 174.00, 'PO20260703003');

INSERT INTO app_user (phone, nickname, status, level_id, points, role) VALUES
('13800138001', '李明华', 1, 1, 500, 'ELDER'),
('13800138002', '王秀兰', 1, 2, 1200, 'ELDER'),
('13800138003', '张德福', 1, 1, 300, 'ELDER'),
('13800138004', '刘淑珍', 1, 3, 2000, 'ELDER'),
('13800138005', '陈建国', 1, 1, 100, 'ELDER');

-- 演示家属账号(C端登录: 13800001234 / 123456)，角色为家属，仅关联李明华一位老人
INSERT INTO app_user (phone, nickname, password, status, level_id, points, role, source) VALUES
('13800001234', '演示用户', '$2b$10$220qQ.q4sU0MHyMSLyXVauF88aTJWsbs0X7t9kdkEPH7ZgKxCrAOm', 1, 1, 200, 'FAMILY', '后台创建');

-- 其他规范家属账号(演示用：姓名/电话与老人家属对应；密码留空，仅用于家属管理关联展示，保证"和用户对得上")
INSERT INTO app_user (phone, nickname, real_name, status, level_id, points, role) VALUES
('13900001111', '张阿姨', '张阿姨', 1, 1, 0, 'FAMILY'),
('13900002222', '王叔叔', '王叔叔', 1, 1, 0, 'FAMILY'),
('13900003333', '小李', '小李', 1, 1, 0, 'FAMILY'),
('13900004444', '周阿姨', '周阿姨', 1, 1, 0, 'FAMILY');

-- 老人健康档案归属(app_user_id)：李明华归演示家属账号；其余老人各归同手机号的老人账号(本人自管)，避免一个账号关联全部老人
UPDATE elder SET app_user_id = (SELECT id FROM app_user WHERE phone = '13800001234' LIMIT 1) WHERE name = '李明华';
UPDATE elder e JOIN app_user u ON e.phone = u.phone SET e.app_user_id = u.id WHERE e.name IN ('王秀兰', '张德福', '刘淑珍', '陈建国');

INSERT INTO user_tag (tag_name, tag_type, color, status) VALUES
('潜在客户', '客户类型', '#909399', 1),
('重点客户', '客户类型', '#F56C6C', 1),
('普通客户', '客户类型', '#67C23A', 1),
('多次消费客户', '客户类型', '#E6A23C', 1),
('高血压', '健康标签', '#F56C6C', 1),
('高血糖', '健康标签', '#E6A23C', 1),
('高血脂', '健康标签', '#F56C6C', 1),
('慢性病', '健康标签', '#909399', 1),
('冠心病', '健康标签', '#F56C6C', 1),
('运动疗法', '康复疗法', '#409EFF', 1),
('心理疗法', '康复疗法', '#409EFF', 1);

INSERT INTO member_level (level_name, min_score, discount, status, sort) VALUES
('普通会员', 0, 1.00, 1, 1),
('银卡会员', 500, 0.95, 1, 2),
('金卡会员', 2000, 0.90, 1, 3),
('钻石会员', 5000, 0.85, 1, 4);

INSERT INTO coupon (name, type, denomination, min_amount, total_count, used_count, start_time, end_time, status) VALUES
('新人专享券', 0, 50.00, 100.00, 100, 10, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1),
('满减优惠', 0, 30.00, 200.00, 200, 50, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1),
('8折优惠券', 1, 0.80, 100.00, 50, 15, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1);

INSERT INTO points_record (user_id, type, amount, balance, source) VALUES
(1, '签到', 10, 10, '每日签到'),
(1, '消费', 26, 36, '购物消费'),
(2, '签到', 10, 10, '每日签到'),
(2, '消费', 89, 99, '购物消费'),
(3, '签到', 10, 10, '每日签到'),
(3, '消费', 32, 42, '购物消费'),
(4, '签到', 10, 10, '每日签到'),
(4, '签到', 10, 20, '每日签到');

CREATE TABLE activity_registration (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    activity_name VARCHAR(200) COMMENT '活动名称',
    user_id BIGINT COMMENT '用户ID',
    user_name VARCHAR(100) COMMENT '用户姓名',
    phone VARCHAR(20) COMMENT '联系电话',
    status TINYINT DEFAULT 0 COMMENT '状态 0待审核 1已通过 2已拒绝',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_activity_id (activity_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动报名表';

CREATE TABLE health_news (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL COMMENT '资讯标题',
    summary VARCHAR(500) COMMENT '资讯摘要',
    content TEXT COMMENT '资讯内容',
    cover_image VARCHAR(500) COMMENT '封面图片',
    category VARCHAR(50) COMMENT '分类',
    view_count INT DEFAULT 0 COMMENT '浏览量',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康资讯表';

INSERT INTO activity_registration (activity_id, activity_name, user_id, user_name, phone, status, remark) VALUES
(1, '夏季健康讲座', 1, '李明华', '13800138001', 1, ''),
(1, '夏季健康讲座', 2, '王秀兰', '13800138002', 1, '希望能讲一些防暑降温的知识'),
(1, '夏季健康讲座', 3, '张德福', '13800138003', 0, ''),
(2, '康复训练体验营', 4, '刘淑珍', '13800138004', 1, ''),
(2, '康复训练体验营', 5, '陈建国', '13800138005', 2, '年龄不符合要求');

INSERT INTO health_news (title, summary, content, cover_image, category, view_count, status, sort) VALUES
('老年人夏季防暑指南', '夏季高温天气对老年人身体健康影响较大，本文为您介绍防暑降温的实用方法。', '<p>夏季是老年人健康的关键时期，高温天气容易引发中暑等问题。以下是一些防暑降温的实用建议：</p><p>1. 保持室内通风，避免中午时段外出</p><p>2. 多喝水，补充电解质</p><p>3. 穿着宽松透气的棉质衣物</p><p>4. 适当进行室内运动</p>', '', '健康科普', 520, 1, 1),
('高血压患者的日常护理', '高血压是老年人常见疾病，正确的日常护理对于控制血压至关重要。', '<p>高血压患者需要长期坚持治疗和护理，以下几点非常重要：</p><p>1. 按时服药，定期测量血压</p><p>2. 低盐饮食，控制体重</p><p>3. 保持心情舒畅，避免情绪波动</p><p>4. 适当运动，如散步、太极拳等</p>', '', '疾病护理', 380, 1, 2),
('老年人营养饮食建议', '合理的饮食是老年人健康的基础，本文介绍老年人饮食的注意事项。', '<p>老年人饮食应遵循以下原则：</p><p>1. 营养均衡，多样化搭配</p><p>2. 易于消化，少食多餐</p><p>3. 控制油盐糖摄入</p><p>4. 多吃蔬菜水果，补充膳食纤维</p>', '', '饮食健康', 450, 1, 3),
('如何预防骨质疏松', '骨质疏松是老年人常见问题，科学预防可以有效减少风险。', '<p>预防骨质疏松需要从多方面入手：</p><p>1. 补充钙质，多吃奶制品、豆制品</p><p>2. 补充维生素D，多晒太阳</p><p>3. 适当进行负重运动</p><p>4. 戒烟限酒</p>', '', '健康科普', 290, 1, 4),
('老年人睡眠质量提升技巧', '良好的睡眠对于老年人身心健康至关重要，本文介绍改善睡眠的方法。', '<p>提升睡眠质量的方法：</p><p>1. 保持规律的作息时间</p><p>2. 创造舒适的睡眠环境</p><p>3. 睡前避免刺激性活动</p><p>4. 适当进行日间活动</p>', '', '生活保健', 320, 1, 5);

-- ============================================================
-- P4-3 数据层补齐种子（重跑真相）：标签关系 / 动态 / 活动 / 更新人
-- ============================================================

-- 服务人员标签关系（按 service_type 精确匹配，避免"经验丰富"跨类型重复）
INSERT INTO service_worker_tag_relation (worker_id, tag_id)
SELECT 1, id FROM service_worker_tag WHERE tag_name = '经验丰富' AND service_type = '家政护理'
UNION ALL SELECT 1, id FROM service_worker_tag WHERE tag_name = '持证上岗' AND service_type = '家政护理'
UNION ALL SELECT 2, id FROM service_worker_tag WHERE tag_name = '专业理疗' AND service_type = '康复理疗'
UNION ALL SELECT 2, id FROM service_worker_tag WHERE tag_name = '针灸推拿' AND service_type = '康复理疗'
UNION ALL SELECT 3, id FROM service_worker_tag WHERE tag_name = '经验丰富' AND service_type = '家政护理'
UNION ALL SELECT 3, id FROM service_worker_tag WHERE tag_name = '耐心细致' AND service_type = '家政护理'
UNION ALL SELECT 4, id FROM service_worker_tag WHERE tag_name = '专业理疗' AND service_type = '康复理疗'
UNION ALL SELECT 4, id FROM service_worker_tag WHERE tag_name = '中医调理' AND service_type = '康复理疗'
UNION ALL SELECT 5, id FROM service_worker_tag WHERE tag_name = '专业医师' AND service_type = '上门体检'
UNION ALL SELECT 5, id FROM service_worker_tag WHERE tag_name = '经验丰富' AND service_type = '上门体检';

-- 用户标签关系（基于本脚本 user_tag 名称）
INSERT INTO user_tag_relation (user_id, tag_id)
SELECT 1, id FROM user_tag WHERE tag_name = '高血压'
UNION ALL SELECT 1, id FROM user_tag WHERE tag_name = '冠心病'
UNION ALL SELECT 2, id FROM user_tag WHERE tag_name = '高血糖'
UNION ALL SELECT 2, id FROM user_tag WHERE tag_name = '运动疗法'
UNION ALL SELECT 3, id FROM user_tag WHERE tag_name = '高血脂'
UNION ALL SELECT 4, id FROM user_tag WHERE tag_name = '慢性病'
UNION ALL SELECT 4, id FROM user_tag WHERE tag_name = '高血压'
UNION ALL SELECT 5, id FROM user_tag WHERE tag_name = '潜在客户';

-- 服务人员补充加入方式 / 头像
UPDATE service_worker SET join_type = '服务端注册', avatar = 'https://i.pravatar.cc/150?img=11' WHERE id = 1;
UPDATE service_worker SET join_type = '服务端注册', avatar = 'https://i.pravatar.cc/150?img=12' WHERE id = 2;
UPDATE service_worker SET join_type = 'APP注册',     avatar = 'https://i.pravatar.cc/150?img=13' WHERE id = 3;
UPDATE service_worker SET join_type = '服务端注册', avatar = 'https://i.pravatar.cc/150?img=14' WHERE id = 4;
UPDATE service_worker SET join_type = 'APP注册',     avatar = 'https://i.pravatar.cc/150?img=15' WHERE id = 5;

-- 商品补充最后更新人
UPDATE product SET update_by = 'admin' WHERE update_by IS NULL OR update_by = '';

-- 动态种子
INSERT INTO dynamic (user_id, user_name, title, content, status, view_count, like_count, topic, user_phone, collect_count, share_count, comment_count, create_time) VALUES
(1, '李明华', '今天和老伴去公园散步，空气真好', '分享一下今天的快乐时光，老年生活也可以很精彩。', 1, 320, 45, '日常分享', '13800138001', 28, 12, 0, '2026-07-10 09:12:00'),
(2, '王秀兰', '推荐一道适合高血压患者的家常菜', '芹菜炒百合，清淡爽口，对控制血压很有帮助。', 1, 210, 33, '健康饮食', '13800138002', 41, 18, 0, '2026-07-11 14:30:00'),
(3, '张德福', '我的康复训练打卡第30天', '坚持康复运动一个月，身体明显有劲了，分享给老伙计们。', 1, 156, 22, '康复训练', '13800138003', 19, 7, 0, '2026-07-12 08:45:00'),
(4, '刘淑珍', '社区义诊活动很贴心', '今天社区来了医生免费测血压，服务很到位，点赞。', 0, 0, 0, '社区活动', '13800138004', 0, 0, 0, '2026-07-12 16:20:00');

-- 活动种子（type 使用真实枚举值：节日活动/健康服务/技能培训/兴趣社团/公益讲座）
INSERT INTO activity (name, type, location, description, image_url, start_time, end_time, status, participant_count, update_by, create_time) VALUES
('夏季健康养生讲座', '健康服务', '朝阳区老年活动中心', '邀请三甲医院专家为老年人讲解夏季养生知识，现场提供免费体检。', 'https://picsum.photos/seed/health1/400/300', '2026-07-20 09:00:00', '2026-07-20 11:30:00', 1, 86, 'admin', '2026-07-05 10:00:00'),
('重阳节敬老文艺汇演', '节日活动', '海淀区文化广场', '由社区老年人自编自演的文艺节目，欢庆重阳佳节。', 'https://picsum.photos/seed/festival1/400/300', '2026-10-15 14:00:00', '2026-10-15 16:30:00', 1, 120, 'admin', '2026-07-06 11:00:00'),
('智能手机使用培训', '技能培训', '西城区社区服务中心', '帮助老年人掌握智能手机基本操作，跨越数字鸿沟。', 'https://picsum.photos/seed/skill1/400/300', '2026-07-25 09:30:00', '2026-07-25 11:00:00', 1, 54, 'admin', '2026-07-07 15:00:00'),
('书法兴趣社团招募', '兴趣社团', '东城区文化活动室', '每周二下午开展书法交流活动，欢迎爱好书法的老年朋友加入。', 'https://picsum.photos/seed/club1/400/300', '2026-07-18 14:00:00', '2026-12-31 17:00:00', 1, 33, 'admin', '2026-07-08 09:00:00'),
('老年人防诈骗公益讲座', '公益讲座', '丰台区老年大学', '结合真实案例讲解常见诈骗手法，提升老年人防范意识。', 'https://picsum.photos/seed/lecture1/400/300', '2026-08-01 09:00:00', '2026-08-01 11:00:00', 0, 0, 'admin', '2026-07-09 10:00:00');

-- ===== 以下为部署补齐时新增的表 (2026-07-16 schema drift fix) =====
-- 补齐 elderly_care_20250921 中代码引用但建库时缺失的 14 张表
-- 仅作用于自己的库，符合老师红线。所有列按对应实体类反推，保证与代码一致。

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


-- ===== 演示种子数据 (2026-07-16 schema drift 补齐后写入) =====
-- 重部署若库已存在数据，请先 TRUNCATE 对应表再执行本段。
INSERT INTO service_project (id, name, category, duration, price, method, status) VALUES
(1, '居家保洁服务', '生活照料', 120, 99.00, '上门服务', 1),
(2, '助餐配送服务', '生活照料', 60, 39.00, '上门服务', 1),
(3, '健康体检套餐', '健康管理', 180, 299.00, '到店服务', 1),
(4, '康复理疗', '健康管理', 90, 159.00, '上门服务', 1),
(5, '陪同就医', '生活照料', 240, 199.00, '陪同服务', 1),
(6, '智能穿戴设备租借', '智慧养老', 30, 49.00, '到店服务', 1);
INSERT INTO app_message (id, title, content, type, status, target, send_time) VALUES
(1, '系统通知', '欢迎使用智慧养老系统，您的健康档案已创建成功。', 'system', 1, 'all', '2026-07-16 09:00:00'),
(2, '活动提醒', '本周六上午9点将在社区活动中心举办健康讲座，欢迎报名参加。', 'activity', 1, 'all', '2026-07-16 10:00:00'),
(3, '服务提醒', '您预约的居家保洁服务将于明日上门，请保持电话畅通。', 'service', 1, 'all', '2026-07-16 11:00:00'),
(4, '健康建议', '根据您的健康档案，建议本周增加两次散步，注意低盐饮食。', 'health', 1, 'all', '2026-07-16 12:00:00');
INSERT INTO conversation (id, user_id, user_name, user_avatar, phone, last_message, unread_count, msg_count, status) VALUES
(1, 1, '张大爷', 'https://api.dicebear.com/7.x/initials/svg?seed=张', '13800001234', '感谢护理员的悉心照料', 2, 5, 1),
(2, 2, '李奶奶', 'https://api.dicebear.com/7.x/initials/svg?seed=李', '13800005678', '请问明天还能预约体检吗？', 1, 3, 1);
INSERT INTO conversation_message (conversation_id, sender_type, content) VALUES
(1, 1, '您好，我是您的专属护理员小王。'),
(1, 0, '谢谢你们，老人家很满意。'),
(1, 1, '不客气，这是我们应该做的，有需要随时联系。'),
(2, 0, '请问明天还能预约体检吗？'),
(2, 1, '可以的，我来帮您安排，稍后确认时间。');
INSERT INTO app_message_read (message_id, user_id, read_time, hidden, create_time) VALUES
(1, 1, '2026-07-16 09:30:00', 0, '2026-07-16 09:30:00'),
(1, 2, '2026-07-16 09:35:00', 0, '2026-07-16 09:35:00'),
(2, 1, '2026-07-16 10:15:00', 0, '2026-07-16 10:15:00'),
(3, 1, '2026-07-16 11:20:00', 0, '2026-07-16 11:20:00');
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
INSERT INTO activity_field (label, type, options, required, status) VALUES
('姓名', 'input', NULL, 1, 1),
('手机号', 'input', NULL, 1, 1),
('参与人数', 'number', NULL, 1, 1),
('备注', 'textarea', NULL, 0, 1);
INSERT INTO medicine_unit (name, description, status) VALUES
('片', '片剂', 1),
('粒', '胶囊', 1),
('毫升', '液体剂型', 1),
('支', '注射剂', 1);
INSERT INTO growth_rule (rule_name, rule_code, action_type, growth_value, limit_count, status, remark) VALUES
('每日签到', 'daily_sign', 'sign', 5, 1, 1, '每日首次签到获得成长值'),
('完善健康档案', 'complete_profile', 'profile', 20, 1, 1, '完善个人健康档案'),
('发布动态', 'publish_dynamic', 'dynamic', 10, 0, 1, '发布一条邻里圈动态');
INSERT INTO points_rule (rule_name, rule_code, action_type, points, limit_count, status, remark) VALUES
('每日签到', 'daily_sign', 'sign', 5, 1, 1, '每日首次签到获得积分'),
('参与活动', 'join_activity', 'activity', 15, 0, 1, '报名并参加一次活动'),
('邀请好友', 'invite_friend', 'invite', 50, 0, 1, '成功邀请一位好友注册');
INSERT INTO salary_record (worker_id, worker_name, month, base_salary, commission_total, bonus, deduction, total_salary, status, pay_time) VALUES
(1, '护理员小王', '2026-07', 5000.00, 800.00, 500.00, 100.00, 6200.00, 1, '2026-07-10 00:00:00'),
(2, '护理员小李', '2026-07', 5000.00, 600.00, 300.00, 0.00, 5900.00, 1, '2026-07-10 00:00:00');
INSERT INTO health_service_order (elder_id, elder_name, service_name, worker_id, worker_name, service_time, duration, status, remark) VALUES
(1, '张大爷', '居家保洁服务', 1, '护理员小王', '2026-07-17 09:00:00', 120, 'pending', '需要打扫客厅和卧室'),
(2, '李奶奶', '健康体检套餐', 2, '护理员小李', '2026-07-18 14:00:00', 180, 'assigned', '需陪同前往社区医院');
INSERT INTO sys_operation_log (user_id, user_name, module, operation, method, request_uri, ip, params, status) VALUES
(1, 'admin', '用户管理', '查询用户列表', 'GET', '/api/user/page', '127.0.0.1', 'current=1&size=10', 1),
(1, 'admin', '养老服务', '查询服务项目', 'GET', '/api/product/service-project/page', '127.0.0.1', 'current=1&size=10', 1),
(1, 'admin', '消息管理', '查询消息列表', 'GET', '/api/user/message/page', '127.0.0.1', 'current=1&size=10', 1);

-- 家属关联真实家属账号(每老人对应一位 FAMILY 角色用户；姓名/电话取自该用户，避免"和用户对不上")
-- 注意：必须放在 app_user 插入之后，子查询才能解析出 id
INSERT INTO elder_family (elder_id, app_user_id, family_name, relation, phone) VALUES
(1, (SELECT id FROM app_user WHERE phone='13800001234' LIMIT 1), '演示用户', '儿子', '13800001234'),
(2, (SELECT id FROM app_user WHERE phone='13900001111' LIMIT 1), '张阿姨', '女儿', '13900001111'),
(3, (SELECT id FROM app_user WHERE phone='13900002222' LIMIT 1), '王叔叔', '儿子', '13900002222'),
(4, (SELECT id FROM app_user WHERE phone='13900003333' LIMIT 1), '小李', '外甥女', '13900003333'),
(5, (SELECT id FROM app_user WHERE phone='13900004444' LIMIT 1), '周阿姨', '女儿', '13900004444');
