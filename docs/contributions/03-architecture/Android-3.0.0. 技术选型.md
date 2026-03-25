### 前端（Android）

技术栈：
- Kotlin（主开发语言）
- Jetpack（ViewModel/LiveData）
- Retrofit（网络请求）
- OkHttp（HTTP通信）
- Glide（图片加载）
- MPAndroidChart（数据可视化）
- Custom View（座位图绘制）

运行环境：
- Android Studio
- Android SDK（API 21+）

通信方式：
- 使用 Retrofit 与后端进行 RESTful API 通信
- OkHttp 作为底层 HTTP 客户端

选择理由：
1. Kotlin 是 Android 官方推荐开发语言，语法简洁，空安全机制减少空指针异常
2. Jetpack 组件（ViewModel/LiveData）实现 MVVM 架构，提高代码可维护性
3. Retrofit 简化网络请求代码，提供类型安全的 API 接口定义
4. Custom View 实现座位图绘制，提供直观的用户体验
5. MPAndroidChart 用于数据可视化，展示自习室使用情况

补充说明：
当前项目基于 Android Studio 初始化，采用 MVVM 架构模式，具备完整的项目结构和依赖管理，符合工程化开发要求