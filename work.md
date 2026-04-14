# 项目架构调整记录

## 调整目标
根据 `frontend/Android/docs/architecture.md` 和 `frontend/Android/docs/work.md` 文档要求，将项目从 XML 布局迁移至 Jetpack Compose UI。

## Compose UI 迁移记录

### 1. 添加 Compose 依赖
- **文件**：`app/build.gradle.kts`
- **修改内容**：
  - 添加 `kotlin-compose` 插件
  - 启用 `buildFeatures.compose = true`
  - 添加 Compose BOM 2024.12.01
  - 添加 material3、ui、ui-tooling-preview、material-icons-extended
  - 添加 activity-compose、lifecycle-viewmodel-compose、lifecycle-runtime-compose
  - 添加 navigation-compose
- **原因**：work.md 要求迁移至 Compose UI

### 2. 创建 Compose 主题
- **文件**：`ui/theme/Theme.kt`
- **修改内容**：创建 IStudySpotTheme，支持亮色/暗色/动态颜色
- **原因**：统一应用主题风格

### 3. 迁移 HomeFragment → Compose
- **文件**：`ui/screen/HomeScreen.kt` + `fragment/HomeFragment.kt`
- **修改内容**：
  - 创建 `HomeScreen` Composable，使用 LazyVerticalGrid 展示功能项
  - 使用 Material Icons 替代原来的图标
  - Fragment 使用 ComposeView 承载 Compose 内容
- **原因**：将 XML 布局替换为 Compose 声明式 UI

### 4. 迁移 ProfileFragment → Compose
- **文件**：`ui/screen/ProfileScreen.kt` + `fragment/ProfileFragment.kt`
- **修改内容**：
  - 创建 `ProfileScreen` Composable
  - 使用 CircleShape 头像、Card 信息卡片
  - 支持点击头像跳转登录、查看订单
  - 从 ConfigManager 读取登录状态显示用户信息
- **原因**：统一 UI 风格

### 5. 迁移 LoginFragment → Compose
- **文件**：`ui/screen/LoginScreen.kt` + `fragment/LoginFragment.kt`
- **修改内容**：
  - 创建 `LoginScreen` Composable
  - 使用 OutlinedTextField 输入框、Button 登录按钮
  - 密码使用 PasswordVisualTransformation
  - 登录成功后保存 token 和用户信息到 ConfigManager
- **原因**：声明式 UI 更简洁

### 6. 迁移 RegisterFragment → Compose
- **文件**：`ui/screen/RegisterScreen.kt` + `fragment/RegisterFragment.kt`
- **修改内容**：
  - 创建 `RegisterScreen` Composable
  - 添加确认密码字段
  - 修复 `response.data.userId` → `response.data.user.id`
  - 注册成功后保存 token 和用户信息到 ConfigManager
- **原因**：与 LoginScreen 风格一致

### 7. 迁移 StudyRoomFragment → Compose
- **文件**：`ui/screen/StudyRoomScreen.kt` + `fragment/StudyRoomFragment.kt`
- **修改内容**：
  - 创建 `StudyRoomScreen` Composable
  - 使用 LazyColumn + Card 展示自习室列表
  - 支持加载状态 CircularProgressIndicator
- **原因**：列表页面更适合 Compose

### 8. 迁移 SeatFragment → Compose
- **文件**：`ui/screen/SeatMapScreen.kt` + `fragment/SeatFragment.kt`
- **修改内容**：
  - 创建 `SeatMapScreen` Composable
  - 使用 LazyVerticalGrid 展示座位图
  - 座位颜色根据状态变化（绿/橙/红/灰）
  - 添加座位状态图例
- **原因**：网格布局用 Compose 更灵活

### 9. 迁移 BookingFragment → Compose
- **文件**：`ui/screen/BookingScreen.kt` + `fragment/BookingFragment.kt`
- **修改内容**：
  - 创建 `BookingScreen` Composable
  - 显示自习室、座位、价格信息
  - 输入开始/结束时间和预订类型
- **原因**：表单页面用 Compose 更简洁

### 10. 迁移 OrderListFragment → Compose
- **文件**：`ui/screen/OrderListScreen.kt` + `fragment/OrderListFragment.kt`
- **修改内容**：
  - 创建 `OrderListScreen` Composable
  - 使用 LazyColumn + Card 展示订单列表
  - 支持空状态提示
- **原因**：统一列表 UI 风格

