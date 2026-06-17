package com.ycyu.istudyspotbackend.mapper;

import com.ycyu.istudyspotbackend.entity.CheckInRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CheckInRecordMapper {

    @Insert("INSERT INTO check_in_record(user_id, reservation_id, study_room_id, seat_id, check_in_time, status) " +
            "VALUES(#{userId}, #{reservationId}, #{studyRoomId}, #{seatId}, NOW(), 'active')")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CheckInRecord record);

    @Select("SELECT * FROM check_in_record WHERE id = #{id}")
    CheckInRecord findById(Long id);

    @Select("SELECT * FROM check_in_record WHERE user_id = #{userId} AND status = 'active' LIMIT 1")
    CheckInRecord findActiveByUserId(@Param("userId") Long userId);

    @Update("UPDATE check_in_record SET check_out_time = NOW(), duration = #{duration}, status = 'completed' WHERE id = #{id}")
    int checkout(@Param("id") Long id, @Param("duration") Integer duration);

    @Select("SELECT * FROM check_in_record WHERE user_id = #{userId} ORDER BY check_in_time DESC")
    List<CheckInRecord> findByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM check_in_record WHERE user_id = #{userId} AND check_in_time >= #{startDate} AND check_in_time <= #{endDate} ORDER BY check_in_time DESC")
    List<CheckInRecord> findByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") String startDate, @Param("endDate") String endDate);
}
