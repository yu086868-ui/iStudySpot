package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.config.WxConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WxServiceImplTest {

    @Mock
    private WxConfig wxConfig;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WxServiceImpl wxService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(wxService, "restTemplate", restTemplate);
        when(wxConfig.getAppId()).thenReturn("test-app-id");
        when(wxConfig.getAppSecret()).thenReturn("test-app-secret");
    }

    @Test
    void testGetOpenIdByCode_TestMode() {
        String result = wxService.getOpenIdByCode("test_openid123");

        assertEquals("openid123", result);
        verifyNoInteractions(restTemplate);
    }

    @Test
    void testGetOpenIdByCode_TestModeWithUnderscore() {
        String result = wxService.getOpenIdByCode("test_my_open_id");

        assertEquals("my_open_id", result);
    }

    @Test
    void testGetOpenIdByCode_RealApiSuccess() {
        String jsonResponse = "{\"openid\":\"wx-openid-123\",\"session_key\":\"session-key\"}";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(jsonResponse);

        String result = wxService.getOpenIdByCode("real_code");

        assertEquals("wx-openid-123", result);
        verify(restTemplate).getForObject(contains("jscode2session"), eq(String.class));
    }

    @Test
    void testGetOpenIdByCode_ApiErrorCode() {
        String jsonResponse = "{\"errcode\":40029,\"errmsg\":\"invalid code\"}";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(jsonResponse);

        assertThrows(RuntimeException.class, () -> {
            wxService.getOpenIdByCode("bad_code");
        });
    }

    @Test
    void testGetOpenIdByCode_NoOpenid() {
        String jsonResponse = "{\"session_key\":\"session-key\"}";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(jsonResponse);

        assertThrows(RuntimeException.class, () -> {
            wxService.getOpenIdByCode("no_openid_code");
        });
    }

    @Test
    void testGetOpenIdByCode_ApiException() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("Network error"));

        assertThrows(RuntimeException.class, () -> {
            wxService.getOpenIdByCode("error_code");
        });
    }

    @Test
    void testGetOpenIdByCode_InvalidJsonResponse() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("not a json");

        assertThrows(RuntimeException.class, () -> {
            wxService.getOpenIdByCode("bad_json_code");
        });
    }
}