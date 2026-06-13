package com.ycyu.istudyspotbackend.agent.chat;

import com.ycyu.istudyspotbackend.agent.tool.AgentToolExecuteRequest;
import com.ycyu.istudyspotbackend.agent.tool.AgentToolDefinition;
import com.ycyu.istudyspotbackend.agent.tool.AgentToolExecutionResult;
import com.ycyu.istudyspotbackend.agent.tool.AgentToolService;
import com.ycyu.istudyspotbackend.service.DeepSeekService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Mock
    private DeepSeekService deepSeekService;

    private AgentChatServiceImpl agentChatService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        agentChatService = new AgentChatServiceImpl(agentToolService, deepSeekService);
        objectMapper = new ObjectMapper();
        when(agentToolService.getCatalog()).thenReturn(List.of(
                new AgentToolDefinition(
                        "list_study_rooms",
                        "自习室列表",
                        "查询可用于搜索和筛选的自习室列表。",
                        true,
                        List.of("studyroom", "read"),
                        Map.of("keyword", "string?", "page", "number?", "pageSize", "number?")
                ),
                new AgentToolDefinition(
                        "get_reservation_rules",
                        "预约规则",
                        "查询预约限制、取消规则和爽约扣分。",
                        true,
                        List.of("reservation", "read", "rules"),
                        Map.of()
                ),
                new AgentToolDefinition(
                        "list_room_seats",
                        "座位列表",
                        "查询指定自习室的座位，可按状态和类型筛选。",
                        true,
                        List.of("seat", "read"),
                        Map.of("studyRoomId", "number")
                )
        ));
    }

    @Test
    void chatShouldExecuteRulesTool() {
        AgentChatRequest request = new AgentChatRequest();
        request.setMessage("查看预约规则");

        when(deepSeekService.chatCompletion(any(), any(), any())).thenThrow(new RuntimeException("LLM unavailable"));
        when(agentToolService.execute(eq(1L), any())).thenReturn(
                new AgentToolExecutionResult(
                        "get_reservation_rules",
                        "已加载预约规则。",
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
        assertTrue(response.getReply().contains("7 天"));
        assertFalse(response.getReplyText().contains("**"));
        assertEquals("plain", response.getReplyFormat());
        assertFalse(response.getBlocks().isEmpty());
        assertFalse(response.getSuggestedPrompts().isEmpty());
    }

    @Test
    void responseShouldExposePlainTextAndBlocksForMarkdownReply() {
        AgentChatResponse response = new AgentChatResponse(
                "session-markdown",
                "根据规则：\n\n- **最多提前 7 天**\n- 取消请到 App 页面操作",
                null,
                List.of(),
                List.of()
        );

        assertEquals("plain", response.getReplyFormat());
        assertEquals("根据规则：\n\n最多提前 7 天\n取消请到 App 页面操作", response.getReplyText());
        assertEquals(2, response.getBlocks().size());
        assertEquals("paragraph", response.getBlocks().get(0).getType());
        assertEquals("bullet", response.getBlocks().get(1).getType());
        assertEquals(List.of("最多提前 7 天", "取消请到 App 页面操作"), response.getBlocks().get(1).getItems());
    }

    @Test
    void chatShouldAskForRoomBeforeSeatTool() {
        AgentChatRequest request = new AgentChatRequest();
        request.setMessage("查看座位");

        when(deepSeekService.chatCompletion(any(), any(), any())).thenThrow(new RuntimeException("LLM unavailable"));
        AgentChatResponse response = agentChatService.chat(1L, request);

        assertNull(response.getToolResult());
        assertTrue(response.getReply().contains("自习室编号"));
        assertFalse(response.getSuggestedPrompts().isEmpty());
        verify(agentToolService, never()).execute(any(), any());
    }

    @Test
    void chatShouldReuseRoomContextForFollowUpSeatQuery() {
        AgentChatRequest firstRequest = new AgentChatRequest();
        firstRequest.setSessionId("session-1");
        firstRequest.setMessage("查看 1 号自习室详情");

        when(deepSeekService.chatCompletion(any(), any(), any())).thenThrow(new RuntimeException("LLM unavailable"));
        when(agentToolService.execute(eq(1L), any())).thenReturn(
                new AgentToolExecutionResult(
                        "get_study_room_detail",
                        "已加载自习室详情。",
                        Map.of("studyRoom", Map.of("id", 1L, "name", "一号自习室", "address", "三楼")),
                        Map.of("type", "navigate", "route", "studyroom_detail", "params", Map.of("studyRoomId", 1L)),
                        Map.of()
                )
        );

        agentChatService.chat(1L, firstRequest);

        AgentChatRequest secondRequest = new AgentChatRequest();
        secondRequest.setSessionId("session-1");
        secondRequest.setMessage("查看座位");

        when(agentToolService.execute(eq(1L), any())).thenReturn(
                new AgentToolExecutionResult(
                        "list_room_seats",
                        "已加载座位。",
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
        request.setMessage("价格和退款政策是什么？");

        when(deepSeekService.chatCompletion(any(), any(), any())).thenThrow(new RuntimeException("LLM unavailable"));
        AgentChatResponse response = agentChatService.chat(1L, request);

        assertNull(response.getToolResult());
        assertTrue(response.getReply().contains("无法"));
        assertFalse(response.getSuggestedPrompts().isEmpty());
        verify(agentToolService, never()).execute(any(), any());
    }

    @Test
    void chatShouldReturnGenericFallbackForUnsupportedPrompt() {
        AgentChatRequest request = new AgentChatRequest();
        request.setMessage("你会做饭吗？");

        when(deepSeekService.chatCompletion(any(), any(), any())).thenThrow(new RuntimeException("LLM unavailable"));
        AgentChatResponse response = agentChatService.chat(1L, request);

        assertNull(response.getToolResult());
        assertTrue(response.getReply().contains("自习室"));
        assertFalse(response.getSuggestedPrompts().isEmpty());
        verify(agentToolService, never()).execute(any(), any());
    }

    @Test
    void chatShouldReturnStudyRecordNavigationActionWithoutToolCall() {
        AgentChatRequest request = new AgentChatRequest();
        request.setMessage("帮我展示学习记录");

        when(deepSeekService.chatCompletion(any(), any(), any())).thenThrow(new RuntimeException("LLM unavailable"));
        AgentChatResponse response = agentChatService.chat(1L, request);

        assertNull(response.getToolResult());
        assertTrue(response.getToolResults().isEmpty());
        assertEquals("navigate", response.getUiAction().get("type"));
        assertEquals("study_record", response.getUiAction().get("route"));
        assertTrue(response.getReply().contains("学习记录"));
        verify(agentToolService, never()).execute(any(), any());
    }

    @Test
    void chatShouldReturnTodoNavigationActionWithoutToolCall() {
        AgentChatRequest request = new AgentChatRequest();
        request.setMessage("打开学习待办");

        when(deepSeekService.chatCompletion(any(), any(), any())).thenThrow(new RuntimeException("LLM unavailable"));
        AgentChatResponse response = agentChatService.chat(1L, request);

        assertNull(response.getToolResult());
        assertTrue(response.getToolResults().isEmpty());
        assertEquals("navigate", response.getUiAction().get("type"));
        assertEquals("todo_list", response.getUiAction().get("route"));
        assertTrue(response.getReply().contains("学习待办"));
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
        request.setMessage("查看我的预约");

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> agentChatService.chat(null, request)
        );

        assertEquals("AUTH_REQUIRED", error.getMessage());
    }

    @Test
    void chatShouldUseLlmToolCallWhenAvailable() throws Exception {
        AgentChatRequest request = new AgentChatRequest();
        request.setMessage("有哪些自习室？");

        when(deepSeekService.chatCompletion(any(), any(), any()))
                .thenReturn(objectMapper.readTree("""
                        {
                          "choices": [
                            {
                              "message": {
                                "content": "{\\"allowedReadOnly\\":true,\\"reply\\":\\"\\"}"
                              }
                            }
                          ]
                        }
                        """))
                .thenReturn(objectMapper.readTree("""
                        {
                          "choices": [
                            {
                              "message": {
                                "tool_calls": [
                                  {
                                    "function": {
                                      "name": "list_study_rooms",
                                      "arguments": "{\\"keyword\\":\\"quiet\\"}"
                                    }
                                  }
                                ]
                              }
                            }
                          ]
                        }
                        """))
                .thenReturn(objectMapper.readTree("""
                        {
                          "choices": [
                            {
                              "message": {
                                "content": "找到 1 间自习室，可以继续查看详情或座位。"
                              }
                            }
                          ]
                        }
                        """));

        when(agentToolService.execute(eq(1L), any())).thenReturn(
                new AgentToolExecutionResult(
                        "list_study_rooms",
                        "已加载 1 间自习室。",
                        Map.of("items", List.of(Map.of("id", 1L, "name", "安静自习室"))),
                        Map.of("type", "navigate", "route", "studyroom_list", "params", Map.of()),
                        Map.of()
                )
        );

        AgentChatResponse response = agentChatService.chat(1L, request);

        assertEquals("找到 1 间自习室，可以继续查看详情或座位。", response.getReply());
        assertEquals("list_study_rooms", response.getToolResult().getTool());
        ArgumentCaptor<AgentToolExecuteRequest> captor = ArgumentCaptor.forClass(AgentToolExecuteRequest.class);
        verify(agentToolService).execute(eq(1L), captor.capture());
        assertEquals("quiet", captor.getValue().getArguments().get("keyword"));
    }

    @Test
    void chatShouldRejectWriteIntentFromLlmPolicyBeforeTools() throws Exception {
        AgentChatRequest request = new AgentChatRequest();
        request.setMessage("帮我预约 1 号自习室");

        when(deepSeekService.chatCompletion(any(), any(), any())).thenReturn(objectMapper.readTree("""
                {
                  "choices": [
                    {
                      "message": {
                        "content": "{\\"allowedReadOnly\\":false,\\"reply\\":\\"我只能读取信息，请到预约页面完成预约。\\"}"
                      }
                    }
                  ]
                }
                """));

        AgentChatResponse response = agentChatService.chat(1L, request);

        assertEquals("我只能读取信息，请到预约页面完成预约。", response.getReply());
        verify(deepSeekService, times(1)).chatCompletion(any(), any(), any());
        verify(agentToolService, never()).execute(any(), any());
    }

    @Test
    void chatShouldAllowReadOnlyActionGuidanceFromPolicy() throws Exception {
        AgentChatRequest request = new AgentChatRequest();
        request.setMessage("怎么取消预约？");

        when(deepSeekService.chatCompletion(any(), any(), any()))
                .thenReturn(objectMapper.readTree("""
                        {
                          "choices": [
                            {
                              "message": {
                                "content": "{\\"allowedReadOnly\\":true,\\"reply\\":\\"\\"}"
                              }
                            }
                          ]
                        }
                        """))
                .thenThrow(new RuntimeException("LLM planner unavailable"));

        when(agentToolService.execute(eq(1L), any())).thenReturn(
                new AgentToolExecutionResult(
                        "get_reservation_rules",
                        "已加载预约规则。",
                        Map.of("maxAdvanceDays", 7),
                        Map.of("type", "navigate", "route", "reservation_rules", "params", Map.of()),
                        Map.of()
                )
        );

        AgentChatResponse response = agentChatService.chat(1L, request);

        assertEquals("get_reservation_rules", response.getToolResult().getTool());
        verify(agentToolService).execute(eq(1L), any());
    }
}
