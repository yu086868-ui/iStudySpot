package com.ycyu.istudyspotbackend.agent.tool;

import com.ycyu.istudyspotbackend.entity.StudyRoom;
import com.ycyu.istudyspotbackend.entity.Order;
import com.ycyu.istudyspotbackend.service.OrderService;
import com.ycyu.istudyspotbackend.service.SeatService;
import com.ycyu.istudyspotbackend.service.StudyRoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentToolServiceImplTest {

    @InjectMocks
    private AgentToolServiceImpl agentToolService;

    @Mock
    private StudyRoomService studyRoomService;

    @Mock
    private SeatService seatService;

    @Mock
    private OrderService orderService;

    @Mock
    private ReservationRulesProvider reservationRulesProvider;

    private StudyRoom room;

    @BeforeEach
    void setUp() {
        room = new StudyRoom();
        room.setId(1L);
        room.setName("Quiet Room");
        room.setStatus(1);
        room.setAddress("Library 3F");
    }

    @Test
    void catalogShouldRequireAuthForAllTools() {
        assertFalse(agentToolService.getCatalog().isEmpty());
        assertTrue(agentToolService.getCatalog().stream().allMatch(AgentToolDefinition::isRequiresAuth));
    }

    @Test
    void listStudyRoomsShouldReturnStructuredPayload() {
        AgentToolExecuteRequest request = new AgentToolExecuteRequest();
        request.setTool("list_study_rooms");
        request.setArguments(Map.of("keyword", "Quiet", "page", 1, "pageSize", 1));

        when(studyRoomService.getStudyRoomList(null, null, "Quiet", 1, 1)).thenReturn(Map.of(
                "list", List.of(room),
                "page", 1,
                "pageSize", 1,
                "total", 1
        ));

        AgentToolExecutionResult result = agentToolService.execute(1L, request);

        assertEquals("1.0", result.getSchemaVersion());
        assertEquals("response", result.getReferenceScope());
        assertEquals("list_study_rooms", result.getTool());
    }

    @Test
    void reservationRulesShouldComeFromSharedProvider() {
        AgentToolExecuteRequest request = new AgentToolExecuteRequest();
        request.setTool("get_reservation_rules");
        when(reservationRulesProvider.getRules()).thenReturn(Map.of("maxAdvanceDays", 7));

        AgentToolExecutionResult result = agentToolService.execute(1L, request);

        assertEquals("1.0", result.getSchemaVersion());
        assertEquals(7, result.getData().get("maxAdvanceDays"));
    }

    @Test
    void myReservationsShouldReturnRedactedItemsAndReferences() {
        AgentToolExecuteRequest request = new AgentToolExecuteRequest();
        request.setTool("get_my_reservations");
        request.setArguments(Map.of("page", 1, "pageSize", 20));

        Order order = new Order();
        order.setOrderNo("ORDER-SECRET-001");
        order.setUserId(99L);
        order.setStatus("paid");
        order.setStudyRoomName("Quiet Room");
        order.setSeatPosition("A-1");
        order.setStartTime(LocalDateTime.of(2026, 6, 10, 9, 0));
        order.setEndTime(LocalDateTime.of(2026, 6, 10, 11, 0));
        order.setTotalAmount(BigDecimal.valueOf(12.34));

        when(orderService.getOrderList(99L, null, null, null, 1, 20)).thenReturn(Map.of(
                "list", List.of(order),
                "page", 1,
                "pageSize", 20,
                "total", 1
        ));

        AgentToolExecutionResult result = agentToolService.execute(99L, request);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) result.getData().get("items");
        Map<String, Object> safeOrder = items.get(0);

        assertEquals("ORDER_REF_1", safeOrder.get("reference"));
        assertFalse(safeOrder.containsKey("orderNo"));
        assertFalse(safeOrder.containsKey("userId"));
        assertFalse(safeOrder.containsKey("totalAmount"));
        assertTrue(((List<?>) safeOrder.get("sensitiveFieldsHidden")).contains("orderNo"));
        assertNotNull(result.getReferences().get("ORDER_REF_1"));
    }
}
