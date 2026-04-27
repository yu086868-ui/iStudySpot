package com.ycyu.istudyspotbackend.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilsTest {

    private JwtUtils jwtUtils = new JwtUtils();

    @Test
    void testGenerateToken() {
        // 测试生成 token
        String token = jwtUtils.generateToken(1L);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testGenerateRefreshToken() {
        // 测试生成 refresh token
        String refreshToken = jwtUtils.generateRefreshToken(1L);
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
    }

    @Test
    void testGetUserIdFromToken() {
        // 测试从 token 中获取用户 ID
        Long userId = 1L;
        String token = jwtUtils.generateToken(userId);
        Long extractedUserId = jwtUtils.getUserIdFromToken(token);
        assertEquals(userId, extractedUserId);
    }

    @Test
    void testValidateToken() {
        // 测试验证 token
        String token = jwtUtils.generateToken(1L);
        boolean isValid = jwtUtils.validateToken(token);
        assertTrue(isValid);
    }

    @Test
    void testValidateTokenWithInvalidToken() {
        // 测试验证无效 token
        String invalidToken = "invalid-token";
        boolean isValid = jwtUtils.validateToken(invalidToken);
        assertFalse(isValid);
    }
}
