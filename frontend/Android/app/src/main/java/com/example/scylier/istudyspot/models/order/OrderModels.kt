package com.example.scylier.istudyspot.models.order

// 创建订单请求
class CreateOrderRequest(
    val studyRoomId: String,
    val seatId: String,
    val startTime: String,
    val endTime: String,
    val bookingType: String
)

// 订单响应
class OrderResponse(
    val id: String,
    val seatId: String,
    val userId: String,
    val startTime: String,
    val endTime: String,
    val totalPrice: Double,
    val status: String, // pending, paid, completed, cancelled
    val createdAt: String
)

// 订单列表项
class OrderItem(
    val id: String,
    val seatId: String,
    val studyRoomName: String,
    val seatPosition: String,
    val startTime: String,
    val endTime: String,
    val totalPrice: Double,
    val status: String,
    val createdAt: String
)

// 订单列表响应
class OrderListResponse(
    val total: Int,
    val list: List<OrderItem>
)

// 订单详情
class OrderDetail(
    val id: String,
    val seatId: String,
    val userId: String,
    val studyRoomName: String,
    val seatPosition: String,
    val startTime: String,
    val endTime: String,
    val totalPrice: Double,
    val status: String,
    val createdAt: String,
    val updatedAt: String
)

// 取消订单响应
class CancelOrderResponse(
    val id: String,
    val status: String
)

// 签到请求
class CheckinRequest(
    val checkinCode: String
)

// 签到响应
class CheckinResponse(
    val id: String,
    val checkinTime: String,
    val status: String
)

// 签退响应
class CheckoutResponse(
    val id: String,
    val checkoutTime: String,
    val actualDuration: Int, // 实际使用分钟数
    val actualPrice: Double,
    val status: String
)
