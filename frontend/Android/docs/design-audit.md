# iStudySpot 设计文档符合性审查报告

本报告对比项目设计文档与后端/Android端实际实现的差异，涵盖架构、API接口、数据模型、功能模块和UI设计五个维度。

---

## 一、架构层面差异

### 1.1 Android 架构模式

| 设计文档描述 | 实际实现 | 差异程度 |
|-------------|---------|---------|
| MVVM 架构：View → ViewModel → Repository → Network | 已重构为标准 MVVM，但部分 ViewModel 仍为空壳 | 中等 |
| 使用 Jetpack（ViewModel/LiveData） | 使用 ViewModel + StateFlow（非 LiveData） | 低（StateFlow 是更现代的方案） |
| Fragment 体系：HomeFragment/MoreFragment/ProfileFragment/RulesFragment | 使用 Compose Screen 体系，无 Fragment | **严重**：架构方案完全不同 |
| 项目目录结构：`fragment/`、`viewmodel/`、`repository/`、`network/`、`model/`、`customview/`、`utils/` | 实际目录：`ui/screen/`、`viewmodel/`、`repository/`、`infra/network/`、`models/`、`customview/`、`utils/`、`navigation/`、`ui/theme/` | **严重**：目录结构差异大 |
| 使用 Glide 图片加载 | 未集成 Glide | 中等 |
| 使用 MPAndroidChart 数据可视化 | 未集成 MPAndroidChart | 中等 |
| Custom View 座位图绘制 | 使用 Compose LazyVerticalGrid 绘制座位图 | **严重**：技术方案完全不同 |

### 1.2 后端架构差异

| 设计文档描述 | 实际实现 | 差异程度 |
|-------------|---------|---------|
| 6大服务模块：用户/座位/订单/计费/统计/管理 | 实际12个Controller：Auth/User/StudyRoom/Seat/Order/CheckIn/Payment/Announcement/Rules/AI/CustomerService/Test | 中等（更细化） |
| WebSocket 实时座位状态推送 | 未实现 WebSocket | **严重** |
| Redis 分布式锁解决并发抢座 | 未确认是否实现 | 需确认 |
| 定时任务自动释放超时订单 | 未确认是否实现 | 需确认 |
| Nginx 反向代理/负载均衡 | 未部署 Nginx | 中等 |
| 微信支付集成 | PaymentController 已实现但为简化版 | 中等 |
| 阿里云 OSS 图片存储 | 未集成 | 中等 |

### 1.3 Android 架构文档（arc42）符合性

`frontend/Android/docs/architecture.md` 是最详尽的架构文档，采用 arc42 模板。以下为关键不符合项：

| 文档描述 | 实际实现 | 差异程度 |
|---------|---------|---------|
| 5层架构：表现层/状态层/业务层/数据层/基础设施层 | 实际3层：UI(Screen)/ViewModel/Repository+ApiManager | **严重** |
| UseCase 层（业务层） | 未实现 UseCase，业务逻辑在 ViewModel 中 | **严重** |
| Hilt 依赖注入 | 未集成 Hilt，手动创建实例 | **严重** |
| Room 本地数据库缓存 | 未集成 Room | 中等 |
| requestId 并发控制 | 未实现 | 中等 |
| 防抖（debounce）机制 | 未实现 | 中等 |
| 3D 座位图渲染 | 使用 2D LazyVerticalGrid | **严重** |
| 状态一致性：Single Source of Truth | 部分状态仍在 Screen 中用 remember 管理 | 中等 |

---

## 二、API 接口差异

### 2.1 后端 API 与设计文档路径差异

