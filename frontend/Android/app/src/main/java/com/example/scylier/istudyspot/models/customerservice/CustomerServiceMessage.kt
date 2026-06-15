package com.example.scylier.istudyspot.models.customerservice

data class CustomerServiceMessage(
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long
)
