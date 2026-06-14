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

class StudyRecordViewModel(
    private val repository: MainRepository = MainRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(StudyRecordUiState())
    val state: StateFlow<StudyRecordUiState> = _state

    fun loadStudyRecords() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val fallback = fallbackStudyRecord()
                var result = fallback
                var errorMessage: String? = null

                when (val response = repository.getCheckinRecords()) {
                    is ApiResponse.Success -> {
                        val data = response.data ?: emptyMap()
                        result = result.copy(
                            totalStudyHours = (data["totalHours"] as? Number)?.toInt() ?: result.totalStudyHours,
                            streakDays = (data["streak"] as? Number)?.toInt() ?: result.streakDays,
                            avgStudyDuration = (data["avgDuration"] as? Number)?.toDouble() ?: result.avgStudyDuration,
                            favoriteSeat = (data["favoriteSeat"] as? String)?.takeIf { it.isNotBlank() }
                                ?: result.favoriteSeat,
                            peakTime = (data["peakTime"] as? String)?.takeIf { it.isNotBlank() }
                                ?: result.peakTime,
                            weekStudyHours = (data["weekHours"] as? Number)?.toInt() ?: result.weekStudyHours,
                            monthStudyHours = (data["monthHours"] as? Number)?.toInt() ?: result.monthStudyHours
                        )
                    }

                    is ApiResponse.Error -> {
                        errorMessage = response.message
                    }
                }

                when (val ordersResponse = repository.getUserOrders()) {
                    is ApiResponse.Success -> {
                        result = result.copy(
                            totalBookings = ordersResponse.data?.total ?: result.totalBookings
                        )
                    }

                    is ApiResponse.Error -> Unit
                }

                _state.value = result.copy(
                    isLoading = false,
                    error = errorMessage
                )
            } catch (e: Exception) {
                _state.value = fallbackStudyRecord().copy(
                    isLoading = false,
                    error = "加载学习报告失败: ${e.message}"
                )
            }
        }
    }

    private fun fallbackStudyRecord(): StudyRecordUiState {
        return StudyRecordUiState(
            weekStudyHours = 12,
            monthStudyHours = 46,
            totalStudyHours = 168,
            totalBookings = 23,
            streakDays = 6,
            avgStudyDuration = 2.8,
            favoriteSeat = "A12",
            peakTime = "14:00-17:00",
            isLoading = false
        )
    }
}
