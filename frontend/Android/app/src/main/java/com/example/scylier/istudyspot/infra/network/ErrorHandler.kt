package com.example.scylier.istudyspot.infra.network

import com.example.scylier.istudyspot.models.ApiResponse

object ErrorHandler {
    fun handleError(code: Int, message: String): String {
        val safeMessage = if (message.contains("<") && message.contains(">")) "服务器异常" else message
        return when (code) {
            400 -> "请求参数错误"
            401 -> "未授权，请重新登录"
            403 -> "没有权限执行此操作"
            404 -> "请求的资源不存在"
            408 -> "网络请求超时，请重试"
            500 -> "服务器内部错误，请稍后重试"
            else -> "网络请求失败($code)"
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
