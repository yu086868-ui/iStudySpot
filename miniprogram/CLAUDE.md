# 项目规则（微信小程序前端）

## 技术栈

- 平台：微信小程序
- 语言：TypeScript
- 结构：WXML + WXSS + TS
- 渲染：Skyline + glass-easel
- 组件框架：glass-easel
- 测试：Jest + ts-jest + miniprogram-simulate

***

## 目录结构

项目主目录为 `mp-user/miniprogram/`，结构如下：

```
mp-user/
├── miniprogram/          # 小程序源码
│   ├── pages/            # 页面目录（页面级逻辑）
│   ├── components/       # 可复用组件
│   ├── services/         # API 请求封装
│   ├── utils/            # 工具函数
│   ├── typings/          # 类型定义
│   ├── assets/           # 项目资源库（图片、图标等静态资源）
│   ├── app.json          # 小程序配置
│   ├── app.ts            # 小程序入口
│   └── app.wxss          # 全局样式
├── tests/                # 单元测试
│   ├── unit/             # 按源码结构镜像的测试目录
│   ├── setup.ts          # 测试环境初始化
│   └── wx-mock.ts        # 微信 API mock
└── demo/                 # 原型与 Demo（非生产代码）
```

### 页面清单

| 页面路径 | 功能 |
|---------|------|
| pages/home/ | 首页 |
| pages/rules/ | 规则公告 |
| pages/profile/ | 个人中心 |
| pages/cards/ | 卡片 |
| pages/seat-selection/ | 选座 |
| pages/study-status/ | 学习状态 |
| pages/index/ | 入口页 |

### 组件清单

| 组件名 | 功能 |
|-------|------|
| navigation-bar | 自定义导航栏 |
| tab-bar | 自定义底部标签栏 |
| card-popup | 卡片弹窗 |
| markdown-card | Markdown 渲染卡片 |
| mp-html | 富文本 HTML 渲染 |

### 服务清单

| 服务模块 | 功能 |
|---------|------|
| auth | 认证登录 |
| user | 用户信息 |
| studyroom | 自习室 |
| seat | 座位 |
| reservation | 预约 |
| checkin | 签到/签退 |
| announcement | 公告 |
| rule | 规则 |
| card | 卡片系统 |
| health-check | 健康检查 |
| metrics | 指标统计 |

***

## 代码规范

### 通用规则

- 使用 TypeScript 编写逻辑代码
- 保持函数职责单一，避免过长函数
- 变量命名语义清晰（禁止 a、b、temp 等命名）

### 页面开发

- 页面逻辑写在 `.ts`
- 页面结构写在 `.wxml`
- 页面样式写在 `.wxss`
- 避免在页面中写复杂业务逻辑（应抽离到 services）

### 组件规范

- 可复用 UI 必须抽离为组件
- 组件命名采用 kebab-case（如 card-popup、markdown-card）
- 组件文件统一使用 `index.` 前缀（如 index.ts、index.wxml、index.wxss、index.json）

### API 规范

- 所有接口调用必须封装在 services/ 中
- 使用统一请求方法（utils/request.ts）
- 不允许在页面中直接调用 wx.request
- services/index.ts 统一导出所有 API 模块

### 资源规范

- 静态资源（图片、图标等）统一放置在 assets/ 目录
- 按类型分子目录管理（如 assets/images/、assets/icons/）
- 资源文件命名采用 kebab-case（如 seat-icon.png）
- 小程序包体积敏感，图片资源需压缩后再添加
- 不允许在代码中硬编码远程图片 URL（应通过接口或 assets 引用）

### 测试规范

- 测试文件放在 `tests/unit/` 下，按源码目录结构镜像
- 测试文件命名：`<模块名>.test.ts`
- 使用 `tests/wx-mock.ts` 提供的微信 API mock
- 运行测试：`npm test`
- 类型检查：`npm run lint`（即 `tsc --noEmit`）

***

## 数据与状态

- 页面状态使用 data 管理
- 避免跨页面共享隐式状态
- 公共数据通过接口获取，不写死
- 全局状态管理使用 utils/store.ts（事件驱动模式）

***

## 样式规范

- 使用类名控制样式，避免内联样式
- 命名采用语义化（如 container、title）
- 避免重复样式，提取公共样式到 app.wxss

***

## 语法兼容性（重要）

微信小程序真机调试环境对部分 ES6+ 语法不支持，必须使用兼容写法：

### 禁止使用的语法

| 禁止语法 | 错误示例 | 正确替代方案 |
|---------|---------|-------------|
| 可选链操作符 | `obj?.prop` | `obj && obj.prop` |
| 可选链调用 | `func?.()` | `func && func()` |
| 可选链数组 | `arr?.[0]` | `arr && arr[0]` |
| 空值合并操作符 | `value ?? default` | `value \|\| default` |
| 类字段初始化 | `class A { x = 1 }` | `class A { x; constructor() { this.x = 1 } }` |

### 代码示例

```typescript
// ❌ 错误：可选链操作符
if (response.data && response.data.list && response.data.list.length > 0) { }

// ✅ 正确：使用 && 判断
if (response.data && response.data.list && response.data.list.length > 0) { }

// ❌ 错误：空值合并操作符
const name = params.name ?? '';

// ✅ 正确：使用 || 运算符
const name = params.name || '';

// ❌ 错误：类字段直接初始化
class NavigationManager {
  private lastPageId: string | null = null;
}

// ✅ 正确：在构造函数中初始化
class NavigationManager {
  private lastPageId: string | null;

  constructor() {
    this.lastPageId = null;
  }
}
```

***

## 禁止事项

- 不允许使用 any 类型（除非必要说明）
- 不允许直接操作 DOM（小程序不推荐）
- 不允许在页面中写网络请求逻辑
- 不允许提交未使用的代码
- 不允许修改项目配置文件（除非明确说明）
- 不允许使用可选链操作符 `?.`
- 不允许使用空值合并操作符 `??`
- 不允许在类中直接初始化字段
- 不允许在源码中硬编码远程资源 URL

***

## 开发原则

- 优先保证代码清晰性，而非复杂技巧
- 小步提交，保证每次提交可运行
- 保持目录结构清晰，避免混乱
- demo/ 目录仅用于原型验证，不作为生产代码

***
