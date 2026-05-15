package com.example.scylier.istudyspot.models.ai

/**
 * AI咨询请求
 */
data class AiChatRequest(
    val message: String,
    val session_id: String? = null,
    val character_id: String? = null
)

/**
 * AI咨询响应
 */
data class AiChatResponse(
    val reply: String,
    val sessionId: String
)

/**
 * AI咨询消息类型
 */
enum class MessageType {
    USER,
    AI
}

/**
 * AI咨询消息
 */
data class AiMessage(
    val id: String,
    val content: String,
    val type: MessageType,
    val timestamp: Long = System.currentTimeMillis()
)
