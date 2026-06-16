# 小程序 vs 后端 API 对齐分析

> 基于全量代码阅读，逐模块对比小程序 `miniprogram/mp-user/` 和后端 `istudyspot-backend/` 的 API 语义差异。

---

## 一、响应包装结构差异

| 层级 | 小程序期望 | 后端实际 |
|------|-----------|---------|
| 通用接口 | `{ code, message, data, timestamp }` | `Result<T>` → `{ code, message, data, timestamp }` ✅ |
| Card 接口 | `{ code, message, data: Card }` | `{ success, message, card }` ❌ 不走 Result 包装 |

**问题**：CardController 直接返回 `ResponseEntity<Map>`，字段名用 `success`(Boolean) 而非 `code`(Integer)，且 card 数据放在 `card` 字段而非 `data`。小程序的 `request.ts` 统一从 `data` 取数据，Card 接口会取到 `null`。

---

## 二、逐模块对齐分析

### 2.1 认证模块 — ✅ 基本对齐

| 端点 | 小程序 | 后端 | 状态 |
|------|--------|------|------|
| POST `/auth/login` | `{ username, password }` → `{ token, refreshToken, user }` | LoginDTO → `{ token, refreshToken, user }` | ✅ |
| POST `/auth/register` | `{ username, password, nickname, phone, studentId }` → `{ userId }` | RegisterDTO → `{ userId }` | ✅ |
| POST `/auth/refresh` | `{ refreshToken }` → `{ token, refreshToken }` | 同左 | ✅ |
| POST `/auth/logout` | → null | → null | ✅ |

---

### 2.2 用户模块 — ⚠️ 字段差异

**GET `/users/me`**

| 字段 | 小程序期望 | 后端实际 | 状态 |
|------|-----------|---------|------|
| `id` | string | String (Long→String) | ✅ |
| `username` | string | String | ✅ |
| `nickname` | string | String | ✅ |
| `avatar` | string | String | ✅ |
| `phone` | string | String | ✅ |
| `email` | string | String | ✅ |
| `studentId` | string | String | ✅ |
| `creditScore` | number | Integer | ✅ |
| `status` | `'active' \| 'banned'` | Integer (0/1) | ❌ 类型不一致 |
| `createdAt` | string (ISO 8601) | **未返回** | ❌ 缺失 |
| `updatedAt` | string (ISO 8601) | **未返回** | ❌ 缺失 |
| — | — | `balance` (BigDecimal) | ⚠️ 小程序无此字段 |
| — | — | `points` (Integer) | ⚠️ 小程序无此字段 |
| — | — | `violationCount` (Integer) | ⚠️ 小程序无此字段 |
| — | — | `lastLoginTime` | ⚠️ 小程序无此字段 |

**问题**：
1. `status` 类型冲突：小程序期望字符串 `'active'/'banned'`，后端返回 Integer
2. 小程序期望 `createdAt`/`updatedAt`，后端 Controller 手动构建 Map 时未包含
3. 后端返回了小程序未定义的 `balance`/`points`/`violationCount`/`lastLoginTime`

---

### 2.3 自习室模块 — ❌ 严重不对齐

**GET `/studyrooms` & GET `/studyrooms/{id}`**

| 字段 | 小程序期望 | 后端实际 | 状态 |
|------|-----------|---------|------|
| `id` | string | Long | ⚠️ 类型 |
| `name` | string | String | ✅ |
| `description` | string | String | ✅ |
| `location` | string | **`address`** | ❌ 字段名不同 |
| `floor` | number | **不存在** | ❌ 缺失 |
| `capacity` | number | **不存在** | ❌ 缺失 |
| `openTime` | string "08:00" | LocalTime "08:00" | ✅ 格式兼容 |
| `closeTime` | string "22:00" | LocalTime "22:00" | ✅ 格式兼容 |
| `facilities` | string[] | **不存在** | ❌ 缺失 |
| `image` | string | **`imageUrl`** | ❌ 字段名不同 |
| `status` | `'open' \| 'closed' \| 'maintenance'` | Integer (0/1) | ❌ 类型+值不一致 |

