import { announcementApi } from '../../services/announcement'
import { ruleApi } from '../../services/rule'
import type { Rule, Announcement } from '../../typings/api'

interface DisplayRule extends Rule {
  contentLines: string[]
}

interface DisplayAnnouncement extends Announcement {
  priorityText: string
  publishTimeText: string
}

const PRIORITY_TEXT: Record<string, string> = {
  high: '紧急',
  medium: '重要',
  low: '普通'
}

function formatPublishTime(isoTime: string): string {
  const date = new Date(isoTime)
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${month}月${day}日`
}

Page({
  data: {
    rules: [] as DisplayRule[],
    announcements: [] as DisplayAnnouncement[]
  },

  onLoad() {
    console.log('[Rules] 页面加载，开始获取数据')
    this.loadRules()
    this.loadAnnouncements()
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({
        currentTab: 'rules'
      })
    }
  },

  async loadRules() {
    console.log('[Rules] 开始获取规则数据...')
    try {
      const res = await ruleApi.getRules()
      console.log('[Rules] 规则API响应:', JSON.stringify(res, null, 2))

      if (res.code === 200 && res.data) {
        const rawRules = Array.isArray(res.data) ? res.data : []
        const rules: DisplayRule[] = rawRules.map((rule: Rule) => ({
          ...rule,
          contentLines: rule.content.split('\n').filter((line: string) => line.trim() !== '')
        }))
        console.log('[Rules] 解析规则数量:', rules.length)
        this.setData({ rules })
      } else {
        console.warn('[Rules] 规则API返回异常:', res.code, res.message)
        this.setData({ rules: [] })
      }
    } catch (error) {
      console.error('[Rules] 获取规则失败', error)
      this.setData({ rules: [] })
    }
  },

  async loadAnnouncements() {
    console.log('[Rules] 开始获取公告数据...')
    try {
      const res = await announcementApi.getAnnouncements()
      console.log('[Rules] 公告API响应:', JSON.stringify(res, null, 2))

      if (res.code === 200 && res.data) {
        let rawAnnouncements: Announcement[] = []

        if (res.data && typeof res.data === 'object' && 'list' in res.data && Array.isArray((res.data as { list: unknown[] }).list)) {
          rawAnnouncements = (res.data as { list: Announcement[] }).list
          console.log('[Rules] 从分页格式解析公告，数量:', rawAnnouncements.length)
        } else if (Array.isArray(res.data)) {
          rawAnnouncements = res.data as Announcement[]
          console.log('[Rules] 从数组格式解析公告，数量:', rawAnnouncements.length)
        }

        const announcements: DisplayAnnouncement[] = rawAnnouncements.map((ann: Announcement) => ({
          ...ann,
          priorityText: PRIORITY_TEXT[ann.priority] || '普通',
          publishTimeText: formatPublishTime(ann.publishTime)
        }))

        console.log('[Rules] 最终公告数量:', announcements.length)
        this.setData({ announcements })
      } else {
        console.warn('[Rules] 公告API返回异常:', res.code, res.message)
        this.setData({ announcements: [] })
      }
    } catch (error) {
      console.error('[Rules] 获取公告失败', error)
      this.setData({ announcements: [] })
    }
  }
})