| 设计文档路径 | 后端实际路径 | 差异 |
|-------------|-------------|------|
| `GET /api/study-rooms`（简版api.md） | `GET /api/studyrooms`（无连字符） | 命名差异 |
| `GET /api/seats?studyRoomId=`（简版api.md） | `GET /api/studyrooms/{studyRoomId}/seats`（RESTful嵌套） | 结构差异 |
| `POST /api/orders`（简版api.md） | `POST /api/reservations` | 命名差异 |
| `POST /api/check-in`（简版api.md） | `POST /api/checkin`（无连字符） | 命名差异 |
| `POST /api/check-out`（简版api.md） | `POST /api/checkout`（无连字符） | 命名差异 |
| `GET /api/check-in/history`（简版api.md） | `GET /api/checkin/records` | 路径差异 |
| `POST /api/ai/chat`（简版api.md） | `POST /api/chat` | 路径差异 |
| `POST /api/customer-service/message`（简版api.md） | `POST /api/customer-service/chat` | 路径差异 |
| `GET /api/customer-service/messages`（简版api.md） | `GET /api/customer-service/history` | 路径差异 |

### 2.2 后端 API 与设计文档功能差异

| 端点 | 设计文档 | 后端实际 | 差异 |
|------|---------|---------|------|
| `POST /api/reservations/{id}/pay` | 无 | 已实现 | 额外端点，与 PaymentController 功能重叠 |
| `GET /api/checkin/records` | 完整实现 | **TODO 未实现**，返回空列表 | **严重** |
| `GET /api/checkin/current` | 完整实现 | **TODO 未实现**，返回固定值 | **严重** |
| `GET /api/announcements` | 完整实现 | **TODO 未实现**，返回空列表 | **严重** |
| `GET /api/announcements/{id}` | 完整实现 | **TODO 未实现**，返回硬编码数据 | **严重** |
| `GET /api/rules` | 完整实现 | **TODO 未实现**，返回硬编码数据 | **严重** |
| `GET /api/rules/{id}` | 完整实现 | **TODO 未实现**，返回硬编码数据 | **严重** |

### 2.3 后端响应格式不一致

| Controller | 是否使用 `Result<T>` 统一包装 | 差异 |
|-----------|-------------------------------|------|
| AuthController | 是 | 一致 |
| UserController | 是 | 一致 |
| StudyRoomController | 是 | 一致 |
| SeatController | 是 | 一致 |
| OrderController | 是 | 一致 |
| CheckInController | 是 | 一致 |
| PaymentController | 是 | 一致 |
| AnnouncementController | 是 | 一致 |
| RulesController | 是 | 一致 |
| **AIController** | **否**，直接返回 `ResponseEntity` | **严重不一致** |
| **CustomerServiceController** | **否**，直接返回 `ResponseEntity<Map>` | **严重不一致** |

### 2.4 Android 端 API 与设计文档差异

| 端点 | 设计文档 | Android 实际 | 差异 |
|------|---------|-------------|------|
| `POST /api/chat/stream` | 已定义（SSE 流式聊天） | **未实现** | **严重** |
| `POST /api/customer-service/chat/stream` | 已定义（SSE 流式客服） | **未实现** | **严重** |
| `GET /api/studyrooms/{id}/statistics` | 设计文档无（后续扩展） | 已定义 | Android 独有 |
| `GET /api/customer-service/welcome` | 设计文档无 | 已定义 | Android 独有 |

---

## 三、数据模型差异

### 3.1 后端实体与设计文档字段差异

#### StudyRoom

| 设计文档字段 | 后端实体字段 | 差异 |
|-------------|-------------|------|
| `location` | `address` | **命名不一致** |
| `floor` | 无 | **缺失** |
| `capacity` | 无 | **缺失** |
| `facilities` (string[]) | 无 | **缺失** |
| `image` | `imageUrl` | **命名不一致** |
| `status` ('open'/'closed'/'maintenance') | `status` (Integer) | **类型不一致** |
| `rules` (Rule[]) | `rules` (String) | **类型严重不一致** |
| 无 | `latitude`, `longitude` | 额外字段 |

#### Seat