**StudyRoomDetail 额外字段**：

| 字段 | 小程序期望 | 后端实际 | 状态 |
|------|-----------|---------|------|
| `rules` | Rule[] | **不包含** | ❌ 缺失 |
| `createdAt` | string | **未返回** | ❌ 缺失 |
| `updatedAt` | string | **未返回** | ❌ 缺失 |

**问题**：
1. `location` vs `address`：字段名完全不同，小程序读 `location` 会得到 `undefined`
2. `image` vs `imageUrl`：字段名不同
3. `floor`/`capacity`/`facilities`：小程序期望但后端不存在
4. `status` 类型冲突：字符串枚举 vs Integer
5. 详情接口不包含 `rules`，小程序 StudyRoomDetail 扩展了 rules 但后端不返回

---

### 2.4 座位模块 — ❌ 严重不对齐

**GET `/studyrooms/{studyRoomId}/seats` & GET `/seats/{id}`**

| 字段 | 小程序期望 | 后端实际 | 状态 |
|------|-----------|---------|------|
| `id` | string | Long | ⚠️ 类型 |
| `studyRoomId` | string | **`roomId`** | ❌ 字段名不同 |
| `row` | number | **`rowNum`** | ❌ 字段名不同 |
| `col` | number | **`colNum`** | ❌ 字段名不同 |
| `seatNumber` | string | String | ✅ |
| `type` | `'normal' \| 'vip' \| 'quiet'` | **`seatType`** Integer (1/2/3/4) | ❌ 字段名+类型+值全不同 |
| `status` | `'available' \| 'occupied' \| 'reserved' \| 'maintenance'` | `'available' \| 'booked' \| 'in_use' \| 'unavailable'` | ❌ 枚举值不同 |
| `facilities` | string[] | **不存在** | ❌ 缺失 |
| `lastUsedAt` | string | **不存在** | ❌ 缺失 |
| — | — | `hasPower` (Integer) | ⚠️ 小程序无 |
| — | — | `hasLamp` (Integer) | ⚠️ 小程序无 |
| — | — | `isWindow` (Integer) | ⚠️ 小程序无 |
| — | — | `pricePerHour` (BigDecimal) | ⚠️ 小程序无 |
| — | — | `description` (String) | ⚠️ 小程序无 |
| — | — | `areaId` (Long) | ⚠️ 小程序无 |

**座位状态值映射表**：

| 小程序期望 | 后端实际 | 含义 |
|-----------|---------|------|
| `'available'` | `'available'` | 空闲 ✅ |
| `'occupied'` | `'in_use'` | 使用中 ❌ |
| `'reserved'` | `'booked'` | 已预订 ❌ |
| `'maintenance'` | `'unavailable'` | 不可用 ❌ |

**座位类型值映射表**：

| 小程序期望 | 后端实际 | 含义 |
|-----------|---------|------|
| `'normal'` | `seatType=1` | 普通 ❌ |
| `'vip'` | `seatType=2` | VIP/沙发 ❌ |
| `'quiet'` | `seatType=3` | 隔间 ❌ |
| _(无)_ | `seatType=4` | 包厢 |

**缺失端点**：小程序没有 `/studyrooms/{id}/seat-layout`（复杂布局接口）

---

### 2.5 预约模块 — ❌ 严重不对齐

**POST `/reservations`**

| 请求字段 | 小程序发送 | 后端期望 | 状态 |
|---------|-----------|---------|------|
| `studyRoomId` | ✅ | Long | ✅ |
| `seatId` | ✅ | Long | ✅ |
| `startTime` | ISO 8601 | `@JsonFormat("yyyy-MM-dd HH:mm")` | ❌ 格式不同 |
| `endTime` | ISO 8601 | `@JsonFormat("yyyy-MM-dd HH:mm")` | ❌ 格式不同 |
| `bookingType` | **不发送** | String (可选) | ⚠️ |

**Reservation 响应字段**：

