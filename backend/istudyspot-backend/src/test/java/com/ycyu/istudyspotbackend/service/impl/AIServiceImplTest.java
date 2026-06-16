package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.AICharacter;
import com.ycyu.istudyspotbackend.entity.Message;
import com.ycyu.istudyspotbackend.entity.Session;
import com.ycyu.istudyspotbackend.service.DeepSeekService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AIServiceImplTest {

    @Mock
    private DeepSeekService deepSeekService;

    @InjectMocks
    private AIServiceImpl aiService;

    @Test
    void testGetAICharacters() {
        List<AICharacter> characters = aiService.getCharacters();
        assertNotNull(characters);
        assertFalse(characters.isEmpty());
        assertEquals(1, characters.size());
    }

    @Test
    void testGetCharacter() {
        AICharacter defaultChar = aiService.getCharacter("customer_service");
        assertNotNull(defaultChar);
        assertEquals("customer_service", defaultChar.getId());

        AICharacter nonExistent = aiService.getCharacter("non-existent");
        assertNotNull(nonExistent);
    }

    @Test
    void testGetOrCreateSession() {
        Session session = aiService.getOrCreateSession("test-session-123", "customer_service");
        assertNotNull(session);
        assertEquals("test-session-123", session.getSession_id());
    }

    @Test
    void testChatWithCustomerService() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("从科学的角度来看...");

        String response = aiService.chat("test-session-scientist", "customer_service", "什么是物理学？");

        assertNotNull(response);
        verify(deepSeekService, times(1)).chat(anyString(), anyList());
    }

    @Test
    void testChatWithInvalidAICharacter() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("回复");

        String response = aiService.chat("test-session-123", "non-existent", "你好");
        assertNotNull(response);
    }

    @Test
    void testChatSessionHistory() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("第一次回复。", "第二次回复。");

        aiService.chat("test-session-history", "customer_service", "问题1");
        aiService.chat("test-session-history", "customer_service", "问题2");

        Session session = aiService.getOrCreateSession("test-session-history", "customer_service");
        List<Message> messages = session.getMessages();

        assertEquals(4, messages.size());
        assertEquals("user", messages.get(0).getRole());
        assertEquals("问题1", messages.get(0).getContent());
    }

    @Test
    void testStreamChat() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = aiService.streamChat("test-stream-session", "customer_service", "你好");
        assertNotNull(emitter);

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(deepSeekService, times(1)).streamChat(anyString(), anyList(), any(), any(), any());
    }

    @Test
    void testStreamChatWithInvalidAICharacter() {
        SseEmitter emitter = aiService.streamChat("test-stream-invalid", "invalid-char", "你好");
        assertNotNull(emitter);
    }

    @Test
    void testStreamChatOnDataCallback() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            Consumer<String> onData = invocation.getArgument(2);
            onData.accept("test chunk");
            latch.countDown();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = aiService.streamChat("test-on-data", "customer_service", "你好");
        assertNotNull(emitter);

        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testStreamChatOnCompletion() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            Runnable onComplete = invocation.getArgument(3);
            onComplete.run();
            latch.countDown();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = aiService.streamChat("test-on-complete", "customer_service", "你好");
        assertNotNull(emitter);

        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testStreamChatOnErrorCallback() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            Consumer<Throwable> onError = invocation.getArgument(4);
            onError.accept(new RuntimeException("Test error"));
            latch.countDown();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = aiService.streamChat("test-on-error", "customer_service", "你好");
        assertNotNull(emitter);

        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testStreamChatInternalException() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            throw new RuntimeException("Internal error");
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = aiService.streamChat("test-internal-exception", "customer_service", "你好");
        assertNotNull(emitter);

        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testStreamChatWithNullSessionId() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = aiService.streamChat(null, "customer_service", "你好");
        assertNotNull(emitter);

        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testStreamChatWithNullMessage() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = aiService.streamChat("test-null-message", "customer_service", null);
        assertNotNull(emitter);

        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testChatWithNullSessionId() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("回复");

        String response = aiService.chat(null, "customer_service", "你好");
        assertNotNull(response);
    }

    @Test
    void testChatWithNullMessage() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("回复");

        String response = aiService.chat("test-null", "customer_service", null);
        assertNotNull(response);
    }

    @Test
    void testGetCharacterReturnsCorrectPersona() {
        AICharacter customerService = aiService.getCharacter("customer_service");
        assertEquals("友好、谨慎，并严格依据应用规则回答。", customerService.getPersona());
    }

    @Test
    void testGetCharacterReturnsCorrectSpeakingStyle() {
        AICharacter customerService = aiService.getCharacter("customer_service");
        assertEquals("简洁实用。", customerService.getSpeaking_style());
    }

    @Test
    void testSessionRecentMessagesLimit() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("回复");

        for (int i = 0; i < 15; i++) {
            aiService.chat("test-session-limit", "customer_service", "问题" + i);
        }

        Session session = aiService.getOrCreateSession("test-session-limit", "customer_service");
        List<Message> recentMessages = session.getRecentMessages(10);

        assertEquals(10, recentMessages.size());
    }

    @Test
    void testMultipleSessions() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("回复");

        aiService.chat("session-a", "customer_service", "问题A");
        aiService.chat("session-b", "customer_service", "问题B");

        Session sessionA = aiService.getOrCreateSession("session-a", "customer_service");
        Session sessionB = aiService.getOrCreateSession("session-b", "customer_service");

        assertNotSame(sessionA, sessionB);
    }

    @Test
    void testChatWithEmptyMessage() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("好的。");

        String response = aiService.chat("test-empty", "customer_service", "");
        assertNotNull(response);
    }

    @Test
    void testGetOrCreateSessionWithNullSessionId() {
        Session session = aiService.getOrCreateSession(null, "customer_service");
        assertNotNull(session);
        assertNull(session.getSession_id());
    }

    @Test
    void testChatMessageBuilding() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("回复");

        aiService.chat("test-message-build", "customer_service", "问题1");

        Session session = aiService.getOrCreateSession("test-message-build", "customer_service");
        List<Message> messages = session.getMessages();

        assertEquals(2, messages.size());
        assertEquals("user", messages.get(0).getRole());
        assertEquals("问题1", messages.get(0).getContent());
    }

    @Test
    void testStreamChatWithAllAICharacters() {
        String[] characterIds = {"customer_service"};

        for (String characterId : characterIds) {
            SseEmitter emitter = aiService.streamChat("test-stream-" + characterId, characterId, "你好");
            assertNotNull(emitter);
        }
    }

    @Test
    void testCharacterListImmutability() {
        List<AICharacter> characters = aiService.getCharacters();

        assertThrows(UnsupportedOperationException.class, () -> {
            characters.add(new AICharacter("new-char", "新角色", "性格", "风格"));
        });
    }

    @Test
    void testBuildSystemPrompt() {
        AICharacter customerService = aiService.getCharacter("customer_service");
        assertNotNull(customerService);
        assertNotNull(customerService.getPersona());
        assertNotNull(customerService.getSpeaking_style());
    }

    @Test
    void testChatWithCustomerServiceCharacter() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("这个问题提得很好！");

        String response = aiService.chat("test-cs", "customer_service", "如何学习编程？");

        assertNotNull(response);
        assertTrue(response.contains("很好"));
        verify(deepSeekService, times(1)).chat(anyString(), anyList());
    }

    @Test
    void testChatWithCustomerServiceReply() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("您好！我是智能助手小i。");

        String response = aiService.chat("test-cs2", "customer_service", "你好");

        assertNotNull(response);
        assertTrue(response.contains("小i"));
    }

    @Test
    void testStreamChatWithNullAICharacterId() {
        SseEmitter emitter = aiService.streamChat("test-null-char", null, "你好");
        assertNotNull(emitter);
    }

    @Test
    void testStreamChatWithEmptyAICharacterId() {
        SseEmitter emitter = aiService.streamChat("test-empty-char", "", "你好");
        assertNotNull(emitter);
    }
}