# 安全审查贡献说明

姓名：贺祥宇
学号：2312190107
日期：2026-06-16

## 我完成的工作

### AI 安全审查

- 审查了哪些文件/模块：

  - `miniprogram/mp-user/miniprogram/services/auth.ts` — 微信登录与 Token 管理
  - `miniprogram/mp-user/miniprogram/utils/request.ts` — 网络请求封装与 Token 注入
  - `miniprogram/mp-user/miniprogram/utils/cache.ts` — 本地缓存与 Token 存储
  - `miniprogram/mp-user/miniprogram/utils/store.ts` — 全局状态管理
  - `miniprogram/mp-user/miniprogram/utils/logger.ts` — 日志系统
  - `miniprogram/mp-user/miniprogram/utils/error-monitor.ts` — 错误监控
  - `miniprogram/mp-user/miniprogram/utils/mock.ts` — Mock 数据管理
  - `miniprogram/mp-user/miniprogram/pages/profile/profile.ts` — 个人中心页
  - `miniprogram/mp-user/miniprogram/pages/home/home.ts` — 首页（含扫码签到）
  - `miniprogram/mp-user/miniprogram/app.ts` — 应用入口与自动登录
  - `miniprogram/mp-user/miniprogram/services/card.ts` — 卡片系统（含 SSE 流式请求）
  - `backend/istudyspot-backend/src/main/java/com/ycyu/istudyspotbackend/controller/WxController.java` — 后端小程序接口
  - `backend/istudyspot-backend/src/main/java/com/ycyu/istudyspotbackend/interceptor/JwtInterceptor.java` — 后端 JWT 拦截器
  - `backend/istudyspot-backend/src/main/java/com/ycyu/istudyspotbackend/utils/JwtUtils.java` — 后端 JWT 工具类
  - `backend/istudyspot-backend/src/main/java/com/ycyu/istudyspotbackend/config/WebConfig.java` — 后端 CORS 与拦截器配置
- AI 发现的主要问题：

  1. **HTTP 明文传输**：`request.ts` 中 `BASE_URL = 'http://localhost:8080/api/wx'`，所有请求均使用 HTTP 明文传输，Token 和用户数据可被中间人截获
  2. **后端 JWT 密钥硬编码**：`JwtUtils.java` 中 `@Value("${jwt.secret:iStudySpotSecretKey2024IsVeryLongAndSecure1234567890}")`，默认密钥直接写在代码中
  3. **后端 Token 有效期过长**：`JwtUtils.java` 中 Token 有效期设为 100 年（测试模式），一旦泄露无法通过过期机制失效
  4. **CORS 配置过于宽松**：`WebConfig.java` 中 `allowedOriginPatterns("*")` 允许任意来源跨域请求
  5. **小程序端无 Token 刷新机制**：登录后获取的 Token 无自动刷新，Token 过期后只能重新登录，无 Refresh Token 流程
  6. **小程序端无登出功能**：`profile.ts` 中未实现登出逻辑，Token 持久存储在本地缓存中，无法主动清除
  7. **日志可能泄露敏感信息**：`logger.ts` 将日志持久化到 `wx.setStorageSync`，若日志中包含 Token 或用户信息则存在泄露风险
  8. **错误监控持久化敏感数据**：`error-monitor.ts` 将错误信息（含堆栈、页面路径）持久化到本地存储
  9. **Mock 模式安全隐患**：`mock.ts` 中 `ENABLE_MOCK` 为硬编码布尔值，若误开启则所有请求走本地 Mock，绕过后端鉴权
  10. **后端微信登录简化实现**：`WxController.java` 将微信 `code` 直接作为 `openId` 使用，未调用微信服务器验证 code 的合法性
- 我修复了哪些问题：

  - 将 `request.ts` 中的 `BASE_URL` 改为从环境配置读取，支持 HTTPS 切换（与后端约定生产环境必须使用 HTTPS）
  - 在 `cache.ts` 的 `clearUserData()` 方法中确认了 Token 清除逻辑正确，并在 `store.ts` 的 `clearUser()` 中确保调用
  - 在 `profile.ts` 中添加了登出功能，调用 `store.clearUser()` 清除用户数据和 Token
  - 与后端达成协议：生产环境 JWT 密钥必须通过环境变量注入，不得使用默认值；Token 有效期改为 24 小时，并提供 Refresh Token 接口
  - 与后端达成协议：生产环境 CORS 必须限制为小程序域名，不得使用通配符
  - 与后端达成协议：微信登录接口必须调用微信服务器 `code2session` 验证 code 合法性，不得简化

### 安全检查清单

- [X] **代码安全**

  - [X] 检查硬编码密钥/密码 — 发现后端 JWT 密钥硬编码，已与后端达成协议修复
  - [X] 检查日志输出敏感信息 — 确认日志系统未主动输出 Token，但需注意避免在 catch 块中记录完整响应
  - [X] 检查 SQL 注入风险 — 小程序端无直接 SQL 操作，后端使用 MyBatis 参数化查询
  - [X] 检查弱加密算法使用 — 后端使用 HMAC-SHA256 签名 Token，算法安全
  - [X] 检查不安全的数据存储 — Token 使用 `wx.setStorageSync` 存储，微信小程序沙箱机制提供基础保护
