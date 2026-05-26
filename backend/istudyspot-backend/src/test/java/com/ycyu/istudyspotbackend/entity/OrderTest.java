package com.ycyu.istudyspotbackend.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class OrderTest {

    @Test
    void testGettersAndSetters() {
        Order order = new Order();

        order.setId(1L);
        assertEquals(1L, order.getId());

        order.setOrderNo("ORD123");
        assertEquals("ORD123", order.getOrderNo());

        order.setUserId(2L);
        assertEquals(2L, order.getUserId());

        order.setSeatId(3L);
        assertEquals(3L, order.getSeatId());

        order.setRoomId(4L);
        assertEquals(4L, order.getRoomId());

        order.setStudyRoomName("自习室1");
        assertEquals("自习室1", order.getStudyRoomName());

        order.setRoomName("Room1");
        assertEquals("Room1", order.getRoomName());

        order.setSeatPosition("A1");
        assertEquals("A1", order.getSeatPosition());

        order.setSeatNumber("1");
        assertEquals("1", order.getSeatNumber());

        order.setTotalAmount(BigDecimal.valueOf(100));
        assertEquals(BigDecimal.valueOf(100), order.getTotalAmount());

        order.setTotalPrice(BigDecimal.valueOf(50));
        assertEquals(BigDecimal.valueOf(50), order.getTotalPrice());

        order.setStatus("pending");
        assertEquals("pending", order.getStatus());

        LocalDateTime now = LocalDateTime.now();
        order.setCheckinTime(now);
        assertEquals(now, order.getCheckinTime());

        order.setCheckoutTime(now);
        assertEquals(now, order.getCheckoutTime());

        order.setActualDuration(120);
        assertEquals(120, order.getActualDuration());

        order.setActualPrice(BigDecimal.valueOf(20));
        assertEquals(BigDecimal.valueOf(20), order.getActualPrice());

        order.setCreatedAt(now);
        assertEquals(now, order.getCreatedAt());

        order.setUpdatedAt(now);
        assertEquals(now, order.getUpdatedAt());
    }

    @Test
    void testStartTimeJsonProperty() {
        Order order = new Order();
        LocalDateTime startTime = LocalDateTime.of(2026, 5, 25, 10, 0);
        order.setStartTime(startTime);

        assertEquals(startTime, order.getStartTime());
        assertEquals(startTime, order.getPlanStartTime());
    }

    @Test
    void testEndTimeJsonProperty() {
        Order order = new Order();
        LocalDateTime endTime = LocalDateTime.of(2026, 5, 25, 12, 0);
        order.setEndTime(endTime);

        assertEquals(endTime, order.getEndTime());
        assertEquals(endTime, order.getPlanEndTime());
    }

    @Test
    void testPlanStartTimeAndEndTime() {
        Order order = new Order();
        LocalDateTime planStart = LocalDateTime.of(2026, 5, 25, 10, 0);
        LocalDateTime planEnd = LocalDateTime.of(2026, 5, 25, 12, 0);

        order.setPlanStartTime(planStart);
        order.setPlanEndTime(planEnd);

        assertEquals(planStart, order.getPlanStartTime());
        assertEquals(planEnd, order.getPlanEndTime());
        assertEquals(planStart, order.getStartTime());
        assertEquals(planEnd, order.getEndTime());
    }

    @Test
    void testActualStartTimeAndEndTime() {
        Order order = new Order();
        LocalDateTime actualStart = LocalDateTime.of(2026, 5, 25, 10, 5);
        LocalDateTime actualEnd = LocalDateTime.of(2026, 5, 25, 11, 55);

        order.setActualStartTime(actualStart);
        order.setActualEndTime(actualEnd);

        assertEquals(actualStart, order.getActualStartTime());
        assertEquals(actualEnd, order.getActualEndTime());
    }

    @Test
    void testNullValues() {
        Order order = new Order();
        assertNull(order.getId());
        assertNull(order.getOrderNo());
        assertNull(order.getUserId());
        assertNull(order.getSeatId());
        assertNull(order.getRoomId());
        assertNull(order.getStudyRoomName());
        assertNull(order.getRoomName());
        assertNull(order.getSeatPosition());
        assertNull(order.getSeatNumber());
        assertNull(order.getPlanStartTime());
        assertNull(order.getPlanEndTime());
        assertNull(order.getActualStartTime());
        assertNull(order.getActualEndTime());
        assertNull(order.getTotalAmount());
        assertNull(order.getTotalPrice());
        assertNull(order.getStatus());
        assertNull(order.getCheckinTime());
        assertNull(order.getCheckoutTime());
        assertNull(order.getActualDuration());
        assertNull(order.getActualPrice());
        assertNull(order.getCreatedAt());
        assertNull(order.getUpdatedAt());
    }
}
