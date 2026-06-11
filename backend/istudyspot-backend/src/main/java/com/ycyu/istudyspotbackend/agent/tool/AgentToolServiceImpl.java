package com.ycyu.istudyspotbackend.agent.tool;

import com.ycyu.istudyspotbackend.entity.Order;
import com.ycyu.istudyspotbackend.entity.Seat;
import com.ycyu.istudyspotbackend.entity.StudyRoom;
import com.ycyu.istudyspotbackend.service.OrderService;
import com.ycyu.istudyspotbackend.service.SeatService;
import com.ycyu.istudyspotbackend.service.StudyRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class AgentToolServiceImpl implements AgentToolService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private StudyRoomService studyRoomService;

    @Autowired
    private SeatService seatService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ReservationRulesProvider reservationRulesProvider;

    @Override
    public List<AgentToolDefinition> getCatalog() {
        List<AgentToolDefinition> tools = new ArrayList<>();
        tools.add(new AgentToolDefinition(
                "list_study_rooms",
                "List study rooms",
                "Return a safe list of study rooms for search and filtering.",
                true,
                List.of("studyroom", "read"),
                mapOf(
                        "status", "string?",
                        "keyword", "string?",
                        "page", "number?",
                        "pageSize", "number?"
                )
        ));
        tools.add(new AgentToolDefinition(
                "get_study_room_detail",
                "Get study room detail",
                "Return safe details for one study room.",
                true,
                List.of("studyroom", "read"),
                mapOf("studyRoomId", "number")
        ));
        tools.add(new AgentToolDefinition(
                "list_room_seats",
                "List room seats",
                "Return seats for one study room, optionally filtered by status and type.",
                true,
                List.of("seat", "read"),
                mapOf(
                        "studyRoomId", "number",
                        "status", "string?",
                        "type", "string?"
                )
        ));
        tools.add(new AgentToolDefinition(
                "get_my_reservations",
                "Get my reservations",
                "Return a redacted reservation view for the authenticated user.",
                true,
                List.of("reservation", "read", "sensitive"),
                mapOf(
                        "status", "string?",
                        "page", "number?",
                        "pageSize", "number?"
                )
        ));
        tools.add(new AgentToolDefinition(
                "get_reservation_rules",
                "Get reservation rules",
                "Return reservation limits, cancellation rules, and no-show penalties.",
                true,
                List.of("reservation", "read", "rules"),
                Map.of()
        ));
        return List.copyOf(tools);
    }

    @Override
    public AgentToolExecutionResult execute(Long userId, AgentToolExecuteRequest request) {
        if (request == null || request.getTool() == null || request.getTool().isBlank()) {
            throw new IllegalArgumentException("MISSING_TOOL");
        }

        return switch (request.getTool()) {
            case "list_study_rooms" -> listStudyRooms(request);
            case "get_study_room_detail" -> getStudyRoomDetail(request);
            case "list_room_seats" -> listRoomSeats(request);
            case "get_my_reservations" -> getMyReservations(userId, request);
            case "get_reservation_rules" -> getReservationRules();
            default -> throw new IllegalArgumentException("UNSUPPORTED_TOOL");
        };
    }

    private AgentToolExecutionResult listStudyRooms(AgentToolExecuteRequest request) {
        String status = asString(request.getArguments().get("status"));
        String keyword = asString(request.getArguments().get("keyword"));
        int page = defaultInt(asInteger(request.getArguments().get("page")), 1);
        int pageSize = defaultInt(asInteger(request.getArguments().get("pageSize")), 20);

        Map<String, Object> result = studyRoomService.getStudyRoomList(status, null, keyword, page, pageSize);
        @SuppressWarnings("unchecked")
        List<StudyRoom> rooms = (List<StudyRoom>) result.getOrDefault("list", List.of());
        List<Map<String, Object>> safeRooms = rooms.stream().map(this::toSafeStudyRoom).toList();

        return new AgentToolExecutionResult(
                "list_study_rooms",
                "Loaded " + safeRooms.size() + " study rooms.",
                mapOf(
                        "items", safeRooms,
                        "page", result.get("page"),
                        "pageSize", result.get("pageSize"),
                        "total", result.get("total")
                ),
                mapOf(
                        "type", "navigate",
                        "route", "studyroom_list",
                        "params", mapOf("keyword", keyword == null ? "" : keyword)
                ),
                Map.of()
        );
    }

    private AgentToolExecutionResult getStudyRoomDetail(AgentToolExecuteRequest request) {
        Long studyRoomId = requireLong(request.getArguments(), "studyRoomId");
        StudyRoom room = studyRoomService.getStudyRoomDetail(studyRoomId);
        Map<String, Object> safeRoom = toSafeStudyRoom(room);

        return new AgentToolExecutionResult(
                "get_study_room_detail",
                "Loaded study room detail: " + room.getName(),
                mapOf("studyRoom", safeRoom),
                mapOf(
                        "type", "navigate",
                        "route", "studyroom_detail",
                        "params", mapOf("studyRoomId", studyRoomId)
                ),
                Map.of()
        );
    }

    private AgentToolExecutionResult listRoomSeats(AgentToolExecuteRequest request) {
        Long studyRoomId = requireLong(request.getArguments(), "studyRoomId");
        String status = asString(request.getArguments().get("status"));
        String type = asString(request.getArguments().get("type"));
        List<Seat> seats = seatService.getSeatList(studyRoomId, status, type, null, null);

        List<Map<String, Object>> safeSeats = seats.stream()
                .map(this::toSafeSeat)
                .toList();

        return new AgentToolExecutionResult(
                "list_room_seats",
                "Loaded " + safeSeats.size() + " seats.",
                mapOf(
                        "studyRoomId", studyRoomId,
                        "items", safeSeats,
                        "statusFilter", status == null ? "" : status,
                        "typeFilter", type == null ? "" : type
                ),
                mapOf(
                        "type", "navigate",
                        "route", "seat_list",
                        "params", mapOf("studyRoomId", studyRoomId)
                ),
                Map.of()
        );
    }

    private AgentToolExecutionResult getMyReservations(Long userId, AgentToolExecuteRequest request) {
        if (userId == null) {
            throw new IllegalArgumentException("AUTH_REQUIRED");
        }

        String status = asString(request.getArguments().get("status"));
        int page = defaultInt(asInteger(request.getArguments().get("page")), 1);
        int pageSize = defaultInt(asInteger(request.getArguments().get("pageSize")), 20);

        Map<String, Object> result = orderService.getOrderList(userId, status, null, null, page, pageSize);
        @SuppressWarnings("unchecked")
        List<Order> orders = (List<Order>) result.getOrDefault("list", List.of());

        List<Map<String, Object>> safeOrders = new ArrayList<>();
        Map<String, Object> references = new LinkedHashMap<>();

        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            String ref = "ORDER_REF_" + (i + 1);
            safeOrders.add(toSafeOrder(order, ref));
            references.put(ref, mapOf(
                    "type", "reservation",
                    "display", mapOf(
                            "status", order.getStatus(),
                            "timeRange", formatRange(order),
                            "roomName", firstNonBlank(order.getStudyRoomName(), order.getRoomName()),
                            "seatPosition", firstNonBlank(order.getSeatPosition(), order.getSeatNumber())
                    )
            ));
        }

        return new AgentToolExecutionResult(
                "get_my_reservations",
                "Loaded " + safeOrders.size() + " reservation records.",
                mapOf(
                        "items", safeOrders,
                        "page", result.get("page"),
                        "pageSize", result.get("pageSize"),
                        "total", result.get("total")
                ),
                mapOf(
                        "type", "navigate",
                        "route", "reservation_list",
                        "params", mapOf("status", status == null ? "" : status)
                ),
                references
        );
    }

    private AgentToolExecutionResult getReservationRules() {
        return new AgentToolExecutionResult(
                "get_reservation_rules",
                "Loaded reservation rules.",
                reservationRulesProvider.getRules(),
                mapOf(
                        "type", "navigate",
                        "route", "reservation_rules",
                        "params", Map.of()
                ),
                Map.of()
        );
    }

    private Map<String, Object> toSafeStudyRoom(StudyRoom room) {
        return mapOf(
                "id", room.getId(),
                "name", room.getName(),
                "address", room.getAddress(),
                "openTime", room.getOpenTime() == null ? null : room.getOpenTime().toString(),
                "closeTime", room.getCloseTime() == null ? null : room.getCloseTime().toString(),
                "description", room.getDescription(),
                "imageUrl", room.getImageUrl(),
                "status", room.getStatus()
        );
    }

    private Map<String, Object> toSafeSeat(Seat seat) {
        return mapOf(
                "id", seat.getId(),
                "roomId", seat.getRoomId(),
                "seatNumber", seat.getSeatNumber(),
                "rowNum", seat.getRowNum(),
                "colNum", seat.getColNum(),
                "seatType", seat.getSeatType(),
                "status", seat.getStatus(),
                "hasPower", seat.getHasPower(),
                "hasLamp", seat.getHasLamp(),
                "isWindow", seat.getIsWindow(),
                "description", seat.getDescription()
        );
    }

    private Map<String, Object> toSafeOrder(Order order, String ref) {
        return mapOf(
                "reference", ref,
                "status", order.getStatus(),
                "roomName", firstNonBlank(order.getStudyRoomName(), order.getRoomName()),
                "seatPosition", firstNonBlank(order.getSeatPosition(), order.getSeatNumber()),
                "timeRange", formatRange(order),
                "canCancel", "pending".equals(order.getStatus()) || "paid".equals(order.getStatus()),
                "canRenew", "in_use".equals(order.getStatus()),
                "sensitiveFieldsHidden", List.of("userId", "orderNo", "totalPrice", "totalAmount")
        );
    }

    private String formatRange(Order order) {
        if (order.getStartTime() == null || order.getEndTime() == null) {
            return "";
        }
        return order.getStartTime().format(DATE_TIME_FORMATTER) + " - " + order.getEndTime().format(DATE_TIME_FORMATTER);
    }

    private Long requireLong(Map<String, Object> arguments, String key) {
        Long value = asLong(arguments.get(key));
        if (value == null) {
            throw new IllegalArgumentException("MISSING_" + key.toUpperCase());
        }
        return value;
    }

    private String asString(Object value) {
        if (value == null) {
            return null;
        }
        String stringValue = value.toString().trim();
        return stringValue.isEmpty() ? null : stringValue;
    }

    private Integer asInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Long asLong(Object value) {
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

    private int defaultInt(Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    private Map<String, Object> mapOf(Object... values) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            Object key = values[i];
            Object value = values[i + 1];
            if (value != null) {
                map.put(Objects.toString(key), value);
            }
        }
        return map;
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second == null ? "" : second;
    }
}
