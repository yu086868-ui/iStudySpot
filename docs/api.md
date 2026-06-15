# API接口文档

## 1. 认证接口

### 1.1 登录
- **路径**: `/api/auth/login`
- **方法**: `POST`
- **参数**:
  - `username`: 用户名
  - `password`: 密码
- **返回**: 包含token、refreshToken和用户信息的对象

### 1.2 注册
- **路径**: `/api/auth/register`
- **方法**: `POST`
- **参数**:
  - `username`: 用户名
  - `password`: 密码
  - `nickname`: 昵称
  - `phone`: 手机号
  - `studentId`: 学号
- **返回**: 包含userId的对象

### 1.3 刷新令牌
- **路径**: `/api/auth/refresh`
- **方法**: `POST`
- **参数**:
  - `refreshToken`: 刷新令牌
- **返回**: 包含新token和refreshToken的对象

### 1.4 登出
- **路径**: `/api/auth/logout`
- **方法**: `POST`
- **参数**: 无（从JWT中获取userId）
- **返回**: 成功消息

## 2. 用户接口

### 2.1 获取用户信息
- **路径**: `/api/users/me`
- **方法**: `GET`
- **认证**: 需要JWT
- **参数**: 无（从JWT中获取userId）
- **返回**: 用户信息对象（id, username, nickname, avatar, phone, email, studentId, creditScore, balance, points, status, violationCount, lastLoginTime）

### 2.2 更新用户信息
- **路径**: `/api/users/me`
- **方法**: `PUT`
- **认证**: 需要JWT
- **参数**:
  - `nickname`: 昵称
  - `avatar`: 头像URL
  - `phone`: 手机号
  - `email`: 邮箱
- **返回**: 更新后的用户信息对象

### 2.3 修改密码
- **路径**: `/api/users/me/password`
- **方法**: `PUT`
- **认证**: 需要JWT
- **参数**:
  - `oldPassword`: 旧密码
  - `newPassword`: 新密码
- **返回**: 成功消息

## 3. 自习室接口

### 3.1 获取自习室列表
- **路径**: `/api/studyrooms`
- **方法**: `GET`
- **参数**:
  - `status`: 状态筛选（可选）
  - `floor`: 楼层筛选（可选）
  - `keyword`: 关键词搜索（可选）
  - `page`: 页码（默认1）
  - `pageSize`: 每页数量（默认20）
- **返回**: 自习室列表（分页）

### 3.2 获取自习室详情
- **路径**: `/api/studyrooms/{id}`
- **方法**: `GET`
- **参数**:
  - `id`: 自习室ID
- **返回**: 自习室详情

## 4. 座位接口

### 4.1 获取座位列表
- **路径**: `/api/studyrooms/{studyRoomId}/seats`
- **方法**: `GET`
- **参数**:
  - `studyRoomId`: 自习室ID（路径参数）
  - `status`: 状态筛选（可选）
  - `type`: 类型筛选（可选）
  - `row`: 行号筛选（可选）
  - `col`: 列号筛选（可选）
- **返回**: 座位列表

### 4.2 获取座位布局
- **路径**: `/api/studyrooms/{studyRoomId}/seat-layout`
- **方法**: `GET`
- **参数**:
  - `studyRoomId`: 自习室ID（路径参数）
- **返回**: 座位布局信息（rows, cols, seats, items, legend）

### 4.3 获取座位详情
- **路径**: `/api/seats/{id}`
- **方法**: `GET`
- **参数**:
  - `id`: 座位ID
- **返回**: 座位详情

## 5. 预约接口

### 5.1 创建预约
- **路径**: `/api/reservations`
- **方法**: `POST`
- **认证**: 需要JWT
- **参数**:
  - `studyRoomId`: 自习室ID
  - `seatId`: 座位ID
  - `startTime`: 开始时间
  - `endTime`: 结束时间
  - `bookingType`: 预约类型（可选）
- **返回**: 预约信息对象

### 5.2 获取我的预约列表
- **路径**: `/api/reservations/my`
- **方法**: `GET`
- **认证**: 需要JWT
- **参数**:
  - `status`: 状态筛选（可选）
  - `startDate`: 开始日期（可选）
  - `endDate`: 结束日期（可选）
  - `page`: 页码（默认1）
  - `pageSize`: 每页数量（默认20）
- **返回**: 预约列表（分页）

### 5.3 获取预约详情
- **路径**: `/api/reservations/{id}`
- **方法**: `GET`
- **参数**:
  - `id`: 预约ID
- **返回**: 预约详情

### 5.4 取消预约
- **路径**: `/api/reservations/{id}/cancel`
- **方法**: `POST`
- **参数**:
  - `id`: 预约ID
- **返回**: 成功消息

### 5.5 支付预约
- **路径**: `/api/reservations/{id}/pay`
- **方法**: `POST`
- **认证**: 需要JWT
- **参数**:
  - `id`: 预约ID
- **返回**: 支付结果

### 5.6 续时
- **路径**: `/api/reservations/{id}/renew`
- **方法**: `POST`
- **参数**:
  - `id`: 预约ID
  - `newEndTime`: 新结束时间（yyyy-MM-dd HH:mm:ss）
