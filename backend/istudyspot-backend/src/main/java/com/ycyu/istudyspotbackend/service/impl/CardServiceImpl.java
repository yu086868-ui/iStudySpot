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

@Service
public class CardServiceImpl implements CardService {

    @Autowired
    private CardMapper cardMapper;

    @Autowired
    private DeepSeekService deepSeekService;

    @Value("${card.image.storage-path:./uploads/cards}")
    private String storagePath;

    @Value("${card.image.base-url:/api/card/image}")
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