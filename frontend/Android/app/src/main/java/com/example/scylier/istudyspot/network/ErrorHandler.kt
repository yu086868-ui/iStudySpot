package com.example.scylier.istudyspot.network

import com.example.scylier.istudyspot.models.ApiResponse

object ErrorHandler {
    fun handleError(code: Int, message: String): String {
        return when (code) {
            400 -> "请求参数错误: $message"
            401 -> "未授权，请重新登录"
            404 -> "资源不存在: $message"
            500 -> "服务器内部错误: $message"
            else -> "网络请求失败: $message"
        }
    }

    fun <T> getErrorMessage(response: ApiResponse<T>): String {
        return when (response) {
            is ApiResponse.Error -> handleError(response.code, response.message)
            else -> "操作成功"
        }
    }

    fun isNetworkError(code: Int): Boolean {
        return code in 500..599
    }

    fun isAuthError(code: Int): Boolean {
        return code == 401
    }

    fun isNotFoundError(code: Int): Boolean {
        return code == 404
    }

    fun isBadRequestError(code: Int): Boolean {
        return code == 400
    }
}
