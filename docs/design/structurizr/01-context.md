# 范围说明

这个 Structurizr 工作区只建模当前已经实现的 `backend/istudyspot-backend` 与 `frontend/Android` 代码。

后端是一个单体 Spring Boot 应用，而不是一组可独立部署的服务。
Android 端是一个单独的 Jetpack Compose 客户端，通过 Retrofit 调用后端接口。

当前范围内的外部依赖包括：

- DeepSeek API：用于 AI 对话、Agent 推理、客服回复与卡牌文案生成
- 本地图片生成服务：仅用于学习卡牌图片生成
- MySQL：用于持久化业务数据
- 本地文件系统目录：用于保存生成后的卡牌图片

当前工作区明确不覆盖：

- 管理端前端
- 小程序或其他客户端
- 超出代码直接可见范围的生产部署拓扑与基础设施