- **返回**: 续时结果

### 5.7 获取预约规则
- **路径**: `/api/reservations/rules`
- **方法**: `GET`
- **参数**: 无
- **返回**: 预约规则（maxAdvanceDays, maxDailyReservations等）

## 6. 支付接口

### 6.1 创建支付
- **路径**: `/api/payments`
- **方法**: `POST`
- **认证**: 需要JWT
- **参数**:
  - `orderId`: 订单ID
  - `amount`: 支付金额
  - `paymentMethod`: 支付方式（wechat/alipay/balance）
- **返回**: 包含paymentId和paymentUrl的对象

### 6.2 获取支付状态
- **路径**: `/api/payments/{id}`
- **方法**: `GET`
- **参数**:
  - `id`: 支付ID
- **返回**: 支付状态对象

### 6.3 支付回调
- **路径**: `/api/payments/callback`
- **方法**: `POST`
- **参数**:
  - `paymentNo`: 支付编号
  - `success`: 支付是否成功
- **返回**: 回调处理结果

## 7. 签到接口

### 7.1 签到
- **路径**: `/api/checkin`
- **方法**: `POST`
- **认证**: 需要JWT
- **参数**:
  - `reservationId`: 预约ID
  - `seatId`: 座位ID
- **返回**: 签到结果

### 7.2 签退
- **路径**: `/api/checkout`
- **方法**: `POST`
- **认证**: 需要JWT
- **参数**:
  - `checkInRecordId`: 签到记录ID
- **返回**: 签退结果

### 7.3 获取签到记录
- **路径**: `/api/checkin/records`
- **方法**: `GET`
- **认证**: 需要JWT
- **参数**:
  - `startDate`: 开始日期（可选）
  - `endDate`: 结束日期（可选）
  - `page`: 页码（默认1）
  - `pageSize`: 每页数量（默认20）
- **返回**: 签到记录列表（含统计数据：totalHours, weekHours, monthHours, streak等）

### 7.4 获取当前签到状态
- **路径**: `/api/checkin/current`
- **方法**: `GET`
- **认证**: 需要JWT
- **参数**: 无
- **返回**: 当前签到状态（isCheckedIn, checkInRecord）

## 8. AI接口

### 8.1 获取角色列表
- **路径**: `/api/characters`
- **方法**: `GET`
- **参数**: 无
- **返回**: AI角色列表

### 8.2 非流式聊天
- **路径**: `/api/chat`
- **方法**: `POST`
- **参数**:
  - `message`: 消息内容
  - `sessionId`: 会话ID（可选，不传则自动生成）
  - `characterId`: 角色ID（可选，默认customer_service）
- **返回**: AI回复（reply, session_id）

### 8.3 流式聊天（SSE）
- **路径**: `/api/chat/stream`
- **方法**: `POST`
- **参数**:
  - `message`: 消息内容
  - `sessionId`: 会话ID（可选）
  - `characterId`: 角色ID（可选）
- **返回**: SSE流式响应

## 9. Agent对话接口

### 9.1 Agent聊天
- **路径**: `/api/agent/chat`
- **方法**: `POST`
- **认证**: 需要JWT
- **参数**:
  - `message`: 消息内容
  - `sessionId`: 会话ID（可选）
- **返回**: Agent回复（含工具调用结果）

### 9.2 Agent工具列表
- **路径**: `/api/agent/tools`
- **方法**: `GET`
- **认证**: 需要JWT
- **参数**: 无
- **返回**: 可用工具列表

## 10. 卡片接口

### 10.1 同步生成卡片
- **路径**: `/api/card/generate`
- **方法**: `POST`
- **参数**:
  - `userID`: 用户ID
  - `studyDuration`: 学习时长（分钟）
- **返回**: 卡片对象

### 10.2 流式生成卡片（SSE）
- **路径**: `/api/card/generate/stream`
- **方法**: `POST`
- **参数**:
  - `userID`: 用户ID
  - `studyDuration`: 学习时长（分钟）
- **返回**: SSE流式响应

### 10.3 获取卡片详情
- **路径**: `/api/card/detail`
- **方法**: `GET`
- **参数**:
  - `id`: 卡片UUID
- **返回**: 卡片详情

### 10.4 获取用户卡片列表
- **路径**: `/api/card/list`
- **方法**: `GET`
- **参数**:
  - `userID`: 用户ID
- **返回**: 卡片列表

### 10.5 获取卡片图片
- **路径**: `/api/card/image/{path}`
- **方法**: `GET`
- **参数**:
  - `path`: 图片相对路径
- **返回**: PNG图片

## 11. 客户服务接口

### 11.1 发送消息
- **路径**: `/api/customer-service/message`
- **方法**: `POST`
- **参数**:
  - `message`: 消息内容
- **返回**: 发送结果

### 11.2 获取消息列表
- **路径**: `/api/customer-service/messages`
- **方法**: `GET`
- **参数**: 无
- **返回**: 消息列表

## 12. 规则接口

