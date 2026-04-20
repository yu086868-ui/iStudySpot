import { announcementApi } from '../../services/announcement'
import { ruleApi } from '../../services/rule'
import type { Rule, Announcement } from '../../typings/api'

Page({
  data: {
    rules: [] as Rule[],
    announcements: [] as Announcement[]
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
        const rules = Array.isArray(res.data) ? res.data : []
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
        let announcements: Announcement[] = []

        if (res.data && typeof res.data === 'object' && 'list' in res.data && Array.isArray((res.data as { list: unknown[] }).list)) {
          announcements = (res.data as { list: Announcement[] }).list
          console.log('[Rules] 从分页格式解析公告，数量:', announcements.length)
        } else if (Array.isArray(res.data)) {
          announcements = res.data as Announcement[]
          console.log('[Rules] 从数组格式解析公告，数量:', announcements.length)
        }

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