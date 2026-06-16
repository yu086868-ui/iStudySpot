package com.example.scylier.istudyspot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.repository.MainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class NotificationItem(
    val id: String = "",
    val title: String,
    val content: String,
    val time: String,
    val isRead: Boolean,
    val type: NotificationType
) {
    val displayTime: String
        get() = formatNotificationTime(time)
}

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

class NotificationViewModel(
    private val repository: MainRepository = MainRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationUiState())
    val state: StateFlow<NotificationUiState> = _state

    val unreadCount: Int
        get() = _state.value.notifications.count { !it.isRead }

    private fun mockNotifications(): List<NotificationItem> = listOf(
        NotificationItem(
            id = "1",
            title = "自习室开放时间调整",
            content = "本周起图书馆三层东侧自习区工作日开放至 22:30，请以现场安排为准。",
            time = "2026-06-16 09:20:00",
            isRead = true,
            type = NotificationType.SYSTEM
        ),
        NotificationItem(
            id = "2",
            title = "预约成功",
            content = "你已成功预约一号自习室 A12，使用时间为 06-16 14:00 至 17:00。",
            time = "2026-06-16 08:45:00",
            isRead = true,
            type = NotificationType.BOOKING
        ),
        NotificationItem(
            id = "3",
            title = "签到提醒",
            content = "你今天的预约将在 15 分钟后开始，请提前到场并完成签到。",
            time = "2026-06-16 08:10:00",
            isRead = true,
            type = NotificationType.REMINDER
        ),
        NotificationItem(
            id = "4",
            title = "场馆入口临时调整",
            content = "受天气影响，今晚 19:00 后二号自习室请从南门进入。",
            time = "2026-06-15 17:40:00",
            isRead = true,
            type = NotificationType.SYSTEM
        ),
        NotificationItem(
            id = "5",
            title = "周末高峰提醒",
            content = "周末 14:00 至 18:00 为使用高峰，建议提前一天完成座位预约。",
            time = "2026-06-14 11:30:00",
            isRead = true,
            type = NotificationType.REMINDER
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
                                val typeStr = item["type"]?.toString()?.lowercase() ?: "system"
                                val notificationType = when (typeStr) {
                                    "booking", "reservation" -> NotificationType.BOOKING
                                    "reminder" -> NotificationType.REMINDER
                                    "activity", "promotion" -> NotificationType.ACTIVITY
                                    else -> NotificationType.SYSTEM
                                }
                                NotificationItem(
                                    id = item["id"]?.toString() ?: "",
                                    title = item["title"]?.toString() ?: "",
                                    content = item["content"]?.toString() ?: "",
                                    time = item["createdAt"]?.toString()
                                        ?: item["publishTime"]?.toString()
                                        ?: "",
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
            } catch (_: Exception) {
                _state.value = NotificationUiState(
                    notifications = mockNotifications(),
                    isLoading = false
                )
            }
        }
    }
}

private fun formatNotificationTime(raw: String): String {
    if (raw.isBlank()) return ""

    val dateTime = parseNotificationTime(raw) ?: return raw
    val now = LocalDateTime.now()
    val today = now.toLocalDate()
    val targetDate = dateTime.toLocalDate()

    return when {
        targetDate == today -> "今天 ${dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        targetDate == today.minusDays(1) -> "昨天 ${dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        targetDate.year == today.year -> dateTime.format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))
        else -> dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }
}

private fun parseNotificationTime(raw: String): LocalDateTime? {
    val patterns = listOf(
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm",
        "yyyy-MM-dd'T'HH:mm"
    )

    patterns.forEach { pattern ->
        runCatching {
            return LocalDateTime.parse(raw, DateTimeFormatter.ofPattern(pattern))
        }
    }

    return runCatching { LocalDateTime.parse(raw) }.getOrNull()
}
