import { reservationApi, userApi, studyRoomApi, seatApi, checkInApi, store, StoreEvent } from '../../services/index'
import { navigationManager } from '../../utils/navigation'
import type { Reservation, CheckInRecord } from '../../typings/api'

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

const CHECKIN_BUFFER_MINUTES = 30

function isReservationCheckinable(reservation: { startTime: string; endTime: string }): boolean {
  const now = new Date()
  const startTime = new Date(reservation.startTime)
  const endTime = new Date(reservation.endTime)
  const checkinWindowStart = new Date(startTime.getTime() - CHECKIN_BUFFER_MINUTES * 60 * 1000)
  
  return now >= checkinWindowStart && now <= endTime
}

function isTimeNearby(startTime: string, thresholdMinutes: number = 30): boolean {
  const now = new Date()
  const start = new Date(startTime)
  const diffMs = start.getTime() - now.getTime()
  const diffMinutes = diffMs / (1000 * 60)
  
  return diffMinutes > 0 && diffMinutes <= thresholdMinutes
}

function isFutureReservation(reservation: { startTime: string; endTime: string }): boolean {
  const now = new Date()
  const endTime = new Date(reservation.endTime)
  return now < endTime
}

function getNearestReservation(reservations: Reservation[]): Reservation | null {
  const now = new Date()
  const futureReservations = reservations.filter(r => {
    const endTime = new Date(r.endTime)
    return endTime > now
  })
  
  if (futureReservations.length === 0) return null
  
  futureReservations.sort((a, b) => {
    const startA = new Date(a.startTime).getTime()
    const startB = new Date(b.startTime).getTime()
    return startA - startB
  })
  
  return futureReservations[0]
}

