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
import java.util.HashMap;
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
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("stream", false);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1024);

            // 创建 HTTP POST 请求
            HttpPost httpPost = new HttpPost(deepSeekConfig.getApiUrl() + "/chat/completions");
            httpPost.setHeader("Content-Type", "application/json; charset=utf-8");
            httpPost.setHeader("Authorization", "Bearer " + deepSeekConfig.getApiKey());
            httpPost.setHeader("Accept", "application/json; charset=utf-8");
            httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(requestBody), ContentType.APPLICATION_JSON));

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
                    String content = (String) message.get("content");
                    if (content != null) {
                        // 检查是否有问号问题
                        if (content.contains("????????")) {
                            System.out.println("DeepSeek API response contains garbled characters");
                            return "Sorry, I can't understand the response from the server. Please try again later.";
                        }
                        return content;
                    }
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
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", model);
                requestBody.put("messages", messages);
                requestBody.put("stream", true);
                requestBody.put("temperature", 0.7);
                requestBody.put("max_tokens", 1024);

                // 创建 HTTP POST 请求
                HttpPost httpPost = new HttpPost(deepSeekConfig.getApiUrl() + "/chat/completions");
                httpPost.setHeader("Content-Type", "application/json; charset=utf-8");
                httpPost.setHeader("Authorization", "Bearer " + deepSeekConfig.getApiKey());
                httpPost.setHeader("Accept", "text/event-stream; charset=utf-8");
                httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(requestBody), ContentType.APPLICATION_JSON));

                // 发送请求并获取响应
                try (CloseableHttpResponse response = httpClient.execute(httpPost);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))) {

                    // 发送开始事件
                    emitter.send(SseEmitter.event().data("{\"type\": \"start\"}"));

                    String line;
                    boolean hasGarbled = false;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String jsonStr = line.substring(6).trim();
                            if ("[DONE]".equals(jsonStr)) {
                                break;
                            }

                            try {
                                // 解析响应
                                Map<?, ?> responseMap = objectMapper.readValue(jsonStr, Map.class);
                                
                                // 检查是否有错误
                                if (responseMap.containsKey("error")) {
                                    Map<?, ?> error = (Map<?, ?>) responseMap.get("error");
                                    String errorMessage = (String) error.get("message");
                                    System.out.println("DeepSeek API error: " + errorMessage);
                                    emitter.send(SseEmitter.event().data("{\"type\": \"error\", \"message\": \"Error from DeepSeek API: " + errorMessage + "\"}"));
                                    hasGarbled = true;
                                    break;
                                }
                                
                                List<?> choices = (List<?>) responseMap.get("choices");
                                if (choices != null && !choices.isEmpty()) {
                                    Map<?, ?> choice = (Map<?, ?>) choices.get(0);
                                    Map<?, ?> delta = (Map<?, ?>) choice.get("delta");
                                    String content = (String) delta.get("content");
                                    if (content != null && !content.isEmpty()) {
                                        // 检查是否有问号问题
                                        if (content.contains("????????")) {
                                            System.out.println("DeepSeek API response contains garbled characters");
                                            emitter.send(SseEmitter.event().data("{\"type\": \"error\", \"message\": \"Sorry, I can't understand the response from the server. Please try again later.\"}"));
                                            hasGarbled = true;
                                            break;
                                        }
                                        emitter.send(SseEmitter.event().data("{\"type\": \"delta\", \"content\": \"" + content + "\"}"));
                                    }
                                }
                            } catch (Exception e) {
                                // 忽略解析错误
                                System.out.println("Error parsing response: " + e.getMessage());
                            }
                        }
                    }

                    if (!hasGarbled) {
                        // 发送结束事件
                        emitter.send(SseEmitter.event().data("{\"type\": \"end\"}"));
                    }
                    emitter.complete();
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        emitter.send(SseEmitter.event().data("{\"type\": \"error\", \"message\": \"Error calling DeepSeek API\"}"));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    emitter.completeWithError(e);
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    emitter.send(SseEmitter.event().data("{\"type\": \"error\", \"message\": \"Error calling DeepSeek API\"}"));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}
