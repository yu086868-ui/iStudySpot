package com.example.scylier.istudyspot.state

data class BookingState(
    val startTime: String = "",
    val endTime: String = "",
    val bookingType: String = "hour",
    val totalPrice: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)
