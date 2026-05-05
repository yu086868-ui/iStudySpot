package com.example.scylier.istudyspot.state

import com.example.scylier.istudyspot.models.studyroom.SeatInfo

data class SeatMapState(
    val seats: List<SeatInfo> = emptyList(),
    val selectedSeat: SeatInfo? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
