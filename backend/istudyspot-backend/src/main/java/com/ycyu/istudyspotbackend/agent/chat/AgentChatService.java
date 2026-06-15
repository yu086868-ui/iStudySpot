package com.ycyu.istudyspotbackend.agent.chat;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AgentChatService {
    AgentChatResponse chat(Long userId, AgentChatRequest request);
    SseEmitter streamChat(Long userId, AgentChatRequest request);
}
