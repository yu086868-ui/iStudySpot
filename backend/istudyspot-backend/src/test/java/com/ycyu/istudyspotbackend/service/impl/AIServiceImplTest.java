package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.Character;
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
public class AIServiceImplTest {

    @Mock
    private DeepSeekService deepSeekService;

    @InjectMocks
    private AIServiceImpl aiService;

    @BeforeEach
    void setUp() {
        // 初始化测试环境
    }

    @Test
    void testGetCharacters() {
        // 测试获取角色列表
        List<Character> characters = aiService.getCharacters();

        // 验证结果
        assertNotNull(characters);
        assertFalse(characters.isEmpty());
        assertEquals(3, characters.size());
    }

    @Test
    void testChat() {
        // 测试与角色聊天
        String response = aiService.chat("test-session-123", "scientist", "你好");

        // 验证结果
        assertNotNull(response);
        assertTrue(response.contains("科学"));
    }
}
