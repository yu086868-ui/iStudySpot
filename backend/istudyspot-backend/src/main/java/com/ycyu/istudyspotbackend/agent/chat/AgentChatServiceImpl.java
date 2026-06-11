package com.ycyu.istudyspotbackend.agent.chat;

import com.ycyu.istudyspotbackend.agent.tool.AgentToolExecuteRequest;
import com.ycyu.istudyspotbackend.agent.tool.AgentToolExecutionResult;
import com.ycyu.istudyspotbackend.agent.tool.AgentToolService;
import com.ycyu.istudyspotbackend.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AgentChatServiceImpl implements AgentChatService {

    private static final int MAX_SESSION_MESSAGES = 20;
    private static final List<String> DEFAULT_SUGGESTIONS = List.of(
            "Show available study rooms",
            "Show reservation rules",
            "Show my reservations",
            "Show seats for room 1"
    );

    private final AgentToolService agentToolService;
    private final Map<String, AgentSessionContext> sessions = new ConcurrentHashMap<>();

    @Autowired
    public AgentChatServiceImpl(AgentToolService agentToolService) {
        this.agentToolService = agentToolService;
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

    private ToolDecision decide(String prompt, AgentSessionContext context) {
        String normalized = prompt.toLowerCase(Locale.ROOT);
        Long roomId = extractFirstLong(prompt);
        if (roomId == null) {
            roomId = context.currentStudyRoomId;
        }

        if (containsAny(normalized, "price", "refund", "payment", "credit", "points", "inventory")) {
            return ToolDecision.reply(
                    "I cannot confirm that business detail here. Please check the rules page, order page, or contact support.",
                    buildDefaultSuggestions()
            );
        }

        if (containsAny(normalized, "rule", "cancel", "no-show", "violation", "policy")) {
            return ToolDecision.tool("get_reservation_rules", Map.of());
        }

        if (containsAny(normalized, "my reservation", "my booking", "my order", "reservation history", "order history")) {
            return ToolDecision.tool("get_my_reservations", buildReservationArguments(normalized));
        }

        if (containsAny(normalized, "seat", "available seat", "vacancy")) {
            if (roomId == null) {
                return ToolDecision.reply(
                        "Please provide a study room id before checking seats. You can ask: Show available study rooms.",
                        buildDefaultSuggestions()
                );
            }
            return ToolDecision.tool("list_room_seats", Map.of("studyRoomId", roomId));
        }

        if (containsAny(normalized, "detail", "address", "location", "open", "info")
                && containsAny(normalized, "study room", "room", "classroom")) {
            if (roomId != null) {
                return ToolDecision.tool("get_study_room_detail", Map.of("studyRoomId", roomId));
            }
            return ToolDecision.tool("list_study_rooms", buildRoomArguments(prompt));
        }

        if (containsAny(normalized, "study room", "room", "available rooms", "classroom", "library")) {
            return ToolDecision.tool("list_study_rooms", buildRoomArguments(prompt));
        }

        return ToolDecision.reply(
                "I can help with study rooms, seats, reservations, and reservation rules.",
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
            return "The tool returned no displayable data.";
        }

        Map<String, Object> data = safeData(result);
        return switch (result.getTool()) {
            case "list_study_rooms" -> {
                int count = listOfMaps(data.get("items")).size();
                if (count == 0) {
                    yield "No matching study rooms were found. Try another keyword.";
                }
                yield "I found " + count + " study rooms. You can view details or check seats next.";
            }
            case "get_study_room_detail" -> {
                Map<String, Object> room = mapValue(data.get("studyRoom"));
                String roomName = defaultText(asString(room.get("name")), "this study room");
                String address = asString(room.get("address"));
                if (isBlank(address)) {
                    yield "Here are the details for " + roomName + ".";
                }
                yield "Here are the details for " + roomName + ", located at " + address + ".";
            }
            case "list_room_seats" -> {
                List<Map<String, Object>> seats = listOfMaps(data.get("items"));
                long availableCount = seats.stream()
                        .filter(this::isAvailableSeat)
                        .count();
                yield "I found " + seats.size() + " seats, with about " + availableCount + " shown as available.";
            }
            case "get_my_reservations" -> {
                int count = listOfMaps(data.get("items")).size();
                if (count == 0) {
                    yield "You do not have displayable reservations right now.";
                }
                yield "I found " + count + " reservation records. You can open the order page for full details.";
            }
            case "get_reservation_rules" -> {
                Object maxAdvanceDays = data.get("maxAdvanceDays");
                if (maxAdvanceDays != null) {
                    yield "I found the reservation rules. You can reserve up to " + maxAdvanceDays + " days in advance.";
                }
                yield "I found the reservation rules.";
            }
            default -> defaultText(result.getSummary(), "Tool execution completed.");
        };
    }

    private List<String> buildSuggestions(AgentToolExecutionResult result) {
        if (result == null || isBlank(result.getTool())) {
            return buildDefaultSuggestions();
        }

        if ("list_study_rooms".equals(result.getTool())) {
            Long roomId = firstRoomId(result);
            String seatPrompt = roomId == null ? "Show seats for room 1" : "Show seats for room " + roomId;
            return List.of(seatPrompt, "Show reservation rules", "Show my reservations");
        }
        if ("get_study_room_detail".equals(result.getTool())) {
            Map<String, Object> room = mapValue(safeData(result).get("studyRoom"));
            Long roomId = toLong(room.get("id"));
            String seatPrompt = roomId == null ? "Show seats for this room" : "Show seats for room " + roomId;
            return List.of(seatPrompt, "Show my reservations", "Show reservation rules");
        }
        if ("list_room_seats".equals(result.getTool())) {
            return List.of("Show my reservations", "Show reservation rules", "Show available study rooms");
        }
        if ("get_my_reservations".equals(result.getTool())) {
            return List.of("Show reservation rules", "Show available study rooms", "Show seats for room 1");
        }
        if ("get_reservation_rules".equals(result.getTool())) {
            return List.of("Show my reservations", "Show available study rooms", "Show seats for room 1");
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
    }
}