### 12.1 获取规则列表
- **路径**: `/api/rules`
- **方法**: `GET`
- **参数**:
  - `studyRoomId`: 自习室ID（可选）
  - `category`: 分类筛选（可选）
- **返回**: 规则列表

### 12.2 获取规则详情
- **路径**: `/api/rules/{id}`
- **方法**: `GET`
- **参数**:
  - `id`: 规则ID
- **返回**: 规则详情

## 13. 公告接口

### 13.1 获取公告列表
- **路径**: `/api/announcements`
- **方法**: `GET`
- **参数**:
  - `type`: 类型筛选（可选）
  - `priority`: 优先级筛选（可选）
  - `page`: 页码（可选）
  - `pageSize`: 每页数量（可选）
- **返回**: 公告列表

### 13.2 获取公告详情
- **路径**: `/api/announcements/{id}`
- **方法**: `GET`
- **参数**:
  - `id`: 公告ID
- **返回**: 公告详情

## 14. 成就接口

### 14.1 获取成就列表
- **路径**: `/api/achievements`
- **方法**: `GET`
- **参数**: 无
- **返回**: 成就列表

### 14.2 获取用户成就
- **路径**: `/api/achievements/user/{userId}`
- **方法**: `GET`
- **参数**:
  - `userId`: 用户ID
- **返回**: 用户已解锁成就列表

## 15. 违规记录接口

### 15.1 获取违规记录
- **路径**: `/api/violations`
- **方法**: `GET`
- **认证**: 需要JWT
- **参数**: 无
- **返回**: 违规记录列表

### 15.2 申诉违规
- **路径**: `/api/violations/{id}/appeal`
- **方法**: `POST`
- **认证**: 需要JWT
- **参数**:
  - `reason`: 申诉理由
- **返回**: 申诉结果

## 16. 统计接口

### 16.1 获取统计数据
- **路径**: `/api/statistics`
- **方法**: `GET`
- **认证**: 需要管理员权限
- **参数**: 无
- **返回**: 系统统计数据

## 17. 健康检查接口

### 17.1 基础健康检查
- **路径**: `/health`
- **方法**: `GET`
- **参数**: 无
- **返回**: 健康状态

### 17.2 就绪检查
- **路径**: `/health/ready`
- **方法**: `GET`
- **参数**: 无
- **返回**: 就绪状态（含数据库连接检查）

## 18. 测试接口

### 18.1 测试接口
- **路径**: `/api/test`
- **方法**: `GET`
- **参数**: 无
- **返回**: 测试结果

## 19. 微信小程序接口

微信小程序接口路径统一使用 `/api/wx/` 前缀，与原有接口隔离。

### 19.1 用户模块
- `POST /api/wx/user/login`: 微信登录（code换token）
- `GET /api/wx/user/profile`: 获取用户信息
- `PUT /api/wx/user/profile`: 修改用户信息
- `POST /api/wx/user/avatar`: 修改头像（multipart/form-data）
- `GET /api/wx/user/home`: 获取用户首页信息

### 19.2 自习室模块
- `GET /api/wx/studyrooms`: 自习室列表
- `GET /api/wx/studyrooms/{id}`: 自习室详情（含规则列表）

### 19.3 座位模块
- `GET /api/wx/studyrooms/{studyRoomId}/seats`: 座位列表
- `GET /api/wx/studyrooms/{studyRoomId}/seat-layout`: 座位布局
- `GET /api/wx/seats/{id}`: 座位详情

### 19.4 预约模块
- `POST /api/wx/reservations`: 创建预约
- `GET /api/wx/reservations/my`: 我的预约列表
- `GET /api/wx/reservations/{id}`: 预约详情
- `POST /api/wx/reservations/{id}/cancel`: 取消预约
- `GET /api/wx/reservations/rules`: 预约规则

### 19.5 签到模块
- `POST /api/wx/checkin`: 签到
- `POST /api/wx/checkout`: 签退
- `GET /api/wx/checkin/records`: 签到记录
- `GET /api/wx/checkin/current`: 当前签到状态

### 19.6 公告模块
- `GET /api/wx/announcements`: 公告列表
- `GET /api/wx/announcements/{id}`: 公告详情

### 19.7 规则模块
- `GET /api/wx/rules`: 规则列表
- `GET /api/wx/rules/{id}`: 规则详情

### 19.8 卡片模块
- `POST /api/wx/card/generate`: 同步生成卡片
- `POST /api/wx/card/generate/stream`: 流式生成卡片（SSE）
- `GET /api/wx/card/detail`: 卡片详情
- `GET /api/wx/card/list`: 卡片列表
- `GET /api/wx/card/image/{path}`: 卡片图片

## 20. 响应格式

所有API接口返回的响应格式如下：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {...},
  "timestamp": 1620000000000
}
```

### 错误响应

```json
{
  "code": 400,
  "message": "错误信息",
  "data": null,
  "timestamp": 1620000000000
}
```

### 卡片模块响应格式（独立）

```json
{
  "success": true,
  "message": "generate success",
  "card": {...}
}
```

## 21. 认证方式

所有需要认证的接口都需要在请求头中携带JWT令牌：

```
Authorization: Bearer <token>
```
