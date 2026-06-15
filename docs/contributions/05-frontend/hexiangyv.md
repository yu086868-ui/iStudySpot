# 前端开发贡献说明

姓名：贺祥宇
学号：2312190107
技术栈：微信小程序（TypeScript + WXML + WXSS + Skyline 渲染引擎）
日期：2026-06-15

## 我完成的工作

### 一、应用架构与基础设施

#### 1.1 应用配置与入口（app.json / app.ts / app.wxss）

- 配置了 7 个页面路由，自定义 TabBar（4 个 Tab：规则公告、首页、卡片、个人中心）
- 启用 Skyline 渲染引擎 + glass-easel 组件框架，自定义导航栏（`navigationStyle: custom`）
- 启用懒加载组件（`lazyCodeLoading: requiredComponents`）
- 启动时自动微信登录：检测 store 登录状态，未登录则调用 `authApi.loginWithWx()`
- 启动时运行全部健康检查（网络、登录、存储、服务器可达性）
- 全局错误处理：`onError` 和 `onUnhandledRejection` 统一接入 errorMonitor

#### 1.2 全局状态管理（store.ts）

- 事件驱动模式：`on()` 订阅事件，`emit()` 触发事件，返回取消函数
- 6 种事件：USER_CHANGED、STUDY_ROOMS_CHANGED、RESERVATIONS_CHANGED、CHECKIN_CHANGED、ANNOUNCEMENTS_CHANGED、CARDS_CHANGED
- 状态覆盖：user、studyRooms、seats、reservations、checkIn、announcements、rules、cards
- 所有状态变更同步写入 cache 持久化

#### 1.3 缓存服务（cache.ts）

- TTL 过期机制：每种数据独立过期时间（用户 24h、自习室 30min、座位 5min、签到 1min 等）
- 前缀管理：所有 key 以 `istudyspot_` 前缀
- 泛型 get/set 方法，类型安全

#### 1.4 网络请求层（request.ts）

- 统一请求方法：get/post/put/delete
- SSE 流式传输支持：`requestSSE()` 方法，使用 `enableChunked` + `onChunkReceived` 实现
- 手动 SSE 解析：解析 `event:` / `data:` 行，支持 ArrayBuffer 解码
- 请求指标集成：每个请求自动记录到 metrics 服务

#### 1.5 导航管理（navigation.ts）

- 5 个目标页：home、rules、profile、reservation、study
- 智能导航：Tab 页用 `switchTab`，普通页用 `navigateTo`，支持清栈和替换
- 便捷方法：`navigateFromReservationToStudy()`、`navigateFromReservationToHome()`、`navigateFromStudyToHome()`

#### 1.6 Mock 系统（mock.ts + mock-data.ts）

- 默认启用 Mock 模式（`ENABLE_MOCK = true`），覆盖全部 API 端点
- SSE 流式模拟：`requestStream()` 模拟卡片流式生成（init -> 逐字 text -> complete）
- 业务逻辑模拟：预约冲突检测、签到状态管理、座位状态同步
- 完整数据集：2 个用户、4 个自习室、自动生成座位、预约记录、签到记录、4 条公告、4 条规则、6 张初始卡片

#### 1.7 日志与监控

- 日志系统（logger.ts）：4 级别（debug/info/warn/error），持久化到 Storage，最大 200 条
- 错误监控（error-monitor.ts）：3 种错误类型（JS 异常、Promise 拒绝、运行时错误），错误去重，持久化最大 50 条
- 指标统计（metrics.ts）：请求指标（URL、方法、耗时、成功/失败）+ 页面加载指标，持久化最大 150 条，提供摘要统计

### 二、页面开发

#### 2.1 首页（pages/home/）

