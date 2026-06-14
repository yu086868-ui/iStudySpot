package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.agent.tool.AgentToolExecuteRequest;
import com.ycyu.istudyspotbackend.agent.tool.AgentToolExecutionResult;
import com.ycyu.istudyspotbackend.agent.tool.AgentToolService;
import com.ycyu.istudyspotbackend.entity.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class AgentToolControllerTest {

    @Mock
    private AgentToolService agentToolService;

    @Mock
    private HttpServletRequest servletRequest;

    @InjectMocks
    private AgentToolController agentToolController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void executeShouldReturnSuccess() {
        AgentToolExecuteRequest request = new AgentToolExecuteRequest();
        request.setTool("get_reservation_rules");

        when(servletRequest.getAttribute("userId")).thenReturn(1L);
        when(agentToolService.execute(1L, request)).thenReturn(
                new AgentToolExecutionResult(
                        "get_reservation_rules",
                        "Reservation rules loaded",
                        Map.of("maxAdvanceDays", 7),
                        Map.of("type", "navigate", "route", "reservation_rules", "params", Map.of()),
                        Map.of()
                )
        );

        Result<?> result = agentToolController.execute(request, servletRequest);

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
    }

    @Test
    void executeShouldReturnUnauthorizedPayload() {
        AgentToolExecuteRequest request = new AgentToolExecuteRequest();
        request.setTool("get_my_reservations");

        when(servletRequest.getAttribute("userId")).thenReturn(null);
        when(agentToolService.execute(null, request)).thenThrow(new IllegalArgumentException("AUTH_REQUIRED"));

        Result<?> result = agentToolController.execute(request, servletRequest);

        assertEquals(401, result.getCode());
        assertEquals("AUTH_REQUIRED", result.getMessage());
        assertTrue(result.getData() instanceof Map<?, ?>);
    }

    @Test
    void executeShouldReturnStructuredFieldError() {
        AgentToolExecuteRequest request = new AgentToolExecuteRequest();
        request.setTool("list_room_seats");

        when(servletRequest.getAttribute("userId")).thenReturn(1L);
        when(agentToolService.execute(1L, request)).thenThrow(new IllegalArgumentException("MISSING_STUDYROOMID"));

        Result<?> result = agentToolController.execute(request, servletRequest);

        assertEquals(400, result.getCode());
        assertEquals("MISSING_STUDYROOMID", result.getMessage());
        assertTrue(result.getData() instanceof Map<?, ?>);
        Map<?, ?> payload = (Map<?, ?>) result.getData();
        assertEquals("1.0", payload.get("schemaVersion"));
    }
}
