import dayjs from 'dayjs'

export function formatDateTime(val) {
  if (!val) return '-'
  return dayjs(val).format('YYYY-MM-DD HH:mm:ss')
}

export function formatDate(val) {
  if (!val) return '-'
  return dayjs(val).format('YYYY-MM-DD')
}

export function formatMoney(val) {
  if (val === null || val === undefined) return '¥0.00'
  return `¥${Number(val).toFixed(2)}`
}

export function statusColor(status) {
  const map = {
    available: 'green',
    occupied: 'red',
    reserved: 'orange',
    maintenance: 'default',
    booked: 'orange',
    unavailable: 'default',
    open: 'green',
    closed: 'red',
    pending: 'orange',
    paid: 'blue',
    in_use: 'cyan',
    confirmed: 'blue',
    checked_in: 'cyan',
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
    occupied: '占用',
    reserved: '已预约',
    maintenance: '维护中',
    booked: '已预约',
    unavailable: '不可用',
    open: '开放',
    closed: '关闭',
    pending: '待支付',
    paid: '已支付',
    in_use: '使用中',
    confirmed: '已确认',
    checked_in: '已签到',
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
