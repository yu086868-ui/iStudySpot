# 卡片生成 API 文档

## 1. 接口概述

本文档包含 iStudySpot 卡片系统的所有 API 接口，包括同步生成、流式生成、卡片查询等功能。

| 属性 | 值 |
|------|-----|
| **基础路径** | `/api/card` |
| **协议** | HTTP/HTTPS |
| **Content-Type** | `application/json` |

---

## 2. 接口列表

| 接口 | 方法 | 说明 |
|------|------|------|
| `/generate` | POST | 同步生成卡片（一次性返回） |
| `/generate/stream` | POST | **SSE 流式生成卡片**（逐字返回） |
| `/detail` | GET | 获取单张卡片详情 |
| `/list` | GET | 获取用户卡片列表 |
| `/image/**` | GET | 获取卡片图片 |

---

## 3. 同步生成接口

### 3.1 接口信息

| 属性 | 值 |
|------|-----|
| **路径** | `/api/card/generate` |
| **方法** | POST |

### 3.2 请求参数

```json
{
  "userID": "string (必填，用户ID)",
  "studyDuration": "integer (必填，学习时长，单位：分钟)"
}
```

### 3.3 成功响应

```json
{
  "success": true,
  "message": "generate success",
  "card": {
    "uuid": "string (卡片唯一标识)",
    "rarity": "string (N/R/SR/SSR/UR/LR)",
    "borderTheme": "string (边框主题)",
    "cardTheme": "string (卡片主题)",
    "themeCategory": "string (主题类别)",
    "markdown": "string (卡片文案)",
    "imageURL": "string (图片路径)",
    "createTime": "string (创建时间)",
    "studyDuration": "integer (学习时长)"
  }
}
```

### 3.4 失败响应

```json
{
  "success": false,
  "message": "error description"
}
```

### 3.5 示例

```bash
curl -X POST http://localhost:8080/api/card/generate \
  -H "Content-Type: application/json" \
  -d '{"userID": "user_001", "studyDuration": 60}'
```

---

## 4. 流式生成接口（SSE）

### 4.1 接口信息

| 属性 | 值 |
|------|-----|
| **路径** | `/api/card/generate/stream` |
| **方法** | POST |
| **协议** | SSE (Server-Sent Events) |
| **超时时间** | 120秒 |

### 4.2 请求参数

```json
{
  "userID": "string (必填，用户ID)",
  "studyDuration": "integer (必填，学习时长，单位：分钟)"
}
```

### 4.3 响应事件

#### 4.3.1 事件类型汇总

| 事件名 | 数据结构 | 说明 |
|--------|----------|------|
| `data` | `InitEvent` | 卡片初始化信息 |
| `data` | `TextEvent` | 流式文本内容（逐字返回） |
| `complete` | `CompleteEvent` | 最终完整卡片数据 |
| `error` | `ErrorEvent` | 错误信息 |

#### 4.3.2 InitEvent - 初始化事件

```json
{
  "type": "init",
  "rarity": "string (N/R/SR/SSR/UR/LR)",
  "themeCategory": "string (主题类别)",
  "borderTheme": "string (边框主题)",
  "cardTheme": "string (卡片主题)"
}
```

#### 4.3.3 TextEvent - 文本流式事件

```json
{
  "type": "text",
  "content": "string (单个字符或文本片段)"
}
```

#### 4.3.4 CompleteEvent - 完成事件

```json
{
  "success": true,
  "message": "generate success",
  "card": {
    "uuid": "string",
    "rarity": "string",
    "borderTheme": "string",
    "cardTheme": "string",
    "themeCategory": "string",
    "markdown": "string",
    "imageURL": "string",
    "createTime": "string",
    "studyDuration": "integer"
  }
}
```

#### 4.3.5 ErrorEvent - 错误事件

```json
{
  "success": false,
  "message": "string (错误描述)"
}
```

### 4.4 完整响应示例

```
event: data
data: {"type":"init","rarity":"UR","themeCategory":"励志成长","borderTheme":"金","cardTheme":"特殊"}

event: data
data: {"type":"text","content":"#"}

event: data
data: {"type":"text","content":"自"}

event: data
data: {"type":"text","content":"律"}

event: complete
data: {"success":true,"message":"generate success","card":{"uuid":"xxx","rarity":"UR","markdown":"# 自律\n> 时间是最好的见证\n---\n每一次坚持，都是对未来的投资。","imageURL":"/api/card/image/xxx.png",...}}
```

### 4.5 前端对接示例

```javascript
async function generateCardStream(userId, studyDuration) {
  const response = await fetch('/api/card/generate/stream', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId, studyDuration })
  });

  const reader = response.body.getReader();
  const decoder = new TextDecoder('utf-8');
  let buffer = '';

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;

    buffer += decoder.decode(value, { stream: true });
    
    while (buffer.includes('\n\n')) {
      const idx = buffer.indexOf('\n\n');
      const eventStr = buffer.substring(0, idx);
      buffer = buffer.substring(idx + 2);

      const lines = eventStr.split('\n');
      let eventName = 'message';
      let eventData = '';

      for (const line of lines) {
        if (line.startsWith('event:')) eventName = line.slice(6).trim();
        if (line.startsWith('data:')) eventData = line.slice(5);
      }

      const data = JSON.parse(eventData);
      
      if (eventName === 'data') {
        if (data.type === 'init') console.log('初始化:', data);
        if (data.type === 'text') console.log('收到文本:', data.content);
      } else if (eventName === 'complete') {
        console.log('完成:', data.card);
      } else if (eventName === 'error') {
        console.error('错误:', data.message);
      }
    }
  }
}
```

