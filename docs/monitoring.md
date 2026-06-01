# 监控配置说明

## 1. 概述

本文档描述 iStudySpot 后端服务的监控配置，包括日志管理、健康检查、指标收集等功能。

## 2. 日志管理

### 2.1 结构化日志

后端使用 SLF4J + Logback 实现结构化日志输出。

#### 2.1.1 日志格式

日志输出格式如下：
```
2026-06-01 20:15:07.580 [main] INFO  c.y.i.IstudyspotBackendApplication - Starting IstudyspotBackendApplication...
```

| 字段 | 说明 |
|------|------|
| `2026-06-01 20:15:07.580` | 时间戳（精确到毫秒） |
| `[main]` | 线程名称 |
| `INFO` | 日志级别 (DEBUG/INFO/WARN/ERROR) |
| `c.y.i.IstudyspotBackendApplication` | 日志记录器名称（缩写） |
| `Starting IstudyspotBackendApplication...` | 日志消息 |

#### 2.1.2 日志配置文件

日志配置文件位于 `src/main/resources/logback-spring.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <property name="LOG_PATH" value="./logs"/>
    
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/istudyspot.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/istudyspot.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

#### 2.1.3 日志文件位置

日志文件保存在 `backend/istudyspot-backend/logs/istudyspot.log`

## 3. 健康检查端点

### 3.1 接口列表

| 端点 | 方法 | 说明 |
|------|------|------|
| `/health` | GET | 基础健康检查 |
| `/health/ready` | GET | 就绪检查（含数据库连接检查） |

### 3.2 /health 端点

**请求**：
```bash
GET /health
```

**响应**：
```json
{
  "code": 200,
  "message": "Service is running",
  "data": {
    "status": "healthy",
    "timestamp": "2026-06-01T20:15:13.4915909",
    "version": "1.0.0",
    "appName": "iStudySpot",
    "serverPort": "8080",
    "service": "iStudySpot Backend",
    "environment": "development"
  }
}
```

### 3.3 /health/ready 端点

**请求**：
```bash
GET /health/ready
```

**响应**：
```json
{
  "code": 200,
  "message": "Ready to serve",
  "data": {
    "status": "ready",
    "timestamp": "2026-06-01T20:15:31.7639071",
    "checks": {
      "database": "connected",
      "services": "available",
      "memory": "sufficient"
    }
  }
}
```

## 4. 指标收集

### 4.1 收集的指标

| 指标 | 类型 | 说明 |
|------|------|------|
| `totalRequests` | long | 总请求数 |
| `successfulRequests` | long | 成功请求数 |
| `failedRequests` | long | 失败请求数 |
| `totalResponseTime` | long | 总响应时间(ms) |
| `endpointRequestCount` | Map<String, Long> | 各端点请求计数 |

### 4.2 指标收集器

`MetricsInterceptor` 拦截器自动收集以下信息：
1. **请求计数**：每次请求自动累加
2. **响应时间**：记录每个请求的处理时间
3. **状态码统计**：区分成功(2xx)和失败(4xx/5xx)请求
4. **端点统计**：按端点路径统计请求次数

### 4.3 日志输出示例

每次请求都会记录：
```
2026-06-01 20:15:10.000 [http-nio-8080-exec-1] INFO  c.y.i.interceptor.MetricsInterceptor - 2026-06-01T20:15:10 GET /health 15ms 200
```

## 5. 错误追踪

### 5.1 Sentry 集成

已集成 Sentry 错误追踪服务：

1. **依赖**：已添加到 `pom.xml`
   ```xml
   <dependency>
       <groupId>io.sentry</groupId>
       <artifactId>sentry-spring-boot-starter</artifactId>
       <version>6.27.0</version>
   </dependency>
   ```

2. **配置**：已在 `application.yml` 中配置
   ```yaml
   sentry:
     dsn: "https://7be566ea6f06435fac0d934efdc5b8a@o4511490021326848.ingest.us.sentry.io/4511490023132906"
     traces-sample-rate: 1.0
   ```

## 6. 告警配置

### 6.1 告警规则

| 告警类型 | 触发条件 | 检查频率 |
|----------|----------|----------|
| 服务不可用 | 连续3次健康检查失败 | 每30秒 |
| 错误率过高 | 错误率 > 5% | 每分钟 |
| 响应时间过长 | 平均响应时间 > 500ms | 每分钟 |

### 6.2 配置说明

在 `application.yml` 中配置告警参数：

```yaml
alert:
  error-rate:
    enabled: true        # 是否启用错误率告警
    threshold: 5.0       # 错误率阈值（百分比）
    window-seconds: 60   # 时间窗口（秒）
  response-time:
    enabled: true        # 是否启用响应时间告警
    threshold-ms: 500    # 响应时间阈值（毫秒）
  service-unavailable:
    enabled: true        # 是否启用服务不可用告警
    consecutive-failures: 3  # 连续失败次数
```

### 6.3 告警服务实现

告警服务使用 Spring Scheduler 自动执行检查：

| 文件 | 说明 |
|------|------|
| `AlertConfig.java` | 告警配置类 |
| `AlertService.java` | 告警服务接口 |
| `AlertServiceImpl.java` | 告警服务实现（定时任务） |

### 6.4 告警输出示例

```
2026-06-01 20:30:00.000 [scheduler-1] ERROR  c.y.i.s.impl.AlertServiceImpl - ALERT: Error rate exceeded threshold! Current: 6.25%, Threshold: 5.0%
2026-06-01 20:31:00.000 [scheduler-1] WARN  c.y.i.s.impl.AlertServiceImpl - ALERT: Average response time exceeded threshold! Current: 523.45ms, Threshold: 500ms
```

### 6.5 监控工具集成

推荐使用以下监控工具：
- **Prometheus**：指标采集
- **Grafana**：可视化仪表盘
- **Alertmanager**：告警管理

## 7. 目录结构

```
iStudySpot/
├── backend/
│   └── istudyspot-backend/
│       ├── src/main/java/com/ycyu/istudyspotbackend/
│       │   ├── config/
│       │   │   └── LoggingConfig.java      # 日志配置类
│       │   ├── controller/
│       │   │   └── HealthController.java   # 健康检查端点
│       │   └── interceptor/
│       │       └── MetricsInterceptor.java # 指标拦截器
│       ├── src/main/resources/
│       │   └── logback-spring.xml          # Logback 配置
│       └── logs/
│           └── istudyspot.log              # 日志文件
└── docs/
    ├── monitoring.md                        # 监控配置说明
    └── contributions/
        └── 13-monitoring/
            └── 余逸晨.md                    # 个人贡献说明
```

## 8. 测试验证

### 8.1 测试健康检查

```bash
curl http://localhost:8080/health -UseBasicParsing
curl http://localhost:8080/health/ready -UseBasicParsing
```

### 8.2 验证日志输出

```bash
cd backend/istudyspot-backend
powershell -Command "Get-Content logs/istudyspot.log"
```

## 9. 注意事项

1. **日志级别**：生产环境建议使用 INFO 级别，开发环境可使用 DEBUG
2. **日志轮转**：日志文件按天轮转，保留最近 30 天
3. **性能影响**：指标收集对性能影响极小，建议始终开启
4. **安全考虑**：避免在日志中输出敏感信息（密码、token等）
