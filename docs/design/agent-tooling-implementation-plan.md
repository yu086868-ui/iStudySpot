# Agent 工具化实施方案

## 1. 目标

本方案用于为 iStudySpot 增加一套独立的 Agent 工具接口层，满足以下目标：

- 不改动现有业务接口。
- Agent 不直接访问数据库，不直接执行任意 SQL。
- 前端可以主导 Agent 编排，但所有真实数据访问和权限控制由后端负责。
- 返回给前端的最终结果统一为结构化 JSON，方便 Compose UI 直接解析、渲染和跳转。
- JWT 作为安全策略的一部分，全量用于 Agent 工具接口。
- 敏感数据不直接暴露给模型或前端，而是通过安全摘要和占位符引用进行传递。

## 2. 非目标

本阶段不做以下内容：

- 不开放任意命令执行能力。
- 不开放任意 SQL 执行能力。
- 不让 Agent 直接访问原始数据库结果。
- 不在现有 `/api/studyrooms`、`/api/reservations`、`/api/chat` 等接口上叠加 Agent 逻辑。
- 不在前端保存模型 API Key。

## 3. 总体原则

### 3.1 前端主导，后端守边界

推荐采用“前端编排，后端提供受限工具能力”的模式：

- 前端负责：
  - 会话状态
  - Agent 推理循环
  - 工具调用时机
  - 工具结果展示
  - 跳转和确认交互
  - Compose UI 渲染

- 后端负责：
  - JWT 鉴权
  - 工具白名单
  - 参数校验
  - 数据查询与业务调用
  - 敏感字段裁剪
  - 占位符映射
  - 审计与日志

### 3.2 独立接口层

Agent 工具层必须独立于现有业务接口，避免污染当前 API 语义。

建议统一挂载在：

```text
/api/agent/tools/**
```

### 3.3 全量结构化输出

后端返回给前端的 Agent 工具结果必须是统一 JSON，而不是仅返回自然语言文本。

前端应默认以 JSON 为第一消费对象，自然语言摘要只作为展示辅助，不作为核心逻辑依据。

## 4. 分层模型

### 4.1 原始数据层

仅后端可见，模型和前端都不直接接触：

- 用户手机号
- userId
- orderNo
- 支付流水
- 实际金额明细
- 内部备注
- 数据库主键关联信息

### 4.2 模型可见安全层

后端在把工具结果交给模型前，必须先构造安全视图：

- 只保留任务需要的信息
- 敏感字段删除或泛化
- 真 ID 替换为临时引用
- 能用布尔状态表达的，不传细节

例如：

```json
{
  "reference": "ORDER_REF_01",
  "status": "paid",
  "timeRange": "2026-06-10 09:00:00 - 2026-06-10 11:00:00",
  "roomName": "三楼安静区",
  "seatPosition": "A12",
  "canCancel": true,
  "canRenew": false,
  "sensitiveFieldsHidden": ["userId", "orderNo", "totalPrice", "totalAmount"]
}
```

### 4.3 前端可见层

前端收到的是可展示 JSON，不应包含模型不该看到、前端也不该看到的隐私字段。

## 5. JWT 安全策略

### 5.1 总原则

所有 Agent 工具接口都应使用 JWT，包括：

- 工具清单读取
- 工具执行
- 后续的 Agent chat / confirm / stream 接口

原因：

- 工具能力属于受限系统能力，不应匿名暴露。
- 工具清单本身反映了系统能力边界，也属于内部协议的一部分。
- 前端在登录后再拉取工具清单，对移动端是可接受的。

### 5.2 身份来源

后端必须从 JWT 中解析用户身份：

- 不信任前端传入的 `userId`
- 工具执行时以 `requestAttribute.userId` 为准
- 所有“我的订单”“我的预约”等查询默认绑定当前登录用户

### 5.3 权限原则

- 读工具：按用户权限返回最小必要数据
- 写工具：必须额外校验业务权限
- 敏感工具：必须做数据脱敏

## 6. 工具化优先于 SQL

### 6.1 当前阶段结论

当前阶段优先使用“工具化封装”，不开放直接 SQL。

理由：

- 现有业务已有 Service 层能力
- 工具化更利于参数约束
- 前后端更容易对齐契约
- 更容易做脱敏
- 更容易审计

### 6.2 将来如需 SQL

只有在后台运营、分析场景下才考虑受限 SQL，并满足：

- 只读
- 白名单表/视图
- 白名单字段
- 自动 LIMIT
- 超时限制
- 审计
- 结果脱敏

