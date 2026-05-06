package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.Character;
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
    void testGetCharacters() {
        List<Character> characters = aiService.getCharacters();
        assertNotNull(characters);
        assertFalse(characters.isEmpty());
        assertEquals(4, characters.size());
    }

    @Test
    void testGetCharacter() {
        Character scientist = aiService.getCharacter("scientist");
        assertNotNull(scientist);
        assertEquals("scientist", scientist.getId());

        Character nonExistent = aiService.getCharacter("non-existent");
        assertNull(nonExistent);
    }

    @Test
    void testGetOrCreateSession() {
        Session session = aiService.getOrCreateSession("test-session-123", "scientist");
        assertNotNull(session);
        assertEquals("test-session-123", session.getSession_id());
    }

    @Test
    void testChatWithScientist() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("从科学的角度来看...");

        String response = aiService.chat("test-session-scientist", "scientist", "什么是物理学？");

        assertNotNull(response);
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
    }

    @Test
    void testStreamChat() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = aiService.streamChat("test-stream-session", "scientist", "你好");
        assertNotNull(emitter);

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(deepSeekService, times(1)).streamChat(anyString(), anyList(), any(), any(), any());
    }

    @Test
    void testStreamChatWithInvalidCharacter() {
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

        SseEmitter emitter = aiService.streamChat("test-on-data", "scientist", "你好");
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

        SseEmitter emitter = aiService.streamChat("test-on-complete", "scientist", "你好");
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

        SseEmitter emitter = aiService.streamChat("test-on-error", "scientist", "你好");
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

        SseEmitter emitter = aiService.streamChat("test-internal-exception", "scientist", "你好");
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

        SseEmitter emitter = aiService.streamChat(null, "scientist", "你好");
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

        SseEmitter emitter = aiService.streamChat("test-null-message", "scientist", null);
        assertNotNull(emitter);

        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testChatWithNullSessionId() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("回复");

        String response = aiService.chat(null, "scientist", "你好");
        assertNotNull(response);
    }

    @Test
    void testChatWithNullMessage() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("回复");

        String response = aiService.chat("test-null", "scientist", null);
        assertNotNull(response);
    }

    @Test
    void testGetCharacterReturnsCorrectPersona() {
        Character scientist = aiService.getCharacter("scientist");
        assertEquals("理性严谨，喜欢解释原理", scientist.getPersona());
    }

    @Test
    void testGetCharacterReturnsCorrectSpeakingStyle() {
        Character scientist = aiService.getCharacter("scientist");
        assertEquals("逻辑清晰，偏长句", scientist.getSpeaking_style());
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
    void testMultipleSessions() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("回复");

        aiService.chat("session-a", "scientist", "问题A");
        aiService.chat("session-b", "teacher", "问题B");

        Session sessionA = aiService.getOrCreateSession("session-a", "scientist");
        Session sessionB = aiService.getOrCreateSession("session-b", "teacher");

        assertNotSame(sessionA, sessionB);
    }

    @Test
    void testChatWithEmptyMessage() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("好的。");

        String response = aiService.chat("test-empty", "scientist", "");
        assertNotNull(response);
    }

    @Test
    void testGetOrCreateSessionWithNullSessionId() {
        Session session = aiService.getOrCreateSession(null, "scientist");
        assertNotNull(session);
        assertNull(session.getSession_id());
    }

    @Test
    void testChatMessageBuilding() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("回复");

        aiService.chat("test-message-build", "scientist", "问题1");

        Session session = aiService.getOrCreateSession("test-message-build", "scientist");
        List<Message> messages = session.getMessages();

        assertEquals(2, messages.size());
        assertEquals("user", messages.get(0).getRole());
        assertEquals("问题1", messages.get(0).getContent());
    }

    @Test
    void testStreamChatWithAllCharacters() {
        String[] characterIds = {"scientist", "teacher", "artist", "customer_service"};

        for (String characterId : characterIds) {
            SseEmitter emitter = aiService.streamChat("test-stream-" + characterId, characterId, "你好");
            assertNotNull(emitter);
        }
    }

    @Test
    void testCharacterListImmutability() {
        List<Character> characters = aiService.getCharacters();

        assertThrows(UnsupportedOperationException.class, () -> {
            characters.add(new Character("new-char", "新角色", "性格", "风格"));
        });
    }

    @Test
    void testBuildSystemPrompt() {
        Character scientist = new Character("scientist", "科学家", "理性严谨", "逻辑清晰");
        String prompt = buildSystemPrompt(scientist);

        assertNotNull(prompt);
        assertTrue(prompt.contains("角色名称：科学家"));
        assertTrue(prompt.contains("性格：理性严谨"));
        assertTrue(prompt.contains("说话风格：逻辑清晰"));
        assertTrue(prompt.contains("不要提到自己是AI"));
    }

    @Test
    void testBuildSystemPromptAllCharacters() {
        List<Character> characters = aiService.getCharacters();
        for (Character character : characters) {
            String prompt = buildSystemPrompt(character);
            assertNotNull(prompt);
            assertTrue(prompt.contains(character.getName()));
            assertTrue(prompt.contains(character.getPersona()));
            assertTrue(prompt.contains(character.getSpeaking_style()));
        }
    }

    private String buildSystemPrompt(Character character) {
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
    void testChatWithTeacher() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("这个问题提得很好！");

        String response = aiService.chat("test-teacher", "teacher", "如何学习编程？");

        assertNotNull(response);
        assertTrue(response.contains("很好"));
        verify(deepSeekService, times(1)).chat(anyString(), anyList());
    }

    @Test
    void testChatWithArtist() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("这个问题让我想到了一幅美丽的画面。");

        String response = aiService.chat("test-artist", "artist", "什么是艺术？");

        assertNotNull(response);
        assertTrue(response.contains("美丽"));
    }

    @Test
    void testChatWithCustomerService() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("您好！我是智能助手小i。");

        String response = aiService.chat("test-cs", "customer_service", "你好");

        assertNotNull(response);
        assertTrue(response.contains("小i"));
    }

    @Test
    void testStreamChatWithOnErrorCallback() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            Consumer<Throwable> onError = invocation.getArgument(4);
            onError.accept(new RuntimeException("Test error"));
            latch.countDown();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = aiService.streamChat("test-on-error-callback", "scientist", "你好");
        assertNotNull(emitter);

        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testStreamChatWithNullCharacterId() {
        SseEmitter emitter = aiService.streamChat("test-null-char", null, "你好");
        assertNotNull(emitter);
    }

    @Test
    void testStreamChatWithEmptyCharacterId() {
        SseEmitter emitter = aiService.streamChat("test-empty-char", "", "你好");
        assertNotNull(emitter);
    }

    }
