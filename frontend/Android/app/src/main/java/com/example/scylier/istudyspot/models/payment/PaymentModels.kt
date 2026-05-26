package com.example.scylier.istudyspot.models.payment

class CreatePaymentRequest(
    val orderId: Long,
    val amount: Double,
    val paymentMethod: String
)

class PaymentResponse(
    val id: Long,
    val paymentNo: String? = null,
    val orderId: Long,
    val amount: Double,
    val paymentMethod: String,
    val paymentUrl: String? = null,
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
