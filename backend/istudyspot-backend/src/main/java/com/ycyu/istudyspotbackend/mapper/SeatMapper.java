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

    @Select("SELECT * FROM seat WHERE room_id = #{roomId} AND row_num = #{row} AND col_num = #{col}")
    Seat findByPosition(@Param("roomId") Long roomId, @Param("row") Integer row, @Param("col") Integer col);

    @Update("UPDATE seat SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}