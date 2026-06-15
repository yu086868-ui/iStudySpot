package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.Card;
import com.ycyu.istudyspotbackend.mapper.CardMapper;
import com.ycyu.istudyspotbackend.service.CardService;
import com.ycyu.istudyspotbackend.service.DeepSeekService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

@Service
public class CardServiceImpl implements CardService {

    @Autowired
    private CardMapper cardMapper;

    @Autowired
    private DeepSeekService deepSeekService;

    @Value("${card.image.storage-path:./uploads/cards}")
    private String storagePath;

    @Value("${card.image.base-url:/api/wx/card/image}")
    private String baseUrl;

    private static final String[] RARITIES = {"N", "R", "SR", "SSR", "UR", "LR"};
    private static final String[] RARITY_NAMES = {"白", "绿", "蓝", "紫", "金", "红"};

    private static final Map<String, List<String>> THEME_POOL = new LinkedHashMap<>();
    static {
        THEME_POOL.put("励志成长", Arrays.asList("自律", "努力", "学习意义", "坚持"));
        THEME_POOL.put("名人与历史", Arrays.asList("历史人物", "科学家", "文学引用"));
        THEME_POOL.put("哲思感悟", Arrays.asList("微型思考", "具有余味的表达"));
        THEME_POOL.put("自然意象", Arrays.asList("星海", "雨夜", "山川", "四季"));
        THEME_POOL.put("科技未来", Arrays.asList("AI", "太空", "科幻", "未来感"));
        THEME_POOL.put("温柔陪伴", Arrays.asList("理解", "缓慢成长", "轻陪伴"));
        THEME_POOL.put("隐藏主题", Arrays.asList("神话", "梦境", "史诗感", "超现实", "宇宙诗意"));
    }

    private static final Map<String, String> RARITY_BORDER_THEME = new LinkedHashMap<>();
    static {
        RARITY_BORDER_THEME.put("N", "白/灰");
        RARITY_BORDER_THEME.put("R", "绿");
        RARITY_BORDER_THEME.put("SR", "蓝");
        RARITY_BORDER_THEME.put("SSR", "紫");
        RARITY_BORDER_THEME.put("UR", "金");
        RARITY_BORDER_THEME.put("LR", "红");
    }

    private static final Map<String, String> RARITY_CARD_THEME = new LinkedHashMap<>();
    static {
        RARITY_CARD_THEME.put("N", "普通");
        RARITY_CARD_THEME.put("R", "普通");
        RARITY_CARD_THEME.put("SR", "普通");
        RARITY_CARD_THEME.put("SSR", "普通");
        RARITY_CARD_THEME.put("UR", "特殊");
        RARITY_CARD_THEME.put("LR", "特殊");
    }

    private static final Random RANDOM = new Random();

    @Override
    public Card generateCard(String userId, Integer studyDuration) {
        String rarity = calculateRarity(studyDuration);
        String themeCategory = selectThemeCategory(rarity);
        
        String markdown = deepSeekService.generateCardContent(rarity, themeCategory);
        String imageUrl = downloadAndSaveImage(rarity, themeCategory);

        Card card = new Card();
        card.setUuid(UUID.randomUUID().toString());
        card.setUserId(userId);
        card.setCardId(UUID.randomUUID().toString().substring(0, 8));
        card.setCreateTime(LocalDateTime.now());
        card.setStudyDuration(studyDuration);
        card.setRarity(rarity);
        card.setBorderTheme(RARITY_BORDER_THEME.get(rarity));
        card.setCardTheme(RARITY_CARD_THEME.get(rarity));
        card.setThemeCategory(themeCategory);
        card.setMarkdown(markdown);
        card.setImageUrl(imageUrl);

        cardMapper.insertCard(card);
        return card;
    }

    @Override
    public Card getCardByUuid(String uuid) {
        return cardMapper.selectCardByUuid(uuid);
    }

    @Override
    public List<Card> getCardsByUserId(String userId) {
        List<Card> cards = cardMapper.selectCardsByUserId(userId);
        cards.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
        return cards;
    }

    @Override
    public Map<String, Object> generateCardData(String rarity) {
        String themeCategory = selectThemeCategory(rarity);
        Map<String, Object> result = new HashMap<>();
        result.put("markdown", deepSeekService.generateCardContent(rarity, themeCategory));
        result.put("imageUrl", downloadAndSaveImage(rarity, themeCategory));
        return result;
    }

