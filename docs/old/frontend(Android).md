# 一、模块功能

Android 用户端主要包含以下功能模块。

## 1 用户登录模块

该模块用于用户身份认证与会话管理。

主要功能：

- 用户账号登录 / 注册
- 调用后端接口进行身份验证
- 获取并存储 JWT Token
- 自动登录与登录状态保持
- 登录异常提示

登录成功后，系统会在本地保存 Token，用于后续接口访问。

------

## 2 自习室浏览模块

用于展示系统中的自习室信息。

主要功能：

- 查看自习室列表
- 显示自习室基本信息
  - 自习室名称
  - 地址
  - 营业时间
  - 当前上座率
- 点击进入自习室详情页面

该模块帮助用户快速选择合适的学习场所。

------

## 3 座位查看模块

该模块用于展示自习室内部的座位布局。

主要功能：

- 显示自习室座位图
- 使用不同颜色区分座位状态
  - 空闲
  - 已预订
  - 使用中
  - 不可用
- 点击座位查看详细信息
- 选择可用座位进行预订

座位图通过 **Custom View + Canvas** 绘制，实现座位状态的可视化展示。

------

## 4 座位预订模块

用于完成座位预订操作。

主要功能：

- 选择座位
- 选择使用时间（小时 / 天）
- 自动计算预订费用
- 提交预订请求
- 生成订单

系统会在提交预订时调用后端接口，并返回订单信息。

------

## 5 订单管理模块

用于管理用户的预订订单。

主要功能：

- 查看当前订单
- 查看历史订单
- 查看订单详情
- 取消未开始的订单

该模块方便用户管理自己的学习安排。

------

## 6 签到 / 签退模块

用于记录用户实际使用座位的时间。

主要功能：

- 到店扫码签到
- 自动开始计时
- 离开时签退
- 自动生成使用记录

系统会根据实际使用时间计算最终费用。

------

# 二、技术选型

Android 用户端主要采用以下技术。

| 技术              | 用途               |
| ----------------- | ------------------ |
| Kotlin            | Android 主开发语言 |
| Jetpack ViewModel | UI 数据管理        |
| LiveData          | 数据响应式更新     |
| Retrofit          | 网络请求           |
| OkHttp            | HTTP 通信          |
| Glide             | 图片加载           |
| MPAndroidChart    | 数据可视化         |
| Custom View       | 座位图绘制         |

------

## 1 Kotlin

Kotlin 是 Android 官方推荐开发语言，相比 Java 具有以下优势：

- 语法更加简洁
- 空安全机制减少空指针异常
- 与 Android Jetpack 组件兼容性好

------

## 2 Jetpack（ViewModel + LiveData）

项目采用 **MVVM 架构**：

```
View (Activity / Fragment)
        ↓
ViewModel
        ↓
Repository
        ↓
Network
```

优点：

- UI 与业务逻辑分离
- 生命周期安全
- 代码更易维护

------

## 3 Retrofit + OkHttp

用于实现 Android 客户端与后端服务器的 HTTP 通信。

主要功能：

- 调用 RESTful API
- JSON 数据解析
- Token 自动携带
- 网络异常处理

------

## 4 Custom View

用于绘制自习室座位图。

实现方式：

- 使用 Canvas 绘制座位
- 不同颜色表示不同状态
- 支持点击交互

------

# 三、项目目录结构

Android 用户端采用 MVVM 结构组织代码。

```
app/src/main/java/com/example/istudyspot/

├── ui/
│   ├── login/            # 登录界面
│   ├── studyroom/        # 自习室列表
│   ├── seat/             # 座位图页面
│   ├── booking/          # 预订页面
│   ├── order/            # 订单管理
│   └── profile/          # 用户中心
│
├── viewmodel/
│   ├── LoginViewModel.kt
│   ├── StudyRoomViewModel.kt
│   ├── SeatViewModel.kt
│   └── OrderViewModel.kt
│
├── repository/
│   └── MainRepository.kt
│
├── network/
│   ├── ApiService.kt
│   ├── RetrofitClient.kt
│   └── AuthInterceptor.kt
│
├── model/
│   ├── User.kt
│   ├── StudyRoom.kt
│   ├── Seat.kt
│   └── Order.kt
│
├── customview/
│   └── SeatMapView.kt
│
└── utils/
    ├── ConfigManager.kt
    └── NetworkUtil.kt
```

------

# 四、运行方式

使用release的apk安装后运行