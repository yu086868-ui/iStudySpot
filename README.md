# iStudySpot-自习室预订系统

## 状态徽章

### CI 状态

[![CI-Backend](https://github.com/yu086868-ui/iStudySpot/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/yu086868-ui/iStudySpot/actions/workflows/backend-ci.yml)
[![CI-Android](https://github.com/yu086868-ui/iStudySpot/actions/workflows/android-ci.yml/badge.svg)](https://github.com/yu086868-ui/iStudySpot/actions/workflows/android-ci.yml)
[![CI-Miniprogram](https://github.com/yu086868-ui/iStudySpot/actions/workflows/ci-miniprogram.yml/badge.svg?branch=main)](https://github.com/yu086868-ui/iStudySpot/actions/workflows/ci-miniprogram.yml)

### 覆盖率徽章

#### 后端覆盖率

**Main分支:** [![Backend Coverage (Main)](https://codecov.io/gh/yu086868-ui/iStudySpot/branch/main/graph/badge.svg?flag=backend)](https://codecov.io/gh/yu086868-ui/iStudySpot)
**Develop分支:** [![Backend Coverage (Develop)](https://codecov.io/gh/yu086868-ui/iStudySpot/branch/develop/graph/badge.svg?flag=backend)](https://codecov.io/gh/yu086868-ui/iStudySpot)

#### 前端覆盖率（Android）

**Main分支:** [![Android Coverage (Main)](https://codecov.io/gh/yu086868-ui/iStudySpot/branch/main/graph/badge.svg?flag=android)](https://codecov.io/gh/yu086868-ui/iStudySpot)
**Develop分支:** [![Android Coverage (Develop)](https://codecov.io/gh/yu086868-ui/iStudySpot/branch/develop/graph/badge.svg?flag=android)](https://codecov.io/gh/yu086868-ui/iStudySpot)

#### 小程序覆盖率

**Main分支:** [![Miniprogram Coverage (Main)](https://codecov.io/gh/yu086868-ui/iStudySpot/branch/main/graph/badge.svg?flag=miniprogram)](https://codecov.io/gh/yu086868-ui/iStudySpot)
**Develop分支:** [![Miniprogram Coverage (Develop)](https://codecov.io/gh/yu086868-ui/iStudySpot/branch/develop/graph/badge.svg?flag=miniprogram)](https://codecov.io/gh/yu086868-ui/iStudySpot)

## 团队成员

| 姓名   | 学号       | 分工                                                     |
| :----- | :--------- | :------------------------------------------------------- |
| 余逸晨 | 2312190113 | 后端开发、数据库设计、核心业务逻辑（预订/计费/并发处理）、AI集成、Docker部署 |
| 黄益政 | 2312190331 | Android App开发、管理端Web开发、数据统计                   |
| 贺祥宇 | 2312190107 | 微信小程序开发、座位图可视化、AI卡片生成、用户端交互实现   |

## 项目简介

iStudySpot是一款面向付费自习室的在线预订系统，主要服务于学习学生、考研党、考证族和远程办公人群。用户可通过微信小程序或Android App实时查看自习室座位布局，按小时预订座位，系统自动计算价格并支持在线支付。到店扫码签到后自动计时，离店一键签退结算。

系统特色功能包括AI学习卡片生成（6级稀有度+6大主题+AI配图）、AI智能助手（基于DeepSeek的Agent系统）、AI角色对话（SSE流式打字机效果）和AI智能客服。

针对自习室管理者，提供可视化座位配置、灵活的价格策略（忙/闲时定价）、上座率统计和用户黑名单管理。系统通过Redis分布式锁解决高峰期热门座位并发抢座问题，确保数据一致性，同时通过定时任务自动释放超时未签到订单，提升座位利用率。

---

## 技术栈

- 前端（微信小程序）：微信原生框架 + Vant Weapp（UI组件）+ ECharts（数据可视化）+ Canvas（座位图绘制）
- 前端（Android）：Kotlin + Jetpack Compose + Retrofit（网络请求）+ OkHttp + Material 3
- 前端（管理端）：React 18 + Vite 6 + Ant Design 5 + Axios
- 后端：Spring Boot 3.1.2、MyBatis 3.0.2、JWT（登录鉴权）、Flyway（数据库迁移）、Redis（缓存/分布式锁）、Sentry（错误监控）
- 数据库：MySQL 8.4（业务数据存储）、Redis 7（缓存座位状态、分布式锁、计数器）
- AI：DeepSeek API（大模型对话/Agent）、火山引擎即梦AI 文生图3.0（卡片配图）
- DevOps：Docker + Docker Compose（容器化部署）、GitHub Actions（CI/CD）、JaCoCo + Codecov（覆盖率）
- 服务器：阿里云 ECS（Ubuntu 24.04, 2核2G）

<br />

## 快速开始

### Docker 一键部署

```bash
# 克隆项目
git clone https://github.com/yu086868-ui/iStudySpot.git
cd iStudySpot

# 配置环境变量
export DB_PASSWORD=your_password
export DEEPSEEK_API_KEY=your_key

# 启动所有服务
docker-compose up -d
```

启动后访问：

| 服务 | 地址 |
| --- | --- |
| 后端 API | http://localhost:18080 |
| 管理端 Web | http://localhost:3001 |

### 本地开发

**后端**：

```bash
cd backend/istudyspot-backend
mvn spring-boot:run
```

**管理端**：

```bash
cd admin
npm install
npm run dev
```

<br />

## 项目文档

- [架构设计文档](docs/architecture.md)
- [开发流程文档](docs/开发文档.md)

<br />

## Figma 链接

小程序：[https://www.figma.com/proto/8NU6ppFuycu9ck0OQhlcx6/miniprogram?node-id=2-1173&amp;p=f&amp;t=xpHscwXPSmeIUJy7-1&amp;scaling=scale-down&amp;content-scaling=fixed&amp;page-id=0%3A1&amp;starting-point-node-id=2%3A1173](https://www.figma.com/proto/8NU6ppFuycu9ck0OQhlcx6/miniprogram?node-id=2-1173&p=f&t=xpHscwXPSmeIUJy7-1&scaling=scale-down&content-scaling=fixed&page-id=0%3A1&starting-point-node-id=2%3A1173)
