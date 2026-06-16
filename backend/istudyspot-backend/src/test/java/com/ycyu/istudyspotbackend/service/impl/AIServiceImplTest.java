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
        AICharacter cs = aiService.getCharacter("customer_service");
        assertNotNull(cs);
        assertEquals("customer_service", cs.getId());

        AICharacter nonExistent = aiService.getCharacter("non-existent");
        assertNotNull(nonExistent);
        assertEquals("customer_service", nonExistent.getId());
    }

    @Test
    void testGetOrCreateSession() {
        Session session = aiService.getOrCreateSession("test-session-123", "customer_service");
        assertNotNull(session);
        assertEquals("test-session-123", session.getSession_id());
    }

    @Test
    void testChatWithCustomerService() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("您好！我是iStudySpot智能助手。");

        String response = aiService.chat("test-session-cs", "customer_service", "什么是自习室？");

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
    void testStreamChatOnErrorCallbackDuplicate() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            Consumer<Throwable> onError = invocation.getArgument(4);
            onError.accept(new RuntimeException("Test error"));
            latch.countDown();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = aiService.streamChat("test-on-error-callback", "customer_service", "你好");
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
        AICharacter cs = aiService.getCharacter("customer_service");
        assertNotNull(cs.getPersona());
        assertFalse(cs.getPersona().isEmpty());
    }

    @Test
    void testGetCharacterReturnsCorrectSpeakingStyle() {
        AICharacter cs = aiService.getCharacter("customer_service");
        assertNotNull(cs.getSpeaking_style());
        assertFalse(cs.getSpeaking_style().isEmpty());
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
        AICharacter cs = new AICharacter("customer_service", "iStudySpot 智能助手", "友好", "简洁");
        String prompt = buildSystemPrompt(cs);

        assertNotNull(prompt);
        assertTrue(prompt.contains("角色名称：iStudySpot 智能助手"));
        assertTrue(prompt.contains("性格：友好"));
        assertTrue(prompt.contains("说话风格：简洁"));
    }

    @Test
    void testBuildSystemPromptAllCharacters() {
        List<AICharacter> characters = aiService.getCharacters();
        for (AICharacter character : characters) {
            String prompt = buildSystemPrompt(character);
            assertNotNull(prompt);
            assertTrue(prompt.contains(character.getName()));
            assertTrue(prompt.contains(character.getPersona()));
            assertTrue(prompt.contains(character.getSpeaking_style()));
        }
    }

    private String buildSystemPrompt(AICharacter character) {
        return "你正在扮演一个角色，请严格遵守以下设定：\n" +
                "\n" +
                "角色名称：" + character.getName() + "\n" +
                "性格：" + character.getPersona() + "\n" +
                "说话风格：" + character.getSpeaking_style() + "\n" +
                "\n" +
                "要求：\n" +
                "- 始终保持角色语气\n" +
                "- 不要提到自己是AI\n" +
                "- 不要跳出角色\n" +
                "- 回答要友好、有帮助\n" +
                "\n" +
                "请根据对话继续交流。";
    }

    @Test
    void testChatWithCustomerServiceContainsName() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("您好！我是iStudySpot智能助手。");

        String response = aiService.chat("test-cs2", "customer_service", "你好");

        assertNotNull(response);
        assertTrue(response.contains("iStudySpot"));
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
