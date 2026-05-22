const MarkdownIt = require('./markdown-it')

const md = new MarkdownIt({
  html: false,
  linkify: true,
  typographer: true,
  breaks: true
})

/**
 * 将 Markdown 内容按 --- 分割为多个 block，每个 block 单独渲染为 HTML
 */
function splitAndRender(contentMd: string): string[] {
  const blocks = contentMd.split(/\n---\n/)
  return blocks.map(block => {
    const trimmed = block.trim()
    if (!trimmed) return ''
    return md.render(trimmed)
  }).filter(html => html !== '')
}

/**
 * 单个 block 渲染为 HTML
 */
function renderBlock(blockMd: string): string {
  return md.render(blockMd.trim())
}

export {
  md,
  splitAndRender,
  renderBlock
}
