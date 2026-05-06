package com.ycyu.istudyspotbackend.service.impl;

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

@Service
public class CustomerServiceServiceImpl implements CustomerServiceService {

    @Autowired
    private DeepSeekService deepSeekService;

    private final Map<String, List<CustomerServiceMessage>> sessionMessages = new HashMap<>();

    private final List<String> recommendedQuestions = List.of(
            "如何预订自习室座位",
            "如何查看自习室使用情况",
            "如何取消预订",
            "自习室开放时间",
            "座位价格是多少",
            "如何延长预订时间",
            "是否可以更换座位",
            "积分如何使用"
    );

    private final String systemPrompt = "你是智能助手小i，专门为用户提供自习室预订系统的咨询服务。请根据以下规则回答用户的问题：\n" +
            "\n" +
            "服务信息：\n" +
            "- 自习室名称：iStudySpot自习室\n" +
            "- 开放时间：周一至周日 07:00 - 23:00\n" +
            "- 座位类型：普通座、VIP座、学习包间\n" +
            "- 价格：普通座 10元/小时，VIP座 15元/小时，包间 30元/小时\n" +
            "- 积分规则：每消费1元获得1积分，100积分可兑换1小时免费时长\n" +
            "\n" +
            "回答规则：\n" +
            "1. 回答要专业、准确，与自习室预订系统相关\n" +
            "2. 回答要简洁明了，避免使用复杂的专业术语\n" +
            "3. 当用户的问题与自习室预订无关时，礼貌地引导用户回到自习室预订相关的话题\n" +
            "4. 当用户询问具体操作步骤时，提供详细的步骤说明\n" +
            "5. 当用户询问开放时间、规则等信息时，提供准确的信息\n" +
            "6. 当用户遇到问题时，提供解决方案和建议\n" +
            "7. 保持友好、耐心的服务态度，使用亲切的语气\n" +
            "8. 如果不知道答案，请如实告知并建议用户联系人工客服\n" +
            "\n" +
            "请用中文友好地回答用户的问题。";

    @Override
    public String getWelcomeMessage() {
        return "尊敬的用户，您好！我是智能助手小i，很高兴为您服务。\n\n" +
                "iStudySpot自习室为您提供安静舒适的学习环境，开放时间为每日07:00-23:00。\n\n" +
                "您可以向我咨询：预订流程、座位查询、开放时间、价格信息等问题。";
    }

    @Override
    public List<String> getRecommendedQuestions() {
        return recommendedQuestions;
    }

    @Override
    public String chatWithCustomerService(String sessionId, String message) {
        saveMessage(sessionId, "user", message);

        List<Map<String, String>> messages = buildMessages(sessionId, message);

        String response = deepSeekService.chat("deepseek-chat", messages);

        saveMessage(sessionId, "assistant", response);

        return response;
    }

    @Override
    public SseEmitter streamChatWithCustomerService(String sessionId, String message) throws IOException {
        saveMessage(sessionId, "user", message);

        List<Map<String, String>> messages = buildMessages(sessionId, message);

        SseEmitter emitter = new SseEmitter(300000L);

        deepSeekService.streamChat("deepseek-chat", messages,
            chunk -> {
                try {
                    emitter.send(SseEmitter.event().data(chunk));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            },
            emitter::complete,
            e -> {
                try {
                    emitter.send(SseEmitter.event().data("{\"type\": \"error\", \"message\": \"STREAM_ERROR\"}"));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                emitter.completeWithError(e);
            });

        return emitter;
    }

    @Override
    public List<CustomerServiceMessage> getSessionHistory(String sessionId) {
        return sessionMessages.getOrDefault(sessionId, new ArrayList<>());
    }

    private void saveMessage(String sessionId, String role, String content) {
        CustomerServiceMessage message = new CustomerServiceMessage();
        message.setId(UUID.randomUUID().toString());
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());

        sessionMessages.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(message);
    }

    private List<Map<String, String>> buildMessages(String sessionId, String currentMessage) {
        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages.add(systemMessage);

        List<CustomerServiceMessage> history = sessionMessages.getOrDefault(sessionId, new ArrayList<>());
        int startIndex = Math.max(0, history.size() - 10);
        for (int i = startIndex; i < history.size(); i++) {
            CustomerServiceMessage msg = history.get(i);
            Map<String, String> message = new HashMap<>();
            message.put("role", msg.getRole());
            message.put("content", msg.getContent());
            messages.add(message);
        }

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", currentMessage);
        messages.add(userMessage);

        return messages;
    }
}