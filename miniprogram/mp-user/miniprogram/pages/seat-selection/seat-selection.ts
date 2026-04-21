import { seatApi } from '../../services/seat'
import { reservationApi } from '../../services/reservation'
import type { Seat, ReservationRules } from '../../typings/api'
import { SeatLayoutUtil } from '../../utils/seat-layout'

interface SeatGroup {
  name: string
  startCol: number
  endCol: number
  rows: {
    rowNumber: number
    seats: Seat[]
  }[]
}

Page({
  data: {
    seats: [] as Seat[],
    seatGroups: [] as SeatGroup[],
    selectedSeat: null as Seat | null,
    studyRoomId: 'room_001',
    isLoading: false,
    
    selectedDate: '',
    selectedStartHour: 9,
    selectedEndHour: 13,
    minDate: '',
    maxDate: '',
    hours: [] as number[],
    
    reservationRules: null as ReservationRules | null,
    
    seatStats: {
      total: 0,
      available: 0,
      occupied: 0,
      reserved: 0
    }
  },

  onLoad() {
    this.initDateTimePicker()
    this.loadReservationRules()
    this.loadSeats()
  },

  initDateTimePicker() {
    const now = new Date()
    const year = now.getFullYear()
    const month = String(now.getMonth() + 1).padStart(2, '0')
    const day = String(now.getDate()).padStart(2, '0')
    const today = `${year}-${month}-${day}`
    
    const maxDateObj = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000)
    const maxYear = maxDateObj.getFullYear()
    const maxMonth = String(maxDateObj.getMonth() + 1).padStart(2, '0')
    const maxDay = String(maxDateObj.getDate()).padStart(2, '0')
    const maxDate = `${maxYear}-${maxMonth}-${maxDay}`
    
    const currentHour = now.getHours()
    const startHour = currentHour + 1
    
    const hours: number[] = []
    for (let i = 8; i <= 21; i++) {
      hours.push(i)
    }
    
    this.setData({
      selectedDate: today,
      minDate: today,
      maxDate: maxDate,
      selectedStartHour: Math.min(startHour, 20),
      selectedEndHour: Math.min(startHour + 4, 22),
      hours
    })
  },

  async loadReservationRules() {
    try {
      const res = await reservationApi.getReservationRules()
      if (res.code === 200 && res.data) {
        this.setData({ reservationRules: res.data })
      }
    } catch (error) {
      console.error('获取预约规则失败', error)
    }
  },

  async loadSeats() {
    this.setData({ isLoading: true })
    try {
      const res = await seatApi.getSeats(this.data.studyRoomId)
      console.log('getSeats response:', res)
      
      if (res.code === 200 && res.data) {
        const seats = res.data
        console.log('seats data:', seats, 'isArray:', Array.isArray(seats))
        
        if (!Array.isArray(seats)) {
          console.error('seats is not an array:', seats)
          wx.showToast({
            title: '座位数据格式错误',
            icon: 'none'
          })
          return
        }
        
        const stats = SeatLayoutUtil.calculateSeatStats(seats)
        
        const layout = SeatLayoutUtil.createSeatLayout(seats)
        const groupConfigs = SeatLayoutUtil.createDefaultGroupConfig(layout.totalCols)
        const seatGroups = SeatLayoutUtil.splitIntoGroups(seats, groupConfigs)
        
        this.setData({
          seats,
          seatGroups,
          seatStats: stats
        })
      }
    } catch (error) {
      console.error('获取座位失败', error)
      wx.showToast({
        title: '获取座位失败',
        icon: 'none'
      })
    } finally {
      this.setData({ isLoading: false })
    }
  },

  onDateChange(e: any) {
    this.setData({
      selectedDate: e.detail.value,
      selectedSeat: null
    })
  },

  onStartHourChange(e: any) {
    const index = e.detail.value
    const startHour = this.data.hours[index]
    let endHour = this.data.selectedEndHour
    
    if (endHour <= startHour) {
      endHour = Math.min(startHour + 1, 22)
    }
    
    if (this.data.reservationRules) {
      const maxEndHour = startHour + this.data.reservationRules.maxDurationHours
      endHour = Math.min(endHour, maxEndHour)
    }
    
    this.setData({
      selectedStartHour: startHour,
      selectedEndHour: endHour,
      selectedSeat: null
    })
  },

  onEndHourChange(e: any) {
    const index = e.detail.value
    const endHour = this.data.hours[index]
    
    if (endHour <= this.data.selectedStartHour) {
      wx.showToast({
        title: '结束时间必须大于开始时间',
        icon: 'none'
      })
      return
    }
    
    if (this.data.reservationRules) {
      const duration = endHour - this.data.selectedStartHour
      if (duration > this.data.reservationRules.maxDurationHours) {
        wx.showToast({
          title: `最长预约${this.data.reservationRules.maxDurationHours}小时`,
          icon: 'none'
        })
        return
      }
    }
    
    this.setData({
      selectedEndHour: endHour,
      selectedSeat: null
    })
  },

  selectSeat(e: any) {
    const { seat } = e.currentTarget.dataset
    if (!seat) return
    
    if (!SeatLayoutUtil.isSeatSelectable(seat)) {
      const statusMessages: Record<string, string> = {
        occupied: '该座位已被占用',
        reserved: '该座位已被预约',
        maintenance: '该座位维护中'
      }
      wx.showToast({
        title: statusMessages[seat.status] || '该座位不可选',
        icon: 'none'
      })
      return
    }
    
    if (this.data.selectedSeat?.id === seat.id) {
      this.setData({ selectedSeat: null })
    } else {
      this.setData({ selectedSeat: seat })
    }
  },

  getSeatClass(seat: Seat): string {
    const classes: string[] = ['seat']
    
    if (this.data.selectedSeat?.id === seat.id) {
      classes.push('seat-selected')
      return classes.join(' ')
    }
    
    const statusClasses: Record<string, string> = {
      available: 'seat-available',
      occupied: 'seat-occupied',
      reserved: 'seat-reserved',
      maintenance: 'seat-maintenance'
    }
    
    classes.push(statusClasses[seat.status] || 'seat-available')
    
    const typeClasses: Record<string, string> = {
      vip: 'seat-vip',
      quiet: 'seat-quiet',
      normal: ''
    }
    
    if (typeClasses[seat.type]) {
      classes.push(typeClasses[seat.type])
    }
    
    return classes.join(' ')
  },

  getRowLabel(rowNumber: number): string {
    return String.fromCharCode(64 + rowNumber)
  },

  formatTimeDisplay(): string {
    const { selectedDate, selectedStartHour, selectedEndHour } = this.data
    const date = new Date(selectedDate)
    const month = date.getMonth() + 1
    const day = date.getDate()
    const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    const weekDay = weekDays[date.getDay()]
    
    return `${month}月${day}日 ${weekDay} ${selectedStartHour}:00 - ${selectedEndHour}:00`
  },

  async confirmSelection() {
    if (!this.data.selectedSeat) {
      wx.showToast({
        title: '请先选择座位',
        icon: 'none'
      })
      return
    }
    
    const { selectedDate, selectedStartHour, selectedEndHour } = this.data
    const startTime = new Date(selectedDate)
    startTime.setHours(selectedStartHour, 0, 0, 0)
    
    const endTime = new Date(selectedDate)
    endTime.setHours(selectedEndHour, 0, 0, 0)
    
    if (startTime <= new Date()) {
      wx.showToast({
        title: '请选择未来的时间段',
        icon: 'none'
      })
      return
    }
    
    wx.showLoading({ title: '预约中...' })
    
    try {
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
  }
})
