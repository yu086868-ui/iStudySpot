package com.ycyu.istudyspotbackend.mapper;

import com.ycyu.istudyspotbackend.entity.Seat;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SeatMapper {

    @Select("SELECT * FROM seat WHERE room_id = #{roomId} AND status = 1")
    List<Seat> findByRoomId(Long roomId);

    @Select("SELECT * FROM seat WHERE id = #{id}")
    Seat findById(Long id);
}