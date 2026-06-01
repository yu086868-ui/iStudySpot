package com.ycyu.istudyspotbackend.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class MetricsInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(MetricsInterceptor.class);

    private static final AtomicLong totalRequests = new AtomicLong(0);
    private static final AtomicLong successfulRequests = new AtomicLong(0);
    private static final AtomicLong failedRequests = new AtomicLong(0);
    private static final AtomicLong totalResponseTime = new AtomicLong(0);
    private static final ConcurrentHashMap<String, AtomicLong> endpointRequestCount = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        long startTime = (Long) request.getAttribute("startTime");
        long responseTime = System.currentTimeMillis() - startTime;
        
        totalRequests.incrementAndGet();
        totalResponseTime.addAndGet(responseTime);
        
        String endpoint = request.getRequestURI();
        endpointRequestCount.computeIfAbsent(endpoint, k -> new AtomicLong(0)).incrementAndGet();
        
        int statusCode = response.getStatus();
        if (statusCode >= 200 && statusCode < 400) {
            successfulRequests.incrementAndGet();
        } else {
            failedRequests.incrementAndGet();
        }

        logger.info("{} {} {} {}ms {}",
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            request.getMethod(),
            endpoint,
            responseTime,
            statusCode
        );

        if (totalRequests.get() % 100 == 0) {
            logMetrics();
        }
    }

    private void logMetrics() {
        long total = totalRequests.get();
        long success = successfulRequests.get();
        long failed = failedRequests.get();
        long totalTime = totalResponseTime.get();
        
        double avgResponseTime = total > 0 ? (double) totalTime / total : 0;
        double errorRate = total > 0 ? (double) failed / total * 100 : 0;
        
        logger.info("===== METRICS REPORT =====");
        logger.info("Total Requests: {}", total);
        logger.info("Successful Requests: {}", success);
        logger.info("Failed Requests: {}", failed);
        logger.info("Error Rate: {:.2f}%", errorRate);
        logger.info("Average Response Time: {:.2f}ms", avgResponseTime);
        logger.info("==========================");
    }

    public static ConcurrentHashMap<String, Object> getMetrics() {
        ConcurrentHashMap<String, Object> metrics = new ConcurrentHashMap<>();
        metrics.put("totalRequests", totalRequests.get());
        metrics.put("successfulRequests", successfulRequests.get());
        metrics.put("failedRequests", failedRequests.get());
        metrics.put("totalResponseTime", totalResponseTime.get());
        metrics.put("endpointRequestCount", endpointRequestCount);
        return metrics;
    }
    
    public double getErrorRate() {
        long total = totalRequests.get();
        long failed = failedRequests.get();
        return total > 0 ? (double) failed / total * 100 : 0;
    }
    
    public double getAverageResponseTime() {
        long total = totalRequests.get();
        long totalTime = totalResponseTime.get();
        return total > 0 ? (double) totalTime / total : 0;
    }
}
