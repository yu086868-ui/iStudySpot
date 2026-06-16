# 软件测试贡献说明

姓名：贺祥宇  学号：2312190107  角色：前端  日期：2026-6-15

## 测试策略

软件测试分为四个方面：

| 测试类型 | 方式 | 说明 |
|---------|------|------|
| 功能测试 | 手工测试 | 对系统核心功能进行人工验证，检查输出是否符合预期 |
| 界面测试 | 手工测试 | 对小程序页面布局、交互、样式进行人工检查 |
| 数据测试 | 自动化测试 | 通过 Jest 编写测试用例，验证数据验证、转换、持久化的正确性 |
| 异常测试 | 自动化测试 | 通过 Jest 编写测试用例，验证网络异常、空数据、并发操作等异常场景 |

> 采用手工测试方式对系统核心功能进行了验证，通过执行预先设计的测试用例，检查系统输出是否符合预期结果。暂不考虑安全方面的测试（如 JWT 与 Token 等）。

## 测试文件列表

### 服务层测试（Service）

| 文件 | 用例数 | 测试内容 |
|------|--------|----------|
| `tests/unit/services/auth.test.ts` | 8 | 微信登录、wxLogin、loginWithWx 成功/失败场景 |
| `tests/unit/services/reservation.test.ts` | 11 | 创建预约、获取预约列表、取消预约、预约规则 |
| `tests/unit/services/checkin.test.ts` | 10 | 签到、签退、签到记录、当前签到状态 |
| `tests/unit/services/card.test.ts` | 9 | 生成卡片、卡片详情、卡片列表 |
| `tests/unit/services/user.test.ts` | 7 | 获取用户信息、更新用户资料、上传头像 |
| `tests/unit/services/seat.test.ts` | 6 | 获取座位列表、座位详情 |
| `tests/unit/services/studyroom.test.ts` | 7 | 获取自习室列表、自习室详情、自习室座位 |
| `tests/unit/services/announcement.test.ts` | 7 | 获取公告列表、公告详情 |
| `tests/unit/services/rule.test.ts` | 6 | 获取规则列表、规则详情 |
| `tests/unit/services/health-check.test.ts` | 17 | 网络检查、登录状态、存储使用率、服务器可达性、总体状态计算 |
| `tests/unit/services/metrics.test.ts` | 13 | 请求追踪、页面加载追踪、指标摘要、失败/慢请求过滤 |

### 数据测试（Data）

| 文件 | 用例数 | 测试内容 |
|------|--------|----------|
| `tests/unit/data/data-validation.test.ts` | 21 | 预约参数验证、签到参数验证、卡片生成参数验证、用户资料更新验证、座位/预约状态验证 |
| `tests/unit/data/data-persistence.test.ts` | 19 | Store-Cache 同步、Cache 过期处理、clearUserData/clearAll、双向同步、复合 key 存取 |
| `tests/unit/data/data-transform.test.ts` | 31 | API→Store 转换、缓存→响应格式转换、座位布局转换、卡片稀有度/主题规则、Markdown 处理 |

### 异常测试（Exception）

| 文件 | 用例数 | 测试内容 |
|------|--------|----------|
| `tests/unit/exception/network-error.test.ts` | 16 | request.post/get 异常、mock 异常、wx.login/uploadFile 失败、非 200 状态码、null data |
| `tests/unit/exception/null-data.test.ts` | 17 | 空用户/预约/签到数据、cache 返回 null、空字符串/undefined 参数、空数组列表 |
| `tests/unit/exception/concurrent-ops.test.ts` | 15 | 连续创建预约、重复签到/取消/签退、clearUser 与 setUser 竞态、事件触发顺序 |

### 工具层测试（Utils）

