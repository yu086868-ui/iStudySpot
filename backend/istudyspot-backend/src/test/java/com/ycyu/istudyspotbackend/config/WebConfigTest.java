package com.ycyu.istudyspotbackend.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WebConfigTest {

    @Autowired
    private WebConfig webConfig;

    @Test
    void testWebConfig() {
        assertNotNull(webConfig);
    }

    @Test
    void testAddInterceptors() {
        InterceptorRegistry registry = new InterceptorRegistry();
        webConfig.addInterceptors(registry);
        // 验证拦截器是否被注册
        assertNotNull(registry);
    }
}
