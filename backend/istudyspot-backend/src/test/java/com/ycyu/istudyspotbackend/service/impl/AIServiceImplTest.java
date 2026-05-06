package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.Character;
import com.ycyu.istudyspotbackend.entity.Message;
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

    @Test
    void testGetCharacters() {
        List<Character> characters = aiService.getCharacters();

        assertNotNull(characters);
        assertFalse(characters.isEmpty());
        assertEquals(4, characters.size());

        assertTrue(characters.stream().anyMatch(c -> "scientist".equals(c.getId())));
        assertTrue(characters.stream().anyMatch(c -> "teacher".equals(c.getId())));
        assertTrue(characters.stream().anyMatch(c -> "artist".equals(c.getId())));
        assertTrue(characters.stream().anyMatch(c -> "customer_service".equals(c.getId())));
    }

    @Test
    void testGetCharacter() {
        Character scientist = aiService.getCharacter("scientist");
        assertNotNull(scientist);
        assertEquals("scientist", scientist.getId());
        assertEquals("科学家", scientist.getName());
        assertEquals("理性严谨，喜欢解释原理", scientist.getPersona());
        assertEquals("逻辑清晰，偏长句", scientist.getSpeaking_style());

        Character teacher = aiService.getCharacter("teacher");
        assertNotNull(teacher);
        assertEquals("teacher", teacher.getId());
        assertEquals("老师", teacher.getName());

        Character artist = aiService.getCharacter("artist");
        assertNotNull(artist);
        assertEquals("artist", artist.getId());
        assertEquals("艺术家", artist.getName());

        Character customerService = aiService.getCharacter("customer_service");
        assertNotNull(customerService);
        assertEquals("customer_service", customerService.getId());
        assertEquals("小i", customerService.getName());

        Character nonExistent = aiService.getCharacter("non-existent");
        assertNull(nonExistent);
    }

    @Test
    void testGetOrCreateSession() {
        Session session = aiService.getOrCreateSession("test-session-123", "scientist");
        assertNotNull(session);
        assertEquals("test-session-123", session.getSession_id());
        assertEquals("scientist", session.getCharacter_id());
        assertNotNull(session.getMessages());
        assertTrue(session.getMessages().isEmpty());

        Session existingSession = aiService.getOrCreateSession("test-session-123", "scientist");
        assertSame(session, existingSession);

        Session newSession = aiService.getOrCreateSession("test-session-456", "teacher");
        assertNotNull(newSession);
        assertEquals("test-session-456", newSession.getSession_id());
        assertEquals("teacher", newSession.getCharacter_id());
        assertNotSame(session, newSession);
    }

    @Test
    void testChatWithScientist() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("从科学的角度来看，这个问题涉及到多个领域的知识。");

        String response = aiService.chat("test-session-scientist", "scientist", "什么是物理学？");

        assertNotNull(response);
        assertTrue(response.contains("科学"));
        verify(deepSeekService, times(1)).chat(anyString(), anyList());
    }

    @Test
    void testChatWithTeacher() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("这个问题提得很好！让我们一起分析一下。");

        String response = aiService.chat("test-session-teacher", "teacher", "如何学习编程？");

        assertNotNull(response);
        assertTrue(response.contains("很好") || response.contains("分析"));
        verify(deepSeekService, times(1)).chat(anyString(), anyList());
    }

    @Test
    void testChatWithArtist() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("这个问题让我想到了一幅美丽的画面。");

        String response = aiService.chat("test-session-artist", "artist", "什么是艺术？");

        assertNotNull(response);
        assertTrue(response.contains("美丽") || response.contains("画面"));
        verify(deepSeekService, times(1)).chat(anyString(), anyList());
    }

    @Test
    void testChatWithCustomerService() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("您好！我是智能助手小i，很高兴为您服务。");

        String response = aiService.chat("test-session-cs", "customer_service", "你好");

        assertNotNull(response);
        assertTrue(response.contains("小i"));
        verify(deepSeekService, times(1)).chat(anyString(), anyList());
    }

    @Test
    void testChatWithInvalidCharacter() {
        assertThrows(IllegalArgumentException.class, () -> {
            aiService.chat("test-session-123", "non-existent", "你好");
        });
    }

    @Test
    void testChatSessionHistory() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("第一次回复。", "第二次回复。");

        aiService.chat("test-session-history", "scientist", "问题1");
        aiService.chat("test-session-history", "scientist", "问题2");

        Session session = aiService.getOrCreateSession("test-session-history", "scientist");
        List<Message> messages = session.getMessages();

        assertEquals(4, messages.size());
        assertEquals("user", messages.get(0).getRole());
        assertEquals("问题1", messages.get(0).getContent());
        assertEquals("assistant", messages.get(1).getRole());
        assertEquals("第一次回复。", messages.get(1).getContent());
        assertEquals("user", messages.get(2).getRole());
        assertEquals("问题2", messages.get(2).getContent());
        assertEquals("assistant", messages.get(3).getRole());
        assertEquals("第二次回复。", messages.get(3).getContent());
    }

    @Test
    void testChatWithEmptyMessage() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("好的。");

        String response = aiService.chat("test-session-empty", "scientist", "");

        assertNotNull(response);
        verify(deepSeekService, times(1)).chat(anyString(), anyList());
    }

    @Test
    void testStreamChat() {
        SseEmitter emitter = aiService.streamChat("test-stream-session", "scientist", "你好");
        assertNotNull(emitter);
    }

    @Test
    void testStreamChatWithInvalidCharacter() {
        SseEmitter emitter = aiService.streamChat("test-stream-invalid", "non-existent", "你好");
        assertNotNull(emitter);
    }

    @Test
    void testStreamChatWithEmptyMessage() {
        SseEmitter emitter = aiService.streamChat("test-stream-empty", "scientist", "");
        assertNotNull(emitter);
    }

    @Test
    void testGetCharacterReturnsCorrectPersona() {
        Character scientist = aiService.getCharacter("scientist");
        assertEquals("理性严谨，喜欢解释原理", scientist.getPersona());

        Character teacher = aiService.getCharacter("teacher");
        assertEquals("耐心细致，善于引导", teacher.getPersona());

        Character artist = aiService.getCharacter("artist");
        assertEquals("富有创意，情感丰富", artist.getPersona());
    }

    @Test
    void testGetCharacterReturnsCorrectSpeakingStyle() {
        Character scientist = aiService.getCharacter("scientist");
        assertEquals("逻辑清晰，偏长句", scientist.getSpeaking_style());

        Character teacher = aiService.getCharacter("teacher");
        assertEquals("温和亲切，鼓励式", teacher.getSpeaking_style());

        Character artist = aiService.getCharacter("artist");
        assertEquals("感性表达，富有想象力", artist.getSpeaking_style());
    }

    @Test
    void testSessionRecentMessagesLimit() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("回复");

        for (int i = 0; i < 15; i++) {
            aiService.chat("test-session-limit", "scientist", "问题" + i);
        }

        Session session = aiService.getOrCreateSession("test-session-limit", "scientist");
        List<Message> recentMessages = session.getRecentMessages(10);

        assertEquals(10, recentMessages.size());
    }

    @Test
    void testSessionRecentMessagesAll() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("回复");

        aiService.chat("test-session-all", "scientist", "问题1");
        aiService.chat("test-session-all", "scientist", "问题2");

        Session session = aiService.getOrCreateSession("test-session-all", "scientist");
        List<Message> recentMessages = session.getRecentMessages(100);

        assertEquals(4, recentMessages.size());
    }
}