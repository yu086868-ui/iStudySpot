package com.example.scylier.istudyspot.repository

import android.content.Context
import com.example.scylier.istudyspot.models.todo.Todo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalTodoStore(
    context: Context,
    private val gson: Gson = Gson()
) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "local_todo_store",
        Context.MODE_PRIVATE
    )
    private val listType = object : TypeToken<List<Todo>>() {}.type
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun loadTodos(userId: Long): List<Todo> {
        return loadAll()
            .filter { it.userId == userId }
            .sortedWith(todoComparator)
    }

    fun createTodo(
        userId: Long,
        title: String,
        priority: Int,
        dueTime: String?,
        orderId: Long?
    ): Todo {
        val now = LocalDateTime.now().format(formatter)
        val todo = Todo(
            id = nextId(),
            userId = userId,
            title = title.trim(),
            priority = priority,
            status = "pending",
            dueTime = dueTime,
            orderId = orderId,
            createdAt = now,
            updatedAt = now
        )
        saveAll(loadAll() + todo)
        return todo
    }

    fun updateTodo(
        userId: Long,
        todoId: Long,
        title: String,
        priority: Int,
        dueTime: String?,
        orderId: Long?
    ): Todo {
        var updated: Todo? = null
        val next = loadAll().map { todo ->
            if (todo.id == todoId && todo.userId == userId) {
                todo.copy(
                    title = title.trim(),
                    priority = priority,
                    dueTime = dueTime,
                    orderId = orderId,
                    updatedAt = LocalDateTime.now().format(formatter)
                ).also { updated = it }
            } else {
                todo
            }
        }
        saveAll(next)
        return requireNotNull(updated) { "Todo not found" }
    }

    fun toggleTodo(userId: Long, todoId: Long): Todo {
        var updated: Todo? = null
        val next = loadAll().map { todo ->
            if (todo.id == todoId && todo.userId == userId) {
                val completed = todo.status != "completed"
                todo.copy(
                    status = if (completed) "completed" else "pending",
                    completedAt = if (completed) LocalDateTime.now().format(formatter) else null,
                    updatedAt = LocalDateTime.now().format(formatter)
                ).also { updated = it }
            } else {
                todo
            }
        }
        saveAll(next)
        return requireNotNull(updated) { "Todo not found" }
    }

    fun deleteTodo(userId: Long, todoId: Long) {
        saveAll(loadAll().filterNot { it.id == todoId && it.userId == userId })
    }

    private fun loadAll(): List<Todo> {
        val raw = preferences.getString(KEY_TODOS, null) ?: return emptyList()
        return runCatching {
            gson.fromJson<List<Todo>>(raw, listType)
        }.getOrNull().orEmpty()
    }

    private fun saveAll(todos: List<Todo>) {
        preferences.edit()
            .putString(KEY_TODOS, gson.toJson(todos))
            .apply()
    }

    private fun nextId(): Long {
        val currentMax = loadAll().maxOfOrNull { it.id } ?: 0L
        return maxOf(System.currentTimeMillis(), currentMax + 1)
    }

    private companion object {
        const val KEY_TODOS = "todos"

        val todoComparator = compareByDescending<Todo> { it.status == "pending" }
            .thenBy { it.priority }
            .thenByDescending { it.createdAt ?: "" }
    }
}
