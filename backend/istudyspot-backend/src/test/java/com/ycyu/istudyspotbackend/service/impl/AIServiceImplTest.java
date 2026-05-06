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
    }

    @Test
    void testGetCharacters() {
        List<Character> characters = aiService.getCharacters();

        assertNotNull(characters);
        assertFalse(characters.isEmpty());
        assertEquals(4, characters.size());
    }

    @Test
    void testGetCharacter() {
        Character character = aiService.getCharacter("scientist");
        assertNotNull(character);
        assertEquals("scientist", character.getId());
        assertEquals("科学家", character.getName());

        Character character2 = aiService.getCharacter("customer_service");
        assertNotNull(character2);
        assertEquals("customer_service", character2.getId());
        assertEquals("小i", character2.getName());

        Character nonExistentCharacter = aiService.getCharacter("non-existent");
        assertNull(nonExistentCharacter);
    }

    @Test
    void testGetOrCreateSession() {
        Session session = aiService.getOrCreateSession("test-session-123", "scientist");
        assertNotNull(session);
        assertEquals("test-session-123", session.getSession_id());
        assertEquals("scientist", session.getCharacter_id());

        Session existingSession = aiService.getOrCreateSession("test-session-123", "scientist");
        assertNotNull(existingSession);
        assertSame(session, existingSession);
    }

    @Test
    void testChat() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("从科学的角度来看，这个问题涉及到多个领域的知识。");

        String response = aiService.chat("test-session-123", "scientist", "你好");

        assertNotNull(response);
        assertTrue(response.contains("科学"));
        verify(deepSeekService, times(1)).chat(anyString(), anyList());
    }

    @Test
    void testChatWithInvalidCharacter() {
        assertThrows(IllegalArgumentException.class, () -> {
            aiService.chat("test-session-123", "non-existent", "你好");
        });
    }

    @Test
    void testStreamChat() {
        SseEmitter emitter = aiService.streamChat("test-session-123", "scientist", "你好");
        assertNotNull(emitter);
    }

    @Test
    void testStreamChatWithInvalidCharacter() {
        SseEmitter emitter = aiService.streamChat("test-session-123", "non-existent", "你好");
        assertNotNull(emitter);
    }
}