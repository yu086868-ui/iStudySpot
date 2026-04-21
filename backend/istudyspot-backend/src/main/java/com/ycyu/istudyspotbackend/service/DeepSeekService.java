package com.ycyu.istudyspotbackend.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface DeepSeekService {
    /**
     * 非流式调用 DeepSeek API
     */
    String chat(String model, List<Map<String, String>> messages);

    /**
     * 流式调用 DeepSeek API
     */
    SseEmitter streamChat(String model, List<Map<String, String>> messages) throws IOException;
}