### 11. 迁移 OrderFragment → Compose
- **文件**：`ui/screen/OrderDetailScreen.kt` + `fragment/OrderFragment.kt`
- **修改内容**：
  - 创建 `OrderDetailScreen` Composable
  - 展示订单详情信息
  - 根据状态显示签到/签退/取消按钮
- **原因**：详情页面用 Compose 更清晰

### 12. 迁移 MoreFragment → Compose
- **文件**：`ui/screen/MoreScreen.kt` + `fragment/MoreFragment.kt`
- **修改内容**：
  - 创建 `MoreScreen` Composable
  - 使用分组 Card + Row 展示功能列表
  - 使用 Material Icons 替代原图标
- **原因**：设置页面用 Compose 更优雅

### 13. 迁移简单页面 → Compose
- **文件**：`ui/screen/SimpleScreens.kt`
- **修改内容**：
  - 创建 `GuideScreen` - 场馆导览
  - 创建 `RulesScreen` - 使用规则
  - 创建 `StudyRecordScreen` - 学习记录
  - 创建 `NotificationScreen` - 通知提醒
- **原因**：统一所有页面为 Compose UI

### 14. 修复编译错误
- **文件**：`fragment/RegisterFragment.kt`
- **修改内容**：`response.data.userId` → `response.data.user.id`，`response.data.username` → `response.data.user.username`
- **原因**：RegisterResponse 的字段结构是 `user: UserInfo`，不是直接的 `userId/username`

## API Mock 数据检查

### Mock 数据状态
所有 API 方法都已实现 Mock 数据（`useMockData = true`）：

| API 类别 | 方法 | Mock 状态 | 说明 |
|---------|------|----------|------|
| **认证** | login | ✅ | 返回 mock_token 和 UserInfo |
| | register | ✅ | 返回 mock_token 和 UserInfo |
| | refreshToken | ✅ | 返回 mock_token |
| **自习室** | getStudyRooms | ✅ | 返回 2 个自习室数据 |
| | getStudyRoomDetail | ✅ | 返回指定自习室详情 |
| | getStudyRoomSeats | ✅ | 返回 5x8 座位矩阵，多种状态 |
| | getSeatDetail | ✅ | 返回座位详情 |
| **订单** | createOrder | ✅ | 创建订单，返回订单信息 |
| | getUserOrders | ✅ | 返回 2 个示例订单（pending/paid）|
| | getOrderDetail | ✅ | 返回订单详情 |
| | cancelOrder | ✅ | 返回取消成功状态 |
| **签到** | checkin | ✅ | 返回签到成功状态 |
| | checkout | ✅ | 返回签退成功状态 |
| **用户** | getUserInfo | ✅ | 返回用户信息 |
| | updateUserInfo | ✅ | 返回更新后的用户信息 |
| | changePassword | ✅ | 返回成功状态 |
| **支付** | createPayment | ✅ | 返回支付信息 |
| | getPaymentStatus | ✅ | 返回支付状态 |
| **统计** | getStudyRoomStatistics | ✅ | 返回统计数据 |

### Mock 数据特点
1. **登录/注册**：返回 mock_token，可用于测试需要 token 的 API
2. **自习室列表**：2 个自习室，包含名称、地址、营业时间、上座率
3. **座位图**：5x8 矩阵，状态根据行列计算（available/booked/occupied）
4. **订单列表**：2 个示例订单，不同状态（pending/paid）
5. **订单详情**：包含完整订单信息，可用于测试签到/签退/取消

### 登录状态保存
- **LoginFragment**：登录成功后保存 token、userId、username、nickname 到 ConfigManager
- **RegisterFragment**：注册成功后同样保存用户信息
- **ProfileScreen**：使用 LaunchedEffect 读取 ConfigManager 显示登录状态

## 迁移策略

采用 **Fragment + ComposeView** 的混合迁移策略：
1. Fragment 仍然作为 Navigation 的目的地
2. Fragment 的 `onCreateView` 使用 `ComposeView` 替代 XML 布局
3. Compose UI 通过 `setContent` 设置
4. 使用 `ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed` 管理生命周期

这种策略的优点：
- 不需要修改 Navigation Graph
- 不需要修改 MainActivity
- 可以逐步迁移，不需要一次性替换所有页面
- Fragment 仍然处理导航和业务逻辑

## 构建状态
✅ **BUILD SUCCESSFUL** - 所有页面已迁移至 Compose UI，API Mock 数据已完善
