# 后端技术文档

## 1. 技术栈

- **框架**: Spring Boot 3.1.2
- **Web框架**: Spring MVC
- **ORM**: MyBatis
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **认证**: JWT
- **API文档**: 无（可考虑添加Springdoc/Swagger）
- **构建工具**: Maven
- **测试框架**: JUnit 5 + Mockito
- **覆盖率工具**: JaCoCo

## 2. 目录结构

```
backend/istudyspot-backend/
├── src/
│   ├── main/
│   │   ├── java/com/ycyu/istudyspotbackend/
│   │   │   ├── config/          # 配置类
│   │   │   ├── controller/       # 控制器
│   │   │   ├── dto/              # 数据传输对象
│   │   │   ├── entity/           # 实体类
│   │   │   ├── interceptor/      # 拦截器
│   │   │   ├── mapper/           # MyBatis映射器
│   │   │   ├── service/          # 服务层
│   │   │   ├── utils/            # 工具类
│   │   │   └── IstudyspotBackendApplication.java  # 应用入口
│   │   └── resources/
│   │       ├── db/migration/     # 数据库迁移脚本
│   │       └── application.yml    # 应用配置
│   └── test/                      # 测试代码
├── .github/workflows/            # CI/CD配置
├── pom.xml                       # Maven配置
└── README.md                     # 后端说明文档
```

## 3. 核心功能

### 3.1 认证与授权
- 用户注册、登录
- JWT令牌生成与验证
- 刷新令牌

### 3.2 自习室管理
- 自习室列表查询
- 自习室详情查询

### 3.3 座位管理
- 座位列表查询
- 座位状态查询

### 3.4 订单管理
- 创建订单
- 查询订单列表
- 订单详情查询

### 3.5 支付管理
- 创建支付
- 查询支付状态
- 支付回调处理

### 3.6 用户管理
- 获取用户信息
- 更新用户信息
- 修改密码

### 3.7 签到管理
- 签到
- 签退
- 签到记录查询

### 3.8 AI集成
- AI对话
- 智能助手

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

- `study_room`: 自习室信息
- `seat`: 座位信息
- `user`: 用户信息
- `order`: 订单信息
- `payment`: 支付信息
- `check_in`: 签到记录

### 5.2 数据库迁移

使用Flyway进行数据库迁移，迁移脚本位于 `src/main/resources/db/migration/` 目录。

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
