package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.config.AlertConfig;
import com.ycyu.istudyspotbackend.service.AlertService;
import com.ycyu.istudyspotbackend.interceptor.MetricsInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class AlertServiceImpl implements AlertService {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertServiceImpl.class);
    
    private final AlertConfig alertConfig;
    private final MetricsInterceptor metricsInterceptor;
    private int consecutiveHealthFailures = 0;
    
    public AlertServiceImpl(AlertConfig alertConfig, MetricsInterceptor metricsInterceptor) {
        this.alertConfig = alertConfig;
        this.metricsInterceptor = metricsInterceptor;
    }
    
    @Override
    @Scheduled(fixedRate = 60000)
    public void checkErrorRate() {
        if (!alertConfig.getErrorRate().isEnabled()) {
            return;
        }
        
        double errorRate = metricsInterceptor.getErrorRate();
        double threshold = alertConfig.getErrorRate().getThreshold();
        
        if (errorRate > threshold) {
            logger.error("ALERT: Error rate exceeded threshold! Current: {}%, Threshold: {}%", 
                     String.format("%.2f", errorRate), threshold);
        }
    }
    
    @Override
    @Scheduled(fixedRate = 60000)
    public void checkResponseTime() {
        if (!alertConfig.getResponseTime().isEnabled()) {
            return;
        }
        
        double avgResponseTime = metricsInterceptor.getAverageResponseTime();
        long threshold = alertConfig.getResponseTime().getThresholdMs();
        
        if (avgResponseTime > threshold) {
            logger.warn("ALERT: Average response time exceeded threshold! Current: {}ms, Threshold: {}ms",
                    String.format("%.2f", avgResponseTime), threshold);
        }
    }
    
    @Override
    @Scheduled(fixedRate = 30000)
    public void checkServiceHealth() {
        if (!alertConfig.getServiceUnavailable().isEnabled()) {
            return;
        }
        
        boolean isHealthy = checkHealth();
        
        if (!isHealthy) {
            consecutiveHealthFailures++;
            if (consecutiveHealthFailures >= alertConfig.getServiceUnavailable().getConsecutiveFailures()) {
                logger.error("ALERT: Service is unavailable! {} consecutive failures detected", 
                         consecutiveHealthFailures);
            }
        } else {
            consecutiveHealthFailures = 0;
        }
    }
    
    private boolean checkHealth() {
        try {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
