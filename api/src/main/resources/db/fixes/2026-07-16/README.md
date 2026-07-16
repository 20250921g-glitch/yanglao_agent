# 2026-07-16 生产环境修复脚本归档

本目录收录 2026-07-16 在老师共享服务器（阿里云 `39.97.240.64`，自有库 `elderly_care_20250921`）上修复「schema 漂移 + 演示数据缺失」时使用的增量 SQL。

> 这些脚本**只作用于自有库 `elderly_care_20250921`**，符合老师红线（不碰他人库/表/进程）。
> 全部脚本幂等：建表用 `IF NOT EXISTS`，加列用 `information_schema` 条件 `ALTER`，可重复执行。

## 脚本清单与执行顺序

| 顺序 | 文件 | 作用 |
| --- | --- | --- |
| 1 | `20250921g-fix-appuser.sql` | 为 `app_user` 补齐代码实体所需但建表 SQL 缺失的 16 列（real_name / gender / birth_date / id_card / nation 等） |
| 2 | `20250921g-create-tables.sql` | 建代码引用但建库时缺失的 14 张表（activity_field、app_message_read、conversation、conversation_message、dynamic_comment、dynamic_favorite、dynamic_like、growth_rule、health_service_order、medicine_unit、points_rule、salary_record、service_project、sys_operation_log） |
| 3 | `20250921g-fix-columns.sql` | 为 `elder` 补 `app_user_id`，为 `product_order` 补 18 列（与实体对齐） |
| 4 | `20250921g-seed.sql` | 为 15 张业务表演示数据（service_project=6、app_message=4 等），使养老服务/消息通知等界面在演示账号下有内容 |

## 配套工具

- `audit_columns.py`：实体类 `@TableName` 列定义 ↔ 运行库 `information_schema` 列全量比对脚本，用于发现上述漂移（缺表/缺列）。需要时改 `DB_*` 连接参数后运行。

## 说明

完整的「一次性建库 + 全量种子」已并入 `src/main/resources/data-init.sql`（63 张表 + 50 段 INSERT），本目录保留的是**增量修复过程脚本**，便于回溯与在其它环境复现同一修复。
