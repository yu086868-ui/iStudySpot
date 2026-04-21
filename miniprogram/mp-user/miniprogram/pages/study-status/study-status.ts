import { checkInApi } from '../../services/checkin'

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
    console.log('[学习状态] 页面加载')
    this.loadCurrentCheckIn()
  },

  onUnload() {
    console.log('[学习状态] 页面卸载')
    this.stopTimer()
  },

  async loadCurrentCheckIn() {
    console.log('[学习状态] 获取当前签到状态')
    try {
      const res = await checkInApi.getCurrentCheckInStatus()
      console.log('[学习状态] 签到状态结果', res)
      
      if (res.code === 200 && res.data?.isCheckedIn && res.data?.checkInRecord) {
        const record = res.data.checkInRecord
        console.log('[学习状态] 找到活跃签到记录', record)
        this.setData({
          checkInRecordId: record.id,
          startTime: new Date(record.checkInTime).getTime()
        })
        this.startTimer()
      } else {
        console.log('[学习状态] 没有活跃签到记录')
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
      console.error('[学习状态] 获取签到状态失败', error)
      this.startTimer()
    }
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
    
    if (!this.data.checkInRecordId) {
      console.error('[签退] 未找到签到记录ID')
      wx.showToast({
        title: '未找到签到记录',
        icon: 'none'
      })
      return
    }

    console.log('[签退] 签到记录ID:', this.data.checkInRecordId)

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

          try {
            console.log('[签退] 调用签退API', { checkInRecordId: this.data.checkInRecordId })
            const result = await checkInApi.checkOut({ checkInRecordId: this.data.checkInRecordId })
            console.log('[签退] 签退成功', result)
          } catch (error) {
            console.error('[签退] 签退失败', error)
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
        } else {
          console.log('[签退] 用户取消结束学习')
        }
      }
    })
  }
})
