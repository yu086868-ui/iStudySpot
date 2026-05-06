package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.CustomerServiceMessage;
import com.ycyu.istudyspotbackend.service.CustomerServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer-service")
public class CustomerServiceController {

    @Autowired
    private CustomerServiceService customerServiceService;

    @GetMapping("/welcome")
    public ResponseEntity<Map<String, Object>> getWelcomeInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("welcomeMessage", customerServiceService.getWelcomeMessage());
        response.put("recommendedQuestions", customerServiceService.getRecommendedQuestions());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chatWithCustomerService(
            @RequestBody Map<String, String> request) {
        try {
            String sessionId = request.get("sessionId");
            String message = request.get("message");

            if (sessionId == null || sessionId.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "EMPTY_SESSION_ID"));
            }
            if (message == null || message.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "EMPTY_MESSAGE"));
            }

            String response = customerServiceService.chatWithCustomerService(sessionId, message);
            Map<String, Object> result = new HashMap<>();
            result.put("response", response);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "INTERNAL_ERROR"));
        }
    }

    @PostMapping("/chat/stream")
    public SseEmitter streamChatWithCustomerService(
            @RequestBody Map<String, String> request) {
        try {
            String sessionId = request.get("sessionId");
            String message = request.get("message");

            if (sessionId == null || sessionId.isEmpty()) {
                SseEmitter emitter = new SseEmitter();
                try {
                    emitter.send(SseEmitter.event().data("{\"type\": \"error\", \"message\": \"EMPTY_SESSION_ID\"}"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                emitter.complete();
                return emitter;
            }
            if (message == null || message.isEmpty()) {
                SseEmitter emitter = new SseEmitter();
                try {
                    emitter.send(SseEmitter.event().data("{\"type\": \"error\", \"message\": \"EMPTY_MESSAGE\"}"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                emitter.complete();
                return emitter;
            }

            return customerServiceService.streamChatWithCustomerService(sessionId, message);
        } catch (Exception e) {
            SseEmitter emitter = new SseEmitter();
            try {
                emitter.send(SseEmitter.event().data("{\"type\": \"error\", \"message\": \"INTERNAL_ERROR\"}"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            emitter.complete();
            return emitter;
        }
    }

    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getSessionHistory(
            @RequestParam String sessionId) {
        try {
            List<CustomerServiceMessage> messages = customerServiceService.getSessionHistory(sessionId);
            Map<String, Object> result = new HashMap<>();
            result.put("messages", messages);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "INTERNAL_ERROR"));
        }
    }
}