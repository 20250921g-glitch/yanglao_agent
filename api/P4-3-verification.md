# P4-3 后端数据层补 · 验收报告

**目标**：P4-2-A 的 6 个核心页（用户列表 / 服务人员 / 动态管理 / 商品列表 / 活动列表 / 订单）此前因后端实体缺字段，部分列永久显示 `—`。本批次全量补齐字段 + 丰富种子，使 6 页 100% 还原蓝图。

**结论**：✅ 达成。6 个接口字段覆盖率 100%，前端 `npm run build` 通过，后端已重新构建运行于 :8080。

---

## 1. 后端改动清单

| 模块 | 改动 |
|------|------|
| `Dynamic.java` | 新增 `topic / userPhone / collectCount / shareCount / commentCount` |
| `Product.java` / `Activity.java` | 新增 `updateBy`（最后更新人） |
| `AppUserService.getPage` | 新增 `fillTags(records)`：批量聚合 `user_tag_relation`+`user_tag`，解决用户列表标签 `—`（此前仅详情页填标签） |
| `ProductService` / `ActivityService` | add/update 时写入 `updateBy = currentOperator()`（取自 `SecurityContextHolder`，回退 `"admin"`） |
| `DynamicService` + `DynamicController` | 新增 `startCreateTime` / `endCreateTime` 参数，按 `createTime` 区间过滤（发布日期筛选真正生效） |

## 2. 数据库 / 种子

- `p4-data-layer-fill.sql`：条件 ALTER 存储过程 + 幂等种子。
- `p4-fix-relations.sql`：按 `service_type` 精确修正服务人员标签关系（消除"经验丰富"跨类型重复）。
- `data-init.sql` 同步：dynamic/product/activity 补列 CREATE + P4-3 全部种子；临时测试库 `elderly_care_test` 干净重跑验证通过。
- 运行库清理：删除空行 `dynamic.id=1`、`service_worker.id=6`（无 FK 引用）；`activity.update_by` 回填 admin（10/10）。

## 3. 接口字段覆盖率验证（urllib + admin token，运行库实测）

| 接口 | 字段 | 覆盖率 | 样例 |
|------|------|--------|------|
| `/api/user/page` | `tags` | 5/7（2 用户真实无标签） | `['高血压','VIP会员']` |
| `/api/service/worker/page` | `serviceType` `region` `joinType` `avatar` `tags` | 5/5 全部 | `家政护理` / `朝阳区` / `服务端注册` / 头像URL |
| `/api/operation/dynamic/page` | `topic` `userPhone` `collectCount` `shareCount` `commentCount` | 4/4 | `社区活动` / `13800138004` |
| `/api/product/page` | `updateBy` | 10/10 | `admin` |
| `/api/operation/activity/page` | `updateBy` `type` | 10/10 | `admin` / `节日活动` |
| 动态日期筛选 | `startCreateTime~endCreateTime` | 2026-07 → 4 行 | ✅ 生效 |

## 4. 构建与运行

- 后端 `mvn package`：**BUILD SUCCESS**（修复 `getPage` 重复声明 `page` 变量编译错；repackage 因旧进程占 jar 失败 → 先杀旧进程再构建）。
- 前端 `npm run build`：**BUILD_EXIT=0**（修复 `service/worker.vue` 标签 `name`→`tagName` 误用，此前会渲染 `[object Object]`）。
- 后端已重新启动运行于 `:8080`。

## 5. 关键经验（已写入 MEMORY.md）

1. **运行库已偏离 data-init.sql**：写 SQL 前先 `SELECT` 摸清真实数据，再按真实值写幂等种子。
2. **标签字段名是 `tagName`（非 `name`）**：前端标签渲染一律用 `tag.tagName`，与 `worker-tag.vue` / `worker-detail.vue` 保持一致。
3. **服务人员用 `ServiceWorker` 实体，不是 `Staff`** —— 之前误判"Staff 缺字段"是错的。
