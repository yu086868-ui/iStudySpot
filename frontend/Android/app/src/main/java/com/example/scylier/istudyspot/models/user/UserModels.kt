package com.example.scylier.istudyspot.models.user

// 更新用户信息请求
class UpdateUserRequest(
    val nickname: String? = null,
    val avatar: String? = null,
    val phone: String? = null,
    val email: String? = null
)

// 修改密码请求
class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)
