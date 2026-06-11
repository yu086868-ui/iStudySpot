package com.ycyu.istudyspotbackend.agent.chat;

import com.ycyu.istudyspotbackend.agent.tool.AgentToolExecuteRequest;
import com.ycyu.istudyspotbackend.agent.tool.AgentToolExecutionResult;
import com.ycyu.istudyspotbackend.agent.tool.AgentToolService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgentChatServiceImplTest {

    @Mock
    private AgentToolService agentToolService;

    private AgentChatServiceImpl agentChatService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        agentChatService = new AgentChatServiceImpl(agentToolService);
    }

    @Test
    void chatShouldExecuteRulesTool() {
        AgentChatRequest request = new AgentChatRequest();
        request.setMessage("Show reservation rules");

        when(agentToolService.execute(eq(1L), any())).thenReturn(
                new AgentToolExecutionResult(
                        "get_reservation_rules",
                        "Reservation rules loaded",
                        Map.of("maxAdvanceDays", 7),
                        Map.of("type", "navigate", "route", "reservation_rules", "params", Map.of()),
                        Map.of()
                )
        );

        AgentChatResponse response = agentChatService.chat(1L, request);

        assertNotNull(response.getSessionId());
        assertEquals("1.0", response.getSchemaVersion());
        assertNotNull(response.getToolResult());
        assertEquals("get_reservation_rules", response.getToolResult().getTool());
        assertEquals(1, response.getToolResults().size());
        assertTrue(response.getReply().contains("7 days"));
        assertFalse(response.getSuggestedPrompts().isEmpty());
    }

    @Test
    void chatShouldAskForRoomBeforeSeatTool() {
        AgentChatRequest request = new AgentChatRequest();
        request.setMessage("Show seats");

        AgentChatResponse response = agentChatService.chat(1L, request);

        assertNull(response.getToolResult());
        assertTrue(response.getReply().contains("study room id"));
        assertFalse(response.getSuggestedPrompts().isEmpty());
        verify(agentToolService, never()).execute(any(), any());
    }

    @Test
    void chatShouldReuseRoomContextForFollowUpSeatQuery() {
        AgentChatRequest firstRequest = new AgentChatRequest();
        firstRequest.setSessionId("session-1");
        firstRequest.setMessage("Show details for room 1");

        when(agentToolService.execute(eq(1L), any())).thenReturn(
                new AgentToolExecutionResult(
                        "get_study_room_detail",
                        "Study room detail loaded",
                        Map.of("studyRoom", Map.of("id", 1L, "name", "Room One", "address", "Floor 3")),
                        Map.of("type", "navigate", "route", "studyroom_detail", "params", Map.of("studyRoomId", 1L)),
                        Map.of()
                )
        );

        agentChatService.chat(1L, firstRequest);

        AgentChatRequest secondRequest = new AgentChatRequest();
        secondRequest.setSessionId("session-1");
        secondRequest.setMessage("Show seats");

        when(agentToolService.execute(eq(1L), any())).thenReturn(
                new AgentToolExecutionResult(
                        "list_room_seats",
                        "Seats loaded",
                        Map.of("studyRoomId", 1L, "items", List.of(Map.of("id", 1L, "status", "available"))),
                        Map.of("type", "navigate", "route", "seat_list", "params", Map.of("studyRoomId", 1L)),
                        Map.of()
                )
        );

        ArgumentCaptor<AgentToolExecuteRequest> captor =
                ArgumentCaptor.forClass(AgentToolExecuteRequest.class);

        AgentChatResponse response = agentChatService.chat(1L, secondRequest);

        verify(agentToolService, times(2)).execute(eq(1L), captor.capture());
        AgentToolExecuteRequest secondRequestSent = captor.getAllValues().get(1);
        assertEquals("list_room_seats", secondRequestSent.getTool());
        assertEquals(1L, secondRequestSent.getArguments().get("studyRoomId"));
        assertEquals("list_room_seats", response.getToolResult().getTool());
    }

    @Test
    void chatShouldNotCallToolForSensitiveBusinessQuestion() {
        AgentChatRequest request = new AgentChatRequest();
        request.setMessage("What is the price and refund policy?");

        AgentChatResponse response = agentChatService.chat(1L, request);

        assertNull(response.getToolResult());
        assertTrue(response.getReply().contains("cannot confirm"));
        assertFalse(response.getSuggestedPrompts().isEmpty());
        verify(agentToolService, never()).execute(any(), any());
    }

    @Test
    void chatShouldReturnGenericFallbackForUnsupportedPrompt() {
        AgentChatRequest request = new AgentChatRequest();
        request.setMessage("Can you cook dinner?");

        AgentChatResponse response = agentChatService.chat(1L, request);

        assertNull(response.getToolResult());
        assertTrue(response.getReply().contains("study rooms"));
        assertFalse(response.getSuggestedPrompts().isEmpty());
        verify(agentToolService, never()).execute(any(), any());
    }

    @Test
    void chatShouldRejectEmptyMessage() {
        AgentChatRequest request = new AgentChatRequest();
        request.setMessage("   ");

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> agentChatService.chat(1L, request)
        );

        assertEquals("EMPTY_MESSAGE", error.getMessage());
    }

    @Test
    void chatShouldRejectAnonymousUser() {
        AgentChatRequest request = new AgentChatRequest();
        request.setMessage("Show my reservations");

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> agentChatService.chat(null, request)
        );

        assertEquals("AUTH_REQUIRED", error.getMessage());
    }
}
