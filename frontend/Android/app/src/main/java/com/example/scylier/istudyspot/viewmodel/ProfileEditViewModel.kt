package com.example.scylier.istudyspot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.repository.MainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileEditUiState(
    val isLoading: Boolean = false,
    val nickname: String = "",
    val phone: String = "",
    val email: String = "",
    val error: String? = null,
    val success: String? = null
)

class ProfileEditViewModel(private val repository: MainRepository = MainRepository()) : ViewModel() {
    private val _state = MutableStateFlow(ProfileEditUiState())
    val state: StateFlow<ProfileEditUiState> = _state

    fun loadCurrentInfo(nickname: String, phone: String, email: String) {
        _state.value = _state.value.copy(nickname = nickname, phone = phone, email = email)
    }

    fun updateUserInfo(nickname: String, phone: String, email: String) {
        _state.value = _state.value.copy(isLoading = true, error = null, success = null)
        viewModelScope.launch {
            when (val response = repository.updateUserInfo(nickname, null, phone, email)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(isLoading = false, success = "资料更新成功")
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = response.message)
                }
            }
        }
    }

    fun changePassword(oldPassword: String, newPassword: String) {
        _state.value = _state.value.copy(isLoading = true, error = null, success = null)
        viewModelScope.launch {
            when (val response = repository.changePassword(oldPassword, newPassword)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(isLoading = false, success = "密码修改成功")
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = response.message)
                }
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(error = null, success = null)
    }
}
