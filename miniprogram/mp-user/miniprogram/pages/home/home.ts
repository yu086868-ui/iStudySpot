import { reservationApi } from '../../services/reservation'
import { userApi } from '../../services/user'

Page({
  data: {
    userInfo: null as any,
    currentReservation: null as any,
    weeklyStudyHours: 18
  },

  onLoad() {
    this.loadUserInfo()
    this.loadCurrentReservation()
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({
        currentTab: 'home'
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

  async loadCurrentReservation() {
    try {
      const res = await reservationApi.getMyReservations({ status: 'confirmed' })
      if (res.code === 200 && res.data.list.length > 0) {
        const reservation = res.data.list[0]
        const startTime = new Date(reservation.startTime)
        const timeStr = `${startTime.getHours()}:${String(startTime.getMinutes()).padStart(2, '0')}`

        this.setData({
          currentReservation: {
            ...reservation,
            displayTime: timeStr,
            roomName: '北极星自习室',
            seatNumber: '22B号桌位'
          }
        })
      }
    } catch (error) {
      console.error('获取预约信息失败', error)
    }
  },

  navigateToSeatSelection() {
    wx.navigateTo({
      url: '/pages/seat-selection/seat-selection'
    })
  },

  handleOnlineCheckIn() {
    wx.showToast({
      title: '线上签到功能开发中',
      icon: 'none'
    })
  },

  handleScanCheckIn() {
    wx.scanCode({
      success: (res) => {
        console.log('扫码结果', res.result)
        wx.showToast({
          title: '扫码成功',
          icon: 'success'
        })
      },
      fail: () => {
        wx.showToast({
          title: '扫码取消',
          icon: 'none'
        })
      }
    })
  }
})
