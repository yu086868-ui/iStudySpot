package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.service.DeepSeekService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class DeepSeekServiceImpl implements DeepSeekService {

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.url}")
    private String apiUrl;

    @Value("${deepseek.api.model}")
    private String model;

    @Value("${deepseek.api.timeout:60000}")
    private int timeout;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final Map<String, String> RARITY_STYLE = new HashMap<>();
    static {
        RARITY_STYLE.put("N", "简洁、基础、鼓励性");
        RARITY_STYLE.put("R", "积极、成长、向上");
        RARITY_STYLE.put("SR", "深度、思考、智慧");
        RARITY_STYLE.put("SSR", "哲学、审美、意境");
        RARITY_STYLE.put("UR", "史诗、未来、宏大");
        RARITY_STYLE.put("LR", "神话、梦境、超现实");
    }

    private static final Map<String, String> THEME_DESCRIPTION = new HashMap<>();
    static {
        THEME_DESCRIPTION.put("励志成长", "关于自律、努力、学习意义、坚持的内容");
        THEME_DESCRIPTION.put("名人与历史", "引用历史人物、科学家、文学作品的智慧");
        THEME_DESCRIPTION.put("哲思感悟", "微型思考、具有余味的表达、生活哲理");
        THEME_DESCRIPTION.put("自然意象", "用星海、雨夜、山川、四季等自然景象做隐喻");
        THEME_DESCRIPTION.put("科技未来", "关于AI、太空、科幻、未来感的内容");
        THEME_DESCRIPTION.put("温柔陪伴", "理解、缓慢成长、轻陪伴的温暖内容");
        THEME_DESCRIPTION.put("隐藏主题", "神话、梦境、史诗感、超现实、宇宙诗意");
    }

    public DeepSeekServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String generateCardContent(String rarity, String themeCategory) {
        String prompt = buildPrompt(rarity, themeCategory);
        
        try {
            String response = callDeepSeekAPI(prompt);
            String result = parseResponse(response);
            if (result != null && !result.isEmpty()) {
                System.out.println("[DeepSeek API] Generated card content - Theme: " + themeCategory + ", Rarity: " + rarity);
                return result;
            }
        } catch (Exception e) {
            System.out.println("[DeepSeek API] Fallback triggered - " + e.getMessage());
        }
        return generateFallbackContent(rarity, themeCategory);
    }

    private static final List<String> TONE_VARIATIONS = Arrays.asList(
            "温暖而有力量",
            "冷静而深刻",
            "诗意而优雅",
            "理性而睿智",
            "温柔而坚定",
            "深邃而富有哲理",
            "幽默而睿智",
            "感性而细腻",
            "冷峻而犀利",
            "浪漫而唯美",
            "朴实而真诚",
            "灵动而俏皮"
    );

    private static final List<String> METAPHOR_TOPICS = Arrays.asList(
            "自然现象",
            "四季更迭",
            "植物生长",
            "天文宇宙",
            "音乐艺术",
            "旅途航行",
            "建筑构造",
            "烹饪烘焙",
            "书法绘画",
            "品茶品酒",
            "园艺种植",
            "星座神话",
            "海洋探索",
            "森林秘境",
            "古镇街巷",
            "云雾山川"
    );

    private static final List<String> NARRATIVE_PERSPECTIVES = Arrays.asList(
            "第一人称内心独白",
            "第三人称观察者视角",
            "自然景物拟人化",
            "时间旅行者的回忆",
            "来自未来的寄语",
            "历史人物的对话",
            "梦境中的启示",
            "书中精灵的低语"
    );

    private static final List<String> CREATIVE_APPROACHES = Arrays.asList(
            "使用对比手法",
            "采用逆向思维",
            "运用象征手法",
            "创造独特比喻",
            "讲述微型故事",
            "提出开放性问题",
            "引用小众知识",
            "融合跨界概念"
    );

    private String buildPrompt(String rarity, String themeCategory) {
        String style = RARITY_STYLE.getOrDefault(rarity, "简洁、鼓励性");
        String themeDesc = THEME_DESCRIPTION.getOrDefault(themeCategory, "学习成长");
        
        Random random = new Random();
        String tone = TONE_VARIATIONS.get(random.nextInt(TONE_VARIATIONS.size()));
        String metaphor = METAPHOR_TOPICS.get(random.nextInt(METAPHOR_TOPICS.size()));
        String perspective = NARRATIVE_PERSPECTIVES.get(random.nextInt(NARRATIVE_PERSPECTIVES.size()));
        String approach = CREATIVE_APPROACHES.get(random.nextInt(CREATIVE_APPROACHES.size()));

        return """
                请为学习卡片生成一段内容，要求如下：

                ## 格式要求：
                - 必须包含一个一级标题（# 开头，10字以内）
                - 必须包含一个引用（> 开头，30字以内，可以是名人名言或原创）
                - 使用 --- 作为卡片分块
                - 包含正文内容（100字左右）
                - 不使用复杂Markdown（无表格、无代码块、无嵌套列表）

                ## 创作参数：
                - 主题类别：%s
                - 风格要求：%s
                - 语气要求：%s
                - 叙事视角：%s
                - 创作手法：%s
                - 建议使用%s相关的隐喻或类比

                ## 创作原则：
                - 避免鸡汤化、内容重复、单调模板化
                - 语言要自然、有深度、有独特视角
                - 可以引用名人名言但不限于名言
                - 可以使用隐喻、类比等修辞手法
                - 内容要有余味，引人思考
                - 尝试从意想不到的角度切入主题

                ## 输出示例格式：
                # 标题
                > 引用内容
                ---
                正文内容...

                请直接输出内容，不要包含其他说明文字。
                """.formatted(themeDesc, style, tone, perspective, approach, metaphor);
    }

    private String callDeepSeekAPI(String prompt) throws Exception {
        String url = apiUrl + "/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.8);
        requestBody.put("max_tokens", 500);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);
        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("DeepSeek API request failed: " + response.getStatusCode());
        }
    }

    private String parseResponse(String response) throws Exception {
        JsonNode root = objectMapper.readTree(response);
        JsonNode choices = root.get("choices");
        
        if (choices != null && !choices.isEmpty()) {
            JsonNode message = choices.get(0).get("message");
            if (message != null) {
                String content = message.get("content").asText().trim();
                return sanitizeMarkdown(content);
            }
        }
        
        return null;
    }

    private String sanitizeMarkdown(String content) {
        content = content.replaceAll("#{2,}\\s", "# ");
        content = content.replaceAll("[*_]{2,}", "");
        content = content.replaceAll("```[\\s\\S]*?```", "");
        
        String[] lines = content.split("\n");
        StringBuilder cleaned = new StringBuilder();
        boolean inCodeBlock = false;
        
        for (String line : lines) {
            if (line.startsWith("```")) {
                inCodeBlock = !inCodeBlock;
                continue;
            }
            if (!inCodeBlock) {
                cleaned.append(line).append("\n");
            }
        }
        
        return cleaned.toString().trim();
    }

    private String generateFallbackContent(String rarity, String themeCategory) {
        String[][] contents = {
            {
                "# 学习点滴\n\n> 积跬步，至千里\n\n---\n\n每一次专注的学习，都是对未来的投资。知识的积累不需要惊天动地，只需要日复一日的坚持。",
                "# 每日精进\n\n> 学而时习之\n\n---\n\n学习是一场漫长的旅程，不在于速度，而在于持续。今天的一小步，终将成为明天的一大步。",
                "# 知识积累\n\n> 厚积薄发\n\n---\n\n知识如滴水汇成江海，每一次学习都在为未来积蓄力量。保持好奇心，保持学习的热情。"
            },
            {
                "# 成长之路\n\n> 破茧成蝶\n\n---\n\n成长从来不是一蹴而就的事。每一次挑战，每一次突破，都是蜕变的必经之路。",
                "# 自我超越\n\n> 每天进步一点点\n\n---\n\n超越昨天的自己，比超越别人更有意义。成长是一场和自己的赛跑，坚持就是胜利。",
                "# 向上攀登\n\n> 志存高远\n\n---\n\n攀登的路上也许会感到疲惫，但每一步都在接近山顶。保持信念，终会看到最美的风景。"
            },
            {
                "# 深度思考\n\n> 思想的力量\n\n---\n\n信息不等于知识，知识不等于智慧。真正的学习需要深度思考，让知识在脑海中生根发芽。",
                "# 认知升级\n\n> 打破思维定式\n\n---\n\n学习的本质是认知的升级。敢于质疑，敢于挑战，才能突破固有的思维边界，看到更广阔的世界。",
                "# 智慧之光\n\n> 思考带来觉醒\n\n---\n\n书籍是人类进步的阶梯，但真正的智慧来自于思考。学会提问，学会反思，才能获得真正的成长。"
            },
            {
                "# 哲思之旅\n\n> 静水流深\n\n---\n\n在喧嚣的世界中保持内心的宁静，在忙碌的生活中留出思考的空间。真正的智慧往往源于内心的沉淀。",
                "# 生命感悟\n\n> 岁月如歌\n\n---\n\n生命是一场体验，学习是其中最美的旋律。用心感受每一个瞬间，让知识与生命共鸣。",
                "# 内心探索\n\n> 认识你自己\n\n---\n\n学习不仅是向外探索世界，更是向内认识自己。在知识的映照下，发现内心的力量与美好。"
            },
            {
                "# 星辰大海\n\n> 探索无限可能\n\n---\n\n人类的想象力没有边界，科技的发展没有终点。保持好奇心，探索未知，未来由我们创造。",
                "# 未来可期\n\n> 科技改变世界\n\n---\n\nAI不是取代人类，而是扩展人类的能力。与机器共生，与未来对话，创造无限可能。",
                "# 宇宙回响\n\n> 超越时空的想象\n\n---\n\n从微小的原子到浩瀚的宇宙，从远古的文明到未来的世界，知识连接着过去、现在与未来。"
            },
            {
                "# 永恒传说\n\n> 书写属于你的史诗\n\n---\n\n每一个坚持学习的人，都在书写自己的传奇。你的故事，将成为激励后人的史诗。",
                "# 传奇诞生\n\n> 超越平凡的界限\n\n---\n\n伟大源于平凡的坚持。每一次学习，每一次突破，都是通往传奇的必经之路。",
                "# 命运交响\n\n> 掌握自己的命运\n\n---\n\n学习赋予我们改变命运的力量。用知识武装自己，用智慧照亮前路，书写属于自己的华章。"
            }
        };

        int index = Arrays.asList(new String[]{"N", "R", "SR", "SSR", "UR", "LR"}).indexOf(rarity);
        index = Math.min(index, contents.length - 1);
        return contents[index][new Random().nextInt(contents[index].length)];
    }

    @Override
    public String chat(String model, List<Map<String, String>> messages) {
        if (messages == null || messages.isEmpty()) {
            return "Error: messages cannot be null or empty";
        }
        
        String url = apiUrl + "/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.8);
        requestBody.put("max_tokens", 1000);
        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                String responseBody = response.getBody();
                JsonNode root = objectMapper.readTree(responseBody);
                
                JsonNode error = root.get("error");
                if (error != null) {
                    String errorMessage = error.get("message").asText();
                    return "Error from DeepSeek API: " + errorMessage;
                }
                
                JsonNode choices = root.get("choices");
                
                if (choices == null || choices.isEmpty()) {
                    return "No response from DeepSeek API";
                }
                
                JsonNode message = choices.get(0).get("message");
                if (message == null) {
                    return "No response from DeepSeek API";
                }
                
                JsonNode content = message.get("content");
                if (content == null) {
                    return "No response from DeepSeek API";
                }
                
                String contentText = content.asText().trim();
                if (contentText.isEmpty() || contentText.matches("[?？]+")) {
                    return "Sorry, I can't understand the response";
                }
                
                return contentText;
            }
        } catch (Exception e) {
            return "Error calling DeepSeek API: " + e.getMessage();
        }
        
        return "No response from DeepSeek API";
    }

    @Override
    public void streamChat(String model, List<Map<String, String>> messages,
                           java.util.function.Consumer<String> onMessage,
                           java.lang.Runnable onComplete,
                           java.util.function.Consumer<Throwable> onError) {
        new Thread(() -> {
            String url = apiUrl + "/chat/completions";

            try {
                java.net.URL apiUrlObj = new java.net.URL(url);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) apiUrlObj.openConnection();
                conn.setConnectTimeout(60000);
                conn.setReadTimeout(60000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                conn.setRequestProperty("Accept", "text/event-stream");
                conn.setDoOutput(true);

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", model);
                requestBody.put("temperature", 0.8);
                requestBody.put("max_tokens", 1000);
                requestBody.put("stream", true);
                requestBody.put("messages", messages);

                String jsonBody = objectMapper.writeValueAsString(requestBody);
                try (java.io.OutputStream os = conn.getOutputStream()) {
                    os.write(jsonBody.getBytes());
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                    try (java.io.BufferedReader reader = new java.io.BufferedReader(
                            new java.io.InputStreamReader(conn.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6);
                                if (!"[DONE]".equals(data)) {
                                    try {
                                        JsonNode root = objectMapper.readTree(data);
                                        
                                        JsonNode error = root.get("error");
                                        if (error != null) {
                                            String errorMessage = error.get("message").asText();
                                            onError.accept(new RuntimeException("Error from DeepSeek API: " + errorMessage));
                                            return;
                                        }
                                        
                                        JsonNode choices = root.get("choices");
                                        if (choices != null && !choices.isEmpty()) {
                                            JsonNode delta = choices.get(0).get("delta");
                                            if (delta != null && delta.has("content")) {
                                                String content = delta.get("content").asText();
                                                if (!content.isEmpty() && !content.matches("[?？]+")) {
                                                    onMessage.accept(content);
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        // ignore parse errors
                                    }
                                }
                            }
                        }
                        onComplete.run();
                    }
                } else {
                    onError.accept(new RuntimeException("API request failed: " + responseCode));
                }
            } catch (Exception e) {
                onError.accept(e);
            }
        }).start();
    }
}