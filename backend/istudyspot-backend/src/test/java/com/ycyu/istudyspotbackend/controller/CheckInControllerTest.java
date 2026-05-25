package com.ycyu.istudyspotbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ycyu.istudyspotbackend.entity.Order;
import com.ycyu.istudyspotbackend.mapper.OrderMapper;
import com.ycyu.istudyspotbackend.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class CheckInControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private CheckInController checkInController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(checkInController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCheckInSuccess() throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("id", "1");
        result.put("status", "in_use");
        when(orderService.checkin(1L, "A1")).thenReturn(result);

        mockMvc.perform(post("/api/checkin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("reservationId", "1", "seatId", "A1")))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("签到成功"));

        verify(orderService, times(1)).checkin(1L, "A1");
    }

    @Test
    void testCheckInWithNullReservationId() throws Exception {
        mockMvc.perform(post("/api/checkin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("seatId", "A1")))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("预约ID不能为空"));

        verify(orderService, never()).checkin(anyLong(), anyString());
    }

    @Test
    void testCheckInWithEmptyReservationId() throws Exception {
        mockMvc.perform(post("/api/checkin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("reservationId", "", "seatId", "A1")))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("预约ID不能为空"));
    }

    @Test
    void testCheckInWithNullSeatId() throws Exception {
        mockMvc.perform(post("/api/checkin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("reservationId", "1")))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("座位ID不能为空"));
    }

    @Test
    void testCheckInWithEmptySeatId() throws Exception {
        mockMvc.perform(post("/api/checkin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("reservationId", "1", "seatId", "")))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("座位ID不能为空"));
    }

    @Test
    void testCheckInWithInvalidReservationId() throws Exception {
        mockMvc.perform(post("/api/checkin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("reservationId", "abc", "seatId", "A1")))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("预约ID格式错误"));
    }

    @Test
    void testCheckInWithServiceException() throws Exception {
        when(orderService.checkin(1L, "A1")).thenThrow(new RuntimeException("订单状态不正确"));

        mockMvc.perform(post("/api/checkin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("reservationId", "1", "seatId", "A1")))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("订单状态不正确"));
    }

    @Test
    void testCheckoutSuccess() throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("id", "1");
        result.put("status", "completed");
        when(orderService.checkout(1L)).thenReturn(result);

        mockMvc.perform(post("/api/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("checkInRecordId", "1")))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("签退成功"));

        verify(orderService, times(1)).checkout(1L);
    }

    @Test
    void testCheckoutWithNullCheckInRecordId() throws Exception {
        mockMvc.perform(post("/api/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("签到记录ID不能为空"));
    }

    @Test
    void testCheckoutWithEmptyCheckInRecordId() throws Exception {
        mockMvc.perform(post("/api/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("checkInRecordId", "")))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("签到记录ID不能为空"));
    }

    @Test
    void testCheckoutWithInvalidCheckInRecordId() throws Exception {
        mockMvc.perform(post("/api/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("checkInRecordId", "abc")))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("签到记录ID格式错误"));
    }

    @Test
    void testCheckoutWithServiceException() throws Exception {
        when(orderService.checkout(1L)).thenThrow(new RuntimeException("订单未在使用中"));

        mockMvc.perform(post("/api/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("checkInRecordId", "1")))
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("订单未在使用中"));
    }

    @Test
    void testGetCheckInRecordsEmpty() throws Exception {
        when(orderMapper.findCheckinRecordsByUserId(1L)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/checkin/records")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalHours").value(0))
                .andExpect(jsonPath("$.data.weekHours").value(0))
                .andExpect(jsonPath("$.data.monthHours").value(0))
                .andExpect(jsonPath("$.data.streak").value(0))
                .andExpect(jsonPath("$.data.favoriteSeat").value(""))
                .andExpect(jsonPath("$.data.peakTime").value(""))
                .andExpect(jsonPath("$.data.records").isEmpty());
    }

    @Test
    void testGetCheckInRecordsWithData() throws Exception {
        List<Order> records = new ArrayList<>();
        Order order1 = new Order();
        order1.setId(1L);
        order1.setSeatPosition("A1");
        order1.setStudyRoomName("自习室1");
        order1.setPlanStartTime(LocalDateTime.now().minusHours(2));
        order1.setPlanEndTime(LocalDateTime.now());
        order1.setActualStartTime(LocalDateTime.now().minusHours(2));
        order1.setActualEndTime(LocalDateTime.now());
        order1.setStatus("completed");
        records.add(order1);

        Order order2 = new Order();
        order2.setId(2L);
        order2.setSeatPosition("B2");
        order2.setStudyRoomName("自习室2");
        order2.setPlanStartTime(LocalDateTime.now().minusHours(4));
        order2.setPlanEndTime(LocalDateTime.now().minusHours(2));
        order2.setStatus("completed");
        records.add(order2);

        when(orderMapper.findCheckinRecordsByUserId(1L)).thenReturn(records);

        mockMvc.perform(get("/api/checkin/records")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.streak").value(2))
                .andExpect(jsonPath("$.data.favoriteSeat").value("A1"))
                .andExpect(jsonPath("$.data.records").isNotEmpty());
    }

    @Test
    void testGetCheckInRecordsWithNullTimes() throws Exception {
        List<Order> records = new ArrayList<>();
        Order order = new Order();
        order.setId(1L);
        order.setSeatPosition("A1");
        order.setPlanStartTime(LocalDateTime.now().minusHours(2));
        order.setPlanEndTime(LocalDateTime.now());
        order.setActualStartTime(null);
        order.setActualEndTime(null);
        order.setStatus("completed");
        records.add(order);

        when(orderMapper.findCheckinRecordsByUserId(1L)).thenReturn(records);

        mockMvc.perform(get("/api/checkin/records")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void testGetCheckInRecordsWithAllNullTimes() throws Exception {
        List<Order> records = new ArrayList<>();
        Order order = new Order();
        order.setId(1L);
        order.setSeatPosition("A1");
        order.setPlanStartTime(null);
        order.setPlanEndTime(null);
        order.setStatus("completed");
        records.add(order);

        when(orderMapper.findCheckinRecordsByUserId(1L)).thenReturn(records);

        mockMvc.perform(get("/api/checkin/records")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalHours").value(0));
    }

    @Test
    void testGetCheckInRecordsWithNullSeatPosition() throws Exception {
        List<Order> records = new ArrayList<>();
        Order order = new Order();
        order.setId(1L);
        order.setSeatPosition(null);
        order.setPlanStartTime(LocalDateTime.now().minusHours(2));
        order.setPlanEndTime(LocalDateTime.now());
        order.setStatus("completed");
        records.add(order);

        when(orderMapper.findCheckinRecordsByUserId(1L)).thenReturn(records);

        mockMvc.perform(get("/api/checkin/records")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.favoriteSeat").value("unknown"));
    }

    @Test
    void testGetCheckInRecordsWithPagination() throws Exception {
        List<Order> records = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            Order order = new Order();
            order.setId((long) i);
            order.setSeatPosition("A" + i);
            order.setPlanStartTime(LocalDateTime.now().minusHours(2));
            order.setPlanEndTime(LocalDateTime.now());
            order.setStatus("completed");
            records.add(order);
        }

        when(orderMapper.findCheckinRecordsByUserId(1L)).thenReturn(records);

        mockMvc.perform(get("/api/checkin/records")
                .param("page", "2")
                .param("pageSize", "10")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.page").value(2))
                .andExpect(jsonPath("$.data.pageSize").value(10));
    }

    @Test
    void testGetCheckInRecordsWithStreakBreak() throws Exception {
        List<Order> records = new ArrayList<>();
        Order order1 = new Order();
        order1.setId(1L);
        order1.setSeatPosition("A1");
        order1.setPlanStartTime(LocalDateTime.now().minusDays(3).minusHours(2));
        order1.setPlanEndTime(LocalDateTime.now().minusDays(3));
        order1.setStatus("completed");
        records.add(order1);

        Order order2 = new Order();
        order2.setId(2L);
        order2.setSeatPosition("A1");
        order2.setPlanStartTime(LocalDateTime.now().minusHours(2));
        order2.setPlanEndTime(LocalDateTime.now());
        order2.setStatus("completed");
        records.add(order2);

        when(orderMapper.findCheckinRecordsByUserId(1L)).thenReturn(records);

        mockMvc.perform(get("/api/checkin/records")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.streak").value(1));
    }

    @Test
    void testGetCheckInRecordsWithDateParams() throws Exception {
        when(orderMapper.findCheckinRecordsByUserId(1L)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/checkin/records")
                .param("startDate", "2026-05-01")
                .param("endDate", "2026-05-25")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testGetCurrentCheckInStatusCheckedIn() throws Exception {
        Order currentOrder = new Order();
        currentOrder.setId(1L);
        currentOrder.setSeatPosition("A1");
        currentOrder.setStudyRoomName("自习室1");
        currentOrder.setPlanStartTime(LocalDateTime.now().minusHours(1));
        currentOrder.setPlanEndTime(LocalDateTime.now().plusHours(1));
        currentOrder.setStatus("in_use");
        when(orderMapper.findCurrentCheckinByUserId(1L)).thenReturn(currentOrder);

        mockMvc.perform(get("/api/checkin/current")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.isCheckedIn").value(true))
                .andExpect(jsonPath("$.data.checkInRecord.id").value(1))
                .andExpect(jsonPath("$.data.checkInRecord.seatPosition").value("A1"))
                .andExpect(jsonPath("$.data.checkInRecord.studyRoomName").value("自习室1"))
                .andExpect(jsonPath("$.data.checkInRecord.status").value("in_use"));
    }

    @Test
    void testGetCurrentCheckInStatusNotCheckedIn() throws Exception {
        when(orderMapper.findCurrentCheckinByUserId(1L)).thenReturn(null);

        mockMvc.perform(get("/api/checkin/current")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.isCheckedIn").value(false))
                .andExpect(jsonPath("$.data.checkInRecord").isEmpty());
    }

    @Test
    void testGetCurrentCheckInStatusWithNullTimes() throws Exception {
        Order currentOrder = new Order();
        currentOrder.setId(1L);
        currentOrder.setSeatPosition("A1");
        currentOrder.setStudyRoomName("自习室1");
        currentOrder.setPlanStartTime(null);
        currentOrder.setPlanEndTime(null);
        currentOrder.setStatus("in_use");
        when(orderMapper.findCurrentCheckinByUserId(1L)).thenReturn(currentOrder);

        mockMvc.perform(get("/api/checkin/current")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isCheckedIn").value(true))
                .andExpect(jsonPath("$.data.checkInRecord.startTime").isEmpty())
                .andExpect(jsonPath("$.data.checkInRecord.endTime").isEmpty());
    }

    @Test
    void testGetCheckInRecordsWithConsecutiveDays() throws Exception {
        List<Order> records = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Order order = new Order();
            order.setId((long) i);
            order.setSeatPosition("A1");
            order.setPlanStartTime(LocalDateTime.now().minusDays(2 - i).minusHours(2));
            order.setPlanEndTime(LocalDateTime.now().minusDays(2 - i));
            order.setStatus("completed");
            records.add(order);
        }

        when(orderMapper.findCheckinRecordsByUserId(1L)).thenReturn(records);

        mockMvc.perform(get("/api/checkin/records")
                .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.streak").value(3));
    }
}
