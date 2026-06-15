# 微信小程序用户端 - iStudySpot

## 一、模块功能

微信小程序用户端主要面向学生、考研党、考证族等学习人群，提供便捷的自习室预订服务。

### 1 用户登录模块

该模块用于用户身份认证与会话管理。

主要功能：

- 微信一键登录
- 调用后端接口进行身份验证
- 获取并存储 JWT Token
- 自动登录与登录状态保持
- 获取用户基本信息（头像、昵称）

登录成功后，系统会在本地缓存 Token，用于后续接口访问。

------

### 2 自习室浏览模块

用于展示系统中的自习室信息。

主要功能：

- 查看自习室列表
- 显示自习室基本信息
  - 自习室名称
  - 地址
  - 营业时间
  - 当前上座率
- 点击进入自习室详情页面
- 支持按城市筛选

该模块帮助用户快速选择合适的学习场所。

------

### 3 座位查看模块

该模块用于展示自习室内部的座位布局。

主要功能：

- 显示自习室座位图
- 使用不同颜色区分座位状态
  - 空闲（绿色）
  - 已预订（黄色）
  - 使用中（红色）
  - 不可用（灰色）
- 点击座位查看详细信息
- 显示座位属性
  - 是否有电源
  - 是否有台灯
  - 是否靠窗
  - 是否静音区
- 选择可用座位进行预订

座位图通过 **Canvas** 绘制，实现座位状态的可视化展示。

------

### 4 座位预订模块

用于完成座位预订操作。

主要功能：

- 选择座位
- 选择使用时间（小时/天）
- 自动计算预订费用
- 显示价格明细
- 提交预订请求
- 生成订单
- 支持在线支付

系统会在提交预订时调用后端接口，并返回订单信息。

------

### 5 订单管理模块

用于管理用户的预订订单。

主要功能：

- 查看当前订单（待使用、使用中）
- 查看历史订单
- 查看订单详情
- 取消未开始的订单
- 查看订单状态流转

该模块方便用户管理自己的学习安排。

------

### 6 签到/签退模块

用于记录用户实际使用座位的时间。

主要功能：

- 到店扫码签到
- 自动开始计时
- 离开时签退
- 自动生成使用记录
- 显示实际使用时长和费用

系统会根据实际使用时间计算最终费用。

------

### 7 个人中心模块

用于管理用户个人信息。

主要功能：

- 查看个人信息
  - 头像
  - 昵称
  - 手机号
- 查看账户余额
- 查看积分
- 账户充值
- 查看违规记录

该模块提供完整的用户信息管理功能。

------

## 二、技术选型

微信小程序用户端主要采用以下技术。

| 技术             | 用途               |
| ---------------- | ------------------ |
| 微信原生框架     | 小程序基础框架     |
| TypeScript       | 类型安全的开发语言 |
| Vant Weapp       | UI组件库           |
| ECharts          | 数据可视化         |
| Canvas           | 座位图绘制         |
| WebSocket        | 实时状态推送       |

------

### 1 微信原生框架

采用微信小程序原生开发方式，具有以下优势：

- 官方支持，性能最优
- 完整的API支持
- 良好的开发工具（微信开发者工具）
- 丰富的组件和API

------

### 2 TypeScript

使用 TypeScript 进行开发，相比 JavaScript 具有以下优势：

- 类型安全，减少运行时错误
- 更好的代码提示和重构支持
- 提高代码可维护性
- 便于团队协作

------

### 3 Vant Weapp

Vant Weapp 是有赞前端团队开发的移动端组件库。

主要特点：

- 丰富的UI组件
- 统一的设计风格
- 良好的文档和社区支持
- 按需引入，减少包体积

------

### 4 Canvas

用于绘制自习室座位图。

实现方式：

- 使用 Canvas 2D API 绘制座位
- 不同颜色表示不同状态
- 支持点击交互
- 支持缩放和拖动

------

### 5 WebSocket

用于接收实时座位状态变化。

主要功能：

- 订阅自习室座位状态
- 实时接收座位状态变化通知
- 接收订单提醒（签到提醒等）

------

## 三、项目目录结构

微信小程序用户端采用标准的小程序目录结构。

