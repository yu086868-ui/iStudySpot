import { userApi } from '../../services/user'

const DEFAULT_MOTTO = '我们的一生皆是征途'

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
    rankPercent: 82,
    motto: DEFAULT_MOTTO
  },

  onLoad() {
    this.loadMotto()
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
    }
  },

  loadMotto() {
    const motto = wx.getStorageSync('user_motto') || DEFAULT_MOTTO
    this.setData({ motto })
  },

  handleEditMotto() {
    wx.showModal({
      title: '修改座右铭',
      editable: true,
      placeholderText: '请输入新的座右铭',
      content: this.data.motto,
      success: (res) => {
        if (res.confirm) {
          const newMotto = (res.content || '').trim()
          if (newMotto.length === 0) {
            wx.showToast({
              title: '座右铭不能为空',
              icon: 'none'
            })
            return
          }
          if (newMotto.length > 20) {
            wx.showToast({
              title: '座右铭最多20个字',
              icon: 'none'
            })
            return
          }
          wx.setStorageSync('user_motto', newMotto)
          this.setData({ motto: newMotto })
          wx.showToast({
            title: '修改成功',
            icon: 'success'
          })
        }
      }
    })
  }
})