- Hero 区域：问候语 + 用户座右铭 + 叶子装饰
- 主功能卡片（绿色渐变）：选座预约/重新预约/进入学习
- 次功能卡片组：签到/学习、扫码签到
- 预约信息卡片：状态标签（未预约/已预约/学习中）+ 台灯装饰 + 自习室/座位/时间信息 + 本周学习时长
- **用户状态机**：`none` -> `reserved` -> `studying` -> `checked_out`，根据签到和预约状态自动判断
- 扫码签到：解析二维码（支持 URL/JSON/斜杠分隔三种格式），匹配预约后签到
- Store 事件订阅：监听 `CHECKIN_CHANGED` 和 `RESERVATIONS_CHANGED` 实时更新

#### 2.2 卡片收藏页（pages/cards/）

- 双列瀑布流布局，每张卡片显示：稀有度徽章、配图、Markdown 内容、时间/时长
- 三种排序：默认/稀有度/获取时间
- 点击卡片弹出 card-popup 查看详情
- 内置"卡片稀有度说明"文档弹窗（Markdown 渲染）
- Store 事件监听 `CARDS_CHANGED` 实时更新

#### 2.3 个人中心（pages/profile/）

- 个人信息卡片：头像（可点击更换）、昵称（可点击修改）、统计数据（累计学习时长/预约次数/信用分）
- 座右铭区域：显示 + 修改按钮
- 修改昵称：调用 `userApi.updateProfile()` 更新
- 更换头像：`wx.chooseMedia` 选择图片，先本地预览，再调用 `userApi.uploadAvatar()` 上传

#### 2.4 规则公告页（pages/rules/）

- 规则卡片：自习室公约，按条目展示（标题 + 内容行）
- 公告通知区域：卡片式布局，含标题、优先级标签（紧急/重要/普通）、内容、作者和发布时间
- 规则内容按换行符分割为多行展示
- 公告优先级映射：high->紧急、medium->重要、low->普通

#### 2.5 选座页（pages/seat-selection/）

- 日期时间选择器：日期、开始时间、结束时间（8:00-21:00，5 分钟间隔）
- 座位地图：分组布局（左右两组 + 过道），行列标号，不同状态颜色区分
- 图例：可用/占用/已预约/已选
- 已选座位信息：座位号、类型（普通/VIP/静音）、设施
- 确认预约按钮（固定底部）
- 使用 `SeatLayoutUtil` 工具类将座位数据转换为分组布局
- 预约规则校验：加载 `reservationRules`，校验最大时长
- 即时模式：支持 `immediate` 参数，预约后直接签到并跳转学习页
- 预选座位：支持 `seatId` 参数，从其他页面跳转时预选座位

#### 2.6 学习状态页（pages/study-status/）

- 圆形图片区域（长按结束学习）
- 状态卡片：标题 + 计时器（H:MM:SS）+ 座右铭
- 结束学习流程：弹窗确认 -> 停止计时 -> 调用签退 API -> 触发卡片流式生成
- **卡片流式生成**：使用 `cardApi.generateCardStream()` SSE 接口，实时显示稀有度和逐字 Markdown
- 流式 UI：弹窗先显示，内容逐步填充，带闪烁光标动画

### 三、组件封装

#### 3.1 card-popup（卡片弹窗组件）

- 支持两种模式：流式模式（`streaming=true`）和完整卡片模式
- 流式模式：实时显示稀有度、主题分类、逐字 Markdown 内容，带闪烁光标动画
- 完整模式：渲染卡片 Markdown、配图、创建时间、学习时长
- 6 种稀有度独立视觉：N(白)/R(绿)/SR(蓝)/SSR(紫光呼吸)/UR(金色暗色主题)/LR(红色脉动)
- 弹窗动画：`cubic-bezier(0.34, 1.56, 0.64, 1)` 弹性缩放

#### 3.2 markdown-card（Markdown 卡片组件）

- 接收 HTML 字符串，使用 `mp-html` 组件渲染
- 白色背景、圆角、阴影的卡片样式

#### 3.3 mp-html（富文本渲染组件）

- 第三方 HTML 渲染引擎，用于将 markdown-it 生成的 HTML 渲染为小程序视图
- 包含 HTML 解析器（parser.js）和节点渲染子组件

#### 3.4 navigation-bar（自定义导航栏组件）