```
miniprogram/
├── pages/                    # 页面目录
│   ├── index/               # 首页
│   │   ├── index.ts         # 页面逻辑
│   │   ├── index.wxml       # 页面结构
│   │   ├── index.wxss       # 页面样式
│   │   └── index.json       # 页面配置
│   │
│   ├── login/               # 登录页
│   │   ├── login.ts
│   │   ├── login.wxml
│   │   ├── login.wxss
│   │   └── login.json
│   │
│   ├── room-list/           # 自习室列表
│   │   ├── room-list.ts
│   │   ├── room-list.wxml
│   │   ├── room-list.wxss
│   │   └── room-list.json
│   │
│   ├── room-detail/         # 自习室详情
│   │   ├── room-detail.ts
│   │   ├── room-detail.wxml
│   │   ├── room-detail.wxss
│   │   └── room-detail.json
│   │
│   ├── seat-map/            # 座位图
│   │   ├── seat-map.ts
│   │   ├── seat-map.wxml
│   │   ├── seat-map.wxss
│   │   └── seat-map.json
│   │
│   ├── booking/             # 预订页面
│   │   ├── booking.ts
│   │   ├── booking.wxml
│   │   ├── booking.wxss
│   │   └── booking.json
│   │
│   ├── order-list/          # 订单列表
│   │   ├── order-list.ts
│   │   ├── order-list.wxml
│   │   ├── order-list.wxss
│   │   └── order-list.json
│   │
│   ├── order-detail/        # 订单详情
│   │   ├── order-detail.ts
│   │   ├── order-detail.wxml
│   │   ├── order-detail.wxss
│   │   └── order-detail.json
│   │
│   ├── profile/             # 个人中心
│   │   ├── profile.ts
│   │   ├── profile.wxml
│   │   ├── profile.wxss
│   │   └── profile.json
│   │
│   └── checkin/             # 签到/签退
│       ├── checkin.ts
│       ├── checkin.wxml
│       ├── checkin.wxss
│       └── checkin.json
│
├── components/              # 自定义组件
│   ├── seat-item/          # 座位项组件
│   ├── time-picker/        # 时间选择器
│   └── order-card/         # 订单卡片
│
├── utils/                   # 工具函数
│   ├── request.ts          # 网络请求封装
│   ├── auth.ts             # 认证相关
│   ├── storage.ts          # 本地存储封装
│   ├── util.ts             # 通用工具函数
│   └── config.ts           # 配置文件
│
├── api/                     # API接口
│   ├── user.ts             # 用户相关接口
│   ├── room.ts             # 自习室相关接口
│   ├── seat.ts             # 座位相关接口
│   └── order.ts            # 订单相关接口
│
├── types/                   # TypeScript类型定义
│   ├── user.ts
│   ├── room.ts
│   ├── seat.ts
│   └── order.ts
│
├── app.ts                   # 小程序入口
├── app.json                 # 小程序配置
├── app.wxss                 # 全局样式
│
└── project.config.json      # 项目配置
```

------

## 四、运行方式

### 4.1 环境要求

- 微信开发者工具（最新稳定版）
- Node.js 14.0+
- 后端服务已启动（http://localhost:8080）

------

### 4.2 配置步骤

1. **修改后端接口地址**

   编辑 `utils/config.ts` 文件，配置后端接口地址：

   ```typescript
   export const BASE_URL = 'http://localhost:8080/api'
   ```

2. **配置小程序AppID**

   在 `project.config.json` 中配置小程序 AppID：

   ```json
   {
     "appid": "your_appid_here"
   }
   ```

------

### 4.3 启动步骤

1. **安装依赖**

   ```bash
   cd miniprogram/mp-user
   npm install
   ```

2. **构建npm包**

   在微信开发者工具中：
   - 点击菜单栏：工具 -> 构建 npm

3. **打开项目**

   - 打开微信开发者工具
   - 选择"导入项目"
   - 选择 `miniprogram/mp-user` 目录
   - 填写项目名称和 AppID

4. **运行项目**

   - 在微信开发者工具中点击"编译"按钮
   - 项目将在模拟器中运行

------

### 4.4 调试方式

1. **查看控制台日志**

   - 在微信开发者工具底部选择"调试器"
   - 查看 Console 面板

2. **网络请求调试**

   - 在调试器中选择 Network 面板
   - 查看所有网络请求详情

3. **真机调试**

   - 点击工具栏"真机调试"按钮
   - 使用微信扫描二维码
   - 在手机上查看实际效果

------

### 4.5 常见问题

