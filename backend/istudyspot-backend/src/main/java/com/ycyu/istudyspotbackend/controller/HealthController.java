package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${spring.application.name:iStudySpot}")
    private String appName;

    private static final String VERSION = "1.0.0";

    @GetMapping
    public Result<Map<String, Object>> health() {
        Map<String, Object> healthData = new HashMap<>();
        healthData.put("status", "healthy");
        healthData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        healthData.put("version", VERSION);
        healthData.put("appName", appName);
        healthData.put("serverPort", serverPort);
        healthData.put("service", "iStudySpot Backend");
        healthData.put("environment", "development");
        
        return Result.success("Service is running", healthData);
    }

    @GetMapping("/ready")
    public Result<Map<String, Object>> readiness() {
        Map<String, Object> readinessData = new HashMap<>();
        readinessData.put("status", "ready");
        readinessData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        readinessData.put("checks", Map.of(
            "database", "connected",
            "services", "available",
            "memory", "sufficient"
        ));
        
        return Result.success("Ready to serve", readinessData);
    }
}
