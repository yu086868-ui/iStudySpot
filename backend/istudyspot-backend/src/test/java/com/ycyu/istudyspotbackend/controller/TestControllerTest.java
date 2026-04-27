package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestControllerTest {

    @InjectMocks
    private TestController testController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testTest() {
        // 调用控制器方法
        Result<String> result = testController.test();

        // 验证结果
        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertNotNull(result.getData());
    }
}  

