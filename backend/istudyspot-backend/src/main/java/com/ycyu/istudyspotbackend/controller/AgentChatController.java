package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.agent.chat.AgentChatRequest;
import com.ycyu.istudyspotbackend.agent.chat.AgentChatResponse;
import com.ycyu.istudyspotbackend.agent.chat.AgentChatService;
import com.ycyu.istudyspotbackend.entity.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/agent")
public class AgentChatController {

    @Autowired
    private AgentChatService agentChatService;

    @PostMapping("/chat")
    public Result<?> chat(
            @RequestBody AgentChatRequest request,
            HttpServletRequest servletRequest) {
        try {
            Long userId = (Long) servletRequest.getAttribute("userId");
            AgentChatResponse response = agentChatService.chat(userId, request);
            return Result.success("success", response);
        } catch (IllegalArgumentException e) {
            return errorResult(resolveStatus(e.getMessage()), e.getMessage(), false);
        } catch (RuntimeException e) {
            return errorResult(500, "INTERNAL_ERROR", true);
        }
    }

    @PostMapping("/chat/stream")
    public SseEmitter streamChat(
            @RequestBody AgentChatRequest request,
            HttpServletRequest servletRequest) {
        Long userId = (Long) servletRequest.getAttribute("userId");
        return agentChatService.streamChat(userId, request);
    }

    private int resolveStatus(String errorCode) {
        if ("AUTH_REQUIRED".equals(errorCode)) {
            return 401;
        }
        return 400;
    }

    private Result<Map<String, Object>> errorResult(int code, String errorCode, boolean retryable) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", errorCode);
        error.put("retryable", retryable);
        if ("EMPTY_MESSAGE".equals(errorCode)) {
            error.put("field", "message");
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("schemaVersion", "1.0");
        payload.put("error", error);

        return new Result<>(code, errorCode, payload);
    }
}
