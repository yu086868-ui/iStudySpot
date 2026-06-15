package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.agent.tool.ReservationRulesProvider;
import com.ycyu.istudyspotbackend.dto.BookingDTO;
import com.ycyu.istudyspotbackend.entity.Order;
import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.service.OrderService;
import com.ycyu.istudyspotbackend.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ReservationRulesProvider reservationRulesProvider;

    @PostMapping
    public Result<Map<String, Object>> createReservation(
            @RequestBody BookingDTO bookingDTO,
            @RequestAttribute Long userId) {
        Map<String, Object> result = orderService.createOrder(
                userId,
                bookingDTO.getStudyRoomId(),
                bookingDTO.getSeatId(),
                bookingDTO.getStartTime(),
                bookingDTO.getEndTime(),
                bookingDTO.getBookingType()
        );
        return Result.success("预约成功", result);
    }

    @GetMapping("/my")
    public Result<Map<String, Object>> getMyReservations(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestAttribute Long userId) {
        Map<String, Object> result = orderService.getOrderList(userId, status, startDate, endDate, page, pageSize);
        return Result.success("success", result);
    }

    @GetMapping("/{id}")
    public Result<Order> getReservationDetail(@PathVariable Long id) {
        Order order = orderService.getOrderDetail(id);
        return Result.success("success", order);
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancelReservation(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return Result.success("预约已取消", null);
    }

    @PostMapping("/{id}/pay")
    public Result<Map<String, Object>> payReservation(
            @PathVariable Long id,
            @RequestAttribute Long userId) {
        try {
            Order order = orderService.getOrderDetail(id);
            if (!"pending".equals(order.getStatus())) {
                return Result.error("订单状态不正确，无法支付");
            }
            Map<String, Object> paymentResult = paymentService.createPayment(
                    userId,
                    id,
                    order.getTotalPrice(),
                    "balance"
            );
            return Result.success("支付成功", paymentResult);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/renew")
    public Result<Map<String, Object>> renewReservation(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            String newEndTimeStr = body.get("newEndTime");
            if (newEndTimeStr == null || newEndTimeStr.isEmpty()) {
                return Result.error("新结束时间不能为空");
            }
            java.time.LocalDateTime newEndTime = java.time.LocalDateTime.parse(newEndTimeStr,
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            Map<String, Object> result = orderService.renew(id, newEndTime);
            return Result.success("续时成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/rules")
    public Result<Map<String, Object>> getReservationRules() {
        Map<String, Object> rules = reservationRulesProvider.getRules();
        return Result.success("success", rules);
    }
}