| 文件 | 用例数 | 测试内容 |
|------|--------|----------|
| `tests/unit/utils/mock.test.ts` | 25 | Mock 数据管理器：用户/预约/签到/卡片的 CRUD、flushRequest |
| `tests/unit/utils/mock-data.test.ts` | 14 | 卡片稀有度生成规则、主题分类、Markdown 生成 |
| `tests/unit/utils/cache.test.ts` | 12 | 缓存读写、过期策略、清除操作 |
| `tests/unit/utils/store.test.ts` | 18 | 全局状态管理、事件系统、clearUser/clearAll |
| `tests/unit/utils/seat-layout.test.ts` | 30 | 座位布局创建、分组、状态查询、统计 |
| `tests/unit/utils/markdown-contract.test.ts` | 12 | Markdown 分割、验证、处理 |
| `tests/unit/utils/util.test.ts` | 6 | 时间格式化 |
| `tests/unit/utils/logger.test.ts` | 18 | 四级日志、级别过滤、截断、查询、持久化 |
| `tests/unit/utils/error-monitor.test.ts` | 16 | JS/Promise/运行时错误、错误合并、截断、加载 |
| `tests/unit/utils/navigation.test.ts` | 17 | 导航方式选择、失败降级、返回首页、路由判断 |

### 组件测试（Component）

| 文件 | 用例数 | 测试内容 |
|------|--------|----------|
| `tests/unit/components/card-popup/index.test.ts` | 25 | 卡片弹窗属性、data 默认值、observer、streaming 功能、动画 |

## 测试覆盖率

```
-----------------------|---------|----------|---------|---------|
File                   | % Stmts | % Branch | % Funcs | % Lines |
-----------------------|---------|----------|---------|---------|
All files              |   95.81 |    85.77 |   95.54 |   95.72 |
 services              |    94.9 |    85.59 |   91.66 |   94.84 |
  announcement.ts      |     100 |    89.28 |     100 |     100 |
  auth.ts              |     100 |    95.23 |     100 |     100 |
  card.ts              |      65 |    61.01 |   44.44 |      65 |
  checkin.ts           |     100 |    84.61 |     100 |     100 |
  health-check.ts      |   97.56 |    96.96 |     100 |   97.53 |
  metrics.ts           |   97.77 |    89.74 |   94.11 |   97.72 |
  reservation.ts       |   94.91 |    88.52 |   85.71 |   94.82 |
  rule.ts              |     100 |    88.46 |     100 |     100 |
  seat.ts              |     100 |    89.28 |     100 |     100 |
  studyroom.ts         |     100 |    92.68 |     100 |     100 |
  user.ts              |     100 |    86.53 |     100 |     100 |
 utils                 |   96.76 |    86.26 |   97.54 |   96.66 |
  cache.ts             |   98.87 |      100 |   97.05 |   98.87 |
  error-monitor.ts     |   96.25 |    82.05 |   93.33 |   96.15 |
  logger.ts            |    98.5 |    84.61 |     100 |   98.48 |
  markdown-contract.ts |     100 |      100 |     100 |     100 |
  markdown-engine.ts   |   85.71 |      100 |      50 |   85.71 |
  navigation.ts        |   98.21 |    86.95 |   94.11 |   98.18 |
  seat-layout.ts       |     100 |      100 |     100 |     100 |
  store.ts             |      92 |    77.08 |     100 |   91.73 |
  util.ts              |     100 |      100 |     100 |     100 |
-----------------------|---------|----------|---------|---------|
```

- 整体语句覆盖率：**95.81%**
- 整体分支覆盖率：**85.77%**
- 整体函数覆盖率：**95.54%**
- 整体行覆盖率：**95.72%**

## 测试结果

- 测试套件总数：**28 个**
- 测试用例总数：**504 个**
- 通过：**504 个**
- 失败：**0 个**

## 命令行启动测试

```bash
cd miniprogram\mp-user

# 安装依赖
npm install

# 运行全部测试
npm test

# 运行测试并生成覆盖率报告
npm run test:ci

# 运行类型检查（lint）
npm run lint

# 仅运行数据测试
npx jest tests/unit/data/

# 仅运行异常测试
npx jest tests/unit/exception/
```

## AI 辅助测试

### 使用工具

- Trae（基于 GLM-5.1）

### Prompt 过程

1. **生成数据测试用例**
   - Prompt："补充数据测试用例，包括数据验证、数据转换、数据持久化三个方面"
   - 修改：调整了 mock 数据与源码中 mock-data 的 ID 类型一致性（number vs string）

2. **生成异常测试用例**
   - Prompt："补充异常测试用例，包括网络异常、空数据处理、并发操作三个方面"
   - 修改：补充了 store 事件触发顺序验证

3. **生成服务和工具测试用例**
   - Prompt："补充缺失的 health-check、metrics、logger、error-monitor、navigation 测试"
   - 修改：修复了 health-check.test.ts 中 ApiResponse 类型缺少 message/timestamp 字段的问题

