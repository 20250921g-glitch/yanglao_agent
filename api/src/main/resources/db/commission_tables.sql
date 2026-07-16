-- 佣金记录表
CREATE TABLE IF NOT EXISTS commission_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL COMMENT '订单ID',
  order_no VARCHAR(64) COMMENT '订单编号',
  worker_id BIGINT NOT NULL COMMENT '服务人员ID',
  worker_name VARCHAR(50) COMMENT '服务人员姓名',
  order_amount DECIMAL(10,2) COMMENT '订单金额',
  commission_rate DECIMAL(5,2) DEFAULT 10.00 COMMENT '佣金比例(%)',
  commission_amount DECIMAL(10,2) COMMENT '佣金金额',
  status TINYINT DEFAULT 0 COMMENT '状态: 0待结算 1已结算',
  settle_time DATETIME COMMENT '结算时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_worker_id (worker_id),
  INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='佣金记录表';

-- 工资记录表
CREATE TABLE IF NOT EXISTS salary_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  worker_id BIGINT NOT NULL COMMENT '服务人员ID',
  worker_name VARCHAR(50) COMMENT '服务人员姓名',
  month VARCHAR(7) NOT NULL COMMENT '月份: 2026-07',
  base_salary DECIMAL(10,2) DEFAULT 0 COMMENT '基本工资',
  commission_total DECIMAL(10,2) DEFAULT 0 COMMENT '佣金合计',
  bonus DECIMAL(10,2) DEFAULT 0 COMMENT '奖金',
  deduction DECIMAL(10,2) DEFAULT 0 COMMENT '扣款',
  total_salary DECIMAL(10,2) COMMENT '应发工资',
  status TINYINT DEFAULT 0 COMMENT '状态: 0待发放 1已发放',
  pay_time DATETIME COMMENT '发放时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_worker_month (worker_id, month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工资记录表';
