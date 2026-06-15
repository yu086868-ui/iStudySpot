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

class NotificationViewModel(private val repository: MainRepository = MainRepository()) : ViewModel() {

    private val _state = MutableStateFlow(NotificationUiState())
    val state: StateFlow<NotificationUiState> = _state

    val unreadCount: Int
        get() = _state.value.notifications.count { !it.isRead }

    private fun mockNotifications(): List<NotificationItem> = listOf(
        NotificationItem(
            id = "1",
            title = "预约成功",
            content = "您已成功预约座位 A12，预约时间：2024-01-15 14:00-17:00",
            time = "10分钟前",
            isRead = false,
            type = NotificationType.BOOKING
        ),
        NotificationItem(
            id = "2",
            title = "签到提醒",
            content = "您预约的座位即将开始使用，请准时签到。",
            time = "30分钟前",
            isRead = false,
            type = NotificationType.REMINDER
        ),
        NotificationItem(
            id = "3",
            title = "系统维护通知",
            content = "系统将于今晚 00:00-02:00 进行维护，期间无法预约。",
            time = "2小时前",
            isRead = true,
            type = NotificationType.SYSTEM
        ),
        NotificationItem(
            id = "4",
            title = "优惠活动",
            content = "新春特惠：充值满 100 送 20，活动截止至 1 月 31 日。",
            time = "1天前",
            isRead = true,
            type = NotificationType.ACTIVITY
        ),
        NotificationItem(
            id = "5",
            title = "预约取消",
            content = "您预约的座位 B05 已取消。",
            time = "2天前",
            isRead = true,
            type = NotificationType.BOOKING
        ),
        NotificationItem(
            id = "6",
            title = "学习成就",
            content = "恭喜你！本周学习时长达到 20 小时，获得“学习达人”徽章。",
            time = "3天前",
            isRead = true,
            type = NotificationType.ACTIVITY
        )
    )

    fun loadNotifications() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                when (val response = repository.getAnnouncements()) {
                    is ApiResponse.Success -> {
                        val data = response.data ?: emptyMap()
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
                            _state.value = NotificationUiState(
                                notifications = notifications,
                                isLoading = false
                            )
                        } else {
                            _state.value = NotificationUiState(
                                notifications = mockNotifications(),
                                isLoading = false
                            )
                        }
                    }

                    is ApiResponse.Error -> {
                        _state.value = NotificationUiState(
                            notifications = mockNotifications(),
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _state.value = NotificationUiState(
                    notifications = mockNotifications(),
                    isLoading = false
                )
            }
        }
    }
}
