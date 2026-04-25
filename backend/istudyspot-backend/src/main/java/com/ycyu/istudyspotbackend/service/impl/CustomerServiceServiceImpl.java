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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class CustomerServiceServiceImpl implements CustomerServiceService {

    @Autowired
    private DeepSeekService deepSeekService;

    private final Map<String, List<CustomerServiceMessage>> sessionMessages = new HashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    // 推荐问题列表
    private final List<String> recommendedQuestions = List.of(
            "如何预订自习室",
            "如何查看自习室使用情况",
            "如何取消预订",
            "自习室开放时间"
    );

    // 系统提示
    private final String systemPrompt = "你是智能助手小i，专门为用户提供自习室预订系统的咨询服务。请根据以下规则回答用户的问题：\n" +
            "1. 回答要专业、准确，与自习室预订系统相关\n" +
            "2. 回答要简洁明了，避免使用复杂的专业术语\n" +
            "3. 当用户的问题与自习室预订无关时，礼貌地引导用户回到自习室预订相关的话题\n" +
            "4. 当用户询问具体操作步骤时，提供详细的步骤说明\n" +
            "5. 当用户询问开放时间、规则等信息时，提供准确的信息\n" +
            "6. 当用户遇到问题时，提供解决方案和建议\n" +
            "7. 保持友好、耐心的服务态度";

    @Override
    public String getWelcomeMessage() {
        return "尊敬的用户，您好！我是智能助手小i，很高兴为您服务。您可以向我咨询自习室预订相关的问题，如预订流程、开放时间等。";
    }

    @Override
    public List<String> getRecommendedQuestions() {
        return recommendedQuestions;
    }

    @Override
    public String chatWithCustomerService(String sessionId, String message) {
        // 保存用户消息
        saveMessage(sessionId, "user", message);

        // 构建消息列表，包括系统提示和对话历史
        List<Map<String, String>> messages = buildMessages(sessionId, message);

        // 调用 DeepSeek API 获取智能客服回复
        String response = deepSeekService.chat("deepseek-chat", messages);

        // 保存客服回复
        saveMessage(sessionId, "assistant", response);

        return response;
    }

    @Override
    public SseEmitter streamChatWithCustomerService(String sessionId, String message) throws IOException {
        // 保存用户消息
        saveMessage(sessionId, "user", message);

        // 构建消息列表，包括系统提示和对话历史
        List<Map<String, String>> messages = buildMessages(sessionId, message);

        // 调用 DeepSeek API 获取流式智能客服回复
        return deepSeekService.streamChat("deepseek-chat", messages);
    }

    @Override
    public List<CustomerServiceMessage> getSessionHistory(String sessionId) {
        return sessionMessages.getOrDefault(sessionId, new ArrayList<>());
    }

    // 保存消息到会话历史
    private void saveMessage(String sessionId, String role, String content) {
        CustomerServiceMessage message = new CustomerServiceMessage();
        message.setId(UUID.randomUUID().toString());
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());

        sessionMessages.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(message);
    }

    // 构建消息列表，包括系统提示和对话历史
    private List<Map<String, String>> buildMessages(String sessionId, String currentMessage) {
        List<Map<String, String>> messages = new ArrayList<>();

        // 添加系统提示
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages.add(systemMessage);

        // 添加对话历史
        List<CustomerServiceMessage> history = sessionMessages.getOrDefault(sessionId, new ArrayList<>());
        for (CustomerServiceMessage msg : history) {
            Map<String, String> message = new HashMap<>();
            message.put("role", msg.getRole());
            message.put("content", msg.getContent());
            messages.add(message);
        }

        // 添加当前用户消息
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", currentMessage);
        messages.add(userMessage);

        return messages;
    }
}