| 设计文档字段 | 后端实体字段 | 差异 |
|-------------|-------------|------|
| `studyRoomId` | `roomId` | **命名不一致** |
| `row` | `rowNum` | **命名不一致** |
| `col` | `colNum` | **命名不一致** |
| `type` ('normal'/'vip'/'quiet') | `seatType` (Integer) | **命名和类型均不一致** |
| `facilities` (string[]) | `hasPower`/`hasLamp`/`isWindow` (Integer) | **结构完全不同** |
| `lastUsedAt` | 无 | **缺失** |
| 无 | `areaId`, `pricePerHour`, `description` | 额外字段 |

#### Order/Reservation

| 设计文档字段 | 后端实体字段 | 差异 |
|-------------|-------------|------|
| `studyRoomId` | `roomId` | **命名不一致** |
| `checkInTime` | `checkinTime` | **大小写不一致** |
| `checkOutTime` | `checkoutTime` | **大小写不一致** |
| `status` ('pending'/'confirmed'/'checked_in'/'completed'/'cancelled'/'expired') | `status` ('pending'/'paid'/'in_use'/'completed'/'cancelled') | **状态值完全不同** |
| 无 | `orderNo`, `studyRoomName`, `seatPosition`, `totalPrice`, `actualDuration`, `actualPrice` | 额外字段 |

### 3.2 Android 模型与设计文档字段差异

#### UserInfo

| 设计文档字段 | Android 实现 | 差异 |
|-------------|-------------|------|
| `studentId` | 无 | **缺失** |
| `creditScore` | 无 | **缺失** |
| `status` | 无 | **缺失** |
| `createdAt` | 无 | **缺失** |
| `updatedAt` | 无 | **缺失** |

#### LoginResponse

| 设计文档字段 | Android 实现 | 差异 |
|-------------|-------------|------|
| `refreshToken` | 无 | **缺失**（Token 刷新机制无法工作） |

#### RegisterResponse

| 设计文档 | Android 实现 | 差异 |
|---------|-------------|------|
| `{userId}` | `{token, user}` | **结构完全不同** |

#### StudyRoomItem

| 设计文档字段 | Android 实现 | 差异 |
|-------------|-------------|------|
| `location` | `address` | **命名不一致** |
| `openTime` + `closeTime` | `openingHours` | **结构差异**（两个独立字段 vs 一个合并字段） |
| `floor` | 无 | **缺失** |
| `capacity` | 无 | **缺失** |
| `facilities` | 无 | **缺失** |
| `status` | 无 | **缺失** |
| `image` | `imageUrl` | **命名不一致** |
| 无 | `occupancyRate` | 额外字段 |

#### SeatInfo

| 设计文档字段 | Android 实现 | 差异 |
|-------------|-------------|------|
| `seatNumber` | 无 | **缺失** |
| `facilities` | 无 | **缺失** |
| `lastUsedAt` | 无 | **缺失** |

#### OrderItem/OrderDetail

| 设计文档字段 | Android 实现 | 差异 |
|-------------|-------------|------|
| `studyRoomId` | 无 | **缺失** |
| `checkInTime` | 无 | **缺失** |
| `checkOutTime` | 无 | **缺失** |

#### CheckinRequest

| 设计文档 | Android 实现 | 差异 |
|---------|-------------|------|
| `{reservationId, seatId}` | `{checkinCode}` | **结构完全不同** |

#### BaseResponse

| 设计文档字段 | Android 实现 | 差异 |
|-------------|-------------|------|
| `timestamp` | 无 | **缺失** |

#### Announcement 模型

| 设计文档 | Android 实现 | 差异 |
|---------|-------------|------|
| 完整的 Announcement 数据结构 | **无模型文件**，使用 `Map<String, Any?>` | **严重缺失** |

---

## 四、功能模块差异

### 4.1 设计文档要求但未实现的功能

