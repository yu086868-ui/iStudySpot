package com.example.scylier.istudyspot.models.payment

class CreatePaymentRequest(
    val orderId: Long,
    val amount: Double,
    val paymentMethod: String
)

class PaymentResponse(
    val paymentId: String? = null,
    val id: Long? = null,
    val paymentNo: String? = null,
    val orderId: Long? = null,
    val amount: Double? = null,
    val paymentMethod: String? = null,
    val status: String? = null,
    val paymentUrl: String? = null,
    val payTime: String? = null,
    val createdAt: String? = null
)

class PaymentStatusResponse(
    val id: Long,
    val orderId: Long,
    val amount: Double,
    val paymentMethod: String,
    val status: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
