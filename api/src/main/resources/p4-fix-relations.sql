-- ============================================================
-- P4-3 关系数据修正（适配当前运行库实际标签体系）
-- 背景：运行库 user_tag / service_worker_tag 已被演化修改，
--       且"经验丰富"标签跨服务类型重复，需按 service_type 精确匹配
-- 作用：重建 user_tag_relation / service_worker_tag_relation
-- ============================================================

USE elderly_care;

DELETE FROM user_tag_relation;
DELETE FROM service_worker_tag_relation;

-- 用户标签：使用运行库当前真实存在的 user_tag 名称
INSERT INTO user_tag_relation (user_id, tag_id)
SELECT 1, id FROM user_tag WHERE tag_name = '高血压'
UNION ALL SELECT 1, id FROM user_tag WHERE tag_name = 'VIP会员'
UNION ALL SELECT 2, id FROM user_tag WHERE tag_name = '糖尿病'
UNION ALL SELECT 2, id FROM user_tag WHERE tag_name = '活跃用户'
UNION ALL SELECT 3, id FROM user_tag WHERE tag_name = '心脏病'
UNION ALL SELECT 3, id FROM user_tag WHERE tag_name = '长期用户'
UNION ALL SELECT 4, id FROM user_tag WHERE tag_name = '骨质疏松'
UNION ALL SELECT 4, id FROM user_tag WHERE tag_name = '自理老人'
UNION ALL SELECT 5, id FROM user_tag WHERE tag_name = 'VIP会员'
UNION ALL SELECT 5, id FROM user_tag WHERE tag_name = '新用户';

-- 服务人员标签：按 service_type 精确匹配，避免"经验丰富"重复关联
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

SELECT 'relations fixed' AS result;
