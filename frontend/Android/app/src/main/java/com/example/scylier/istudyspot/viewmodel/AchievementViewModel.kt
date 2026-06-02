package com.example.scylier.istudyspot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.repository.MainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AchievementItem(
    val code: String,
    val name: String,
    val description: String,
    val icon: String,
    val category: String,
    val isUnlocked: Boolean,
    val unlockedAt: String? = null
)

data class AchievementUiState(
    val achievements: List<AchievementItem> = emptyList(),
    val unlockedCount: Int = 0,
    val totalCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

class AchievementViewModel(private val repository: MainRepository = MainRepository()) : ViewModel() {
    private val _state = MutableStateFlow(AchievementUiState())
    val state: StateFlow<AchievementUiState> = _state

    fun loadAchievements() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                when (val response = repository.getAchievements()) {
                    is ApiResponse.Success -> {
                        val items = response.data?.map { map ->
                            AchievementItem(
                                code = map["code"] as? String ?: "",
                                name = map["name"] as? String ?: "",
                                description = map["description"] as? String ?: "",
                                icon = map["icon"] as? String ?: "",
                                category = map["category"] as? String ?: "",
                                isUnlocked = map["isUnlocked"] as? Boolean ?: false,
                                unlockedAt = map["unlockedAt"] as? String
                            )
                        } ?: emptyList()
                        val unlocked = items.count { it.isUnlocked }
                        _state.value = AchievementUiState(
                            achievements = items,
                            unlockedCount = unlocked,
                            totalCount = items.size,
                            isLoading = false
                        )
                    }
                    is ApiResponse.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = response.message
                        )
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "加载成就失败: ${e.message}"
                )
            }
        }
    }
}
