package com.ycyu.istudyspotbackend.service;

import com.ycyu.istudyspotbackend.entity.Seat;
import java.util.List;
import java.util.Map;

public interface SeatService {
    List<Seat> getSeatStatus(Long roomId);
    Map<String, Object> calculatePrice(Long seatId, String startTime, String endTime);
    Map<String, Object> createOrder(Long userId, Long seatId, String startTime, String endTime);
}