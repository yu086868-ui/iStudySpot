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
    val isLoading: Boolean = true
)

class StudyRecordViewModel : ViewModel() {
    private val repository = MainRepository()

    private val _state = MutableStateFlow(StudyRecordUiState())
    val state: StateFlow<StudyRecordUiState> = _state

    private fun mockState() = StudyRecordUiState(
        weekStudyHours = 24,
        monthStudyHours = 96,
        totalStudyHours = 328,
        totalBookings = 45,
        streakDays = 7,
        avgStudyDuration = 3.5,
        favoriteSeat = "A区-12号",
        peakTime = "14:00-17:00",
        isLoading = false
    )

    fun loadStudyRecords() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                var weekHours = 0
                var monthHours = 0
                var totalHours = 0
                var totalBookings = 0
                var streak = 0
                var avgDuration = 0.0
                var favSeat = ""
                var peak = ""

                when (val response = repository.getCheckinRecords()) {
                    is ApiResponse.Success -> {
                        val data = response.data
                        @Suppress("UNCHECKED_CAST")
                        val records = (data["records"] as? List<Map<String, Any?>>) ?: emptyList()
                        totalHours = (data["totalHours"] as? Number)?.toInt() ?: records.size * 3
                        streak = (data["streak"] as? Number)?.toInt() ?: 0
                        avgDuration = (data["avgDuration"] as? Number)?.toDouble() ?: 3.5
                        favSeat = (data["favoriteSeat"] as? String) ?: ""
                        peak = (data["peakTime"] as? String) ?: ""
                        weekHours = (data["weekHours"] as? Number)?.toInt() ?: 24
                        monthHours = (data["monthHours"] as? Number)?.toInt() ?: 96
                    }
                    is ApiResponse.Error -> {}
                }

                when (val ordersResponse = repository.getUserOrders()) {
                    is ApiResponse.Success -> {
                        totalBookings = ordersResponse.data.list.size
                    }
                    is ApiResponse.Error -> {}
                }

                if (totalHours > 0 || totalBookings > 0) {
                    _state.value = StudyRecordUiState(
                        weekStudyHours = weekHours,
                        monthStudyHours = monthHours,
                        totalStudyHours = totalHours,
                        totalBookings = totalBookings,
                        streakDays = streak,
                        avgStudyDuration = avgDuration,
                        favoriteSeat = favSeat,
                        peakTime = peak,
                        isLoading = false
                    )
                } else {
                    _state.value = mockState()
                }
            } catch (e: Exception) {
                _state.value = mockState()
            }
        }
    }
}
