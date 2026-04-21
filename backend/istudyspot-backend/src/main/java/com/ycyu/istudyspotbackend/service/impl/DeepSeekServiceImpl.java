package com.ycyu.istudyspotbackend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycyu.istudyspotbackend.config.DeepSeekConfig;
import com.ycyu.istudyspotbackend.service.DeepSeekService;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class DeepSeekServiceImpl implements DeepSeekService {

    @Autowired
    private DeepSeekConfig deepSeekConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Override
    public String chat(String model, List<Map<String, String>> messages) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 构建请求体
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", messages,
                    "stream", false
            );

            // 创建 HTTP POST 请求
            HttpPost httpPost = new HttpPost(deepSeekConfig.getApiUrl() + "/chat/completions");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + deepSeekConfig.getApiKey());
            httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(requestBody)));

            // 发送请求并获取响应
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                System.out.println("DeepSeek API status code: " + statusCode);
                
                // 使用 UTF-8 编码读取响应
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                System.out.println("DeepSeek API response: " + responseBody);
                
                // 解析响应
                Map<?, ?> responseMap = objectMapper.readValue(responseBody, Map.class);
                
                // 检查是否有错误
                if (responseMap.containsKey("error")) {
                    Map<?, ?> error = (Map<?, ?>) responseMap.get("error");
                    String errorMessage = (String) error.get("message");
                    System.out.println("DeepSeek API error: " + errorMessage);
                    return "Error from DeepSeek API: " + errorMessage;
                }
                
                List<?> choices = (List<?>) responseMap.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<?, ?> choice = (Map<?, ?>) choices.get(0);
                    Map<?, ?> message = (Map<?, ?>) choice.get("message");
                    return (String) message.get("content");
                }
                return "No response from DeepSeek API";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error calling DeepSeek API: " + e.getMessage();
        }
    }

    @Override
    public SseEmitter streamChat(String model, List<Map<String, String>> messages) throws IOException {
        SseEmitter emitter = new SseEmitter();

        executorService.execute(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                // 构建请求体
                Map<String, Object> requestBody = Map.of(
                        "model", model,
                        "messages", messages,
                        "stream", true
                );

                // 创建 HTTP POST 请求
                HttpPost httpPost = new HttpPost(deepSeekConfig.getApiUrl() + "/chat/completions");
                httpPost.setHeader("Content-Type", "application/json");
                httpPost.setHeader("Authorization", "Bearer " + deepSeekConfig.getApiKey());
                httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(requestBody)));

                // 发送请求并获取响应
                try (CloseableHttpResponse response = httpClient.execute(httpPost);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {

                    // 发送开始事件
                    emitter.send(SseEmitter.event().data("{\"type\": \"start\"}"));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String jsonStr = line.substring(6).trim();
                            if ("[DONE]".equals(jsonStr)) {
                                break;
                            }

                            try {
                                // 解析响应
                                Map<?, ?> responseMap = objectMapper.readValue(jsonStr, Map.class);
                                List<?> choices = (List<?>) responseMap.get("choices");
                                if (choices != null && !choices.isEmpty()) {
                                    Map<?, ?> choice = (Map<?, ?>) choices.get(0);
                                    Map<?, ?> delta = (Map<?, ?>) choice.get("delta");
                                    String content = (String) delta.get("content");
                                    if (content != null && !content.isEmpty()) {
                                        emitter.send(SseEmitter.event().data("{\"type\": \"delta\", \"content\": \"" + content + "\"}"));
                                    }
                                }
                            } catch (Exception e) {
                                // 忽略解析错误
                            }
                        }
                    }

                    // 发送结束事件
                    emitter.send(SseEmitter.event().data("{\"type\": \"end\"}"));
                    emitter.complete();
                } catch (Exception e) {
                    emitter.completeWithError(e);
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}
