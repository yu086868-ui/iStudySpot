# 后端技术文档

## 1. 技术栈

- **框架**: Spring Boot 3.5.11
- **Web框架**: Spring MVC
- **ORM**: MyBatis 3.0.5
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **认证**: JWT
- **构建工具**: Maven
- **测试框架**: JUnit 5 + Mockito
- **覆盖率工具**: JaCoCo
- **AI集成**: DeepSeek API

## 2. 目录结构

```
backend/istudyspot-backend/
├── src/
│   ├── main/
│   │   ├── java/com/ycyu/istudyspotbackend/
│   │   │   ├── agent/            # AI Agent模块
│   │   │   │   ├── chat/         # Agent对话服务
│   │   │   │   └── tool/         # Agent工具服务
│   │   │   ├── config/           # 配置类
│   │   │   ├── controller/       # 控制器
│   │   │   │   ├── Wx*           # 微信小程序控制器
│   │   │   │   └── *Controller   # 原有控制器
│   │   │   ├── dto/              # 数据传输对象
│   │   │   ├── entity/           # 实体类
│   │   │   ├── interceptor/      # 拦截器
│   │   │   ├── mapper/           # MyBatis映射器
│   │   │   ├── service/          # 服务层
│   │   │   ├── utils/            # 工具类
│   │   │   └── IstudyspotBackendApplication.java
│   │   └── resources/
│   │       ├── db/migration/     # 数据库迁移脚本
│   │       └── application.yml   # 应用配置
│   └── test/                     # 测试代码
├── .github/workflows/            # CI/CD配置
├── Dockerfile                    # Docker构建文件
├── docker-compose.yml            # Docker编排文件
├── pom.xml                       # Maven配置
└── README.md                     # 后端说明文档
```

## 3. 核心功能

### 3.1 认证与授权
- 用户注册、登录（用户名/密码）
- 微信小程序登录（code换token）
- JWT令牌生成与验证
- 刷新令牌
- 登出

### 3.2 自习室管理
- 自习室列表查询（支持分页、状态筛选、楼层筛选、关键词搜索）
- 自习室详情查询（含规则列表）

### 3.3 座位管理
- 座位列表查询（支持状态、类型、行列筛选）
- 座位布局查询（含布局元素、图例）
- 座位详情查询

### 3.4 预约管理
- 创建预约
- 查询我的预约列表（支持状态、日期筛选）
- 预约详情查询
- 取消预约
- 预约支付
- 预约续时
- 预约规则查询

### 3.5 支付管理
- 创建支付
- 查询支付状态
- 支付回调处理

### 3.6 用户管理
- 获取用户信息
- 更新用户信息（昵称、头像、手机、邮箱）
- 修改密码
- 上传头像

### 3.7 签到管理
- 签到
- 签退
- 签到记录查询（含统计数据：总时长、周时长、月时长、连续打卡天数等）
- 当前签到状态查询

### 3.8 AI集成
- AI角色列表查询
- AI非流式聊天
- AI流式聊天（SSE）
- Agent对话（含工具调用）
- AI卡片生成（同步/流式）
- 卡片列表查询
- 卡片详情查询
- 卡片图片访问

### 3.9 规则管理
- 规则列表查询（支持自习室ID、分类筛选）
- 规则详情查询

### 3.10 公告管理
- 公告列表查询（支持类型、优先级筛选）
- 公告详情查询

### 3.11 成就系统
- 成就列表查询
- 用户成就查询

### 3.12 违规记录
- 违规记录查询
- 违规申诉

### 3.13 健康检查
- 基础健康检查
- 就绪检查（含数据库连接检查）

## 4. 测试情况

### 4.1 测试文件

#### 控制器测试
- `AIControllerTest.java`
- `AnnouncementControllerTest.java`
- `AuthControllerTest.java`
- `CheckInControllerTest.java`
- `CustomerServiceControllerTest.java`
- `OrderControllerTest.java`
- `PaymentControllerTest.java`
- `RulesControllerTest.java`
- `SeatControllerTest.java`
- `StudyRoomControllerTest.java`
- `TestControllerTest.java`
- `UserControllerTest.java`