普通用户侧 Agent 不应拥有 SQL 能力。

## 7. 接口设计

### 7.0 评审补充说明

在自评过程中发现，Agent 工具接口除了“能返回结构化 JSON”之外，还必须补齐以下约束，否则后续会在前后端联调和安全治理上出现问题：

- 文档必须明确错误协议，而不能只定义成功响应。
- 文档必须明确写操作确认的幂等机制，否则移动端重试会导致重复预约。
- 文档必须明确占位符引用的生命周期，否则引用可能被跨会话滥用。
- 文档必须明确协议版本与兼容性规则，否则 Compose 端迭代会被字段变更卡住。
- 文档必须明确工具结果的“单一真相源”，避免 Agent 工具层和原有业务接口出现规则漂移。

以下章节已按上述问题补充。

## 7.1 工具清单接口

```http
GET /api/agent/tools/catalog
Authorization: Bearer <jwt>
```

返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "name": "list_study_rooms",
      "title": "列出自习室",
      "description": "获取自习室列表，适合用于首页探索和筛选。",
      "requiresAuth": true,
      "tags": ["studyroom", "read"],
      "inputSchema": {
        "status": "string?",
        "floor": "number?",
        "keyword": "string?",
        "page": "number?",
        "pageSize": "number?"
      }
    }
  ],
  "timestamp": 1781015851356
}
```

说明：

- 推荐全部工具在 catalog 中标记 `requiresAuth: true`
- 前端登录后缓存清单即可

## 7.2 工具执行接口

```http
POST /api/agent/tools/execute
Authorization: Bearer <jwt>
Content-Type: application/json
```

请求：

```json
{
  "tool": "list_room_seats",
  "arguments": {
    "studyRoomId": 1,
    "status": "available"
  }
}
```

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "tool": "list_room_seats",
    "summary": "已获取 8 个可用座位。",
    "data": {
      "studyRoomId": 1,
      "items": []
    },
    "uiAction": {
      "type": "navigate",
      "route": "seat_list",
      "params": {
        "studyRoomId": 1
      }
    },
    "references": {}
  },
  "timestamp": 1781015851433
}
```

## 8. 返回协议约定

为适配 Compose UI，建议固定输出协议如下：

```json
{
  "schemaVersion": "1.0",
  "tool": "tool_name",
  "summary": "给用户看的摘要文本",
  "data": {},
  "uiAction": {
    "type": "navigate | open_sheet | show_card | none | confirm",
    "route": "compose_route_name",
    "params": {}
  },
  "references": {}
}
```

### 8.1 字段说明

- `schemaVersion`
  - 协议版本号
  - 前端应以此判断兼容性

- `tool`
  - 当前执行的工具名

- `summary`
  - 给用户看的可读摘要
  - 可直接显示在聊天消息中

- `data`
  - 前端用于渲染的结构化实体
  - 必须稳定、可解析、避免混入自然语言

- `uiAction`
  - 前端动作建议
  - 前端可以采纳，也可以忽略

- `references`
  - 占位符引用映射
  - 用于保护敏感信息、建立可点击引用卡片

### 8.2 `uiAction.type` 约定

建议第一版固定支持以下枚举：

- `navigate`
- `show_card`
- `open_sheet`
- `confirm`
- `none`

### 8.3 `route` 约定

推荐 route 使用前端内部稳定命名，而不是 URL：

- `studyroom_list`
- `studyroom_detail`
- `seat_list`
- `reservation_list`
- `reservation_detail`
- `reservation_rules`

这样 Compose 导航层可以直接映射。

### 8.4 协议兼容性规则

为避免前后端同时演进时互相阻塞，建议遵守以下规则：

- 新增字段应保持向后兼容，旧前端忽略未知字段。
- 已发布字段不应直接改名，应通过新增字段逐步迁移。
- `uiAction.type` 与 `route` 一旦发布，应维护稳定枚举表。
- `schemaVersion` 的主版本变化表示不兼容变更，次版本变化表示兼容扩展。

## 8.5 错误响应协议

Agent 工具接口不能只依赖 HTTP 状态码，建议统一错误体：

```json
{
  "code": 400,
  "message": "MISSING_STUDYROOMID",
  "data": {
    "error": {
      "code": "MISSING_STUDYROOMID",
      "retryable": false,
      "field": "studyRoomId",
      "suggestion": "请补充 studyRoomId 后重试"
    }
  },
  "timestamp": 1781015851433
}
```

建议约定：

