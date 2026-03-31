package com.example.scylier.istudyspot.models.statistics

// 每日统计数据
class DailyData(
    val date: String,
    val occupancyRate: Double,
    val bookings: Int,
    val revenue: Double
)

// 自习室使用统计响应
class StudyRoomStatisticsResponse(
    val studyRoomId: String,
    val studyRoomName: String,
    val totalSeats: Int,
    val avgOccupancyRate: Double,
    val totalBookings: Int,
    val totalRevenue: Double,
    val dailyData: List<DailyData>
)