- 自动获取胶囊按钮位置，计算内边距
- 适配 iOS/Android/开发者工具的安全区域
- 支持返回按钮、首页按钮、加载状态
- 支持多 slot（left/center/right）
- 显示/隐藏动画

#### 3.5 tab-bar（自定义底部标签栏组件）

- 4 个 Tab：规则公告、首页、卡片、个人中心
- 选中状态：图标背景变浅绿，文字变绿加粗
- 图标使用 filled/outline 两套 PNG 图片
- 安全区域适配

### 四、服务层（API 对接）

封装了完整的网络请求层，所有服务遵循统一模式：缓存优先 -> Mock 支持 -> API 调用 -> Store 同步。

| 服务 | 文件 | 核心方法 |
|------|------|----------|
| 认证 | auth.ts | `wxLogin()`、`loginWithWx()` |
| 用户 | user.ts | `getCurrentUser()`、`updateProfile()`、`uploadAvatar()`、`getUserHome()` |
| 自习室 | studyroom.ts | `getStudyRooms()`、`getStudyRoomDetail()` |
| 座位 | seat.ts | `getSeats()`、`getSeatDetail()` |
| 预约 | reservation.ts | `createReservation()`、`getMyReservations()`、`cancelReservation()`、`getReservationRules()` |
| 签到 | checkin.ts | `checkIn()`、`checkOut()`、`getMyCheckInRecords()`、`getCurrentCheckInStatus()` |
| 公告 | announcement.ts | `getAnnouncements()`、`getAnnouncementDetail()` |
| 规则 | rule.ts | `getRules()`、`getRuleDetail()` |
| 卡片 | card.ts | `generateCard()`、`generateCardStream()`、`getCardDetail()`、`getCardList()` |
| 健康检查 | health-check.ts | `runAllChecks()`、`getQuickNetworkStatus()` |
| 指标统计 | metrics.ts | 请求指标 + 页面加载指标 + 摘要统计 |

### 五、工具层

| 工具 | 文件 | 说明 |
|------|------|------|
| 座位布局 | seat-layout.ts | `createSeatLayout()`、`splitIntoGroups()`、`calculateSeatStats()`、`isSeatSelectable()` |
| Markdown 引擎 | markdown-engine.ts | 基于 markdown-it，配置 html=false、linkify=true、typographer=true、breaks=true |
| Markdown 协议 | markdown-contract.ts | 项目级 Markdown 渲染规则，标题限制、文本长度、分割协议 |
| 类型定义 | typings/api.ts | 完整 TypeScript 类型定义，涵盖所有业务实体和 SSE 事件类型 |

## 遇到的问题和解决

1. **SSE 流式传输在小程序中的实现**：小程序不支持原生 EventSource，通过 `wx.request` 的 `enableChunked` + `onChunkReceived` 实现流式接收，手动解析 ArrayBuffer 中的 SSE 事件。
2. **Skyline 渲染引擎兼容性**：Skyline 对部分 CSS 属性支持有限，需要针对性地调整样式写法，如动画属性需要使用 `worklet` 方式。
3. **自定义 TabBar 与系统 TabBar 的冲突**：使用自定义 TabBar 组件替代系统 TabBar，需要在 app.json 中声明 `"custom": true` 并自行管理选中状态和页面跳转逻辑。
4. **Mock 模式与真实 API 的切换**：设计统一的 Mock 管理器，所有服务层方法内置 Mock 判断，通过 `ENABLE_MOCK` 开关一键切换，确保前后端可独立开发。

## 心得体会

本次前端开发从零搭建了完整的微信小程序架构，涵盖了状态管理、缓存策略、网络请求层、Mock 系统、日志监控等基础设施，以及 6 个业务页面和 5 个核心组件。最大的收获是在 SSE 流式传输方面的探索——小程序环境对流式数据的支持有限，需要从底层手动解析 ArrayBuffer，这让我对 HTTP 协议和小程序底层机制有了更深的理解。卡片系统的视觉差异化设计（6 级稀有度 × 7 大主题）也是一个有趣的挑战，需要在有限的样式能力下实现丰富的视觉效果。
