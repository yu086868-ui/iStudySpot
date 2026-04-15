import { checkinApi } from '../../services/checkin'

Page({
  data: {
    studyTime: '2:28:37',
    isStudying: true,
    timer: null as number | null,
    startTime: Date.now() - (2 * 3600 + 28 * 60 + 37) * 1000,
    motto: '我们的一生皆是征途'
  },

  onLoad() {
    this.startTimer()
  },

  onUnload() {
    this.stopTimer()
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
            await checkinApi.checkOut({ checkInRecordId: '' })
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
