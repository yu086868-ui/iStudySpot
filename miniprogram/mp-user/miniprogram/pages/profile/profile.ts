import { userApi } from '../../services/user'

Page({
  data: {
    userInfo: null as any,
    totalStudyHours: 624,
    rankPercent: 82
  },

  onLoad() {
    this.loadUserInfo()
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({
        currentTab: 'profile'
      })
    }
  },

  async loadUserInfo() {
    try {
      const res = await userApi.getCurrentUser()
      if (res.code === 200) {
        this.setData({ userInfo: res.data })
      }
    } catch (error) {
      console.error('获取用户信息失败', error)
    }
  },

  handleRecharge() {
    wx.showToast({
      title: '账户充值功能开发中',
      icon: 'none'
    })
  },

  handleSettings() {
    wx.showToast({
      title: '设置功能开发中',
      icon: 'none'
    })
  }
})
