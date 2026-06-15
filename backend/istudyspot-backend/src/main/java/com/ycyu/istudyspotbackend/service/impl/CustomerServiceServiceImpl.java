package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.ai.AiRules;
import com.ycyu.istudyspotbackend.ai.AiRulesRegistry;
import com.ycyu.istudyspotbackend.entity.CustomerServiceMessage;
import com.ycyu.istudyspotbackend.service.CustomerServiceService;
import com.ycyu.istudyspotbackend.service.DeepSeekService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CustomerServiceServiceImpl implements CustomerServiceService {

    @Autowired
    private DeepSeekService deepSeekService;

    private final AiRules rules;
    private final AiRules.CustomerService customerServiceRules;
    private final Map<String, List<CustomerServiceMessage>> sessionMessages = new ConcurrentHashMap<>();

    public CustomerServiceServiceImpl() {
        this.rules = AiRulesRegistry.getRules();
        this.customerServiceRules = rules.getCustomerService();
    }

    @Override
    public String getWelcomeMessage() {
        return customerServiceRules.getWelcomeMessage();
    }

    @Override
    public List<String> getRecommendedQuestions() {
        return customerServiceRules.getRecommendedQuestions();
    }

    @Override
    public String chatWithCustomerService(String sessionId, String message) {
        String resolvedSessionId = normalizeSessionId(sessionId);
        String normalizedMessage = normalizeMessage(message);
        saveMessage(resolvedSessionId, "user", normalizedMessage);

        List<Map<String, String>> messages = buildMessages(resolvedSessionId);
        String response = deepSeekService.chat("deepseek-chat", messages);
        String safeResponse = sanitizeResponse(response);

        saveMessage(resolvedSessionId, "assistant", safeResponse);
        return safeResponse;
    }

    @Override
    public SseEmitter streamChatWithCustomerService(String sessionId, String message) {
        String resolvedSessionId = normalizeSessionId(sessionId);
        String normalizedMessage = normalizeMessage(message);
        saveMessage(resolvedSessionId, "user", normalizedMessage);

        List<Map<String, String>> messages = buildMessages(resolvedSessionId);
        SseEmitter emitter = new SseEmitter(300000L);
        StringBuilder assistantReply = new StringBuilder();

        deepSeekService.streamChat("deepseek-chat", messages,
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
                            ? customerServiceRules.getFallbackReply()
                            : assistantReply.toString();
                    saveMessage(resolvedSessionId, "assistant", finalReply);
                    emitter.complete();
                },
                error -> {
                    try {
                        emitter.send(SseEmitter.event().data("{\"type\": \"error\", \"message\": \"STREAM_ERROR\"}"));
                    } catch (IOException ignored) {
                    }
                    emitter.completeWithError(error);
                });

        return emitter;
    }

    @Override
    public List<CustomerServiceMessage> getSessionHistory(String sessionId) {
        return sessionMessages.getOrDefault(normalizeSessionId(sessionId), new ArrayList<>());
    }

    private void saveMessage(String sessionId, String role, String content) {
        CustomerServiceMessage message = new CustomerServiceMessage();
        message.setId(UUID.randomUUID().toString());
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());

        sessionMessages.computeIfAbsent(sessionId, ignored -> new ArrayList<>()).add(message);
    }

    private List<Map<String, String>> buildMessages(String sessionId) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(createMessage("system", buildSystemPrompt()));

        List<CustomerServiceMessage> history = sessionMessages.getOrDefault(sessionId, new ArrayList<>());
        int startIndex = Math.max(0, history.size() - rules.getConversation().getHistoryLimit());
        for (int i = startIndex; i < history.size(); i++) {
            CustomerServiceMessage historyMessage = history.get(i);
            messages.add(createMessage(historyMessage.getRole(), historyMessage.getContent()));
        }
        return messages;
    }

    private String buildSystemPrompt() {
        StringBuilder builder = new StringBuilder();
        builder.append("你是 iStudySpot 自习室的智能客服 ").append(customerServiceRules.getAssistantName()).append("。\n");
        builder.append("你的目标是帮助用户完成预约、自习室使用、订单操作与规则咨询相关问题。\n\n");
        builder.append("已知服务事实：\n");
        appendBulletList(builder, customerServiceRules.getServiceFacts());
        builder.append("\n回答规则：\n");
        appendBulletList(builder, customerServiceRules.getResponseRules());
        builder.append("\n通用对话规则：\n");
        appendBulletList(builder, rules.getConversation().getSystemGuidelines());
        builder.append("\n回答要求：\n");
        appendBulletList(builder, rules.getConversation().getResponseDirectives());
        builder.append("\n请基于这些信息回答用户问题。");
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

    private String normalizeSessionId(String sessionId) {
        return sessionId == null || sessionId.isBlank() ? "__customer_service__" : sessionId;
    }

    private String normalizeMessage(String message) {
        return message == null ? "" : message.trim();
    }

    private String sanitizeResponse(String response) {
        if (response == null || response.isBlank() || response.startsWith("Error")) {
            return customerServiceRules.getFallbackReply();
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
