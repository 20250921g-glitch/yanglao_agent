-- 佣金记录表
CREATE TABLE IF NOT EXISTS commission_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  order_no VARCHAR(64),
  worker_id BIGINT NOT NULL,
  worker_name VARCHAR(50),
  order_amount DECIMAL(10,2),
  commission_rate DECIMAL(5,2) DEFAULT 10.00,
  commission_amount DECIMAL(10,2),
  status TINYINT DEFAULT 0,
  settle_time DATETIME,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_worker_id (worker_id),
  INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 工资记录表
CREATE TABLE IF NOT EXISTS salary_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  worker_id BIGINT NOT NULL,
  worker_name VARCHAR(50),
  month VARCHAR(7) NOT NULL,
  base_salary DECIMAL(10,2) DEFAULT 0,
  commission_total DECIMAL(10,2) DEFAULT 0,
  bonus DECIMAL(10,2) DEFAULT 0,
  deduction DECIMAL(10,2) DEFAULT 0,
  total_salary DECIMAL(10,2),
  status TINYINT DEFAULT 0,
  pay_time DATETIME,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_worker_month (worker_id, month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入测试佣金数据
INSERT INTO commission_record (order_id, order_no, worker_id, worker_name, order_amount, commission_rate, commission_amount, status) VALUES
(1, 'SO20260708001', 1, '张护士', 200.00, 10.00, 20.00, 0),
(2, 'SO20260708002', 2, '李护士', 150.00, 10.00, 15.00, 1);

-- 插入测试工资数据
INSERT INTO salary_record (worker_id, worker_name, month, base_salary, commission_total, bonus, deduction, total_salary, status) VALUES
(1, '张护士', '2026-07', 3000.00, 580.00, 200.00, 0, 3780.00, 0),
(2, '李护士', '2026-07', 3000.00, 420.00, 100.00, 50.00, 3470.00, 1);

SELECT 'Tables created and test data inserted' AS result;
