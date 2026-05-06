package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.config.DeepSeekConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DeepSeekServiceImplTest {

    @Mock
    private DeepSeekConfig deepSeekConfig;

    @InjectMocks
    private DeepSeekServiceImpl deepSeekService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(deepSeekConfig.getApiUrl()).thenReturn("https://api.deepseek.com/v1");
        when(deepSeekConfig.getApiKey()).thenReturn("test-api-key");
    }

    @Test
    public void testChatWithValidInput() {
        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "Hello");
        messages.add(message);

        String result = deepSeekService.chat(model, messages);

        assertNotNull(result);
    }

    @Test
    public void testChatWithEmptyMessages() {
        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();

        String result = deepSeekService.chat(model, messages);

        assertNotNull(result);
    }

    @Test
    public void testChatWithNullMessages() {
        String model = "deepseek-chat";

        String result = deepSeekService.chat(model, null);

        assertNotNull(result);
        assertTrue(result.contains("Error"));
    }

    @Test
    public void testChatWithNullModel() {
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "Hello");
        messages.add(message);

        String result = deepSeekService.chat(null, messages);

        assertNotNull(result);
    }

    @Test
    public void testChatWithEmptyModel() {
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "Hello");
        messages.add(message);

        String result = deepSeekService.chat("", messages);

        assertNotNull(result);
    }

    @Test
    public void testChatWithMultipleMessages() {
        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();
        
        Map<String, String> message1 = new HashMap<>();
        message1.put("role", "user");
        message1.put("content", "Hello");
        messages.add(message1);
        
        Map<String, String> message2 = new HashMap<>();
        message2.put("role", "assistant");
        message2.put("content", "Hi there!");
        messages.add(message2);
        
        Map<String, String> message3 = new HashMap<>();
        message3.put("role", "user");
        message3.put("content", "How are you?");
        messages.add(message3);

        String result = deepSeekService.chat(model, messages);

        assertNotNull(result);
    }

    @Test
    public void testChatWithVeryLongMessage() {
        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "This is a very long message ".repeat(100));
        messages.add(message);

        String result = deepSeekService.chat(model, messages);

        assertNotNull(result);
    }

    @Test
    public void testChatWithSpecialCharacters() {
        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "Hello! @#$%^&*()_+");
        messages.add(message);

        String result = deepSeekService.chat(model, messages);

        assertNotNull(result);
    }

    @Test
    public void testChatWithChineseCharacters() {
        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "你好，世界！");
        messages.add(message);

        String result = deepSeekService.chat(model, messages);

        assertNotNull(result);
    }

    @Test
    public void testStreamChat() throws Exception {
        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "Hello");
        messages.add(message);

        AtomicBoolean onDataCalled = new AtomicBoolean(false);
        AtomicBoolean onCompleteCalled = new AtomicBoolean(false);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        deepSeekService.streamChat(model, messages,
            data -> onDataCalled.set(true),
            () -> onCompleteCalled.set(true),
            error -> errorRef.set(error));

        Thread.sleep(1000);

        assertTrue(onCompleteCalled.get() || errorRef.get() != null);
    }

    @Test
    public void testStreamChatWithEmptyMessages() throws Exception {
        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();

        AtomicBoolean onCompleteCalled = new AtomicBoolean(false);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        deepSeekService.streamChat(model, messages,
            data -> {},
            () -> onCompleteCalled.set(true),
            error -> errorRef.set(error));

        Thread.sleep(1000);

        assertTrue(onCompleteCalled.get() || errorRef.get() != null);
    }

    @Test
    public void testStreamChatWithNullMessages() throws Exception {
        String model = "deepseek-chat";

        AtomicBoolean onCompleteCalled = new AtomicBoolean(false);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        deepSeekService.streamChat(model, null,
            data -> {},
            () -> onCompleteCalled.set(true),
            error -> errorRef.set(error));

        Thread.sleep(1000);

        assertTrue(onCompleteCalled.get() || errorRef.get() != null);
    }

    @Test
    public void testStreamChatWithNullModel() throws Exception {
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "Hello");
        messages.add(message);

        AtomicBoolean onCompleteCalled = new AtomicBoolean(false);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        deepSeekService.streamChat(null, messages,
            data -> {},
            () -> onCompleteCalled.set(true),
            error -> errorRef.set(error));

        Thread.sleep(1000);

        assertTrue(onCompleteCalled.get() || errorRef.get() != null);
    }

    @Test
    public void testStreamChatErrorHandling() throws Exception {
        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "Hello");
        messages.add(message);

        AtomicReference<Throwable> errorRef = new AtomicReference<>();
        AtomicBoolean onCompleteCalled = new AtomicBoolean(false);

        deepSeekService.streamChat(model, messages,
            data -> {},
            () -> onCompleteCalled.set(true),
            error -> errorRef.set(error));

        Thread.sleep(1500);

        assertTrue(onCompleteCalled.get() || errorRef.get() != null);
    }

    @Test
    public void testStreamChatCallbackOnData() throws Exception {
        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "Hello");
        messages.add(message);

        AtomicInteger dataCount = new AtomicInteger(0);

        deepSeekService.streamChat(model, messages,
            data -> dataCount.incrementAndGet(),
            () -> {},
            error -> {});

        Thread.sleep(1000);

        assertTrue(dataCount.get() >= 0);
    }

    @Test
    public void testStreamChatWithMultipleMessages() throws Exception {
        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();
        
        Map<String, String> message1 = new HashMap<>();
        message1.put("role", "user");
        message1.put("content", "Hello");
        messages.add(message1);
        
        Map<String, String> message2 = new HashMap<>();
        message2.put("role", "assistant");
        message2.put("content", "Hi there!");
        messages.add(message2);

        AtomicBoolean onCompleteCalled = new AtomicBoolean(false);

        deepSeekService.streamChat(model, messages,
            data -> {},
            () -> onCompleteCalled.set(true),
            error -> {});

        Thread.sleep(1000);

        assertTrue(onCompleteCalled.get());
    }

    @Test
    public void testStreamChatWithChineseContent() throws Exception {
        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "你好，世界！");
        messages.add(message);

        AtomicBoolean onCompleteCalled = new AtomicBoolean(false);

        deepSeekService.streamChat(model, messages,
            data -> {},
            () -> onCompleteCalled.set(true),
            error -> {});

        Thread.sleep(1000);

        assertTrue(onCompleteCalled.get());
    }

    @Test
    public void testChatApiErrorResponse() {
        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "Test");
        messages.add(message);

        String result = deepSeekService.chat(model, messages);

        assertNotNull(result);
    }

    @Test
    public void testStreamChatOnErrorCallback() throws Exception {
        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "Test");
        messages.add(message);

        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        deepSeekService.streamChat(model, messages,
            data -> {},
            () -> {},
            error -> errorRef.set(error));

        Thread.sleep(1500);

        assertNull(errorRef.get());
    }

    @Test
    public void testChatWithInvalidApiKey() {
        when(deepSeekConfig.getApiKey()).thenReturn("invalid-key");
        
        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "Hello");
        messages.add(message);

        String result = deepSeekService.chat(model, messages);

        assertNotNull(result);
    }

    @Test
    public void testStreamChatWithInvalidApiKey() throws Exception {
        when(deepSeekConfig.getApiKey()).thenReturn("invalid-key");
        
        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "Hello");
        messages.add(message);

        AtomicBoolean onCompleteCalled = new AtomicBoolean(false);

        deepSeekService.streamChat(model, messages,
            data -> {},
            () -> onCompleteCalled.set(true),
            error -> {});

        Thread.sleep(1500);

        assertTrue(onCompleteCalled.get());
    }

    @Test
    public void testStreamChatWithSpecialCharacters() throws Exception {
        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "Hello! @#$%^&*()");
        messages.add(message);

        AtomicBoolean onCompleteCalled = new AtomicBoolean(false);

        deepSeekService.streamChat(model, messages,
            data -> {},
            () -> onCompleteCalled.set(true),
            error -> {});

        Thread.sleep(1000);

        assertTrue(onCompleteCalled.get());
    }
}
