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
    val displayLabel: String
        get() = seatNumber?.takeIf { it.isNotBlank() } ?: "${rowNum}-${colNum}"
    val coordinateLabel: String
        get() = "${rowNum}排${colNum}列"
    val row: Int
        get() = rowNum
    val col: Int
        get() = colNum
}

class SeatLayoutItemInfo(
    val id: Long,
    val roomId: Long,
    val areaId: Long? = null,
    val seatId: Long? = null,
    val itemType: String,
    val itemKey: String? = null,
    val label: String? = null,
    val rowNum: Int,
    val colNum: Int,
    val widthUnits: Int = 1,
    val heightUnits: Int = 1,
    val rotation: Int = 0,
    val zIndex: Int = 0,
    val metadata: String? = null
) {
    val row: Int
        get() = rowNum
    val col: Int
        get() = colNum
    val width: Int
        get() = widthUnits.coerceAtLeast(1)
    val height: Int
        get() = heightUnits.coerceAtLeast(1)
    val isSeat: Boolean
        get() = itemType == "seat"
}

class SeatLayoutData(
    val studyRoomId: Long,
    val studyRoomName: String,
    val rows: Int,
    val cols: Int,
    val cellSize: Int = 40,
    val layoutMode: String = "grid",
    val seats: List<SeatInfo> = emptyList(),
    val items: List<SeatLayoutItemInfo> = emptyList()
)

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
    val displayLabel: String
        get() = seatNumber?.takeIf { it.isNotBlank() } ?: "${rowNum}-${colNum}"
    val row: Int
        get() = rowNum
    val col: Int
        get() = colNum
    val type: String
        get() = if (seatType == 2) "vip" else "normal"
}
