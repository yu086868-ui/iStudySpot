package com.example.scylier.istudyspot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.repository.MainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ViolationItem(
    val id: Long,
    val type: String,
    val description: String,
    val relatedOrderId: Long?,
    val status: String,
    val createdAt: String?,
    val appealReason: String?,
    val appealResult: String?
) {
    val statusLabel: String get() = when (status) {
        "active" -> "生效中"
        "appealing" -> "申诉中"
        "appeal_approved" -> "申诉通过"
        "appeal_rejected" -> "申诉驳回"
        "expired" -> "已过期"
        else -> status
    }
    val typeLabel: String get() = when (type) {
        "no_show" -> "未签到"
        "late_checkin" -> "迟到签到"
        "overstay" -> "超时占用"
        "unauthorized_transfer" -> "违规转让"
        else -> "其他违规"
    }
}

data class ViolationUiState(
    val violations: List<ViolationItem> = emptyList(),
    val totalCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
    val appealSuccess: String? = null
)

class ViolationViewModel(private val repository: MainRepository = MainRepository()) : ViewModel() {
    private val _state = MutableStateFlow(ViolationUiState())
    val state: StateFlow<ViolationUiState> = _state

    fun loadViolations() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                when (val response = repository.getViolations()) {
                    is ApiResponse.Success -> {
                        val data = response.data
                        if (data == null) {
                            _state.value = _state.value.copy(isLoading = false, error = "数据为空")
                            return@launch
                        }
                        val list = (data["list"] as? List<Map<String, Any?>>)?.map { map ->
                            ViolationItem(
                                id = (map["id"] as? Number)?.toLong() ?: 0L,
                                type = map["type"] as? String ?: "",
                                description = map["description"] as? String ?: "",
                                relatedOrderId = (map["relatedOrderId"] as? Number)?.toLong(),
                                status = map["status"] as? String ?: "active",
                                createdAt = map["createdAt"] as? String,
                                appealReason = map["appealReason"] as? String,
                                appealResult = map["appealResult"] as? String
                            )
                        } ?: emptyList()
                        val total = (data["total"] as? Number)?.toInt() ?: 0
                        _state.value = ViolationUiState(
                            violations = list,
                            totalCount = total,
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
                    error = "加载违规记录失败: ${e.message}"
                )
            }
        }
    }

    fun submitAppeal(violationId: Long, reason: String) {
        viewModelScope.launch {
            try {
                when (val response = repository.appealViolation(violationId, reason)) {
                    is ApiResponse.Success -> {
                        _state.value = _state.value.copy(appealSuccess = "申诉已提交")
                        loadViolations()
                    }
                    is ApiResponse.Error -> {
                        _state.value = _state.value.copy(error = response.message)
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "申诉失败: ${e.message}")
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(error = null, appealSuccess = null)
    }
}
