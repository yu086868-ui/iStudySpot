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
    try {
      const res = await ruleApi.getRules()

      if (res.code === 200 && res.data) {
        const rawRules = Array.isArray(res.data) ? res.data : []
        const rules: DisplayRule[] = rawRules.map((rule: Rule) => ({
          ...rule,
          contentLines: rule.content.split('\n').filter((line: string) => line.trim() !== '')
        }))
        this.setData({ rules })
      } else {
        this.setData({ rules: [] })
      }
    } catch (error) {
      this.setData({ rules: [] })
    }
  },

  async loadAnnouncements() {
    try {
      const res = await announcementApi.getAnnouncements()

      if (res.code === 200 && res.data) {
        let rawAnnouncements: Announcement[] = []

        if (res.data && typeof res.data === 'object' && 'list' in res.data && Array.isArray((res.data as { list: unknown[] }).list)) {
          rawAnnouncements = (res.data as { list: Announcement[] }).list
        } else if (Array.isArray(res.data)) {
          rawAnnouncements = res.data as Announcement[]
        }

        const announcements: DisplayAnnouncement[] = rawAnnouncements.map((ann: Announcement) => ({
          ...ann,
          priorityText: PRIORITY_TEXT[ann.priority] || '普通',
          publishTimeText: formatPublishTime(ann.publishTime)
        }))

        this.setData({ announcements })
      } else {
        this.setData({ announcements: [] })
      }
    } catch (error) {
      this.setData({ announcements: [] })
    }
  }
})
