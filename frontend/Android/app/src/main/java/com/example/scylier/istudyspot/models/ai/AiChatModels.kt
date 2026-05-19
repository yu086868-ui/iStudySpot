package com.example.scylier.istudyspot.models.ai

import androidx.compose.ui.graphics.Color

data class AiCharacter(
    val id: String,
    val name: String,
    val persona: String,
    val speakingStyle: String,
    val avatarColor: Color
)

data class AiChatRequest(
    val message: String,
    val session_id: String? = null,
    val character_id: String? = null
)

data class AiChatResponse(
    val reply: String,
    val sessionId: String
)

enum class MessageType {
    USER,
    AI
}

data class AiMessage(
    val id: String,
    val content: String,
    val type: MessageType,
    val timestamp: Long = System.currentTimeMillis()
)
