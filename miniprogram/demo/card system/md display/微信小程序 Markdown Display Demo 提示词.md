下面是一份专注于 **Markdown Display Demo（微信小程序）** 的 AI IDE 提示词，目标明确为：

> 实现 AI 返回 Markdown 文本的解析与漂亮显示 Demo。
> 仅处理 md display，不涉及图片生成、服务器接口、卡片主题系统等额外功能。

你可以直接交给 AI IDE 使用。

------

# 微信小程序 Markdown Display Demo 提示词

你正在协助开发一个微信小程序中的 Markdown 渲染 Demo。

目标不是实现完整业务系统，而是完成一个独立、可运行、可拓展的 **Markdown Display 模块**。

## 一、任务目标

实现：

AI 返回 Markdown 字符串 → 微信小程序解析 → 漂亮显示。

重点：

- Markdown 内容展示
- 富文本渲染
- 自定义分块效果
- 良好的视觉排版

暂时不涉及：

- 图片生成
- AI接口调用
- 服务器数据库
- 卡片主题/稀有度系统
- 用户系统
- 发布收集逻辑

这是一个纯展示层 Demo。

------

## 二、技术路线

采用：

markdown-it + mp-html

渲染链路：

```text
Markdown String
↓
markdown-it
↓
HTML
↓
mp-html
↓
微信小程序显示
```

不要使用：

- innerHTML
- WebView方案
- 过时或重量级方案

优先保证：

- 简洁
- 可读
- Demo可快速运行

------

## 三、Demo输入数据

模拟 AI 返回内容。

使用本地 mock 数据：

~~~md
# 夜航日志

风穿过云层。

我在观察远方。

---

## 第二段

有些东西并不需要解释。

- 风
- 云
- 夜色

---

```python
print("hello")
Markdown 内容统一保存为：

```js
content_md
~~~

不拆：

- title
- body
- paragraph

即：

使用单字段 Markdown 富文本模型。

------

## 四、核心功能要求

### 1 Markdown 解析

使用：

markdown-it

完成：

```js
md.render(content_md)
```

生成 HTML。

要求：

- 支持标题
- 段落
- 列表
- 加粗斜体
- 代码块
- 分割线
- 引用

保留后续扩展空间。

------

### 2 mp-html 显示

使用：

mp-html

实现：

HTML → 微信小程序展示。

不要自己手写复杂 rich-text 节点树。

组件结构清晰。

------

### 3 自定义分块显示（重点）

不要将整个 Markdown 渲染为一个长文本。

需求：

Markdown 中：

```md
---
```

不是仅作为普通 `<hr>`。

而是：

作为 **内容分层标记**。

实现：

Markdown 内容先按：

```text
---
```

进行 block split。

即：

```js
blocks = content_md.split(/\n---\n/)
```

得到：

```js
[
 block1,
 block2,
 block3
]
```

然后：

每个 block：

单独 markdown-it 解析。

单独：

mp-html 渲染。

最终：

显示为：

多个独立内容块。

即：

```text
Card Block 1

Card Block 2

Card Block 3
```

而不是：

一篇连续文章。

这是本 Demo 的重要特性。

------

## 五、视觉样式要求

风格：

现代、简洁、轻量。

参考：

- GitHub Markdown
- AI聊天卡片
- 文档阅读体验

不要：

- 花哨动画
- 复杂UI库
- 赛博风

重点：

阅读体验。

建议样式：

整体：

- 留白
- 圆角
- 柔和阴影
- 良好行距

块：

```css
block
```

建议：

- margin
- padding
- border-radius
- 独立背景

标题：

- 层级清晰
- 与正文有间距

代码块：

- 深浅背景区分
- 等宽字体
- 支持横向滚动

列表：

- 间距自然

不要默认浏览器裸样式。

------

## 六、推荐目录结构

希望生成：

```text
components/
    markdown-card/
        index.js
        index.wxml
        index.wxss
        index.json

utils/
    markdown.js

pages/demo/
    demo.js
    demo.wxml
    demo.wxss
```

职责：

markdown.js：

- markdown-it初始化
- split逻辑

markdown-card：

- block渲染组件

demo：

- mock数据展示

结构清晰。

------

## 七、最终输出要求

请直接生成：

1. 依赖说明
2. 目录结构
3. 完整示例代码
4. 可运行 Demo
5. 必要注释

优先：

**先跑通，再优化。**

不要只讲原理。
直接输出可落地实现。