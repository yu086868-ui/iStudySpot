package com.ycyu.istudyspotbackend.service.impl;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.ycyu.istudyspotbackend.config.DeepSeekConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

public class DeepSeekServiceImplWireMockTest {

    private WireMockServer wireMockServer;
    private DeepSeekServiceImpl deepSeekService;
    private DeepSeekConfig deepSeekConfig;

    @BeforeEach
    public void setUp() throws Exception {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
        
        deepSeekConfig = new DeepSeekConfig();
        
        java.lang.reflect.Field apiKeyField = DeepSeekConfig.class.getDeclaredField("apiKey");
        apiKeyField.setAccessible(true);
        apiKeyField.set(deepSeekConfig, "test-api-key");
        
        java.lang.reflect.Field apiUrlField = DeepSeekConfig.class.getDeclaredField("apiUrl");
        apiUrlField.setAccessible(true);
        apiUrlField.set(deepSeekConfig, "http://localhost:" + wireMockServer.port() + "/v1");
        
        deepSeekService = new DeepSeekServiceImpl();
        
        java.lang.reflect.Field configField = DeepSeekServiceImpl.class.getDeclaredField("deepSeekConfig");
        configField.setAccessible(true);
        configField.set(deepSeekService, deepSeekConfig);
    }

    @AfterEach
    public void tearDown() {
        wireMockServer.stop();
    }

    @Test
    public void testChatSuccessfulResponse() {
        wireMockServer.stubFor(post(urlEqualTo("/v1/chat/completions"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{\"choices\": [{\"message\": {\"content\": \"Hello from DeepSeek!\"}}]}")));

        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "Hello");
        messages.add(message);

        String result = deepSeekService.chat(model, messages);

        assertNotNull(result);
        assertEquals("Hello from DeepSeek!", result);
    }

    @Test
    public void testChatErrorResponse() {
        wireMockServer.stubFor(post(urlEqualTo("/v1/chat/completions"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\": {\"message\": \"Invalid API key\"}}")));

        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "Hello");
        messages.add(message);

        String result = deepSeekService.chat(model, messages);

