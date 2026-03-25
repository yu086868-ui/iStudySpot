package com.example.scylier.istudyspot.viewmodel

import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {
    // 首页相关的数据和逻辑
    val mainFeatures = listOf(
        Feature("预约座位", "blue_600"),
        Feature("签到", "green_600"),
        Feature("场馆导览", "purple_600"),
        Feature("我的预约", "orange_600"),
        Feature("学习记录", "pink_600"),
        Feature("团队预约", "indigo_600"),
        Feature("通知提醒", "red_600"),
        Feature("偏好设置", "teal_600")
    )

    data class Feature(val label: String, val color: String)
}
