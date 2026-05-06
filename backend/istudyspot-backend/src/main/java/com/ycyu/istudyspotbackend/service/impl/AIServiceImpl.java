package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.Character;
import com.ycyu.istudyspotbackend.entity.Message;
import com.ycyu.istudyspotbackend.entity.Session;
import com.ycyu.istudyspotbackend.service.AIService;
import com.ycyu.istudyspotbackend.service.DeepSeekService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class AIServiceImpl implements AIService {

    @Autowired
    private DeepSeekService deepSeekService;

    private final List<Character> characters;
    private final Map<String, Session> sessions;
    private final ExecutorService executorService;

    public AIServiceImpl() {
        this.characters = new ArrayList<>();
        characters.add(new Character("scientist", "科学家", "理性严谨，喜欢解释原理", "逻辑清晰，偏长句"));
        characters.add(new Character("teacher", "老师", "耐心细致，善于引导", "温和亲切，鼓励式"));
        characters.add(new Character("artist", "艺术家", "富有创意，情感丰富", "感性表达，富有想象力"));
        characters.add(new Character("customer_service", "小i", "热情友好，专业细致", "亲切自然，简洁明了"));

        this.sessions = new HashMap<>();
        this.executorService = Executors.newFixedThreadPool(10);
    }

    @Override
    public List<Character> getCharacters() {
        return characters;
    }

    @Override
    public Character getCharacter(String characterId) {
        return characters.stream()
                .filter(character -> character.getId().equals(characterId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Session getOrCreateSession(String sessionId, String characterId) {
        return sessions.computeIfAbsent(sessionId, id -> new Session(id, characterId));
    }

    @Override
    public String chat(String sessionId, String characterId, String message) {
        Session session = getOrCreateSession(sessionId, characterId);

        Character character = getCharacter(characterId);
        if (character == null) {
            throw new IllegalArgumentException("Invalid character ID");
        }

        session.addMessage(new Message("user", message));

        String systemPrompt = buildSystemPrompt(character);

        List<Map<String, String>> messages = new ArrayList<>();
        
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages.add(systemMessage);

        List<Message> recentMessages = session.getRecentMessages(10);
        for (Message msg : recentMessages) {
            Map<String, String> msgMap = new HashMap<>();
            msgMap.put("role", msg.getRole());
            msgMap.put("content", msg.getContent());
            messages.add(msgMap);
        }

        String response = deepSeekService.chat("deepseek-chat", messages);

        session.addMessage(new Message("assistant", response));

        return response;
    }

    @Override
    public SseEmitter streamChat(String sessionId, String characterId, String message) {
        SseEmitter emitter = new SseEmitter();

        executorService.submit(() -> {
            try {
                Session session = getOrCreateSession(sessionId, characterId);

                Character character = getCharacter(characterId);
                if (character == null) {
                    emitter.send(SseEmitter.event().data("{\"type\": \"error\", \"message\": \"INVALID_CHARACTER\"}"));
                    emitter.complete();
                    return;
                }

                session.addMessage(new Message("user", message));

                String systemPrompt = buildSystemPrompt(character);

                List<Map<String, String>> messages = new ArrayList<>();
                
                Map<String, String> systemMessage = new HashMap<>();
                systemMessage.put("role", "system");
                systemMessage.put("content", systemPrompt);
                messages.add(systemMessage);

                List<Message> recentMessages = session.getRecentMessages(10);
                for (Message msg : recentMessages) {
                    Map<String, String> msgMap = new HashMap<>();
                    msgMap.put("role", msg.getRole());
                    msgMap.put("content", msg.getContent());
                    messages.add(msgMap);
                }

                SseEmitter deepSeekEmitter = deepSeekService.streamChat("deepseek-chat", messages);
                
                deepSeekEmitter.onCompletion(() -> {
                    try {
                        emitter.complete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                
                deepSeekEmitter.onError(e -> {
                    try {
                        emitter.send(SseEmitter.event().data("{\"type\": \"error\", \"message\": \"STREAM_ERROR\"}"));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    emitter.completeWithError(e);
                });
                
                deepSeekEmitter.onTimeout(() -> {
                    try {
                        emitter.send(SseEmitter.event().data("{\"type\": \"error\", \"message\": \"TIMEOUT\"}"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    emitter.complete();
                });

            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event().data("{\"type\": \"error\", \"message\": \"INTERNAL_ERROR\"}"));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    private String buildSystemPrompt(Character character) {
        return "你正在扮演一个角色，请严格遵守以下设定：\n" +
                "\n" +
                "角色名称：" + character.getName() + "\n" +
                "性格：" + character.getPersona() + "\n" +
                "说话风格：" + character.getSpeaking_style() + "\n" +
                "\n" +
                "要求：\n" +
                "- 始终保持角色语气\n" +
                "- 不要提到自己是AI\n" +
                "- 不要跳出角色\n" +
                "- 回答要友好、有帮助\n" +
                "\n" +
                "请根据对话继续交流。";
    }
}