import dayjs from 'dayjs'
import customParseFormat from 'dayjs/plugin/customParseFormat'
import utc from 'dayjs/plugin/utc'
import timezone from 'dayjs/plugin/timezone'

dayjs.extend(customParseFormat)
dayjs.extend(utc)
dayjs.extend(timezone)

const SHANGHAI_TIMEZONE = 'Asia/Shanghai'
const RMB_SYMBOL = '\u00A5'
const LOCAL_TIME_PATTERNS = [
  'YYYY-MM-DD HH:mm:ss',
  'YYYY-MM-DD HH:mm',
  'YYYY-MM-DDTHH:mm:ss',
  'YYYY-MM-DDTHH:mm',
]

function parseTimestamp(value) {
  if (value === null || value === undefined || value === '') return null

  if (typeof value === 'number') {
    return dayjs(value).tz(SHANGHAI_TIMEZONE)
  }

  const raw = String(value).trim()
  if (!raw) return null

  const hasZoneInfo = /([zZ]|[+-]\d{2}:?\d{2})$/.test(raw)
  if (hasZoneInfo) {
    const zoned = dayjs(raw)
    return zoned.isValid() ? zoned.tz(SHANGHAI_TIMEZONE) : null
  }

  if (raw.includes('T')) {
    const parsedAsUtc = dayjs.utc(raw)
    if (parsedAsUtc.isValid()) {
      return parsedAsUtc.tz(SHANGHAI_TIMEZONE)
    }
  }

  for (const pattern of LOCAL_TIME_PATTERNS) {
    const parsed = dayjs.tz(raw, pattern, SHANGHAI_TIMEZONE)
    if (parsed.isValid()) {
      return parsed
    }
  }

  const fallback = dayjs(raw)
  return fallback.isValid() ? fallback.tz(SHANGHAI_TIMEZONE) : null
}

export function formatDateTime(value) {
  const parsed = parseTimestamp(value)
  return parsed ? parsed.format('YYYY-MM-DD HH:mm:ss') : '-'
}

export function formatDate(value) {
  const parsed = parseTimestamp(value)
  return parsed ? parsed.format('YYYY-MM-DD') : '-'
}

export function formatMoney(value) {
  if (value === null || value === undefined || value === '') return `${RMB_SYMBOL}0.00`
  return `${RMB_SYMBOL}${Number(value).toFixed(2)}`
}

export function statusColor(status) {
  const map = {
    available: 'green',
    booked: 'orange',
    in_use: 'cyan',
    occupied: 'cyan',
    unavailable: 'default',
    open: 'green',
    closed: 'red',
    pending: 'orange',
    paid: 'blue',
    completed: 'green',
    cancelled: 'red',
    expired: 'default',
    success: 'green',
    failed: 'red',
    published: 'green',
    draft: 'orange',
    archived: 'default',
    active: 'green',
    banned: 'red',
  }
  return map[status] || 'default'
}

export function statusLabel(status) {
  const map = {
    available: '可用',
    booked: '已预约',
    in_use: '使用中',
    occupied: '使用中',
    unavailable: '不可用',
    open: '开放',
    closed: '关闭',
    pending: '待支付',
    paid: '已支付',
    completed: '已完成',
    cancelled: '已取消',
    expired: '已过期',
    success: '成功',
    failed: '失败',
    published: '已发布',
    draft: '草稿',
    archived: '已归档',
    active: '正常',
    banned: '封禁',
  }
  return map[status] || status || '-'
}

export function rarityColor(rarity) {
  const map = {
    N: '#d9d9d9',
    R: '#52c41a',
    SR: '#1677ff',
    SSR: '#722ed1',
    UR: '#faad14',
    LR: '#f5222d',
  }
  return map[rarity] || '#d9d9d9'
}
