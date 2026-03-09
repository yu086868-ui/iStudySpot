# API接口文档 - iStudySpot

## 一、接口规范

### 1.1 基础信息

- **基础URL**：`/api`
- **数据格式**：JSON
- **字符编码**：UTF-8

### 1.2 通用响应格式

```json
{
  "code": 200,        // 状态码
  "msg": "success",   // 提示信息
  "data": {}          // 返回数据
}
```

### 1.3 状态码说明

| 状态码 | 说明                 |
| ------ | -------------------- |
| 200    | 成功                 |
| 400    | 参数错误             |
| 401    | 未登录或token失效    |
| 403    | 无权限               |
| 404    | 资源不存在           |
| 409    | 冲突（座位已被预订） |
| 500    | 服务器内部错误       |

### 1.4 认证方式

所有需要登录的接口需在请求头中携带token：

```
Authorization: Bearer <jwt_token>
```

---

## 二、用户模块接口

### 2.1 微信登录

**请求**

```
POST /api/user/login
Content-Type: application/json

{
  "code": "微信临时登录code"
}
```

**响应**

```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "userInfo": {
      "id": 1,
      "nickname": "用户昵称",
      "avatarUrl": "https://...",
      "phone": "",
      "balance": 100.00
    }
  }
}
```

### 2.2 获取用户信息

**请求**

```
GET /api/user/info
Authorization: Bearer <token>
```

**响应**

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "nickname": "用户昵称",
    "avatarUrl": "https://...",
    "phone": "13800138000",
    "balance": 100.00,
    "points": 200,
    "violationCount": 0
  }
}
```

### 2.3 更新用户信息

**请求**

```
PUT /api/user/update
Authorization: Bearer <token>
Content-Type: application/json

{
  "nickname": "新昵称",
  "avatarUrl": "https://..."
}
```

**响应**

```json
{
  "code": 200,
  "msg": "更新成功"
}
```

### 2.4 账户充值

**请求**

```
POST /api/user/recharge
Authorization: Bearer <token>
Content-Type: application/json

{
  "amount": 100.00,
  "payType": 2  // 1-余额 2-微信 3-支付宝
}
```

**响应**

```json
{
  "code": 200,
  "data": {
    "orderNo": "RECHARGE20240309001",
    "balance": 200.00
  }
}
```

---

## 三、自习室模块接口

### 3.1 获取自习室列表

**请求**

```
GET /api/room/list?city=北京&page=1&size=10
```

**响应**

```json
{
  "code": 200,
  "data": {
    "total": 3,
    "list": [
      {
        "id": 1,
        "name": "iStudySpot 学习空间（五道口店）",
        "address": "北京市海淀区五道口购物中心3F",
        "openTime": "08:00:00",
        "closeTime": "23:00:00",
        "status": 1
      }
    ]
  }
}
```

### 3.2 获取自习室详情

**请求**

```
GET /api/room/detail/{roomId}
```

**响应**

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "iStudySpot 学习空间（五道口店）",
    "address": "北京市海淀区五道口购物中心3F",
    "latitude": 39.992,
    "longitude": 116.338,
    "openTime": "08:00:00",
    "closeTime": "23:00:00",
    "description": "临近清华北大，考研党聚集地",
    "images": ["url1", "url2"],
    "areas": [
      {
        "id": 1,
        "name": "沉浸学习区",
        "seatCount": 20
      }
    ]
  }
}
```

---

## 四、座位模块接口

### 4.1 查询座位实时状态

**请求**

```
GET /api/seat/status?roomId=1&date=2024-03-09
```

**响应**

```json
{
  "code": 200,
  "data": [
    {
      "seatId": 1,
      "seatNumber": "A01",
      "areaId": 1,
      "areaName": "沉浸学习区",
      "seatType": 1,
      "rowNum": 1,
      "colNum": 1,
      "hasPower": true,
      "hasLamp": true,
      "isWindow": false,
      "isQuiet": true,
      "status": "free",  // free-空闲 booked-已预订 paid-已支付 using-使用中
      "currentOrder": null
    },
    {
      "seatId": 2,
      "seatNumber": "A02",
      "status": "using",
      "currentOrder": {
        "orderId": 1001,
        "startTime": "2024-03-09 10:00:00",
        "endTime": "2024-03-09 12:00:00"
      }
    }
  ]
}
```

