package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.service.CustomerServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/customer-service")
public class CustomerServiceController {

    @Autowired
    private CustomerServiceService customerServiceService;

    @GetMapping("/welcome")
    public Result<Map<String, Object>> getWelcomeInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("welcomeMessage", customerServiceService.getWelcomeMessage());
        response.put("recommendedQuestions", customerServiceService.getRecommendedQuestions());
        return Result.success(response);
    }

    @PostMapping("/chat")
    public Result<Map<String, Object>> chatWithCustomerService(
            @RequestBody Map<String, String> request) {
        try {
            String sessionId = request.get("sessionId");
            String message = request.get("message");

            if (sessionId == null || sessionId.isEmpty()) {
                return Result.error(400, "EMPTY_SESSION_ID");
            }
            if (message == null || message.isEmpty()) {
                return Result.error(400, "EMPTY_MESSAGE");
            }

            String response = customerServiceService.chatWithCustomerService(sessionId, message);
            Map<String, Object> result = new HashMap<>();
            result.put("response", response);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "INTERNAL_ERROR");
        }
    }

    @PostMapping("/chat/stream")
    public SseEmitter streamChatWithCustomerService(
            @RequestBody Map<String, String> request) throws IOException {
        String sessionId = request.get("sessionId");
        String message = request.get("message");

        if (sessionId == null || sessionId.isEmpty()) {
            SseEmitter emitter = new SseEmitter();
            emitter.send(SseEmitter.event().data("{\"type\": \"error\", \"message\": \"EMPTY_SESSION_ID\"}"));
            emitter.complete();
            return emitter;
        }
        if (message == null || message.isEmpty()) {
            SseEmitter emitter = new SseEmitter();
            emitter.send(SseEmitter.event().data("{\"type\": \"error\", \"message\": \"EMPTY_MESSAGE\"}"));
            emitter.complete();
            return emitter;
        }

        return customerServiceService.streamChatWithCustomerService(sessionId, message);
    }

    @GetMapping("/history")
    public Result<Map<String, Object>> getSessionHistory(
            @RequestParam String sessionId) {
        Map<String, Object> result = new HashMap<>();
        result.put("messages", customerServiceService.getSessionHistory(sessionId));
        return Result.success(result);
    }
}