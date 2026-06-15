package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.Order;
import com.ycyu.istudyspotbackend.entity.Seat;
import com.ycyu.istudyspotbackend.mapper.OrderMapper;
import com.ycyu.istudyspotbackend.mapper.SeatMapper;
import com.ycyu.istudyspotbackend.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private SeatMapper seatMapper;

    @Mock
    private com.ycyu.istudyspotbackend.mapper.StudyRoomMapper studyRoomMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUserId(1L);
        testOrder.setSeatId(1L);
        testOrder.setRoomId(1L);
        testOrder.setOrderNo("ORD20260425001");
        testOrder.setStatus("pending");
        testOrder.setStartTime(LocalDateTime.now());
        testOrder.setEndTime(LocalDateTime.now().plusHours(2));
        testOrder.setCheckinTime(null);
        testOrder.setCheckoutTime(null);
        testOrder.setTotalPrice(java.math.BigDecimal.valueOf(10.0));
        testOrder.setTotalAmount(java.math.BigDecimal.valueOf(10.0));
        testOrder.setCreatedAt(LocalDateTime.now());
        testOrder.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateOrder() {
        com.ycyu.istudyspotbackend.entity.Seat seat = new com.ycyu.istudyspotbackend.entity.Seat();
        seat.setId(1L);
        seat.setPricePerHour(java.math.BigDecimal.valueOf(10.0));
        seat.setRowNum(1);
        seat.setColNum(1);
        seat.setSeatNumber("1-1");
        when(seatMapper.findById(1L)).thenReturn(seat);

        com.ycyu.istudyspotbackend.entity.StudyRoom studyRoom = new com.ycyu.istudyspotbackend.entity.StudyRoom();
        studyRoom.setId(1L);
        studyRoom.setName("Study Room 1");
        when(studyRoomMapper.findById(1L)).thenReturn(studyRoom);

        when(orderMapper.checkTimeConflict(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(0);
        when(orderMapper.insert(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return 1;
        });

        Map<String, Object> result = orderService.createOrder(1L, 1L, 1L, LocalDateTime.now(), LocalDateTime.now().plusHours(2), "normal");

        assertNotNull(result);

        verify(seatMapper, times(1)).findById(1L);
        verify(orderMapper, times(1)).checkTimeConflict(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(orderMapper, times(1)).insert(any(Order.class));
    }

    @Test
    void testCreateOrderSeatOccupied() {
        com.ycyu.istudyspotbackend.entity.Seat seat = new com.ycyu.istudyspotbackend.entity.Seat();
        seat.setId(1L);
        seat.setPricePerHour(java.math.BigDecimal.valueOf(10.0));
        when(seatMapper.findById(1L)).thenReturn(seat);

        when(orderMapper.checkTimeConflict(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(1);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(1L, 1L, 1L, LocalDateTime.now(), LocalDateTime.now().plusHours(2), "normal");
        });

        assertEquals("该时段座位已被预订", exception.getMessage());

        verify(seatMapper, times(1)).findById(1L);
        verify(orderMapper, times(1)).checkTimeConflict(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(orderMapper, never()).insert(any(Order.class));
    }

    @Test
    void testMarkAsPaid() {
        when(orderMapper.findById(1L)).thenReturn(testOrder);
        when(orderMapper.updateStatus(1L, "paid")).thenReturn(1);

        orderService.markAsPaid(1L);

        verify(orderMapper, times(1)).findById(1L);
        verify(orderMapper, times(1)).updateStatus(1L, "paid");
    }

    @Test
    void testMarkAsPaidNotFound() {
        when(orderMapper.findById(1L)).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.markAsPaid(1L);
        });

        assertEquals("订单不存在", exception.getMessage());

        verify(orderMapper, times(1)).findById(1L);
    }

    @Test
    void testCancelOrder() {
        when(orderMapper.findById(1L)).thenReturn(testOrder);
        when(orderMapper.updateStatus(1L, "cancelled")).thenReturn(1);

        orderService.cancelOrder(1L);

        verify(orderMapper, times(1)).findById(1L);
        verify(orderMapper, times(1)).updateStatus(1L, "cancelled");
    }

    @Test
    void testGetOrderList() {
        List<Order> orders = new ArrayList<>();
        orders.add(testOrder);
        when(orderMapper.findByUserId(1L)).thenReturn(orders);

        Map<String, Object> result = orderService.getOrderList(1L, null, null, null, 1, 20);

        assertNotNull(result);

        verify(orderMapper, times(1)).findByUserId(1L);
    }

    @Test
    void testGetOrderListWithStatus() {
        List<Order> orders = new ArrayList<>();
        orders.add(testOrder);
        when(orderMapper.findByUserIdAndStatus(1L, "pending")).thenReturn(orders);

        Map<String, Object> result = orderService.getOrderList(1L, "pending", null, null, 1, 20);

        assertNotNull(result);

        verify(orderMapper, times(1)).findByUserIdAndStatus(1L, "pending");
    }

    @Test
    void testCheckin() {
        testOrder.setStatus("paid");
        when(orderMapper.findById(1L)).thenReturn(testOrder);
        when(orderMapper.checkin(1L)).thenReturn(1);

        Map<String, Object> result = orderService.checkin(1L, "test-code");

        assertNotNull(result);
        assertEquals("1", result.get("id"));
        assertEquals("in_use", result.get("status"));

        verify(orderMapper, times(1)).findById(1L);
        verify(orderMapper, times(1)).checkin(1L);
    }

    @Test
    void testCheckinOrderNotFound() {
        when(orderMapper.findById(1L)).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.checkin(1L, "test-code");
        });

        assertEquals("订单不存在", exception.getMessage());

        verify(orderMapper, times(1)).findById(1L);
    }

    @Test
    void testCheckinOrderStatusIncorrect() {
        testOrder.setStatus("pending");
        when(orderMapper.findById(1L)).thenReturn(testOrder);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.checkin(1L, "test-code");
        });

        assertTrue(exception.getMessage().contains("订单状态不正确，无法签到"));

        verify(orderMapper, times(1)).findById(1L);
    }

    @Test
    void testCheckout() {
        testOrder.setStatus("in_use");
        testOrder.setCheckinTime(LocalDateTime.now().minusHours(1));
        when(orderMapper.findById(1L)).thenReturn(testOrder);
        when(orderMapper.checkout(eq(1L), anyInt(), any(BigDecimal.class))).thenReturn(1);

        Map<String, Object> result = orderService.checkout(1L);

        assertNotNull(result);
        assertEquals("1", result.get("id"));
        assertEquals("completed", result.get("status"));

        verify(orderMapper, times(1)).findById(1L);
        verify(orderMapper, times(1)).checkout(eq(1L), anyInt(), any(BigDecimal.class));
    }

    @Test
    void testCheckoutOrderNotFound() {
        when(orderMapper.findById(1L)).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.checkout(1L);
        });

        assertEquals("订单不存在", exception.getMessage());

        verify(orderMapper, times(1)).findById(1L);
    }

    @Test
    void testCheckoutOrderStatusIncorrect() {
        testOrder.setStatus("pending");
        when(orderMapper.findById(1L)).thenReturn(testOrder);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.checkout(1L);
        });

        assertTrue(exception.getMessage().contains("订单未在使用中"));

        verify(orderMapper, times(1)).findById(1L);
    }

    @Test
    void testRenew() {
        testOrder.setStatus("in_use");
        testOrder.setEndTime(LocalDateTime.now());
        testOrder.setSeatId(1L);
        testOrder.setTotalPrice(BigDecimal.valueOf(10.0));
        testOrder.setTotalAmount(BigDecimal.valueOf(10.0));
        when(orderMapper.findById(1L)).thenReturn(testOrder);

        Seat seat = new Seat();
        seat.setPricePerHour(BigDecimal.valueOf(10.0));
        when(seatMapper.findById(1L)).thenReturn(seat);

        when(orderMapper.updateRenew(eq(1L), any(LocalDateTime.class), any(BigDecimal.class), any(BigDecimal.class))).thenReturn(1);

        LocalDateTime newEndTime = LocalDateTime.now().plusHours(1);
        Map<String, Object> result = orderService.renew(1L, newEndTime);

        assertNotNull(result);
        assertEquals("1", result.get("orderId"));
        assertNotNull(result.get("additionalAmount"));
        assertNotNull(result.get("newEndTime"));

        verify(orderMapper, times(1)).findById(1L);
        verify(seatMapper, times(1)).findById(1L);
        verify(orderMapper, times(1)).updateRenew(eq(1L), any(LocalDateTime.class), any(BigDecimal.class), any(BigDecimal.class));
    }

    @Test
    void testRenewOrderNotFound() {
        when(orderMapper.findById(1L)).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.renew(1L, LocalDateTime.now().plusHours(1));
        });

        assertEquals("订单不存在", exception.getMessage());

        verify(orderMapper, times(1)).findById(1L);
    }

    @Test
    void testRenewOrderStatusIncorrect() {
        testOrder.setStatus("pending");
        when(orderMapper.findById(1L)).thenReturn(testOrder);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.renew(1L, LocalDateTime.now().plusHours(1));
        });

        assertEquals("订单未在使用中", exception.getMessage());

        verify(orderMapper, times(1)).findById(1L);
    }

    @Test
    void testRenewEndTimeBeforeOriginal() {
        testOrder.setStatus("in_use");
        testOrder.setEndTime(LocalDateTime.now().plusHours(1));
        when(orderMapper.findById(1L)).thenReturn(testOrder);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.renew(1L, LocalDateTime.now());
        });

        assertEquals("新结束时间必须晚于原结束时间", exception.getMessage());

        verify(orderMapper, times(1)).findById(1L);
    }

    @Test
    void testGetOrderDetail() {
        when(orderMapper.findById(1L)).thenReturn(testOrder);

        Order order = orderService.getOrderDetail(1L);

        assertNotNull(order);
        assertEquals(1L, order.getId());

        verify(orderMapper, times(1)).findById(1L);
    }

    @Test
    void testCancelOrderWithPaidStatus() {
        testOrder.setStatus("paid");
        when(orderMapper.findById(1L)).thenReturn(testOrder);
        when(orderMapper.updateStatus(1L, "cancelled")).thenReturn(1);

        orderService.cancelOrder(1L);

        verify(orderMapper, times(1)).updateStatus(1L, "cancelled");
    }

    @Test
    void testCancelOrderWithInvalidStatus() {
        testOrder.setStatus("in_use");
        when(orderMapper.findById(1L)).thenReturn(testOrder);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.cancelOrder(1L);
        });

        assertEquals("当前状态无法取消", exception.getMessage());
    }

    @Test
    void testCheckoutWithNullCheckinTime() {
        testOrder.setStatus("in_use");
        testOrder.setActualStartTime(null);
        testOrder.setCheckinTime(null);
        when(orderMapper.findById(1L)).thenReturn(testOrder);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.checkout(1L);
        });

        assertEquals("签到时间不存在", exception.getMessage());
    }

    @Test
    void testCheckoutWithActualStartTime() {
        testOrder.setStatus("in_use");
        testOrder.setActualStartTime(LocalDateTime.now().minusHours(1));
        testOrder.setCheckinTime(null);
        testOrder.setTotalPrice(BigDecimal.valueOf(10.0));
        when(orderMapper.findById(1L)).thenReturn(testOrder);
        when(orderMapper.checkout(eq(1L), anyInt(), any(BigDecimal.class))).thenReturn(1);

        Map<String, Object> result = orderService.checkout(1L);

        assertNotNull(result);
        assertEquals("completed", result.get("status"));
    }

    @Test
    void testMarkAsPaidWithInvalidStatus() {
        testOrder.setStatus("paid");
        when(orderMapper.findById(1L)).thenReturn(testOrder);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.markAsPaid(1L);
        });

        assertEquals("订单状态不正确，无法支付", exception.getMessage());
    }

    @Test
    void testCreateOrderSeatNotFound() {
        when(seatMapper.findById(999L)).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(1L, 1L, 999L, LocalDateTime.now(), LocalDateTime.now().plusHours(2), "normal");
        });

        assertEquals("座位不存在", exception.getMessage());
    }
}
