# AI 功能集成贡献说明

姓名：贺祥宇
学号：2312190107
日期：2026-06-15

## 我完成的工作

### 一、卡片系统——AI 生成 + 游戏化收藏

卡片系统是项目中最核心的 AI 功能，定位为：**学习行为 -> AI 生成卡片 -> 游戏化收藏**。核心不是抽卡，而是"用收藏的方式，记录你认真投入过的时间"。

#### 1.1 卡片数据结构设计

定义在 `typings/api.ts` 中的 `Card` 接口：

| 字段 | 类型 | 说明 |
|------|------|------|
| uuid | string | 卡片唯一 ID |
| userID | string | 所属用户 |
| cardID | string | 卡片模板/生成编号 |
| createTime | string | 创建时间 |
| studyDuration | number | 学习时长（分钟） |
| rarity | CardRarity | 稀有度（N/R/SR/SSR/UR/LR） |
| borderTheme | string | 边框主题 |
| cardTheme | string | 卡面主题 |
| themeCategory | ThemeCategory | 主题分类 |
| markdown | string | AI 生成的 Markdown 文本 |
| imageURL | string | AI 生成的配图 URL |

#### 1.2 稀有度系统设计

六档稀有度，由**学习时长**驱动概率池计算，AI 不决定稀有度：

| 等级 | 颜色 | 边框色 | 内容特点 |
|------|------|--------|----------|
| N | 白 | #CCCCCC | 基础鼓励 |
| R | 绿 | #4CAF50 | 学习成长 |
| SR | 蓝 | #2196F3 | 思考 |
| SSR | 紫 | #9C27B0 | 哲思与审美 |
| UR | 金 | #FFD700 | 未来/史诗（特殊卡面） |
| LR | 红 | #F44336 | 隐藏主题（特殊卡面） |

概率池按学习时长分档（0-10min/10-30min/30-60min/60-120min/120-240min/240min+），时长越长高品质概率越高，LR 红卡需 120min 以上才开放且概率极低（1-2%）。无保底机制。

#### 1.3 主题分类系统

七类主题，按概率分配：

| 主题 | 占比 | 内容 |
|------|------|------|
| growth（励志成长） | 25% | 自律、努力、学习意义 |
| philosophy（哲思感悟） | 20% | 微型思考、余味表达 |
| history（名人与历史） | 15% | 历史人物、科学家、文学引用 |
| nature（自然意象） | 15% | 星海、雨夜、山川、四季 |
| tech（科技未来） | 10% | AI、太空、科幻 |
| companion（温柔陪伴） | 10% | 理解、缓慢成长、轻陪伴 |
| hidden（隐藏主题） | 5% | 神话、梦境、史诗感（仅 UR/LR 开放） |

#### 1.4 AI 文本协议（Prompt Contract）

采用双层设计：

- **Layer1 - Markdown Engine**（`markdown-engine.ts`）：基于 markdown-it 的通用渲染，输出 HTML
- **Layer2 - Prompt Contract**（`markdown-contract.ts`）：项目级规则，包括：
  - 标题限制：建议仅使用一级标题 `#`
  - 文本长度：80-300 字，不超过 500 字
  - 分割协议：`---` 语义为 Card Section Divider（非普通 hr），先 split 再逐块渲染
  - HTML 不鼓励使用
  - 校验产生 warnings 但不阻止渲染

#### 1.5 AI 图片生成 Demo

在 `demo/card system/AI image generation/` 中集成了**火山引擎即梦 AI 文生图 3.0**：
- 异步任务模式：先提交任务获取 `task_id`，再轮询查询结果
- 使用火山引擎 V4 签名机制（HMAC-SHA256）
- 生成 1024x1024 图片，返回 URL 有效期 24 小时
- 后端 Node.js 代理处理签名和下载

### 二、卡片前端实现

#### 2.1 卡片服务层（services/card.ts）

