package com.ycyu.istudyspotbackend.mapper;

import com.ycyu.istudyspotbackend.entity.SeatLayoutItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SeatLayoutMapper {

    @Select("SELECT * FROM seat_layout_item WHERE room_id = #{roomId} ORDER BY z_index ASC, row_num ASC, col_num ASC, id ASC")
    List<SeatLayoutItem> findByRoomId(Long roomId);
}
