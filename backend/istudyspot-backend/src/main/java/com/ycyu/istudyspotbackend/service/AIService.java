package com.ycyu.istudyspotbackend.service;

import com.ycyu.istudyspotbackend.entity.Character;
import com.ycyu.istudyspotbackend.entity.Session;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AIService {
    List<Character> getCharacters();
    Character getCharacter(String characterId);
    Session getOrCreateSession(String sessionId, String characterId);
    String chat(String sessionId, String characterId, String message);
    SseEmitter streamChat(String sessionId, String characterId, String message);
}
