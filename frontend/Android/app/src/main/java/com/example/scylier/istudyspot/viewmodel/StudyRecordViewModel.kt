package com.example.scylier.istudyspot.viewmodel

import androidx.lifecycle.ViewModel

/**
 * 学习记录ViewModel
 * 管理用户的学习统计数据
 */
class StudyRecordViewModel : ViewModel() {

    // 本周学习时长（小时）
    val weekStudyHours: Int = 24

    // 本月学习时长（小时）
    val monthStudyHours: Int = 96

    // 累计学习时长（小时）
    val totalStudyHours: Int = 328

    // 累计预约次数
    val totalBookings: Int = 45

    // 连续学习天数
    val streakDays: Int = 7

    // 平均每次学习时长（小时）
    val avgStudyDuration: Double = 3.5

    // 最喜欢的座位
    val favoriteSeat: String = "A区-12号"

    // 最常去的时间段
    val peakTime: String = "14:00-17:00"
}