- 参数缺失：400
- 未登录：401
- 无权限：403
- 资源不存在：404
- 业务冲突：409
- 不可执行：422
- 系统异常：500

前端不应只依赖 `message` 做分支，而应读取稳定的 `error.code`。

## 9. 敏感数据与占位符策略

### 9.1 为什么需要占位符

如果工具把真实订单内容完整交给模型，会产生两个风险：

- 模型看到不该看到的隐私字段
- 模型在回答中无意泄露敏感信息

因此需要引用占位符。

### 9.2 推荐结构

```json
{
  "summary": "你有一笔即将开始的预约，参考编号为 [ORDER_REF_01]。",
  "data": {
    "items": [
      {
        "reference": "ORDER_REF_01",
        "status": "paid",
        "timeRange": "2026-06-10 09:00:00 - 2026-06-10 11:00:00",
        "roomName": "三楼安静区",
        "seatPosition": "A12",
        "canCancel": true
      }
    ]
  },
  "references": {
    "ORDER_REF_01": {
      "type": "reservation",
      "display": {
        "status": "paid",
        "timeRange": "2026-06-10 09:00:00 - 2026-06-10 11:00:00",
        "roomName": "三楼安静区",
        "seatPosition": "A12"
      }
    }
  }
}
```

### 9.3 不允许直接透出的字段

默认不应直接出现在工具结果中：

- `userId`
- `orderNo`
- `totalPrice`
- `totalAmount`
- 手机号
- 支付方式详情
- 支付流水号
- 内部备注

### 9.4 允许透出的字段

在用户本人场景下，一般可考虑允许：

- 状态
- 时间段
- 房间名
- 座位位置
- 是否可取消
- 是否可续时

### 9.5 占位符生命周期

占位符引用不能被视为长期稳定 ID，建议遵守以下规则：

- 引用默认只在当前响应内有效，或只在当前 Agent 会话内有效。
- 引用必须绑定当前用户身份，不能跨用户复用。
- 引用不能直接映射为数据库主键暴露给模型。
- 若前端需要基于引用继续请求详情，应通过后端引用表或安全映射层解析。

推荐：

- 给模型：`ORDER_REF_01`
- 给前端跳转：后端决定是否返回可用的业务 ID，或返回单独的安全 `actionToken`

不要让模型看到真实订单号，也不要默认把引用当成业务主键。

## 10. 初始工具白名单

建议第一阶段先接入只读工具：

- `list_study_rooms`
- `get_study_room_detail`
- `list_room_seats`
- `get_my_reservations`
- `get_reservation_rules`

第二阶段再加入写工具，但必须走确认机制：

- `create_reservation`
- `cancel_reservation`
- `renew_reservation`
- `checkin_reservation`
- `checkout_reservation`

### 10.1 工具权限矩阵建议

建议为每个工具显式维护：

- 是否需要登录
- 是否只读
- 是否涉及敏感数据
- 是否要求二次确认
- 是否允许模型直接消费结果

示例：

| tool | requiresAuth | readOnly | sensitive | needsConfirm |
| --- | --- | --- | --- | --- |
| list_study_rooms | true | true | false | false |
| list_room_seats | true | true | false | false |
| get_my_reservations | true | true | true | false |
| create_reservation | true | false | true | true |
| cancel_reservation | true | false | true | true |

## 11. 写工具确认机制

写操作不建议由前端直接执行“自由决定”。

建议协议上加入：

```json
{
  "tool": "create_reservation",
  "summary": "准备为你预约明天 09:00-11:00 的 A12 座位。",
  "data": {
    "pendingAction": {
      "action": "create_reservation",
      "arguments": {
        "studyRoomId": 1,
        "seatId": 3,
        "startTime": "2026-06-10 09:00:00",
        "endTime": "2026-06-10 11:00:00"
      }
    }
  },
  "uiAction": {
    "type": "confirm",
    "route": "reservation_confirm",
    "params": {}
  },
  "references": {}
}
```

前端确认后，再请求：

```http
POST /api/agent/tools/confirm
```

本阶段可以先不实现该接口，但协议上应预留。

### 11.1 幂等与重复提交保护

写工具确认机制必须有幂等设计，否则移动端重试、网络抖动或重复点击会导致重复创建订单。

建议在待确认动作中加入：

```json
{
  "pendingAction": {
    "action": "create_reservation",
    "actionToken": "act_9f7d...",
    "idempotencyKey": "idem_2ab1...",
    "expiresAt": "2026-06-10T08:59:00+08:00",
    "arguments": {}
  }
}
```