| 字段 | 小程序期望 | 后端实际 | 状态 |
|------|-----------|---------|------|
| `id` | string | String (Long→String) | ✅ |
| `userId` | string | String | ✅ |
| `studyRoomId` | string | String | ✅ |
| `seatId` | string | String | ✅ |
| `startTime` | string | `startTime` (@JsonProperty) | ✅ |
| `endTime` | string | `endTime` (@JsonProperty) | ✅ |
| `status` | `'pending' \| 'confirmed' \| 'checked_in' \| 'completed' \| 'cancelled' \| 'expired'` | `'pending' \| 'paid' \| 'in_use' \| 'completed' \| 'cancelled' \| 'expired'` | ❌ 枚举值不同 |
| `checkInTime` | string \| null | **`checkinTime`** | ❌ 大小写不同 |
| `checkOutTime` | string \| null | **`checkoutTime`** | ❌ 大小写不同 |
| `createdAt` | string | `createdAt` (@JsonProperty from createTime) | ✅ |
| `updatedAt` | string | `updatedAt` | ✅ |
| — | — | `orderNo` | ⚠️ 小程序无 |
| — | — | `studyRoomName` | ⚠️ 小程序无 |
| — | — | `roomName` | ⚠️ 小程序无 |
| — | — | `seatPosition` | ⚠️ 小程序无 |
| — | — | `seatNumber` | ⚠️ 小程序无 |
| — | — | `totalPrice` | ⚠️ 小程序无 |
| — | — | `totalAmount` | ⚠️ 小程序无 |

**预约状态值映射表**：

| 小程序期望 | 后端实际 | 含义 |
|-----------|---------|------|
| `'pending'` | `'pending'` | 待支付 ✅ |
| `'confirmed'` | `'paid'` | 已确认/已支付 ❌ |
| `'checked_in'` | `'in_use'` | 已签到 ❌ |
| `'completed'` | `'completed'` | 已完成 ✅ |
| `'cancelled'` | `'cancelled'` | 已取消 ✅ |
| `'expired'` | `'expired'` | 已过期 ✅ |

**GET `/reservations/rules`**：

| 字段 | 小程序期望 | 后端实际 | 状态 |
|------|-----------|---------|------|
| `maxAdvanceDays` | number | 需确认 | ⚠️ |
| `maxDailyReservations` | number | 需确认 | ⚠️ |
| `maxDurationHours` | number | 需确认 | ⚠️ |
| `minDurationMinutes` | number | 需确认 | ⚠️ |
| `cancellationDeadlineMinutes` | number | 需确认 | ⚠️ |
| `noShowPenalty` | number | 需确认 | ⚠️ |

**后端有但小程序没有的端点**：
- POST `/reservations/{id}/pay` — 支付
- POST `/reservations/{id}/renew` — 续约

---

### 2.6 签到/签退模块 — ❌ 严重不对齐

**POST `/checkin`**

| 请求字段 | 小程序发送 | 后端期望 | 状态 |
|---------|-----------|---------|------|
| `reservationId` | ✅ | Long | ✅ |
| `seatId` | ✅ | String (被当作 checkinCode) | ⚠️ 语义不同 |

| 响应字段 | 小程序期望 | 后端实际 | 状态 |
|---------|-----------|---------|------|
| `checkInRecordId` | string | **`id`** | ❌ 字段名不同 |
| `checkInTime` | string | **`checkinTime`** | ❌ 大小写不同 |
| `reservationId` | string | **不返回** | ❌ 缺失 |
| `seatId` | string | **不返回** | ❌ 缺失 |
| — | — | `status` | ⚠️ 小程序不期望 |

**POST `/checkout`**

| 响应字段 | 小程序期望 | 后端实际 | 状态 |
|---------|-----------|---------|------|
| `checkOutTime` | string | **`checkoutTime`** | ❌ 大小写不同 |
| `duration` | number (分钟) | **`actualDuration`** | ❌ 字段名不同 |
| — | — | `id` | ⚠️ 小程序不期望 |
| — | — | `actualPrice` | ⚠️ 小程序不期望 |
| — | — | `status` | ⚠️ 小程序不期望 |

**GET `/checkin/records`**

