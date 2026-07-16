-- 补齐 elderly_care_20250921 中现有表缺失的列（schema 漂移修复）
-- 仅作用于自己的库，符合老师红线。所有列按对应实体类反推类型。
USE elderly_care_20250921;

-- elder: 补 C 端关联用户
SET @exists := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema='elderly_care_20250921' AND table_name='elder' AND column_name='app_user_id');
SET @sql := IF(@exists=0, 'ALTER TABLE elder ADD COLUMN app_user_id BIGINT NULL COMMENT ''关联的普通用户ID(C端健康档案归属)'', ADD KEY idx_app_user (app_user_id)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- product_order: 补订单字段
SET @cnt := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema='elderly_care_20250921' AND table_name='product_order' AND column_name='user_id');
SET @sql2 := IF(@cnt=0,
  'ALTER TABLE product_order
    ADD COLUMN user_id BIGINT NULL COMMENT ''下单用户ID(app_user)'',
    ADD COLUMN goods_amount DECIMAL(10,2) NULL COMMENT ''商品总价'',
    ADD COLUMN discount_price DECIMAL(10,2) NULL COMMENT ''优惠金额'',
    ADD COLUMN freight DECIMAL(10,2) NULL COMMENT ''运费'',
    ADD COLUMN payable_price DECIMAL(10,2) NULL COMMENT ''应付款'',
    ADD COLUMN pay_type VARCHAR(50) NULL COMMENT ''支付方式'',
    ADD COLUMN pay_time DATETIME NULL COMMENT ''支付时间'',
    ADD COLUMN order_source VARCHAR(50) NULL COMMENT ''订单来源'',
    ADD COLUMN remark VARCHAR(500) NULL COMMENT ''备注'',
    ADD COLUMN product_name VARCHAR(255) NULL COMMENT ''首商品名'',
    ADD COLUMN product_image VARCHAR(500) NULL COMMENT ''首商品图'',
    ADD COLUMN product_count INT NULL COMMENT ''商品件数'',
    ADD COLUMN worker_id BIGINT NULL COMMENT ''派单服务人员ID'',
    ADD COLUMN worker_name VARCHAR(50) NULL COMMENT ''派单服务人员姓名'',
    ADD COLUMN appointment_time DATETIME NULL COMMENT ''预约上门时间'',
    ADD COLUMN dispatch_time DATETIME NULL COMMENT ''派单时间'',
    ADD COLUMN dispatch_user VARCHAR(50) NULL COMMENT ''派单人'',
    ADD COLUMN service_order_no VARCHAR(50) NULL COMMENT ''关联工单编号'',
    ADD KEY idx_user (user_id),
    ADD KEY idx_worker (worker_id)',
  'SELECT 1');
PREPARE stmt2 FROM @sql2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

SELECT 'DONE: missing columns fixed' AS result;
