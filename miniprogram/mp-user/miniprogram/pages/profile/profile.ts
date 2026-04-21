import { userApi } from '../../services/user'

Page({
  data: {
    userInfo: {
      id: '',
      username: '',
      nickname: '未设置昵称',
      avatar: '/assets/avatar-placeholder.png',
      phone: '',
      email: '',
      studentId: '',
      creditScore: 0,
      status: 'active'
    } as any,
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
      if (res.code === 200 && res.data) {
        this.setData({
          userInfo: {
            id: res.data.id || '',
            username: res.data.username || '',
            nickname: res.data.nickname || '未设置昵称',
            avatar: res.data.avatar || '/assets/avatar-placeholder.png',
            phone: res.data.phone || '',
            email: res.data.email || '',
            studentId: res.data.studentId || '',
            creditScore: res.data.creditScore || 0,
            status: res.data.status || 'active'
          }
        })
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