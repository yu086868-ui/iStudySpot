# iStudySpot 前端测试文档

## 测试概述

本文档描述了 iStudySpot 微信小程序前端测试的完整方案，包括单元测试、集成测试和端到端测试。

## 测试范围

### 1. 单元测试 (Unit Tests)

#### 1.1 工具函数测试
- **文件位置**: `tests/unit/utils/`
- **测试内容**:
  - `util.test.ts`: 时间格式化函数测试
  - `seat-layout.test.ts`: 座位布局工具类测试
  - `request.test.ts`: HTTP请求封装类测试

#### 1.2 服务层测试
- **文件位置**: `tests/unit/services/`
- **测试内容**:
  - `auth.test.ts`: 用户认证服务测试
  - `reservation.test.ts`: 预约服务测试
  - `seat.test.ts`: 座位服务测试

### 2. 集成测试 (Integration Tests)

#### 2.1 页面组件测试
- **文件位置**: `tests/integration/pages/`
- **测试内容**:
  - `home.test.ts`: 首页页面逻辑测试
  - `seat-selection.test.ts`: 座位选择页面测试

### 3. 端到端测试 (E2E Tests)

#### 3.1 业务流程测试
- **文件位置**: `tests/e2e/`
- **测试内容**:
  - `auth-flow.test.ts`: 用户认证流程测试
  - `reservation-flow.test.ts`: 座位预约流程测试
  - `checkin-flow.test.ts`: 签到签退流程测试

## 测试覆盖率要求

### 目标覆盖率
- **全局覆盖率**: ≥ 70%
  - 分支覆盖率 (Branches): ≥ 70%
  - 函数覆盖率 (Functions): ≥ 70%
  - 行覆盖率 (Lines): ≥ 70%
  - 语句覆盖率 (Statements): ≥ 70%

### 覆盖范围
- `miniprogram/utils/**/*.ts`: 工具函数
- `miniprogram/services/**/*.ts`: 服务层API

## 测试工具和框架

### 主要工具
- **Jest**: JavaScript测试框架
- **ts-jest**: TypeScript支持
- **miniprogram-simulate**: 微信小程序组件模拟

### Mock工具
- **WxMock**: 微信小程序API模拟类
- **TestDataFactory**: 测试数据工厂

## 运行测试

### 安装依赖
```bash
npm install
```

### 运行所有测试
```bash
npm test
```

### 运行测试并生成覆盖率报告
```bash
npm run test:coverage
```

### 监听模式运行测试
```bash
npm run test:watch
```

## 测试文件结构

```
mp-user/
├── tests/
│   ├── setup.ts                    # 测试环境设置
│   ├── mocks/
│   │   └── wx-mock.ts              # 微信API Mock
│   ├── utils/
│   │   └── test-data-factory.ts    # 测试数据工厂
│   ├── unit/                       # 单元测试
│   │   ├── utils/
│   │   │   ├── util.test.ts
│   │   │   ├── seat-layout.test.ts
│   │   │   └── request.test.ts
│   │   └── services/
│   │       ├── auth.test.ts
│   │       ├── reservation.test.ts
│   │       └── seat.test.ts
│   ├── integration/                # 集成测试
│   │   └── pages/
│   │       ├── home.test.ts
│   │       └── seat-selection.test.ts
│   └── e2e/                        # 端到端测试
│       ├── auth-flow.test.ts
│       ├── reservation-flow.test.ts
│       └── checkin-flow.test.ts
└── coverage/                       # 覆盖率报告
    ├── lcov.info
    └── index.html
```

## 测试用例统计

### 单元测试
- **工具函数测试**: 45+ 测试用例
- **服务层测试**: 40+ 测试用例

### 集成测试
- **页面组件测试**: 50+ 测试用例

### 端到端测试
- **业务流程测试**: 30+ 测试用例

**总计**: 165+ 测试用例

## 测试最佳实践

### 1. 测试命名
- 使用描述性的测试名称
- 遵循 "should + 预期行为" 的命名规范

### 2. 测试隔离
- 每个测试用例独立运行
- 使用 beforeEach 和 afterEach 清理环境

### 3. Mock使用
- 合理使用 Mock 隔离外部依赖
- 避免过度 Mock 导致测试失真

### 4. 断言
- 每个测试用例至少包含一个断言
- 断言应该明确且有意义

### 5. 测试数据
- 使用 TestDataFactory 生成测试数据
- 避免硬编码测试数据

## 持续集成

测试应集成到 CI/CD 流程中：
1. 代码提交时自动运行测试
2. 测试失败阻止代码合并
3. 定期生成覆盖率报告

## 测试报告

运行 `npm run test:coverage` 后，可以在 `coverage/index.html` 查看详细的覆盖率报告。

报告包含：
- 文件级别的覆盖率统计
- 未覆盖代码高亮显示
- 分支覆盖详情
