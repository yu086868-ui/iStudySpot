## iStudySpot 系统架构设计文档

---

## 整体系统架构图

```mermaid
graph TB
    subgraph 客户端层
        A1[微信小程序<br/>用户端]
        A2[Android App<br/>管理端]
    end
    
    subgraph 网关层
        B[Nginx<br/>反向代理/负载均衡]
    end
    
    subgraph 后端服务层
        C1[用户服务<br/>User Service]
        C2[座位服务<br/>Seat Service]
        C3[订单服务<br/>Order Service]
        C4[计费服务<br/>Price Service]
        C5[统计服务<br/>Stat Service]
    end
    
    subgraph 数据层
        D1[(MySQL<br/>主数据库)]
        D2[(Redis<br/>缓存)]
    end
    
    subgraph 外部服务
        E1[微信支付]
        E2[阿里云OSS]
    end
    
    A1 --> B
    A2 --> B
    B --> C1
    B --> C2
    B --> C3
    B --> C4
    B --> C5
    
    C1 --> D1
    C1 --> D2
    C2 --> D1
    C2 --> D2
    C3 --> D1
    C3 --> D2
    C4 --> D1
    C4 --> D2
    C5 --> D1
    
    C3 --> E1
    C2 --> E2
```

---

## 后端架构

### 后端分层架构

```mermaid
graph TB
    subgraph 表现层 Controller
        U1[UserController]
        S1[SeatController]
        O1[OrderController]
        A1[AdminController]
    end
    
    subgraph 业务层 Service
        U2[UserService]
        S2[SeatService]
        O2[OrderService]
        P2[PriceService]
        T2[StatService]
    end
    
    subgraph 数据访问层 Mapper
        U3[UserMapper]
        S3[SeatMapper]
        O3[OrderMapper]
        P3[PriceMapper]
    end
    
    subgraph 基础设施
        I1[Redis缓存]
        I2[JWT认证]
        I3[WebSocket]
        I4[定时任务]
    end
    
    U1 --> U2
    S1 --> S2
    O1 --> O2
    A1 --> P2
    A1 --> T2
    
    U2 --> U3
    S2 --> S3
    O2 --> O3
    P2 --> P3
    T2 --> O3
    T2 --> U3
    
    U2 -.-> I1
    S2 -.-> I1
    O2 -.-> I1
    
    U2 -.-> I2
    S2 -.-> I2
    O2 -.-> I2
    
    O2 -.-> I3
    S2 -.-> I3
    
    O2 -.-> I4
```

### 服务模块划分

| 模块         | 包路径                 | 核心职责                 | 主要接口              |
| ------------ | ---------------------- | ------------------------ | --------------------- |
| **用户服务** | `service.UserService`  | 微信登录、用户信息、余额 | `/api/user/*`         |
| **座位服务** | `service.SeatService`  | 座位状态、预订、时段查询 | `/api/seat/*`         |
| **订单服务** | `service.OrderService` | 订单创建、支付、签到签退 | `/api/order/*`        |
| **计费服务** | `service.PriceService` | 价格计算、策略管理       | `/api/seat/calculate` |
| **统计服务** | `service.StatService`  | 上座率、营收统计         | `/api/admin/stat/*`   |
| **管理服务** | `service.AdminService` | 座位配置、价格策略       | `/api/admin/*`        |

### 后端目录结构

```
backend/src/main/java/com/ycyu/istudyspotbackend/
│
├── controller/                    # 接口层
│   ├── UserController.java
│   ├── SeatController.java
│   ├── OrderController.java
│   └── AdminController.java
│
├── service/                       # 业务层
│   ├── UserService.java
│   ├── SeatService.java
│   ├── OrderService.java
│   ├── PriceService.java
│   ├── StatService.java
│   ├── AdminService.java
│   └── impl/
│       ├── UserServiceImpl.java
│       ├── SeatServiceImpl.java
│       ├── OrderServiceImpl.java
│       ├── PriceServiceImpl.java
│       ├── StatServiceImpl.java
│       └── AdminServiceImpl.java
│
├── mapper/                        # 数据访问层
│   ├── UserMapper.java
│   ├── SeatMapper.java
│   ├── OrderMapper.java
│   └── PriceMapper.java
│
├── entity/                        # 实体类
│   ├── User.java
│   ├── Seat.java
│   ├── Order.java
│   └── Result.java
│
├── dto/                           # 数据传输对象
│   ├── LoginDTO.java
│   ├── BookDTO.java
│   └── PriceDTO.java
│
├── config/                        # 配置类
│   ├── WebConfig.java
│   ├── RedisConfig.java
│   └── WebSocketConfig.java
│
├── interceptor/                   # 拦截器
│   └── JwtInterceptor.java
│
├── utils/                         # 工具类
│   ├── JwtUtils.java
│   └── RedisCache.java
│
├── task/                          # 定时任务
│   └── OrderTask.java
│
└── exception/                     # 异常处理
    └── GlobalExceptionHandler.java
```

