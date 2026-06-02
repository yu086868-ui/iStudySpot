package com.example.scylier.istudyspot.models

sealed class ApiResponse<out T> {
    data class Success<out T>(val code: Int, val message: String, val data: T?) : ApiResponse<T>()
    data class Error(val code: Int, val message: String) : ApiResponse<Nothing>()
}

data class BaseResponse<T>(
    val code: Int,
    val message: String,
    val data: T?,
    val timestamp: Long? = null
)