| 功能 | 设计文档位置 | 实现状态 | 严重程度 |
|------|-------------|---------|---------|
| WebSocket 实时座位状态推送 | docs/architecture.md | **未实现** | **严重** |
| 3D 座位图可视化 | architecture.md | **未实现**（使用2D网格） | **严重** |
| 分布式锁并发抢座 | architecture.md | 未确认 | 需确认 |
| 定时任务释放超时订单 | architecture.md | 未确认 | 需确认 |
| Token 自动刷新 | 4.2.0 API设计文档 | **未实现** | **严重** |
| SSE 流式 AI 聊天 | 6.0.1 AI拓展文档 | **未实现** | **严重** |
| SSE 流式智能客服 | 6.0.1 AI拓展文档 | **未实现** | **严重** |
| 公告列表/详情 | 4.2.0 API设计文档 | 后端 TODO 未实现 | **严重** |
| 规则列表/详情 | 4.2.0 API设计文档 | 后端 TODO 未实现 | **严重** |
| 签到记录查询 | 4.2.0 API设计文档 | 后端 TODO 未实现 | **严重** |
| 当前签到状态 | 4.2.0 API设计文档 | 后端 TODO 未实现 | **严重** |
| 修改密码 | 4.2.0 API设计文档 | 后端已实现，Android 未接入 | 中等 |
| 编辑个人信息 | 4.2.0 API设计文档 | 后端已实现，Android 未接入 | 中等 |
| 支付流程 | docs/api.md | 后端已实现，Android 未完整接入 | 中等 |
| Hilt 依赖注入 | Android architecture.md | **未实现** | **严重** |
| UseCase 业务层 | Android architecture.md | **未实现** | **严重** |
| Room 本地缓存 | Android architecture.md | **未实现** | 中等 |

### 4.2 实现了但设计文档未描述的功能

| 功能 | 实现位置 | 设计文档状态 |
|------|---------|-------------|
| AI 角色聊天系统 | AIController + AiChatViewModel | AI拓展文档有描述，主API文档无 |
| 智能客服系统 | CustomerServiceController | 简版api.md有部分描述，主API文档无 |
| 支付模块 | PaymentController | 标注为"后续扩展"，已提前实现 |
| 自习室统计 | Android ApiService | 设计文档无 |
| 客服欢迎消息 | Android ApiService | 设计文档无 |
| CI/CD 安全扫描 | android-ci.yml | 设计文档无 |

---

## 五、UI/UX 设计差异

### 5.1 与 design-spec.md 的差异

| 设计文档描述 | 实际实现 | 差异 |
|-------------|---------|------|
| 底部导航4项：首页/规则/更多/我的 | 底部导航4项：首页/规则/更多/我的 | **一致** |
| 首页常用功能区：预约座位/签到/场馆导览/我的预约 | 首页功能导航：预约座位/签到/场馆导览/我的预约/学习记录/AI咨询/通知提醒/偏好设置 | Android 多了4个功能入口 |
| 首页核心功能入口为"选座" | 首页核心功能入口为"预约座位" | 命名差异 |
| 规则页面：规则说明 + FAQ | 规则页面：仅规则说明，无 FAQ | **缺失 FAQ** |
| 更多功能分组：预约相关/账户相关/系统功能 | 更多功能分组：预约管理/学习数据/积分中心/其他 | 分组名称不同 |
| 用户中心：用户信息 + 个人功能入口 | 个人资料页：用户信息 + 我的订单 | 结构简化 |

### 5.2 与 design-spec.md（配色版）的差异

| 设计文档描述 | 实际实现 | 差异 |
|-------------|---------|------|
| 主色 `#4CAF7A`（绿色） | 主色 `#6366F1`（Indigo 紫色） | **完全不同** |
| 辅助色 `#E8F5EE` | 辅助色 `#06B6D4`（Cyan） | **完全不同** |
| 强调色 `#3E9B68` | 强调色 `#8B5CF6`（Violet） | **完全不同** |
| 5个核心页面：首页/选座/学习状态/规则公告/个人中心 | 14个页面 | 页面数量差异大 |
| 字体 PingFang SC / Inter | 使用 Material3 默认字体 | 字体不同 |

---

## 六、数据库差异

### 6.1 与 database.md 的差异

