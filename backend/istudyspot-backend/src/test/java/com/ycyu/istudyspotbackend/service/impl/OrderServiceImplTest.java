package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.Order;
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
