package com.example.scylier.istudyspot.viewmodel

import androidx.lifecycle.ViewModel
import com.example.scylier.istudyspot.R
import com.example.scylier.istudyspot.models.FunctionItem

class HomeViewModel : ViewModel() {
    // 首页相关的数据和逻辑
    val mainFeatures = listOf(
        FunctionItem("booking", R.drawable.ic_calendar, "预约座位", R.color.blue_600),
        FunctionItem("checkin", R.drawable.ic_clock, "签到", R.color.green_600),
        FunctionItem("guide", R.drawable.ic_map_pin, "场馆导览", R.color.purple_600),
        FunctionItem("my_booking", R.drawable.ic_credit_card, "我的预约", R.color.orange_600),
        FunctionItem("study_record", R.drawable.ic_book_marked, "学习记录", R.color.pink_600),
        FunctionItem("team_booking", R.drawable.ic_users, "团队预约", R.color.indigo_600),
        FunctionItem("notification", R.drawable.ic_bell, "通知提醒", R.color.red_600),
        FunctionItem("settings", R.drawable.ic_settings, "偏好设置", R.color.teal_600)
    )
}