---

## 数据库设计（ER图）

![p1](https://raw.githubusercontent.com/Siangus/pictures/14e5d13e27a2d44ac27dc61659cb41eb45cc71a9/p1.png)

### 核心表说明

| 表名           | 说明       | 核心字段                                             |
| -------------- | ---------- | ---------------------------------------------------- |
| `user`         | 用户表     | id, openid, nickname, balance                        |
| `study_room`   | 自习室表   | id, name, address, open_time, close_time             |
| `area`         | 区域表     | id, room_id, name                                    |
| `seat`         | 座位表     | id, seat_number, room_id, has_power                  |
| `order`        | 订单表     | id, order_no, user_id, seat_id, status, total_amount |
| `order_detail` | 订单明细表 | id, order_id, start_time, end_time, amount           |
| `payment_log`  | 支付流水表 | id, order_id, pay_no, amount, status                 |

---

## 系统交互流程

### 用户预订完整流程

```mermaid
sequenceDiagram
    participant User as 用户
    participant MP as 小程序
    participant API as 后端API
    participant Redis as Redis
    participant DB as MySQL
    participant Pay as 微信支付

    User->>MP: 进入座位图
    MP->>API: GET /api/seat/status?roomId=1
    API->>Redis: 查询座位缓存
    Redis-->>API: 座位状态
    API-->>MP: 座位列表
    MP-->>User: 显示座位图

    User->>MP: 选择座位和时间
    MP->>API: POST /api/seat/calculate
    API->>DB: 查询价格策略
    DB-->>API: 价格
    API-->>MP: 价格详情
    MP-->>User: 显示价格

    User->>MP: 确认预订
    MP->>API: POST /api/seat/book
    API->>Redis: 分布式锁(seatId)
    API->>DB: 检查时间段冲突
    API->>DB: 创建订单
    API->>Redis: 更新座位状态缓存
    API->>Redis: 释放锁
    API-->>MP: 订单信息
    MP-->>User: 跳转支付

    User->>MP: 发起支付
    MP->>Pay: 调用微信支付
    Pay-->>MP: 支付结果
    MP->>API: POST /api/order/pay/{orderId}
    API->>DB: 更新订单状态
    API-->>MP: 支付成功
    MP-->>User: 支付完成
```

###  签到签退流程

```mermaid
sequenceDiagram
    participant User as 用户
    participant MP as 小程序
    participant API as 后端API
    participant DB as MySQL
    participant WS as WebSocket

    User->>MP: 扫描二维码签到
    MP->>API: POST /api/order/checkin/{orderId}
    API->>DB: 查询订单
    alt 订单有效
        API->>DB: 更新订单状态为使用中
        API->>DB: 记录实际开始时间
        API-->>MP: 签到成功
        MP-->>User: 显示倒计时
        API->>WS: 推送签到成功通知
    else 订单无效
        API-->>MP: 签到失败
        MP-->>User: 提示错误
    end

    User->>MP: 点击签退
    MP->>API: POST /api/order/checkout/{orderId}
    API->>DB: 查询订单
    API->>DB: 计算实际时长
    API->>DB: 更新订单为已完成
    API->>DB: 释放座位
    API-->>MP: 结算金额
    MP-->>User: 显示订单详情
    API->>WS: 推送座位释放通知
```

### 实时座位状态推送流程

```mermaid
sequenceDiagram
    participant User as 用户
    participant MP as 小程序
    participant WS as WebSocket
    participant API as 后端API
    participant Redis as Redis

    User->>MP: 进入座位图页面
    MP->>WS: 建立WebSocket连接
    MP->>WS: 订阅座位状态(roomId)
    WS-->>MP: 订阅成功

    Note over API,Redis: 其他用户预订座位
    API->>Redis: 更新座位状态
    API->>WS: 推送状态变化
    WS->>MP: 座位状态更新
    MP->>MP: 刷新座位图
    MP-->>User: 显示最新状态
```

