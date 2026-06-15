package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.agent.tool.ReservationRulesProvider;
import com.ycyu.istudyspotbackend.dto.WxBookingDTO;
import com.ycyu.istudyspotbackend.dto.WxOrderDTO;
import com.ycyu.istudyspotbackend.entity.Order;
import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wx/reservations")
public class WxOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ReservationRulesProvider reservationRulesProvider;

    @PostMapping
    public Result<Map<String, Object>> createReservation(
            @RequestBody WxBookingDTO bookingDTO,
            @RequestAttribute Long userId) {
        Map<String, Object> result = orderService.createOrder(
                userId,
                Long.valueOf(bookingDTO.getStudyRoomId()),
                Long.valueOf(bookingDTO.getSeatId()),
                bookingDTO.getParsedStartTime(),
                bookingDTO.getParsedEndTime(),
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
        // 将 Order 列表转换为 WxOrderDTO 列表
        Object listObj = result.get("list");
        if (listObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Order> orders = (List<Order>) listObj;
            result.put("list", orders.stream().map(WxOrderDTO::fromEntity).collect(Collectors.toList()));
        }
        return Result.success("success", result);
    }

    @GetMapping("/{id}")
    public Result<WxOrderDTO> getReservationDetail(@PathVariable Long id) {
        Order order = orderService.getOrderDetail(id);
        return Result.success("success", WxOrderDTO.fromEntity(order));
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancelReservation(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return Result.success("预约已取消", null);
    }

    @GetMapping("/rules")
    public Result<Map<String, Object>> getReservationRules() {
        Map<String, Object> rules = reservationRulesProvider.getRules();
        return Result.success("success", rules);
    }
}
