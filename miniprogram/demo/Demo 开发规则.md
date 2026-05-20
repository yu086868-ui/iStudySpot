友好迁移原则

# Demo 开发规则（HTML/CSS/JS 实验环境）

## 任务定位

当前开发为：

**独立 Web Demo（实验模块）**

目的：

- 快速验证 UI / 交互 / 业务流程
- 避免直接修改微信小程序主工程
- 验证通过后迁移至小程序

因此：

**优先验证体验与结构，不追求完整工程化。**

------

# 技术栈

使用：

```
HTML + CSS + JavaScript
```

允许：

- 原生 JS
- Fetch API
- CSS 动画
- Flex/Grid

不使用：

- React
- Vue
- jQuery
- 重型构建链

除非明确要求。

------

# Demo 目录约定

采用轻量结构：

```
demo/
├─ index.html
├─ style.css
├─ script.js
├─ assets/
│   └─ images/
└─ mock/
```

规则：

- HTML：结构
- CSS：样式
- JS：逻辑
- mock：测试数据

禁止：

- 单文件堆叠全部代码
- JS/CSS 大量内联

------

# Demo 开发原则

Demo 不是成品。

目标：

> 快速验证 + 方便迁移

因此：

### 允许

- 简化数据
- Mock API
- 假数据
- 局部硬编码

但必须：

明确标注。

例如：

```
// mock data
```

------

# 迁移友好原则（重要）

Demo 必须考虑未来迁移至微信小程序。

因此：

## 结构映射

保持：

| Demo | 小程序 |
| ---- | ------ |
| HTML | WXML   |
| CSS  | WXSS   |
| JS   | TS     |

避免：

浏览器独占设计。

------

## 禁止 DOM 强耦合

避免：

```
document.body.innerHTML
```

大量：

```
querySelector + 拼HTML
```

推荐：

数据驱动更新。

例如：

```
render(state)
```

而非：

```
innerHTML +=
```

因为：

小程序迁移时不存在 DOM。

------

# JS 规范

使用：

- const / let
- async / await
- 单一职责函数

保持：

函数短小。

禁止：

```
function test()
```

命名。

要求：

语义化。

例如：

```
createImageCard
openPopup
generatePreview
```

------

# 状态管理

Demo 采用：

```
state
```

集中管理。

例如：

```
const state = {
  popupVisible: false,
  prompt: '',
  imageUrl: ''
}
```

避免：

全局散乱变量。

------

# AI 接口规则

AI 能力必须封装。

统一：

```
api/
service
```

形式。

例如：

```
generateImage()
```

禁止：

按钮点击里直接写请求。

错误：

```
button.onclick = async ()=>{
 fetch(...)
}
```

正确：

```
button.onclick
↓
service
↓
render
```

------

# 样式规范

使用：

语义化类名。

例如：

```
popup-card
preview-image
action-button
```

避免：

```
box1
redDiv
```

禁止：

大量：

```
style=""
```

内联样式。

动画：

优先：

```
transition
transform
```

避免复杂 JS 动画。

------

# Demo 与主工程边界

Demo：

**实验场（sandbox）**

不是：

主工程。

因此：

禁止：

- 修改小程序配置
- 引入主项目依赖
- 耦合业务代码

原则：

> Demo 可删除、可重建、可独立运行

------

# 输出要求（给 AI 的约束）

生成代码时：

1. 保持 Demo 可直接运行
2. 给出完整文件
3. 标注 mock 与真实接口位置
4. 保持迁移友好
5. 不过度工程化
6. 优先验证交互

------

一句简化版核心规则：

> 当前任务为“可迁移 Web Demo 实验开发”，目标是快速验证交互与模块结构，不修改微信小程序主工程；使用 HTML/CSS/JS，保持目录清晰、逻辑可迁移、代码可独立运行。