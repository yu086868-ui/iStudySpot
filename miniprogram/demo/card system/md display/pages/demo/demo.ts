import { processContent } from '../../utils/markdown-contract'

// Mock 数据：覆盖所有支持的 Markdown 语法
var MOCK_MD_COMPLIANT = `# 夜航

风经过城市。

我仍在观察。

---

# 观察记录

- 风向：东南
- 温度：18°C
- 能见度：良好

---

# 引用

> 有些事情并不需要答案。

夜色渐深。

---

# 代码

\`const light = true\`

---

# 列表

1. 第一项：观察
2. 第二项：记录
3. 第三项：思考

---

# 格式

**加粗文本** 与 *斜体文本* 混合展示。

[查看详情](https://example.com)`

// Mock 数据：包含协议违规的示例（用于展示 Contract 校验能力）
var MOCK_MD_VIOLATION = `# 正常标题

这段内容符合协议。

---

## 二级标题

这段使用了二级标题，违反了协议中仅使用一级标题的建议。同时这段文字也比较长，超过了建议的文本长度范围，用于展示协议校验的长度警告功能。在实际使用中，AI 应该控制每个卡片块的文本长度在合理范围内，避免出现长篇大论的情况，保持卡片阅读的简洁性。这样用户可以快速浏览和理解每个卡片的内容，而不需要滚动很长的文本。

---

### 三级标题

这里使用了三级标题，同样违反协议建议。`

Page({
  data: {
    blocks: [] as Array<{
      html: string
      index: number
      warnings: Array<{ type: string; message: string }>
    }>,
    activeTab: 'compliant' as string
  },

  onLoad: function() {
    this.loadMockData('compliant')
  },

  loadMockData: function(tab: string) {
    var content = tab === 'compliant' ? MOCK_MD_COMPLIANT : MOCK_MD_VIOLATION
    var blocks = processContent(content)
    this.setData({
      blocks: blocks,
      activeTab: tab
    })
  },

  onTabCompliant: function() {
    this.loadMockData('compliant')
  },

  onTabViolation: function() {
    this.loadMockData('violation')
  }
})
