package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.ai.AiRules;
import com.ycyu.istudyspotbackend.ai.AiRulesRegistry;
import com.ycyu.istudyspotbackend.entity.AICharacter;
import com.ycyu.istudyspotbackend.entity.Message;
import com.ycyu.istudyspotbackend.entity.Session;
import com.ycyu.istudyspotbackend.service.AIService;
import com.ycyu.istudyspotbackend.service.DeepSeekService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class AIServiceImpl implements AIService {

    @Autowired
    private DeepSeekService deepSeekService;

    private final AiRules rules;
    private final List<AICharacter> characters;
    private final Map<String, AiRules.CharacterRule> characterRules;
    private final Map<String, Session> sessions;
    private final ExecutorService executorService;

    public AIServiceImpl() {
        this.rules = AiRulesRegistry.getRules();
        this.characterRules = rules.getCharacters().stream()
                .collect(Collectors.toMap(AiRules.CharacterRule::getId, rule -> rule));
        this.characters = rules.getCharacters().stream()
                .map(AiRules.CharacterRule::toCharacter)
                .collect(Collectors.toCollection(ArrayList::new));
        this.sessions = new ConcurrentHashMap<>();
        this.executorService = Executors.newFixedThreadPool(10);
    }

    @Override
    public List<AICharacter> getCharacters() {
        return Collections.unmodifiableList(characters);
    }

    @Override
    public AICharacter getCharacter(String characterId) {
        AiRules.CharacterRule rule = getCharacterRule(characterId);
        return rule.toCharacter();
    }

    @Override
    public Session getOrCreateSession(String sessionId, String characterId) {
        String resolvedSessionId = sessionId == null ? "__anonymous__" : sessionId;
        return sessions.computeIfAbsent(resolvedSessionId, id -> new Session(sessionId, resolveCharacterId(characterId)));
    }

    @Override
    public String chat(String sessionId, String characterId, String message) {
        Session session = getOrCreateSession(sessionId, characterId);
        AiRules.CharacterRule characterRule = getCharacterRule(characterId);

        session.setCharacter_id(characterRule.getId());
        session.addMessage(new Message("user", normalizeMessage(message)));

        List<Map<String, String>> promptMessages = buildPromptMessages(session, characterRule);
        String response = deepSeekService.chat("deepseek-chat", promptMessages);
        String safeResponse = sanitizeResponse(response, characterRule.getFallbackReply());

        session.addMessage(new Message("assistant", safeResponse));
        return safeResponse;
    }

    @Override
    public SseEmitter streamChat(String sessionId, String characterId, String message) {
        SseEmitter emitter = new SseEmitter(300000L);

        executorService.submit(() -> {
            try {
                Session session = getOrCreateSession(sessionId, characterId);
                AiRules.CharacterRule characterRule = getCharacterRule(characterId);

                session.setCharacter_id(characterRule.getId());
                session.addMessage(new Message("user", normalizeMessage(message)));

                List<Map<String, String>> promptMessages = buildPromptMessages(session, characterRule);
                StringBuilder assistantReply = new StringBuilder();

                deepSeekService.streamChat("deepseek-chat", promptMessages,
                        chunk -> {
                            try {
                                String normalizedChunk = normalizeStreamChunk(chunk, assistantReply);
                                emitter.send(SseEmitter.event().data(normalizedChunk));
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        () -> {
                            String finalReply = assistantReply.length() == 0
                                    ? characterRule.getFallbackReply()
                                    : assistantReply.toString();
                            session.addMessage(new Message("assistant", finalReply));
                            emitter.complete();
                        },
                        error -> {
                            try {
                                emitter.send(SseEmitter.event().data("{\"type\": \"error\", \"message\": \"STREAM_ERROR\"}"));
                            } catch (IOException ignored) {
                            }
                            emitter.completeWithError(error);
                        });
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event().data("{\"type\": \"error\", \"message\": \"INTERNAL_ERROR\"}"));
                } catch (IOException ignored) {
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    private List<Map<String, String>> buildPromptMessages(Session session, AiRules.CharacterRule characterRule) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(createMessage("system", buildSystemPrompt(characterRule)));

        for (Message historyMessage : session.getRecentMessages(rules.getConversation().getHistoryLimit())) {
            messages.add(createMessage(historyMessage.getRole(), normalizeMessage(historyMessage.getContent())));
        }
        return messages;
    }

    private String buildSystemPrompt(AiRules.CharacterRule characterRule) {
        StringBuilder builder = new StringBuilder();
        builder.append("你正在 iStudySpot 中扮演一个固定角色，请严格遵守以下设定。\n\n");
        builder.append("角色名称：").append(characterRule.getName()).append("\n");
        builder.append("角色性格：").append(characterRule.getPersona()).append("\n");
        builder.append("说话风格：").append(characterRule.getSpeakingStyle()).append("\n");

        if (!characterRule.getSpecialties().isEmpty()) {
            builder.append("擅长领域：").append(String.join("、", characterRule.getSpecialties())).append("\n");
        }

        builder.append("\n全局规则：\n");
        appendBulletList(builder, rules.getConversation().getSystemGuidelines());
        builder.append("\n角色规则：\n");
        appendBulletList(builder, characterRule.getRules());
        builder.append("\n回答要求：\n");
        appendBulletList(builder, rules.getConversation().getResponseDirectives());
        builder.append("\n请基于以上规则继续对话。");
        return builder.toString();
    }

    private void appendBulletList(StringBuilder builder, List<String> items) {
        for (String item : items) {
            builder.append("- ").append(item).append("\n");
        }
    }

    private Map<String, String> createMessage(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private AiRules.CharacterRule getCharacterRule(String characterId) {
        String resolvedId = resolveCharacterId(characterId);
        return characterRules.getOrDefault(resolvedId, characterRules.get(rules.getConversation().getDefaultCharacterId()));
    }

    private String resolveCharacterId(String characterId) {
        if (characterId == null || characterId.isBlank()) {
            return rules.getConversation().getDefaultCharacterId();
        }
        return characterId;
    }

    private String normalizeMessage(String message) {
        return message == null ? "" : message.trim();
    }

    private String sanitizeResponse(String response, String fallbackReply) {
        if (response == null || response.isBlank()) {
            return fallbackReply != null && !fallbackReply.isBlank()
                    ? fallbackReply
                    : rules.getConversation().getFallbackReply();
        }
        if (response.startsWith("Error")) {
            return fallbackReply != null && !fallbackReply.isBlank()
                    ? fallbackReply
                    : rules.getConversation().getFallbackReply();
        }
        return response.trim();
    }

    private String normalizeStreamChunk(String chunk, StringBuilder assistantReply) {
        if (chunk == null || chunk.isBlank()) {
            return "{\"type\": \"delta\", \"content\": \"\"}";
        }
        if (chunk.startsWith("{\"type\":")) {
            if (chunk.contains("\"content\":")) {
                int contentStart = chunk.indexOf("\"content\": \"");
                if (contentStart >= 0) {
                    int valueStart = contentStart + 12;
                    int valueEnd = chunk.lastIndexOf("\"");
                    if (valueEnd > valueStart) {
                        assistantReply.append(chunk, valueStart, valueEnd);
                    }
                }
            }
            return chunk;
        }

        assistantReply.append(chunk);
        return "{\"type\": \"delta\", \"content\": \"" + escapeJson(chunk) + "\"}";
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
