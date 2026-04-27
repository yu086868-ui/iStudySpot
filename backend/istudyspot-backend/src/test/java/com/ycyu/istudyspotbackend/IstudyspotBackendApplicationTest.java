package com.ycyu.istudyspotbackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IstudyspotBackendApplicationTest {

    @Test
    void testApplicationContextLoads() {
        // 验证应用上下文能够正常加载
        assertTrue(true);
    }

    @Test
    void testMainMethod() {
        // 测试main方法是否能正常执行
        assertDoesNotThrow(() -> {
            IstudyspotBackendApplication.main(new String[]{});
        });
    }
}