#### 服务层测试
- `AIServiceImplTest.java`
- `AuthServiceImplTest.java`
- `CustomerServiceServiceImplTest.java`
- `DeepSeekServiceImplTest.java`
- `OrderServiceImplTest.java`
- `PaymentServiceImplTest.java`
- `SeatServiceImplTest.java`
- `StudyRoomServiceImplTest.java`
- `UserServiceImplTest.java`

#### 工具类测试
- `JwtUtilsTest.java`

### 4.2 测试覆盖率

- **整体覆盖率**: 61%
- **核心模块覆盖率**: > 60%

### 4.3 测试运行

```bash
# 运行测试
mvn test

# 生成覆盖率报告
mvn test jacoco:report

# 查看覆盖率报告
target/site/jacoco/index.html
```

## 5. 数据库设计

### 5.1 主要表结构

**核心业务表:**
- `user`: 用户信息
- `study_room`: 自习室信息
- `seat`: 座位信息
- `order`: 预约订单信息
- `payment`: 支付信息
- `check_in_record`: 签到记录

**扩展功能表:**
- `card`: AI卡片
- `rule`: 规则配置
- `announcement`: 公告信息
- `achievement`: 成就定义
- `user_achievement`: 用户成就
- `violation_record`: 违规记录
- `seat_layout_item`: 座位布局元素

**辅助表:**
- `area`: 区域信息
- `price_strategy`: 价格策略
- `order_detail`: 订单明细
- `payment_log`: 支付流水
- `seat_status_log`: 座位状态日志
- `blacklist`: 黑名单

### 5.2 数据库迁移

数据库初始化脚本位于项目根目录 `init-db.sql`。

## 6. API接口

### 6.1 认证接口
- `POST /api/auth/login`: 登录
- `POST /api/auth/register`: 注册
- `POST /api/auth/refresh`: 刷新令牌
- `POST /api/auth/logout`: 登出

### 6.2 用户接口
- `GET /api/users/me`: 获取用户信息
- `PUT /api/users/me`: 更新用户信息
- `PUT /api/users/me/password`: 修改密码

### 6.3 自习室接口
- `GET /api/study-rooms`: 获取自习室列表
- `GET /api/study-rooms/{id}`: 获取自习室详情

### 6.4 座位接口
- `GET /api/seats`: 获取座位列表
- `GET /api/seats/{id}`: 获取座位详情

### 6.5 订单接口
- `POST /api/orders`: 创建订单
- `GET /api/orders`: 获取订单列表
- `GET /api/orders/{id}`: 获取订单详情

### 6.6 支付接口
- `POST /api/payments`: 创建支付
- `GET /api/payments/{id}`: 获取支付状态
- `POST /api/payments/callback`: 支付回调

### 6.7 签到接口
- `POST /api/check-in`: 签到
- `POST /api/check-out`: 签退
- `GET /api/check-in/history`: 获取签到记录

### 6.8 客户服务接口
- `POST /api/customer-service/message`: 发送消息
- `GET /api/customer-service/messages`: 获取消息列表

### 6.9 规则接口
- `GET /api/rules`: 获取规则列表
- `GET /api/rules/{id}`: 获取规则详情

### 6.10 测试接口
- `GET /api/test`: 测试接口

## 7. 部署与运行

### 7.1 本地运行

```bash
# 使用Maven运行
mvn spring-boot:run

# 使用Docker运行
docker-compose up
```

### 7.2 环境配置

主要配置项位于 `application.yml` 文件中，包括数据库连接、Redis连接、JWT密钥等。

## 8. 监控与维护

### 8.1 日志

使用Spring Boot默认的日志系统，日志输出到控制台和文件。

### 8.2 错误处理

统一的错误处理机制，返回标准化的错误响应。

### 8.3 性能优化

- 使用Redis缓存热门数据
- 使用MySQL索引优化查询
- 使用连接池管理数据库连接

## 9. 未来规划

- 添加API文档（Springdoc/Swagger）
- 增加更多单元测试和集成测试
- 优化数据库查询性能
- 添加更多监控指标
- 实现更多AI功能
