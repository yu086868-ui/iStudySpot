/**
 * Layer1: Markdown Engine
 * 通用 Markdown 渲染能力，不包含业务限制
 */
const MarkdownIt = require('./markdown-it')

const md = new MarkdownIt({
  html: false,
  linkify: true,
  typographer: true,
  breaks: true
})

/**
 * 将 Markdown 字符串渲染为 HTML
 */
function render(markdown: string): string {
  return md.render(markdown.trim())
}

/**
 * 获取 markdown-it 实例（用于插件扩展等）
 */
function getEngine(): any {
  return md
}

export {
  render,
  getEngine
}
