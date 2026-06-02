package com.example.scylier.istudyspot.models.order

class CreateOrderRequest(
    val studyRoomId: Long,
    val seatId: Long,
    val startTime: String,
    val endTime: String,
    val bookingType: String
)

class OrderResponse(
    val id: Long = 0,
    val seatId: Long = 0,
    val userId: Long = 0,
    val startTime: String? = null,
    val endTime: String? = null,
    val totalPrice: Double? = null,
    val status: String? = null,
    val createdAt: String? = null
)

class OrderItem(
    val id: Long = 0,
    val orderNo: String? = null,
    val seatId: Long = 0,
    val studyRoomName: String? = null,
    val seatPosition: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val totalPrice: Double? = null,
    val totalAmount: Double? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val roomName: String? = null,
    val seatNumber: String? = null
) {
    val displayName: String get() = studyRoomName ?: roomName ?: "自习室"
    val displaySeat: String get() = seatPosition ?: seatNumber ?: "未知座位"
    val displayPrice: Double get() = totalPrice ?: totalAmount ?: 0.0
}

class OrderListResponse(
    val total: Int = 0,
    val list: List<OrderItem> = emptyList()
)

class OrderDetail(
    val id: Long = 0,
    val orderNo: String? = null,
    val seatId: Long = 0,
    val userId: Long = 0,
    val studyRoomName: String? = null,
    val seatPosition: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val totalPrice: Double? = null,
    val totalAmount: Double? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val roomName: String? = null,
    val seatNumber: String? = null,
    val checkinTime: String? = null,
    val checkoutTime: String? = null,
    val actualDuration: Int? = null,
    val actualPrice: Double? = null,
    val actualStartTime: String? = null,
    val actualEndTime: String? = null
) {
    val displayName: String get() = studyRoomName ?: roomName ?: "自习室"
    val displaySeat: String get() = seatPosition ?: seatNumber ?: "未知座位"
    val displayPrice: Double get() = totalPrice ?: totalAmount ?: 0.0
}

class CancelOrderResponse(
    val id: Long = 0,
    val status: String? = null
)

class CheckinRequest(
    val reservationId: String,
    val seatId: String
)

class CheckinResponse(
    val id: Long = 0,
    val checkinTime: String? = null,
    val status: String? = null
)

class CheckoutResponse(
    val id: Long = 0,
    val checkoutTime: String? = null,
    val actualDuration: Int = 0,
    val actualPrice: Double = 0.0,
    val status: String? = null
)
