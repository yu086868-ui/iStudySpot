# 微信小程序 Markdown Display 协议与专用实现 Prompt

你正在协助开发一个微信小程序中的 Markdown Display 模块。

当前任务：

不是普通 Markdown 阅读器。

而是：

**支持通用 Markdown 能力，同时建立项目级 Markdown 协议（Prompt Contract）**。

目标：

AI 输出 Markdown 时既具备通用兼容性，又符合当前卡片系统的展示规则。

------

# 一、设计原则

采用：

双层设计。

即：

```text
Layer1
Markdown Engine
（通用能力）

Layer2
Prompt Contract
（项目规则）
```

即：

先支持 Markdown。

再限制：

当前业务允许什么。

不要反过来。

不要把业务限制写死到解析器里。

------

# 二、Layer1：通用 Markdown 支持

技术：

markdown-it + mp-html。

目标：

实现完整 Markdown 渲染能力。

支持：

Common Markdown / GFM 常见能力。

包括：

- 一级标题
- 二级标题
- 三级标题
- 段落
- 加粗
- 斜体
- 引用
- 列表
- 有序列表
- 代码块
- 行内代码
- 分割线
- 链接

保留：

未来扩展：

- 表格
- 图片
- HTML
- 数学公式

当前可以不重点实现。

目标：

先具备：

通用 Markdown Engine。

不要只支持当前场景。

------

# 三、Layer2：当前项目 Markdown 协议

当前项目不是自由 Markdown。

而是：

**受控 AI Markdown 协议。**

需要：

建立一套 Prompt Contract。

即：

AI被要求：

只使用允许语法。

当前规则：

------

## 1 标题限制

仅允许：

一级标题：

```md
# 标题
```

禁止：

：

```md
##
###
```

原因：

卡片展示层级简单。

避免：

视觉混乱。

因此：

Renderer：

可以支持 h2/h3。

但：

Prompt Contract：

不鼓励生成。

------

## 2 文本长度限制

AI返回：

短文本。

控制：

阅读密度。

建议：

- 80~300字
- 不超过500字

避免：

长文章。

目标：

卡片阅读。

不是博客。

------

## 3 分割协议（重点）

项目约定：

Markdown：

```md
---
```

不作为普通 hr。

而是：

**Card Section Divider。**

即：

语义：

内容块切割。

不是：

单纯横线。

解析规则：

Markdown：

先：

```js
split("---")
```

得到：

```js
blocks[]
```

然后：

每个 block：

独立：

markdown-it

独立：

mp-html

显示：

多个：

Card Block。

效果：

：

```text
Block1

Block2

Block3
```

不是：

连续长文。

这是：

项目核心协议。

------

## 4 代码块策略

当前：

允许：

~~~md
```lang
~~~

显示。

但：

不是主要场景。

要求：

代码块：

- 不影响布局
- 支持滚动
- 自动换行策略明确

仅作为：

Markdown兼容能力。

------

## 5 HTML策略

当前：

不依赖：

原始HTML。

即：

AI Prompt：

不要求：

：

```html
<div>
```

Markdown优先。

HTML：

保留扩展。

避免：

安全和兼容复杂度。

------

# 四、架构要求

实现：

Engine + Contract。

即：

不要把：

：

```text
---
= split
```

写死在 markdown-it 内部。

而是：

建立：

独立协议层。

推荐：

：

```text
utils/

markdown-engine.js
markdown-contract.js
```

职责：

markdown-engine：

- markdown-it初始化
- html生成

markdown-contract：

- AI Markdown规则
- split逻辑
- 限制处理
- 协议校验

实现：

职责分离。

------

# 五、Demo要求

生成：

微信小程序可运行 Demo。

包括：

目录：

```text
components/
    markdown-card/

utils/
    markdown-engine.js
    markdown-contract.js

pages/demo/
```

使用：

mock Markdown。

展示：

：

```md
# 夜航

风经过城市。

---

我仍在观察。

---

有些事情并不需要答案。
```

最终：

显示：

三个独立块。

具有：

现代阅读样式。

------

# 六、输出要求

请直接生成：

1 完整目录结构
2 依赖安装说明
3 markdown-engine实现
4 markdown-contract实现
5 markdown-card组件
6 demo页面
7 必要注释

目标：

**先跑通。**

不要只给思路。
直接输出可落地代码。