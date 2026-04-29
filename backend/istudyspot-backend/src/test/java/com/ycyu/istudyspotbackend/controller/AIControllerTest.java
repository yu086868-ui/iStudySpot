package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Character;
import com.ycyu.istudyspotbackend.service.AIService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        // 准备测试数据
        List<Character> characters = new ArrayList<>();
        Character character = new Character();
        character.setId("1");
        character.setName("Test Character");
        characters.add(character);

        // 模拟服务方法
        when(aiService.getCharacters()).thenReturn(characters);

        // 调用控制器方法
        ResponseEntity<List<Character>> response = aiController.getCharacters();

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(characters, response.getBody());
        verify(aiService, times(1)).getCharacters();
    }

    @Test
    public void testChatWithValidParams() {
        // 准备测试数据
        Map<String, String> request = Map.of(
                "session_id", "session123",
                "character_id", "char123",
                "message", "Hello"
        );
        String expectedReply = "Hello, how can I help you?";

        // 模拟服务方法
        when(aiService.chat("session123", "char123", "Hello")).thenReturn(expectedReply);

        // 调用控制器方法
        ResponseEntity<?> response = aiController.chat(request);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<?, ?> responseBody = (Map<?, ?>) response.getBody();
        assertEquals(expectedReply, responseBody.get("reply"));
        verify(aiService, times(1)).chat("session123", "char123", "Hello");
    }

    @Test
    public void testChatWithEmptySessionId() {
        // 准备测试数据
        Map<String, String> request = Map.of(
                "character_id", "char123",
                "message", "Hello"
        );

        // 调用控制器方法
        ResponseEntity<?> response = aiController.chat(request);

        // 验证结果
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<?, ?> responseBody = (Map<?, ?>) response.getBody();
        assertEquals("EMPTY_SESSION_ID", responseBody.get("error"));
        verify(aiService, never()).chat(anyString(), anyString(), anyString());
    }

    @Test
    public void testChatWithEmptyCharacterId() {
        // 准备测试数据
        Map<String, String> request = Map.of(
                "session_id", "session123",
                "message", "Hello"
        );

        // 调用控制器方法
        ResponseEntity<?> response = aiController.chat(request);

        // 验证结果
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<?, ?> responseBody = (Map<?, ?>) response.getBody();
        assertEquals("EMPTY_CHARACTER_ID", responseBody.get("error"));
        verify(aiService, never()).chat(anyString(), anyString(), anyString());
    }

    @Test
    public void testChatWithEmptyMessage() {
        // 准备测试数据
        Map<String, String> request = Map.of(
                "session_id", "session123",
                "character_id", "char123"
        );

        // 调用控制器方法
        ResponseEntity<?> response = aiController.chat(request);

        // 验证结果
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<?, ?> responseBody = (Map<?, ?>) response.getBody();
        assertEquals("EMPTY_MESSAGE", responseBody.get("error"));
        verify(aiService, never()).chat(anyString(), anyString(), anyString());
    }

    @Test
    public void testChatWithIllegalArgumentException() {
        // 准备测试数据
        Map<String, String> request = Map.of(
                "session_id", "session123",
                "character_id", "char123",
                "message", "Hello"
        );

        // 模拟服务方法抛出异常
        when(aiService.chat("session123", "char123", "Hello")).thenThrow(new IllegalArgumentException());

        // 调用控制器方法
        ResponseEntity<?> response = aiController.chat(request);

        // 验证结果
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<?, ?> responseBody = (Map<?, ?>) response.getBody();
        assertEquals("INVALID_CHARACTER", responseBody.get("error"));
        verify(aiService, times(1)).chat("session123", "char123", "Hello");
    }

    @Test
    public void testChatWithException() {
        // 准备测试数据
        Map<String, String> request = Map.of(
                "session_id", "session123",
                "character_id", "char123",
                "message", "Hello"
        );

        // 模拟服务方法抛出异常
        when(aiService.chat("session123", "char123", "Hello")).thenThrow(new RuntimeException());

        // 调用控制器方法
        ResponseEntity<?> response = aiController.chat(request);

        // 验证结果
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<?, ?> responseBody = (Map<?, ?>) response.getBody();
        assertEquals("INTERNAL_ERROR", responseBody.get("error"));
        verify(aiService, times(1)).chat("session123", "char123", "Hello");
    }

    @Test
    public void testStreamChatWithValidParams() {
        // 准备测试数据
        Map<String, String> request = Map.of(
                "session_id", "session123",
                "character_id", "char123",
                "message", "Hello"
        );
        SseEmitter expectedEmitter = new SseEmitter();

        // 模拟服务方法
        when(aiService.streamChat("session123", "char123", "Hello")).thenReturn(expectedEmitter);

        // 调用控制器方法
        SseEmitter emitter = aiController.streamChat(request);

        // 验证结果
        assertNotNull(emitter);
        verify(aiService, times(1)).streamChat("session123", "char123", "Hello");
    }

    @Test
    public void testStreamChatWithEmptySessionId() {
        // 准备测试数据
        Map<String, String> request = Map.of(
                "character_id", "char123",
                "message", "Hello"
        );

        // 调用控制器方法
        SseEmitter emitter = aiController.streamChat(request);

        // 验证结果
        assertNotNull(emitter);
        verify(aiService, never()).streamChat(anyString(), anyString(), anyString());
    }

    @Test
    public void testStreamChatWithEmptyCharacterId() {
        // 准备测试数据
        Map<String, String> request = Map.of(
                "session_id", "session123",
                "message", "Hello"
        );

        // 调用控制器方法
        SseEmitter emitter = aiController.streamChat(request);

        // 验证结果
        assertNotNull(emitter);
        verify(aiService, never()).streamChat(anyString(), anyString(), anyString());
    }

    @Test
    public void testStreamChatWithEmptyMessage() {
        // 准备测试数据
        Map<String, String> request = Map.of(
                "session_id", "session123",
                "character_id", "char123"
        );

        // 调用控制器方法
        SseEmitter emitter = aiController.streamChat(request);

        // 验证结果
        assertNotNull(emitter);
        verify(aiService, never()).streamChat(anyString(), anyString(), anyString());
    }

    @Test
    public void testStreamChatWithException() {
        // 准备测试数据
        Map<String, String> request = Map.of(
                "session_id", "session123",
                "character_id", "char123",
                "message", "Hello"
        );

        // 模拟服务方法抛出异常
        when(aiService.streamChat("session123", "char123", "Hello")).thenThrow(new RuntimeException());

        // 调用控制器方法
        SseEmitter emitter = aiController.streamChat(request);

        // 验证结果
        assertNotNull(emitter);
        verify(aiService, times(1)).streamChat("session123", "char123", "Hello");
    }
}
