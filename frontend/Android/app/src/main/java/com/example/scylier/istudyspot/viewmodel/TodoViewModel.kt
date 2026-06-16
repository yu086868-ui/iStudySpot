package com.example.scylier.istudyspot.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.scylier.istudyspot.models.todo.Todo
import com.example.scylier.istudyspot.repository.LocalTodoStore
import com.example.scylier.istudyspot.utils.ConfigManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class TodoUiState(
    val todos: List<Todo> = emptyList(),
    val pendingTodos: List<Todo> = emptyList(),
    val completedTodos: List<Todo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isActionLoading: Boolean = false,
    val successMessage: String? = null
)

class TodoViewModel(application: Application) : AndroidViewModel(application) {
    private val todoStore = LocalTodoStore(application.applicationContext)
    private val configManager = ConfigManager.getInstance(application.applicationContext)

    private val _state = MutableStateFlow(TodoUiState())
    val state: StateFlow<TodoUiState> = _state

    fun loadTodos() {
        val todos = todoStore.loadTodos(currentUserId())
        _state.value = _state.value.copy(
            todos = todos,
            pendingTodos = todos.filter { it.status == "pending" },
            completedTodos = todos.filter { it.status == "completed" },
            isLoading = false,
            error = null
        )
    }

    fun createTodo(title: String, priority: Int = 2, dueTime: String? = null, orderId: Long? = null) {
        if (title.isBlank()) {
            _state.value = _state.value.copy(error = "标题不能为空")
            return
        }
        if (title.length > 100) {
            _state.value = _state.value.copy(error = "标题不能超过100个字符")
            return
        }
        todoStore.createTodo(currentUserId(), title, priority, dueTime, orderId)
        loadTodos()
        _state.value = _state.value.copy(successMessage = "创建成功")
    }

    fun toggleTodo(todoId: Long) {
        runCatching {
            todoStore.toggleTodo(currentUserId(), todoId)
        }.onSuccess {
            loadTodos()
        }.onFailure {
            _state.value = _state.value.copy(error = "待办不存在")
        }
    }

    fun updateTodo(todoId: Long, title: String, priority: Int, dueTime: String? = null, orderId: Long? = null) {
        if (title.isBlank()) {
            _state.value = _state.value.copy(error = "标题不能为空")
            return
        }
        if (title.length > 100) {
            _state.value = _state.value.copy(error = "标题不能超过100个字符")
            return
        }
        runCatching {
            todoStore.updateTodo(currentUserId(), todoId, title, priority, dueTime, orderId)
        }.onSuccess {
            loadTodos()
            _state.value = _state.value.copy(successMessage = "保存成功")
        }.onFailure {
            _state.value = _state.value.copy(error = "待办不存在")
        }
    }

    fun deleteTodo(todoId: Long) {
        todoStore.deleteTodo(currentUserId(), todoId)
        loadTodos()
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _state.value = _state.value.copy(successMessage = null)
    }

    private fun currentUserId(): Long {
        return configManager.getUserId()?.toLongOrNull() ?: 0L
    }
}
