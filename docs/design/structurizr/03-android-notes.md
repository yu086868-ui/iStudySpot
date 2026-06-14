# Android 端说明

Android 客户端当前呈现的是一套轻量的 MVVM 结构：

- Compose 页面负责渲染界面
- ViewModels 负责页面状态与交互编排
- `MainRepository` 暴露客户端用例
- `ApiManager` 与 `ApiService` 负责连接 Retrofit
- `ApiClient` 负责 OkHttp、请求头与令牌注入路径
- `ConfigManager` 通过 `SharedPreferences` 持久化访问令牌、用户标识片段与主题模式

模型也显式反映了当前代码的限制：

- 客户端目前主要使用普通请求-响应接口处理 AI、Agent 与客服流程，虽然服务端已经提供了 SSE 端点
- `ApiClient` 虽然包含 refresh token 支持字段，但 `ConfigManager` 尚未真正持久化或回填 refresh token
- 当前 Android 实现中没有单独的本地数据库层
