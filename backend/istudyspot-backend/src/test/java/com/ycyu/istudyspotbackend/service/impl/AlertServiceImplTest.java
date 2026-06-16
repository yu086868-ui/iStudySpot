package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.config.AlertConfig;
import com.ycyu.istudyspotbackend.interceptor.MetricsInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceImplTest {

    @Mock
    private AlertConfig alertConfig;

    @Mock
    private MetricsInterceptor metricsInterceptor;

    private AlertServiceImpl alertService;

    @BeforeEach
    void setUp() {
        alertService = new AlertServiceImpl(alertConfig, metricsInterceptor);
    }

    @Test
    void testCheckErrorRate_Disabled() {
        AlertConfig.ErrorRateAlert errorRateConfig = new AlertConfig.ErrorRateAlert();
        errorRateConfig.setEnabled(false);
        when(alertConfig.getErrorRate()).thenReturn(errorRateConfig);

        alertService.checkErrorRate();

        verifyNoInteractions(metricsInterceptor);
    }

    @Test
    void testCheckErrorRate_Exceeded() {
        AlertConfig.ErrorRateAlert errorRateConfig = new AlertConfig.ErrorRateAlert();
        errorRateConfig.setEnabled(true);
        errorRateConfig.setThreshold(5.0);
        when(alertConfig.getErrorRate()).thenReturn(errorRateConfig);
        when(metricsInterceptor.getErrorRate()).thenReturn(10.0);

        alertService.checkErrorRate();

        verify(metricsInterceptor).getErrorRate();
    }

    @Test
    void testCheckErrorRate_Normal() {
        AlertConfig.ErrorRateAlert errorRateConfig = new AlertConfig.ErrorRateAlert();
        errorRateConfig.setEnabled(true);
        errorRateConfig.setThreshold(5.0);
        when(alertConfig.getErrorRate()).thenReturn(errorRateConfig);
        when(metricsInterceptor.getErrorRate()).thenReturn(2.0);

        alertService.checkErrorRate();

        verify(metricsInterceptor).getErrorRate();
    }

    @Test
    void testCheckResponseTime_Disabled() {
        AlertConfig.ResponseTimeAlert responseTimeConfig = new AlertConfig.ResponseTimeAlert();
        responseTimeConfig.setEnabled(false);
        when(alertConfig.getResponseTime()).thenReturn(responseTimeConfig);

        alertService.checkResponseTime();

        verifyNoInteractions(metricsInterceptor);
    }

    @Test
    void testCheckResponseTime_Exceeded() {
        AlertConfig.ResponseTimeAlert responseTimeConfig = new AlertConfig.ResponseTimeAlert();
        responseTimeConfig.setEnabled(true);
        responseTimeConfig.setThresholdMs(1000L);
        when(alertConfig.getResponseTime()).thenReturn(responseTimeConfig);
        when(metricsInterceptor.getAverageResponseTime()).thenReturn(2000.0);

        alertService.checkResponseTime();

        verify(metricsInterceptor).getAverageResponseTime();
    }

    @Test
    void testCheckServiceHealth_Disabled() {
        AlertConfig.ServiceUnavailableAlert serviceConfig = new AlertConfig.ServiceUnavailableAlert();
        serviceConfig.setEnabled(false);
        when(alertConfig.getServiceUnavailable()).thenReturn(serviceConfig);

        alertService.checkServiceHealth();

        verifyNoInteractions(metricsInterceptor);
    }

    @Test
    void testCheckServiceHealth_Enabled() {
        AlertConfig.ServiceUnavailableAlert serviceConfig = new AlertConfig.ServiceUnavailableAlert();
        serviceConfig.setEnabled(true);
        when(alertConfig.getServiceUnavailable()).thenReturn(serviceConfig);

        alertService.checkServiceHealth();
    }
}