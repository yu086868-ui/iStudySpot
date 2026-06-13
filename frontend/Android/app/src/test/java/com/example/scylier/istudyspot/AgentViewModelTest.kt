package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.agent.AgentChatResponse
import com.example.scylier.istudyspot.models.agent.AgentMessage
import com.example.scylier.istudyspot.models.agent.AgentMessageRole
import com.example.scylier.istudyspot.models.agent.AgentReplyBlock
import com.example.scylier.istudyspot.models.agent.AgentToolDefinition
import com.example.scylier.istudyspot.models.agent.AgentToolExecutionResult
import com.example.scylier.istudyspot.models.agent.AgentUiAction
import com.example.scylier.istudyspot.repository.AgentConversationSnapshot
import com.example.scylier.istudyspot.repository.InMemoryAgentConversationStore
import com.example.scylier.istudyspot.repository.MainRepository
import com.example.scylier.istudyspot.viewmodel.AgentViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AgentViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository = mockk<MainRepository>()
    private lateinit var conversationStore: InMemoryAgentConversationStore
    private lateinit var viewModel: AgentViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        conversationStore = InMemoryAgentConversationStore()
        viewModel = AgentViewModel(repository, conversationStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadCatalogSuccessShouldUpdateCatalog() = runTest {
        coEvery { repository.getAgentToolCatalog() } returns ApiResponse.Success(
            200,
            "success",
            listOf(
                AgentToolDefinition(
                    name = "list_study_rooms",
                    title = "自习室列表"
                )
            )
        )

        viewModel.loadCatalog(forceRefresh = true)

        assertFalse(viewModel.uiState.value.isCatalogLoading)
        assertEquals(1, viewModel.uiState.value.toolCatalog.size)
        assertEquals("list_study_rooms", viewModel.uiState.value.toolCatalog.first().name)
    }

    @Test
    fun submitPromptShouldAppendAssistantMessageAndSuggestions() = runTest {
        coEvery {
            repository.sendAgentMessage("查看预约规则", null)
        } returns ApiResponse.Success(
            200,
            "success",
            AgentChatResponse(
                sessionId = "session-1",
                reply = "已找到预约规则。你最多可以提前 7 天预约。",
                toolResult = AgentToolExecutionResult(
                    tool = "get_reservation_rules",
                    summary = "已加载预约规则",
                    data = mapOf("maxAdvanceDays" to 7),
                    uiAction = AgentUiAction(
                        type = "navigate",
                        route = "reservation_rules",
                        params = emptyMap()
                    )
                ),
                suggestedPrompts = listOf("查看我的预约", "查看可用自习室")
            )
        )

        viewModel.submitPrompt("查看预约规则")

        val state = viewModel.uiState.value
        assertEquals(2, state.messages.size)
        assertEquals("查看预约规则", state.messages.first().content)
        assertTrue(state.messages.last().content.contains("7 天"))
        assertEquals("get_reservation_rules", state.messages.last().result?.tool)
        assertEquals(listOf("查看我的预约", "查看可用自习室"), state.suggestedPrompts)
    }

    @Test
    fun submitPromptShouldPreferStructuredReplyTextAndBlocks() = runTest {
        val blocks = listOf(
            AgentReplyBlock(type = "paragraph", text = "根据规则："),
            AgentReplyBlock(type = "bullet", items = listOf("最多提前 7 天"))
        )
        coEvery {
            repository.sendAgentMessage("查看预约规则", null)
        } returns ApiResponse.Success(
            200,
            "success",
            AgentChatResponse(
                sessionId = "session-structured",
                reply = "**根据规则：**\n\n- **最多提前 7 天**",
                replyText = "根据规则：\n\n最多提前 7 天",
                blocks = blocks,
                suggestedPrompts = listOf("查看可用自习室")
            )
        )

        viewModel.submitPrompt("查看预约规则")

        val assistantMessage = viewModel.uiState.value.messages.last()
        assertEquals("根据规则：\n\n最多提前 7 天", assistantMessage.content)
        assertEquals(blocks, assistantMessage.blocks)
        assertFalse(assistantMessage.content.contains("**"))
    }

    @Test
    fun viewModelShouldRestoreLocalConversationSnapshot() = runTest {
        val restoredMessage = AgentMessage(
            id = "message-1",
            role = AgentMessageRole.ASSISTANT,
            content = "上次查询到 3 间自习室。"
        )
        val restoredStore = InMemoryAgentConversationStore(
            AgentConversationSnapshot(
                sessionId = "restored-session",
                messages = listOf(restoredMessage),
                suggestedPrompts = listOf("查看预约规则")
            )
        )

        val restoredViewModel = AgentViewModel(repository, restoredStore)

        assertEquals(listOf(restoredMessage), restoredViewModel.uiState.value.messages)
        assertEquals(listOf("查看预约规则"), restoredViewModel.uiState.value.suggestedPrompts)
    }

    @Test
    fun submitPromptShouldFallbackToLocalSuggestionsWhenBackendDoesNotProvideAny() = runTest {
        coEvery {
            repository.sendAgentMessage("查看我的预约", null)
        } returns ApiResponse.Success(
            200,
            "success",
            AgentChatResponse(
                sessionId = "session-2",
                reply = "找到 1 条预约记录。你可以打开预约列表查看完整详情。",
                toolResult = AgentToolExecutionResult(
                    tool = "get_my_reservations",
                    summary = "已加载预约记录",
                    data = mapOf("items" to listOf(mapOf("reference" to "ORDER_REF_1")))
                ),
                suggestedPrompts = emptyList()
            )
        )

        viewModel.submitPrompt("查看我的预约")

        val prompts = viewModel.uiState.value.suggestedPrompts
        assertTrue(prompts.contains("查看预约规则"))
        assertTrue(prompts.contains("查看可用自习室"))
    }

    @Test
    fun submitPromptShouldKeepTopLevelNavigationAction() = runTest {
        coEvery {
            repository.sendAgentMessage("展示学习记录", null)
        } returns ApiResponse.Success(
            200,
            "success",
            AgentChatResponse(
                sessionId = "session-study-record",
                reply = "可以，我会在下方提供入口，你可以打开学习记录页面查看完整统计。",
                uiAction = AgentUiAction(
                    type = "navigate",
                    route = "study_record",
                    params = emptyMap()
                ),
                suggestedPrompts = listOf("查看学习待办")
            )
        )

        viewModel.submitPrompt("展示学习记录")

        val assistantMessage = viewModel.uiState.value.messages.last()
        assertEquals("navigate", assistantMessage.uiAction?.type)
        assertEquals("study_record", assistantMessage.uiAction?.route)
        assertTrue(assistantMessage.results.isEmpty())
        assertEquals(listOf("查看学习待办"), viewModel.uiState.value.suggestedPrompts)
    }

    @Test
    fun restoredConversationShouldKeepNavigationAction() = runTest {
        val restoredAction = AgentUiAction(
            type = "navigate",
            route = "todo_list",
            params = emptyMap()
        )
        val restoredMessage = AgentMessage(
            id = "message-action",
            role = AgentMessageRole.ASSISTANT,
            content = "可以打开学习待办页面查看或管理任务。",
            uiAction = restoredAction
        )
        val restoredStore = InMemoryAgentConversationStore(
            AgentConversationSnapshot(
                sessionId = "restored-action-session",
                messages = listOf(restoredMessage),
                suggestedPrompts = listOf("查看学习记录")
            )
        )

        val restoredViewModel = AgentViewModel(repository, restoredStore)

        assertEquals("todo_list", restoredViewModel.uiState.value.messages.first().uiAction?.route)
        assertEquals(listOf("查看学习记录"), restoredViewModel.uiState.value.suggestedPrompts)
    }

    @Test
    fun submitPromptShouldKeepAllBackendToolResults() = runTest {
        val roomResult = AgentToolExecutionResult(
            tool = "list_study_rooms",
            summary = "已加载自习室",
            data = mapOf(
                "items" to listOf(
                    mapOf(
                        "id" to 12L,
                        "name" to "北区自习室"
                    )
                )
            )
        )
        val ruleResult = AgentToolExecutionResult(
            tool = "get_reservation_rules",
            summary = "已加载预约规则",
            data = mapOf("maxAdvanceDays" to 7)
        )
        coEvery {
            repository.sendAgentMessage("查看自习室和预约规则", null)
        } returns ApiResponse.Success(
            200,
            "success",
            AgentChatResponse(
                sessionId = "session-multi",
                reply = "已找到自习室和预约规则。",
                toolResults = listOf(roomResult, ruleResult),
                suggestedPrompts = emptyList()
            )
        )

        viewModel.submitPrompt("查看自习室和预约规则")

        val assistantMessage = viewModel.uiState.value.messages.last()
        assertEquals("list_study_rooms", assistantMessage.result?.tool)
        assertEquals(listOf("list_study_rooms", "get_reservation_rules"), assistantMessage.results.map { it.tool })
        assertTrue(viewModel.uiState.value.suggestedPrompts.contains("查看 12 号自习室的座位"))
    }

    @Test
    fun submitPromptSensitiveDenialShouldRenderWithoutToolResultOrError() = runTest {
        coEvery {
            repository.sendAgentMessage("帮我预约一个靠窗座位", null)
        } returns ApiResponse.Success(
            200,
            "success",
            AgentChatResponse(
                sessionId = "session-denial",
                reply = "我可以帮你查看座位是否可用，但不能代你创建或修改预约。",
                toolResults = emptyList(),
                suggestedPrompts = listOf("查看可用自习室")
            )
        )

        viewModel.submitPrompt("帮我预约一个靠窗座位")

        val assistantMessage = viewModel.uiState.value.messages.last()
        assertFalse(assistantMessage.isError)
        assertEquals(null, assistantMessage.result)
        assertTrue(assistantMessage.results.isEmpty())
        assertEquals(listOf("查看可用自习室"), viewModel.uiState.value.suggestedPrompts)
    }

    @Test
    fun submitPromptUnauthorizedShouldShowReadableError() = runTest {
        coEvery {
            repository.sendAgentMessage("查看我的预约", null)
        } returns ApiResponse.Error(401, "UNAUTHORIZED")

        viewModel.submitPrompt("查看我的预约")

        val lastMessage = viewModel.uiState.value.messages.last()
        assertTrue(lastMessage.isError)
        assertEquals("请先登录后再使用智能助手。", lastMessage.content)
    }

    @Test
    fun executeToolShouldStillSupportCatalogQuickAction() = runTest {
        coEvery {
            repository.executeAgentTool("get_reservation_rules", any())
        } returns ApiResponse.Success(
            200,
            "success",
            AgentToolExecutionResult(
                tool = "get_reservation_rules",
                summary = "已加载预约规则",
                data = mapOf("maxAdvanceDays" to 7)
            )
        )

        viewModel.executeTool("get_reservation_rules")

        val state = viewModel.uiState.value
        val lastMessage = state.messages.last()
        assertEquals("已加载预约规则", lastMessage.content)
        assertEquals("get_reservation_rules", lastMessage.result?.tool)
        assertTrue(state.suggestedPrompts.contains("查看我的预约"))
    }

    @Test
    fun triggerToolShortcutShouldUsePromptFallbackForSeatCatalogShortcutWithoutRoomContext() = runTest {
        val seatTool = AgentToolDefinition(
            name = "list_room_seats",
            title = "座位列表"
        )

        coEvery {
            repository.sendAgentMessage("查看可用自习室", null)
        } returns ApiResponse.Success(
            200,
            "success",
            AgentChatResponse(
                sessionId = "session-seat",
                reply = "找到 1 间自习室。你可以继续查看详情或座位。",
                toolResult = AgentToolExecutionResult(
                    tool = "list_study_rooms",
                    summary = "已加载自习室",
                    data = mapOf(
                        "items" to listOf(
                            mapOf(
                                "id" to 9L,
                                "name" to "九号自习室"
                            )
                        )
                    )
                ),
                suggestedPrompts = emptyList()
            )
        )

        viewModel.triggerToolShortcut(seatTool)

        val state = viewModel.uiState.value
        assertEquals("查看可用自习室", state.messages.first().content)
        assertTrue(state.suggestedPrompts.contains("查看 9 号自习室的座位"))
    }

    @Test
    fun executeToolShouldUseReturnedRoomIdForSuggestions() = runTest {
        coEvery {
            repository.executeAgentTool("list_study_rooms", any())
        } returns ApiResponse.Success(
            200,
            "success",
            AgentToolExecutionResult(
                tool = "list_study_rooms",
                summary = "已加载自习室",
                data = mapOf(
                    "items" to listOf(
                        mapOf(
                            "id" to 12L,
                            "name" to "北区自习室"
                        )
                    )
                )
            )
        )

        viewModel.executeTool("list_study_rooms")

        val prompts = viewModel.uiState.value.suggestedPrompts
        assertTrue(prompts.contains("查看 12 号自习室的座位"))
        assertFalse(prompts.contains("查看 1 号自习室的座位"))
    }
}
