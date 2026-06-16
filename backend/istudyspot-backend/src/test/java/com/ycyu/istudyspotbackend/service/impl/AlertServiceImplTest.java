package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.config.AlertConfig;
import com.ycyu.istudyspotbackend.interceptor.MetricsInterceptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AlertServiceImplTest {

    @Mock
    private AlertConfig alertConfig;

    @Mock
    private MetricsInterceptor metricsInterceptor;

    @InjectMocks
    private AlertServiceImpl alertService;

    @Test
    void testCheckErrorRateNormal() {
        AlertConfig.ErrorRateAlert errorRateAlert = new AlertConfig.ErrorRateAlert();
        errorRateAlert.setEnabled(true);
        errorRateAlert.setThreshold(5.0);
        when(alertConfig.getErrorRate()).thenReturn(errorRateAlert);
        when(metricsInterceptor.getErrorRate()).thenReturn(2.0);

        alertService.checkErrorRate();

        verify(metricsInterceptor, times(1)).getErrorRate();
    }

    @Test
    void testCheckErrorRateExceeded() {
        AlertConfig.ErrorRateAlert errorRateAlert = new AlertConfig.ErrorRateAlert();
        errorRateAlert.setEnabled(true);
        errorRateAlert.setThreshold(5.0);
        when(alertConfig.getErrorRate()).thenReturn(errorRateAlert);
        when(metricsInterceptor.getErrorRate()).thenReturn(10.0);

        alertService.checkErrorRate();

        verify(metricsInterceptor, times(1)).getErrorRate();
    }

    @Test
    void testCheckErrorRateDisabled() {
        AlertConfig.ErrorRateAlert errorRateAlert = new AlertConfig.ErrorRateAlert();
        errorRateAlert.setEnabled(false);
        when(alertConfig.getErrorRate()).thenReturn(errorRateAlert);

        alertService.checkErrorRate();

        verify(metricsInterceptor, never()).getErrorRate();
    }

    @Test
    void testCheckResponseTimeNormal() {
        AlertConfig.ResponseTimeAlert responseTimeAlert = new AlertConfig.ResponseTimeAlert();
        responseTimeAlert.setEnabled(true);
        responseTimeAlert.setThresholdMs(1000);
        when(alertConfig.getResponseTime()).thenReturn(responseTimeAlert);
        when(metricsInterceptor.getAverageResponseTime()).thenReturn(500.0);

        alertService.checkResponseTime();

        verify(metricsInterceptor, times(1)).getAverageResponseTime();
    }

    @Test
    void testCheckResponseTimeExceeded() {
        AlertConfig.ResponseTimeAlert responseTimeAlert = new AlertConfig.ResponseTimeAlert();
        responseTimeAlert.setEnabled(true);
        responseTimeAlert.setThresholdMs(1000);
        when(alertConfig.getResponseTime()).thenReturn(responseTimeAlert);
        when(metricsInterceptor.getAverageResponseTime()).thenReturn(2000.0);

        alertService.checkResponseTime();

        verify(metricsInterceptor, times(1)).getAverageResponseTime();
    }

    @Test
    void testCheckResponseTimeDisabled() {
        AlertConfig.ResponseTimeAlert responseTimeAlert = new AlertConfig.ResponseTimeAlert();
        responseTimeAlert.setEnabled(false);
        when(alertConfig.getResponseTime()).thenReturn(responseTimeAlert);

        alertService.checkResponseTime();

        verify(metricsInterceptor, never()).getAverageResponseTime();
    }

    @Test
    void testCheckServiceHealth() {
        AlertConfig.ServiceUnavailableAlert serviceUnavailableAlert = new AlertConfig.ServiceUnavailableAlert();
        serviceUnavailableAlert.setEnabled(true);
        serviceUnavailableAlert.setConsecutiveFailures(3);
        when(alertConfig.getServiceUnavailable()).thenReturn(serviceUnavailableAlert);

        alertService.checkServiceHealth();

        verify(metricsInterceptor, never()).getErrorRate();
    }

    @Test
    void testCheckServiceHealthDisabled() {
        AlertConfig.ServiceUnavailableAlert serviceUnavailableAlert = new AlertConfig.ServiceUnavailableAlert();
        serviceUnavailableAlert.setEnabled(false);
        when(alertConfig.getServiceUnavailable()).thenReturn(serviceUnavailableAlert);

        alertService.checkServiceHealth();

        verify(metricsInterceptor, never()).getErrorRate();
    }
}