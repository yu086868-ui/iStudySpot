package com.example.scylier.istudyspot.models.studyroom

class StudyRoomItem(
    val id: Long,
    val name: String,
    val address: String,
    val openTime: String? = null,
    val closeTime: String? = null,
    val imageUrl: String? = null,
    val status: Int? = null,
    val description: String? = null
) {
    val openingHours: String
        get() = "${openTime ?: ""}-${closeTime ?: ""}"
}

class StudyRoomListResponse(
    val total: Int,
    val list: List<StudyRoomItem>
)

class StudyRoomDetail(
    val id: Long,
    val name: String,
    val address: String,
    val openTime: String? = null,
    val closeTime: String? = null,
    val description: String? = null,
    val rules: String? = null,
    val imageUrl: String? = null
) {
    val openingHours: String
        get() = "${openTime ?: ""}-${closeTime ?: ""}"
}

class SeatInfo(
    val id: Long,
    val rowNum: Int,
    val colNum: Int,
    val status: String,
    val seatType: Int,
    val pricePerHour: Double,
    val seatNumber: String? = null,
    val hasPower: Int? = null,
    val hasLamp: Int? = null,
    val isWindow: Int? = null,
    val description: String? = null
) {
    val type: String
        get() = if (seatType == 2) "vip" else "normal"
    val row: Int
        get() = rowNum
    val col: Int
        get() = colNum
}

class SeatDetail(
    val id: Long,
    val roomId: Long,
    val rowNum: Int,
    val colNum: Int,
    val status: String,
    val seatType: Int,
    val pricePerHour: Double,
    val seatNumber: String? = null,
    val description: String? = null
) {
    val studyRoomId: Long
        get() = roomId
    val row: Int
        get() = rowNum
    val col: Int
        get() = colNum
    val type: String
        get() = if (seatType == 2) "vip" else "normal"
}
