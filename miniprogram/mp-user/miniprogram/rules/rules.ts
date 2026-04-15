import { announcementApi } from '../services/announcement'
import { ruleApi } from '../services/rule'

Page({
  data: {
    rules: [] as any[],
    announcements: [] as any[]
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
      if (res.code === 200) {
        this.setData({ rules: res.data })
      }
    } catch (error) {
      console.error('获取规则失败', error)
    }
  },

  async loadAnnouncements() {
    try {
      const res = await announcementApi.getAnnouncements()
      if (res.code === 200) {
        this.setData({ announcements: res.data.list || res.data })
      }
    } catch (error) {
      console.error('获取公告失败', error)
    }
  }
})