| 字段 | 小程序期望 | 后端实际 | 状态 |
|------|-----------|---------|------|
| 列表字段名 | `list` | **`records`** | ❌ 字段名不同 |
| 记录 `userId` | string | **不返回** | ❌ 缺失 |
| 记录 `reservationId` | string | **不返回** | ❌ 缺失 |
| 记录 `studyRoomId` | string | **不返回** | ❌ 缺失 |
| 记录 `seatId` | string | **不返回** | ❌ 缺失 |
| 记录 `checkInTime` | string | **不返回** | ❌ 缺失 |
| 记录 `checkOutTime` | string | **不返回** | ❌ 缺失 |
| 记录 `duration` | number | `duration` | ✅ |
| — | — | `seatPosition` | ⚠️ 小程序无 |
| — | — | `studyRoomName` | ⚠️ 小程序无 |
| — | — | `startTime`/`endTime` | ⚠️ 小程序无 |
| 统计字段 | **不期望** | `totalHours`/`weekHours`/`monthHours`/`streak`/`avgDuration`/`favoriteSeat`/`peakTime` | ⚠️ 额外返回 |

**GET `/checkin/current`**

| 字段 | 小程序期望 | 后端实际 | 状态 |
|------|-----------|---------|------|
| `isCheckedIn` | boolean | Boolean | ✅ |
| `checkInRecord.id` | string | String | ✅ |
| `checkInRecord.userId` | string | **不返回** | ❌ |
| `checkInRecord.reservationId` | string | **不返回** | ❌ |
| `checkInRecord.studyRoomId` | string | **不返回** | ❌ |
| `checkInRecord.seatId` | string | **不返回** | ❌ |
| `checkInRecord.checkInTime` | string | **不返回** | ❌ |
| `checkInRecord.checkOutTime` | string | **不返回** | ❌ |
| `checkInRecord.duration` | number | **不返回** | ❌ |
| `checkInRecord.status` | `'active' \| 'completed'` | String | ✅ |
| — | — | `seatPosition` | ⚠️ 小程序无 |
| — | — | `studyRoomName` | ⚠️ 小程序无 |
| — | — | `startTime`/`endTime` | ⚠️ 小程序无 |

---

### 2.7 公告模块 — ⚠️ 部分对齐

**GET `/announcements` & GET `/announcements/{id}`**

| 字段 | 小程序期望 | 后端实际 | 状态 |
|------|-----------|---------|------|
| `id` | string | Long | ⚠️ 类型 |
| `title` | string | String | ✅ |
| `content` | string | String | ✅ |
| `type` | `'notice' \| 'maintenance' \| 'event' \| 'emergency'` | String | ⚠️ 需确认值 |
| `priority` | `'low' \| 'medium' \| 'high'` | String | ⚠️ 需确认值 |
| `publishTime` | string | String | ✅ |
| `expireTime` | string \| null | String | ✅ |
| `author` | string | String | ✅ |
| `status` | `'published' \| 'draft' \| 'archived'` | String | ⚠️ 需确认值 |

**注意**：后端公告接口当前为 TODO 硬编码实现，未接入数据库。

---

### 2.8 规则模块 — ⚠️ 部分对齐

**GET `/rules` & GET `/rules/{id}`**

| 字段 | 小程序期望 | 后端实际 | 状态 |
|------|-----------|---------|------|
| `id` | string | Long | ⚠️ 类型 |
| `studyRoomId` | string \| null | **不返回** | ❌ 缺失 |
| `category` | `'booking' \| 'usage' \| 'penalty' \| 'general'` | String | ✅ |
| `title` | string | String | ✅ |
| `content` | string | String | ✅ |
| `priority` | number | Integer | ✅ |
| `createdAt` | string | **不返回** | ❌ 缺失 |
| `updatedAt` | string | **不返回** | ❌ 缺失 |
| — | — | `categoryLabel` | ⚠️ 小程序无 |
| — | — | `type` | ⚠️ 小程序无 |

---

### 2.9 卡片模块 — ❌ 响应结构不对齐

**POST `/card/generate`**

| 请求字段 | 小程序发送 | 后端期望 | 状态 |
|---------|-----------|---------|------|
| `userID` | string | String | ✅ |
| `studyDuration` | number | Integer | ✅ |

