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

        // 模拟智能客服回复
        String response = generateCustomerServiceResponse(message);

        // 保存客服回复
        saveMessage(sessionId, "assistant", response);

        return response;
    }

    @Override
    public SseEmitter streamChatWithCustomerService(String sessionId, String message) throws IOException {
        // 保存用户消息
        saveMessage(sessionId, "user", message);

        // 模拟智能客服回复
        String response = generateCustomerServiceResponse(message);

        // 保存客服回复
        saveMessage(sessionId, "assistant", response);

        // 创建 SSE 发射器
        SseEmitter emitter = new SseEmitter();

        // 异步发送流式响应
        executorService.execute(() -> {
            try {
                // 发送开始事件
                emitter.send(SseEmitter.event().data("{\"type\": \"start\"}"));

                // 模拟流式输出
                for (int i = 0; i < response.length(); i++) {
                    char c = response.charAt(i);
                    emitter.send(SseEmitter.event().data("{\"type\": \"delta\", \"content\": \"" + c + "\"}"));
                    Thread.sleep(50); // 模拟打字效果
                }

                // 发送结束事件
                emitter.send(SseEmitter.event().data("{\"type\": \"end\"}"));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    // 模拟生成智能客服回复
    private String generateCustomerServiceResponse(String message) {
        if (message.contains("预订")) {
            return "您可以通过以下步骤预订自习室：1. 登录系统；2. 选择自习室和座位；3. 选择预订时间；4. 确认预订。预订成功后，您会收到预订成功的通知。";
        } else if (message.contains("查看")) {
            return "您可以通过以下方式查看自习室使用情况：1. 登录系统；2. 进入自习室列表页面；3. 查看每个自习室的当前使用情况和可预订时间。";
        } else if (message.contains("取消")) {
            return "您可以通过以下步骤取消预订：1. 登录系统；2. 进入我的预订页面；3. 选择要取消的预订；4. 点击取消按钮。请注意，提前取消预订可以避免影响您的信用评分。";
        } else if (message.contains("开放时间")) {
            return "自习室的开放时间为：周一至周五 8:00-22:00，周末 9:00-21:00。具体开放时间可能会根据节假日进行调整，请关注系统通知。";
        } else if (message.contains("签到")) {
            return "您可以通过以下步骤进行签到：1. 到达自习室后，打开系统；2. 进入我的预订页面；3. 找到当前预订；4. 点击签到按钮。请注意，需要在预订开始时间前后15分钟内完成签到，否则预订可能会被取消。";
        } else if (message.contains("签退")) {
            return "您可以通过以下步骤进行签退：1. 离开自习室前，打开系统；2. 进入我的预订页面；3. 找到当前使用中的预订；4. 点击签退按钮。签退后，系统会记录您的使用时长。";
        } else if (message.contains("你好") || message.contains("您好")) {
            return "您好！我是智能客服小i，很高兴为您服务。请问您需要了解关于自习室预订的什么信息？您可以询问如何预订自习室、如何查看自习室使用情况、如何取消预订、自习室开放时间等问题。";
        } else if (message.contains("谢谢") || message.contains("感谢")) {
            return "不客气！如果您还有其他问题，随时可以向我咨询。祝您学习愉快！";
        } else {
            return "感谢您的咨询。请问您还有其他问题吗？您可以尝试询问以下问题：如何预订自习室、如何查看自习室使用情况、如何取消预订、自习室开放时间。";
        }
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
