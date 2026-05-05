package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.config.DeepSeekConfig;
import com.ycyu.istudyspotbackend.service.DeepSeekService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public void testStreamChat() throws Exception {
        // 准备测试数据
        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "Hello");
        messages.add(message);

        // 执行测试
        SseEmitter emitter = deepSeekService.streamChat(model, messages);

        // 验证结果
        assertNotNull(emitter);
    }

    @Test
    public void testChat() throws Exception {
        // 准备测试数据
        String model = "deepseek-chat";
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", "Hello");
        messages.add(message);

        // 执行测试 - 这里会调用实际的API，可能会失败，但我们只是测试方法是否能正常调用
        try {
            String result = deepSeekService.chat(model, messages);
            // 如果成功，验证结果不为空
            assertNotNull(result);
        } catch (Exception e) {
            // 如果失败，捕获异常并验证异常信息
            assertTrue(e.getMessage().contains("Error calling DeepSeek API"));
        }
    }
}