| 响应结构 | 小程序期望 | 后端实际 | 状态 |
|---------|-----------|---------|------|
| 顶层 | `data: Card` | `{ success, message, card: Card }` | ❌ 包装不同 |
| `card.cardID` | string | `cardId` | ❌ 大小写不同 |
| `card.imageURL` | string | `imageURL` | ✅ |
| `card.createTime` | string | String | ✅ |
| `card.rarity` | CardRarity | String | ✅ |
| `card.themeCategory` | ThemeCategory | String | ✅ |
| `card.markdown` | string | String | ✅ |
| `card.studyDuration` | number | Integer | ✅ |

**GET `/card/detail` & GET `/card/list`**

同样存在响应包装差异：后端用 `{ success, card/list }` 而非 `Result.data`。

---

## 三、全局性问题汇总

### 3.1 ID 类型不一致

| 模块 | 小程序 | 后端 |
|------|--------|------|
| 所有 `id` 字段 | `string` | `Long`（部分 Service 层转为 String） |
| Card `userID`/`cardID` | 大写 ID | 后端 Card Entity 也是 `userId`/`cardId`（小写），但 Controller 输出时用 `userID`/`cardID` |

### 3.2 时间格式不一致

| 场景 | 小程序期望 | 后端实际 |
|------|-----------|---------|
| 通用时间 | ISO 8601 (`2024-01-01T08:00:00`) | `yyyy-MM-dd'T'HH:mm:ss`（Jackson 全局配置）✅ |
| 预约创建请求 | ISO 8601 | `yyyy-MM-dd HH:mm`（@JsonFormat）❌ |
| Card.createTime | `yyyy-MM-dd HH:mm:ss` | `yyyy-MM-dd HH:mm:ss` ✅ |
| Service 层手动格式化 | — | `yyyy-MM-dd HH:mm:ss`（非 ISO）❌ |

### 3.3 状态枚举值体系冲突

这是最核心的对齐问题。小程序和后端使用了完全不同的状态词汇：

| 模块 | 小程序 | 后端 |
|------|--------|------|
| User.status | `'active' / 'banned'` | `Integer (0/1)` |
| StudyRoom.status | `'open' / 'closed' / 'maintenance'` | `Integer (0/1)` |
| Seat.type | `'normal' / 'vip' / 'quiet'` | `seatType: Integer (1/2/3/4)` |
| Seat.status | `'available' / 'occupied' / 'reserved' / 'maintenance'` | `'available' / 'booked' / 'in_use' / 'unavailable'` |
| Reservation.status | `'pending' / 'confirmed' / 'checked_in' / ...` | `'pending' / 'paid' / 'in_use' / ...` |

### 3.4 字段命名风格不一致

| 小程序 | 后端 | 模块 |
|--------|------|------|
| `location` | `address` | StudyRoom |
| `image` | `imageUrl` | StudyRoom |
| `studyRoomId` | `roomId` | Seat |
| `row` / `col` | `rowNum` / `colNum` | Seat |
| `type` | `seatType` | Seat |
| `checkInTime` | `checkinTime` | Reservation/CheckIn |
| `checkOutTime` | `checkoutTime` | Reservation/CheckIn |
| `checkInRecordId` | `id` | CheckIn |
| `duration` | `actualDuration` | CheckOut |
| `list` | `records` | CheckIn Records |

---

## 四、缺失端点

### 小程序有但后端未完整实现的

| 端点 | 状态 |
|------|------|
| GET `/announcements` | TODO 硬编码 |
| GET `/announcements/{id}` | TODO 硬编码 |

### 后端有但小程序未调用的