    @Override
    public void generateCardStream(String userId, Integer studyDuration,
                                   Consumer<Map<String, Object>> onData,
                                   Consumer<Map<String, Object>> onComplete,
                                   Consumer<Throwable> onError) {
        try {
            String rarity = calculateRarity(studyDuration);
            String themeCategory = selectThemeCategory(rarity);
            StringBuilder markdownBuilder = new StringBuilder();
            String[] imageUrlHolder = new String[1];
            java.util.concurrent.atomic.AtomicBoolean textCompleted = new java.util.concurrent.atomic.AtomicBoolean(false);
            java.util.concurrent.atomic.AtomicBoolean imageCompleted = new java.util.concurrent.atomic.AtomicBoolean(false);
            java.util.concurrent.atomic.AtomicReference<Card> cardRef = new java.util.concurrent.atomic.AtomicReference<>(null);

            Map<String, Object> initData = new HashMap<>();
            initData.put("type", "init");
            initData.put("rarity", rarity);
            initData.put("themeCategory", themeCategory);
            initData.put("borderTheme", RARITY_BORDER_THEME.get(rarity));
            initData.put("cardTheme", RARITY_CARD_THEME.get(rarity));
            onData.accept(initData);

            final String finalRarity = rarity;
            new Thread(() -> {
                try {
                    String imgUrl = downloadAndSaveImage(finalRarity, themeCategory);
                    imageUrlHolder[0] = imgUrl;
                    imageCompleted.set(true);
                    checkComplete(onData, onComplete, cardRef, markdownBuilder, imageUrlHolder, textCompleted, imageCompleted);
                } catch (Exception e) {
                    imageUrlHolder[0] = "https://via.placeholder.com/512x512?text=Image+Unavailable";
                    imageCompleted.set(true);
                    checkComplete(onData, onComplete, cardRef, markdownBuilder, imageUrlHolder, textCompleted, imageCompleted);
                }
            }).start();

            List<Map<String, String>> messages = buildPromptMessages(rarity, themeCategory);
            
            deepSeekService.streamChat("deepseek-chat", messages,
                chunk -> {
                    markdownBuilder.append(chunk);
                    Map<String, Object> textData = new HashMap<>();
                    textData.put("type", "text");
                    textData.put("content", chunk);
                    onData.accept(textData);
                },
                () -> {
                    textCompleted.set(true);
                    
                    String uuid = UUID.randomUUID().toString();
                    Card card = new Card();
                    card.setUuid(uuid);
                    card.setUserId(userId);
                    card.setCardId(UUID.randomUUID().toString().substring(0, 8));
                    card.setCreateTime(LocalDateTime.now());
                    card.setStudyDuration(studyDuration);
                    card.setRarity(rarity);
                    card.setBorderTheme(RARITY_BORDER_THEME.get(rarity));
                    card.setCardTheme(RARITY_CARD_THEME.get(rarity));
                    card.setThemeCategory(themeCategory);
                    cardRef.set(card);
                    
                    checkComplete(onData, onComplete, cardRef, markdownBuilder, imageUrlHolder, textCompleted, imageCompleted);
                },
                onError
            );
        } catch (Exception e) {
            onError.accept(e);
        }
    }

