package com.example.scylier.istudyspot.viewmodel

import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {
    // 用户信息相关的数据和逻辑
    val userInfo = UserInfo(
        "张三",
        "2024001234",
        1280,
        "LV5",
        48,
        "156h",
        0
    )

    val menuItems = listOf(
        MenuItem("消息中心", "3", "blue_600", "blue_50"),
        MenuItem("我的钱包", "¥128.00", "green_600", "green_50"),
        MenuItem("个人设置", null, "gray_600", "gray_50")
    )

    data class UserInfo(
        val name: String,
        val studentId: String,
        val points: Int,
        val level: String,
        val reservationCount: Int,
        val studyHours: String,
        val violationCount: Int
    )

    data class MenuItem(val label: String, val rightText: String?, val color: String, val bgColor: String)
}