Page({
  data: {
    userInfo: null as any,
    userState: 'none' as UserState,
    currentReservation: null as LocalReservation | null,
    weeklyStudyHours: 18,
    studyRooms: [] as any[],
    seats: [] as any[],
    stateDisplayText: '',
    reserveButtonText: '选座预约',
    checkInButtonText: '签到 / 学习'
  },

  unsubscribeCheckIn: null as (() => void) | null,
  unsubscribeReservations: null as (() => void) | null,

  onLoad() {
    this.loadUserInfo()
    this.loadStudyRooms()
    this.updateUserState()
    this.subscribeStoreEvents()
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({
        currentTab: 'home'
      })
    }
    this.updateUserState()
  },

  onUnload() {
    if (this.unsubscribeCheckIn) {
      this.unsubscribeCheckIn()
    }
    if (this.unsubscribeReservations) {
      this.unsubscribeReservations()
    }
  },

  subscribeStoreEvents() {
    this.unsubscribeCheckIn = store.on(StoreEvent.CHECKIN_CHANGED, () => {
      console.log('[首页] 收到签到状态变化事件')
      this.updateUserState(false)
    })

    this.unsubscribeReservations = store.on(StoreEvent.RESERVATIONS_CHANGED, () => {
      console.log('[首页] 收到预约状态变化事件')
      this.updateUserState(false)
    })
  },

  async loadUserInfo() {
    const cachedUser = store.getUser()
    if (cachedUser) {
      this.setData({ userInfo: cachedUser })
    }

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
    const cachedRooms = store.getStudyRooms()
    if (cachedRooms.length > 0) {
      this.setData({ studyRooms: cachedRooms })
    }

    try {
      const res = await studyRoomApi.getStudyRooms()
      if (res.code === 200) {
        this.setData({ studyRooms: res.data.list || [] })
      }
    } catch (error) {
      console.error('获取自习室列表失败', error)
    }
  },

  async updateUserState(shouldRefreshFromServer: boolean = true) {
    try {
      const cachedCheckIn = store.getCurrentCheckIn()
      
      if (cachedCheckIn.isCheckedIn && cachedCheckIn.checkInRecord) {
        const record = cachedCheckIn.checkInRecord
        if (record.status === 'active') {
          await this.setStudyingState(record)
          return
        }
      }

      const cachedReservations = store.getMyReservations()
      const confirmedReservations = cachedReservations.filter(r => r.status === 'confirmed')
      
      const nearestCachedReservation = getNearestReservation(confirmedReservations)
      if (nearestCachedReservation) {
        await this.setReservedState(nearestCachedReservation)
        return
      }

      if (!shouldRefreshFromServer) {
        this.setNoneState()
        return
      }

      const checkInRes = await checkInApi.getCurrentCheckInStatus(true)
      
      if (checkInRes.code === 200 && checkInRes.data?.isCheckedIn && checkInRes.data?.checkInRecord) {
        const record = checkInRes.data.checkInRecord
        if (record.status === 'active') {
          await this.setStudyingState(record)
          return
        }
      }

      const reservationRes = await reservationApi.getMyReservations({ status: 'confirmed' }, true)
      
      if (reservationRes.code === 200 && reservationRes.data?.list?.length > 0) {
        const nearestReservation = getNearestReservation(reservationRes.data.list)
        
        if (nearestReservation) {
          await this.setReservedState(nearestReservation)
          return
        }
      }

      this.setNoneState()
    } catch (error) {
      console.error('更新用户状态失败', error)
      this.setNoneState()
    }
  },

  async setStudyingState(checkInRecord: CheckInRecord) {
    let roomName = '未知自习室'
    let seatNumber = '未知座位'

    if (checkInRecord.studyRoomId) {
      const cachedRoom = store.getStudyRoomDetail(checkInRecord.studyRoomId)
      if (cachedRoom) {
        roomName = cachedRoom.name || '未知自习室'
      } else {
        const roomRes = await studyRoomApi.getStudyRoomDetail(checkInRecord.studyRoomId)
        if (roomRes.code === 200 && roomRes.data) {
          roomName = roomRes.data.name || '未知自习室'
        }
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
    const duration = Math.floor((now.getTime() - checkInTime.getTime()) / (1000 * 60))
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
      stateDisplayText: '学习中',
      reserveButtonText: '进入学习',
      checkInButtonText: '进入学习'
    })
  },

  async setReservedState(reservation: Reservation) {
    const startTime = new Date(reservation.startTime)
    const endTime = new Date(reservation.endTime)
    
    const month = startTime.getMonth() + 1
    const day = startTime.getDate()
    const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    const weekDay = weekDays[startTime.getDay()]
    
    const timeStr = `${month}月${day}日 ${weekDay} ${startTime.getHours()}:${String(startTime.getMinutes()).padStart(2, '0')}-${endTime.getHours()}:${String(endTime.getMinutes()).padStart(2, '0')}`

    let roomName = '未知自习室'
    let seatNumber = '未知座位'

    if (reservation.studyRoomId) {
      const cachedRoom = store.getStudyRoomDetail(reservation.studyRoomId)
      if (cachedRoom) {
        roomName = cachedRoom.name || '未知自习室'
      } else {
        const roomRes = await studyRoomApi.getStudyRoomDetail(reservation.studyRoomId)
        if (roomRes.code === 200 && roomRes.data) {
          roomName = roomRes.data.name || '未知自习室'
        }
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
      stateDisplayText: '已预约',
      reserveButtonText: '重新预约',
      checkInButtonText: '签到 / 学习'
    })
  },

  setNoneState() {
    this.setData({
      userState: 'none',
      currentReservation: null,
      stateDisplayText: '未预约',
      reserveButtonText: '选座预约',
      checkInButtonText: '签到 / 学习'
    })
  },

  handleReserveButton() {
    const { userState, currentReservation } = this.data

    if (userState === 'studying') {
      navigationManager.navigateTo('study')
      return
    }

    if (userState === 'reserved' && currentReservation) {
      wx.showModal({
        title: '已有预约',
        content: `您已预约 ${currentReservation.roomName} ${currentReservation.displayTime}，座位 ${currentReservation.seatNumber}。是否取消当前预约并重新选座？`,
        confirmText: '取消预约',
        cancelText: '返回',
        success: async (res) => {
          if (res.confirm) {
            await this.cancelCurrentReservation()
          }
        }
      })
      return
    }

    navigationManager.navigateTo('reservation')
  },

  async cancelCurrentReservation() {
    const { currentReservation } = this.data
    if (!currentReservation) return

    wx.showLoading({ title: '取消中...' })
    try {
      const res = await reservationApi.cancelReservation(currentReservation.id)
      wx.hideLoading()

      if (res.code === 200) {
        wx.showToast({
          title: '预约已取消',
          icon: 'success'
        })
        this.setNoneState()
        setTimeout(() => {
          navigationManager.navigateTo('reservation')
        }, 1500)
      } else {
        wx.showToast({
          title: res.message || '取消失败',
          icon: 'none'
        })
      }
    } catch (error) {
      wx.hideLoading()
      console.error('取消预约失败', error)
      wx.showToast({
        title: '取消失败，请重试',
        icon: 'none'
      })
    }
  },

  handleCheckInButton() {
    const { userState, currentReservation } = this.data

    if (userState === 'studying') {
      navigationManager.navigateTo('study')
      return
    }

    if (userState === 'reserved' && currentReservation) {
      if (isReservationCheckinable(currentReservation)) {
        this.performCheckIn(currentReservation.id, currentReservation.seatId)
      } else {
        const startTime = new Date(currentReservation.startTime)
        const checkinTime = new Date(startTime.getTime() - CHECKIN_BUFFER_MINUTES * 60 * 1000)
        const checkinHour = checkinTime.getHours()
        const checkinMinute = checkinTime.getMinutes()
        
        wx.showModal({
          title: '签到时间未到',
          content: `您的预约时间为 ${currentReservation.displayTime}，请在 ${checkinHour}:${String(checkinMinute).padStart(2, '0')} 之后签到。`,
          showCancel: false,
          confirmText: '我知道了'
        })
      }
      return
    }

    this.handleNoneStateCheckIn()
  },

  async handleNoneStateCheckIn() {
    try {
      const reservationRes = await reservationApi.getMyReservations({ status: 'confirmed' }, true)
      
      if (reservationRes.code === 200 && reservationRes.data?.list?.length > 0) {
        const nearbyReservation = reservationRes.data.list.find(r => 
          isTimeNearby(r.startTime, 30)
        )
        
        if (nearbyReservation) {
          wx.showModal({
            title: '检测到学习时间临近',
            content: '是否直接预约并进入学习？',
            confirmText: '立即开始',
            cancelText: '取消',
            success: async (res) => {
              if (res.confirm) {
                await this.quickReserveAndCheckIn(nearbyReservation)
              }
            }
          })
          return
        }
      }
    } catch (error) {
      console.error('检查临近预约失败', error)
    }

    wx.showModal({
      title: '暂无预约',
      content: '您当前没有预约，是否立即选座开始学习？',
      confirmText: '立即选座',
      cancelText: '取消',
      success: (res) => {
        if (res.confirm) {
          navigationManager.navigateTo('reservation', {
            params: { immediate: 'true' }
          })
        }
      }
    })
  },

  async quickReserveAndCheckIn(reservation: Reservation) {
    wx.showLoading({ title: '处理中...' })
    try {
      const checkInRes = await checkInApi.checkIn({
        reservationId: reservation.id,
        seatId: reservation.seatId
      })
      wx.hideLoading()

      if (checkInRes.code === 200) {
        wx.showToast({
          title: '签到成功',
          icon: 'success'
        })
        setTimeout(() => {
          navigationManager.navigateTo('study')
        }, 1500)
      } else {
        wx.showToast({
          title: checkInRes.message || '签到失败',
          icon: 'none'
        })
      }
    } catch (error) {
      wx.hideLoading()
      console.error('快速签到失败', error)
      wx.showToast({
        title: '签到失败，请重试',
        icon: 'none'
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
              navigationManager.navigateTo('reservation', {
                params: {
                  studyRoomId: params.studyRoomId,
                  seatId: params.seatId,
                  immediate: 'true'
                }
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
    console.log('[签到] 开始签到', { reservationId, seatId })
    wx.showLoading({ title: '签到中...' })
    try {
      const res = await checkInApi.checkIn({
        reservationId,
        seatId
      })
      wx.hideLoading()

      if (res.code === 200) {
        console.log('[签到] 签到成功', res.data)
        wx.showToast({
          title: '签到成功',
          icon: 'success'
        })
        setTimeout(() => {
          navigationManager.navigateTo('study')
        }, 1500)
      } else {
        console.error('[签到] 签到失败', res.message)
        wx.showToast({
          title: res.message || '签到失败',
          icon: 'none'
        })
      }
    } catch (error) {
      wx.hideLoading()
      console.error('[签到] 签到异常', error)
      wx.showToast({
        title: '签到失败，请重试',
        icon: 'none'
      })
    }
  }
})
