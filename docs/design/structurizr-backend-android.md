# iStudySpot 后端、Android 与管理员 Web Structurizr 模型

这个目录包含一份基于当前代码整理的 Structurizr DSL 模型，覆盖 `backend/istudyspot-backend`、`frontend/Android` 与 `admin` 范围。

- DSL 源文件：`docs/design/structurizr-backend-android.dsl`
- 同步后的 Lite 工作区：`docs/design/workspace.dsl`
- 建模范围：Android App、Web 管理员端、Backend API、MySQL、本地卡牌图片存储、DeepSeek API 与本地图片生成服务
- 明确排除：小程序，以及超出当前后端、Android 与管理员 Web 可见范围的部署拓扑

当前模型包含：

- C1：学生用户、系统管理员、iStudySpot 与外部 AI/图片服务的系统上下文
- C2：Android App、Web 管理员端、Backend API、MySQL 与本地卡牌图片目录的容器图
- C3：后端组件图、Android 组件图与 Web 管理员端组件图

补充说明：

- Web 管理员端已按当前实际页面建模：仪表盘、用户管理、自习室/座位管理、订单管理、公告管理、规则内容和系统健康。
- Web 管理员端不再包含支付查询、学习卡牌或 AI 管理入口；这些已从当前 admin 前端界面移除。
- 后端仍建模为一个 Spring Boot 模块化单体，因为当前代码库不是多个可独立部署的微服务。
- Android 端仍保留 `ViewModel -> Repository -> API 管理层 -> 后端` 的叙事结构。
- Agent 被建模为只读信息助手：敏感意图由规则与 LLM 判定，后端工具网关以只读工具白名单作为最终边界。

常用命令：

```bash
structurizr validate -workspace docs/design/structurizr-backend-android.dsl
structurizr export -workspace docs/design/structurizr-backend-android.dsl -format mermaid
```

本地 Web 预览和导出方式可参考：

- `docs/design/structurizr-viewing.md`
- `scripts/run-structurizr-lite.ps1`
- `scripts/export-structurizr-diagrams.ps1`