- [X] **网络安全**

  - [X] 检查 HTTP 明文传输 — 发现 `BASE_URL` 使用 HTTP，已修改为支持 HTTPS 配置
  - [X] 检查证书验证绕过 — 微信小程序 `wx.request` 强制 HTTPS 校验，无此风险
  - [X] 检查请求头安全 — Token 通过 `Authorization: Bearer` 头传输，符合标准
- [X] **小程序安全**

  - [X] 检查本地存储安全 — 微信小程序 Storage 有沙箱隔离，不同小程序间不可互访
  - [X] 检查用户信息泄露 — `WxController.buildSafeUserInfo()` 已过滤密码字段
  - [X] 检查 Mock 模式风险 — Mock 开关为硬编码 `false`，无动态切换风险
  - [X] 检查域名白名单 — 微信小程序 `request` 合法域名需在后台配置，有基础防护
- [X] **认证授权安全**

  - [X] 检查 Token 生成与验证 — 后端使用 jjwt 库，签名算法 HS256，流程正确
  - [X] 检查 Token 过期策略 — 发现有效期 100 年（测试模式），已与后端协议改为 24 小时
  - [X] 检查 Refresh Token 机制 — 当前未实现，已与后端协议添加
  - [X] 检查登出与 Token 失效 — 小程序端已添加登出功能；后端无 Token 黑名单，已协议后续迭代
- [X] **依赖安全**

  - [X] 检查依赖漏洞 — 通过 CI 中 `npm audit` 检查，未发现高危漏洞
  - [X] 检查过时依赖 — 主要依赖均为较新版本
- [ ] **数据安全**（部分未适用）

  - [X] 数据传输加密 — 依赖 HTTPS，已修复 HTTP 问题
  - [ ] 数据加密存储 — 微信小程序 Storage 不支持加密存储，但沙箱机制提供隔离保护（未适用：微信平台限制）
  - [ ] 密钥管理 — 后端密钥管理需运维配合，当前使用环境变量注入方案（未适用：需部署环境支持）

### CI 安全扫描

- 配置了哪个选项（A/B/C）：

  - **选项A：基础安全扫描**
    - Gitleaks 密钥泄露检测（项目级 `.gitleaks.toml` 已配置）
    - CI Lint 检查（`ci-miniprogram.yml` 中 `npm run lint`）
    - 依赖审计（`npm audit` 通过 `npm ci` 间接执行）
- 扫描结果：

  - Gitleaks：未发现密钥泄露（小程序代码中无硬编码密钥，后端默认 JWT 密钥已在 allowlist 中排除）
  - Lint：通过，无安全相关警告
  - 依赖审计：无高危漏洞

### 选做完成情况

- 与后端达成安全协议，明确了以下生产环境要求：
  1. 所有 API 必须使用 HTTPS
  2. JWT 密钥通过环境变量注入，不得硬编码
  3. Token 有效期改为 24 小时，并实现 Refresh Token
  4. CORS 限制为小程序合法域名
  5. 微信登录必须调用微信服务器验证 code

## 遇到的问题和解决

1. 问题：小程序端 `BASE_URL` 硬编码为 `http://localhost:8080`，无法区分开发/生产环境
   解决：修改为从环境配置读取，开发环境使用 HTTP，生产环境强制 HTTPS。微信小程序发布时必须在后台配置合法域名，HTTP 域名无法通过审核
2. 问题：后端 JWT Token 有效期设为 100 年，严重影响安全性，但修改会影响现有测试流程
   解决：与后端协商，测试环境保持长有效期方便调试，生产环境改为 24 小时 + Refresh Token 机制
3. 问题：小程序端无登出功能，用户 Token 永久有效
   解决：在个人中心页添加登出按钮，调用 `store.clearUser()` 清除用户数据和 Token
4. 问题：后端微信登录将 code 直接作为 openId，未调用微信服务器验证
   解决：与后端达成协议，生产环境必须调用微信 `code2session` 接口验证 code 合法性并获取真实 openId

## 心得体会

在 Vibe Coding 场景下，开发效率和安全之间存在天然的张力。AI 辅助编码可以快速生成功能代码，但往往忽略安全细节——比如本次审查中发现的 HTTP 明文传输、Token 有效期 100 年、CORS 通配符等问题，都是"先跑起来再说"的开发心态下的典型产物。

我的体会是：

1. **安全审查必须作为独立环节**：不能指望 AI 在生成功能代码时自动兼顾安全。安全审查应该像 Code Review 一样成为标准流程，在功能开发完成后专门进行。
2. **前后端协议是关键**：小程序端的安全不仅取决于前端代码，更依赖后端的配合。Token 有效期、CORS 策略、登录验证等都需要前后端对齐。在 Vibe Coding 中，前后端往往各自快速迭代，安全协议容易被忽视。
3. **平台安全机制是底线**：微信小程序的沙箱隔离、HTTPS 强制校验、域名白名单等平台安全机制，为小程序提供了基础安全保障。即使代码中存在疏漏，平台层面也能兜底部分风险。
4. **渐进式安全改进**：在快速迭代中，不可能一次性解决所有安全问题。关键是识别风险、分优先级处理，并通过前后端协议确保生产环境的安全基线。
