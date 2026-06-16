import { checkInApi, cardApi, store, StoreEvent } from '../../services/index'
import { navigationManager } from '../../utils/navigation'
import { render as renderMarkdown } from '../../utils/markdown-engine'
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
    studyDurationMin: 0,
    isStreaming: false,
    streamingHtml: '',
    streamingRarity: 'N',
    streamingThemeCategory: ''
  },

  unsubscribeCheckIn: null as (() => void) | null,
  cancelStream: null as (() => void) | null,

  onLoad() {
    this.loadCurrentCheckIn()
    this.subscribeStoreEvents()
  },

  onUnload() {
    this.stopTimer()
    if (this.unsubscribeCheckIn) {
      this.unsubscribeCheckIn()
    }
    if (this.cancelStream) {
      this.cancelStream()
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

  tryGenerateCard(studyDurationMin: number) {
    this.setData({ studyDurationMin })

    var user = store.getUser()
    var userID = user ? String(user.id) : '1'
    var streamingText = ''
    var self = this

    // 先显示弹窗，进入流式模式
    this.setData({
      showCardPopup: true,
      isStreaming: true,
      streamingHtml: '',
      streamingRarity: 'N',
      streamingThemeCategory: ''
    })

    this.cancelStream = cardApi.generateCardStream(
      { userID: userID, studyDuration: studyDurationMin },
      {
        onInit: function (data) {
          self.setData({
            streamingRarity: data.rarity,
            streamingThemeCategory: data.themeCategory
          })
        },
        onText: function (content) {
          streamingText += content
          var html = renderMarkdown(streamingText)
          self.setData({
            streamingHtml: html
          })
        },
        onComplete: function (card) {
          self.setData({
            newCard: card,
            isStreaming: false
          })
          self.cancelStream = null
        },
        onError: function (message) {
          wx.showToast({
            title: message || '卡片生成失败',
            icon: 'none'
          })
          self.setData({ isStreaming: false })
          self.cancelStream = null
          self.navigateBack()
        }
      }
    )
  },

  onCardPopupClose() {
    if (this.cancelStream) {
      this.cancelStream()
      this.cancelStream = null
    }
    this.setData({ showCardPopup: false, newCard: null, isStreaming: false })
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
