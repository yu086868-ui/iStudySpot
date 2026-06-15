package com.example.scylier.istudyspot.models.ai

import androidx.compose.ui.graphics.Color
import com.google.gson.annotations.SerializedName

data class AiCharacter(
    val id: String,
    val name: String,
    val persona: String,
    @SerializedName("speaking_style")
    val speakingStyle: String,
    val avatarColor: Color = Color(0xFF6366F1)
)

data class AiChatRequest(
    val message: String,
    val session_id: String? = null,
    val character_id: String? = null
)

data class AiChatResponse(
    val reply: String,
    val session_id: String? = null
) {
    val sessionId: String?
        get() = session_id
}

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
