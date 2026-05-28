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
    console.log('[学习状态] 页面加载')
    this.loadCurrentCheckIn()
    this.subscribeStoreEvents()
  },

  onUnload() {
    console.log('[学习状态] 页面卸载')
    this.stopTimer()
    if (this.unsubscribeCheckIn) {
      this.unsubscribeCheckIn()
    }
  },

  subscribeStoreEvents() {
    this.unsubscribeCheckIn = store.on(StoreEvent.CHECKIN_CHANGED, (data) => {
      console.log('[学习状态] 收到签到状态变化事件', data)
      const checkInData = data as { isCheckedIn: boolean; checkInRecord: any }
      if (!checkInData.isCheckedIn) {
        console.log('[学习状态] 签到已结束，停止计时器')
        this.stopTimer()
      }
    })
  },

  async loadCurrentCheckIn() {
    console.log('[学习状态] 获取当前签到状态')
    
    const cachedCheckIn = store.getCurrentCheckIn()
    if (cachedCheckIn.isCheckedIn && cachedCheckIn.checkInRecord) {
      console.log('[学习状态] 使用缓存的签到记录', cachedCheckIn.checkInRecord)
      await this.initWithCheckInRecord(cachedCheckIn.checkInRecord)
      return
    }

    try {
      const res = await checkInApi.getCurrentCheckInStatus(true)
      console.log('[学习状态] 签到状态结果', res)
      
      if (res.code === 200 && res.data && res.data.isCheckedIn && res.data.checkInRecord) {
        const record = res.data.checkInRecord
        console.log('[学习状态] 找到活跃签到记录', record)
        await this.initWithCheckInRecord(record)
      } else {
        console.log('[学习状态] 没有活跃签到记录')
        this.showNoActiveSession()
      }
    } catch (error) {
      console.error('[学习状态] 获取签到状态失败', error)
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
    console.log('[学习状态] 启动计时器')
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
    console.log('[学习状态] 停止计时器')
    if (this.data.timer) {
      clearInterval(this.data.timer)
      this.setData({ timer: null })
    }
  },

  async endStudySession() {
    console.log('[签退] 触发结束学习')
    
    const currentCheckIn = store.getCurrentCheckIn()
    const checkInRecordId = this.data.checkInRecordId || (currentCheckIn.checkInRecord && currentCheckIn.checkInRecord.id)
    
    if (!checkInRecordId) {
      console.error('[签退] 未找到签到记录ID')
      wx.showToast({
        title: '未找到签到记录',
        icon: 'none'
      })
      return
    }

    console.log('[签退] 签到记录ID:', checkInRecordId)

    wx.showModal({
      title: '结束学习',
      content: '确定要结束本次自习吗？',
      confirmText: '结束',
      cancelText: '继续',
      success: async (res) => {
        if (res.confirm) {
          console.log('[签退] 用户确认结束学习')
          this.stopTimer()
          this.setData({ isStudying: false })

          const studyDurationMin = this.calcStudyDuration()

          try {
            console.log('[签退] 调用签退API', { checkInRecordId })
            const result = await checkInApi.checkOut({ checkInRecordId })
            console.log('[签退] 签退成功', result)
          } catch (error) {
            console.error('[签退] 签退失败', error)
          }

          await this.tryGenerateCard(studyDurationMin)
        } else {
          console.log('[签退] 用户取消结束学习')
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
      console.error('[卡片] 生成卡片失败', error)
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
