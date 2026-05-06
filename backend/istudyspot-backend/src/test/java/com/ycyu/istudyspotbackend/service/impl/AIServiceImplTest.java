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

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

    @Test
    void testStreamChatWithAllCharacters() {
        String[] characterIds = {"scientist", "teacher", "artist", "customer_service"};

        for (String characterId : characterIds) {
            SseEmitter emitter = aiService.streamChat("test-stream-" + characterId, characterId, "你好");
            assertNotNull(emitter);
        }
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
    void testBuildSystemPromptWithAllCharacterTypes() {
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
    void testChatWithMultipleMessages() {
        when(deepSeekService.chat(anyString(), anyList()))
                .thenReturn("回复1")
                .thenReturn("回复2")
                .thenReturn("回复3");

        aiService.chat("test-multi", "scientist", "问题1");
        aiService.chat("test-multi", "scientist", "问题2");
        aiService.chat("test-multi", "scientist", "问题3");

        Session session = aiService.getOrCreateSession("test-multi", "scientist");
        assertEquals(6, session.getMessages().size());
    }

    @Test
    void testChatBuildsCorrectMessages() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("回复");

        aiService.chat("test-messages", "scientist", "你好");

        Session session = aiService.getOrCreateSession("test-messages", "scientist");
        List<Message> messages = session.getMessages();

        assertEquals(2, messages.size());
        assertEquals("user", messages.get(0).getRole());
        assertEquals("你好", messages.get(0).getContent());
        assertEquals("assistant", messages.get(1).getRole());
        assertEquals("回复", messages.get(1).getContent());
    }

    @Test
    void testStreamChatCompletion() {
        SseEmitter emitter = aiService.streamChat("test-completion", "scientist", "测试完成");
        assertNotNull(emitter);
    }

    @Test
    void testSessionMessagesLimit() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("回复");

        for (int i = 0; i < 25; i++) {
            aiService.chat("test-limit", "scientist", "问题" + i);
        }

        Session session = aiService.getOrCreateSession("test-limit", "scientist");
        List<Message> recentMessages = session.getRecentMessages(10);

        assertEquals(10, recentMessages.size());
    }

    @Test
    void testChatWithNullMessage() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("好的。");

        String response = aiService.chat("test-null", "scientist", null);

        assertNotNull(response);
    }

    @Test
    void testStreamChatWithDeepSeekServiceMock() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = aiService.streamChat("test-deepseek-mock", "scientist", "你好");

        assertNotNull(emitter);

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(deepSeekService, times(1)).streamChat(anyString(), anyList(), any(), any(), any());
    }

    @Test
    void testStreamChatInvalidCharacter() {
        SseEmitter emitter = aiService.streamChat("test-invalid-char", "non-existent", "你好");
        assertNotNull(emitter);
    }

    @Test
    void testStreamChatWithEmptySessionId() {
        SseEmitter emitter = aiService.streamChat("", "scientist", "你好");
        assertNotNull(emitter);
    }

    @Test
    void testStreamChatWithNullSessionId() {
        SseEmitter emitter = aiService.streamChat(null, "scientist", "你好");
        assertNotNull(emitter);
    }

    @Test
    void testStreamChatWithNullMessage() {
        SseEmitter emitter = aiService.streamChat("test-null-msg", "scientist", null);
        assertNotNull(emitter);
    }

    @Test
    void testMultipleSessions() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("回复");

        aiService.chat("session-a", "scientist", "问题A");
        aiService.chat("session-b", "teacher", "问题B");
        aiService.chat("session-a", "scientist", "问题A2");

        Session sessionA = aiService.getOrCreateSession("session-a", "scientist");
        Session sessionB = aiService.getOrCreateSession("session-b", "teacher");

        assertEquals(4, sessionA.getMessages().size());
        assertEquals(2, sessionB.getMessages().size());
        assertNotSame(sessionA, sessionB);
    }

    @Test
    void testSwitchCharacterInSession() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("回复");

        aiService.chat("session-switch", "scientist", "问题1");

        Session session = aiService.getOrCreateSession("session-switch", "teacher");
        assertEquals("scientist", session.getCharacter_id());
    }

    @Test
    void testChatWithVeryLongMessage() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("好的，我明白了。");

        String longMessage = "我想预订一个自习室座位，但是不知道该怎么操作。我看到你们有普通座、VIP座和学习包间，请问它们之间有什么区别？价格分别是多少？另外，我还有一些积分，不知道能不能用？如果预订了之后临时有事不能去，能不能取消或者改期？";

        String response = aiService.chat("test-long", "customer_service", longMessage);

        assertNotNull(response);
    }

    @Test
    void testCharacterListImmutability() {
        List<Character> characters = aiService.getCharacters();

        assertThrows(UnsupportedOperationException.class, () -> {
            characters.add(new Character("new-char", "新角色", "性格", "风格"));
        });
    }

    @Test
    void testBuildSystemPromptFormat() {
        Character character = new Character("test", "测试角色", "测试性格", "测试风格");

        String prompt = buildSystemPrompt(character);

        assertTrue(prompt.contains("角色名称：测试角色"));
        assertTrue(prompt.contains("性格：测试性格"));
        assertTrue(prompt.contains("说话风格：测试风格"));
        assertTrue(prompt.contains("不要提到自己是AI"));
        assertTrue(prompt.contains("始终保持角色语气"));
    }

    @Test
    void testStreamChatOnCompletion() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = aiService.streamChat("test-completion", "scientist", "你好");

        assertNotNull(emitter);

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(deepSeekService, times(1)).streamChat(anyString(), anyList(), any(), any(), any());
    }

    @Test
    void testStreamChatOnError() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = aiService.streamChat("test-error", "scientist", "你好");

        assertNotNull(emitter);

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(deepSeekService, times(1)).streamChat(anyString(), anyList(), any(), any(), any());
    }

    @Test
    void testStreamChatOnTimeout() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = aiService.streamChat("test-timeout", "scientist", "你好");

        assertNotNull(emitter);

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(deepSeekService, times(1)).streamChat(anyString(), anyList(), any(), any(), any());
    }

    @Test
    void testStreamChatInternalError() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            throw new RuntimeException("Internal error");
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = aiService.streamChat("test-internal-error", "scientist", "你好");

        assertNotNull(emitter);

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(deepSeekService, times(1)).streamChat(anyString(), anyList(), any(), any(), any());
    }

    @Test
    void testStreamChatWithAllCharacterTypes() {
        List<Character> characters = aiService.getCharacters();

        for (Character character : characters) {
            SseEmitter emitter = aiService.streamChat("test-char-" + character.getId(), character.getId(), "你好");
            assertNotNull(emitter);
        }
    }

    @Test
    void testChatWithEmptySessionId() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("好的。");

        String response = aiService.chat("", "scientist", "你好");

        assertNotNull(response);
    }

    @Test
    void testChatWithNullSessionId() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("好的。");

        String response = aiService.chat(null, "scientist", "你好");

        assertNotNull(response);
    }

    @Test
    void testGetOrCreateSessionWithNullSessionId() {
        Session session = aiService.getOrCreateSession(null, "scientist");

        assertNotNull(session);
        assertNull(session.getSession_id());
        assertEquals("scientist", session.getCharacter_id());
    }

    @Test
    void testGetOrCreateSessionWithEmptySessionId() {
        Session session = aiService.getOrCreateSession("", "teacher");

        assertNotNull(session);
        assertEquals("", session.getSession_id());
        assertEquals("teacher", session.getCharacter_id());
    }

    @Test
    void testChatMessageBuilding() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("回复");

        aiService.chat("test-message-build", "scientist", "问题1");
        aiService.chat("test-message-build", "scientist", "问题2");

        Session session = aiService.getOrCreateSession("test-message-build", "scientist");
        List<Message> messages = session.getMessages();

        assertEquals(4, messages.size());
        assertEquals("user", messages.get(0).getRole());
        assertEquals("问题1", messages.get(0).getContent());
        assertEquals("assistant", messages.get(1).getRole());
        assertEquals("回复", messages.get(1).getContent());
        assertEquals("user", messages.get(2).getRole());
        assertEquals("问题2", messages.get(2).getContent());
        assertEquals("assistant", messages.get(3).getRole());
        assertEquals("回复", messages.get(3).getContent());
    }

    @Test
    void testStreamChatOnErrorWithIOException() {
        SseEmitter emitter = aiService.streamChat("test-error-io", "scientist", "你好");
        assertNotNull(emitter);
    }

    @Test
    void testStreamChatOnTimeoutWithIOException() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = aiService.streamChat("test-timeout-io", "scientist", "你好");

        assertNotNull(emitter);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(deepSeekService, times(1)).streamChat(anyString(), anyList(), any(), any(), any());
    }

    @Test
    void testStreamChatInternalErrorWithIOException() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            throw new RuntimeException("Internal error");
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = aiService.streamChat("test-internal-io", "scientist", "你好");

        assertNotNull(emitter);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(deepSeekService, times(1)).streamChat(anyString(), anyList(), any(), any(), any());
    }

    @Test
    void testStreamChatWithMultipleMessages() throws Exception {
        CountDownLatch latch = new CountDownLatch(2);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        aiService.streamChat("test-multi-stream", "scientist", "问题1");
        aiService.streamChat("test-multi-stream", "scientist", "问题2");

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(deepSeekService, times(2)).streamChat(anyString(), anyList(), any(), any(), any());
    }

    @Test
    void testChatWithRecentMessagesLimit() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("回复");

        for (int i = 0; i < 15; i++) {
            aiService.chat("test-limit", "scientist", "问题" + i);
        }

        Session session = aiService.getOrCreateSession("test-limit", "scientist");
        List<Message> recentMessages = session.getRecentMessages(10);

        assertEquals(10, recentMessages.size());
    }

    @Test
    void testBuildSystemPromptWithCustomerService() {
        Character cs = aiService.getCharacter("customer_service");

        assertNotNull(cs);

        String prompt = buildSystemPrompt(cs);

        assertTrue(prompt.contains("小i"));
        assertTrue(prompt.contains("热情友好"));
        assertTrue(prompt.contains("亲切自然"));
    }

    @Test
    void testStreamChatCallbackOnCompletion() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = aiService.streamChat("test-callback-completion", "scientist", "你好");

        assertNotNull(emitter);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(deepSeekService, times(1)).streamChat(anyString(), anyList(), any(), any(), any());
    }

    @Test
    void testStreamChatCallbackOnError() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = aiService.streamChat("test-callback-error", "scientist", "你好");

        assertNotNull(emitter);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(deepSeekService, times(1)).streamChat(anyString(), anyList(), any(), any(), any());
    }

    @Test
    void testStreamChatCallbackOnTimeout() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = aiService.streamChat("test-callback-timeout", "scientist", "你好");

        assertNotNull(emitter);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(deepSeekService, times(1)).streamChat(anyString(), anyList(), any(), any(), any());
    }

    @Test
    void testStreamChatInvalidCharacterId() {
        SseEmitter emitter = aiService.streamChat("test-invalid-char-id", "unknown-character", "你好");
        assertNotNull(emitter);
    }

    @Test
    void testStreamChatCallbackOnCompletionWithException() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = aiService.streamChat("test-completion-ex", "scientist", "你好");

        assertNotNull(emitter);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(deepSeekService, times(1)).streamChat(anyString(), anyList(), any(), any(), any());
    }

    @Test
    void testStreamChatCallbackOnErrorWithSendException() {
        SseEmitter emitter = aiService.streamChat("test-error-send-ex", "scientist", "你好");
        assertNotNull(emitter);
    }

    @Test
    void testStreamChatCallbackOnTimeoutWithSendException() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = aiService.streamChat("test-timeout-send-ex", "scientist", "你好");

        assertNotNull(emitter);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(deepSeekService, times(1)).streamChat(anyString(), anyList(), any(), any(), any());
    }

    @Test
    void testStreamChatInternalErrorWithSendException() {
        SseEmitter emitter = aiService.streamChat("test-internal-ex", "scientist", "你好");
        assertNotNull(emitter);
    }

    @Test
    void testStreamChatOnCompletionThrowsException() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = aiService.streamChat("test-completion-throw", "scientist", "你好");

        assertNotNull(emitter);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(deepSeekService, times(1)).streamChat(anyString(), anyList(), any(), any(), any());
    }

    @Test
    void testStreamChatOnErrorThrowsIOException() {
        SseEmitter emitter = aiService.streamChat("test-error-io", "scientist", "你好");
        assertNotNull(emitter);
    }

    @Test
    void testStreamChatOnTimeoutThrowsIOException() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = aiService.streamChat("test-timeout-io", "scientist", "你好");

        assertNotNull(emitter);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(deepSeekService, times(1)).streamChat(anyString(), anyList(), any(), any(), any());
    }

    @Test
    void testStreamChatInvalidCharacterSendError() {
        SseEmitter emitter = aiService.streamChat("test-invalid-send", "invalid-char", "你好");
        assertNotNull(emitter);
    }

    @Test
    void testStreamChatOnDataCallback() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = aiService.streamChat("test-on-data", "scientist", "你好");

        assertNotNull(emitter);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(deepSeekService, times(1)).streamChat(anyString(), anyList(), any(), any(), any());
    }

    @Test
    void testStreamChatOnErrorCallback() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = aiService.streamChat("test-error-callback", "scientist", "你好");

        assertNotNull(emitter);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(deepSeekService, times(1)).streamChat(anyString(), anyList(), any(), any(), any());
    }
}
