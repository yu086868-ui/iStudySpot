package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.Character;
import com.ycyu.istudyspotbackend.entity.Session;
import com.ycyu.istudyspotbackend.service.DeepSeekService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class AIServiceImplTest {

    @Mock
    private DeepSeekService deepSeekService;

    @InjectMocks
    private AIServiceImpl aiService;

    @BeforeEach
    void setUp() {
        // 初始化测试环境
    }

    @Test
    void testGetCharacters() {
        // 测试获取角色列表
        List<Character> characters = aiService.getCharacters();

        // 验证结果
        assertNotNull(characters);
        assertFalse(characters.isEmpty());
        assertEquals(3, characters.size());
    }

    @Test
    void testGetCharacter() {
        // 测试获取角色
        Character character = aiService.getCharacter("scientist");
        assertNotNull(character);
        assertEquals("scientist", character.getId());
        assertEquals("科学家", character.getName());

        // 测试获取不存在的角色
        Character nonExistentCharacter = aiService.getCharacter("non-existent");
        assertNull(nonExistentCharacter);
    }

    @Test
    void testGetOrCreateSession() {
        // 测试获取或创建会话
        Session session = aiService.getOrCreateSession("test-session-123", "scientist");
        assertNotNull(session);
        assertEquals("test-session-123", session.getSession_id());
        assertEquals("scientist", session.getCharacter_id());

        // 测试获取已存在的会话
        Session existingSession = aiService.getOrCreateSession("test-session-123", "scientist");
        assertNotNull(existingSession);
        assertSame(session, existingSession);
    }

    @Test
    void testChat() {
        // 测试与角色聊天
        String response = aiService.chat("test-session-123", "scientist", "你好");

        // 验证结果
        assertNotNull(response);
        assertTrue(response.contains("科学"));
    }

    @Test
    void testChatWithInvalidCharacter() {
        // 测试与不存在的角色聊天
        assertThrows(IllegalArgumentException.class, () -> {
            aiService.chat("test-session-123", "non-existent", "你好");
        });
    }

    @Test
    void testStreamChat() {
        // 测试流式聊天
        SseEmitter emitter = aiService.streamChat("test-session-123", "scientist", "你好");
        assertNotNull(emitter);
    }

    @Test
    void testStreamChatWithInvalidCharacter() {
        // 测试与不存在的角色流式聊天
        SseEmitter emitter = aiService.streamChat("test-session-123", "non-existent", "你好");
        assertNotNull(emitter);
    }
}
