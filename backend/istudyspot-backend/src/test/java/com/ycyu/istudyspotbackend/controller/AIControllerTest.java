package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Character;
import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.service.AIService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class AIControllerTest {

    @Mock
    private AIService aiService;

    @InjectMocks
    private AIController aiController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetCharacters() {
        List<Character> characters = new ArrayList<>();
        Character character = new Character();
        character.setId("1");
        character.setName("Test Character");
        characters.add(character);

        when(aiService.getCharacters()).thenReturn(characters);

        Result<List<Character>> result = aiController.getCharacters();

        assertEquals(200, result.getCode());
        assertEquals(characters, result.getData());
        verify(aiService, times(1)).getCharacters();
    }

    @Test
    public void testChatWithValidParams() {
        Map<String, String> request = Map.of(
                "session_id", "session123",
                "character_id", "char123",
                "message", "Hello"
        );
        String expectedReply = "Hello, how can I help you?";

        when(aiService.chat("session123", "char123", "Hello")).thenReturn(expectedReply);

        Result<?> result = aiController.chat(request);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        Map<?, ?> data = (Map<?, ?>) result.getData();
        assertEquals(expectedReply, data.get("reply"));
        verify(aiService, times(1)).chat("session123", "char123", "Hello");
    }

    @Test
    public void testChatWithEmptySessionId() {
        Map<String, String> request = Map.of(
                "character_id", "char123",
                "message", "Hello"
        );

        when(aiService.chat(anyString(), eq("char123"), eq("Hello"))).thenReturn("Reply");

        Result<?> result = aiController.chat(request);

        assertEquals(200, result.getCode());
        verify(aiService, times(1)).chat(anyString(), eq("char123"), eq("Hello"));
    }

    @Test
    public void testChatWithEmptyCharacterId() {
        Map<String, String> request = Map.of(
                "session_id", "session123",
                "message", "Hello"
        );

        when(aiService.chat(eq("session123"), eq("customer_service"), eq("Hello"))).thenReturn("Reply");

        Result<?> result = aiController.chat(request);

        assertEquals(200, result.getCode());
        verify(aiService, times(1)).chat(eq("session123"), eq("customer_service"), eq("Hello"));
    }

    @Test
    public void testChatWithEmptyMessage() {
        Map<String, String> request = Map.of(
                "session_id", "session123",
                "character_id", "char123"
        );

        Result<?> result = aiController.chat(request);

        assertEquals(400, result.getCode());
        assertEquals("EMPTY_MESSAGE", result.getMessage());
        verify(aiService, never()).chat(anyString(), anyString(), anyString());
    }

    @Test
    public void testChatWithIllegalArgumentException() {
        Map<String, String> request = Map.of(
                "session_id", "session123",
                "character_id", "char123",
                "message", "Hello"
        );

        when(aiService.chat("session123", "char123", "Hello")).thenThrow(new IllegalArgumentException());

        Result<?> result = aiController.chat(request);

        assertEquals(500, result.getCode());
        assertEquals("INTERNAL_ERROR", result.getMessage());
        verify(aiService, times(1)).chat("session123", "char123", "Hello");
    }

    @Test
    public void testChatWithException() {
        Map<String, String> request = Map.of(
                "session_id", "session123",
                "character_id", "char123",
                "message", "Hello"
        );

        when(aiService.chat("session123", "char123", "Hello")).thenThrow(new RuntimeException());

        Result<?> result = aiController.chat(request);

        assertEquals(500, result.getCode());
        assertEquals("INTERNAL_ERROR", result.getMessage());
        verify(aiService, times(1)).chat("session123", "char123", "Hello");
    }

    @Test
    public void testStreamChatWithValidParams() {
        Map<String, String> request = Map.of(
                "session_id", "session123",
                "character_id", "char123",
                "message", "Hello"
        );
        SseEmitter expectedEmitter = new SseEmitter();

        when(aiService.streamChat("session123", "char123", "Hello")).thenReturn(expectedEmitter);

        SseEmitter emitter = aiController.streamChat(request);

        assertNotNull(emitter);
        verify(aiService, times(1)).streamChat("session123", "char123", "Hello");
    }

    @Test
    public void testStreamChatWithEmptySessionId() {
        Map<String, String> request = Map.of(
                "character_id", "char123",
                "message", "Hello"
        );

        SseEmitter mockEmitter = new SseEmitter();
        when(aiService.streamChat(anyString(), eq("char123"), eq("Hello"))).thenReturn(mockEmitter);

        SseEmitter emitter = aiController.streamChat(request);

        assertNotNull(emitter);
        verify(aiService, times(1)).streamChat(anyString(), eq("char123"), eq("Hello"));
    }

    @Test
    public void testStreamChatWithEmptyCharacterId() {
        Map<String, String> request = Map.of(
                "session_id", "session123",
                "message", "Hello"
        );

        SseEmitter emitter = aiController.streamChat(request);

        assertNotNull(emitter);
        verify(aiService, never()).streamChat(anyString(), anyString(), anyString());
    }

    @Test
    public void testStreamChatWithEmptyMessage() {
        Map<String, String> request = Map.of(
                "session_id", "session123",
                "character_id", "char123"
        );

        SseEmitter emitter = aiController.streamChat(request);

        assertNotNull(emitter);
        verify(aiService, never()).streamChat(anyString(), anyString(), anyString());
    }

    @Test
    public void testStreamChatWithException() {
        Map<String, String> request = Map.of(
                "session_id", "session123",
                "character_id", "char123",
                "message", "Hello"
        );

        when(aiService.streamChat("session123", "char123", "Hello")).thenThrow(new RuntimeException());

        SseEmitter emitter = aiController.streamChat(request);

        assertNotNull(emitter);
        verify(aiService, times(1)).streamChat("session123", "char123", "Hello");
    }
}
