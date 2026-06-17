package com.example.scylier.istudyspot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.studyroom.StudyRoomGuideDetail
import com.example.scylier.istudyspot.models.studyroom.StudyRoomGuideSummary
import com.example.scylier.istudyspot.repository.MainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class Facility(
    val name: String,
    val description: String
)

data class GuideUiState(
    val guides: List<StudyRoomGuideSummary> = emptyList(),
    val selectedGuide: StudyRoomGuideDetail? = null,
    val isLoadingList: Boolean = true,
    val isLoadingDetail: Boolean = false,
    val errorMessage: String? = null
)

class GuideViewModel(private val repository: MainRepository = MainRepository()) : ViewModel() {

    private val _state = MutableStateFlow(GuideUiState())
    val state: StateFlow<GuideUiState> = _state

    fun loadGuideList() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingList = true, errorMessage = null) }
            when (val response = repository.getStudyRoomGuides()) {
                is ApiResponse.Success -> {
                    val guides = response.data ?: emptyList()
                    _state.update {
                        it.copy(
                            guides = guides,
                            isLoadingList = false,
                            errorMessage = if (guides.isEmpty()) "暂无可导览场馆" else null
                        )
                    }
                }
                is ApiResponse.Error -> {
                    _state.update {
                        it.copy(
                            isLoadingList = false,
                            errorMessage = response.message
                        )
                    }
                }
            }
        }
    }

    fun loadGuideDetail(studyRoomId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingDetail = true, errorMessage = null) }
            when (val response = repository.getStudyRoomGuideDetail(studyRoomId)) {
                is ApiResponse.Success -> {
                    _state.update {
                        it.copy(
                            selectedGuide = response.data,
                            isLoadingDetail = false
                        )
                    }
                }
                is ApiResponse.Error -> {
                    _state.update {
                        it.copy(
                            selectedGuide = null,
                            isLoadingDetail = false,
                            errorMessage = response.message
                        )
                    }
                }
            }
        }
    }

    fun clearSelectedGuide() {
        _state.update { it.copy(selectedGuide = null, isLoadingDetail = false, errorMessage = null) }
    }
}
