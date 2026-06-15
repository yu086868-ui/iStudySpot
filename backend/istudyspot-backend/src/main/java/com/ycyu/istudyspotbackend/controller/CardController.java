package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Card;
import com.ycyu.istudyspotbackend.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wx/card")
public class CardController {

    @Autowired
    private CardService cardService;

    @Value("${card.image.storage-path:./uploads/cards}")
    private String storagePath;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateCard(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String userId = (String) request.get("userID");
            Integer studyDuration = (Integer) request.get("studyDuration");
            
            if (userId == null || userId.isEmpty()) {
                response.put("success", false);
                response.put("message", "userID is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (studyDuration == null || studyDuration <= 0) {
                response.put("success", false);
                response.put("message", "studyDuration must be positive");
                return ResponseEntity.badRequest().body(response);
            }
            
            Card card = cardService.generateCard(userId, studyDuration);
            
            response.put("success", true);
            response.put("message", "generate success");
            response.put("card", formatCardResponse(card));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "generate failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/generate/stream")
    public SseEmitter generateCardStream(@RequestBody Map<String, Object> request) {
        SseEmitter emitter = new SseEmitter(120000L);
        
        String userId = (String) request.get("userID");
        Integer studyDuration = (Integer) request.get("studyDuration");
        
        if (userId == null || userId.isEmpty()) {
            try {
                emitter.send(SseEmitter.event()
                    .name("error")
                    .data("{\"success\":false,\"message\":\"userID is required\"}"));
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
            return emitter;
        }
        
        if (studyDuration == null || studyDuration <= 0) {
            try {
                emitter.send(SseEmitter.event()
                    .name("error")
                    .data("{\"success\":false,\"message\":\"studyDuration must be positive\"}"));
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
            return emitter;
        }
        
        cardService.generateCardStream(userId, studyDuration,
            data -> {
                try {
                    emitter.send(SseEmitter.event()
                        .name("data")
                        .data(data));
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            },
            completeData -> {
                try {
                    emitter.send(SseEmitter.event()
                        .name("complete")
                        .data(completeData));
                    emitter.complete();
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            },
            error -> {
                try {
                    emitter.send(SseEmitter.event()
                        .name("error")
                        .data("{\"success\":false,\"message\":\"" + error.getMessage() + "\"}"));
                    emitter.completeWithError(error);
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }
        );
        
        return emitter;
    }

    @GetMapping("/detail")
    public ResponseEntity<Map<String, Object>> getCardDetail(@RequestParam("id") String uuid) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Card card = cardService.getCardByUuid(uuid);
            
            if (card == null) {
                response.put("success", false);
                response.put("message", "card not found");
                return ResponseEntity.notFound().build();
            }
            
            response.put("success", true);
            response.put("card", formatCardResponse(card));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "get card failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getCardList(@RequestParam("userID") String userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Card> cards = cardService.getCardsByUserId(userId);
            
            response.put("success", true);
            response.put("list", cards.stream().map(this::formatCardResponse).toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "get card list failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/image/**")
    public ResponseEntity<Resource> getImage(HttpServletRequest request) {
        try {
            String requestUri = request.getRequestURI();
            String imagePath = requestUri.replace("/api/wx/card/image/", "");
            
            File file = new File(storagePath, imagePath);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=31536000")
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    private Map<String, Object> formatCardResponse(Card card) {
        Map<String, Object> cardMap = new HashMap<>();
        cardMap.put("uuid", card.getUuid());
        cardMap.put("rarity", card.getRarity());
        cardMap.put("borderTheme", card.getBorderTheme());
        cardMap.put("cardTheme", card.getCardTheme());
        cardMap.put("themeCategory", card.getThemeCategory());
        cardMap.put("markdown", card.getMarkdown());
        cardMap.put("imageURL", card.getImageUrl());
        cardMap.put("createTime", card.getCreateTime() != null ? card.getCreateTime().format(FORMATTER) : null);
        cardMap.put("studyDuration", card.getStudyDuration());
        return cardMap;
    }
}