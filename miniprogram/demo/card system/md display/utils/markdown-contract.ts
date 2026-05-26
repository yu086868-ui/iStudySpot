/**
 * Layer2: Markdown Contract
 * 项目级 AI Markdown 协议，定义业务规则
 * 与 Engine 分离，不写死到解析器内部
 */
import { render } from './markdown-engine'

/** 协议校验警告 */
interface ContractWarning {
  type: string
  message: string
}

/** 处理后的卡片块数据 */
interface ProcessedBlock {
  html: string
  index: number
  warnings: ContractWarning[]
}

/** 文本长度上限 */
var MAX_BLOCK_LENGTH = 500
/** 建议最小长度 */
var MIN_BLOCK_LENGTH = 80

/**
 * 按 --- 分割 Markdown 为多个独立块
 * --- 在本协议中语义为 Card Section Divider，非普通 hr
 */
function splitByDivider(contentMd: string): string[] {
  var blocks = contentMd.split(/\n---\n/)
  return blocks.map(function(block: string) {
    return block.trim()
  }).filter(function(block: string) {
    return block !== ''
  })
}

/**
 * 校验单个 Markdown 块是否符合协议规则
 * 返回警告列表，不阻止渲染
 */
function validateBlock(blockMd: string): ContractWarning[] {
  var warnings: ContractWarning[] = []

  // 规则1：标题限制 - 仅允许 h1，不鼓励 h2/h3
  if (/^#{2,3}\s/m.test(blockMd)) {
    warnings.push({
      type: 'heading',
      message: '协议建议仅使用一级标题，当前包含二级或三级标题'
    })
  }

  // 规则2：文本长度限制
  var plainText = blockMd
    .replace(/```[\s\S]*?```/g, '')
    .replace(/`[^`]+`/g, '')
    .replace(/[#*>`\-\[\]()]/g, '')
    .replace(/\n/g, '')
    .trim()

  if (plainText.length > MAX_BLOCK_LENGTH) {
    warnings.push({
      type: 'length',
      message: '文本超过500字（当前' + plainText.length + '字），建议控制在80~300字'
    })
  } else if (plainText.length < MIN_BLOCK_LENGTH && plainText.length > 0) {
    warnings.push({
      type: 'length',
      message: '文本不足80字（当前' + plainText.length + '字），建议丰富内容'
    })
  }

  // 规则3：HTML 检测（当前协议不鼓励原始 HTML）
  if (/<[a-zA-Z][^>]*>/.test(blockMd)) {
    warnings.push({
      type: 'html',
      message: '协议不鼓励使用原始 HTML，建议使用 Markdown 语法'
    })
  }

  return warnings
}

/**
 * 完整处理流程：分割 → 校验 → 渲染
 * 返回处理后的卡片块数组
 */
function processContent(contentMd: string): ProcessedBlock[] {
  var blocks = splitByDivider(contentMd)
  return blocks.map(function(block: string, idx: number) {
    var warnings = validateBlock(block)
    var html = render(block)
    return {
      html: html,
      index: idx + 1,
      warnings: warnings
    }
  })
}

export {
  splitByDivider,
  validateBlock,
  processContent,
  ContractWarning,
  ProcessedBlock
}
