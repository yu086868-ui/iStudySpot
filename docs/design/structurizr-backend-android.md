# iStudySpot 后端与 Android Structurizr 模型

这个目录现在包含一份基于代码整理的 Structurizr DSL 模型，覆盖当前 `backend/istudyspot-backend` 与 `frontend/Android` 范围：

- DSL 文件：`docs/design/structurizr-backend-android.dsl`
- 建模范围：Android App、Backend API、MySQL、本地卡牌图片存储、DeepSeek API 与本地图片生成服务
- 明确排除：管理端前端、小程序，以及超出本次后端与 Android 可见范围的部署拓扑

当前模型包含：

- C1：学生用户、iStudySpot 与外部 AI/图片服务的系统上下文
- C2：Android App、Backend API、MySQL 与本地卡牌图片目录的容器图
- C3：按当前包结构与类职责划分的后端组件图与 Android 组件图；后端组件图已下钻到 Agent 的 LLM 策略守卫、LLM 编排器、只读工具网关与短期会话上下文

补充说明：

- 后端被建模为一个 Spring Boot 容器，因为当前代码库是模块化单体，而不是多个可独立部署的服务。
- Android 端被建模为一个移动端容器，因为这和当前 Compose + MVVM 结构一致。
- DSL 已经显式反映当前实现限制：Android 目前主要使用普通请求-响应接口而不是 SSE 页面流程；refresh token 持久化尚未通过 `ConfigManager` 接通；AI、Agent 与客服会话保存在后端内存中；公告与统计接口仍以占位返回为主。
- Agent 被建模为只读信息助手：敏感意图由 LLM 读取 `ai-rules.json` 规则后主动判定，用户要求 Agent 代办预约、取消、签到、签退、支付或续时等写操作时返回只读说明；后端工具网关仍以只读工具白名单作为最终边界。
- 目前没有继续下钻到 C4 代码/类图，因为项目现有特性类过多，手工维护整套类级图不稳定；如果后续需要，可以再按 `reservation`、`ai-agent`、`card` 等局部领域单独细化。

这个 DSL 的常用用法：

```bash
structurizr validate -workspace docs/design/structurizr-backend-android.dsl
structurizr export -workspace docs/design/structurizr-backend-android.dsl -format mermaid
```

如果团队通过本地 Structurizr Web 查看器预览，直接让查看器指向工作区根目录，并加载上面的 DSL 文件即可。

关于本地 Web 预览与 IDE 友好的导出方式，可参考：

- `docs/design/structurizr-viewing.md`
- `scripts/run-structurizr-lite.ps1`
- `scripts/export-structurizr-diagrams.ps1`
