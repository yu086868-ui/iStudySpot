package com.ycyu.istudyspotbackend.interceptor;

import com.ycyu.istudyspotbackend.utils.JwtUtils;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtInterceptorTest {

    @InjectMocks
    private JwtInterceptor jwtInterceptor;

    @Mock
    private JwtUtils jwtUtils;

    public JwtInterceptorTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPreHandleWithValidToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // 设置请求头
        request.addHeader("Authorization", "Bearer valid-token");

        // 模拟JwtUtils验证token
        when(jwtUtils.validateToken("valid-token")).thenReturn(true);
        when(jwtUtils.getUserIdFromToken("valid-token")).thenReturn(1L);

        boolean result = jwtInterceptor.preHandle(request, response, new Object());
        assertTrue(result);
        assertEquals(1L, request.getAttribute("userId"));
    }

    @Test
    void testPreHandleWithInvalidToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // 设置请求头
        request.addHeader("Authorization", "Bearer invalid-token");

        // 模拟JwtUtils验证token
        when(jwtUtils.validateToken("invalid-token")).thenReturn(false);

        boolean result = jwtInterceptor.preHandle(request, response, new Object());
        assertFalse(result);
        assertEquals(401, response.getStatus());
    }

    @Test
    void testPreHandleWithoutToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = jwtInterceptor.preHandle(request, response, new Object());
        assertFalse(result);
        assertEquals(401, response.getStatus());
    }

    @Test
    void testPreHandleWithOptionsRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // 设置为OPTIONS请求
        request.setMethod(HttpMethod.OPTIONS.name());

        boolean result = jwtInterceptor.preHandle(request, response, new Object());
        assertTrue(result);
    }
}
