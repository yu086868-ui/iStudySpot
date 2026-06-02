import { checkInApi, cardApi, store, StoreEvent } from '../../services/index'
import { navigationManager } from '../../utils/navigation'
import type { Card } from '../../typings/api'

Page({
  data: {
    studyTime: '0:00:00',
    isStudying: true,
    timer: null as ReturnType<typeof setInterval> | null,
    startTime: Date.now(),
    motto: '我们的一生皆是征途',
    checkInRecordId: '' as string,
    roomName: '' as string,
    seatNumber: '' as string,
    showCardPopup: false,
    newCard: null as Card | null,
    studyDurationMin: 0
  },

  unsubscribeCheckIn: null as (() => void) | null,

  onLoad() {
    this.loadCurrentCheckIn()
    this.subscribeStoreEvents()
  },

  onUnload() {
    this.stopTimer()
    if (this.unsubscribeCheckIn) {
      this.unsubscribeCheckIn()
    }
  },

  subscribeStoreEvents() {
    this.unsubscribeCheckIn = store.on(StoreEvent.CHECKIN_CHANGED, (data) => {
      const checkInData = data as { isCheckedIn: boolean; checkInRecord: any }
      if (!checkInData.isCheckedIn) {
        this.stopTimer()
      }
    })
  },

  async loadCurrentCheckIn() {
    const cachedCheckIn = store.getCurrentCheckIn()
    if (cachedCheckIn.isCheckedIn && cachedCheckIn.checkInRecord) {
      await this.initWithCheckInRecord(cachedCheckIn.checkInRecord)
      return
    }

    try {
      const res = await checkInApi.getCurrentCheckInStatus(true)
      
      if (res.code === 200 && res.data && res.data.isCheckedIn && res.data.checkInRecord) {
        const record = res.data.checkInRecord
        await this.initWithCheckInRecord(record)
      } else {
        this.showNoActiveSession()
      }
    } catch (error) {
      this.showNoActiveSession()
    }
  },

  async initWithCheckInRecord(record: any) {
    this.setData({
      checkInRecordId: record.id,
      startTime: new Date(record.checkInTime).getTime()
    })

    if (record.studyRoomId) {
      const cachedRoom = store.getStudyRoomDetail(record.studyRoomId)
      if (cachedRoom) {
        this.setData({ roomName: cachedRoom.name || '未知自习室' })
      }
    }

    if (record.seatId) {
      const seats = store.getSeats(record.studyRoomId)
      if (seats) {
        const seat = seats.find(s => s.id === record.seatId)
        if (seat) {
          this.setData({ seatNumber: seat.seatNumber })
        }
      }
    }

    this.startTimer()
  },

  showNoActiveSession() {
    wx.showModal({
      title: '提示',
      content: '您当前没有进行中的学习会话',
      confirmText: '返回首页',
      showCancel: false,
      success: () => {
        navigationManager.navigateFromStudyToHome()
      }
    })
  },

  startTimer() {
    this.setData({ isStudying: true })

    const timer = setInterval(() => {
      const now = Date.now()
      const diff = now - this.data.startTime
      const hours = Math.floor(diff / (1000 * 60 * 60))
      const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
      const seconds = Math.floor((diff % (1000 * 60)) / 1000)

      this.setData({
        studyTime: `${hours}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
      })
    }, 1000)

    this.setData({ timer })
  },

  stopTimer() {
    if (this.data.timer) {
      clearInterval(this.data.timer)
      this.setData({ timer: null })
    }
  },

  async endStudySession() {
    const currentCheckIn = store.getCurrentCheckIn()
    const checkInRecordId = this.data.checkInRecordId || (currentCheckIn.checkInRecord && currentCheckIn.checkInRecord.id)
    
    if (!checkInRecordId) {
      wx.showToast({
        title: '未找到签到记录',
        icon: 'none'
      })
      return
    }

    wx.showModal({
      title: '结束学习',
      content: '确定要结束本次自习吗？',
      confirmText: '结束',
      cancelText: '继续',
      success: async (res) => {
        if (res.confirm) {
          this.stopTimer()
          this.setData({ isStudying: false })

          const studyDurationMin = this.calcStudyDuration()

          try {
            await checkInApi.checkOut({ checkInRecordId })
          } catch (error) {
          }

          await this.tryGenerateCard(studyDurationMin)
        }
      }
    })
  },

  calcStudyDuration(): number {
    const now = Date.now()
    const diff = now - this.data.startTime
    return Math.max(1, Math.floor(diff / (1000 * 60)))
  },

  async tryGenerateCard(studyDurationMin: number) {
    this.setData({ studyDurationMin })

    try {
      const user = store.getUser()
      const userID = user ? user.id : 'user_001'
      const res = await cardApi.generateCard({ userID, studyDuration: studyDurationMin })

      if (res.code === 200 && res.data) {
        this.setData({
          showCardPopup: true,
          newCard: res.data
        })
        return
      }
    } catch (error) {
    }

    this.navigateBack()
  },

  onCardPopupClose() {
    this.setData({ showCardPopup: false, newCard: null })
    this.navigateBack()
  },

  onCardPopupAction() {
    this.setData({ showCardPopup: false, newCard: null })
    this.navigateBack()
  },

  navigateBack() {
    wx.showToast({
      title: '本次学习已结束',
      icon: 'success',
      duration: 1500
    })
    setTimeout(() => {
      navigationManager.navigateFromStudyToHome()
    }, 1500)
  }
})
