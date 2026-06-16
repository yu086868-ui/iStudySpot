# 前端集成测试临时修改说明

> 日期：2026-06-16
> 说明：为进行微信小程序与后端的集成测试，对后端代码和前端配置做了一系列临时修改。以下为完整清单，后端同学请据此评估哪些需要合入正式代码、哪些需要回滚。

---

## 一、后端新增文件（临时，需决定去留）

### 1. `src/main/java/com/ycyu/istudyspotbackend/controller/WxController.java`
- **用途**：微信小程序端控制器，处理 `/api/wx/**` 路由
- **内容**：实现了微信登录、用户信息、自习室、座位、预约、签到、公告、规则共 23 个接口
- **与安卓端的隔离**：路由前缀 `/api/wx`，与安卓端 `/api/**` 完全隔离
- **微信登录简化实现**：将 wx.login 的 code 加 `"wx_"` 前缀作为 username 存入 user 表，查不到则自动创建用户，返回 JWT token
- **建议**：**应合入正式代码**，这是小程序端必需的路由层。但微信登录的简化实现需要后端同学替换为真实的微信 openId 获取逻辑

### 2. `src/main/resources/application-local.yml`
- **用途**：本地开发 profile，使用 H2 内存数据库替代 MySQL，排除 Redis
- **建议**：可保留作为本地开发配置，但不应影响生产环境

### 3. `src/main/resources/db/h2-init.sql`
- **用途**：H2 内存数据库的建表和测试数据初始化脚本
- **建议**：仅用于本地测试，可保留但不应部署

---

## 二、后端修改的文件

### 1. `pom.xml`
- **修改内容**：H2 依赖的 scope 从 `test` 改为 `runtime`
- **原始值**：`<scope>test</scope>`
- **当前值**：`<scope>runtime</scope>`
- **建议**：如果后端不需要在非 test 环境使用 H2，应改回 `test`

### 2. `src/main/java/.../mapper/UserMapper.java`
- **修改内容**：新增 `findByOpenId(String openId)` 方法
- **说明**：SQL 为 `SELECT * FROM user WHERE username = #{openId}`，复用 username 字段
- **建议**：**应保留**，微信登录需要此方法。但后端同学应考虑是否需要独立的 openId 字段

### 3. `src/main/java/.../mapper/OrderMapper.java`
- **修改内容**：所有 SQL 中的 `` `order` ``（MySQL 反引号）改为 `order`（无引号）
- **原因**：H2 数据库不支持 MySQL 反引号语法
- **建议**：如果后端仅使用 MySQL，**应改回反引号**，因为 `order` 是 MySQL 保留字

### 4. `src/main/java/.../config/WebConfig.java`
- **修改内容**：JWT 拦截器排除列表中新增了微信端无需认证的路径：
  ```java
  "/api/wx/user/login",
  "/api/wx/studyrooms",
  "/api/wx/studyrooms/**",
  "/api/wx/seats",
  "/api/wx/seats/**",
  "/api/wx/announcements",
  "/api/wx/announcements/**",
  "/api/wx/rules",
  "/api/wx/rules/**",
  "/api/wx/reservations/rules",
  "/api/wx/reservations/{id}",
  "/api/wx/card/**"
  ```
- **建议**：**应保留**，这些是微信端无需 JWT 即可访问的接口

---

## 三、前端修改的文件（均为临时，需回滚）

### 1. `miniprogram/mp-user/miniprogram/utils/mock.ts`
- **修改**：`ENABLE_MOCK` 从 `true` 改为 `false`
- **需回滚**：改回 `true`

### 2. `miniprogram/mp-user/miniprogram/utils/request.ts`
- **修改**：`BASE_URL` 从 `https://192.168.21.3:8080/api/wx` 改为 `http://localhost:8080/api/wx`
- **修改**：添加了 `import cache from './cache'` 和请求头自动携带 JWT token 的逻辑
- **需回滚**：BASE_URL 改回原值；token 携带逻辑**应保留**（这是正式功能）

### 3. `miniprogram/mp-user/miniprogram/utils/cache.ts`
- **修改**：新增 `TOKEN` 缓存 key、过期时间、`setToken()`/`getToken()`/`removeToken()` 方法，`clearUserData()` 中添加了 `removeToken`
- **需回滚**：**不应回滚**，这是 JWT 认证的必要功能

### 4. `miniprogram/mp-user/miniprogram/services/auth.ts`
- **修改**：添加了 `import cache`，登录成功后保存 token 到缓存
- **需回滚**：**不应回滚**，这是 JWT 认证的必要功能

### 5. `miniprogram/mp-user/project.private.config.json`
- **修改**：`urlCheck` 从 `true` 改为 `false`
- **需回滚**：改回 `true`（这是开发者工具的域名校验开关，仅本地测试需要关闭）

---

## 四、总结

| 文件 | 操作 | 原因 |
|------|------|------|
| WxController.java | **保留** | 小程序端必需的路由层 |
| application-local.yml | **保留** | 本地开发配置，不影响生产 |
| h2-init.sql | **保留** | 本地测试数据，不影响生产 |
| pom.xml (H2 scope) | **回滚为 test** | 生产环境不需要 H2 runtime |
| UserMapper.java (findByOpenId) | **保留** | 微信登录需要 |
| OrderMapper.java (反引号) | **回滚为反引号** | MySQL 保留字需要反引号 |
| WebConfig.java (wx排除列表) | **保留** | 微信端接口白名单 |
| mock.ts (ENABLE_MOCK) | **回滚为 true** | 恢复 Mock 模式 |
| request.ts (BASE_URL) | **回滚原地址** | 恢复远程服务器地址 |
| request.ts (token携带) | **保留** | JWT 认证必需 |
| cache.ts (token方法) | **保留** | JWT 认证必需 |
| auth.ts (保存token) | **保留** | JWT 认证必需 |
| project.private.config.json (urlCheck) | **回滚为 true** | 恢复域名校验 |
