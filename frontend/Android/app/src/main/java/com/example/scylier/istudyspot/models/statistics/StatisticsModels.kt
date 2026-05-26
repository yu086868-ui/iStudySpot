package com.example.scylier.istudyspot.models.statistics

class DailyData(
    val date: String,
    val occupancyRate: Double,
    val bookings: Int,
    val revenue: Double
)

class StudyRoomStatisticsResponse(
    val studyRoomId: Long,
    val studyRoomName: String,
    val totalSeats: Int,
    val avgOccupancyRate: Double,
    val totalBookings: Int,
    val totalRevenue: Double,
    val dailyData: List<DailyData>
)
