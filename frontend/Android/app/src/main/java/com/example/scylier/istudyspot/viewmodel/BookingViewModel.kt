package com.example.scylier.istudyspot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.repository.MainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class BookingUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val orderId: String? = null,
    val error: String? = null
)

class BookingViewModel : ViewModel() {
    private val repository = MainRepository()

    private val _state = MutableStateFlow(BookingUiState())
    val state: StateFlow<BookingUiState> = _state

    fun createOrder(studyRoomId: String, seatId: String, startTime: String, endTime: String, bookingType: String) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (val response = repository.createOrder(studyRoomId, seatId, startTime, endTime, bookingType)) {
                is ApiResponse.Success -> {
                    _state.value = BookingUiState(
                        isSuccess = true,
                        orderId = response.data.id
                    )
                }
                is ApiResponse.Error -> {
                    _state.value = BookingUiState(error = response.message)
                }
            }
        }
    }

    fun resetState() { _state.value = BookingUiState() }
}
