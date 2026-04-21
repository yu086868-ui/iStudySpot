package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.service.CustomerServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    /**
     * 获取智能客服欢迎消息和推荐问题
     */
    @GetMapping("/welcome")
    public ResponseEntity<Map<String, Object>> getWelcomeInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("welcomeMessage", customerServiceService.getWelcomeMessage());
        response.put("recommendedQuestions", customerServiceService.getRecommendedQuestions());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 非流式智能客服聊天
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chatWithCustomerService(
            @RequestParam String sessionId,
            @RequestParam String message) {
        String response = customerServiceService.chatWithCustomerService(sessionId, message);
        Map<String, Object> result = new HashMap<>();
        result.put("response", response);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * 流式智能客服聊天
     */
    @PostMapping("/chat/stream")
    public SseEmitter streamChatWithCustomerService(
            @RequestParam String sessionId,
            @RequestParam String message) throws IOException {
        return customerServiceService.streamChatWithCustomerService(sessionId, message);
    }

    /**
     * 获取会话历史
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getSessionHistory(
            @RequestParam String sessionId) {
        Map<String, Object> result = new HashMap<>();
        result.put("messages", customerServiceService.getSessionHistory(sessionId));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
