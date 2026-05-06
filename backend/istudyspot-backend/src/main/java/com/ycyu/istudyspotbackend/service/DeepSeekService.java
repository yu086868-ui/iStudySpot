package com.ycyu.istudyspotbackend.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface DeepSeekService {
    /**
     * 非流式调用 DeepSeek API
     */
    String chat(String model, List<Map<String, String>> messages);

    /**
     * 流式调用 DeepSeek API
     */
    void streamChat(String model, List<Map<String, String>> messages, Consumer<String> onData, Runnable onComplete, Consumer<Throwable> onError) throws IOException;
}
