package com.example.scylier.istudyspot.viewmodel

import androidx.lifecycle.ViewModel

/**
 * 通知提醒ViewModel
 * 管理用户的通知消息
 */
class NotificationViewModel : ViewModel() {

    data class Notification(
        val title: String,
        val content: String,
        val time: String,
        val isRead: Boolean,
        val type: NotificationType
    )

    enum class NotificationType {
        SYSTEM,      // 系统通知
        BOOKING,     // 预约相关
        REMINDER,    // 提醒
        ACTIVITY     // 活动消息
    }

    // 通知列表（模拟数据）
    val notifications = listOf(
        Notification(
            "预约成功",
            "您已成功预约A区-12号座位，预约时间：2024-01-15 14:00-17:00",
            "10分钟前",
            false,
            NotificationType.BOOKING
        ),
        Notification(
            "签到提醒",
            "您预约的座位即将开始，请准时签到",
            "30分钟前",
            false,
            NotificationType.REMINDER
        ),
        Notification(
            "系统维护通知",
            "系统将于今晚00:00-02:00进行维护，期间无法预约",
            "2小时前",
            true,
            NotificationType.SYSTEM
        ),
        Notification(
            "优惠活动",
            "新春特惠：充值满100送20，活动截止至1月31日",
            "1天前",
            true,
            NotificationType.ACTIVITY
        ),
        Notification(
            "预约取消",
            "您预约的B区-05号座位已取消",
            "2天前",
            true,
            NotificationType.BOOKING
        ),
        Notification(
            "学习成就",
            "恭喜您！本周学习时长达到20小时，获得\"学习达人\"徽章",
            "3天前",
            true,
            NotificationType.ACTIVITY
        )
    )

    // 未读消息数量
    val unreadCount: Int
        get() = notifications.count { !it.isRead }
}