        assertNotNull(result);
        assertTrue(result.contains("Error from DeepSeek API"));
        assertTrue(result.contains("Invalid API key"));
    }

    @Test
    public void testChatGarbledResponse() {
        wireMockServer.stubFor(post(urlEqualTo("/v1/chat/completions"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{\"choices\": [{\"message\": {\"content\": \"????????\"}}]}")));

        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "Hello");
        messages.add(message);

        String result = deepSeekService.chat(model, messages);

        assertNotNull(result);
        assertTrue(result.contains("Sorry, I can't understand the response"));
    }

    @Test
    public void testChatEmptyChoices() {
        wireMockServer.stubFor(post(urlEqualTo("/v1/chat/completions"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{\"choices\": []}")));

        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "Hello");
        messages.add(message);

        String result = deepSeekService.chat(model, messages);

        assertNotNull(result);
        assertEquals("No response from DeepSeek API", result);
    }

    @Test
    public void testChatNullContent() {
        wireMockServer.stubFor(post(urlEqualTo("/v1/chat/completions"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{\"choices\": [{\"message\": {}}]}")));

        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "Hello");
        messages.add(message);

        String result = deepSeekService.chat(model, messages);

        assertNotNull(result);
        assertEquals("No response from DeepSeek API", result);
    }

    @Test
    public void testChatConnectionError() {
        wireMockServer.stop();

        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "Hello");
        messages.add(message);

        String result = deepSeekService.chat(model, messages);

        assertNotNull(result);
        assertTrue(result.contains("Error calling DeepSeek API"));
    }

    @Test
    public void testStreamChatSuccessfulResponse() throws Exception {
        wireMockServer.stubFor(post(urlEqualTo("/v1/chat/completions"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "text/event-stream")
                .withBody(
                    "data: {\"choices\": [{\"delta\": {\"content\": \"Hello\"}}]}\n" +
                    "data: {\"choices\": [{\"delta\": {\"content\": \" World\"}}]}\n" +
                    "data: [DONE]\n"
                )));

        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "Hello");
        messages.add(message);

        CountDownLatch latch = new CountDownLatch(1);
        StringBuilder responseContent = new StringBuilder();

        deepSeekService.streamChat(model, messages,
            data -> responseContent.append(data),
            () -> latch.countDown(),
            error -> {});

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(responseContent.toString().contains("Hello"));
        assertTrue(responseContent.toString().contains("World"));
        assertTrue(responseContent.toString().contains("\"type\": \"end\""));
    }

    @Test
    public void testStreamChatErrorResponse() throws Exception {
        wireMockServer.stubFor(post(urlEqualTo("/v1/chat/completions"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "text/event-stream")
                .withBody("data: {\"error\": {\"message\": \"Stream error\"}}\n")));

        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "Hello");
        messages.add(message);

        CountDownLatch latch = new CountDownLatch(1);
        StringBuilder responseContent = new StringBuilder();

        deepSeekService.streamChat(model, messages,
            data -> {
                responseContent.append(data);
                latch.countDown();
            },
            () -> {},
            error -> {});

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(responseContent.toString().contains("Error from DeepSeek API"));
    }

    @Test
    public void testStreamChatGarbledResponse() throws Exception {
        wireMockServer.stubFor(post(urlEqualTo("/v1/chat/completions"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "text/event-stream")
                .withBody("data: {\"choices\": [{\"delta\": {\"content\": \"????????\"}}]}\n")));

        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "Hello");
        messages.add(message);

        CountDownLatch latch = new CountDownLatch(1);
        StringBuilder responseContent = new StringBuilder();

        deepSeekService.streamChat(model, messages,
            data -> {
                responseContent.append(data);
                latch.countDown();
            },
            () -> {},
            error -> {});

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(responseContent.toString().contains("can't understand the response"));
    }

    @Test
    public void testStreamChatParsingError() throws Exception {
        wireMockServer.stubFor(post(urlEqualTo("/v1/chat/completions"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "text/event-stream")
                .withBody("data: invalid json {{{}\n")));

        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "Hello");
        messages.add(message);

        CountDownLatch latch = new CountDownLatch(1);

        deepSeekService.streamChat(model, messages,
            data -> {},
            () -> latch.countDown(),
            error -> {});

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testStreamChatEmptyDeltaContent() throws Exception {
        wireMockServer.stubFor(post(urlEqualTo("/v1/chat/completions"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "text/event-stream")
                .withBody(
                    "data: {\"choices\": [{\"delta\": {}}]}\n" +
                    "data: [DONE]\n"
                )));

        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "Hello");
        messages.add(message);

        CountDownLatch latch = new CountDownLatch(1);

        deepSeekService.streamChat(model, messages,
            data -> {},
            () -> latch.countDown(),
            error -> {});

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testStreamChatConnectionError() throws Exception {
        wireMockServer.stop();

        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "Hello");
        messages.add(message);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean errorCalled = new AtomicBoolean(false);

        deepSeekService.streamChat(model, messages,
            data -> {},
            () -> {},
            error -> {
                errorCalled.set(true);
                latch.countDown();
            });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(errorCalled.get());
    }

    @Test
    public void testStreamChatMultipleChunks() throws Exception {
        wireMockServer.stubFor(post(urlEqualTo("/v1/chat/completions"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "text/event-stream")
                .withBody(
                    "data: {\"choices\": [{\"delta\": {\"content\": \"First\"}}]}\n" +
                    "data: {\"choices\": [{\"delta\": {\"content\": \" Second\"}}]}\n" +
                    "data: {\"choices\": [{\"delta\": {\"content\": \" Third\"}}]}\n" +
                    "data: [DONE]\n"
                )));

        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "Hello");
        messages.add(message);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger chunkCount = new AtomicInteger(0);

        deepSeekService.streamChat(model, messages,
            data -> {
                if (data.contains("\"type\": \"delta\"")) {
                    chunkCount.incrementAndGet();
                }
            },
            () -> latch.countDown(),
            error -> {});

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(3, chunkCount.get());
    }
}
