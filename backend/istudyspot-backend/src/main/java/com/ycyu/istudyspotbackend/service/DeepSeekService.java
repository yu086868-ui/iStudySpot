package com.ycyu.istudyspotbackend.service;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

public interface DeepSeekService {
    String generateCardContent(String rarity, String themeCategory);
    String chat(String model, List<Map<String, String>> messages);
    JsonNode chatCompletion(String model, List<Map<String, Object>> messages, List<Map<String, Object>> tools);
    void streamChat(String model, List<Map<String, String>> messages, 
                    java.util.function.Consumer<String> onMessage,
                    java.lang.Runnable onComplete,
                    java.util.function.Consumer<Throwable> onError);
}
