package com.ycyu.istudyspotbackend.agent.chat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycyu.istudyspotbackend.agent.tool.AgentToolExecuteRequest;
import com.ycyu.istudyspotbackend.agent.tool.AgentToolDefinition;
import com.ycyu.istudyspotbackend.agent.tool.AgentToolExecutionResult;
import com.ycyu.istudyspotbackend.agent.tool.AgentToolService;
import com.ycyu.istudyspotbackend.ai.AiRules;
import com.ycyu.istudyspotbackend.ai.AiRulesRegistry;
import com.ycyu.istudyspotbackend.entity.Message;
import com.ycyu.istudyspotbackend.service.DeepSeekService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AgentChatServiceImpl implements AgentChatService {

    private static final int MAX_SESSION_MESSAGES = 20;
    private static final List<String> DEFAULT_SUGGESTIONS = List.of(
            "查看可用自习室",
            "查看预约规则",
            "查看我的预约",
            "查看 1 号自习室的座位"
    );

    private final AgentToolService agentToolService;
    private final DeepSeekService deepSeekService;
    private final ObjectMapper objectMapper;
    private final Map<String, AgentSessionContext> sessions = new ConcurrentHashMap<>();

    @Value("${deepseek.api.model:deepseek-chat}")
    private String model;

    @Autowired
    public AgentChatServiceImpl(AgentToolService agentToolService, DeepSeekService deepSeekService) {
        this.agentToolService = agentToolService;
        this.deepSeekService = deepSeekService;
        this.objectMapper = new ObjectMapper();
    }

    AgentChatServiceImpl(AgentToolService agentToolService) {
        this.agentToolService = agentToolService;
        this.deepSeekService = null;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public AgentChatResponse chat(Long userId, AgentChatRequest request) {
        if (userId == null) {
            throw new IllegalArgumentException("AUTH_REQUIRED");
        }
        if (request == null || isBlank(request.getMessage())) {
            throw new IllegalArgumentException("EMPTY_MESSAGE");
        }

        String sessionId = isBlank(request.getSessionId())
                ? UUID.randomUUID().toString()
                : request.getSessionId().trim();
        AgentSessionContext context = sessions.computeIfAbsent(
                buildSessionKey(userId, sessionId),
                key -> new AgentSessionContext(userId, sessionId)
        );

        String prompt = request.getMessage().trim();
        context.addMessage("user", prompt);

        AgentChatResponse policyResponse = tryPolicyGuard(sessionId, prompt);
        if (policyResponse != null) {
            context.addMessage("assistant", policyResponse.getReply());
            return policyResponse;
        }
        if (deepSeekService == null && isWriteActionIntent(prompt)) {
            AgentChatResponse response = actionNotAvailableResponse(sessionId);
            context.addMessage("assistant", response.getReply());
            return response;
        }

        AgentChatResponse navigationResponse = tryNavigationResponse(sessionId, prompt);
        if (navigationResponse != null) {
            context.addMessage("assistant", navigationResponse.getReply());
            return navigationResponse;
        }

        AgentChatResponse llmResponse = tryLlmOrchestration(userId, sessionId, prompt, context);
        if (llmResponse != null) {
            context.addMessage("assistant", llmResponse.getReply());
            return llmResponse;
        }

        ToolDecision decision = decide(prompt, context);
        if (!decision.hasTool()) {
            context.addMessage("assistant", decision.reply());
            return new AgentChatResponse(
                    sessionId,
                    decision.reply(),
                    null,
                    List.of(),
                    decision.suggestedPrompts()
            );
        }

        AgentToolExecutionResult toolResult = agentToolService.execute(
                userId,
                buildToolRequest(decision)
        );
        updateContext(context, toolResult);

        String reply = buildReply(toolResult);
        context.addMessage("assistant", reply);
        return new AgentChatResponse(
                sessionId,
                reply,
                toolResult,
                List.of(toolResult),
                buildSuggestions(toolResult)
        );
    }

    @Override
    public SseEmitter streamChat(Long userId, AgentChatRequest request) {
        SseEmitter emitter = new SseEmitter(30_000L);
        try {
            AgentChatResponse response = chat(userId, request);
            emitter.send(SseEmitter.event().name("agent_message").data(response));
            emitter.complete();
        } catch (IOException e) {
            sendStreamError(emitter, "STREAM_SEND_FAILED", true);
        } catch (IllegalArgumentException e) {
            sendStreamError(emitter, e.getMessage(), false);
        } catch (RuntimeException e) {
            sendStreamError(emitter, "INTERNAL_ERROR", true);
        }
        return emitter;
    }

    private AgentChatResponse tryPolicyGuard(String sessionId, String prompt) {
        if (deepSeekService == null) {
            return null;
        }

        try {
            JsonNode response = deepSeekService.chatCompletion(
                    model,
                    buildPolicyMessages(prompt),
                    List.of()
            );
            PolicyDecision decision = parsePolicyDecision(extractContent(firstChoiceMessage(response)));
            if (decision == null || decision.allowedReadOnly()) {
                return null;
            }
            return new AgentChatResponse(
                    sessionId,
                    defaultText(decision.reply(), "我只能帮你查询 iStudySpot 信息。预约、取消、支付、签到等操作请到对应页面完成。"),
                    null,
                    List.of(),
                    buildDefaultSuggestions()
            );
        } catch (RuntimeException e) {
            if (isWriteActionIntent(prompt)) {
                return actionNotAvailableResponse(sessionId);
            }
            return null;
        }
    }

    private List<Map<String, Object>> buildPolicyMessages(String prompt) {
        return List.of(
                mapOf(
                        "role", "system",
                        "content", buildPolicySystemPrompt()
                ),
                mapOf(
                        "role", "user",
                        "content", prompt
                )
        );
    }

    private String buildPolicySystemPrompt() {
        AiRules rules = AiRulesRegistry.getRules();
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是 iStudySpot 智能助手的策略守卫。判断用户请求是否允许由只读助手处理。\n");
        prompt.append("助手只能读取自习室、座位、用户脱敏预约记录和预约规则。\n");
        prompt.append("助手不能创建、取消、续约、修改、签到、签退、支付、删除或写入任何业务数据。\n");
        prompt.append("如果用户询问如何操作或询问规则/政策，属于只读说明，可以允许。\n");
        prompt.append("如果用户要求助手代为执行操作，请拒绝，并说明需要到 App 对应页面完成。\n\n");
        appendRuleSection(prompt, "助手安全规则", rules.getAgent().getSafetyRules());
        appendRuleSection(prompt, "助手隐私规则", rules.getAgent().getPrivacyRules());
        appendRuleSection(prompt, "助手工具规则", rules.getAgent().getToolRules());
        appendRuleSection(prompt, "客服事实", rules.getCustomerService().getServiceFacts());
        prompt.append("\n只返回紧凑 JSON，格式如下：\n");
        prompt.append("{\"allowedReadOnly\":true|false,\"reply\":\"拒绝时给用户看的简短中文说明，允许时为空\"}");
        return prompt.toString();
    }

    private void appendRuleSection(StringBuilder builder, String title, List<String> rules) {
        if (rules == null || rules.isEmpty()) {
            return;
        }
        builder.append(title).append(":\n");
        for (String rule : rules) {
            builder.append("- ").append(rule).append("\n");
        }
    }

    private PolicyDecision parsePolicyDecision(String content) {
        if (isBlank(content)) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(content);
            JsonNode allowed = root.has("allowedReadOnly") ? root.get("allowedReadOnly") : root.get("allowed");
            if (allowed == null || !allowed.isBoolean()) {
                return null;
            }
            String reply = root.has("reply") && !root.get("reply").isNull()
                    ? root.get("reply").asText()
                    : "";
            return new PolicyDecision(allowed.asBoolean(), reply);
        } catch (Exception e) {
            return null;
        }
    }

    private AgentChatResponse tryLlmOrchestration(Long userId, String sessionId, String prompt, AgentSessionContext context) {
        if (deepSeekService == null) {
            return null;
        }

        try {
            List<AgentToolDefinition> catalog = agentToolService.getCatalog();
            List<Map<String, Object>> toolSchemas = buildToolSchemas(catalog);
            Set<String> allowedTools = new HashSet<>();
            for (AgentToolDefinition definition : catalog) {
                allowedTools.add(definition.getName());
            }

            JsonNode firstResponse = deepSeekService.chatCompletion(
                    model,
                    buildPlannerMessages(prompt, context),
                    toolSchemas
            );

            JsonNode message = firstChoiceMessage(firstResponse);
            List<ToolCall> toolCalls = parseToolCalls(message);
            if (toolCalls.isEmpty()) {
                String reply = extractContent(message);
                if (isBlank(reply)) {
                    return null;
                }
                return new AgentChatResponse(
                        sessionId,
                        reply,
                        null,
                        List.of(),
                        buildDefaultSuggestions()
                );
            }

            List<AgentToolExecutionResult> results = new ArrayList<>();
            for (ToolCall toolCall : toolCalls.stream().limit(3).toList()) {
                if (!allowedTools.contains(toolCall.name())) {
                    return actionNotAvailableResponse(sessionId);
                }

                Map<String, Object> arguments = enrichToolArguments(toolCall.name(), toolCall.arguments(), context);
                if (requiresStudyRoomId(toolCall.name()) && toLong(arguments.get("studyRoomId")) == null) {
                    return new AgentChatResponse(
                            sessionId,
                            "请先提供自习室编号，再查询该信息。",
                            null,
                            List.of(),
                            buildDefaultSuggestions()
                    );
                }

                AgentToolExecuteRequest toolRequest = new AgentToolExecuteRequest();
                toolRequest.setTool(toolCall.name());
                toolRequest.setArguments(arguments);

                AgentToolExecutionResult result = agentToolService.execute(userId, toolRequest);
                results.add(result);
                updateContext(context, result);
            }

            AgentToolExecutionResult lastResult = results.isEmpty() ? null : results.get(results.size() - 1);
            String reply = buildLlmFinalReply(prompt, results);
            if (isBlank(reply)) {
                reply = buildReply(lastResult);
            }

            return new AgentChatResponse(
                    sessionId,
                    reply,
                    lastResult,
                    results,
                    buildSuggestions(lastResult)
            );
        } catch (RuntimeException e) {
            return null;
        }
    }

    private List<Map<String, Object>> buildPlannerMessages(String prompt, AgentSessionContext context) {
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(mapOf(
                "role", "system",
                "content", buildPlannerSystemPrompt(context)
        ));
        for (Message message : context.getRecentMessages(MAX_SESSION_MESSAGES - 1)) {
            messages.add(mapOf(
                    "role", safeRole(message.getRole()),
                    "content", defaultText(message.getContent(), "")
            ));
        }
        if (messages.stream().noneMatch(message -> prompt.equals(message.get("content")))) {
            messages.add(mapOf("role", "user", "content", prompt));
        }
        return messages;
    }

    private String buildPlannerSystemPrompt(AgentSessionContext context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是 iStudySpot 的只读智能助手编排器。\n");
        prompt.append("只能使用工具读取自习室、座位、预约和预约规则信息。\n");
        prompt.append("绝不能创建、取消、续约、签到、签退、支付、修改或删除任何业务数据。\n");
        prompt.append("如果用户要求执行操作，请说明你只能查询信息，并引导用户到 App 对应页面完成。\n");
        prompt.append("不要猜测工具未返回的价格、退款、扣分、库存或政策。\n");
        prompt.append("优先只调用一个工具；缺少必要编号时，只问一个简洁的澄清问题。\n");
        prompt.append("默认使用中文回答；如果用户明确使用其他语言，再保持同语言。\n");
        if (context.currentStudyRoomId != null) {
            prompt.append("当前会话中的 studyRoomId: ").append(context.currentStudyRoomId).append(".\n");
        }
        if (!isBlank(context.currentStudyRoomName)) {
            prompt.append("当前会话中的自习室名称: ").append(context.currentStudyRoomName).append(".\n");
        }
        return prompt.toString();
    }

    private String buildLlmFinalReply(String userPrompt, List<AgentToolExecutionResult> results) {
        if (deepSeekService == null || results == null || results.isEmpty()) {
            return null;
        }

        try {
            List<Map<String, Object>> messages = List.of(
                    mapOf(
                            "role", "system",
                            "content", "你是 iStudySpot 智能助手。请仅基于提供的只读工具结果，用中文简洁回答。不要暴露隐藏字段或原始 id。如果用户要求写操作，请说明助手只能查询信息。"
                    ),
                    mapOf(
                            "role", "user",
                            "content", userPrompt
                    ),
                    mapOf(
                            "role", "system",
                            "content", "工具结果 JSON:\n" + objectMapper.writeValueAsString(results)
                    )
            );
            JsonNode response = deepSeekService.chatCompletion(model, messages, List.of());
            return extractContent(firstChoiceMessage(response));
        } catch (Exception e) {
            return null;
        }
    }

    private List<Map<String, Object>> buildToolSchemas(List<AgentToolDefinition> catalog) {
        List<Map<String, Object>> schemas = new ArrayList<>();
        for (AgentToolDefinition definition : catalog) {
            schemas.add(mapOf(
                    "type", "function",
                    "function", mapOf(
                            "name", definition.getName(),
                            "description", definition.getDescription(),
                            "parameters", toJsonSchema(definition.getInputSchema())
                    )
            ));
        }
        return schemas;
    }

    private Map<String, Object> toJsonSchema(Map<String, Object> inputSchema) {
        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();
        if (inputSchema != null) {
            for (Map.Entry<String, Object> entry : inputSchema.entrySet()) {
                String rawType = entry.getValue() == null ? "string?" : entry.getValue().toString();
                boolean optional = rawType.endsWith("?");
                String type = optional ? rawType.substring(0, rawType.length() - 1) : rawType;
                properties.put(entry.getKey(), mapOf("type", toJsonType(type)));
                if (!optional) {
                    required.add(entry.getKey());
                }
            }
        }
        Map<String, Object> schema = mapOf(
                "type", "object",
                "properties", properties,
                "additionalProperties", false
        );
        if (!required.isEmpty()) {
            schema.put("required", required);
        }
        return schema;
    }

    private String toJsonType(String type) {
        return switch (type) {
            case "number" -> "number";
            case "boolean" -> "boolean";
            default -> "string";
        };
    }

    private JsonNode firstChoiceMessage(JsonNode response) {
        if (response == null) {
            return null;
        }
        JsonNode choices = response.get("choices");
        if (choices == null || choices.isEmpty()) {
            return null;
        }
        return choices.get(0).get("message");
    }

    private List<ToolCall> parseToolCalls(JsonNode message) {
        if (message == null || !message.has("tool_calls")) {
            return List.of();
        }
        JsonNode calls = message.get("tool_calls");
        if (calls == null || !calls.isArray()) {
            return List.of();
        }

        List<ToolCall> toolCalls = new ArrayList<>();
        for (JsonNode call : calls) {
            JsonNode function = call.get("function");
            if (function == null) {
                continue;
            }
            String name = function.path("name").asText(null);
            String argumentsJson = function.path("arguments").asText("{}");
            if (isBlank(name)) {
                continue;
            }
            JsonNode argumentsNode = function.get("arguments");
            toolCalls.add(new ToolCall(name, parseArguments(argumentsNode, argumentsJson)));
        }
        return toolCalls;
    }

    private Map<String, Object> parseArguments(JsonNode argumentsNode, String fallbackJson) {
        if (argumentsNode != null && argumentsNode.isObject()) {
            return objectMapper.convertValue(argumentsNode, new TypeReference<LinkedHashMap<String, Object>>() {
            });
        }
        return parseArguments(fallbackJson);
    }

    private Map<String, Object> parseArguments(String argumentsJson) {
        if (isBlank(argumentsJson)) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(argumentsJson, new TypeReference<LinkedHashMap<String, Object>>() {
            });
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    private Map<String, Object> enrichToolArguments(String toolName, Map<String, Object> arguments, AgentSessionContext context) {
        Map<String, Object> enriched = new LinkedHashMap<>();
        if (arguments != null) {
            enriched.putAll(arguments);
        }
        if (requiresStudyRoomId(toolName) && toLong(enriched.get("studyRoomId")) == null && context.currentStudyRoomId != null) {
            enriched.put("studyRoomId", context.currentStudyRoomId);
        }
        return enriched;
    }

    private boolean requiresStudyRoomId(String toolName) {
        return "get_study_room_detail".equals(toolName) || "list_room_seats".equals(toolName);
    }

    private AgentChatResponse actionNotAvailableResponse(String sessionId) {
        return new AgentChatResponse(
                sessionId,
                "我只能帮你查询 iStudySpot 信息。预约、取消、支付、签到、签退等操作请到 App 对应页面完成。",
                null,
                List.of(),
                buildDefaultSuggestions()
        );
    }

    private AgentChatResponse tryNavigationResponse(String sessionId, String prompt) {
        NavigationTarget target = detectNavigationTarget(prompt);
        if (target == null) {
            return null;
        }
        return new AgentChatResponse(
                sessionId,
                target.reply(),
                null,
                List.of(),
                mapOf(
                        "type", "navigate",
                        "route", target.route(),
                        "params", Map.of()
                ),
                target.suggestedPrompts()
        );
    }

    private NavigationTarget detectNavigationTarget(String prompt) {
        String normalized = prompt.toLowerCase(Locale.ROOT);
        boolean asksToOpen = containsAny(normalized, "show", "open", "display", "go to", "navigate", "view")
                || containsAny(prompt, "展示", "显示", "打开", "跳转", "进入", "看看", "查看");
        if (!asksToOpen) {
            return null;
        }
        if (containsAny(normalized, "study record", "study history", "study statistics")
                || containsAny(prompt, "学习记录", "学习统计", "自习记录", "学习时长")) {
            return new NavigationTarget(
                    "study_record",
                    "可以，我会在下方提供入口，你可以打开学习记录页面查看完整统计。",
                    List.of("查看可用自习室", "查看我的预约", "查看学习待办")
            );
        }
        if (containsAny(normalized, "todo", "task", "to-do", "to do")
                || containsAny(prompt, "学习待办", "待办", "学习任务", "任务清单")) {
            return new NavigationTarget(
                    "todo_list",
                    "可以，我会在下方提供入口，你可以打开学习待办页面查看或管理任务。",
                    List.of("查看学习记录", "查看我的预约", "查看可用自习室")
            );
        }
        return null;
    }

    private boolean isWriteActionIntent(String prompt) {
        String normalized = prompt.toLowerCase(Locale.ROOT);
        if (containsAny(normalized, "rule", "policy", "deadline", "how do i", "how to", "can i", "what is", "when can")) {
            return false;
        }
        if (containsAny(prompt, "规则", "政策", "说明", "怎么", "如何", "能不能", "可以", "什么时候")) {
            return false;
        }
        return containsAny(
                normalized,
                "book a", "book the", "reserve a", "reserve the", "make a reservation", "create reservation",
                "cancel my", "cancel this", "cancel reservation", "renew my", "renew reservation",
                "check in", "check-in", "checkout", "check out", "pay for", "pay order",
                "modify my", "change my", "delete my"
        ) || containsAny(prompt, "帮我预约", "替我预约", "立即预约", "创建预约", "取消我的", "取消这笔", "取消预约",
                "续约", "签到", "签退", "支付", "付款", "修改我的", "改一下我的", "删除我的");
    }

    private String extractContent(JsonNode message) {
        if (message == null || !message.has("content") || message.get("content").isNull()) {
            return null;
        }
        String content = message.get("content").asText();
        return isBlank(content) ? null : content.trim();
    }

    private String safeRole(String role) {
        if ("assistant".equals(role) || "user".equals(role) || "system".equals(role)) {
            return role;
        }
        return "user";
    }

    private ToolDecision decide(String prompt, AgentSessionContext context) {
        String normalized = prompt.toLowerCase(Locale.ROOT);
        Long roomId = extractFirstLong(prompt);
        if (roomId == null) {
            roomId = context.currentStudyRoomId;
        }

        if (containsAny(normalized, "price", "refund", "payment", "credit", "points", "inventory")
                || containsAny(prompt, "价格", "费用", "退款", "支付", "积分", "库存")) {
            return ToolDecision.reply(
                    "我无法在这里确认该业务细节。请查看规则页、预约页，或联系客服确认。",
                    buildDefaultSuggestions()
            );
        }

        if (containsAny(normalized, "rule", "cancel", "no-show", "violation", "policy")
                || containsAny(prompt, "规则", "政策", "取消", "爽约", "违约")) {
            return ToolDecision.tool("get_reservation_rules", Map.of());
        }

        if (containsAny(normalized, "my reservation", "my booking", "my order", "reservation history", "order history")
                || containsAny(prompt, "我的预约", "我的订单", "预约记录", "订单记录")) {
            return ToolDecision.tool("get_my_reservations", buildReservationArguments(normalized));
        }

        if (containsAny(normalized, "seat", "available seat", "vacancy")
                || containsAny(prompt, "座位", "空位")) {
            if (roomId == null) {
                return ToolDecision.reply(
                        "请先提供自习室编号再查询座位。你可以先问：查看可用自习室。",
                        buildDefaultSuggestions()
                );
            }
            return ToolDecision.tool("list_room_seats", Map.of("studyRoomId", roomId));
        }

        if (containsAny(normalized, "detail", "address", "location", "open", "info")
                && containsAny(normalized, "study room", "room", "classroom")
                || containsAny(prompt, "详情", "地址", "位置", "开放", "营业", "信息")
                && containsAny(prompt, "自习室", "房间", "教室")) {
            if (roomId != null) {
                return ToolDecision.tool("get_study_room_detail", Map.of("studyRoomId", roomId));
            }
            return ToolDecision.tool("list_study_rooms", buildRoomArguments(prompt));
        }

        if (containsAny(normalized, "study room", "room", "available rooms", "classroom", "library")
                || containsAny(prompt, "自习室", "房间", "教室", "图书馆")) {
            return ToolDecision.tool("list_study_rooms", buildRoomArguments(prompt));
        }

        return ToolDecision.reply(
                "我可以帮你查询自习室、座位、预约记录和预约规则。",
                buildDefaultSuggestions()
        );
    }

    private AgentToolExecuteRequest buildToolRequest(ToolDecision decision) {
        AgentToolExecuteRequest request = new AgentToolExecuteRequest();
        request.setTool(decision.tool());
        request.setArguments(new LinkedHashMap<>(decision.arguments()));
        return request;
    }

    private void updateContext(AgentSessionContext context, AgentToolExecutionResult result) {
        if (result == null || isBlank(result.getTool())) {
            return;
        }

        Map<String, Object> data = safeData(result);
        if ("list_study_rooms".equals(result.getTool())) {
            List<Map<String, Object>> items = listOfMaps(data.get("items"));
            if (items.size() == 1) {
                context.currentStudyRoomId = toLong(items.get(0).get("id"));
                context.currentStudyRoomName = asString(items.get(0).get("name"));
            }
            return;
        }

        if ("get_study_room_detail".equals(result.getTool())) {
            Map<String, Object> room = mapValue(data.get("studyRoom"));
            context.currentStudyRoomId = toLong(room.get("id"));
            context.currentStudyRoomName = asString(room.get("name"));
            return;
        }

        if ("list_room_seats".equals(result.getTool())) {
            context.currentStudyRoomId = toLong(data.get("studyRoomId"));
        }
    }

    private String buildReply(AgentToolExecutionResult result) {
        if (result == null) {
            return "工具没有返回可展示的数据。";
        }

        Map<String, Object> data = safeData(result);
        return switch (result.getTool()) {
            case "list_study_rooms" -> {
                int count = listOfMaps(data.get("items")).size();
                if (count == 0) {
                    yield "没有找到匹配的自习室，可以换个关键词试试。";
                }
                yield "找到 " + count + " 间自习室。你可以继续查看详情或座位。";
            }
            case "get_study_room_detail" -> {
                Map<String, Object> room = mapValue(data.get("studyRoom"));
                String roomName = defaultText(asString(room.get("name")), "该自习室");
                String address = asString(room.get("address"));
                if (isBlank(address)) {
                    yield "这是 " + roomName + " 的详情。";
                }
                yield "这是 " + roomName + " 的详情，位置在 " + address + "。";
            }
            case "list_room_seats" -> {
                List<Map<String, Object>> seats = listOfMaps(data.get("items"));
                long availableCount = seats.stream()
                        .filter(this::isAvailableSeat)
                        .count();
                yield "找到 " + seats.size() + " 个座位，其中约 " + availableCount + " 个显示可用。";
            }
            case "get_my_reservations" -> {
                int count = listOfMaps(data.get("items")).size();
                if (count == 0) {
                    yield "你当前没有可展示的预约记录。";
                }
                yield "找到 " + count + " 条预约记录。你可以打开预约列表查看完整详情。";
            }
            case "get_reservation_rules" -> {
                Object maxAdvanceDays = data.get("maxAdvanceDays");
                if (maxAdvanceDays != null) {
                    yield "已找到预约规则。你最多可以提前 " + maxAdvanceDays + " 天预约。";
                }
                yield "已找到预约规则。";
            }
            default -> defaultText(result.getSummary(), "工具查询已完成。");
        };
    }

    private List<String> buildSuggestions(AgentToolExecutionResult result) {
        if (result == null || isBlank(result.getTool())) {
            return buildDefaultSuggestions();
        }

        if ("list_study_rooms".equals(result.getTool())) {
            Long roomId = firstRoomId(result);
            String seatPrompt = roomId == null ? "查看 1 号自习室的座位" : "查看 " + roomId + " 号自习室的座位";
            return List.of(seatPrompt, "查看预约规则", "查看我的预约");
        }
        if ("get_study_room_detail".equals(result.getTool())) {
            Map<String, Object> room = mapValue(safeData(result).get("studyRoom"));
            Long roomId = toLong(room.get("id"));
            String seatPrompt = roomId == null ? "查看该自习室的座位" : "查看 " + roomId + " 号自习室的座位";
            return List.of(seatPrompt, "查看我的预约", "查看预约规则");
        }
        if ("list_room_seats".equals(result.getTool())) {
            return List.of("查看我的预约", "查看预约规则", "查看可用自习室");
        }
        if ("get_my_reservations".equals(result.getTool())) {
            return List.of("查看预约规则", "查看可用自习室", "查看 1 号自习室的座位");
        }
        if ("get_reservation_rules".equals(result.getTool())) {
            return List.of("查看我的预约", "查看可用自习室", "查看 1 号自习室的座位");
        }
        return buildDefaultSuggestions();
    }

    private List<String> buildDefaultSuggestions() {
        return DEFAULT_SUGGESTIONS;
    }

    private Map<String, Object> buildRoomArguments(String prompt) {
        String keyword = extractRoomKeyword(prompt);
        if (isBlank(keyword)) {
            return Map.of();
        }
        return Map.of("keyword", keyword);
    }

    private Map<String, Object> buildReservationArguments(String normalizedPrompt) {
        String status = null;
        if (containsAny(normalizedPrompt, "pending", "unpaid")) {
            status = "pending";
        } else if (containsAny(normalizedPrompt, "in use", "in_use", "active")) {
            status = "in_use";
        } else if (containsAny(normalizedPrompt, "cancel", "cancelled", "canceled")) {
            status = "cancelled";
        } else if (containsAny(normalizedPrompt, "complete", "completed")) {
            status = "completed";
        } else if (containsAny(normalizedPrompt, "paid")) {
            status = "paid";
        }
        if (status == null) {
            return Map.of();
        }
        return Map.of("status", status);
    }

    private String extractRoomKeyword(String prompt) {
        String cleaned = prompt.toLowerCase(Locale.ROOT);
        for (String word : List.of(
                "show", "find", "search", "available", "study", "room", "rooms", "classroom",
                "library", "detail", "details", "address", "location", "open", "info", "seat", "seats",
                "reservation", "reservations", "rule", "rules", "for", "the", "a", "an", "please"
        )) {
            cleaned = cleaned.replace(word, " ");
        }
        cleaned = cleaned.replaceAll("\\d+", " ");
        cleaned = cleaned.replaceAll("[^a-z0-9 ]", " ");
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        if (cleaned.isBlank() || cleaned.length() > 30) {
            return null;
        }
        return cleaned;
    }

    private void sendStreamError(SseEmitter emitter, String errorCode, boolean retryable) {
        try {
            emitter.send(SseEmitter.event()
                    .name("agent_error")
                    .data(Map.of(
                            "schemaVersion", "1.0",
                            "error", Map.of(
                                    "code", errorCode,
                                    "retryable", retryable
                            )
                    )));
        } catch (IOException ignored) {
            // The client may have disconnected; completing keeps the emitter lifecycle tidy.
        } finally {
            emitter.complete();
        }
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private Long extractFirstLong(String text) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d+)").matcher(text);
        if (matcher.find()) {
            return toLong(matcher.group(1));
        }
        return null;
    }

    private Long firstRoomId(AgentToolExecutionResult result) {
        List<Map<String, Object>> items = listOfMaps(safeData(result).get("items"));
        if (items.isEmpty()) {
            return null;
        }
        return toLong(items.get(0).get("id"));
    }

    private boolean isAvailableSeat(Map<String, Object> seat) {
        String status = asString(seat.get("status"));
        if (status == null) {
            return false;
        }
        String normalized = status.toLowerCase(Locale.ROOT);
        return "available".equals(normalized)
                || "free".equals(normalized)
                || "idle".equals(normalized)
                || "1".equals(normalized);
    }

    private Map<String, Object> safeData(AgentToolExecutionResult result) {
        return result.getData() == null ? Map.of() : result.getData();
    }

    private String buildSessionKey(Long userId, String sessionId) {
        return userId + ":" + sessionId;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String defaultText(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Map<String, Object> mapOf(Object... values) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            Object key = values[i];
            Object value = values[i + 1];
            if (value != null) {
                map.put(key.toString(), value);
            }
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mapValue(Object value) {
        return value instanceof Map<?, ?> ? (Map<String, Object>) value : Map.of();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> listOfMaps(Object value) {
        return value instanceof List<?> ? (List<Map<String, Object>>) value : List.of();
    }

    private record ToolDecision(
            String tool,
            Map<String, Object> arguments,
            String reply,
            List<String> suggestedPrompts
    ) {
        static ToolDecision tool(String tool, Map<String, Object> arguments) {
            return new ToolDecision(tool, arguments, null, List.of());
        }

        static ToolDecision reply(String reply, List<String> suggestedPrompts) {
            return new ToolDecision(null, Map.of(), reply, suggestedPrompts);
        }

        boolean hasTool() {
            return tool != null && !tool.isBlank();
        }
    }

    private record ToolCall(String name, Map<String, Object> arguments) {
    }

    private record PolicyDecision(boolean allowedReadOnly, String reply) {
    }

    private record NavigationTarget(String route, String reply, List<String> suggestedPrompts) {
    }

    private static class AgentSessionContext {
        private final Long userId;
        private final String sessionId;
        private final List<Message> messages = new ArrayList<>();
        private Long currentStudyRoomId;
        private String currentStudyRoomName;

        private AgentSessionContext(Long userId, String sessionId) {
            this.userId = userId;
            this.sessionId = sessionId;
        }

        private void addMessage(String role, String content) {
            messages.add(new Message(role, content));
            while (messages.size() > MAX_SESSION_MESSAGES) {
                messages.remove(0);
            }
        }

        private List<Message> getRecentMessages(int limit) {
            if (messages.size() <= limit) {
                return List.copyOf(messages);
            }
            return List.copyOf(messages.subList(messages.size() - limit, messages.size()));
        }
    }
}
