package com.example.scylier.istudyspot.models.auth

import com.example.scylier.istudyspot.models.BaseResponse

// 登录请求
class LoginRequest(
    val username: String,
    val password: String
)

// 注册请求
class RegisterRequest(
    val username: String,
    val password: String,
    val nickname: String,
    val phone: String? = null,
    val studentId: String? = null
)

// 用户信息
class UserInfo(
    val id: String,
    val username: String,
    val nickname: String,
    val avatar: String? = null,
    val phone: String? = null,
    val email: String? = null
)

// 登录响应
class LoginResponse(
    val token: String,
    val user: UserInfo
)

// 注册响应
class RegisterResponse(
    val token: String,
    val user: UserInfo
)

// 令牌响应
class TokenResponse(
    val token: String
)
