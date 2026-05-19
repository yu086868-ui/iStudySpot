package com.example.scylier.istudyspot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.repository.MainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class NotificationItem(
    val id: String = "",
    val title: String,
    val content: String,
    val time: String,
    val isRead: Boolean,
    val type: NotificationType
)

enum class NotificationType {
    SYSTEM,
    BOOKING,
    REMINDER,
    ACTIVITY
}

data class NotificationUiState(
    val notifications: List<NotificationItem> = emptyList(),
    val isLoading: Boolean = true
)

class NotificationViewModel : ViewModel() {
    private val repository = MainRepository()

    private val _state = MutableStateFlow(NotificationUiState())
    val state: StateFlow<NotificationUiState> = _state

    val unreadCount: Int
        get() = _state.value.notifications.count { !it.isRead }

    private fun mockNotifications(): List<NotificationItem> = listOf(
        NotificationItem("1", "预约成功", "您已成功预约A区-12号座位，预约时间：2024-01-15 14:00-17:00", "10分钟前", false, NotificationType.BOOKING),
        NotificationItem("2", "签到提醒", "您预约的座位即将开始，请准时签到", "30分钟前", false, NotificationType.REMINDER),
        NotificationItem("3", "系统维护通知", "系统将于今晚00:00-02:00进行维护，期间无法预约", "2小时前", true, NotificationType.SYSTEM),
        NotificationItem("4", "优惠活动", "新春特惠：充值满100送20，活动截止至1月31日", "1天前", true, NotificationType.ACTIVITY),
        NotificationItem("5", "预约取消", "您预约的B区-05号座位已取消", "2天前", true, NotificationType.BOOKING),
        NotificationItem("6", "学习成就", "恭喜您！本周学习时长达到20小时，获得\"学习达人\"徽章", "3天前", true, NotificationType.ACTIVITY)
    )

    fun loadNotifications() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                when (val response = repository.getAnnouncements()) {
                    is ApiResponse.Success -> {
                        val data = response.data
                        @Suppress("UNCHECKED_CAST")
                        val items = (data["list"] as? List<Map<String, Any?>>) ?: emptyList()
                        if (items.isNotEmpty()) {
                            val notifications = items.map { item ->
                                val typeStr = item["type"] as? String ?: "system"
                                val notificationType = when (typeStr.lowercase()) {
                                    "booking", "reservation" -> NotificationType.BOOKING
                                    "reminder" -> NotificationType.REMINDER
                                    "activity", "promotion" -> NotificationType.ACTIVITY
                                    else -> NotificationType.SYSTEM
                                }
                                NotificationItem(
                                    id = item["id"] as? String ?: "",
                                    title = item["title"] as? String ?: "",
                                    content = item["content"] as? String ?: "",
                                    time = item["createdAt"] as? String ?: "",
                                    isRead = item["isRead"] as? Boolean ?: true,
                                    type = notificationType
                                )
                            }
                            _state.value = NotificationUiState(notifications = notifications, isLoading = false)
                        } else {
                            _state.value = NotificationUiState(notifications = mockNotifications(), isLoading = false)
                        }
                    }
                    is ApiResponse.Error -> {
                        _state.value = NotificationUiState(notifications = mockNotifications(), isLoading = false)
                    }
                }
            } catch (e: Exception) {
                _state.value = NotificationUiState(notifications = mockNotifications(), isLoading = false)
            }
        }
    }
}
