package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.config.WxConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class WxServiceImplTest {

    @Mock
    private WxConfig wxConfig;

    @InjectMocks
    private WxServiceImpl wxService;

    @Test
    void testGetOpenIdByCodeTestMode() {
        String openId = wxService.getOpenIdByCode("test_myopenid123");

        assertEquals("myopenid123", openId);
    }

    @Test
    void testGetOpenIdByCodeTestModeWithUnderscore() {
        String openId = wxService.getOpenIdByCode("test_user_openid");

        assertEquals("user_openid", openId);
    }

    @Test
    void testGetOpenIdByCodeRealCodeThrowsException() {
        assertThrows(RuntimeException.class, () -> {
            wxService.getOpenIdByCode("real_code_from_wechat");
        });
    }
}