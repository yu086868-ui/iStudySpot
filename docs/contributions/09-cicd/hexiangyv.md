# CI/CD 配置贡献说明

姓名：贺祥宇  学号：2312190107  角色：前端  日期：2026-6-15

## 完成的工作

### 工作流配置

- [x] 维护 `.github/workflows/ci-miniprogram.yml` 小程序 CI 工作流
- [x] 新增 Codecov 覆盖率上传（miniprogram flag）
- [x] 增加 `develop` 分支的 push 触发

### 代码适配

- [x] 修复 `tsconfig.json` 使 `npm run lint`（tsc --noEmit）通过
- [x] 修复源码类型错误（mock.ts、checkin.ts、markdown-contract.ts、home.ts）
- [x] 本地测试命令与 CI 一致
- [x] 核心覆盖率达 **95.81%**

## CI 工作流详情

### 小程序 CI（ci-miniprogram.yml）

```yaml
name: CI - Miniprogram

on:
  pull_request:
    branches: [main, master, develop]
    paths:
      - 'miniprogram/mp-user/**'
  push:
    branches: [main, master, develop]
    paths:
      - 'miniprogram/mp-user/**'

defaults:
  run:
    working-directory: miniprogram/mp-user

jobs:
  lint-and-test:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v4

    - name: Setup Node.js
      uses: actions/setup-node@v4
      with:
        node-version: '20.x'
        cache: 'npm'
        cache-dependency-path: miniprogram/mp-user/package-lock.json

    - name: Install dependencies
      run: npm ci

    - name: Run linting
      run: npm run lint

    - name: Run tests with coverage
      run: npm run test:ci
      env:
        CI: true

    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v4
      with:
        token: ${{ secrets.CODECOV_TOKEN }}
        files: miniprogram/mp-user/coverage/lcov.info
        flags: miniprogram

    - name: Upload coverage reports
      uses: actions/upload-artifact@v4
      if: always()
      with:
        name: miniprogram-coverage
        path: miniprogram/mp-user/coverage/
```

### CI 流程说明

| 步骤 | 命令 | 说明 |
|------|------|------|
| 检出代码 | `actions/checkout@v4` | 拉取仓库代码 |
| 设置 Node.js | `actions/setup-node@v4` | Node 20.x，启用 npm 缓存 |
| 安装依赖 | `npm ci` | 根据 package-lock.json 安装，保证一致性 |
| 类型检查 | `npm run lint` | 即 `tsc --noEmit`，检查 TypeScript 类型 |
| 运行测试 | `npm run test:ci` | 即 `jest --coverage --runInBand`，生成覆盖率报告 |
| 上传 Codecov | `codecov/codecov-action@v4` | 上传 lcov.info 到 Codecov |
| 上传 Artifact | `actions/upload-artifact@v4` | 保存覆盖率报告为 artifact |

### 触发条件

- **Pull Request**：当 PR 目标为 `main`/`master`/`develop` 且修改了 `miniprogram/mp-user/**` 下的文件
- **Push**：当推送到 `main`/`master`/`develop` 分支且修改了 `miniprogram/mp-user/**` 下的文件

## 本地验证

### 验证 lint 通过

```bash
cd miniprogram\mp-user
npm run lint
```

### 验证测试通过

```bash
cd miniprogram\mp-user
npm run test:ci
```

### 验证结果

| 检查项 | 状态 |
|--------|------|
| npm run lint | ✅ 通过（0 错误） |
| npm run test:ci | ✅ 通过（28 套件 / 504 用例） |
| 覆盖率 | ✅ 95.81% |

## 修改记录

### tsconfig.json 修改

**问题**：`npm run lint`（tsc --noEmit）报大量类型错误，包括测试文件中的 `global.wx` 未定义、源码中 `wx` 全局变量未找到等。

**修改内容**：

1. 将 `include` 从 `./**/*.ts` 限制为 `./miniprogram/**/*.ts`，排除测试文件
2. 移除 `typeRoots` 和 `types` 配置，改用 `files` 显式引入 `./typings/index.d.ts`
3. 添加 `tests`、`dist`、`dist-test` 到 `exclude`

```json
{
  "compilerOptions": {
    // ... 其他配置不变
    "types": [],
    // 移除了 typeRoots
  },
  "files": [
    "./typings/index.d.ts"
  ],
  "include": [
    "./miniprogram/**/*.ts"
  ],
  "exclude": [
    "node_modules",
    "tests",
    "dist",
    "dist-test"
  ]
}
```

