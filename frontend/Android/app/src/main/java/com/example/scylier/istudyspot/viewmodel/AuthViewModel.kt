package com.example.scylier.istudyspot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scylier.istudyspot.infra.network.ApiClient
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.repository.MainRepository
import com.example.scylier.istudyspot.utils.ConfigManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class AuthViewModel : ViewModel() {
    private val repository = MainRepository()

    private val _loginState = MutableStateFlow(AuthUiState())
    val loginState: StateFlow<AuthUiState> = _loginState

    private val _registerState = MutableStateFlow(AuthUiState())
    val registerState: StateFlow<AuthUiState> = _registerState

    fun login(username: String, password: String, configManager: ConfigManager) {
        if (username.isEmpty() || password.isEmpty()) {
            _loginState.value = AuthUiState(error = "用户名和密码不能为空")
            return
        }
        _loginState.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            when (val response = repository.login(username, password)) {
                is ApiResponse.Success -> {
                    val loginData = response.data
                    if (loginData != null) {
                        configManager.saveToken(loginData.token)
                        configManager.saveUserId(loginData.user.id.toString())
                        configManager.saveUsername(loginData.user.username)
                        configManager.saveNickname(loginData.user.nickname ?: "")
                        ApiClient.currentToken = loginData.token
                    }
                    _loginState.value = AuthUiState(isSuccess = true)
                }
                is ApiResponse.Error -> {
                    _loginState.value = AuthUiState(error = response.message)
                }
            }
        }
    }

    fun register(username: String, password: String, confirmPassword: String, nickname: String, configManager: ConfigManager) {
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            _registerState.value = AuthUiState(error = "请填写所有必填字段")
            return
        }
        if (password != confirmPassword) {
            _registerState.value = AuthUiState(error = "两次密码输入不一致")
            return
        }
        _registerState.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            when (val response = repository.register(username, password, nickname)) {
                is ApiResponse.Success -> {
                    val registerData = response.data
                    val token = registerData?.token
                    val user = registerData?.user
                    if (token != null && user != null) {
                        configManager.saveToken(token)
                        configManager.saveUserId(user.id.toString())
                        configManager.saveUsername(user.username)
                        configManager.saveNickname(user.nickname ?: "")
                        ApiClient.currentToken = token
                    }
                    _registerState.value = AuthUiState(isSuccess = true)
                }
                is ApiResponse.Error -> {
                    _registerState.value = AuthUiState(error = response.message)
                }
            }
        }
    }

    fun resetLoginState() { _loginState.value = AuthUiState() }
    fun resetRegisterState() { _registerState.value = AuthUiState() }
}