### 4.2 查询座位时段详情

**请求**

```
GET /api/seat/timeline/{seatId}?date=2024-03-09
```

**响应**

```json
{
  "code": 200,
  "data": [
    {
      "startTime": "08:00",
      "endTime": "09:00",
      "status": "free"
    },
    {
      "startTime": "09:00",
      "endTime": "10:00",
      "status": "free"
    },
    {
      "startTime": "10:00",
      "endTime": "12:00",
      "status": "booked",
      "orderId": 1001
    }
  ]
}
```

### 4.3 试算价格

**请求**

```
POST /api/seat/calculate
Content-Type: application/json

{
  "seatId": 1,
  "startTime": "2024-03-09 14:00:00",
  "endTime": "2024-03-09 17:00:00"
}
```

**响应**

```json
{
  "code": 200,
  "data": {
    "totalAmount": 45.00,
    "details": [
      {
        "startTime": "14:00-15:00",
        "price": 15.00
      },
      {
        "startTime": "15:00-16:00", 
        "price": 15.00
      },
      {
        "startTime": "16:00-17:00",
        "price": 15.00
      }
    ]
  }
}
```

---

## 五、订单模块接口

### 5.1 创建订单

**请求**

```
POST /api/order/create
Authorization: Bearer <token>
Content-Type: application/json

{
  "seatId": 1,
  "startTime": "2024-03-09 14:00:00",
  "endTime": "2024-03-09 17:00:00"
}
```

**响应**

```json
{
  "code": 200,
  "data": {
    "orderId": 20240309001,
    "orderNo": "ORD20240309001",
    "totalAmount": 45.00,
    "status": 1,  // 1-待支付
    "expireTime": "2024-03-09 14:15:00"  // 15分钟内支付
  }
}
```

### 5.2 支付订单

**请求**

```
POST /api/order/pay/{orderId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "payType": 2  // 1-余额 2-微信 3-支付宝
}
```

**响应**

```json
{
  "code": 200,
  "data": {
    "orderId": 20240309001,
    "status": 2,  // 2-已支付待使用
    "payTime": "2024-03-09 14:05:00"
  }
}
```

### 5.3 取消订单

**请求**

```
POST /api/order/cancel/{orderId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "reason": "行程有变"
}
```

**响应**

```json
{
  "code": 200,
  "msg": "订单已取消"
}
```

### 5.4 签到

**请求**

```
POST /api/order/checkin/{orderId}
Authorization: Bearer <token>
```

**响应**

```json
{
  "code": 200,
  "data": {
    "orderId": 20240309001,
    "status": 3,  // 3-使用中
    "actualStartTime": "2024-03-09 13:55:00"
  }
}
```

### 5.5 签退

**请求**

```
POST /api/order/checkout/{orderId}
Authorization: Bearer <token>
```

**响应**

```json
{
  "code": 200,
  "data": {
    "orderId": 20240309001,
    "status": 4,  // 4-已完成
    "actualEndTime": "2024-03-09 17:10:00",
    "totalHours": 3.2,
    "actualAmount": 48.00,
    "refundAmount": 0.00
  }
}
```

### 5.6 续费

**请求**

```
POST /api/order/renew
Authorization: Bearer <token>
Content-Type: application/json

{
  "orderId": 20240309001,
  "newEndTime": "2024-03-09 18:00:00"
}
```

**响应**

```json
{
  "code": 200,
  "data": {
    "orderId": 20240309001,
    "additionalAmount": 15.00,
    "newEndTime": "2024-03-09 18:00:00"
  }
}
```

### 5.7 获取订单列表

**请求**

```
GET /api/order/list?status=2&page=1&size=10
Authorization: Bearer <token>
```

**响应**

```json
{
  "code": 200,
  "data": {
    "total": 5,
    "list": [
      {
        "orderId": 20240309001,
        "orderNo": "ORD20240309001",
        "roomName": "iStudySpot 五道口店",
        "seatNumber": "A01",
        "startTime": "2024-03-09 14:00:00",
        "endTime": "2024-03-09 17:00:00",
        "status": 2,
        "totalAmount": 45.00
      }
    ]
  }
}
```

### 5.8 获取订单详情

**请求**

```
GET /api/order/detail/{orderId}
Authorization: Bearer <token>
```

