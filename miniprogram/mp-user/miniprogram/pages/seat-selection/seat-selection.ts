import { seatApi } from '../../services/seat'
import { reservationApi } from '../../services/reservation'

Page({
  data: {
    seats: [] as any[],
    selectedSeat: null as any,
    selectedTime: '',
    studyRoomId: 'room_001',
    isLoading: false
  },

  onLoad() {
    this.updateSelectedTime()
    this.loadSeats()
  },

  updateSelectedTime() {
    const now = new Date()
    const startTime = new Date(now.getTime() + 30 * 60 * 1000)
    const endTime = new Date(startTime.getTime() + 4 * 60 * 60 * 1000)

    const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    const month = startTime.getMonth() + 1
    const date = startTime.getDate()
    const weekDay = weekDays[startTime.getDay()]
    const startHour = startTime.getHours()
    const startMin = String(startTime.getMinutes()).padStart(2, '0')
    const endHour = endTime.getHours()
    const endMin = String(endTime.getMinutes()).padStart(2, '0')

    this.setData({
      selectedTime: `${month}月${date}日 ${weekDay} ${startHour}:${startMin} - ${endHour}:${endMin}`
    })
  },

  async loadSeats() {
    this.setData({ isLoading: true })
    try {
      const res = await seatApi.getSeats(this.data.studyRoomId)
      if (res.code === 200 && res.data) {
        const leftSeats = res.data.filter((seat: any) => seat.col <= 4).slice(0, 24)
        const rightSeats = res.data.filter((seat: any) => seat.col > 4 && seat.col <= 8).slice(0, 24)

        this.setData({
          seats: {
            left: leftSeats,
            right: rightSeats
          }
        })
      }
    } catch (error) {
      console.error('获取座位失败', error)
    } finally {
      this.setData({ isLoading: false })
    }
  },

  selectSeat(e: any) {
    const { seat } = e.currentTarget.dataset
    if (!seat || seat.status !== 'available') {
      wx.showToast({
        title: '该座位不可选',
        icon: 'none'
      })
      return
    }

    this.setData({ selectedSeat: seat })
  },

  async confirmSelection() {
    if (!this.data.selectedSeat) {
      wx.showToast({
        title: '请先选择座位',
        icon: 'none'
      })
      return
    }

    wx.showLoading({ title: '预约中...' })

    try {
      const now = new Date()
      const startTime = new Date(now.getTime() + 30 * 60 * 1000)
      const endTime = new Date(startTime.getTime() + 4 * 60 * 60 * 1000)

      const res = await reservationApi.createReservation({
        studyRoomId: this.data.studyRoomId,
        seatId: this.data.selectedSeat.id,
        startTime: startTime.toISOString(),
        endTime: endTime.toISOString()
      })

      wx.hideLoading()

      if (res.code === 200) {
        wx.showToast({
          title: '预约成功',
          icon: 'success'
        })

        setTimeout(() => {
          wx.navigateTo({
            url: '/pages/study-status/study-status'
          })
        }, 1500)
      } else {
        wx.showToast({
          title: res.message || '预约失败',
          icon: 'none'
        })
      }
    } catch (error) {
      wx.hideLoading()
      console.error('预约失败', error)
      wx.showToast({
        title: '预约失败，请重试',
        icon: 'none'
      })
    }
  },

  getSeatClass(seat: any): string {
    if (!seat) return 'seat-available'

    if (this.data.selectedSeat?.id === seat.id) {
      return 'seat-selected'
    }

    switch (seat.status) {
      case 'available':
        return 'seat-available'
      case 'occupied':
        return 'seat-occupied'
      case 'reserved':
        return 'seat-reserved'
      default:
        return 'seat-available'
    }
  }
})