1. **网络请求失败**

   - 检查后端服务是否启动
   - 检查 `BASE_URL` 配置是否正确
   - 确保后端接口允许跨域

2. **登录失败**

   - 检查 AppID 是否正确
   - 确认后端登录接口正常
   - 查看控制台错误信息

3. **Canvas绘制异常**

   - 检查Canvas组件是否正确配置
   - 确认Canvas上下文获取时机
   - 查看Canvas相关API调用

------

## 五、核心功能实现说明

### 5.1 网络请求封装

使用 `wx.request` 封装统一的请求方法，支持：

- 自动携带 Token
- 统一错误处理
- 请求/响应拦截
- Loading 状态管理

```typescript
// utils/request.ts
function request<T>(options: RequestOptions): Promise<T> {
  const token = wx.getStorageSync('token')
  
  return new Promise((resolve, reject) => {
    wx.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      success: (res) => {
        if (res.data.code === 200) {
          resolve(res.data.data)
        } else {
          reject(res.data)
        }
      },
      fail: reject
    })
  })
}
```

------

### 5.2 座位图绘制

使用 Canvas 绘制座位图，支持：

- 座位状态渲染
- 点击交互
- 缩放功能

```typescript
// pages/seat-map/seat-map.ts
drawSeatMap() {
  const ctx = wx.createCanvasContext('seatCanvas')
  const seats = this.data.seats
  
  seats.forEach(seat => {
    ctx.setFillStyle(this.getSeatColor(seat.status))
    ctx.fillRect(seat.x, seat.y, seat.width, seat.height)
    
    ctx.setFillStyle('#333')
    ctx.fillText(seat.number, seat.x + 10, seat.y + 20)
  })
  
  ctx.draw()
}
```

------

### 5.3 WebSocket连接

使用 WebSocket 接收实时座位状态变化：

```typescript
connectWebSocket() {
  const token = wx.getStorageSync('token')
  wx.connectSocket({
    url: `ws://localhost:8080/ws/seat?token=${token}`
  })
  
  wx.onSocketMessage((res) => {
    const data = JSON.parse(res.data)
    if (data.type === 'seat_status_change') {
      this.updateSeatStatus(data.data)
    }
  })
}
```

------

## 六、API接口调用示例

### 6.1 用户登录

```typescript
// api/user.ts
export function login(code: string) {
  return request({
    url: '/user/login',
    method: 'POST',
    data: { code }
  })
}

// 使用示例
wx.login({
  success: (res) => {
    login(res.code).then(data => {
      wx.setStorageSync('token', data.token)
      wx.setStorageSync('userInfo', data.userInfo)
    })
  }
})
```

------

### 6.2 获取自习室列表

```typescript
// api/room.ts
export function getRoomList(params: { city?: string, page: number, size: number }) {
  return request({
    url: '/room/list',
    method: 'GET',
    data: params
  })
}

// 使用示例
getRoomList({ city: '北京', page: 1, size: 10 }).then(data => {
  this.setData({ roomList: data.list })
})
```

------

### 6.3 创建订单

```typescript
// api/order.ts
export function createOrder(params: { seatId: number, startTime: string, endTime: string }) {
  return request({
    url: '/order/create',
    method: 'POST',
    data: params
  })
}

// 使用示例
createOrder({
  seatId: 1,
  startTime: '2024-03-09 14:00:00',
  endTime: '2024-03-09 17:00:00'
}).then(data => {
  wx.navigateTo({
    url: `/pages/order-detail/order-detail?orderId=${data.orderId}`
  })
})
```

------

## 七、开发规范

### 7.1 命名规范

- 文件名：使用短横线分隔（kebab-case）
- 变量名：使用驼峰命名（camelCase）
- 常量名：使用全大写下划线分隔（UPPER_SNAKE_CASE）

### 7.2 代码风格

- 使用 TypeScript 进行类型定义
- 使用 async/await 处理异步操作
- 统一使用单引号
- 代码缩进使用2个空格

### 7.3 注释规范

- 文件头部添加文件说明注释
- 复杂逻辑添加行内注释
- API接口添加JSDoc注释

------

## 八、后续优化方向

1. **性能优化**
   - 图片懒加载
   - 列表分页加载
   - 骨架屏优化

2. **用户体验**
   - 添加加载动画
   - 优化错误提示
   - 增加操作引导

3. **功能扩展**
   - 添加收藏功能
   - 支持评价系统
   - 增加消息通知
