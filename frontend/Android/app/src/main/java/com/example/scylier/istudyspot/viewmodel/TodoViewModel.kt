package com.example.scylier.istudyspot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scylier.istudyspot.infra.network.ApiManager
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.todo.Todo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TodoUiState(
    val todos: List<Todo> = emptyList(),
    val pendingTodos: List<Todo> = emptyList(),
    val completedTodos: List<Todo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isActionLoading: Boolean = false
)

class TodoViewModel : ViewModel() {
    private val apiManager = ApiManager()

    private val _state = MutableStateFlow(TodoUiState())
    val state: StateFlow<TodoUiState> = _state

    fun loadTodos() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (val response = apiManager.getTodos()) {
                is ApiResponse.Success -> {
                    val todos = response.data ?: emptyList()
                    _state.value = _state.value.copy(
                        todos = todos,
                        pendingTodos = todos.filter { it.status == "pending" },
                        completedTodos = todos.filter { it.status == "completed" },
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
        }
    }

    fun createTodo(title: String, priority: Int = 2, dueTime: String? = null, orderId: Long? = null) {
        _state.value = _state.value.copy(isActionLoading = true)
        viewModelScope.launch {
            when (val response = apiManager.createTodo(title, priority, dueTime, orderId)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(isActionLoading = false)
                    loadTodos()
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isActionLoading = false,
                        error = response.message
                    )
                }
            }
        }
    }

    fun toggleTodo(todoId: Long) {
        viewModelScope.launch {
            when (val response = apiManager.toggleTodo(todoId)) {
                is ApiResponse.Success -> loadTodos()
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(error = response.message)
                }
            }
        }
    }

    fun updateTodo(todoId: Long, title: String, priority: Int, dueTime: String? = null, orderId: Long? = null) {
        _state.value = _state.value.copy(isActionLoading = true)
        viewModelScope.launch {
            when (val response = apiManager.updateTodo(todoId, title, priority, dueTime, orderId)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(isActionLoading = false)
                    loadTodos()
                }
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isActionLoading = false,
                        error = response.message
                    )
                }
            }
        }
    }

    fun deleteTodo(todoId: Long) {
        viewModelScope.launch {
            when (val response = apiManager.deleteTodo(todoId)) {
                is ApiResponse.Success -> loadTodos()
                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(error = response.message)
                }
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