    private void checkComplete(Consumer<Map<String, Object>> onData, Consumer<Map<String, Object>> onComplete,
                              java.util.concurrent.atomic.AtomicReference<Card> cardRef, 
                              StringBuilder markdownBuilder, String[] imageUrlHolder,
                              java.util.concurrent.atomic.AtomicBoolean textCompleted,
                              java.util.concurrent.atomic.AtomicBoolean imageCompleted) {
        if (textCompleted.get() && imageCompleted.get() && cardRef.get() != null && imageUrlHolder[0] != null) {
            Card card = cardRef.get();
            String markdown = markdownBuilder.toString();
            String imageUrl = imageUrlHolder[0];
            
            card.setMarkdown(markdown);
            card.setImageUrl(imageUrl);
            cardMapper.insertCard(card);
            
            Map<String, Object> cardData = new HashMap<>();
            cardData.put("uuid", card.getUuid());
            cardData.put("rarity", card.getRarity());
            cardData.put("borderTheme", card.getBorderTheme());
            cardData.put("cardTheme", card.getCardTheme());
            cardData.put("themeCategory", card.getThemeCategory());
            cardData.put("markdown", card.getMarkdown());
            cardData.put("imageURL", card.getImageUrl());
            cardData.put("createTime", card.getCreateTime() != null ? 
                card.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
            cardData.put("studyDuration", card.getStudyDuration());
            
            Map<String, Object> completeData = new HashMap<>();
            completeData.put("success", true);
            completeData.put("message", "generate success");
            completeData.put("card", cardData);
            onComplete.accept(completeData);
        }
    }

    private static final Map<String, String> RARITY_STYLE_MAP = new HashMap<>();
    static {
        RARITY_STYLE_MAP.put("N", "简洁、基础、鼓励性");
        RARITY_STYLE_MAP.put("R", "积极、成长、向上");
        RARITY_STYLE_MAP.put("SR", "深度、思考、智慧");
        RARITY_STYLE_MAP.put("SSR", "哲学、审美、意境");
        RARITY_STYLE_MAP.put("UR", "史诗、未来、宏大");
        RARITY_STYLE_MAP.put("LR", "神话、梦境、超现实");
    }

    private static final Map<String, String> THEME_DESCRIPTION_MAP = new HashMap<>();
    static {
        THEME_DESCRIPTION_MAP.put("励志成长", "关于自律、努力、学习意义、坚持的内容");
        THEME_DESCRIPTION_MAP.put("名人与历史", "引用历史人物、科学家、文学作品的智慧");
        THEME_DESCRIPTION_MAP.put("哲思感悟", "微型思考、具有余味的表达、生活哲理");
        THEME_DESCRIPTION_MAP.put("自然意象", "用星海、雨夜、山川、四季等自然景象做隐喻");
        THEME_DESCRIPTION_MAP.put("科技未来", "关于AI、太空、科幻、未来感的内容");
        THEME_DESCRIPTION_MAP.put("温柔陪伴", "理解、缓慢成长、轻陪伴的温暖内容");
        THEME_DESCRIPTION_MAP.put("隐藏主题", "神话、梦境、史诗感、超现实、宇宙诗意");
    }

    private static final List<String> TONE_VARIATIONS = Arrays.asList(
            "温暖而有力量", "冷静而深刻", "诗意而优雅", "理性而睿智",
            "温柔而坚定", "深邃而富有哲理", "幽默而睿智", "感性而细腻",
            "冷峻而犀利", "浪漫而唯美", "朴实而真诚", "灵动而俏皮"
    );

    private static final List<String> METAPHOR_TOPICS = Arrays.asList(
            "自然现象", "四季更迭", "植物生长", "天文宇宙", "音乐艺术",
            "旅途航行", "建筑构造", "烹饪烘焙", "书法绘画", "品茶品酒",
            "园艺种植", "星座神话", "海洋探索", "森林秘境", "古镇街巷", "云雾山川"
    );

    private static final List<String> NARRATIVE_PERSPECTIVES = Arrays.asList(
            "第一人称内心独白", "第三人称观察者视角", "自然景物拟人化",
            "时间旅行者的回忆", "来自未来的寄语", "历史人物的对话",
            "梦境中的启示", "书中精灵的低语"
    );

    private static final List<String> CREATIVE_APPROACHES = Arrays.asList(
            "使用对比手法", "采用逆向思维", "运用象征手法", "创造独特比喻",
            "讲述微型故事", "提出开放性问题", "引用小众知识", "融合跨界概念"
    );

    private List<Map<String, String>> buildPromptMessages(String rarity, String themeCategory) {
        String rarityName = "";
        for (int i = 0; i < RARITIES.length; i++) {
            if (RARITIES[i].equals(rarity)) {
                rarityName = RARITY_NAMES[i];
                break;
            }
        }

        String style = RARITY_STYLE_MAP.getOrDefault(rarity, "简洁、鼓励性");
        String themeDesc = THEME_DESCRIPTION_MAP.getOrDefault(themeCategory, "学习成长");
        
        Random random = new Random();
        String tone = TONE_VARIATIONS.get(random.nextInt(TONE_VARIATIONS.size()));
        String metaphor = METAPHOR_TOPICS.get(random.nextInt(METAPHOR_TOPICS.size()));
        String perspective = NARRATIVE_PERSPECTIVES.get(random.nextInt(NARRATIVE_PERSPECTIVES.size()));
        String approach = CREATIVE_APPROACHES.get(random.nextInt(CREATIVE_APPROACHES.size()));

        String systemPrompt = """
                你是一位资深的学习激励卡片文案创作专家。请按照以下严格的结构和要求生成精简的卡片内容：
                
                ## 核心要求：
                1. **简洁精炼**：内容必须精简，适合卡片展示
                2. **主题明确**：所有内容围绕核心主题展开
                3. **深度思考**：避免鸡汤，要有独特见解
                4. **语言优美**：使用恰当的修辞手法
                
                ## 格式规范：
                - 标题：# 开头，4-6字，概括核心思想
                - 引用：> 开头，15-20字，与主题紧密相关
                - 分隔线：--- 
                - 正文：50-80字，一段或两段，言简意赅
                - 结尾：有余味，引人深思
                
                ## 结构示例：
                # 标题
                > 引用
                ---
                正文内容...
                
                请直接输出完整的卡片内容，无需额外说明。
                """;

        String userPrompt = String.format("""
                请为学习卡片创作一段精简的文案：
                
                - 核心主题：%s
                - 风格调性：%s
                - 语气基调：%s
                - 表现手法：%s
                - 意象素材：%s
                
                要求：简洁精炼，富有哲理，适合卡片展示。
                """, themeDesc, style, tone, approach, metaphor);

        List<Map<String, String>> messages = new ArrayList<>();
        
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);
        
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userPrompt);
        messages.add(userMsg);
        
