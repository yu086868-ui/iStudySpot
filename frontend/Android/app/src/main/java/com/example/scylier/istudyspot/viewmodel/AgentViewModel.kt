package com.example.scylier.istudyspot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.agent.AgentChatResponse
import com.example.scylier.istudyspot.models.agent.AgentMessage
import com.example.scylier.istudyspot.models.agent.AgentMessageRole
import com.example.scylier.istudyspot.models.agent.AgentToolDefinition
import com.example.scylier.istudyspot.models.agent.AgentToolExecutionResult
import com.example.scylier.istudyspot.repository.MainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class AgentUiState(
    val toolCatalog: List<AgentToolDefinition> = emptyList(),
    val messages: List<AgentMessage> = emptyList(),
    val suggestedPrompts: List<String> = defaultAgentPrompts(),
    val isCatalogLoading: Boolean = false,
    val isExecuting: Boolean = false,
    val catalogError: String? = null
)

class AgentViewModel(
    private val repository: MainRepository = MainRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgentUiState())
    val uiState: StateFlow<AgentUiState> = _uiState

    private var sessionId: String? = null

    fun loadCatalog(forceRefresh: Boolean = false) {
        if (_uiState.value.toolCatalog.isNotEmpty() && !forceRefresh) {
            return
        }
        _uiState.update { it.copy(isCatalogLoading = true, catalogError = null) }
        viewModelScope.launch {
            when (val response = repository.getAgentToolCatalog()) {
                is ApiResponse.Success -> {
                    _uiState.update {
                        it.copy(
                            isCatalogLoading = false,
                            toolCatalog = response.data ?: emptyList(),
                            catalogError = null
                        )
                    }
                }

                is ApiResponse.Error -> {
                    _uiState.update {
                        it.copy(
                            isCatalogLoading = false,
                            catalogError = readableError(response.code, response.message)
                        )
                    }
                }
            }
        }
    }

    fun submitPrompt(prompt: String) {
        val text = prompt.trim()
        if (text.isBlank()) return
        appendMessage(AgentMessageRole.USER, text)

        _uiState.update { it.copy(isExecuting = true) }
        viewModelScope.launch {
            when (val response = repository.sendAgentMessage(text, sessionId)) {
                is ApiResponse.Success -> {
                    handleAgentChatSuccess(response.data)
                }

                is ApiResponse.Error -> {
                    appendMessage(
                        role = AgentMessageRole.ASSISTANT,
                        content = readableError(response.code, response.message),
                        isError = true
                    )
                }
            }
            _uiState.update { state ->
                state.copy(
                    isExecuting = false,
                    suggestedPrompts = state.suggestedPrompts.ifEmpty { defaultAgentPrompts() }
                )
            }
        }
    }

    fun executeTool(
        tool: String,
        arguments: Map<String, Any?> = emptyMap()
    ) {
        _uiState.update { it.copy(isExecuting = true) }
        viewModelScope.launch {
            when (val response = repository.executeAgentTool(tool, arguments)) {
                is ApiResponse.Success -> {
                    val result = response.data
                    if (result != null) {
                        appendMessage(
                            role = AgentMessageRole.ASSISTANT,
                            content = result.summary ?: buildReadableSummary(result),
                            result = result
                        )
                        _uiState.update {
                            it.copy(suggestedPrompts = buildToolSuggestions(result))
                        }
                    } else {
                        appendMessage(
                            role = AgentMessageRole.ASSISTANT,
                            content = "No displayable data was returned.",
                            isError = true
                        )
                    }
                }

                is ApiResponse.Error -> {
                    appendMessage(
                        role = AgentMessageRole.ASSISTANT,
                        content = readableError(response.code, response.message),
                        isError = true
                    )
                }
            }
            _uiState.update { it.copy(isExecuting = false) }
        }
    }

    fun triggerToolShortcut(tool: AgentToolDefinition) {
        when (tool.name) {
            "list_study_rooms" -> executeTool(tool.name)
            "get_reservation_rules" -> executeTool(tool.name)
            "get_my_reservations" -> executeTool(tool.name)
            "get_study_room_detail" -> submitPrompt("Show available study rooms")
            "list_room_seats" -> {
                val roomId = lastKnownStudyRoomId()
                if (roomId != null) {
                    submitPrompt("Show seats for room $roomId")
                } else {
                    submitPrompt("Show available study rooms")
                }
            }

            else -> executeTool(tool.name)
        }
    }

    fun displayToolName(toolName: String): String = when (toolName) {
        "list_study_rooms" -> "Study rooms"
        "get_study_room_detail" -> "Study room detail"
        "list_room_seats" -> "Seats"
        "get_my_reservations" -> "My reservations"
        "get_reservation_rules" -> "Reservation rules"
        else -> toolName
    }

    private fun handleAgentChatSuccess(payload: AgentChatResponse?) {
        if (payload == null) {
            appendMessage(
                role = AgentMessageRole.ASSISTANT,
                content = "No displayable data was returned.",
                isError = true
            )
            return
        }

        sessionId = payload.sessionId ?: sessionId
        appendMessage(
            role = AgentMessageRole.ASSISTANT,
            content = payload.reply,
            result = payload.toolResult
        )
        _uiState.update {
            it.copy(
                suggestedPrompts = payload.suggestedPrompts.takeIf { prompts -> prompts.isNotEmpty() }
                    ?: buildToolSuggestions(payload.toolResult)
            )
        }
    }

    private fun appendMessage(
        role: AgentMessageRole,
        content: String,
        result: AgentToolExecutionResult? = null,
        isError: Boolean = false
    ) {
        val message = AgentMessage(
            id = UUID.randomUUID().toString(),
            role = role,
            content = content,
            result = result,
            isError = isError
        )
        _uiState.update { state ->
            state.copy(messages = state.messages + message)
        }
    }

    private fun buildReadableSummary(result: AgentToolExecutionResult): String {
        return result.summary ?: "Tool execution completed."
    }

    private fun buildToolSuggestions(result: AgentToolExecutionResult?): List<String> {
        val roomId = suggestedRoomId(result)
        val roomSeatPrompt = if (roomId != null) {
            "Show seats for room $roomId"
        } else {
            "Show seats for room 1"
        }

        return when (result?.tool) {
            "list_study_rooms" -> listOf(roomSeatPrompt, "Show reservation rules", "Show my reservations")
            "get_study_room_detail" -> listOf(roomSeatPrompt, "Show my reservations", "Show reservation rules")
            "list_room_seats" -> listOf("Show my reservations", "Show reservation rules", "Show available study rooms")
            "get_my_reservations" -> listOf("Show reservation rules", "Show available study rooms", roomSeatPrompt)
            "get_reservation_rules" -> listOf("Show my reservations", "Show available study rooms", roomSeatPrompt)
            else -> defaultAgentPrompts()
        }
    }

    private fun suggestedRoomId(result: AgentToolExecutionResult?): Long? {
        return roomIdFromResult(result) ?: lastKnownStudyRoomId()
    }

    private fun roomIdFromResult(result: AgentToolExecutionResult?): Long? {
        if (result == null) return null
        return when (result.tool) {
            "list_study_rooms" -> {
                val items = result.data["items"] as? List<*>
                val first = items?.firstOrNull() as? Map<*, *>
                longValue(first?.get("id"))
            }

            "get_study_room_detail" -> {
                val room = result.data["studyRoom"] as? Map<*, *>
                longValue(room?.get("id"))
            }

            "list_room_seats" -> longValue(result.data["studyRoomId"])
            else -> null
        }
    }

    private fun lastKnownStudyRoomId(): Long? {
        return uiState.value.messages.asReversed()
            .mapNotNull { roomIdFromResult(it.result) }
            .firstOrNull()
    }

    private fun longValue(value: Any?): Long? {
        return when (value) {
            is Number -> value.toLong()
            is String -> value.toLongOrNull()
            else -> null
        }
    }

    private fun readableError(code: Int, message: String): String {
        return when {
            code == 401 -> "Please log in before using AI Agent."
            message.contains("EMPTY_MESSAGE", ignoreCase = true) -> "Please enter a question."
            message.contains("MISSING_STUDYROOMID", ignoreCase = true) -> {
                "This query needs a study room id. Try asking: Show available study rooms."
            }

            message.contains("UNSUPPORTED_TOOL", ignoreCase = true) -> "This agent tool is not available in the app yet."
            message.isBlank() -> "The query could not be completed. Please try again later."
            else -> message
        }
    }
}

private fun defaultAgentPrompts(): List<String> = listOf(
    "Show available study rooms",
    "Show reservation rules",
    "Show my reservations",
    "Show seats for room 1"
)
