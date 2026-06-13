package com.example.scylier.istudyspot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.agent.AgentChatResponse
import com.example.scylier.istudyspot.models.agent.AgentMessage
import com.example.scylier.istudyspot.models.agent.AgentMessageRole
import com.example.scylier.istudyspot.models.agent.AgentReplyBlock
import com.example.scylier.istudyspot.models.agent.AgentToolDefinition
import com.example.scylier.istudyspot.models.agent.AgentToolExecutionResult
import com.example.scylier.istudyspot.models.agent.AgentUiAction
import com.example.scylier.istudyspot.repository.AgentConversationSnapshot
import com.example.scylier.istudyspot.repository.AgentConversationStore
import com.example.scylier.istudyspot.repository.InMemoryAgentConversationStore
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
    private val repository: MainRepository = MainRepository(),
    private val conversationStore: AgentConversationStore = InMemoryAgentConversationStore()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgentUiState())
    val uiState: StateFlow<AgentUiState> = _uiState

    private var sessionId: String? = null

    init {
        restoreConversation()
    }

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
                    result = result,
                    results = listOf(result)
                )
                _uiState.update {
                    it.copy(suggestedPrompts = buildToolSuggestions(result))
                }
                        persistConversation()
                    } else {
                        appendMessage(
                            role = AgentMessageRole.ASSISTANT,
                            content = "没有可展示的数据。",
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

    fun clearConversation() {
        sessionId = null
        conversationStore.clear()
        _uiState.update {
            it.copy(
                messages = emptyList(),
                suggestedPrompts = defaultAgentPrompts(),
                isExecuting = false
            )
        }
    }

    fun triggerToolShortcut(tool: AgentToolDefinition) {
        when (tool.name) {
            "list_study_rooms" -> executeTool(tool.name)
            "get_reservation_rules" -> executeTool(tool.name)
            "get_my_reservations" -> executeTool(tool.name)
            "get_study_room_detail" -> submitPrompt("查看可用自习室")
            "list_room_seats" -> {
                val roomId = lastKnownStudyRoomId()
                if (roomId != null) {
                    submitPrompt("查看 $roomId 号自习室的座位")
                } else {
                    submitPrompt("查看可用自习室")
                }
            }

            else -> executeTool(tool.name)
        }
    }

    fun displayToolName(toolName: String): String = when (toolName) {
        "list_study_rooms" -> "自习室"
        "get_study_room_detail" -> "自习室详情"
        "list_room_seats" -> "座位"
        "get_my_reservations" -> "我的预约"
        "get_reservation_rules" -> "预约规则"
        else -> toolName
    }

    private fun handleAgentChatSuccess(payload: AgentChatResponse?) {
        if (payload == null) {
            appendMessage(
                role = AgentMessageRole.ASSISTANT,
                content = "没有可展示的数据。",
                isError = true
            )
            return
        }

        sessionId = payload.sessionId ?: sessionId
        val results = payload.toolResults.orEmpty().ifEmpty {
            listOfNotNull(payload.toolResult)
        }
        val primaryResult = payload.toolResult ?: results.firstOrNull()
        appendMessage(
            role = AgentMessageRole.ASSISTANT,
            content = payload.displayText(),
            blocks = payload.blocks.orEmpty(),
            result = primaryResult,
            results = results,
            uiAction = payload.uiAction
        )
        _uiState.update {
            it.copy(
                suggestedPrompts = payload.suggestedPrompts.takeIf { prompts -> prompts.isNotEmpty() }
                    ?: buildToolSuggestions(primaryResult)
            )
        }
        persistConversation()
    }

    private fun appendMessage(
        role: AgentMessageRole,
        content: String,
        blocks: List<AgentReplyBlock> = emptyList(),
        result: AgentToolExecutionResult? = null,
        results: List<AgentToolExecutionResult> = emptyList(),
        uiAction: AgentUiAction? = null,
        isError: Boolean = false
    ) {
        val message = AgentMessage(
            id = UUID.randomUUID().toString(),
            role = role,
            content = content,
            blocks = blocks,
            result = result,
            results = results.ifEmpty { listOfNotNull(result) },
            uiAction = uiAction,
            isError = isError
        )
        _uiState.update { state ->
            state.copy(messages = state.messages + message)
        }
        persistConversation()
    }

    private fun restoreConversation() {
        val snapshot = conversationStore.load() ?: return
        sessionId = snapshot.sessionId
        _uiState.update {
            it.copy(
                messages = snapshot.messages,
                suggestedPrompts = snapshot.suggestedPrompts.ifEmpty { defaultAgentPrompts() }
            )
        }
    }

    private fun persistConversation() {
        conversationStore.save(
            AgentConversationSnapshot(
                sessionId = sessionId,
                messages = _uiState.value.messages.takeLast(MAX_LOCAL_MESSAGES),
                suggestedPrompts = _uiState.value.suggestedPrompts
            )
        )
    }

    private fun AgentChatResponse.displayText(): String {
        return replyText?.takeIf { it.isNotBlank() } ?: stripMarkdown(reply)
    }

    private fun stripMarkdown(value: String): String {
        return value
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
            .replace(Regex("`([^`]*)`"), "$1")
            .replace(Regex("^#{1,6}\\s+", RegexOption.MULTILINE), "")
            .replace(Regex("^\\s*[-*+]\\s+", RegexOption.MULTILINE), "")
            .replace(Regex("^\\s*\\d+[.)]\\s+", RegexOption.MULTILINE), "")
            .replace(Regex("\\[(.*?)]\\((.*?)\\)"), "$1")
            .trim()
    }

    private fun buildReadableSummary(result: AgentToolExecutionResult): String {
        return result.summary ?: "工具查询已完成。"
    }

    private fun buildToolSuggestions(result: AgentToolExecutionResult?): List<String> {
        val roomId = suggestedRoomId(result)
        val roomSeatPrompt = if (roomId != null) {
            "查看 $roomId 号自习室的座位"
        } else {
            "查看 1 号自习室的座位"
        }

        return when (result?.tool) {
            "list_study_rooms" -> listOf(roomSeatPrompt, "查看预约规则", "查看我的预约")
            "get_study_room_detail" -> listOf(roomSeatPrompt, "查看我的预约", "查看预约规则")
            "list_room_seats" -> listOf("查看我的预约", "查看预约规则", "查看可用自习室")
            "get_my_reservations" -> listOf("查看预约规则", "查看可用自习室", roomSeatPrompt)
            "get_reservation_rules" -> listOf("查看我的预约", "查看可用自习室", roomSeatPrompt)
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
            .flatMap { message -> message.results.ifEmpty { listOfNotNull(message.result) } }
            .mapNotNull { result -> roomIdFromResult(result) }
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
            code == 401 -> "请先登录后再使用智能助手。"
            message.contains("EMPTY_MESSAGE", ignoreCase = true) -> "请输入你的问题。"
            message.contains("MISSING_STUDYROOMID", ignoreCase = true) -> {
                "这个查询需要自习室编号。你可以先问：查看可用自习室。"
            }

            message.contains("UNSUPPORTED_TOOL", ignoreCase = true) -> "当前应用暂不支持这个助手工具。"
            message.isBlank() -> "查询暂时无法完成，请稍后重试。"
            else -> message
        }
    }
}

private fun defaultAgentPrompts(): List<String> = listOf(
    "查看可用自习室",
    "查看预约规则",
    "查看我的预约",
    "查看 1 号自习室的座位"
)

private const val MAX_LOCAL_MESSAGES = 80
