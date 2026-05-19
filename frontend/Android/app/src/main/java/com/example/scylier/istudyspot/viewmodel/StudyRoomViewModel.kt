package com.example.scylier.istudyspot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.studyroom.SeatInfo
import com.example.scylier.istudyspot.models.studyroom.StudyRoomItem
import com.example.scylier.istudyspot.repository.MainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class StudyRoomUiState(
    val studyRooms: List<StudyRoomItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class SeatMapUiState(
    val seats: List<SeatInfo> = emptyList(),
    val rows: Int = 0,
    val cols: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

data class StudyRoomDetailUiState(
    val totalSeats: Int = 30,
    val description: String = "",
    val facilities: List<String> = emptyList(),
    val isLoading: Boolean = true
)

class StudyRoomViewModel : ViewModel() {
    private val repository = MainRepository()

    private val _studyRoomState = MutableStateFlow(StudyRoomUiState())
    val studyRoomState: StateFlow<StudyRoomUiState> = _studyRoomState

    private val _seatMapState = MutableStateFlow(SeatMapUiState())
    val seatMapState: StateFlow<SeatMapUiState> = _seatMapState

    private val _detailState = MutableStateFlow(StudyRoomDetailUiState())
    val detailState: StateFlow<StudyRoomDetailUiState> = _detailState

    fun loadStudyRooms() {
        _studyRoomState.value = _studyRoomState.value.copy(isLoading = true)
        viewModelScope.launch {
            when (val response = repository.getStudyRooms()) {
                is ApiResponse.Success -> {
                    _studyRoomState.value = StudyRoomUiState(
                        studyRooms = response.data.list,
                        isLoading = false
                    )
                }
                is ApiResponse.Error -> {
                    _studyRoomState.value = StudyRoomUiState(
                        isLoading = false,
                        error = response.message
                    )
                }
            }
        }
    }

    fun loadSeats(studyRoomId: String) {
        _seatMapState.value = _seatMapState.value.copy(isLoading = true)
        viewModelScope.launch {
            when (val response = repository.getStudyRoomSeats(studyRoomId)) {
                is ApiResponse.Success -> {
                    _seatMapState.value = SeatMapUiState(
                        seats = response.data.seats,
                        rows = response.data.rows,
                        cols = response.data.cols,
                        isLoading = false
                    )
                }
                is ApiResponse.Error -> {
                    _seatMapState.value = SeatMapUiState(
                        isLoading = false,
                        error = response.message
                    )
                }
            }
        }
    }

    fun loadStudyRoomDetail(studyRoomId: String) {
        _detailState.value = _detailState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                when (val response = repository.getStudyRoomDetail(studyRoomId)) {
                    is ApiResponse.Success -> {
                        val detail = response.data
                        _detailState.value = StudyRoomDetailUiState(
                            totalSeats = 30,
                            description = detail.description ?: "",
                            facilities = emptyList(),
                            isLoading = false
                        )
                    }
                    is ApiResponse.Error -> {
                        _detailState.value = StudyRoomDetailUiState(
                            totalSeats = 30,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _detailState.value = StudyRoomDetailUiState(
                    totalSeats = 30,
                    isLoading = false
                )
            }
        }
    }
}
