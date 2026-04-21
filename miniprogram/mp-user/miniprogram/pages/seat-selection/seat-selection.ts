import { seatApi } from '../../services/seat'
import { reservationApi } from '../../services/reservation'
import { checkInApi } from '../../services/checkin'
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
    selectedStartMinute: 0,
    selectedEndHour: 13,
    selectedEndMinute: 0,
    minDate: '',
    maxDate: '',
    hours: [] as number[],
    minutes: [] as number[],
    
    reservationRules: null as ReservationRules | null,
    
    seatStats: {
      total: 0,
      available: 0,
      occupied: 0,
      reserved: 0
    },
    
    preselectedSeatId: '',
    isImmediateMode: false
  },

  onLoad(options: { studyRoomId?: string; seatId?: string; immediate?: string }) {
    if (options.studyRoomId) {
      this.setData({ studyRoomId: options.studyRoomId })
    }
    if (options.seatId) {
      this.setData({ preselectedSeatId: options.seatId })
    }
    if (options.immediate === 'true') {
      this.setData({ isImmediateMode: true })
    }
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
    const currentMinute = now.getMinutes()
    
    const hours: number[] = []
    for (let i = 8; i <= 21; i++) {
      hours.push(i)
    }
    
    const minutes: number[] = []
    for (let i = 0; i < 60; i += 5) {
      minutes.push(i)
    }
    
    const roundedMinute = Math.ceil(currentMinute / 5) * 5
    
    let startHour = currentHour
    let startMinute = roundedMinute
    
    if (roundedMinute >= 60) {
      startHour = currentHour + 1
      startMinute = 0
    }
    
    const endHour = Math.min(startHour + 2, 22)
    const endMinute = startMinute
    
    this.setData({
      selectedDate: today,
      minDate: today,
      maxDate: maxDate,
      selectedStartHour: Math.min(startHour, 21),
      selectedStartMinute: startMinute,
      selectedEndHour: endHour,
      selectedEndMinute: endMinute,
      hours,
      minutes
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
      
      if (res.code === 200 && res.data) {
        let seats = res.data
        
        if (!Array.isArray(seats)) {
          console.error('seats is not an array:', seats)
          wx.showToast({
            title: '座位数据格式错误',
            icon: 'none'
          })
          return
        }

        seats = seats.map(seat => ({
          ...seat,
          facilities: Array.isArray(seat.facilities) ? seat.facilities : []
        }))
        
        const stats = SeatLayoutUtil.calculateSeatStats(seats)
        
        const layout = SeatLayoutUtil.createSeatLayout(seats)
        const groupConfigs = SeatLayoutUtil.createDefaultGroupConfig(layout.totalCols)
        const seatGroups = SeatLayoutUtil.splitIntoGroups(seats, groupConfigs)
        
        let selectedSeat: Seat | null = null
        if (this.data.preselectedSeatId) {
          selectedSeat = seats.find(s => s.id === this.data.preselectedSeatId) || null
        }
        
        this.setData({
          seats,
          seatGroups,
          seatStats: stats,
          selectedSeat
        })
      } else {
        console.error('API returned error:', res)
        wx.showToast({
          title: res.message || '获取座位失败',
          icon: 'none'
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

  onStartMinuteChange(e: any) {
    const index = e.detail.value
    const startMinute = this.data.minutes[index]
    this.setData({
      selectedStartMinute: startMinute,
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

  onEndMinuteChange(e: any) {
    const index = e.detail.value
    const endMinute = this.data.minutes[index]
    
    const startTotalMinutes = this.data.selectedStartHour * 60 + this.data.selectedStartMinute
    const endTotalMinutes = this.data.selectedEndHour * 60 + endMinute
    
    if (endTotalMinutes <= startTotalMinutes) {
      wx.showToast({
        title: '结束时间必须大于开始时间',
        icon: 'none'
      })
      return
    }
    
    this.setData({
      selectedEndMinute: endMinute,
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
    const { selectedDate, selectedStartHour, selectedStartMinute, selectedEndHour, selectedEndMinute } = this.data
    const date = new Date(selectedDate)
    const month = date.getMonth() + 1
    const day = date.getDate()
    const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    const weekDay = weekDays[date.getDay()]
    
    const startTimeStr = `${selectedStartHour}:${String(selectedStartMinute).padStart(2, '0')}`
    const endTimeStr = `${selectedEndHour}:${String(selectedEndMinute).padStart(2, '0')}`
    
    return `${month}月${day}日 ${weekDay} ${startTimeStr} - ${endTimeStr}`
  },

  isImmediateStartTime(startTime: Date): boolean {
    const now = new Date()
    const diffMs = Math.abs(startTime.getTime() - now.getTime())
    const diffMinutes = diffMs / (1000 * 60)
    return diffMinutes <= 5
  },

  async confirmSelection() {
    if (!this.data.selectedSeat) {
      wx.showToast({
        title: '请先选择座位',
        icon: 'none'
      })
      return
    }
    
    const { selectedDate, selectedStartHour, selectedStartMinute, selectedEndHour, selectedEndMinute } = this.data
    const startTime = new Date(selectedDate)
    startTime.setHours(selectedStartHour, selectedStartMinute, 0, 0)
    
    const endTime = new Date(selectedDate)
    endTime.setHours(selectedEndHour, selectedEndMinute, 0, 0)
    
    const now = new Date()
    
    if (startTime <= now) {
      wx.showToast({
        title: '请选择未来的时间段',
        icon: 'none'
      })
      return
    }
    
    const isImmediateStart = this.isImmediateStartTime(startTime) || this.data.isImmediateMode
    
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
        if (isImmediateStart) {
          wx.showModal({
            title: '预约成功',
            content: '是否立即进入学习？',
            confirmText: '立即开始',
            cancelText: '稍后再说',
            success: async (modalRes) => {
              if (modalRes.confirm) {
                await this.performCheckIn(res.data.id, this.data.selectedSeat!.id)
              } else {
                wx.switchTab({
                  url: '/pages/home/home'
                })
              }
            }
          })
        } else {
          wx.showModal({
            title: '预约成功',
            content: `预约成功！您的预约时间为 ${this.formatTimeDisplay()}，请按时前往自习。`,
            showCancel: false,
            confirmText: '返回首页',
            success: () => {
              wx.switchTab({
                url: '/pages/home/home'
              })
            }
          })
        }
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
