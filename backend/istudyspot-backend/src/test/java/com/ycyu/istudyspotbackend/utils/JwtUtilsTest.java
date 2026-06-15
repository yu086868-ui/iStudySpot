package com.ycyu.istudyspotbackend.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secretKey", "iStudySpotSecretKey2024IsVeryLongAndSecure1234567890");
    }

    @Test
    void testGenerateToken() {
        String token = jwtUtils.generateToken(1L);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testGenerateRefreshToken() {
        String refreshToken = jwtUtils.generateRefreshToken(1L);
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
    }

    @Test
    void testGetUserIdFromToken() {
        Long userId = 1L;
        String token = jwtUtils.generateToken(userId);
        Long extractedUserId = jwtUtils.getUserIdFromToken(token);
        assertEquals(userId, extractedUserId);
    }

    @Test
    void testValidateToken() {
        String token = jwtUtils.generateToken(1L);
        boolean isValid = jwtUtils.validateToken(token);
        assertTrue(isValid);
    }

    @Test
    void testValidateTokenWithInvalidToken() {
        String invalidToken = "invalid-token";
        boolean isValid = jwtUtils.validateToken(invalidToken);
        assertFalse(isValid);
    }
}