---

## 5. 获取卡片详情

### 5.1 接口信息

| 属性 | 值 |
|------|-----|
| **路径** | `/api/card/detail` |
| **方法** | GET |

### 5.2 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `id` | string | 是 | 卡片 UUID |

### 5.3 成功响应

```json
{
  "success": true,
  "card": {
    "uuid": "string",
    "rarity": "string",
    "borderTheme": "string",
    "cardTheme": "string",
    "themeCategory": "string",
    "markdown": "string",
    "imageURL": "string",
    "createTime": "string",
    "studyDuration": "integer"
  }
}
```

### 5.4 失败响应

```json
{
  "success": false,
  "message": "card not found"
}
```

### 5.5 示例

```bash
curl http://localhost:8080/api/card/detail?id=xxx-xxx-xxx
```

---

## 6. 获取用户卡片列表

### 6.1 接口信息

| 属性 | 值 |
|------|-----|
| **路径** | `/api/card/list` |
| **方法** | GET |

### 6.2 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `userID` | string | 是 | 用户 ID |

### 6.3 成功响应

```json
{
  "success": true,
  "list": [
    {
      "uuid": "string",
      "rarity": "string",
      "borderTheme": "string",
      "cardTheme": "string",
      "themeCategory": "string",
      "markdown": "string",
      "imageURL": "string",
      "createTime": "string",
      "studyDuration": "integer"
    }
  ]
}
```

### 6.4 示例

```bash
curl http://localhost:8080/api/card/list?userID=user_001
```

---

## 7. 获取卡片图片

### 7.1 接口信息

| 属性 | 值 |
|------|-----|
| **路径** | `/api/card/image/{path}` |
| **方法** | GET |
| **Content-Type** | `image/png` |

### 7.2 路径参数

| 参数 | 说明 |
|------|------|
| `path` | 图片相对路径，如 `2024/01/01/xxx.png` |

### 7.3 响应

- **成功**：返回 PNG 图片二进制数据
- **失败**：404 Not Found

### 7.4 示例

```bash
curl http://localhost:8080/api/card/image/2024/01/01/card_xxx.png
```

---

## 8. 错误码说明

| HTTP状态码 | 场景 | 响应 |
|------------|------|------|
| 400 | 参数错误 | `{"success":false,"message":"userID is required"}` |
| 404 | 卡片不存在 | `{"success":false,"message":"card not found"}` |
| 500 | 服务器错误 | `{"success":false,"message":"具体错误描述"}` |

---

## 9. 数据结构

### 9.1 Card 对象

| 字段 | 类型 | 说明 |
|------|------|------|
| `uuid` | string | 卡片唯一标识 |
| `rarity` | string | 稀有度（N/R/SR/SSR/UR/LR） |
| `borderTheme` | string | 边框主题颜色 |
| `cardTheme` | string | 卡片主题类型 |
| `themeCategory` | string | 主题类别 |
| `markdown` | string | 卡片文案内容 |
| `imageURL` | string | 图片访问路径 |
| `createTime` | string | 创建时间 |
| `studyDuration` | integer | 学习时长（分钟） |

### 9.2 稀有度说明

| 稀有度 | 边框主题 | 卡片主题 | 获取概率 |
|--------|----------|----------|----------|
| N | 白/灰 | 普通 | 常见 |
| R | 绿 | 普通 | 较常见 |
| SR | 蓝 | 普通 | 中等 |
| SSR | 紫 | 普通 | 稀有 |
| UR | 金 | 特殊 | 非常稀有 |
| LR | 红 | 特殊 | 极其稀有 |

### 9.3 主题类别

- 励志成长
- 名人与历史
- 哲思感悟
- 自然意象
- 科技未来
- 温柔陪伴
- 隐藏主题（仅 UR/LR 稀有度可获得）

---

## 10. 接口对比

| 特性 | `/generate` | `/generate/stream` |
|------|-------------|---------------------|
| 响应方式 | 一次性返回 | 流式逐步返回 |
| 等待体验 | 长时间等待 | 实时显示进度 |
| 文本体验 | 直接显示 | 打字机效果 |
| 适用场景 | 批量生成 | 用户交互 |
| 连接方式 | 短连接 | 长连接（SSE） |

---

## 11. 注意事项

1. **认证**：所有接口已添加到 JWT 白名单，无需登录即可访问
2. **图片存储**：生成的图片存储在服务器本地，通过 `/api/card/image/` 访问
3. **SSE 超时**：流式接口超时时间为 120 秒
4. **编码格式**：所有响应编码为 UTF-8

---


