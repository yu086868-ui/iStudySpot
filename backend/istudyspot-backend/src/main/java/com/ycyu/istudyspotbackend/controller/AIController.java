package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Character;
import com.ycyu.istudyspotbackend.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AIController {

    @Autowired
    private AIService aiService;

    // 获取角色列表
    @GetMapping("/characters")
    public ResponseEntity<List<Character>> getCharacters() {
        List<Character> characters = aiService.getCharacters();
        return ResponseEntity.ok(characters);
    }

    // 非流式聊天
    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody Map<String, String> request) {
        try {
            String sessionId = request.get("session_id");
            String characterId = request.get("character_id");
            String message = request.get("message");

            // 验证参数
            if (sessionId == null || sessionId.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "EMPTY_SESSION_ID"));
            }
            if (characterId == null || characterId.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "EMPTY_CHARACTER_ID"));
            }
            if (message == null || message.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "EMPTY_MESSAGE"));
            }

            // 调用服务
            String reply = aiService.chat(sessionId, characterId, message);
            return ResponseEntity.ok(Map.of("reply", reply));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "INVALID_CHARACTER"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "INTERNAL_ERROR"));
        }
    }

    // 流式聊天
    @PostMapping("/chat/stream")
    public SseEmitter streamChat(@RequestBody Map<String, String> request) {
        try {
            String sessionId = request.get("session_id");
            String characterId = request.get("character_id");
            String message = request.get("message");

            // 验证参数
            if (sessionId == null || sessionId.isEmpty()) {
                SseEmitter emitter = new SseEmitter();
                emitter.send(SseEmitter.event().data("{\"type\": \"error\", \"message\": \"EMPTY_SESSION_ID\"}"));
                emitter.complete();
                return emitter;
            }
            if (characterId == null || characterId.isEmpty()) {
                SseEmitter emitter = new SseEmitter();
                emitter.send(SseEmitter.event().data("{\"type\": \"error\", \"message\": \"EMPTY_CHARACTER_ID\"}"));
                emitter.complete();
                return emitter;
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
}
