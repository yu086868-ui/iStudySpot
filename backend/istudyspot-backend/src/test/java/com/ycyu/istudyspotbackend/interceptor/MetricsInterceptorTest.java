package com.ycyu.istudyspotbackend.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricsInterceptorTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private final MetricsInterceptor interceptor = new MetricsInterceptor();

    @Test
    void testPreHandle() {
        boolean result = interceptor.preHandle(request, response, null);

        assertTrue(result);
        verify(request).setAttribute(eq("startTime"), anyLong());
    }

    @Test
    void testAfterCompletion_Success() {
        when(request.getAttribute("startTime")).thenReturn(System.currentTimeMillis() - 100);
        when(request.getRequestURI()).thenReturn("/api/test");
        when(response.getStatus()).thenReturn(200);

        interceptor.afterCompletion(request, response, null, null);

        verify(request).getAttribute("startTime");
        verify(response).getStatus();
    }

    @Test
    void testAfterCompletion_Error() {
        when(request.getAttribute("startTime")).thenReturn(System.currentTimeMillis() - 100);
        when(request.getRequestURI()).thenReturn("/api/error");
        when(response.getStatus()).thenReturn(500);

        interceptor.afterCompletion(request, response, null, null);

        verify(response).getStatus();
    }

    @Test
    void testGetErrorRate() {
        double rate = interceptor.getErrorRate();
        assertTrue(rate >= 0);
    }

    @Test
    void testGetAverageResponseTime() {
        double avg = interceptor.getAverageResponseTime();
        assertTrue(avg >= 0);
    }
}