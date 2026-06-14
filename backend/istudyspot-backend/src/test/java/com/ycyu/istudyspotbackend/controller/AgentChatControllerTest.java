package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.agent.chat.AgentChatRequest;
import com.ycyu.istudyspotbackend.agent.chat.AgentChatResponse;
import com.ycyu.istudyspotbackend.agent.chat.AgentChatService;
import com.ycyu.istudyspotbackend.agent.tool.AgentToolExecutionResult;
import com.ycyu.istudyspotbackend.entity.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class AgentChatControllerTest {

    @Mock
    private AgentChatService agentChatService;

    @Mock
    private HttpServletRequest servletRequest;

    @InjectMocks
    private AgentChatController agentChatController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void chatShouldReturnSuccess() {
        AgentChatRequest request = new AgentChatRequest();
        request.setMessage("Show reservation rules");

        AgentToolExecutionResult toolResult = new AgentToolExecutionResult(
                "get_reservation_rules",
                "Reservation rules loaded",
                Map.of("maxAdvanceDays", 7),
                Map.of("type", "navigate", "route", "reservation_rules", "params", Map.of()),
                Map.of()
        );

        when(servletRequest.getAttribute("userId")).thenReturn(1L);
        when(agentChatService.chat(1L, request)).thenReturn(
                new AgentChatResponse(
                        "session-1",
                        "I found the reservation rules. You can reserve up to 7 days in advance.",
                        toolResult,
                        List.of(toolResult),
                        List.of("Show my reservations")
                )
        );

        Result<?> result = agentChatController.chat(request, servletRequest);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void chatShouldReturnStructuredError() {
        AgentChatRequest request = new AgentChatRequest();
        request.setMessage("");

        when(servletRequest.getAttribute("userId")).thenReturn(1L);
        when(agentChatService.chat(1L, request)).thenThrow(new IllegalArgumentException("EMPTY_MESSAGE"));

        Result<?> result = agentChatController.chat(request, servletRequest);

        assertEquals(400, result.getCode());
        assertEquals("EMPTY_MESSAGE", result.getMessage());
        assertTrue(result.getData() instanceof Map<?, ?>);
        Map<?, ?> payload = (Map<?, ?>) result.getData();
        assertEquals("1.0", payload.get("schemaVersion"));
        assertTrue(payload.containsKey("error"));
    }

    @Test
    void chatShouldReturnUnauthorizedPayload() {
        AgentChatRequest request = new AgentChatRequest();
        request.setMessage("Show my reservations");

        when(servletRequest.getAttribute("userId")).thenReturn(null);
        when(agentChatService.chat(null, request)).thenThrow(new IllegalArgumentException("AUTH_REQUIRED"));

        Result<?> result = agentChatController.chat(request, servletRequest);

        assertEquals(401, result.getCode());
        assertEquals("AUTH_REQUIRED", result.getMessage());
        assertTrue(result.getData() instanceof Map<?, ?>);
        Map<?, ?> payload = (Map<?, ?>) result.getData();
        assertEquals("1.0", payload.get("schemaVersion"));
    }
}
