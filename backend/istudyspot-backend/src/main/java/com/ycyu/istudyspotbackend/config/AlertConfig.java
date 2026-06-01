package com.ycyu.istudyspotbackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "alert")
public class AlertConfig {
    
    private ErrorRateAlert errorRate = new ErrorRateAlert();
    private ResponseTimeAlert responseTime = new ResponseTimeAlert();
    private ServiceUnavailableAlert serviceUnavailable = new ServiceUnavailableAlert();
    
    public ErrorRateAlert getErrorRate() {
        return errorRate;
    }
    
    public void setErrorRate(ErrorRateAlert errorRate) {
        this.errorRate = errorRate;
    }
    
    public ResponseTimeAlert getResponseTime() {
        return responseTime;
    }
    
    public void setResponseTime(ResponseTimeAlert responseTime) {
        this.responseTime = responseTime;
    }
    
    public ServiceUnavailableAlert getServiceUnavailable() {
        return serviceUnavailable;
    }
    
    public void setServiceUnavailable(ServiceUnavailableAlert serviceUnavailable) {
        this.serviceUnavailable = serviceUnavailable;
    }
    
    public static class ErrorRateAlert {
        private boolean enabled = true;
        private double threshold = 5.0;
        private int windowSeconds = 60;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public double getThreshold() {
            return threshold;
        }
        
        public void setThreshold(double threshold) {
            this.threshold = threshold;
        }
        
        public int getWindowSeconds() {
            return windowSeconds;
        }
        
        public void setWindowSeconds(int windowSeconds) {
            this.windowSeconds = windowSeconds;
        }
    }
    
    public static class ResponseTimeAlert {
        private boolean enabled = true;
        private long thresholdMs = 500;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public long getThresholdMs() {
            return thresholdMs;
        }
        
        public void setThresholdMs(long thresholdMs) {
            this.thresholdMs = thresholdMs;
        }
    }
    
    public static class ServiceUnavailableAlert {
        private boolean enabled = true;
        private int consecutiveFailures = 3;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public int getConsecutiveFailures() {
            return consecutiveFailures;
        }
        
        public void setConsecutiveFailures(int consecutiveFailures) {
            this.consecutiveFailures = consecutiveFailures;
        }
    }
}