4. **修复已有测试**
   - Prompt："检查并修复失败的测试用例"
   - 修改：修复了 auth.test.ts（wx.login 未 mock）、user.test.ts（uploadAvatar 期望值）、card-popup（组件源码更新后测试未同步）、mock.test.ts（userID 类型不匹配）

### AI 生成 + 人工修改的测试数量

- 原有测试用例：283 个
- 新增测试用例：221 个（数据测试 71 + 异常测试 48 + 服务测试 30 + 工具测试 51 + 组件修复 21）
- 最终测试用例：**504 个**

## 遇到的问题和解决

1. **问题：14 个已有测试用例失败**
   - 原因：`setup.ts` 中缺少 `wx.login` 和 `wx.uploadFile` 的 mock；组件源码更新后测试未同步；mock 数据中 userID 类型不匹配
   - 解决：在 `setup.ts` 中添加缺失的 wx mock；同步 card-popup 组件测试与源码；修正 userID 从 `'user_001'` 为 `'1'`

2. **问题：`npm run lint`（tsc --noEmit）报大量类型错误**
   - 原因：`tsconfig.json` 的 `include` 包含了测试文件；`types` 配置限制了类型包加载；源码中存在泛型 null 赋值、string/number 类型不匹配等问题
   - 解决：将 `include` 限制为 `miniprogram/**/*.ts`，排除 `tests/`；用 `files` 引入 `typings/index.d.ts`；修复 `mock.ts`、`checkin.ts`、`markdown-contract.ts`、`home.ts` 的类型错误

3. **问题：微信小程序环境没有 `URL` API**
   - 原因：`home.ts` 中使用了 `new URL()` 解析二维码内容，但小程序运行时不支持
   - 解决：替换为手动解析查询字符串的方式

## 心得体会

通过本次测试工作，我对微信小程序的自动化测试有了更深入的理解：

- **测试分层的重要性**：将测试分为数据测试和异常测试两大类，使得测试用例的组织更加清晰，也更容易定位问题
- **Mock 的必要性**：微信小程序的 `wx` API 无法在 Node.js 环境中直接运行，必须通过 mock 来模拟，而 mock 的准确性直接影响测试的可靠性
- **类型安全与测试的关系**：修复 TypeScript 类型错误的过程也是发现潜在 bug 的过程，例如 `string | number` 类型混用可能导致运行时错误
- **测试维护成本**：组件源码更新后测试未同步是常见问题，需要建立良好的代码变更通知机制



---






# ~~软件测试贡献说明~~

~~姓名：贺祥宇  学号：2312190107  角色：前端  日期：2026-4-28~~

## ~~完成的测试工作~~

### ~~测试文件~~

~~`miniprogram\mp-user\tests`下，相关测试文档与测试文件。~~

~~命令行启动测试：~~

```
cd miniprogram\mp-user
npm install

# 运行测试
npm test

# 运行测试并生成覆盖率报告
npm run test:coverage
```

```
cd miniprogram\ai-chat
npm install

# 运行测试
npm test

# 运行测试并生成覆盖率报告
npm run test:coverage
```

### ~~测试清单~~

- ~~ 正常情况测试（150 个）~~
- ~~ 边界 / 异常情况测试（48 个）~~
- ~~ Mock 使用（数据库 / API / 组件外部依赖）~~

### ~~覆盖率~~

- ~~核心模块覆盖率：82.46 %~~

### ~~AI 辅助（如有）~~

- ~~使用工具：Trae~~

- ~~Prompt 示例：~~

  > ~~为XXX项目生成测试。~~
  >
  > ~~单元测试、集成测试、端到端测试，工作范围限制在前端。~~
  >
  > ~~包含边界与异常等情况，使用MOCK。~~
  >
  > ~~达到覆盖率要求。~~

  - ~~AI 生成 + 人工修改的测试数量：198 个~~

## ~~PR 链接~~

- ~~PR #45: [Feature/贺祥宇 frontend doc by kiraTheresa · Pull Request #45 · yu086868-ui/iStudySpot](https://github.com/yu086868-ui/iStudySpot/pull/45)~~

## ~~遇到的问题和解决~~

## ~~心得体会~~