后端要求：

- `actionToken` 必须短时有效
- `actionToken` 必须绑定当前用户
- `actionToken` 只能消费一次
- `idempotencyKey` 用于抵御网络重试导致的重复写入

前端要求：

- 确认按钮点击后应立即进入 loading
- 同一 `actionToken` 不应重复提交
- 若收到“已执行”响应，应直接刷新结果而不是再次发起写操作

## 12. 与 Compose UI 的适配建议

### 12.1 前端解析方式

Compose 层建议按以下顺序消费结果：

1. 先解析 `uiAction`
2. 再渲染 `summary`
3. 用 `data` 渲染结构化卡片
4. 用 `references` 做引用点击与详情展开

### 12.2 前端不要依赖自然语言

以下逻辑不应依赖 `summary` 文本：

- 页面跳转
- 是否显示按钮
- 是否可取消
- 是否展示确认框

这些都应从 `data` 和 `uiAction` 读取。

### 12.3 建议的前端消费顺序

建议 Compose 层以如下顺序处理工具响应：

1. 校验 `schemaVersion`
2. 若存在 `error`，进入错误分支
3. 读取 `uiAction.type`
4. 将 `data` 映射为 UI model
5. 将 `references` 注册到当前会话作用域
6. 渲染 `summary`

这样可以避免“先显示文本，后发现不能跳转”的状态抖动。

## 13. 审计与日志建议

后端应记录：

- 当前用户 ID
- 工具名
- 输入参数
- 执行时间
- 返回记录数
- 是否命中敏感工具
- 是否触发确认流程
- 关联 traceId / requestId
- 是否命中限流
- 执行耗时和下游依赖耗时

目的：

- 便于调试
- 便于排查越权
- 便于后续优化工具设计

### 13.1 工具治理建议

除了日志外，建议给工具层增加以下治理能力：

- 单工具超时限制
- 用户级限流
- 会话级限流
- 下游失败熔断
- 慢调用告警

原因：

- Agent 前端会比普通页面调用更频繁
- 同一轮编排中可能连续触发多个工具
- 如果没有治理，容易对业务库和业务服务造成放大压力

## 14. 实施分阶段

### 阶段 1：只读工具层

- 新增 `/api/agent/tools/catalog`
- 新增 `/api/agent/tools/execute`
- 接入只读工具
- 返回统一 JSON
- JWT 全量启用
- 建立统一错误协议
- 建立 `schemaVersion`

### 阶段 2：脱敏占位符增强

- 统一抽离安全视图转换器
- 对订单、支付、用户数据做更细粒度脱敏
- 为前端卡片建立稳定引用结构
- 明确引用生命周期与作用域

### 阶段 3：写工具确认机制

- 新增 `confirm` 协议
- 新增待确认动作结构
- 接入预约、取消、续时等写能力
- 加入 `actionToken` 和 `idempotencyKey`

### 阶段 4：Agent 编排层

- 前端主导工具调用循环
- 后端增加 `/api/agent/chat`
- 模型只基于安全工具结果做推理
- 将工具结果区分为“模型可见安全视图”和“前端可见展示视图”

## 14.5 单一真相源原则

Agent 工具层不应重新发明业务规则，也不应长期复制已有控制器或业务模块中的固定常量。

推荐原则：

- 工具层优先调用现有 Service 层
- 规则数据优先来自已有规则源
- 若当前阶段为了快速接入做了临时常量，应在后续阶段回收到统一配置或业务服务

特别注意：

- 预约规则
- 价格规则
- 取消规则
- 签到签退规则

这些若同时存在于旧接口和 Agent 工具层，必须明确谁是唯一真相源，否则前端会遇到同一问题两个答案不一致的情况。

## 15. 当前推荐结论

对于 iStudySpot，推荐采用以下落地方式：

- JWT 必须启用
- 工具层独立开口
- 不改现有业务接口
- 所有结果统一 JSON
- 前端解析 JSON 做 Compose 跳转
- 后端先脱敏，再给模型，再返回前端
- 敏感对象全部走占位符引用
- 当前阶段只做工具化，不做直接 SQL
- 当前阶段必须把错误协议、版本号、幂等确认、引用生命周期一起设计进去

这套方案兼顾了以下几点：

- 前端主导体验与 Agent 编排
- 后端保留数据真相与安全边界
- 现有系统改动可控
- 后续可以平滑扩展到真正的 Agent chat 与 confirm 流程
