import { reservationApi } from '../../services/reservation'
import { userApi } from '../../services/user'
import { studyRoomApi } from '../../services/studyroom'
import { seatApi } from '../../services/seat'

Page({
  data: {
    userInfo: null as any,
    currentReservation: null as any,
    weeklyStudyHours: 18,
    studyRooms: [] as any[],
    seats: [] as any[]
  },

  onLoad() {
    this.loadUserInfo()
    this.loadCurrentReservation()
    this.loadStudyRooms()
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

  async loadStudyRooms() {
    try {
      const res = await studyRoomApi.getStudyRooms()
      if (res.code === 200) {
        this.setData({ studyRooms: res.data.list || [] })
      }
    } catch (error) {
      console.error('获取自习室列表失败', error)
    }
  },

  async loadCurrentReservation() {
    try {
      const res = await reservationApi.getMyReservations({ status: 'confirmed' })
      if (res.code === 200 && res.data?.list?.length > 0) {
        const reservation = res.data.list[0]
        const startTime = new Date(reservation.startTime)
        const endTime = new Date(reservation.endTime)
        const timeStr = `${startTime.getHours()}:${String(startTime.getMinutes()).padStart(2, '0')}-${endTime.getHours()}:${String(endTime.getMinutes()).padStart(2, '0')}`

        let roomName = '未知自习室'
        let seatNumber = '未知座位'

        if (reservation.studyRoomId) {
          const roomRes = await studyRoomApi.getStudyRoomDetail(reservation.studyRoomId)
          if (roomRes.code === 200 && roomRes.data) {
            roomName = roomRes.data.name || '未知自习室'
          }
        }

        if (reservation.seatId) {
          const seatRes = await seatApi.getSeatDetail(reservation.seatId)
          if (seatRes.code === 200 && seatRes.data) {
            seatNumber = seatRes.data.seatNumber || '未知座位'
          }
        }

        this.setData({
          currentReservation: {
            ...reservation,
            displayTime: timeStr,
            roomName: roomName,
            seatNumber: seatNumber
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