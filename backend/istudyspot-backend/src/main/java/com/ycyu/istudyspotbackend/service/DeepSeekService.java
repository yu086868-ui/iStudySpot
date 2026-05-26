package com.ycyu.istudyspotbackend.service;

import java.util.List;
import java.util.Map;

public interface DeepSeekService {
    String generateCardContent(String rarity, String themeCategory);
    String chat(String model, List<Map<String, String>> messages);
    void streamChat(String model, List<Map<String, String>> messages, 
                    java.util.function.Consumer<String> onMessage,
                    java.lang.Runnable onComplete,
                    java.util.function.Consumer<Throwable> onError);
}