提供四个 API 方法：
- `generateCard(params)` — 非流式生成卡片
- `getCardDetail(uuid)` — 获取卡片详情（带本地缓存）
- `getCardList(params)` — 获取卡片列表（带本地缓存）
- **`generateCardStream(params, callbacks)`** — 流式生成卡片（核心方法）

流式生成使用 SSE 协议，事件类型：
- `init` — 返回 rarity、themeCategory、borderTheme、cardTheme
- `text` — 逐字返回 Markdown 内容
- `complete` — 返回完整 Card 对象
- `error` — 错误信息

所有方法都支持 Mock 模式（`ENABLE_MOCK = true`），Mock 管理器完整模拟了卡片生成的流式过程。

#### 2.2 卡片弹窗组件（card-popup）

- 支持两种模式：**流式模式**（`streaming=true`）和**完整卡片模式**
- 流式模式下实时显示 `streamingHtml`、`streamingRarity`、`streamingThemeCategory`，带闪烁光标动画
- 完整模式下渲染 `card.markdown`、`card.imageURL`、`card.createTime`、`card.studyDuration`
- 稀有度决定边框颜色和视觉特效：SSR 有呼吸光效，UR 有金色渐变深色背景，LR 有红色脉动光效
- 弹窗动画：scale + opacity 过渡，`cubic-bezier(0.34, 1.56, 0.64, 1)` 弹性缩放

#### 2.3 卡片收藏页面（pages/cards/）

- 展示用户所有卡片，支持三种排序：默认/稀有度/获取时间
- 双列瀑布流布局，每张卡片显示：稀有度徽章、配图、Markdown 内容、时间/时长
- 点击卡片弹出 card-popup 查看详情
- 内置"卡片稀有度说明"文档弹窗（Markdown 渲染）
- 通过 store 事件订阅实现卡片数据实时更新

#### 2.4 学习状态页中的卡片生成入口（pages/study-status/）

这是卡片生成的入口页面：
1. 用户结束学习会话 -> 计算学习时长
2. 调用 `cardApi.generateCardStream()` 进入流式生成
3. 先弹出 card-popup 进入流式模式（显示 rarity + 逐字 Markdown）
4. 流式完成后切换为完整卡片模式
5. 用户点击"收下卡片"后返回首页

### 三、Demo 实验系统

#### 3.1 卡片视觉效果 Demo（`demo/card system/` 根目录）

纯 HTML/CSS/JS 的 Web Demo，展示六种稀有度的卡片视觉效果：
- 普通卡（N/R/SR/SSR）：边框颜色变化，普通白色卡面
- 特殊卡（UR 金/LR 红）：深色渐变背景、光效跟随鼠标、3D 倾斜交互
- SSR 有呼吸光效动画，LR 有脉动动画

#### 3.2 Markdown Display Demo（`demo/card system/md display/`）

微信小程序独立 Demo，验证 Markdown 渲染链路：
- 使用 markdown-it + mp-html
- 实现 `---` 分割为独立 Card Block 的协议
- 包含 `markdown-engine.ts`（通用渲染）和 `markdown-contract.ts`（协议校验）
- 这些代码已迁移至主工程的 `utils/` 目录

#### 3.3 AI 图片生成 Demo（`demo/card system/AI image generation/`）

Node.js 后端 + 前端的完整 Demo，集成即梦 AI 文生图 3.0。

### 四、后端 AI 架构

#### 4.1 AIController API 端点

- **`GET /api/characters`** — 获取角色列表
- **`POST /api/chat`** — 非流式聊天，请求体含 `session_id`、`character_id`、`message`，返回 `{"reply": "..."}`
- **`POST /api/chat/stream`** — 流式聊天（SSE），请求体同上，返回 SSE 事件流：`start` -> `delta`(逐字) -> `end`

#### 4.2 角色扮演系统（Character/Session/Message）

三个核心实体：
- **Character**：`id`、`name`、`persona`（性格）、`speaking_style`（说话风格）
- **Session**：`session_id`、`character_id`、`messages[]`，支持 `addMessage()` 和 `getRecentMessages(limit)`
- **Message**：`role`（user/assistant）、`content`

