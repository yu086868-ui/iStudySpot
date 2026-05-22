import { splitAndRender } from '../../utils/markdown'

// 模拟 AI 返回的 Markdown 内容
const MOCK_MD = `# 夜航日志

风穿过云层。

我在观察远方。

---

## 第二段

有些东西并不需要解释。

- 风
- 云
- 夜色

---

\`\`\`python
print("hello")
\`\`\`

> 这是一段引用文字，用于展示引用块的样式效果。

**加粗文本** 和 *斜体文本* 混合展示。`

Page({
  data: {
    blocks: [] as string[]
  },

  onLoad() {
    const htmlBlocks = splitAndRender(MOCK_MD)
    this.setData({
      blocks: htmlBlocks
    })
  }
})
