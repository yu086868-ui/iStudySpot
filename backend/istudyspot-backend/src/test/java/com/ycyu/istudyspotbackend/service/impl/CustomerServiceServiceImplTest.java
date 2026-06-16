package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.CustomerServiceMessage;
import com.ycyu.istudyspotbackend.service.DeepSeekService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceServiceImplTest {

    @Mock
    private DeepSeekService deepSeekService;

    @InjectMocks
    private CustomerServiceServiceImpl customerServiceService;

    @Test
    void testGetWelcomeMessage() {
        String welcomeMessage = customerServiceService.getWelcomeMessage();

        assertNotNull(welcomeMessage);
        assertFalse(welcomeMessage.isEmpty());
        assertTrue(welcomeMessage.contains("iStudySpot"));
        assertTrue(welcomeMessage.contains("智能助手"));
    }

    @Test
    void testGetRecommendedQuestions() {
        List<String> recommendedQuestions = customerServiceService.getRecommendedQuestions();

        assertNotNull(recommendedQuestions);
        assertFalse(recommendedQuestions.isEmpty());
        assertEquals(4, recommendedQuestions.size());
    }

    @Test
    void testChatWithCustomerService() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("您好，我来协助您。");

        String response = customerServiceService.chatWithCustomerService("test-session-123", "你好");

        assertEquals("您好，我来协助您。", response);
        verify(deepSeekService, times(1)).chat(anyString(), anyList());
    }

    @Test
    void testChatWithCustomerServiceMultipleMessages() {
        when(deepSeekService.chat(anyString(), anyList()))
                .thenReturn("第一次回复")
                .thenReturn("第二次回复");

        String response1 = customerServiceService.chatWithCustomerService("test-session-multi", "问题1");
        String response2 = customerServiceService.chatWithCustomerService("test-session-multi", "问题2");

        assertEquals("第一次回复", response1);
        assertEquals("第二次回复", response2);
        verify(deepSeekService, times(2)).chat(anyString(), anyList());
    }

    @Test
    void testChatWithCustomerServiceEmptyMessage() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("好的");

        String response = customerServiceService.chatWithCustomerService("test-session-empty", "");

        assertNotNull(response);
        verify(deepSeekService).chat(anyString(), anyList());
    }

    @Test
    void testChatWithCustomerServiceNullMessage() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("好的");

        String response = customerServiceService.chatWithCustomerService("test-session-null", null);

        assertNotNull(response);
        verify(deepSeekService).chat(anyString(), anyList());
    }

    @Test
    void testChatWithCustomerServiceNullSessionId() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("您好");

        String response = customerServiceService.chatWithCustomerService(null, "你好");

        assertNotNull(response);
        verify(deepSeekService).chat(anyString(), anyList());
    }

    @Test
    void testChatUsesFallbackWhenModelReturnsErrorPrefix() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("Error: timeout");

        String response = customerServiceService.chatWithCustomerService("test-fallback", "你好");

        assertNotNull(response);
        assertFalse(response.startsWith("Error"));
    }

    @Test
    void testGetSessionHistory() {
        List<CustomerServiceMessage> history = customerServiceService.getSessionHistory("test-session-123");

        assertNotNull(history);
        assertTrue(history.isEmpty());
    }

    @Test
    void testGetSessionHistoryAfterChat() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("回复");

        customerServiceService.chatWithCustomerService("test-session-history", "你好");

        List<CustomerServiceMessage> history = customerServiceService.getSessionHistory("test-session-history");

        assertEquals(2, history.size());
        assertEquals("user", history.get(0).getRole());
        assertEquals("assistant", history.get(1).getRole());
    }

    @Test
    void testGetSessionHistoryWithMultipleMessages() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("回复1").thenReturn("回复2");

        customerServiceService.chatWithCustomerService("test-session-multi-history", "问题1");
        customerServiceService.chatWithCustomerService("test-session-multi-history", "问题2");

        List<CustomerServiceMessage> history = customerServiceService.getSessionHistory("test-session-multi-history");
        assertEquals(4, history.size());
    }

    @Test
    void testStreamChatWithCustomerService() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = customerServiceService.streamChatWithCustomerService("test-stream-session", "你好");

        assertNotNull(emitter);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        verify(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());
    }

    @Test
    void testStreamChatWithCustomerServiceEmptyMessage() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = customerServiceService.streamChatWithCustomerService("test-stream-empty", "");

        assertNotNull(emitter);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testStreamChatWithCustomerServiceNullSessionId() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = customerServiceService.streamChatWithCustomerService(null, "你好");

        assertNotNull(emitter);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testGetSessionHistoryNonExistentSession() {
        List<CustomerServiceMessage> history = customerServiceService.getSessionHistory(UUID.randomUUID().toString());

        assertNotNull(history);
        assertTrue(history.isEmpty());
    }

    @Test
    void testChatWithCustomerServiceChineseCharacters() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("好的，我来帮您解答。");

        String response = customerServiceService.chatWithCustomerService("test-chinese", "如何预约座位？");

        assertNotNull(response);
        assertTrue(response.contains("好的"));
    }

    @Test
    void testChatWithCustomerServiceSpecialCharacters() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("没问题！");

        String response = customerServiceService.chatWithCustomerService("test-special", "价格是多少？@#$%");

        assertNotNull(response);
    }

    @Test
    void testStreamChatWithCustomerServiceOnDataCallback() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            Consumer<String> onData = invocation.getArgument(2);
            onData.accept("test data");
            latch.countDown();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = customerServiceService.streamChatWithCustomerService("test-stream-data", "你好");
        assertNotNull(emitter);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testStreamChatWithCustomerServiceOnComplete() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            Runnable onComplete = invocation.getArgument(3);
            onComplete.run();
            latch.countDown();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = customerServiceService.streamChatWithCustomerService("test-stream-complete", "你好");
        assertNotNull(emitter);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testStreamChatWithCustomerServiceOnError() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            Consumer<Throwable> onError = invocation.getArgument(4);
            onError.accept(new RuntimeException("Test error"));
            latch.countDown();
            return null;
        }).when(deepSeekService).streamChat(anyString(), anyList(), any(), any(), any());

        SseEmitter emitter = customerServiceService.streamChatWithCustomerService("test-stream-error", "你好");
        assertNotNull(emitter);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testBuildMessagesWithHistory() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("回复");

        customerServiceService.chatWithCustomerService("test-build-messages", "问题1");
        customerServiceService.chatWithCustomerService("test-build-messages", "问题2");

        List<CustomerServiceMessage> history = customerServiceService.getSessionHistory("test-build-messages");
        assertEquals(4, history.size());
    }

    @Test
    void testSaveMessageWithNullSessionId() {
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("回复");

        customerServiceService.chatWithCustomerService(null, "测试消息");

        List<CustomerServiceMessage> history = customerServiceService.getSessionHistory(null);
        assertEquals(2, history.size());
    }

    @Test
    void testRecommendedQuestionsImmutability() {
        List<String> questions = customerServiceService.getRecommendedQuestions();

        assertThrows(UnsupportedOperationException.class, () -> questions.add("新问题"));
    }
}
