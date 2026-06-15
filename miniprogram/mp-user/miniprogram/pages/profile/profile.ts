import { userApi, authApi } from '../../services/index'

const DEFAULT_MOTTO = '我们的一生皆是征途'

Page({
  data: {
    userInfo: {
      id: 0,
      nickname: '未设置昵称',
      avatarUrl: '/assets/avatar-placeholder.png',
      status: 'normal'
    } as any,
    reservationCount: 0,
    studyHours: 0,
    creditScore: 100,
    motto: DEFAULT_MOTTO
  },

  onLoad() {
    this.loadMotto()
    this.loadUserHome()
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({
        currentTab: 'profile'
      })
    }
  },

  async loadUserHome() {
    try {
      const res = await userApi.getUserHome()
      if (res.code === 200 && res.data) {
        this.setData({
          userInfo: {
            id: res.data.user.id || 0,
            nickname: res.data.user.nickname || '未设置昵称',
            avatarUrl: res.data.user.avatarUrl || '/assets/avatar-placeholder.png',
            status: 'normal'
          },
          reservationCount: res.data.reservationCount || 0,
          studyHours: res.data.studyHours || 0,
          creditScore: res.data.creditScore || 100
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
  },

  handleEditNickname() {
    wx.showModal({
      title: '修改昵称',
      editable: true,
      placeholderText: '请输入新的昵称',
      content: this.data.userInfo.nickname,
      success: async (res) => {
        if (res.confirm) {
          const newNickname = (res.content || '').trim()
          if (!newNickname) {
            wx.showToast({ title: '昵称不能为空', icon: 'none' })
            return
          }
          const updateRes = await userApi.updateProfile({ nickname: newNickname })
          if (updateRes.code === 200) {
            this.setData({ 'userInfo.nickname': newNickname })
            wx.showToast({ title: '修改成功', icon: 'success' })
          } else {
            wx.showToast({ title: updateRes.message || '修改失败', icon: 'none' })
          }
        }
      }
    })
  },

  handleChooseAvatar() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: async (res) => {
        const tempFilePath = res.tempFiles[0].tempFilePath
        // 先立即用本地临时路径更新显示
        this.setData({ 'userInfo.avatarUrl': tempFilePath })
        wx.showLoading({ title: '上传中...' })
        try {
          const uploadRes = await userApi.uploadAvatar(tempFilePath)
          wx.hideLoading()
          if (uploadRes.code === 200 && uploadRes.data) {
            // 上传成功后用服务端返回的路径更新
            this.setData({ 'userInfo.avatarUrl': uploadRes.data.avatarUrl })
            wx.showToast({ title: '头像已更新', icon: 'success' })
          } else {
            wx.showToast({ title: uploadRes.message || '上传失败', icon: 'none' })
          }
        } catch (error) {
          wx.hideLoading()
          wx.showToast({ title: '上传失败', icon: 'none' })
        }
      }
    })
  },

  onAvatarError() {
    this.setData({ 'userInfo.avatarUrl': '/assets/avatar-placeholder.png' })
  }
})
