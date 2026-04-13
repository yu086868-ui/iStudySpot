package com.ycyu.istudyspotbackend.controller;

import com.ycyu.istudyspotbackend.entity.Result;
import com.ycyu.istudyspotbackend.entity.Seat;
import com.ycyu.istudyspotbackend.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SeatController {

    @Autowired
    private SeatService seatService;

    @GetMapping("/studyrooms/{studyRoomId}/seats")
    public Result<List<Seat>> getSeatList(
            @PathVariable Long studyRoomId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer row,
            @RequestParam(required = false) Integer col) {
        List<Seat> seats = seatService.getSeatList(studyRoomId, status, type, row, col);
        return Result.success("success", seats);
    }

    @GetMapping("/seats/{id}")
    public Result<Seat> getSeatDetail(@PathVariable Long id) {
        Seat seat = seatService.getSeatDetail(id);
        return Result.success("success", seat);
    }
}