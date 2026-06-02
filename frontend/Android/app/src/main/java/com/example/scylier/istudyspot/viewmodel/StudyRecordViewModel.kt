package com.example.scylier.istudyspot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.repository.MainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class StudyRecordUiState(
    val weekStudyHours: Int = 0,
    val monthStudyHours: Int = 0,
    val totalStudyHours: Int = 0,
    val totalBookings: Int = 0,
    val streakDays: Int = 0,
    val avgStudyDuration: Double = 0.0,
    val favoriteSeat: String = "",
    val peakTime: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

class StudyRecordViewModel(private val repository: MainRepository = MainRepository()) : ViewModel() {

    private val _state = MutableStateFlow(StudyRecordUiState())
    val state: StateFlow<StudyRecordUiState> = _state

    fun loadStudyRecords() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                var weekHours = 0
                var monthHours = 0
                var totalHours = 0
                var totalBookings = 0
                var streak = 0
                var avgDuration = 0.0
                var favSeat = ""
                var peak = ""
                var hasError = false
                var errorMsg = ""

                when (val response = repository.getCheckinRecords()) {
                    is ApiResponse.Success -> {
                        val data = response.data ?: emptyMap()
                        totalHours = (data["totalHours"] as? Number)?.toInt() ?: 0
                        streak = (data["streak"] as? Number)?.toInt() ?: 0
                        avgDuration = (data["avgDuration"] as? Number)?.toDouble() ?: 0.0
                        favSeat = (data["favoriteSeat"] as? String) ?: ""
                        peak = (data["peakTime"] as? String) ?: ""
                        weekHours = (data["weekHours"] as? Number)?.toInt() ?: 0
                        monthHours = (data["monthHours"] as? Number)?.toInt() ?: 0
                    }
                    is ApiResponse.Error -> {
                        hasError = true
                        errorMsg = response.message
                    }
                }

                when (val ordersResponse = repository.getUserOrders()) {
                    is ApiResponse.Success -> {
                        totalBookings = ordersResponse.data?.total ?: 0
                    }
                    is ApiResponse.Error -> {}
                }

                _state.value = StudyRecordUiState(
                    weekStudyHours = weekHours,
                    monthStudyHours = monthHours,
                    totalStudyHours = totalHours,
                    totalBookings = totalBookings,
                    streakDays = streak,
                    avgStudyDuration = avgDuration,
                    favoriteSeat = favSeat,
                    peakTime = peak,
                    isLoading = false,
                    error = if (hasError) errorMsg else null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "加载学习报告失败: ${e.message}"
                )
            }
        }
    }
}
