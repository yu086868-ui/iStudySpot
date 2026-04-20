import { checkinApi } from '../../services/checkin'

Page({
  data: {
    studyTime: '0:00:00',
    isStudying: true,
    timer: null as number | null,
    startTime: Date.now(),
    motto: '我们的一生皆是征途',
    checkInRecordId: '' as string
  },

  onLoad() {
    this.loadCurrentCheckIn()
  },

  onUnload() {
    this.stopTimer()
  },

  async loadCurrentCheckIn() {
    try {
      const res = await checkinApi.getCurrentCheckInStatus()
      if (res.code === 200 && res.data?.isCheckedIn && res.data?.checkInRecord) {
        const record = res.data.checkInRecord
        this.setData({
          checkInRecordId: record.id,
          startTime: new Date(record.checkInTime).getTime()
        })
        this.startTimer()
      } else {
        wx.showModal({
          title: '提示',
          content: '您当前没有进行中的学习会话',
          confirmText: '返回首页',
          cancelText: '留在本页',
          success: (res) => {
            if (res.confirm) {
              wx.switchTab({
                url: '/pages/home/home'
              })
            } else {
              this.startTimer()
            }
          }
        })
      }
    } catch (error) {
      console.error('获取签到状态失败', error)
      this.startTimer()
    }
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
    if (!this.data.checkInRecordId) {
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

          try {
            await checkinApi.checkOut({ checkInRecordId: this.data.checkInRecordId })
          } catch (error) {
            console.error('签退失败', error)
          }

          wx.showToast({
            title: '本次学习已结束',
            icon: 'success',
            duration: 2000
          })

          setTimeout(() => {
            wx.switchTab({
              url: '/pages/home/home'
            })
          }, 2000)
        }
      }
    })
  }
})