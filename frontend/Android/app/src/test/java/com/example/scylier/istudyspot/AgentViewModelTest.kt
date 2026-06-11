package com.example.scylier.istudyspot

import com.example.scylier.istudyspot.models.ApiResponse
import com.example.scylier.istudyspot.models.agent.AgentChatResponse
import com.example.scylier.istudyspot.models.agent.AgentToolDefinition
import com.example.scylier.istudyspot.models.agent.AgentToolExecutionResult
import com.example.scylier.istudyspot.models.agent.AgentUiAction
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
    private lateinit var viewModel: AgentViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AgentViewModel(repository)
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
                    title = "Study rooms"
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
            repository.sendAgentMessage("Show reservation rules", null)
        } returns ApiResponse.Success(
            200,
            "success",
            AgentChatResponse(
                sessionId = "session-1",
                reply = "I found the reservation rules. You can reserve up to 7 days in advance.",
                toolResult = AgentToolExecutionResult(
                    tool = "get_reservation_rules",
                    summary = "Reservation rules loaded",
                    data = mapOf("maxAdvanceDays" to 7),
                    uiAction = AgentUiAction(
                        type = "navigate",
                        route = "reservation_rules",
                        params = emptyMap()
                    )
                ),
                suggestedPrompts = listOf("Show my reservations", "Show available study rooms")
            )
        )

        viewModel.submitPrompt("Show reservation rules")

        val state = viewModel.uiState.value
        assertEquals(2, state.messages.size)
        assertEquals("Show reservation rules", state.messages.first().content)
        assertTrue(state.messages.last().content.contains("7 days"))
        assertEquals("get_reservation_rules", state.messages.last().result?.tool)
        assertEquals(listOf("Show my reservations", "Show available study rooms"), state.suggestedPrompts)
    }

    @Test
    fun submitPromptShouldFallbackToLocalSuggestionsWhenBackendDoesNotProvideAny() = runTest {
        coEvery {
            repository.sendAgentMessage("Show my reservations", null)
        } returns ApiResponse.Success(
            200,
            "success",
            AgentChatResponse(
                sessionId = "session-2",
                reply = "I found 1 reservation records. You can open the order page for full details.",
                toolResult = AgentToolExecutionResult(
                    tool = "get_my_reservations",
                    summary = "Reservations loaded",
                    data = mapOf("items" to listOf(mapOf("reference" to "ORDER_REF_1")))
                ),
                suggestedPrompts = emptyList()
            )
        )

        viewModel.submitPrompt("Show my reservations")

        val prompts = viewModel.uiState.value.suggestedPrompts
        assertTrue(prompts.contains("Show reservation rules"))
        assertTrue(prompts.contains("Show available study rooms"))
    }

    @Test
    fun submitPromptUnauthorizedShouldShowReadableError() = runTest {
        coEvery {
            repository.sendAgentMessage("Show my reservations", null)
        } returns ApiResponse.Error(401, "UNAUTHORIZED")

        viewModel.submitPrompt("Show my reservations")

        val lastMessage = viewModel.uiState.value.messages.last()
        assertTrue(lastMessage.isError)
        assertEquals("Please log in before using AI Agent.", lastMessage.content)
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
                summary = "Reservation rules loaded",
                data = mapOf("maxAdvanceDays" to 7)
            )
        )

        viewModel.executeTool("get_reservation_rules")

        val state = viewModel.uiState.value
        val lastMessage = state.messages.last()
        assertEquals("Reservation rules loaded", lastMessage.content)
        assertEquals("get_reservation_rules", lastMessage.result?.tool)
        assertTrue(state.suggestedPrompts.contains("Show my reservations"))
    }

    @Test
    fun triggerToolShortcutShouldUsePromptFallbackForSeatCatalogShortcutWithoutRoomContext() = runTest {
        val seatTool = AgentToolDefinition(
            name = "list_room_seats",
            title = "Seats"
        )

        coEvery {
            repository.sendAgentMessage("Show available study rooms", null)
        } returns ApiResponse.Success(
            200,
            "success",
            AgentChatResponse(
                sessionId = "session-seat",
                reply = "I found 1 study rooms. You can view details or check seats next.",
                toolResult = AgentToolExecutionResult(
                    tool = "list_study_rooms",
                    summary = "Rooms loaded",
                    data = mapOf(
                        "items" to listOf(
                            mapOf(
                                "id" to 9L,
                                "name" to "Room Nine"
                            )
                        )
                    )
                ),
                suggestedPrompts = emptyList()
            )
        )

        viewModel.triggerToolShortcut(seatTool)

        val state = viewModel.uiState.value
        assertEquals("Show available study rooms", state.messages.first().content)
        assertTrue(state.suggestedPrompts.contains("Show seats for room 9"))
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
                summary = "Study rooms loaded",
                data = mapOf(
                    "items" to listOf(
                        mapOf(
                            "id" to 12L,
                            "name" to "North Room"
                        )
                    )
                )
            )
        )

        viewModel.executeTool("list_study_rooms")

        val prompts = viewModel.uiState.value.suggestedPrompts
        assertTrue(prompts.contains("Show seats for room 12"))
        assertFalse(prompts.contains("Show seats for room 1"))
    }
}
