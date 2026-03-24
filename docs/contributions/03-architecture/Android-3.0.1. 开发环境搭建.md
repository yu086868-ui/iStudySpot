### 前端开发环境

开发工具：
- Android Studio（用于Android应用开发、调试与预览）
- JDK 11+（Java开发工具包）

项目初始化：
- 使用 Android Studio 新建项目
- 选择 Empty Activity 模板
- 配置项目名称和包名

关键配置说明：

1. build.gradle.kts（项目级）
- 配置 Gradle 版本
- 配置仓库地址
- 定义依赖版本

2. build.gradle.kts（应用级）
- 配置应用版本信息
- 添加 Kotlin 插件
- 添加 Jetpack 组件依赖
- 添加网络请求依赖（Retrofit、OkHttp）
- 添加其他第三方库依赖

3. AndroidManifest.xml
- 配置应用权限
- 配置活动声明
- 配置应用图标和主题

4. 项目结构：
- app/src/main/java/：Java/Kotlin 源代码
- app/src/main/res/：资源文件（布局、图片、字符串等）
- app/src/main/AndroidManifest.xml：应用配置文件

开发流程：
1. 打开 Android Studio
2. 导入或新建项目
3. 配置依赖项
4. 编写代码
5. 运行应用进行测试
6. 构建APK发布