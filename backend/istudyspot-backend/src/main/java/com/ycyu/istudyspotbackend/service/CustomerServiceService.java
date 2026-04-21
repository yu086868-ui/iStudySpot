package com.ycyu.istudyspotbackend.service;

import com.ycyu.istudyspotbackend.entity.CustomerServiceMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

public interface CustomerServiceService {
    /**
     * 获取智能客服欢迎消息
     */
    String getWelcomeMessage();

    /**
     * 获取推荐问题列表
     */
    List<String> getRecommendedQuestions();

    /**
     * 非流式智能客服聊天
     */
    String chatWithCustomerService(String sessionId, String message);

    /**
     * 流式智能客服聊天
     */
    SseEmitter streamChatWithCustomerService(String sessionId, String message) throws IOException;

    /**
     * 获取会话历史
     */
    List<CustomerServiceMessage> getSessionHistory(String sessionId);
}
