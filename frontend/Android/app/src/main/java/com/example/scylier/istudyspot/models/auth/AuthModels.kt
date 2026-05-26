package com.example.scylier.istudyspot.models.auth

class LoginRequest(
    val username: String,
    val password: String
)

class RegisterRequest(
    val username: String,
    val password: String,
    val nickname: String? = null,
    val phone: String? = null,
    val studentId: String? = null
)

class UserInfo(
    val id: Long,
    val username: String,
    val nickname: String? = null,
    val avatar: String? = null,
    val phone: String? = null,
    val email: String? = null
)

class LoginResponse(
    val token: String,
    val refreshToken: String? = null,
    val user: UserInfo
)

class RegisterResponse(
    val userId: Long? = null,
    val token: String? = null,
    val user: UserInfo? = null
)

class TokenResponse(
    val token: String
)
