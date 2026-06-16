package com.example.scylier.istudyspot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scylier.istudyspot.infra.network.ApiClient
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.repository.MainRepository
import com.example.scylier.istudyspot.utils.ConfigManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val username: String = "未登录",
    val nickname: String = "",
    val phone: String = "未设置",
    val email: String = "未设置",
    val avatar: String? = null,
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = true,
    val totalStudyHours: Int = 0,
    val streakDays: Int = 0,
    val totalBookings: Int = 0,
    val studyLevel: Int = 1,
    val studyLevelTitle: String = "新手",
    val levelProgress: Float = 0f
)

class ProfileViewModel(
    private val repository: MainRepository = MainRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state

    private fun mockStudyStats(): ProfileUiState = ProfileUiState(
        totalStudyHours = 328,
        streakDays = 7,
        totalBookings = 45,
        studyLevel = 5,
        studyLevelTitle = "学习达人",
        levelProgress = 0.65f
    )

    fun loadProfile(configManager: ConfigManager) {
        val savedToken = configManager.getToken()
        if (savedToken != null) {
            ApiClient.currentToken = savedToken
            _state.value = _state.value.copy(isLoggedIn = true)
            configManager.getUsername()?.let { _state.value = _state.value.copy(username = it) }
            configManager.getNickname()?.let {
                if (it.isNotEmpty()) _state.value = _state.value.copy(nickname = it)
            }
            viewModelScope.launch {
                when (val response = repository.getUserInfo()) {
                    is ApiResponse.Success -> {
                        val user = response.data
                        if (user != null) {
                            _state.value = _state.value.copy(
                                username = user.username,
                                nickname = user.nickname ?: user.username,
                                phone = user.phone ?: "未设置",
                                email = user.email ?: "未设置",
                                avatar = user.avatar,
                                isLoading = false
                            )
                        } else {
                            _state.value = _state.value.copy(isLoading = false)
                        }
                    }
                    is ApiResponse.Error -> {
                        _state.value = _state.value.copy(isLoading = false)
                    }
                }
                loadStudyStats()
            }
        } else {
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    private suspend fun loadStudyStats() {
        try {
            var totalHours = 0
            var streak = 0
            var bookings = 0

            when (val recordsResponse = repository.getCheckinRecords()) {
                is ApiResponse.Success -> {
                    val data = recordsResponse.data ?: emptyMap()
                    totalHours = (data["totalHours"] as? Number)?.toInt() ?: 0
                    streak = (data["streak"] as? Number)?.toInt() ?: 0
                }
                is ApiResponse.Error -> {}
            }

            when (val ordersResponse = repository.getUserOrders()) {
                is ApiResponse.Success -> {
                    bookings = ordersResponse.data?.list?.size ?: 0
                }
                is ApiResponse.Error -> {}
            }

            if (totalHours > 0 || bookings > 0) {
                val level = calculateLevel(totalHours)
                val progress = calculateLevelProgress(totalHours, level)
                _state.value = _state.value.copy(
                    totalStudyHours = totalHours,
                    streakDays = streak,
                    totalBookings = bookings,
                    studyLevel = level,
                    studyLevelTitle = getLevelTitle(level),
                    levelProgress = progress
                )
            } else {
                val mock = mockStudyStats()
                _state.value = _state.value.copy(
                    totalStudyHours = mock.totalStudyHours,
                    streakDays = mock.streakDays,
                    totalBookings = mock.totalBookings,
                    studyLevel = mock.studyLevel,
                    studyLevelTitle = mock.studyLevelTitle,
                    levelProgress = mock.levelProgress
                )
            }
        } catch (e: Exception) {
            val mock = mockStudyStats()
            _state.value = _state.value.copy(
                totalStudyHours = mock.totalStudyHours,
                streakDays = mock.streakDays,
                totalBookings = mock.totalBookings,
                studyLevel = mock.studyLevel,
                studyLevelTitle = mock.studyLevelTitle,
                levelProgress = mock.levelProgress
            )
        }
    }

    fun calculateLevel(hours: Int): Int {
        return when {
            hours < 10 -> 1
            hours < 50 -> 2
            hours < 100 -> 3
            hours < 200 -> 4
            hours < 500 -> 5
            hours < 1000 -> 6
            hours < 2000 -> 7
            else -> 8
        }
    }

    fun calculateLevelProgress(hours: Int, level: Int): Float {
        val thresholds = listOf(0, 10, 50, 100, 200, 500, 1000, 2000)
        if (level >= thresholds.size) return 1f
        val currentThreshold = thresholds[level - 1]
        val nextThreshold = thresholds[level]
        return ((hours - currentThreshold).toFloat() / (nextThreshold - currentThreshold)).coerceIn(0f, 1f)
    }

    fun getLevelTitle(level: Int): String {
        return when (level) {
            1 -> "新手"
            2 -> "入门"
            3 -> "进阶"
            4 -> "熟练"
            5 -> "学习达人"
            6 -> "学霸"
            7 -> "学神"
            else -> "传奇"
        }
    }

    fun logout(configManager: ConfigManager) {
        viewModelScope.launch {
            repository.logout()
            configManager.clearAll()
            ApiClient.currentToken = null
            _state.value = ProfileUiState()
        }
    }
}
