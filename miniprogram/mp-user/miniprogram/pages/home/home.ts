import { reservationApi } from '../../services/reservation'
import { userApi } from '../../services/user'
import { studyRoomApi } from '../../services/studyroom'
import { seatApi } from '../../services/seat'
import { checkInApi } from '../../services/checkin'

type UserState = 'none' | 'reserved' | 'studying' | 'checked_out'

interface QrCodeParams {
  studyRoomId: string
  seatId: string
}

interface LocalReservation {
  id: string
  studyRoomId: string
  seatId: string
  seatNumber: string
  roomName: string
  startTime: string
  endTime: string
  displayTime: string
  status: string
}

Page({
  data: {
    userInfo: null as any,
    userState: 'none' as UserState,
    currentReservation: null as LocalReservation | null,
    weeklyStudyHours: 18,
    studyRooms: [] as any[],
    seats: [] as any[],
    stateDisplayText: ''
  },

  onLoad() {
    this.loadUserInfo()
    this.loadStudyRooms()
    this.updateUserState()
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({
        currentTab: 'home'
      })
    }
    this.updateUserState()
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

  async updateUserState() {
    try {
      const checkInRes = await checkInApi.getCurrentCheckInStatus()
      
      if (checkInRes.code === 200 && checkInRes.data?.isCheckedIn && checkInRes.data?.checkInRecord) {
        const record = checkInRes.data.checkInRecord
        if (record.status === 'active') {
          await this.setStudyingState(record)
          return
        }
      }

      const reservationRes = await reservationApi.getMyReservations({ status: 'confirmed' })
      
      if (reservationRes.code === 200 && reservationRes.data?.list?.length > 0) {
        const reservation = reservationRes.data.list[0]
        await this.setReservedState(reservation)
        return
      }

      this.setNoneState()
    } catch (error) {
      console.error('更新用户状态失败', error)
      this.setNoneState()
    }
  },

  async setStudyingState(checkInRecord: any) {
    let roomName = '未知自习室'
    let seatNumber = '未知座位'

    if (checkInRecord.studyRoomId) {
      const roomRes = await studyRoomApi.getStudyRoomDetail(checkInRecord.studyRoomId)
      if (roomRes.code === 200 && roomRes.data) {
        roomName = roomRes.data.name || '未知自习室'
      }
    }

    if (checkInRecord.seatId) {
      const seatRes = await seatApi.getSeatDetail(checkInRecord.seatId)
      if (seatRes.code === 200 && seatRes.data) {
        seatNumber = seatRes.data.seatNumber || '未知座位'
      }
    }

    const checkInTime = new Date(checkInRecord.checkInTime)
    const now = new Date()
    const duration = Math.floor((now.getTime() - checkInTime.getTime()) / (1000 * 60 * 60))
    const hours = Math.floor(duration / 60)
    const minutes = duration % 60
    const displayTime = `${hours}小时${minutes}分钟`

    this.setData({
      userState: 'studying',
      currentReservation: {
        id: checkInRecord.id,
        studyRoomId: checkInRecord.studyRoomId,
        seatId: checkInRecord.seatId,
        seatNumber,
        roomName,
        startTime: checkInRecord.checkInTime,
        endTime: '',
        displayTime,
        status: 'studying'
      },
      stateDisplayText: '学习中'
    })
  },

  async setReservedState(reservation: any) {
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
      userState: 'reserved',
      currentReservation: {
        id: reservation.id,
        studyRoomId: reservation.studyRoomId,
        seatId: reservation.seatId,
        seatNumber,
        roomName,
        startTime: reservation.startTime,
        endTime: reservation.endTime,
        displayTime: timeStr,
        status: 'reserved'
      },
      stateDisplayText: '已预约'
    })
  },

  setNoneState() {
    this.setData({
      userState: 'none',
      currentReservation: null,
      stateDisplayText: '未预约'
    })
  },

  reserveSeat() {
    wx.navigateTo({
      url: '/pages/seat-selection/seat-selection'
    })
  },

  async onlineCheckIn() {
    const { userState, currentReservation } = this.data

    if (userState === 'studying') {
      wx.showToast({
        title: '您已在学习中',
        icon: 'none'
      })
      return
    }

    if (userState === 'reserved' && currentReservation) {
      await this.performCheckIn(currentReservation.id, currentReservation.seatId)
    } else {
      wx.showModal({
        title: '暂无预约',
        content: '您当前没有预约，是否立即选座开始学习？',
        confirmText: '立即选座',
        cancelText: '取消',
        success: (res) => {
          if (res.confirm) {
            wx.navigateTo({
              url: '/pages/seat-selection/seat-selection?immediate=true'
            })
          }
        }
      })
    }
  },

  scanCheckIn() {
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
    const { userState, currentReservation } = this.data

    if (userState === 'studying') {
      wx.showToast({
        title: '您已在学习中',
        icon: 'none'
      })
      return
    }

    wx.showLoading({ title: '签到中...' })

    try {
      if (userState === 'reserved' && currentReservation) {
        if (currentReservation.seatId === params.seatId) {
          await this.performCheckIn(currentReservation.id, params.seatId)
        } else {
          wx.hideLoading()
          wx.showModal({
            title: '座位不匹配',
            content: `扫描的座位与您的预约（${currentReservation.seatNumber}）不匹配，是否使用扫描的座位签到？`,
            confirmText: '确认签到',
            cancelText: '取消',
            success: async (res) => {
              if (res.confirm) {
                await this.performCheckIn(currentReservation.id, params.seatId)
              }
            }
          })
        }
      } else {
        wx.hideLoading()
        wx.showModal({
          title: '暂无预约',
          content: '您当前没有预约，是否立即选座开始学习？',
          confirmText: '立即选座',
          cancelText: '取消',
          success: (res) => {
            if (res.confirm) {
              wx.navigateTo({
                url: `/pages/seat-selection/seat-selection?studyRoomId=${params.studyRoomId}&seatId=${params.seatId}&immediate=true`
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
  }
})
