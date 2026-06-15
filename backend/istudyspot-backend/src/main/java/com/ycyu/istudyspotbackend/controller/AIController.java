package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.AICharacter;
import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class AIController {

    private static final String DEFAULT_CHARACTER_ID = "customer_service";

    @Autowired
    private AIService aiService;

    // 获取角色列表
    @GetMapping("/characters")
    public Result<List<AICharacter>> getCharacters() {
        List<AICharacter> characters = aiService.getCharacters();
        return Result.success(characters);
    }

    // 非流式聊天
    @PostMapping("/chat")
    public Result<?> chat(@RequestBody Map<String, String> request) {
        try {
            String sessionId = firstNonBlank(request, "session_id", "sessionId");
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = UUID.randomUUID().toString();
            }
            String characterId = firstNonBlank(request, "character_id", "characterId");
            if (characterId == null || characterId.isEmpty()) {
                characterId = DEFAULT_CHARACTER_ID;
            }
            String message = request.get("message");

            if (message == null || message.isEmpty()) {
                return Result.error(400, "EMPTY_MESSAGE");
            }

            String reply = aiService.chat(sessionId, characterId, message);
            return Result.success(Map.of("reply", reply, "session_id", sessionId));
        } catch (Exception e) {
            return Result.error(500, "INTERNAL_ERROR");
        }
    }

    // 流式聊天
    @PostMapping("/chat/stream")
    public SseEmitter streamChat(@RequestBody Map<String, String> request) {
        try {
            String sessionId = firstNonBlank(request, "session_id", "sessionId");
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = UUID.randomUUID().toString();
            }
            String characterId = firstNonBlank(request, "character_id", "characterId");
            String message = request.get("message");

            if (characterId == null || characterId.isEmpty()) {
                characterId = DEFAULT_CHARACTER_ID;
            }
            if (message == null || message.isEmpty()) {
                SseEmitter emitter = new SseEmitter();
                emitter.send(SseEmitter.event().data("{\"type\": \"error\", \"message\": \"EMPTY_MESSAGE\"}"));
                emitter.complete();
                return emitter;
            }

            // 调用服务
            return aiService.streamChat(sessionId, characterId, message);
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

    private String firstNonBlank(Map<String, String> request, String... keys) {
        for (String key : keys) {
            String value = request.get(key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