        return messages;
    }

    private String downloadAndSaveImage(String rarity, String themeCategory) {
        String prompt = getPromptForRarityAndTheme(rarity, themeCategory);
        String localPath = generateLocalFilePath();
        
        try {
            Path dirPath = Paths.get(storagePath);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            
            URL url = new URL("http://localhost:3002/generate");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(60000);
            conn.setReadTimeout(60000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setDoOutput(true);
            
            String requestBody = "{\"prompt\": \"" + prompt + "\"}";
            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes());
            }
            
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream in = conn.getInputStream()) {
                    byte[] responseBody = in.readAllBytes();
                    String responseStr = new String(responseBody);
                    
                    if (responseStr.contains("\"success\":true")) {
                        int urlIndex = responseStr.indexOf("\"url\":\"");
                        if (urlIndex > 0) {
                            int start = urlIndex + 7;
                            int end = responseStr.indexOf("\"", start);
                            String imageUrl = "http://localhost:3002" + responseStr.substring(start, end);
                            
                            URL imageUrlObj = new URL(imageUrl);
                            HttpURLConnection imageConn = (HttpURLConnection) imageUrlObj.openConnection();
                            imageConn.setConnectTimeout(30000);
                            imageConn.setReadTimeout(30000);
                            
                            try (InputStream imageIn = imageConn.getInputStream();
                                 OutputStream out = new FileOutputStream(localPath)) {
                                byte[] buffer = new byte[4096];
                                int bytesRead;
                                while ((bytesRead = imageIn.read(buffer)) != -1) {
                                    out.write(buffer, 0, bytesRead);
                                }
                            }
                            
                            return baseUrl + "/" + getImageFileName(localPath);
                        }
                    }
                }
            }
            
            return "https://via.placeholder.com/512x512?text=Image+Unavailable";
            
        } catch (Exception e) {
            return "https://via.placeholder.com/512x512?text=Image+Unavailable";
        }
    }

    private String getPromptForRarityAndTheme(String rarity, String themeCategory) {
        Map<String, List<String>> themePrompts = new HashMap<>();
        themePrompts.put("励志成长", Arrays.asList(
            "person climbing mountain sunrise, minimalist style",
            "seedling growing through stone, hope concept",
            "butterfly emerging from cocoon, transformation",
            "road leading to horizon, journey concept",
            "sunrise over mountain, new beginning"
        ));
        themePrompts.put("名人与历史", Arrays.asList(
            "ancient library with floating books, magical",
            "philosopher in contemplation, classical painting style",
            "scientist in laboratory, vintage illustration",
            "historical figure silhouette, dramatic lighting",
            "quill pen writing on parchment, antique"
        ));
        themePrompts.put("哲思感悟", Arrays.asList(
            "zen garden with raked sand, peaceful",
            "moon reflection on water, serene night",
            "silhouette meditating on mountain top",
            "candlelight in dark room, introspection",
            "autumn leaves falling, contemplation"
        ));
        themePrompts.put("自然意象", Arrays.asList(
            "starry night sky with milky way, cosmic",
            "rainy window with city lights, cozy",
            "misty mountains with pine trees, serene",
            "cherry blossoms in spring, soft pink",
            "ocean waves at sunset, golden hour"
        ));
        themePrompts.put("科技未来", Arrays.asList(
            "futuristic city with holographic displays, neon",
            "space exploration with stars and nebula",
            "AI robot with gentle expression, friendly",
            "virtual reality interface, digital art",
            "spaceship in space with planet background"
        ));
        themePrompts.put("温柔陪伴", Arrays.asList(
            "warm coffee cup with steam, cozy atmosphere",
            "soft sunlight through window, peaceful morning",
            "cat sleeping on books, warm and cozy",
            "rainy day reading by fireplace, comfort",
            "hands holding a book, gentle touch"
        ));
        themePrompts.put("隐藏主题", Arrays.asList(
            "epic fantasy castle with aurora lights",
            "mystical dragon in misty mountains",
            "ancient temple glowing with magic",
            "phoenix rising from flames, rebirth",
            "celestial beings in cosmic realm, ethereal"
        ));

        List<String> prompts = themePrompts.getOrDefault(themeCategory, 
            themePrompts.get("励志成长"));
        
        String styleSuffix = getRarityStyleSuffix(rarity);
        return prompts.get(RANDOM.nextInt(prompts.size())) + ", " + styleSuffix;
    }

    private String getRarityStyleSuffix(String rarity) {
        switch (rarity) {
            case "N": return "simple, clean, minimalist";
            case "R": return "vibrant, hopeful, positive";
            case "SR": return "thoughtful, intellectual, elegant";
            case "SSR": return "philosophical, serene, beautiful";
            case "UR": return "epic, grand, cinematic";
            case "LR": return "mystical, ethereal, legendary";
            default: return "beautiful, aesthetic";
        }
    }

    private String generateLocalFilePath() {
        String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = UUID.randomUUID().toString() + ".png";
        Path dirPath = Paths.get(storagePath, dateDir);
        
        try {
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
        } catch (IOException e) {
            // ignore
        }
        
        return Paths.get(storagePath, dateDir, fileName).toString();
    }

    private String getImageFileName(String localPath) {
        String localPathNormalized = localPath.replace("\\", "/");
        String storagePathNormalized = storagePath.replace("\\", "/");
        
        if (storagePathNormalized.startsWith("./")) {
            storagePathNormalized = storagePathNormalized.substring(2);
        }
        
        String prefix = storagePathNormalized + "/";
        if (localPathNormalized.contains(prefix)) {
            return localPathNormalized.substring(localPathNormalized.indexOf(prefix) + prefix.length());
        }
        
        Path path = Paths.get(localPath);
        Path relativePath = Paths.get(storagePath).toAbsolutePath().normalize()
                .relativize(Paths.get(localPath).toAbsolutePath().normalize());
        return relativePath.toString().replace(File.separator, "/");
    }

    private String calculateRarity(Integer studyDuration) {
        if (studyDuration <= 10) {
            return "N";
        } else if (studyDuration <= 30) {
            double rand = Math.random();
            if (rand < 0.45) return "N";
            else if (rand < 0.90) return "R";
            else return "SR";
        } else if (studyDuration <= 60) {
            double rand = Math.random();
            if (rand < 0.20) return "N";
            else if (rand < 0.65) return "R";
            else if (rand < 0.90) return "SR";
            else return "SSR";
        } else if (studyDuration <= 120) {
            double rand = Math.random();
            if (rand < 0.10) return "N";
            else if (rand < 0.45) return "R";
            else if (rand < 0.75) return "SR";
            else if (rand < 0.95) return "SSR";
            else return "UR";
        } else if (studyDuration <= 240) {
            double rand = Math.random();
            if (rand < 0.05) return "N";
            else if (rand < 0.30) return "R";
            else if (rand < 0.60) return "SR";
            else if (rand < 0.85) return "SSR";
            else if (rand < 0.99) return "UR";
            else return "LR";
        } else {
            double rand = Math.random();
            if (rand < 0.03) return "N";
            else if (rand < 0.23) return "R";
            else if (rand < 0.50) return "SR";
            else if (rand < 0.80) return "SSR";
            else if (rand < 0.98) return "UR";
            else return "LR";
        }
    }

    private String selectThemeCategory(String rarity) {
        List<String> themes;
        
        if ("LR".equals(rarity)) {
            themes = new ArrayList<>(THEME_POOL.keySet());
        } else if ("UR".equals(rarity)) {
            themes = new ArrayList<>(THEME_POOL.keySet());
        } else {
            themes = new ArrayList<>(THEME_POOL.keySet());
            themes.remove("隐藏主题");
        }

        double rand = Math.random();
        double cumulative = 0;
        
        for (String theme : themes) {
            double weight = getThemeWeight(theme);
            if (rand < cumulative + weight) {
                return theme;
            }
            cumulative += weight;
        }
        
        return "励志成长";
    }

    private double getThemeWeight(String theme) {
        switch (theme) {
            case "励志成长": return 0.25;
            case "名人与历史": return 0.15;
            case "哲思感悟": return 0.20;
            case "自然意象": return 0.15;
            case "科技未来": return 0.10;
            case "温柔陪伴": return 0.10;
            case "隐藏主题": return 0.05;
            default: return 0.10;
        }
    }
}