**响应**

```json
{
  "code": 200,
  "data": {
    "orderId": 20240309001,
    "orderNo": "ORD20240309001",
    "room": {
      "id": 1,
      "name": "iStudySpot 五道口店"
    },
    "seat": {
      "id": 1,
      "number": "A01",
      "area": "沉浸学习区"
    },
    "planStartTime": "2024-03-09 14:00:00",
    "planEndTime": "2024-03-09 17:00:00",
    "actualStartTime": "2024-03-09 13:55:00",
    "actualEndTime": "2024-03-09 17:10:00",
    "totalHours": 3.2,
    "totalAmount": 45.00,
    "actualAmount": 48.00,
    "status": 4,
    "details": [
      {
        "startTime": "14:00-15:00",
        "price": 15.00
      },
      {
        "startTime": "15:00-16:00",
        "price": 15.00
      },
      {
        "startTime": "16:00-17:00",
        "price": 15.00
      }
    ],
    "payments": [
      {
        "payType": 2,
        "amount": 45.00,
        "payTime": "2024-03-09 14:05:00"
      }
    ],
    "createTime": "2024-03-09 14:00:00"
  }
}
```

---

## 六、管理端接口

### 6.1 价格策略配置

**请求**

```
POST /api/admin/price/config
Authorization: Bearer <token>
Content-Type: application/json

{
  "roomId": 1,
  "areaId": null,
  "seatType": null,
  "weekDays": "1,2,3,4,5",
  "startTime": "08:00",
  "endTime": "18:00",
  "price": 15.00,
  "isHoliday": false,
  "priority": 1
}
```

**响应**

```json
{
  "code": 200,
  "msg": "配置成功"
}
```

### 6.2 获取价格策略列表

**请求**

```
GET /api/admin/price/list?roomId=1
Authorization: Bearer <token>
```

**响应**

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "roomId": 1,
      "areaName": "全部区域",
      "weekDays": "工作日",
      "timeRange": "08:00-18:00",
      "price": 15.00
    }
  ]
}
```

### 6.3 上座率统计

**请求**

```
GET /api/admin/stat/occupancy?roomId=1&startDate=2024-03-01&endDate=2024-03-07
Authorization: Bearer <token>
```

**响应**

```json
{
  "code": 200,
  "data": {
    "total": {
      "orderCount": 156,
      "totalHours": 468,
      "revenue": 7020.00
    },
    "daily": [
      {
        "date": "2024-03-01",
        "orderCount": 22,
        "totalHours": 66,
        "revenue": 990.00
      }
    ],
    "hourly": [
      {
        "hour": 9,
        "occupancyRate": 0.45
      },
      {
        "hour": 10,
        "occupancyRate": 0.68
      }
    ]
  }
}
```

### 6.4 用户黑名单管理

**请求**

```
POST /api/admin/blacklist/add
Authorization: Bearer <token>
Content-Type: application/json

{
  "userId": 123,
  "roomId": null,
  "reason": "多次占座不来",
  "expireTime": "2024-04-09 00:00:00"
}
```

**响应**

```json
{
  "code": 200,
  "msg": "已加入黑名单"
}
```

---

## 七、WebSocket接口

### 7.1 连接地址

```
ws://localhost:8080/ws/seat?token={jwt_token}
```

### 7.2 消息类型

**订阅座位状态变化**

```javascript
// 发送订阅
{
  "type": "subscribe",
  "roomId": 1
}

// 接收推送
{
  "type": "seat_status_change",
  "data": {
    "seatId": 1,
    "status": "using",
    "userId": 123,
    "timestamp": "2024-03-09 14:00:00"
  }
}
```

**接收订单提醒**

```json
{
  "type": "order_reminder",
  "data": {
    "orderId": 20240309001,
    "type": "checkin_reminder",  // 签到提醒
    "message": "您的订单即将开始，请及时签到"
  }
}
```

---

## 八、错误码示例

### 400 参数错误

```json
{
  "code": 400,
  "msg": "参数错误：开始时间不能晚于结束时间",
  "data": null
}
```

### 401 未登录

```json
{
  "code": 401,
  "msg": "token已过期，请重新登录",
  "data": null
}
```

### 409 座位冲突

```
{
  "code": 409,
  "msg": "该时段座位已被预订",
  "data": null
}
```

