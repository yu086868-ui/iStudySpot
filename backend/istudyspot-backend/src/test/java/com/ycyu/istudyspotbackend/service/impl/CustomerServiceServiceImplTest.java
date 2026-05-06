package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.CustomerServiceMessage;
import com.ycyu.istudyspotbackend.service.DeepSeekService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceServiceImplTest {

    @Mock
    private DeepSeekService deepSeekService;

    @InjectMocks
    private CustomerServiceServiceImpl customerServiceService;

    @BeforeEach
    void setUp() {
        // 初始化测试环境
    }

    @Test
    void testGetWelcomeMessage() {
        // 测试获取欢迎消息
        String welcomeMessage = customerServiceService.getWelcomeMessage();

        // 验证结果
        assertNotNull(welcomeMessage);
        assertFalse(welcomeMessage.isEmpty());
    }

    @Test
    void testGetRecommendedQuestions() {
        // 测试获取推荐问题
        List<String> recommendedQuestions = customerServiceService.getRecommendedQuestions();

        // 验证结果
        assertNotNull(recommendedQuestions);
        assertFalse(recommendedQuestions.isEmpty());
        assertEquals(8, recommendedQuestions.size());
    }

    @Test
    void testChatWithCustomerService() {
        // 模拟 DeepSeek API 响应
        when(deepSeekService.chat(anyString(), anyList())).thenReturn("您好！我是智能客服小i，很高兴为您服务。");

        // 测试聊天
        String response = customerServiceService.chatWithCustomerService("test-session-123", "你好");

        // 验证结果
        assertNotNull(response);
        assertEquals("您好！我是智能客服小i，很高兴为您服务。", response);

        // 验证方法调用
        verify(deepSeekService, times(1)).chat(anyString(), anyList());
    }

    @Test
    void testGetSessionHistory() {
        // 测试获取会话历史
        List<CustomerServiceMessage> history = customerServiceService.getSessionHistory("test-session-123");

        // 验证结果
        assertNotNull(history);
        assertTrue(history.isEmpty());
    }
}
