package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RulesControllerTest {

    @InjectMocks
    private RulesController rulesController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetRulesList() {
        // 调用控制器方法
        Result<List<Map<String, Object>>> result = rulesController.getRulesList(null, null);

        // 验证结果
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertNotNull(result.getData());
    }

    @Test
    public void testGetRuleDetail() {
        // 调用控制器方法
        Result<Map<String, Object>> result = rulesController.getRuleDetail(1L);

        // 验证结果
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertNotNull(result.getData());
    }
}  

