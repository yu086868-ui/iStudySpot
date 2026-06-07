package com.example.scylier.istudyspot.models.todo

data class Todo(
    val id: Long,
    val userId: Long,
    val title: String,
    val priority: Int = 2,
    val status: String = "pending",
    val dueTime: String? = null,
    val orderId: Long? = null,
    val completedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class CreateTodoRequest(
    val title: String,
    val priority: Int = 2,
    val dueTime: String? = null,
    val orderId: Long? = null
)

data class UpdateTodoRequest(
    val title: String? = null,
    val priority: Int? = null,
    val dueTime: String? = null,
    val orderId: Long? = null
)
