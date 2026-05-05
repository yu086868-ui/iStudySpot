package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.Character;
import com.ycyu.istudyspotbackend.entity.Message;
import com.ycyu.istudyspotbackend.entity.Session;
import com.ycyu.istudyspotbackend.service.AIService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class AIServiceImpl implements AIService {

    private final List<Character> characters;
    private final Map<String, Session> sessions;
    private final ExecutorService executorService;

    public AIServiceImpl() {
        // 初始化角色列表
        this.characters = new ArrayList<>();
        characters.add(new Character("scientist", "科学家", "理性严谨，喜欢解释原理", "逻辑清晰，偏长句"));
        characters.add(new Character("teacher", "老师", "耐心细致，善于引导", "温和亲切，鼓励式"));
        characters.add(new Character("artist", "艺术家", "富有创意，情感丰富", "感性表达，富有想象力"));

        // 初始化会话存储
        this.sessions = new HashMap<>();

        // 初始化线程池
        this.executorService = Executors.newFixedThreadPool(10);
    }

    @Override
    public List<Character> getCharacters() {
        return characters;
    }

    @Override
    public Character getCharacter(String characterId) {
        return characters.stream()
                .filter(character -> character.getId().equals(characterId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Session getOrCreateSession(String sessionId, String characterId) {
        return sessions.computeIfAbsent(sessionId, id -> new Session(id, characterId));
    }

    @Override
    public String chat(String sessionId, String characterId, String message) {
        // 获取或创建会话
        Session session = getOrCreateSession(sessionId, characterId);

        // 检查角色是否存在
        Character character = getCharacter(characterId);
        if (character == null) {
            throw new IllegalArgumentException("Invalid character ID");
        }

        // 添加用户消息
        session.addMessage(new Message("user", message));

        // 构建系统提示词
        String systemPrompt = buildSystemPrompt(character);

        // 获取最近的消息
        List<Message> recentMessages = session.getRecentMessages(10);

        // 模拟 LLM 响应
        String response = generateResponse(character, message);

        // 添加助手回复
        session.addMessage(new Message("assistant", response));

        return response;
    }

    @Override
    public SseEmitter streamChat(String sessionId, String characterId, String message) {
        SseEmitter emitter = new SseEmitter();

        executorService.submit(() -> {
            try {
                // 获取或创建会话
                Session session = getOrCreateSession(sessionId, characterId);

                // 检查角色是否存在
                Character character = getCharacter(characterId);
                if (character == null) {
                    emitter.send(SseEmitter.event().data("{\"type\": \"error\", \"message\": \"INVALID_CHARACTER\"}"));
                    emitter.complete();
                    return;
                }

                // 添加用户消息
                session.addMessage(new Message("user", message));

                // 发送开始事件
                emitter.send(SseEmitter.event().data("{\"type\": \"start\"}"));

                // 模拟 LLM 流式响应
                String response = generateResponse(character, message);
                for (char c : response.toCharArray()) {
                    Thread.sleep(50); // 模拟延迟
                    emitter.send(SseEmitter.event().data("{\"type\": \"delta\", \"content\": \"" + c + "\"}"));
                }

                // 添加助手回复
                session.addMessage(new Message("assistant", response));

                // 发送结束事件
                emitter.send(SseEmitter.event().data("{\"type\": \"end\"}"));
                emitter.complete();
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event().data("{\"type\": \"error\", \"message\": \"INTERNAL_ERROR\"}"));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    private String buildSystemPrompt(Character character) {
        return "你正在扮演一个角色，请严格遵守以下设定：\n" +
                "\n" +
                "角色名称：" + character.getName() + "\n" +
                "性格：" + character.getPersona() + "\n" +
                "说话风格：" + character.getSpeaking_style() + "\n" +
                "\n" +
                "要求：\n" +
                "- 始终保持角色语气\n" +
                "- 不要提到自己是AI\n" +
                "- 不要跳出角色\n" +
                "\n" +
                "请根据对话继续交流。";
    }

    private String generateResponse(Character character, String message) {
        // 模拟不同角色的响应
        switch (character.getId()) {
            case "scientist":
                return "从科学的角度来看，这个问题涉及到多个领域的知识。首先，我们需要了解基本原理，然后分析各种因素的影响，最后得出结论。这种系统性的思考方法是解决复杂问题的关键。";
            case "teacher":
                return "这个问题提得很好！让我们一起分析一下。首先，我们需要理解问题的本质，然后思考可能的解决方案。你觉得应该从哪些方面入手呢？";
            case "artist":
                return "这个问题让我想到了一幅美丽的画面。想象一下，当我们面对这样的情况时，就像在创作一幅画，每一个选择都是一种色彩，最终构成一幅完整的作品。艺术就是这样，充满了无限的可能。";
            default:
                return "我理解你的问题。让我思考一下如何回答你。";
        }
    }
}
