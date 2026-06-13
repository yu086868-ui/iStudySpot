package com.example.scylier.istudyspot.models.agent

import com.google.gson.annotations.SerializedName

data class AgentToolDefinition(
    val name: String,
    val title: String? = null,
    val description: String? = null,
    val requiresAuth: Boolean = true,
    val tags: List<String> = emptyList(),
    val inputSchema: Map<String, Any?> = emptyMap()
)

data class AgentToolExecuteRequest(
    val tool: String,
    val arguments: Map<String, @JvmSuppressWildcards Any?> = emptyMap()
)

data class AgentUiAction(
    val type: String? = null,
    val route: String? = null,
    val params: Map<String, Any?> = emptyMap()
)

data class AgentToolExecutionResult(
    val schemaVersion: String = "1.0",
    val referenceScope: String? = null,
    val tool: String,
    val summary: String? = null,
    val data: Map<String, Any?> = emptyMap(),
    val uiAction: AgentUiAction? = null,
    val references: Map<String, Any?> = emptyMap()
)

data class AgentChatRequest(
    val message: String,
    @SerializedName("sessionId")
    val sessionId: String? = null
)

data class AgentReplyBlock(
    val type: String,
    val text: String? = null,
    val items: List<String> = emptyList()
)

data class AgentChatResponse(
    val schemaVersion: String = "1.0",
    @SerializedName("sessionId")
    val sessionId: String? = null,
    val reply: String,
    val replyText: String? = null,
    val replyFormat: String? = null,
    val blocks: List<AgentReplyBlock>? = emptyList(),
    val toolResult: AgentToolExecutionResult? = null,
    val toolResults: List<AgentToolExecutionResult>? = emptyList(),
    val uiAction: AgentUiAction? = null,
    val suggestedPrompts: List<String> = emptyList()
)

enum class AgentMessageRole {
    USER,
    ASSISTANT
}

data class AgentMessage(
    val id: String,
    val role: AgentMessageRole,
    val content: String,
    val blocks: List<AgentReplyBlock> = emptyList(),
    val result: AgentToolExecutionResult? = null,
    val results: List<AgentToolExecutionResult> = emptyList(),
    val uiAction: AgentUiAction? = null,
    val isError: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