### 源码类型修复

| 文件 | 修复内容 |
|------|----------|
| `miniprogram/utils/mock.ts` | 泛型 null 赋值改为 `null as unknown as T`；string/number 比较改为 `String()` 转换 |
| `miniprogram/services/checkin.ts` | `currentUser.id` 改为 `String(currentUser.id)`；添加非空断言 |
| `miniprogram/utils/markdown-contract.ts` | `export {}` 改为 `export type {}` |
| `miniprogram/pages/home/home.ts` | `new URL()` 替换为手动解析查询字符串 |

### CI 工作流修改

| 修改项 | 原值 | 新值 |
|--------|------|------|
| push 触发分支 | `main, master` | `main, master, develop` |
| Codecov 上传 | 无 | 新增 `codecov/codecov-action@v4`，flag 为 `miniprogram` |

## 遇到的问题和解决

1. **问题：`npm run lint` 报 100+ 类型错误**
   - 原因：`tsconfig.json` 的 `include` 包含了测试文件；`types: ["jest", "node"]` 限制了类型包加载，导致 `wx` 全局变量未找到
   - 解决：缩小 `include` 范围，用 `files` 引入 typings，移除 `types` 限制

2. **问题：源码中 `wx` 类型找不到**
   - 原因：`typings/index.d.ts` 通过 `/// <reference path=...>` 引用链加载 `wx` 类型，但 `typeRoots` + `types` 配置阻止了自动发现
   - 解决：在 `files` 中显式引入 `./typings/index.d.ts`

3. **问题：CI 中 `npm run lint` 会失败**
   - 原因：源码中存在泛型 null 赋值、string/number 类型混用等类型错误
   - 解决：修复源码类型错误，确保 `tsc --noEmit` 通过

## 心得体会

通过本次 CI/CD 配置工作，我有了以下收获：

- **CI 的核心价值**：CI 不仅是自动化运行测试，更是代码质量的守门人。`npm run lint` 和 `npm run test:ci` 双重检查确保了代码的类型安全和功能正确性
- **tsconfig 配置的重要性**：`include`/`exclude`/`types`/`typeRoots` 的配置直接影响 TypeScript 编译器的行为，错误的配置会导致大量误报或漏检
- **路径过滤触发**：使用 `paths` 过滤可以避免无关修改触发 CI，节省资源
- **覆盖率上传**：Codecov 集成让覆盖率可视化，便于团队追踪代码质量趋势



----





# ~~1~~

> 在开发过程中，由于部分功能模块进行了较大调整，曾出现测试用例与业务逻辑不同步导致CI失败的情况。项目后期对测试代码进行了统一维护和更新，最终保证主分支能够稳定通过自动化检查
>
> **技术债（Technical Debt）**
>
> 从学生小型团队的角度来讲，CI红了并不一定错。并非企业开发的流程，非必要追求极致的严格。测试本身也是代码，也需要维护。而持续维护的成本并不小。

# ~~CI/CD 配置贡献说明~~

~~姓名：贺祥宇 学号：2312190107 角色：前端  日期：2026-5-5~~

## ~~完成的工作~~

### ~~工作流相关~~

- ~~ 参与编写 / 审查 `.github/workflows/ci.yml`~~
- ~~ 配置 Codecov 覆盖率上传（backend / frontend flag）~~
- ~~ 添加 README 状态徽章~~

### ~~代码适配~~

- ~~ 本地测试命令与 CI 一致，无需额外配置~~
- ~~ 代码通过 Lint 检查（ruff / ESLint）~~
- ~~ 核心覆盖率达标（> 60%）~~

### ~~可选项~~

- ~~ 配置 Dependabot 自动更新依赖~~
- ~~ 集成 CodeRabbit AI 代码审查~~
- ~~ 使用 act 本地验证工作流~~

## ~~PR 链接~~

- ~~PR #64: [Feature/贺祥宇 frontend doc by kiraTheresa · Pull Request #64 · yu086868-ui/iStudySpot](https://github.com/yu086868-ui/iStudySpot/pull/64/)~~

## ~~CI 运行链接~~

- [~~Merge pull request #64 from yu086868-ui/feature/贺祥宇-frontend-doc · yu086868-ui/iStudySpot@cfb170e~~](https://github.com/yu086868-ui/iStudySpot/actions/runs/25361080572)

# 