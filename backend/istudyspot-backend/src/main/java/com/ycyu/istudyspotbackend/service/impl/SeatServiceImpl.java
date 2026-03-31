package com.ycyu.istudyspotbackend.service.impl;

import com.ycyu.istudyspotbackend.entity.Seat;
import com.ycyu.istudyspotbackend.entity.StudyRoom;
import com.ycyu.istudyspotbackend.mapper.SeatMapper;
import com.ycyu.istudyspotbackend.mapper.StudyRoomMapper;
import com.ycyu.istudyspotbackend.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SeatServiceImpl implements SeatService {

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private StudyRoomMapper studyRoomMapper;

    @Override
    public Map<String, Object> getSeatMap(Long roomId) {
        StudyRoom room = studyRoomMapper.findById(roomId);
        if (room == null) {
            throw new RuntimeException("自习室不存在");
        }

        List<Seat> seats = seatMapper.findByRoomId(roomId);

        // 计算最大行和列
        int maxRow = 0;
        int maxCol = 0;
        for (Seat seat : seats) {
            if (seat.getRowNum() != null && seat.getRowNum() > maxRow) {
                maxRow = seat.getRowNum();
            }
            if (seat.getColNum() != null && seat.getColNum() > maxCol) {
                maxCol = seat.getColNum();
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("studyRoomId", roomId);
        result.put("rows", maxRow);
        result.put("cols", maxCol);
        result.put("seats", seats);
        return result;
    }

    @Override
    public Seat getSeatDetail(Long id) {
        Seat seat = seatMapper.findById(id);
        if (seat == null) {
            throw new RuntimeException("座位不存在");
        }
        return seat;
    }

    @Override
    public Map<String, Object> getSeatTimeline(Long seatId, String date) {
        // TODO: 实现座位时段详情
        Map<String, Object> result = new HashMap<>();
        // 模拟返回数据
        return result;
    }

    @Override
    public Map<String, Object> calculatePrice(Long seatId, String startTime, String endTime) {
        // TODO: 实现价格计算
        Map<String, Object> result = new HashMap<>();
        result.put("totalAmount", new BigDecimal("20.00"));
        result.put("hours", 2);
        return result;
    }
}