硬编码了三个角色：科学家、老师、艺术家。

核心流程：
```
用户输入 -> 写入 session.messages -> 构建 systemPrompt -> 截取 recentMessages(10条) -> 拼接 finalMessages -> 调用 LLM API -> 写入 assistant 回复 -> 返回结果
```

#### 4.3 DeepSeek 集成

- 配置：通过 `DeepSeekConfig` 从 `application.properties` 读取 API Key 和 URL
- 非流式调用：Apache HttpClient5 发送 POST 到 `/chat/completions`，`temperature=0.7`，`max_tokens=1024`
- 流式调用：`stream=true`，使用 `BufferedReader` 逐行读取 SSE 响应，解析 `delta.content` 逐字转发
- 线程池：`Executors.newFixedThreadPool(10)` 处理异步流式响应

#### 4.4 当前对接状态

- 后端 AI 聊天 API 已实现，DeepSeekService 已实现但**未与 AIService 串联**（AIServiceImpl 仍使用硬编码模拟响应）
- 卡片生成后端**尚未实现** Controller，前端完整实现了卡片服务层、组件、页面，但**全部运行在 Mock 模式**
- 前端定义的卡片 SSE 协议（init/text/complete）与后端 AI 聊天的 SSE 协议（start/delta/end）格式不同，说明卡片系统和角色聊天系统是两套独立的 AI 功能

## 遇到的问题和解决

1. **SSE 流式传输在小程序中的实现**：小程序不支持原生 EventSource，通过 `wx.request` 的 `enableChunked` + `onChunkReceived` 实现流式接收，手动解析 ArrayBuffer 中的 SSE 事件，需要处理 UTF-8 多字节字符的截断问题。
2. **Markdown 渲染链路验证**：小程序中 Markdown 渲染需要 markdown-it 解析 -> HTML -> mp-html 渲染，中间涉及 `---` 分割协议的特殊处理，通过独立 Demo 验证后才迁移到主工程。
3. **卡片稀有度视觉差异化**：6 级稀有度需要在有限的 WXSS 能力下实现明显视觉差异，UR/LR 使用深色渐变背景 + 动画光效，与普通卡的白色背景形成强烈对比。
4. **前后端协议不一致**：卡片系统的 SSE 协议（init/text/complete）与 AI 聊天的 SSE 协议（start/delta/end）格式不同，需要在前端服务层做适配。

## 心得体会

卡片系统是本项目中最具创意的 AI 功能，将"学习时长"这一枯燥数据转化为"收藏卡片"这一游戏化体验。在设计过程中，最大的挑战不是技术实现，而是如何让 AI 生成的内容有温度——通过 7 大主题池和稀有度驱动的概率系统，让每张卡片都有独特的情感表达。流式生成的前端实现也让我深入理解了 SSE 协议在小程序环境下的工作方式，以及如何在不支持原生 EventSource 的环境中实现实时数据流。

---

# ~~AI 功能集成贡献说明~~

~~姓名：贺祥宇
学号：2312190107
日期：2026-4-21~~

## ~~我完成的工作~~

### ~~1. AI 功能~~

~~AI角色扮演系统。~~

~~AI扮演模板角色，如科学家爱因斯坦与用户进行对话，可进行自习计划与数据分析。~~

#### ~~核心功能：~~

1. ~~与角色对话聊天~~
2. ~~多轮对话管理~~
3. ~~保持角色一致性~~
4. ~~美观的界面与交互辅助~~

#### ~~架构设计：~~

1. ~~角色层~~
2. ~~会话层~~
3. ~~控制层~~
4. ~~上下文策略~~

### ~~2. 实现内容~~

- ~~ 前端调用~~
- ~~ 错误处理~~

## ~~PR 链接~~

- ~~PR #35: [Feature/贺祥宇 frontend doc by kiraTheresa · Pull Request #35 · yu086868-ui/iStudySpot](https://github.com/yu086868-ui/iStudySpot/pull/35)~~
