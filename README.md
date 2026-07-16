# 智慧养老系统 (Elderly Care System)

基于 WorkBuddy 开发的智慧养老前后端一体化系统，包含老人健康管理、活动报名、邻里圈、AI 健康建议等模块。

## 模块结构

| 目录 | 说明 | 技术栈 | 默认端口 |
|------|------|--------|----------|
| `api/` | 后端服务 | Spring Boot 2.7 + MyBatis-Plus + JWT + Redis + MySQL | 8080 |
| `admin/` | 管理后台前端 | Vue 3 + Element Plus + Vite | 3000 |
| `app/` | 用户端（C 端）前端 | Vue 3 + Element Plus + Vite | 5173 |

## 技术要点

- **AI 健康建议**：接入 DeepSeek（`deepseek-chat`），聚合当前用户关联的全部老人档案 + 近 20 条健康记录生成个性化建议；密钥缺失/异常时自动走降级文案。
- **缓存**：AI 建议结果 Redis 缓存 30 分钟，写入新健康记录后写后失效。
- **鉴权**：JWT 过滤器解析 `token`，C 端未登录返回 401，越权返回业务码 `403`。
- **短信验证码**：由 `sms.dev-mode` 开关控制——`true` 返回明文 `devCode`（开发/演示），`false` 走真实网关（上线）。

## 快速启动

```bash
# 1. 后端
cd api
# 复制配置模板并填入自己的密钥/数据库信息
cp src/main/resources/application.example.yml src/main/resources/application.yml
mvn package
java -jar target/elderly-care-api-1.0.0.jar

# 2. 管理后台（另开终端）
cd admin && npm install && npm run dev

# 3. 用户端（另开终端）
cd app && npm install && npm run dev
```

## 默认账号

- 管理后台：`admin / 123456`
- 用户端演示：`13800001234 / 123456`

## 说明

- `api/src/main/resources/application.yml` 含数据库/DeepSeek 密钥，**已被 .gitignore 忽略**，不会进入版本库；请参考 `application.example.yml` 自行配置。
- 后端接口文档：`http://localhost:8080/doc.html`（Knife4j / Swagger）。
