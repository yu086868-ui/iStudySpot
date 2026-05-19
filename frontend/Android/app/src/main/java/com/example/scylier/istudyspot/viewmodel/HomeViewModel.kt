package com.example.scylier.istudyspot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scylier.istudyspot.R
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.FunctionItem
import com.example.scylier.istudyspot.repository.MainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeUiState(
    val todayBookings: Int = 0,
    val studyHours: String = "0h",
    val streakDays: String = "0天",
    val greeting: String = "",
    val motivationalQuote: String = "",
    val isLoading: Boolean = true
)

class HomeViewModel : ViewModel() {
    private val repository = MainRepository()

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state

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

    private val quotes = listOf(
        "学如逆水行舟，不进则退",
        "书山有路勤为径，学海无涯苦作舟",
        "宝剑锋从磨砺出，梅花香自苦寒来",
        "千里之行，始于足下",
        "业精于勤，荒于嬉",
        "读书破万卷，下笔如有神",
        "天道酬勤，力耕不欺",
        "勤学如春起之苗，不见其增，日有所长"
    )

    private fun mockHomeUiState(): HomeUiState {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour in 5..11 -> "早上好"
            hour in 12..13 -> "中午好"
            hour in 14..17 -> "下午好"
            else -> "晚上好"
        }
        return HomeUiState(
            todayBookings = 3,
            studyHours = "2.5h",
            streakDays = "7天",
            greeting = greeting,
            motivationalQuote = quotes.random(),
            isLoading = false
        )
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val greeting = when {
                hour in 5..11 -> "早上好"
                hour in 12..13 -> "中午好"
                hour in 14..17 -> "下午好"
                else -> "晚上好"
            }

            try {
                var todayBookings = 0
                var studyHours = "0h"
                var streakDays = "0天"

                when (val ordersResponse = repository.getUserOrders()) {
                    is ApiResponse.Success -> {
                        todayBookings = ordersResponse.data.list.count {
                            it.status == "paid" || it.status == "pending"
                        }
                    }
                    is ApiResponse.Error -> {}
                }

                when (val recordsResponse = repository.getCheckinRecords()) {
                    is ApiResponse.Success -> {
                        val data = recordsResponse.data
                        @Suppress("UNCHECKED_CAST")
                        val records = (data["records"] as? List<Map<String, Any?>>) ?: emptyList()
                        val totalMinutes = records.size * 150
                        val hours = totalMinutes / 60.0
                        studyHours = String.format("%.1fh", hours)
                        val streak = (data["streak"] as? Number)?.toInt() ?: 0
                        streakDays = "${streak}天"
                    }
                    is ApiResponse.Error -> {}
                }

                _state.value = HomeUiState(
                    todayBookings = todayBookings,
                    studyHours = studyHours,
                    streakDays = streakDays,
                    greeting = greeting,
                    motivationalQuote = quotes.random(),
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = mockHomeUiState()
            }
        }
    }
}