| 端点 | 说明 |
|------|------|
| GET `/studyrooms/{id}/seat-layout` | 复杂座位布局（Android 已对接） |
| POST `/reservations/{id}/pay` | 支付 |
| POST `/reservations/{id}/renew` | 续约 |
| POST `/payments` | 创建支付 |
| GET `/payments/{id}` | 查询支付 |
| POST `/payments/callback` | 支付回调 |
| GET/POST/PUT/DELETE `/todos/*` | 待办事项 |
| GET `/achievements` | 成就系统 |
| GET `/violations` | 违规记录 |
| POST `/violations/{id}/appeal` | 违规申诉 |
| GET `/characters` | AI 角色 |
| POST `/chat` / `/chat/stream` | AI 聊天 |
| `/customer-service/*` | 客服系统 |
| `/agent/*` | Agent 系统 |
| GET `/studyrooms/{id}/statistics` | 自习室统计 |
| POST `/card/generate/stream` | 卡片流式生成 |
| GET `/card/image/**` | 卡片图片 |

---

## 五、修复优先级建议

### P0 — 不修复则小程序完全无法工作

| # | 问题 | 影响范围 | 建议修复方向 |
|---|------|---------|-------------|
| 1 | Card 接口不走 Result 包装 | 卡片功能全挂 | 后端 CardController 改用 Result 包装，或小程序 request.ts 增加 Card 响应适配 |
| 2 | Seat 字段名全错 (`studyRoomId`→`roomId`, `row`→`rowNum` 等) | 选座页无法渲染 | 统一字段名，建议后端加 @JsonProperty 别名 |
| 3 | Seat 状态枚举不同 (`occupied`→`in_use`, `reserved`→`booked`) | 座位颜色/状态显示错误 | 统一状态值，建议对齐到后端的 `available/booked/in_use/unavailable` |
| 4 | Reservation 状态枚举不同 (`confirmed`→`paid`, `checked_in`→`in_use`) | 预约列表状态显示错误 | 统一状态值 |
| 5 | `checkInTime`/`checkOutTime` 大小写 | 签到时间显示为 null | 统一为 `checkinTime`/`checkoutTime` |

### P1 — 核心功能受影响

| # | 问题 | 影响范围 | 建议修复方向 |
|---|------|---------|-------------|
| 6 | StudyRoom `location`→`address`, `image`→`imageUrl` | 自习室列表/详情显示异常 | 后端加 @JsonProperty 别名或前端改字段名 |
| 7 | 预约创建时间格式 ISO vs `yyyy-MM-dd HH:mm` | 创建预约可能失败 | 统一为 ISO 8601 或前端适配 |
| 8 | CheckIn 响应 `checkInRecordId`→`id`, `duration`→`actualDuration` | 签到/签退后数据丢失 | 统一字段名 |
| 9 | CheckIn Records `list`→`records` | 签到记录列表为空 | 统一为 `list` |
| 10 | StudyRoom `status` 字符串 vs Integer | 状态判断失败 | 后端输出字符串枚举 |

### P2 — 功能缺失但不阻塞

| # | 问题 | 影响范围 | 建议修复方向 |
|---|------|---------|-------------|
| 11 | StudyRoom 缺 `floor`/`capacity`/`facilities` | 详情页信息不全 | 后端补字段或前端移除 |
| 12 | StudyRoom Detail 不含 `rules` | 需额外请求 | 后端在详情接口中包含 rules |
| 13 | Seat 缺 `facilities`/`lastUsedAt` | 座位特性展示不全 | 后端补字段或前端移除 |
| 14 | CheckIn Current/Records 记录字段严重缺失 | 签到状态页信息不全 | 后端补全字段 |
| 15 | User 缺 `createdAt`/`updatedAt` | 个人中心信息不全 | 后端在 Map 中加入时间字段 |
| 16 | Rules 不返回 `studyRoomId`/`createdAt`/`updatedAt` | 规则关联信息不全 | 后端补字段 |

### P3 — 可后续处理

| # | 问题 | 影响范围 | 建议修复方向 |
|---|------|---------|-------------|
| 17 | User.status Integer vs 字符串 | 状态显示可能异常 | 后端输出映射为字符串 |
| 18 | Seat.type 字符串 vs seatType Integer | 座位类型筛选失败 | 后端输出映射为字符串 |
| 19 | Card `cardID` 大小写 | 卡片 ID 读取可能失败 | 统一为 `cardId` |
| 20 | 公告接口 TODO 硬编码 | 公告功能不可用 | 后端实现数据库接入 |
