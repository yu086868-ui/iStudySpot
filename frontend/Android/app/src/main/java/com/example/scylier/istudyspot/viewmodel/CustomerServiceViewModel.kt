package com.example.scylier.istudyspot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.customerservice.CustomerServiceMessage
import com.example.scylier.istudyspot.repository.MainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CustomerServiceViewModel(private val repository: MainRepository = MainRepository()) : ViewModel() {
    private val sessionId = java.util.UUID.randomUUID().toString()

    private val _messages = MutableStateFlow<List<CustomerServiceMessage>>(emptyList())
    val messages: StateFlow<List<CustomerServiceMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _welcomeMessage = MutableStateFlow("")
    val welcomeMessage: StateFlow<String> = _welcomeMessage

    private val _recommendedQuestions = MutableStateFlow<List<String>>(emptyList())
    val recommendedQuestions: StateFlow<List<String>> = _recommendedQuestions

    fun loadWelcome() {
        viewModelScope.launch {
            when (val response = repository.getCustomerServiceWelcome()) {
                is ApiResponse.Success -> {
                    val data = response.data ?: emptyMap()
                    _welcomeMessage.value = data["welcomeMessage"] as? String ?: ""
                    @Suppress("UNCHECKED_CAST")
                    _recommendedQuestions.value = data["recommendedQuestions"] as? List<String> ?: emptyList()
                }
                is ApiResponse.Error -> {}
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = CustomerServiceMessage(
            id = System.currentTimeMillis().toString(),
            content = text,
            isFromUser = true,
            timestamp = System.currentTimeMillis()
        )
        _messages.value = _messages.value + userMsg
        _isLoading.value = true

        viewModelScope.launch {
            when (val response = repository.customerServiceChat(sessionId, text)) {
                is ApiResponse.Success -> {
                    val reply = response.data?.get("response") as? String ?: "抱歉，我暂时无法回答这个问题"
                    val botMsg = CustomerServiceMessage(
                        id = System.currentTimeMillis().toString(),
                        content = reply,
                        isFromUser = false,
                        timestamp = System.currentTimeMillis()
                    )
                    _messages.value = _messages.value + botMsg
                }
                is ApiResponse.Error -> {
                    val errorMsg = CustomerServiceMessage(
                        id = System.currentTimeMillis().toString(),
                        content = "网络异常，请稍后重试",
                        isFromUser = false,
                        timestamp = System.currentTimeMillis()
                    )
                    _messages.value = _messages.value + errorMsg
                }
            }
            _isLoading.value = false
        }
    }
}
