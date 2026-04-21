import { reservationApi } from '../../services/reservation'
import { userApi } from '../../services/user'
import { studyRoomApi } from '../../services/studyroom'
import { seatApi } from '../../services/seat'
import { checkInApi } from '../../services/checkin'

interface QrCodeParams {
  studyRoomId: string
  seatId: string
}

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
    this.loadCurrentReservation()
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
      } else {
        this.setData({ currentReservation: null })
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

  async handleOnlineCheckIn() {
    if (this.data.currentReservation) {
      const reservation = this.data.currentReservation
      const now = new Date()
      const startTime = new Date(reservation.startTime)
      const endTime = new Date(reservation.endTime)

      if (now >= startTime && now <= endTime) {
        wx.showModal({
          title: '签到确认',
          content: `确认签到座位 ${reservation.seatNumber} 吗？`,
          confirmText: '确认签到',
          success: async (modalRes) => {
            if (modalRes.confirm) {
              await this.performCheckIn(reservation.id, reservation.seatId)
            }
          }
        })
      } else if (now < startTime) {
        wx.showModal({
          title: '预约未开始',
          content: `您的预约时间为 ${reservation.displayTime}，请到时间后再签到。是否立即选座开始学习？`,
          confirmText: '立即选座',
          cancelText: '等待预约',
          success: (modalRes) => {
            if (modalRes.confirm) {
              wx.navigateTo({
                url: '/pages/seat-selection/seat-selection'
              })
            }
          }
        })
      } else {
        wx.showToast({
          title: '预约已过期',
          icon: 'none'
        })
      }
    } else {
      wx.showModal({
        title: '暂无预约',
        content: '您当前没有进行中的预约，是否立即选座开始学习？',
        confirmText: '立即选座',
        cancelText: '取消',
        success: (modalRes) => {
          if (modalRes.confirm) {
            wx.navigateTo({
              url: '/pages/seat-selection/seat-selection'
            })
          }
        }
      })
    }
  },

  async performCheckIn(reservationId: string, seatId: string) {
    wx.showLoading({ title: '签到中...' })
    try {
      const res = await checkInApi.checkIn({
        reservationId,
        seatId
      })
      wx.hideLoading()

      if (res.code === 200) {
        wx.showToast({
          title: '签到成功',
          icon: 'success'
        })
        setTimeout(() => {
          wx.navigateTo({
            url: '/pages/study-status/study-status'
          })
        }, 1500)
      } else {
        wx.showToast({
          title: res.message || '签到失败',
          icon: 'none'
        })
      }
    } catch (error) {
      wx.hideLoading()
      console.error('签到失败', error)
      wx.showToast({
        title: '签到失败，请重试',
        icon: 'none'
      })
    }
  },

  handleScanCheckIn() {
    wx.scanCode({
      success: async (res) => {
        console.log('扫码结果', res.result)
        const qrParams = this.parseQrCode(res.result)
        if (qrParams) {
          await this.handleQrCodeCheckIn(qrParams)
        } else {
          wx.showToast({
            title: '二维码格式不正确',
            icon: 'none'
          })
        }
      },
      fail: () => {
        wx.showToast({
          title: '扫码取消',
          icon: 'none'
        })
      }
    })
  },

  parseQrCode(qrContent: string): QrCodeParams | null {
    try {
      if (qrContent.includes('studyRoomId=') && qrContent.includes('seatId=')) {
        const url = new URL(qrContent)
        const studyRoomId = url.searchParams.get('studyRoomId')
        const seatId = url.searchParams.get('seatId')
        if (studyRoomId && seatId) {
          return { studyRoomId, seatId }
        }
      }

      const jsonMatch = qrContent.match(/^\{.*\}$/)
      if (jsonMatch) {
        const data = JSON.parse(qrContent)
        if (data.studyRoomId && data.seatId) {
          return {
            studyRoomId: data.studyRoomId,
            seatId: data.seatId
          }
        }
      }

      const parts = qrContent.split('/')
      if (parts.length >= 2) {
        return {
          studyRoomId: parts[0],
          seatId: parts[1]
        }
      }

      return null
    } catch (error) {
      console.error('解析二维码失败', error)
      return null
    }
  },

  async handleQrCodeCheckIn(params: QrCodeParams) {
    wx.showLoading({ title: '签到中...' })
    try {
      const reservationRes = await reservationApi.getMyReservations({ status: 'confirmed' })
      
      if (reservationRes.code === 200 && reservationRes.data?.list?.length > 0) {
        const matchingReservation = reservationRes.data.list.find(
          (r: { seatId: string }) => r.seatId === params.seatId
        )
        
        if (matchingReservation) {
          const checkInRes = await checkInApi.checkIn({
            reservationId: matchingReservation.id,
            seatId: params.seatId
          })
          wx.hideLoading()
          
          if (checkInRes.code === 200) {
            wx.showToast({
              title: '签到成功',
              icon: 'success'
            })
            setTimeout(() => {
              wx.navigateTo({
                url: '/pages/study-status/study-status'
              })
            }, 1500)
          } else {
            wx.showToast({
              title: checkInRes.message || '签到失败',
              icon: 'none'
            })
          }
        } else {
          wx.hideLoading()
          wx.showModal({
            title: '座位不匹配',
            content: '扫描的座位与您的预约不匹配，是否立即选座开始学习？',
            confirmText: '立即选座',
            cancelText: '取消',
            success: (modalRes) => {
              if (modalRes.confirm) {
                wx.navigateTo({
                  url: `/pages/seat-selection/seat-selection?studyRoomId=${params.studyRoomId}&seatId=${params.seatId}`
                })
              }
            }
          })
        }
      } else {
        wx.hideLoading()
        wx.showModal({
          title: '暂无预约',
          content: '您当前没有进行中的预约，是否立即选座开始学习？',
          confirmText: '立即选座',
          cancelText: '取消',
          success: (modalRes) => {
            if (modalRes.confirm) {
              wx.navigateTo({
                url: `/pages/seat-selection/seat-selection?studyRoomId=${params.studyRoomId}&seatId=${params.seatId}`
              })
            }
          }
        })
      }
    } catch (error) {
      wx.hideLoading()
      console.error('扫码签到失败', error)
      wx.showToast({
        title: '签到失败，请重试',
        icon: 'none'
      })
    }
  }
})