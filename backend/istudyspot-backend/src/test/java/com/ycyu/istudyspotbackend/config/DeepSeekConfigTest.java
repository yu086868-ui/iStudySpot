package com.ycyu.istudyspotbackend.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DeepSeekConfigTest {

    @Autowired
    private DeepSeekConfig deepSeekConfig;

    @Test
    void testDeepSeekConfigProperties() {
        assertNotNull(deepSeekConfig);
        assertNotNull(deepSeekConfig.getApiKey());
        assertNotNull(deepSeekConfig.getApiUrl());
    }
}
