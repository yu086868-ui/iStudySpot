package com.example.scylier.istudyspot.models.studyroom

// 自习室列表项
class StudyRoomItem(
    val id: String,
    val name: String,
    val address: String,
    val openingHours: String,
    val occupancyRate: Double,
    val imageUrl: String? = null
)

// 自习室列表响应
class StudyRoomListResponse(
    val total: Int,
    val list: List<StudyRoomItem>
)

// 自习室详情
class StudyRoomDetail(
    val id: String,
    val name: String,
    val address: String,
    val openingHours: String,
    val description: String? = null,
    val rules: String? = null,
    val imageUrl: String? = null
)

// 座位信息
class SeatInfo(
    val id: String,
    val row: Int,
    val col: Int,
    val status: String, // available, booked, occupied, unavailable
    val type: String, // normal, vip
    val pricePerHour: Double
)

// 座位图响应
class SeatMapResponse(
    val studyRoomId: String,
    val rows: Int,
    val cols: Int,
    val seats: List<SeatInfo>
)

// 座位详情
class SeatDetail(
    val id: String,
    val studyRoomId: String,
    val row: Int,
    val col: Int,
    val status: String,
    val type: String,
    val pricePerHour: Double,
    val description: String? = null
)
