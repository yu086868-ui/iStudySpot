# 项目规则（Android前端）

## 技术栈

* 平台：Android
* 语言：Kotlin
* 架构：MVVM（Model-View-ViewModel）
* UI：XML + Jetpack Compose（可选）

---

## 目录结构

* fragment/            # 页面片段
* viewmodel/           # 视图模型
* repository/          # 数据仓库
* network/             # 网络请求
* model/               # 数据模型
* customview/          # 自定义视图
* utils/               # 工具函数

---

## 代码规范

### 通用规则

* 使用 Kotlin 编写逻辑代码
* 保持函数职责单一，避免过长函数
* 变量命名语义清晰（禁止 a、b、temp 等命名）
* 使用 Kotlin 特性（如扩展函数、协程等）提高代码质量

### 页面开发

* 页面逻辑写在 Fragment 中
* 页面布局写在 XML 文件中
* 业务逻辑写在 ViewModel 中
* 避免在 Fragment 中写复杂业务逻辑（应抽离到 ViewModel）

### 组件规范

* 可复用 UI 必须抽离为自定义 View 或 Compose 组件
* 组件命名采用 PascalCase（如 SeatMapView）

### API 规范

* 所有接口调用必须封装在 network/ 中
* 使用 Retrofit 定义 API 接口
* 不允许在 Fragment 中直接调用网络请求

---

## 数据与状态

* 页面状态使用 ViewModel 管理
* 数据绑定使用 LiveData 或 Flow
* 避免跨页面共享隐式状态
* 公共数据通过接口获取，不写死

---

## 样式规范

* 使用 XML 布局文件控制样式，避免硬编码
* 命名采用语义化（如 container、title）
* 避免重复样式，使用 style 和 theme
* 颜色和尺寸定义在资源文件中

---

## 禁止事项（重要）

* 不允许在主线程执行耗时操作
* 不允许直接操作 UI 线程外的视图
* 不允许在 Fragment 中写网络请求逻辑
* 不允许提交未使用的代码
* 不允许修改项目配置文件（除非明确说明）

---

## 开发原则

* 优先保证代码清晰性，而非复杂技巧
* 小步提交，保证每次提交可运行
* 保持目录结构清晰，避免混乱
* 遵循 Android 官方开发规范

---