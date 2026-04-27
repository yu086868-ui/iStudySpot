# iStudySpot 后端 API 测试

## 覆盖率徽章

### 后端覆盖率
[![Backend Coverage](https://codecov.io/gh/yu086868-ui/iStudySpot/branch/main/graph/badge.svg?flag=backend)](https://codecov.io/gh/yu086868-ui/iStudySpot)

### 前端覆盖率
[![Frontend Coverage](https://codecov.io/gh/yu086868-ui/iStudySpot/branch/main/graph/badge.svg?flag=frontend)](https://codecov.io/gh/yu086868-ui/iStudySpot)

## 测试环境准备

### 1. 启动后端服务

在运行测试之前，需要确保后端服务已经启动。可以通过以下命令启动：

```bash
# 使用 Maven 启动
mvn spring-boot:run

# 或者使用 Docker 启动
docker-compose up
```

## 运行测试

### 运行后端测试

```bash
# 在项目根目录运行
mvn test
```

### 生成覆盖率报告

```bash
# 运行测试并生成覆盖率报告
mvn test jacoco:report

# 查看覆盖率报告
# 打开 target/site/jacoco/index.html
```

## 测试内容

### 1. 单元测试（含 Mock）

- **JwtUtilsTest**：测试 JWT 令牌生成和验证
- **AuthServiceImplTest**：测试认证服务实现
- **OrderServiceImplTest**：测试订单服务实现
- **StudyRoomServiceImplTest**：测试自习室服务实现
- **SeatServiceImplTest**：测试座位服务实现
- **CustomerServiceServiceImplTest**：测试客服服务实现
- **AIServiceImplTest**：测试 AI 服务实现
- **UserServiceImplTest**：测试用户服务实现
- **DeepSeekServiceImplTest**：测试 DeepSeek API 服务实现
- **PaymentServiceImplTest**：测试支付服务实现

### 2. API 接口测试

- **AuthControllerTest**：测试认证相关接口
- **StudyRoomControllerTest**：测试自习室相关接口
- **OrderControllerTest**：测试订单相关接口
- **SeatControllerTest**：测试座位相关接口
- **CheckInControllerTest**：测试签到相关接口
- **AnnouncementControllerTest**：测试公告相关接口

## 测试结果

测试运行完成后，会显示每个测试用例的执行结果，包括通过、失败或跳过的测试。

## 注意事项

1. 确保后端服务已经启动，并且可以正常访问
2. 确保数据库已经初始化，并且有必要的测试数据
3. 测试过程中可能会创建临时用户，测试完成后可以手动清理
4. 如果测试失败，可以查看错误信息，定位问题所在
5. 测试覆盖率报告可以在 target/site/jacoco/index.html 查看
