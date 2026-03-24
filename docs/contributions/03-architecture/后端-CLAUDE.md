# iStudySpot 后端项目规则

## 技术栈

| 分类       | 技术        | 版本   | 说明           |
| ---------- | ----------- | ------ | -------------- |
| 核心框架   | Spring Boot | 3.5.11 | 基础框架       |
| ORM框架    | MyBatis     | 3.0.5  | 数据访问层     |
| 数据库     | MySQL       | 8.0    | 主数据库       |
| 缓存       | Redis       | 6.x    | 缓存、分布式锁 |
| 认证       | JWT         | 0.11.5 | 用户认证       |
| 实时推送   | WebSocket   | 内置   | 座位状态推送   |
| 数据库迁移 | Flyway      | 内置   | 版本管理       |
| 构建工具   | Maven       | 3.8+   | 项目构建       |
| JDK        | OpenJDK     | 17     | 运行环境       |

## 目录结构

```
backend/src/main/java/com/ycyu/istudyspotbackend/
├── controller/           # 接口层（处理HTTP请求）
├── service/              # 业务逻辑层
│   └── impl/            # 服务实现类
├── mapper/              # 数据访问层
├── entity/              # 实体类（对应数据库表）
├── dto/                 # 数据传输对象（接口入参/出参）
├── config/              # 配置类
├── interceptor/         # 拦截器
├── utils/               # 工具类
├── task/                # 定时任务
└── exception/           # 异常处理

backend/src/main/resources/
├── application.yml      # 主配置文件
├── db/migration/        # Flyway迁移脚本
└── mapper/              # MyBatis XML文件（可选）
```

## 代码规范

### 命名规范

| 类型   | 规范                      | 示例                             |
| ------ | ------------------------- | -------------------------------- |
| 类名   | 大驼峰                    | `UserController`, `OrderService` |
| 方法名 | 小驼峰                    | `getUserInfo`, `createOrder`     |
| 变量名 | 小驼峰                    | `userId`, `totalAmount`          |
| 常量   | 全大写下划线              | `MAX_RETRY_COUNT`, `JWT_SECRET`  |
| 包名   | 全小写                    | `com.ycyu.istudyspotbackend`     |
| 接口   | 大驼峰 + Service/Impl后缀 | `UserService`, `UserServiceImpl` |
| Mapper | 大驼峰 + Mapper后缀       | `UserMapper`, `OrderMapper`      |

### 分层规范

#### Controller 层
- 只负责接收请求、参数校验、返回响应
- 不包含业务逻辑
- 使用 `@RestController` 注解
- 统一返回 `Result<T>` 格式
- 使用 `@RequestAttribute` 获取 userId

```java
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestAttribute Long userId) {
        User user = userService.getUserInfo(userId);
        return Result.success(user);
    }
}
```

#### Service 层
- 包含核心业务逻辑
- 使用 `@Service` 注解
- 事务边界放在 Service 层（`@Transactional`）
- 接口定义在 `service/` 下，实现类在 `service/impl/` 下

```java
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    
    @Transactional
    public Map<String, Object> createOrder(Long userId, Long seatId, LocalDateTime startTime, LocalDateTime endTime) {
        // 业务逻辑
    }
}
```

#### Mapper 层
- 使用 `@Mapper` 注解
- 方法名规范：`insert`, `update`, `delete`, `select` 开头
- 复杂查询使用 XML 文件，简单查询使用注解
- 参数使用 `@Param` 注解

```java
@Mapper
public interface OrderMapper {
    @Insert("INSERT INTO `order`(...) VALUES(...)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);
    
    @Select("SELECT * FROM `order` WHERE user_id = #{userId}")
    List<Order> findByUserId(@Param("userId") Long userId);
}
```

#### Entity 层
- 对应数据库表结构
- 使用驼峰命名，MyBatis 自动映射
- 使用 `java.time.LocalDateTime` 处理时间
- 使用 `java.math.BigDecimal` 处理金额

```java
public class Order {
    private Long id;
    private String orderNo;
    private BigDecimal totalAmount;
    private LocalDateTime createTime;
    // getter/setter
}
```

### 统一响应格式

```java
public class Result<T> {
    private Integer code;   // 200:成功, 400:参数错误, 401:未登录, 500:服务器错误
    private String msg;     // 提示信息
    private T data;         // 返回数据
    
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }
    
    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }
}
```

### 配置文件规范

```yaml
# application.yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/iseatspace
    username: root
    password: ${DB_PASSWORD}  # 使用环境变量，不硬编码
  data:
    redis:
      host: localhost
      port: 6379

jwt:
  secret: ${JWT_SECRET}  # 使用环境变量
  expire: 604800
```

### 异常处理

- 使用全局异常处理器统一处理
- 自定义业务异常：`BusinessException`

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }
}
```

## 数据库规范

### 表命名
- 全小写，下划线分隔：`study_room`, `order_detail`
- 单数形式（非复数）

### 字段规范
- 主键统一：`id` (bigint, auto_increment)
- 时间字段：`create_time`, `update_time`
- 软删除：`is_deleted` (0-未删除, 1-已删除)
- 金额字段：`decimal(10,2)`

### SQL 规范
- 使用 `#{param}` 而不是 `${param}`（防SQL注入）
- 查询使用索引字段
- 避免 `SELECT *`，明确指定字段

## 缓存规范（Redis）

### Key 命名
- 格式：`模块:子模块:标识`
- 示例：`seat:status:123`, `user:token:456`

### 使用场景
- 座位实时状态：`seat:status:{roomId}` (Hash)
- 分布式锁：`seat:lock:{seatId}:{timeSlot}` (String, 3秒过期)
- 价格缓存：`price:{roomId}:{areaId}:{time}` (String, 1小时过期)
- JWT Token：`user:token:{userId}` (String, 7天过期)

## Git 规范

### 分支命名
- `main` - 主分支（稳定版本）
- `develop` - 开发分支
- `feature/xxx` - 功能分支

## 禁止事项

1. **不要硬编码配置**
   - 密码、密钥等敏感信息使用环境变量
   - 不要在代码中写死 URL、端口

2. **不要忽略异常**
   - 必须处理或向上抛出
   - 不要 catch 后什么都不做

3. **不要在 Controller 写业务逻辑**
   - Controller 只负责参数校验和返回
   - 业务逻辑在 Service 层

4. **不要直接返回 Entity**
   - 使用 DTO 或 VO 返回给前端
   - 避免暴露敏感字段

5. **不要使用 `SELECT *`**
   - 明确指定需要的字段
   - 提高查询效率

6. **不要提交本地配置文件**
   - `application-local.yml` 加入 `.gitignore`
   - 敏感信息不提交

7. **不要使用魔法值**
   - 定义常量或枚举
   - 示例：订单状态用常量 `ORDER_STATUS_PAID = 2`

8. **不要忽略空值判断**
   - 对可能为 null 的对象进行判断
   - 使用 `Optional` 或 `if (xxx != null)`

## 开发原则

1. **单一职责**：一个类只负责一件事
2. **依赖倒置**：面向接口编程
3. **开闭原则**：对扩展开放，对修改关闭
4. **小步提交**：每次提交保证代码可运行
5. **测试优先**：核心逻辑编写单元测试
6. **代码审查**：PR 前自我审查

## 常用命令

```bash
# 打包
mvn clean package

# 运行测试
mvn test

# 运行项目
mvn spring-boot:run

# 查看依赖树
mvn dependency:tree

# Flyway 迁移
mvn flyway:migrate
```