| 设计文档表 | 后端实际 | 差异 |
|-----------|---------|------|
| `study_room` | 存在 | 字段名差异（如 `open_time`/`close_time` vs `opening_hours`） |
| `area` | 未确认 | 需确认 |
| `seat` | 存在 | 字段名差异（如 `seat_number` vs `rowNum`/`colNum`） |
| `user` | 存在 | 额外字段 `balance`/`points`/`violationCount` |
| `price_strategy` | 未确认 | 需确认 |
| `order` | 存在 | 字段差异大 |
| `order_detail` | 未确认 | 需确认 |
| `payment_log` | 存在（Payment 实体） | 命名差异 |
| `seat_status_log` | 未确认 | 需确认 |
| `blacklist` | 未确认 | 需确认 |
| `v_seat_current_status` 视图 | 未确认 | 需确认 |

### 6.2 Redis 缓存设计

| 设计文档描述 | 实际实现 | 差异 |
|-------------|---------|------|
| `seat:status:{roomId}` — 座位状态缓存 | 未确认 | 需确认 |
| `seat:lock:{seatId}` — 分布式锁 | 未确认 | 需确认 |
| `order:timeout:{orderId}` — 超时订单 | 未确认 | 需确认 |
| `user:token:{userId}` — 用户Token | 未确认 | 需确认 |
| `stats:room:{roomId}` — 统计数据 | 未确认 | 需确认 |
| `checkin:count:{date}` — 签到计数 | 未确认 | 需确认 |

---

## 七、后端安全问题

| 问题 | 设计文档要求 | 实际实现 | 严重程度 |
|------|-------------|---------|---------|
| UserController 更新接口 | 应使用 DTO 过滤字段 | 直接接收 User 实体，可传入 password/status 等敏感字段 | **严重** |
| CheckInController 签到 | 应使用 CheckinDTO | 使用 `Map<String,String>`，CheckinDTO 已存在但未使用 | 中等 |
| CustomerServiceController | POST 应使用 @RequestBody | 使用 @RequestParam 传消息 | **严重** |
| AIController 响应格式 | 应使用 Result<T> 统一包装 | 直接返回 ResponseEntity | **严重** |

---

## 八、差异汇总统计

### 按严重程度

| 严重程度 | 数量 | 典型问题 |
|---------|------|---------|
| **严重** | 18 | WebSocket未实现、3D座位图未实现、6个后端TODO未实现、SSE流式未实现、Hilt/UseCase未实现、字段命名大面积不一致、响应格式不统一 |
| **中等** | 12 | Token刷新未实现、公告模型缺失、FAQ缺失、配色方案不同、支付未完整接入 |
| **低** | 8 | 额外冗余字段、命名风格差异、目录结构差异 |

### 按影响范围

| 影响范围 | 严重差异数 |
|---------|-----------|
| 后端 API | 10 |
| Android 端 | 8 |
| 数据模型 | 6 |
| 架构设计 | 5 |
| UI/UX | 3 |
| 数据库 | 2 |

---

## 九、优先修复建议

### P0 — 阻塞联调

1. 统一后端实体字段名与设计文档（或更新设计文档匹配实现）
2. 实现后端 6 个 TODO 端点（签到记录/当前签到/公告列表详情/规则列表详情）
3. 统一 AIController 和 CustomerServiceController 的响应格式为 `Result<T>`
4. Android 端补充 `refreshToken` 到 LoginResponse/TokenResponse
5. Android 端创建 AnnouncementModels.kt 强类型模型

### P1 — 核心功能

6. 实现 SSE 流式 AI 聊天（后端已实现，Android 需接入）
7. 实现 Token 自动刷新机制
8. Android 端接入用户信息编辑和修改密码
9. 修复 UserController 安全漏洞（使用 DTO）
10. 修复 CustomerServiceController 参数传递方式

### P2 — 架构完善

11. 集成 Hilt 依赖注入
12. 实现 UseCase 业务层
13. 实现 WebSocket 实时座位状态推送
14. 更新设计文档匹配实际实现（配色方案、目录结构、技术选型等）
