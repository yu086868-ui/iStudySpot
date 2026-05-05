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
        // 模拟座位查询
        com.ycyu.istudyspotbackend.entity.Seat seat = new com.ycyu.istudyspotbackend.entity.Seat();
        seat.setId(1L);
        seat.setPricePerHour(java.math.BigDecimal.valueOf(10.0));
        seat.setRowNum(1);
        seat.setColNum(1);
        seat.setSeatNumber("1-1");
        when(seatMapper.findById(1L)).thenReturn(seat);

        // 模拟自习室查询
        com.ycyu.istudyspotbackend.entity.StudyRoom studyRoom = new com.ycyu.istudyspotbackend.entity.StudyRoom();
        studyRoom.setId(1L);
        studyRoom.setName("Study Room 1");
        when(studyRoomMapper.findById(1L)).thenReturn(studyRoom);

        // 模拟时间冲突检查
        when(orderMapper.checkTimeConflict(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(0);
        when(orderMapper.insert(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L); // 设置ID
            return 1;
        });

        // 测试创建订单
        Map<String, Object> result = orderService.createOrder(1L, 1L, 1L, LocalDateTime.now(), LocalDateTime.now().plusHours(2), "normal");

        // 验证结果
        assertNotNull(result);

        // 验证方法调用
        verify(seatMapper, times(1)).findById(1L);
        verify(orderMapper, times(1)).checkTimeConflict(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(orderMapper, times(1)).insert(any(Order.class));
    }

    @Test
    void testCreateOrderSeatOccupied() {
        // 模拟座位查询
        com.ycyu.istudyspotbackend.entity.Seat seat = new com.ycyu.istudyspotbackend.entity.Seat();
        seat.setId(1L);
        seat.setPricePerHour(java.math.BigDecimal.valueOf(10.0));
        when(seatMapper.findById(1L)).thenReturn(seat);

        // 模拟时间冲突检查
        when(orderMapper.checkTimeConflict(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(1);

        // 测试创建订单
        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(1L, 1L, 1L, LocalDateTime.now(), LocalDateTime.now().plusHours(2), "normal");
        });

        assertEquals("该时段座位已被预订", exception.getMessage());

        // 验证方法调用
        verify(seatMapper, times(1)).findById(1L);
        verify(orderMapper, times(1)).checkTimeConflict(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(orderMapper, never()).insert(any(Order.class));
    }

    @Test
    void testMarkAsPaid() {
        // 模拟订单查询
        when(orderMapper.findById(1L)).thenReturn(testOrder);
        when(orderMapper.updateStatus(1L, "paid")).thenReturn(1);

        // 测试标记订单为已支付
        orderService.markAsPaid(1L);

        // 验证方法调用
        verify(orderMapper, times(1)).findById(1L);
        verify(orderMapper, times(1)).updateStatus(1L, "paid");
    }

    @Test
    void testMarkAsPaidNotFound() {
        // 模拟订单不存在
        when(orderMapper.findById(1L)).thenReturn(null);

        // 测试标记订单为已支付
        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.markAsPaid(1L);
        });

        assertEquals("订单不存在", exception.getMessage());

        // 验证方法调用
        verify(orderMapper, times(1)).findById(1L);
    }

    @Test
    void testCancelOrder() {
        // 模拟订单查询
        when(orderMapper.findById(1L)).thenReturn(testOrder);
        when(orderMapper.updateStatus(1L, "cancelled")).thenReturn(1);

        // 测试取消订单
        orderService.cancelOrder(1L);

        // 验证方法调用
        verify(orderMapper, times(1)).findById(1L);
        verify(orderMapper, times(1)).updateStatus(1L, "cancelled");
    }

    @Test
    void testGetOrderList() {
        // 模拟订单列表查询
        List<Order> orders = new ArrayList<>();
        orders.add(testOrder);
        when(orderMapper.findByUserId(1L)).thenReturn(orders);

        // 测试获取订单列表
        Map<String, Object> result = orderService.getOrderList(1L, null, null, null, 1, 20);

        // 验证结果
        assertNotNull(result);

        // 验证方法调用
        verify(orderMapper, times(1)).findByUserId(1L);
    }

    @Test
    void testGetOrderListWithStatus() {
        // 模拟订单列表查询
        List<Order> orders = new ArrayList<>();
        orders.add(testOrder);
        when(orderMapper.findByUserIdAndStatus(1L, "pending")).thenReturn(orders);

        // 测试获取订单列表
        Map<String, Object> result = orderService.getOrderList(1L, "pending", null, null, 1, 20);

        // 验证结果
        assertNotNull(result);

        // 验证方法调用
        verify(orderMapper, times(1)).findByUserIdAndStatus(1L, "pending");
    }

    @Test
    void testCheckin() {
        // 模拟订单查询
        testOrder.setStatus("2");
        when(orderMapper.findById(1L)).thenReturn(testOrder);
        when(orderMapper.checkin(1L)).thenReturn(1);

        // 测试签到
        Map<String, Object> result = orderService.checkin(1L, "test-code");

        // 验证结果
        assertNotNull(result);
        assertEquals("1", result.get("id"));
        assertEquals("in_use", result.get("status"));

        // 验证方法调用
        verify(orderMapper, times(1)).findById(1L);
        verify(orderMapper, times(1)).checkin(1L);
    }

    @Test
    void testCheckinOrderNotFound() {
        // 模拟订单不存在
        when(orderMapper.findById(1L)).thenReturn(null);

        // 测试签到
        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.checkin(1L, "test-code");
        });

        assertEquals("订单不存在", exception.getMessage());

        // 验证方法调用
        verify(orderMapper, times(1)).findById(1L);
    }

    @Test
    void testCheckinOrderStatusIncorrect() {
        // 模拟订单状态不正确
        testOrder.setStatus("pending");
        when(orderMapper.findById(1L)).thenReturn(testOrder);

        // 测试签到
        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.checkin(1L, "test-code");
        });

        assertEquals("订单状态不正确，无法签到", exception.getMessage());

        // 验证方法调用
        verify(orderMapper, times(1)).findById(1L);
    }

    @Test
    void testCheckout() {
        // 模拟订单查询
        testOrder.setStatus("in_use");
        testOrder.setCheckinTime(LocalDateTime.now().minusHours(1));
        when(orderMapper.findById(1L)).thenReturn(testOrder);
        when(orderMapper.checkout(eq(1L), anyInt(), any(BigDecimal.class))).thenReturn(1);

        // 测试签退
        Map<String, Object> result = orderService.checkout(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals("1", result.get("id"));
        assertEquals("completed", result.get("status"));

        // 验证方法调用
        verify(orderMapper, times(1)).findById(1L);
        verify(orderMapper, times(1)).checkout(eq(1L), anyInt(), any(BigDecimal.class));
    }

    @Test
    void testCheckoutOrderNotFound() {
        // 模拟订单不存在
        when(orderMapper.findById(1L)).thenReturn(null);

        // 测试签退
        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.checkout(1L);
        });

        assertEquals("订单不存在", exception.getMessage());

        // 验证方法调用
        verify(orderMapper, times(1)).findById(1L);
    }

    @Test
    void testCheckoutOrderStatusIncorrect() {
        // 模拟订单状态不正确
        testOrder.setStatus("pending");
        when(orderMapper.findById(1L)).thenReturn(testOrder);

        // 测试签退
        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.checkout(1L);
        });

        assertTrue(exception.getMessage().contains("订单未在使用中"));

        // 验证方法调用
        verify(orderMapper, times(1)).findById(1L);
    }

    @Test
    void testRenew() {
        // 模拟订单查询
        testOrder.setStatus("3");
        testOrder.setEndTime(LocalDateTime.now());
        testOrder.setSeatId(1L);
        testOrder.setTotalPrice(BigDecimal.valueOf(10.0));
        testOrder.setTotalAmount(BigDecimal.valueOf(10.0));
        when(orderMapper.findById(1L)).thenReturn(testOrder);

        // 模拟座位查询
        Seat seat = new Seat();
        seat.setPricePerHour(BigDecimal.valueOf(10.0));
        when(seatMapper.findById(1L)).thenReturn(seat);

        when(orderMapper.updateStatus(1L, "3")).thenReturn(1);

        // 测试续费
        LocalDateTime newEndTime = LocalDateTime.now().plusHours(1);
        Map<String, Object> result = orderService.renew(1L, newEndTime);

        // 验证结果
        assertNotNull(result);
        assertEquals("1", result.get("orderId"));

        // 验证方法调用
        verify(orderMapper, times(1)).findById(1L);
        verify(seatMapper, times(1)).findById(1L);
        verify(orderMapper, times(1)).updateStatus(1L, "3");
    }

    @Test
    void testRenewOrderNotFound() {
        // 模拟订单不存在
        when(orderMapper.findById(1L)).thenReturn(null);

        // 测试续费
        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.renew(1L, LocalDateTime.now().plusHours(1));
        });

        assertEquals("订单不存在", exception.getMessage());

        // 验证方法调用
        verify(orderMapper, times(1)).findById(1L);
    }

    @Test
    void testRenewOrderStatusIncorrect() {
        // 模拟订单状态不正确
        testOrder.setStatus("pending");
        when(orderMapper.findById(1L)).thenReturn(testOrder);

        // 测试续费
        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.renew(1L, LocalDateTime.now().plusHours(1));
        });

        assertEquals("订单未在使用中", exception.getMessage());

        // 验证方法调用
        verify(orderMapper, times(1)).findById(1L);
    }

    @Test
    void testRenewEndTimeBeforeOriginal() {
        // 模拟订单查询
        testOrder.setStatus("3");
        testOrder.setEndTime(LocalDateTime.now().plusHours(1));
        when(orderMapper.findById(1L)).thenReturn(testOrder);

        // 测试续费
        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.renew(1L, LocalDateTime.now());
        });

        assertEquals("新结束时间必须晚于原结束时间", exception.getMessage());

        // 验证方法调用
        verify(orderMapper, times(1)).findById(1L);
    }

    @Test
    void testGetOrderDetail() {
        // 模拟订单查询
        when(orderMapper.findById(1L)).thenReturn(testOrder);

        // 测试获取订单详情
        Order order = orderService.getOrderDetail(1L);

        // 验证结果
        assertNotNull(order);
        assertEquals(1L, order.getId());

        // 验证方法调用
        verify(orderMapper, times(1)).findById(1L);
    }
}
