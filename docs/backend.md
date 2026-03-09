# 后端模块说明 - iStudySpot

## 一、模块概述

### 1.1 功能职责

- 用户认证与授权
- 自习室、区域、座位信息管理
- 预订业务逻辑处理
- 计时计费引擎
- 订单管理与支付
- 数据统计分析

### 1.2 技术栈

| 技术        | 版本   | 用途           |
| ----------- | ------ | -------------- |
| SpringBoot  | 2.7.0  | 核心框架       |
| SpringMVC   | 2.7.0  | Web层          |
| MyBatis     | 3.5.9  | ORM框架        |
| MySQL       | 8.0    | 业务数据库     |
| Redis       | 6.x    | 缓存、分布式锁 |
| JWT         | 0.11.5 | 用户认证       |
| WebSocket   | 内置   | 实时推送       |
| Spring Task | 内置   | 定时任务       |
| Maven       | 3.8+   | 项目管理       |

---

## 二、项目结构

```markdown
backend/
├── src/main/java/com/iseatspace/
│ ├── controller/ # 接口层
│ │ ├── UserController.java
│ │ ├── SeatController.java
│ │ ├── OrderController.java
│ │ └── AdminController.java
│ ├── service/ # 业务逻辑层
│ │ ├── impl/
│ │ │ ├── UserServiceImpl.java
│ │ │ ├── SeatServiceImpl.java
│ │ │ ├── OrderServiceImpl.java
│ │ │ └── PriceServiceImpl.java
│ │ └── Service.java # 接口定义
│ ├── mapper/ # 数据访问层
│ │ ├── UserMapper.java
│ │ ├── SeatMapper.java
│ │ ├── OrderMapper.java
│ │ └── ...
│ ├── entity/ # 实体类
│ │ ├── User.java
│ │ ├── Order.java
│ │ ├── Seat.java
│ │ └── ...
│ ├── config/ # 配置类
│ │ ├── RedisConfig.java
│ │ ├── WebSocketConfig.java
│ │ ├── CorsConfig.java
│ │ └── SwaggerConfig.java
│ ├── utils/ # 工具类
│ │ ├── JwtUtils.java
│ │ ├── RedisCache.java
│ │ └── PriceCalculator.java
│ ├── task/ # 定时任务
│ │ └── OrderTask.java
│ └── interceptor/ # 拦截器
│ └── JwtInterceptor.java
├── src/main/resources/
│ ├── mapper/ # MyBatis XML文件
│ │ ├── UserMapper.xml
│ │ ├── SeatMapper.xml
│ │ └── OrderMapper.xml
│ ├── application.yml # 主配置文件
│ └── application-dev.yml # 开发环境配置
├── sql/ # SQL脚本
│ └── iStudySpot.sql
└── pom.xml # Maven依赖
```

---

## 三、数据库设计概览

### 3.1 核心表清单

| 表名              | 说明           |
| ----------------- | -------------- |
| `user`            | 用户表         |
| `study_room`      | 自习室表       |
| `area`            | 区域表         |
| `seat`            | 座位表         |
| `order`           | 订单表         |
| `order_detail`    | 订单明细表     |
| `price_strategy`  | 价格策略表     |
| `payment_log`     | 支付流水表     |
| `seat_status_log` | 座位状态流水表 |
| `blacklist`       | 黑名单表       |

### 3.2 核心表结构示例

**订单表 (order)**

| 字段            | 类型        | 说明                                         |
| --------------- | ----------- | -------------------------------------------- |
| id              | bigint      | 订单ID                                       |
| order_no        | varchar(32) | 订单号                                       |
| user_id         | bigint      | 用户ID                                       |
| seat_id         | bigint      | 座位ID                                       |
| plan_start_time | datetime    | 计划开始                                     |
| plan_end_time   | datetime    | 计划结束                                     |
| status          | tinyint     | 1-待支付 2-已支付 3-使用中 4-已完成 5-已取消 |
| total_amount    | decimal     | 总金额                                       |
| create_time     | datetime    | 创建时间                                     |

> 完整建表SQL见：`backend/sql/iStudySpot.sql`

---

## 四、Redis缓存设计

| Key格式                          | 类型   | 说明         | 过期时间 |
| -------------------------------- | ------ | ------------ | -------- |
| `seat:status:{roomId}`           | Hash   | 座位实时状态 | 永久     |
| `seat:lock:{seatId}:{timeSlot}`  | String | 分布式锁     | 3秒      |
| `price:{roomId}:{areaId}:{time}` | String | 价格缓存     | 1小时    |
| `user:token:{userId}`            | String | JWT token    | 7天      |
| `room:info:{roomId}`             | String | 自习室信息   | 1小时    |

---

## 五、核心业务逻辑

### 5.1 预订流程

1. 用户选座 → 2. 检查时间段是否可预订 → 3. 计算价格 → 
2. 创建订单（状态：待支付）→ 5. 支付 → 6. 订单生效 → 
3. 签到 → 8. 使用中 → 9. 签退 → 10. 结算

### 5.2 并发控制（防止超卖）

```java
// 使用Redis分布式锁
String lockKey = "seat:lock:" + seatId + ":" + timeSlot;
Boolean locked = redisTemplate.opsForValue()
    .setIfAbsent(lockKey, "1", 3, TimeUnit.SECONDS);
```

### 5.3 定时任务

| 任务               | 说明             | 执行频率 |
| ------------------ | ---------------- | -------- |
| 释放超时未支付订单 | 15分钟后自动取消 | 每5分钟  |
| 释放超时未签到订单 | 30分钟后自动取消 | 每5分钟  |
| 清理过期黑名单     | 解除过期拉黑     | 每天0点  |

---

## 六、配置文件示例

### application.yml

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/iseatspace?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  redis:
    host: localhost
    port: 6379
    database: 0

mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.iseatspace.entity
  configuration:
    map-underscore-to-camel-case: true

jwt:
  secret: iStudySpotSecretKey2024
  expire: 604800  # 7天
```

---

## 七、运行方式

### 7.1 环境要求

- JDK 1.8+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.x+

### 7.2 启动步骤

```bash
# 1. 创建数据库
mysql -u root -p < sql/iStudySpot.sql

# 2. 修改配置文件
vim src/main/resources/application.yml

# 3. 打包
mvn clean package

# 4. 运行
java -jar target/iseatspace-1.0.0.jar

# 或者直接在IDEA中运行
```

### 7.3 接口访问

- 基础路径：`http://localhost:8080/api`
- 接口文档：`http://localhost:8080/doc.html`（需集成Knife4j）

---

## 八、依赖管理（pom.xml关键部分）

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <dependency>
        <groupId>org.mybatis.spring.boot</groupId>
        <artifactId>mybatis-spring-boot-starter</artifactId>
        <version>2.2.2</version>
    </dependency>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.33</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt</artifactId>
        <version>0.9.1</version>
    </dependency>
</dependencies>
```

