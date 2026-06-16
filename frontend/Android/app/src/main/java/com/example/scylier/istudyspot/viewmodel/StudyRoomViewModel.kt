package com.example.scylier.istudyspot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.studyroom.SeatInfo
import com.example.scylier.istudyspot.models.studyroom.SeatLayoutData
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
    val layout: SeatLayoutData? = null,
    val layoutMode: String = "grid",
    val isLoading: Boolean = true,
    val error: String? = null
)

data class StudyRoomDetailUiState(
    val totalSeats: Int = 30,
    val description: String = "",
    val facilities: List<String> = emptyList(),
    val isLoading: Boolean = true
)

class StudyRoomViewModel(
    private val repository: MainRepository = MainRepository()
) : ViewModel() {

    private val _studyRoomState = MutableStateFlow(StudyRoomUiState())
    val studyRoomState: StateFlow<StudyRoomUiState> = _studyRoomState

    private val _seatMapState = MutableStateFlow(SeatMapUiState())
    val seatMapState: StateFlow<SeatMapUiState> = _seatMapState

    private val _detailState = MutableStateFlow(StudyRoomDetailUiState())
    val detailState: StateFlow<StudyRoomDetailUiState> = _detailState

    fun loadStudyRooms(keyword: String? = null) {
        _studyRoomState.value = _studyRoomState.value.copy(isLoading = true)
        viewModelScope.launch {
            when (val response = repository.getStudyRooms(keyword = keyword)) {
                is ApiResponse.Success -> {
                    _studyRoomState.value = StudyRoomUiState(
                        studyRooms = response.data?.list ?: emptyList(),
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

    fun loadSeats(studyRoomId: Long) {
        _seatMapState.value = _seatMapState.value.copy(isLoading = true)
        viewModelScope.launch {
            when (val response = repository.getStudyRoomSeatLayout(studyRoomId)) {
                is ApiResponse.Success -> {
                    val layout = response.data
                    if (layout != null) {
                        _seatMapState.value = SeatMapUiState(
                            seats = layout.seats,
                            rows = layout.rows,
                            cols = layout.cols,
                            layout = layout,
                            layoutMode = layout.layoutMode,
                            isLoading = false
                        )
                    } else {
                        loadSeatFallback(studyRoomId, "座位布局为空，已回退普通视图")
                    }
                }
                is ApiResponse.Error -> {
                    loadSeatFallback(studyRoomId, response.message)
                }
            }
        }
    }

    private suspend fun loadSeatFallback(studyRoomId: Long, fallbackReason: String? = null) {
        when (val response = repository.getStudyRoomSeats(studyRoomId)) {
            is ApiResponse.Success -> {
                val seats = response.data ?: emptyList()
                val maxRow = seats.maxOfOrNull { it.row } ?: 0
                val maxCol = seats.maxOfOrNull { it.col } ?: 0
                _seatMapState.value = SeatMapUiState(
                    seats = seats,
                    rows = maxRow,
                    cols = maxCol,
                    layout = null,
                    layoutMode = "grid",
                    isLoading = false,
                    error = null
                )
            }
            is ApiResponse.Error -> {
                _seatMapState.value = SeatMapUiState(
                    isLoading = false,
                    error = fallbackReason ?: response.message
                )
            }
        }
    }

    fun loadStudyRoomDetail(studyRoomId: Long) {
        _detailState.value = _detailState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                when (val response = repository.getStudyRoomDetail(studyRoomId)) {
                    is ApiResponse.Success -> {
                        val detail = response.data
                        _detailState.value = StudyRoomDetailUiState(
                            totalSeats = 30,
                            description = detail?.description ?: "",
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
