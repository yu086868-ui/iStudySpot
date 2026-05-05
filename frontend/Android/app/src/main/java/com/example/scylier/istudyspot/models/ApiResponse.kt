package com.example.scylier.istudyspot.models

/**
 * API响应的通用结构
 */
sealed class ApiResponse<out T> {
    data class Success<out T>(val code: Int, val message: String, val data: T) : ApiResponse<T>()
    data class Error(val code: Int, val message: String) : ApiResponse<Nothing>()
}

/**
 * API响应的基础结构
 */
data class BaseResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
)
