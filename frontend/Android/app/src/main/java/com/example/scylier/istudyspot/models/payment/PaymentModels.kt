package com.example.scylier.istudyspot.models.payment

// 创建支付请求
class CreatePaymentRequest(
    val orderId: String,
    val amount: Double,
    val paymentMethod: String // wechat, alipay
)

// 支付响应
class PaymentResponse(
    val paymentId: String,
    val orderId: String,
    val amount: Double,
    val paymentMethod: String,
    val paymentUrl: String,
    val createdAt: String
)

// 支付状态响应
class PaymentStatusResponse(
    val id: String,
    val orderId: String,
    val amount: Double,
    val paymentMethod: String,
    val status: String, // pending, success, failed
    val createdAt: String,
    val updatedAt: String
)
